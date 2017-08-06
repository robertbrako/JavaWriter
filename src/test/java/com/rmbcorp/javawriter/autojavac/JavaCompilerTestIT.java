package com.rmbcorp.javawriter.autojavac;

import com.rmbcorp.javawriter.BuildJob;
import com.rmbcorp.javawriter.clazz.*;
import com.rmbcorp.javawriter.logman.LoggerManager;
import com.rmbcorp.javawriter.processor.ClazzProcessor;
import com.rmbcorp.javawriter.processor.ProcessResult;
import com.rmbcorp.javawriter.processor.ProcessorProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


public class JavaCompilerTestIT {

    private static final String RELATIVE_PATH = "src/test/gen";

    private Compiler javaCompiler;
    private ClazzProcessor<ClazzReadable> processorProvider;
    private List<File> testFiles = new ArrayList<>();

    @Before
    public void setUp() {
        javaCompiler = new JavaCompiler(new LoggerManager(false));
        processorProvider = ProcessorProvider.getBeanProcessor();
    }

    @Test
    public void testSetParams() throws AutoJavacException {
        String fileName = "int";
        testFiles.add(new File(RELATIVE_PATH, fileName + ".java"));
        testFiles.add(new File(RELATIVE_PATH, fileName + ".class"));

        BuildJob job = BuildJob.get(fileName, RELATIVE_PATH);
        ClazzImpl clazz = new ClazzImpl("", fileName);
        clazz.addBeanVariable(new JVariable("classId", String.class));
        ProcessResult result = processorProvider.writeOut(clazz);
        assertTrue(result.getErrorCache().contains(ClazzError.INVALID_CLASS_NAME));

        job.setFileContents(result.getContents());
        job.setBinPath("src/test/bin");

        javaCompiler.setParams(JavacParams.NO_SAVE_ERRORS);
        CompileResult compileResult = javaCompiler.compile(job);
        String firstError = compileResult.getCompileErrors().stream()
                .map(CompileError::getReason).findFirst().get();

        assertTrue(firstError.matches(".*identifier.*expected"));
        assertTrue(compileResult.getClassName().isEmpty());
    }

    @After
    public void cleanup() {
        try {
            testFiles.stream().filter(File::exists).forEach(File::delete);
        } catch (SecurityException ignored) {
        }
    }
}