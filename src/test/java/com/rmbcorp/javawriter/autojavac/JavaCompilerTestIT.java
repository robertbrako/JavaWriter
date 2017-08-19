package com.rmbcorp.javawriter.autojavac;

import com.rmbcorp.javawriter.BuildJob;
import com.rmbcorp.javawriter.SampleInterface;
import com.rmbcorp.javawriter.clazz.*;
import com.rmbcorp.javawriter.logman.LoggerManager;
import com.rmbcorp.javawriter.processor.ClazzProcessor;
import com.rmbcorp.javawriter.processor.ProcessResult;
import com.rmbcorp.javawriter.processor.ProcessorProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rmbcorp.javawriter.autojavac.JavaCompiler.QT;
import static com.rmbcorp.javawriter.autojavac.JavaCompiler.SL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class JavaCompilerTestIT {

    private static final String RELATIVE_PATH = "src/test/gen";
    private static final int RANDOM_CLASSES = 9;
    private static final String BIN_PATH = "src/test/bin";

    private Compiler javaCompiler;
    private ClazzProcessor<ClazzReadable> processor;
    private List<File> testFiles = new ArrayList<>();
    private List<Class> testClasses = new ArrayList<>();

    @Before
    public void setUp() {
        javaCompiler = new JavaCompiler(new LoggerManager(false));
        processor = ProcessorProvider.getClazzProcessor();
    }

    @Test
    public void testSetParams() throws AutoJavacException {
        String fileName = "int";
        setupDeletableFile(fileName);

        BuildJob job = BuildJob.get(fileName, RELATIVE_PATH);
        ClazzImpl clazz = new ClazzImpl("", fileName);
        clazz.addBeanVariable(new JVariable("classId", String.class));
        ProcessResult result = processor.writeOut(clazz);
        assertTrue(result.getErrorCache().contains(ClazzError.INVALID_CLASS_NAME));

        job.setFileContents(result.getContents());
        job.setBinPath(BIN_PATH);

        javaCompiler.setParams(JavacParams.NO_SAVE_ERRORS);
        CompileResult compileResult = javaCompiler.compile(job);
        String firstError = compileResult.getCompileErrors().stream()
                .map(CompileError::getReason).findFirst().orElse("");

        assertTrue(firstError.matches(".*identifier.*expected"));
        assertTrue(compileResult.getClassName().isEmpty());
        assertEquals(2, compileResult.getCompileErrors().size());
    }

    private void setupDeletableFile(String fileName) {
        testFiles.add(new File(RELATIVE_PATH, fileName + ".java"));
    }

    @Test public void localClassCompiles() throws AutoJavacException {
        String className = "ReadableImpl";
        setupDeletableFile(className);
        ClazzImpl clazz = new ClazzImpl("", className);
        clazz.addImplementations(Arrays.asList(Clazz.class, ClazzReadable.class));
        BuildJob buildJob = BuildJob.get(className, RELATIVE_PATH);
        buildJob.setFileContents(processor.writeOut(clazz).getContents());
        buildJob.setClassPath(QT + getPwd() + "target" + SL + "classes" + QT);
        buildJob.setBinPath(BIN_PATH);
        CompileResult compileResult = javaCompiler.compile(buildJob);
        assertEquals(0, compileResult.getCompileErrors().size());
    }

    @Test
    public void superTest() throws AutoJavacException, ClassNotFoundException {
        testClasses = Arrays.asList(Compiler.class, Clazz.class, ClazzReadable.class,
                Class.forName("com.rmbcorp.javawriter.processor.BeanProcessor"), BuildJob.class,
                JavacParams.class, Collection.class, List.class, Supplier.class);
        List<BuildJob> jobs = Stream.iterate(1, e -> e + 1)
                .limit(RANDOM_CLASSES)
                .map(this::getBuildJob)
                .collect(Collectors.toList());

        jobs.stream().parallel().map(job -> {
            try {
                return autoResolve(job);
            } catch (AutoJavacException e) {
                return new CompileResult(Collections.singletonList(new CompileError(Collections.nCopies(1, "AutoJavacException"))));
            }
        }).filter(Objects::nonNull).forEach((compileResult) ->
            assertEquals("File in error: " + compileResult.getClassName(), 0, compileResult.getCompileErrors().size()));
    }

    private BuildJob getBuildJob(Integer index) {
        String fileName = "file" + index;
        setupDeletableFile(fileName);
        BuildJob job = BuildJob.get(fileName, RELATIVE_PATH);
        ClazzImpl clazz = new ClazzImpl("", fileName);
        Class aClass = testClasses.get(index - 1);
        if (aClass.isInterface()) {
            clazz.addImplementations(Collections.singletonList(aClass));
            clazz.addImports(Collections.singletonList(Clazz.class));
        } else {
            clazz.addExtension(aClass);
            clazz.addImports(Collections.singletonList(aClass));
        }
        clazz.addBeanVariable(new JVariable("classId", String.class));
        String fileContents = processor.writeOut(clazz).getContents();

        job.setFileContents(fileContents);
        job.setBinPath(BIN_PATH);
        job.setClassPath(QT + getPwd() + "target" + SL + "classes" + QT);
        return job;
    }

    private String getPwd() {
        String pwd = System.getenv().get("PWD");
        return pwd == null ? "" : pwd + SL;
    }

    private CompileResult autoResolve(BuildJob job) throws AutoJavacException {
        CompileResult compileResult = javaCompiler.compile(job);
        if (compileResult.getCompileErrors().isEmpty()) {
            return compileResult;
        }
        Pattern pattern1 = Pattern.compile(".* is not public in (.*);.*");
        Set<String> newPackageName = compileResult.getCompileErrors().stream()
                .map(error -> pattern1.matcher(error.getReason()))
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1))
                .collect(Collectors.toSet());
        if (newPackageName.size() == 1) {
            String packageName = newPackageName.iterator().next();
            job.setPackageName(packageName);
            job.setFileContents(job.getFileContents().replaceFirst("(import\\s.*)", "package " + packageName + ";\n\n$1"));
            compileResult = javaCompiler.compile(job);
        }
        return compileResult;
    }

    @Test
    public void makeJettyServer() throws Exception {
        String mvnRepo = System.getenv("M2_REPO");
        if (mvnRepo == null) {
            System.err.println("Test 'makeJettyServer' requires env M2_REPO (something like ~/.m2)");
            return;
        }
        String fileName = "FooServlet";
        setupDeletableFile(fileName);
        BuildJob job = BuildJob.get(fileName, RELATIVE_PATH);
        String packagePath = "";
        ClazzImpl clazz = new ClazzImpl(packagePath, fileName);

        String jarPath = Paths.get(mvnRepo, "repository/javax/servlet/javax.servlet-api/3.1.0/javax.servlet-api-3.1.0.jar").toString();
        List<Class> httpClasses = getHttpClasses(jarPath);
        clazz.setVisibility(Clazz.Visibility.PUBLIC);
        clazz.addImports(httpClasses);
        clazz.addExtension(httpClasses.stream().filter(clz -> clz.getName().endsWith("HttpServlet")).findFirst().orElse(Object.class));
        clazz.addMethod(new JMethod("getClassId", String.class, Clazz.Visibility.PUBLIC, packagePath));
        String fileContents = processor.writeOut(clazz).getContents();

        job.setFileContents(fileContents);
        job.setBinPath(BIN_PATH);
        job.setClassPath(QT + jarPath + QT);

        int errs = javaCompiler.compile(job).getCompileErrors().size();
        assertTrue(new File(BIN_PATH, fileName + ".class").exists());
        assertEquals(0, errs);
    }

    private List<Class> getHttpClasses(String jarResourcePath) throws IOException, ClassNotFoundException {
        URL[] urls = { new URL("jar:file:" + jarResourcePath +"!/") };
        URLClassLoader child = new URLClassLoader(urls, this.getClass().getClassLoader());
        return new JarFile(jarResourcePath).stream()
                .filter(entry -> !entry.isDirectory() && entry.getName().endsWith(".class"))
                .map(entry -> entry.getName().replace(".class", "").replaceAll("/", "."))
                .filter(entry -> Arrays.asList("javax.servlet.http.HttpServlet", "javax.servlet.ServletException",
                        "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse",
                        "javax.servlet.ServletRequest", "javax.servlet.ServletResponse")
                        .contains(entry))
                .map(entry -> toClass(child, entry))
                .collect(Collectors.toList());
    }

    private Class<?> toClass(URLClassLoader child, String entry)  {
        try {
            return Class.forName(entry, false, child);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @After
    public void cleanup() {
        try {
            testFiles.stream().filter(File::exists).forEach(File::delete);
            Stack<File> fileStack = new Stack<>();
            File testBin = new File(BIN_PATH);
            Arrays.stream(testBin.listFiles()).forEach(file -> deleteAll(fileStack, file));
            boolean deletedAll = true;
            while (!fileStack.empty()) {
                deletedAll = deletedAll && fileStack.pop().delete();
            }
        } catch (SecurityException ignored) {
        }
    }

    private void deleteAll(Stack<File> fileStack, File currentFile) {
        if (!currentFile.delete()) {
            fileStack.push(currentFile);
            for (File file : currentFile.listFiles()) {
                deleteAll(fileStack, file);
            }
        }
    }
}