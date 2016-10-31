package com.rmbcorp.javawriter;

import com.rmbcorp.javawriter.autojavac.AutoJavacException;
import com.rmbcorp.javawriter.autojavac.Compiler;
import com.rmbcorp.javawriter.autojavac.JavaCompiler;
import com.rmbcorp.javawriter.clazz.Clazz;
import com.rmbcorp.javawriter.clazz.ClazzImplManager;
import com.rmbcorp.javawriter.logman.LoggerManager;
import com.rmbcorp.javawriter.logman.TempLogger;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**Need to produce classes on the fly somehow...
 * Created by rmbdev on 9/5/2016.
 */
class JavaWriter {

    public static final String DEFAULT_FILENAME = "default";
    private static final String SL = "\"";

    private static TempLogger logger;
    private static Compiler compiler;
    private static ClazzImplManager clazzManager;
    private static boolean debugEnv = false;
    private static boolean useSout = true;

    public static void main(String[] args) {
        logger = new LoggerManager(useSout);
        compiler = new JavaCompiler(logger);
        clazzManager = ClazzImplManager.getInstance();

        JavacJob buildJob = getJavacJob();
        try {
            compiler.compile(buildJob);
        } catch (AutoJavacException ignored) {
        }
        checkDebug();
    }

    private static JavacJob getJavacJob() {
        JavacJob buildJob = new JavacJob("", Compiler.GEN_FOLDER);
        buildJob.setClassPath(SL + System.getenv().get("USERPROFILE") + "\\IdeaProjects\\JavaWriter\\target\\classes" + SL);
        buildJob.setBinPath(Compiler.BIN_FOLDER);

        String filename = buildJob.getFileName();
        if (filename.isEmpty()) {
            filename = getFilename();
        }
        buildJob.setFileName(filename);
        Clazz clazz = getClazz(clazzManager, "com.rmbcorp.javawriter.clazz", filename);
        buildJob.setFileContents(clazzManager.writeOut(clazz));
        return buildJob;
    }

    private static String getFilename() {
        String filename;
        logger.logPlain("name (omit .java please; it will be added)? ");
        try (BufferedReader bin = new BufferedReader(new InputStreamReader(System.in)) ) {
            filename = bin.readLine();
        } catch (IOException e) {
            filename = DEFAULT_FILENAME;
        }
        return filename;
    }

    private static Clazz getClazz(ClazzImplManager clazzManager, String packageName, String filename) {
        Clazz clazz = clazzManager.get(packageName, filename);
        clazz.setClassType(Clazz.ClassType.CLASS);
        clazz.addImports(Arrays.<Class>asList(Integer.class, String.class, String.class));
        clazz.setVisibility(Clazz.Visibility.PUBLIC);
        clazz.setFinal(true);
        clazz.addImplementations(Collections.<Class>singletonList(Clazz.class));
        return clazz;
    }

    private static void checkDebug() {
        if (debugEnv) {
            Map<String, String> stuff = System.getenv();
            for (Map.Entry item : stuff.entrySet()) {
                logger.logPlain(item.getKey() + " : " + item.getValue());
            }
        }
    }
}