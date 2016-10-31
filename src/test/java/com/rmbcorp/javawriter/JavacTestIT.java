package com.rmbcorp.javawriter;

import com.rmbcorp.javawriter.autojavac.AutoJavacException;
import com.rmbcorp.javawriter.autojavac.JavaCompiler;
import com.rmbcorp.javawriter.clazz.Clazz;
import com.rmbcorp.javawriter.clazz.ClazzImplManager;
import com.rmbcorp.javawriter.logman.LoggerManager;
import com.rmbcorp.javawriter.logman.TempLogger;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Created by rmbdev on 10/29/2016.
 */
public class JavacTestIT {

    private static final String FILE_NAME = "JunitOutput";
    private static final String RELATIVE_PATH = "src/test/gen";
    private static final String EXT = ".java";
    private static final String BIN_PATH = "src/test/bin";
    public static final String SL = "/";

    private File testFile;

    private TempLogger logger = new LoggerManager(false);
    private JavaCompiler javaCompiler = new JavaCompiler(logger);

    @Test public void minimalBuildJobTest() {
        testFile = new File(RELATIVE_PATH + SL + FILE_NAME + EXT);
        compile(new JavacJob(FILE_NAME, RELATIVE_PATH));
        assertTrue(testFile.exists());
    }

    private void compile(BuildJob buildJob) {
        try {
            javaCompiler.compile(buildJob);
        } catch (AutoJavacException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test public void emptyFileNameIsNoGoodTest() {
        testFile = new File(RELATIVE_PATH + SL + FILE_NAME + EXT);
        try {
            javaCompiler.compile(new JavacJob("", RELATIVE_PATH));
        } catch (AutoJavacException e) {
            assertTrue(e.getMessage().contains(AutoJavacException.EMPTY_FILENAME));
        }
        assertFalse(testFile.exists());
    }

    @Test public void emptyPathMakesFileInCurrentDirTest() {
        testFile = new File(FILE_NAME + EXT);
        compile(new JavacJob(FILE_NAME, ""));
        assertTrue(testFile.exists());
    }

    @Test public void pathWithOrWithoutSlashIsSame() {
        testFile = new File(RELATIVE_PATH + SL + FILE_NAME + EXT);
        compile(new JavacJob(FILE_NAME, RELATIVE_PATH));
        assertTrue(testFile.exists());
        testFile.delete();
        assertFalse(testFile.exists());
        compile(new JavacJob(FILE_NAME, RELATIVE_PATH + SL));
        assertTrue(testFile.exists()); //compare against same testFile obj even though this time we appended / to path
    }

    @Test public void fullCompileTest() {
        testFile = new File(RELATIVE_PATH + SL + FILE_NAME + EXT);
        ClazzImplManager clazzImplManager = ClazzImplManager.getInstance();
        Clazz clazz = setupClass(clazzImplManager.get("", FILE_NAME));
        BuildJob buildJob = new JavacJob(FILE_NAME, RELATIVE_PATH);
        buildJob.setBinPath(BIN_PATH);
        buildJob.setClassPath("\"" + System.getenv().get("USERPROFILE").replace("Aspire", "root") + "\\IdeaProjects\\JavaWriter\\target\\classes" + "\"");
        buildJob.setFileContents(clazzImplManager.writeOut(clazz));
        compile(buildJob);

        File binary = new File(BIN_PATH + SL + FILE_NAME + ".class");
        assertTrue(binary.exists());
        binary.delete();
    }

    private Clazz setupClass(Clazz clazz) {
        clazz.addImplementations(Collections.<Class>singletonList(Runnable.class));
        clazz.setClassType(Clazz.ClassType.CLASS);//todo: need to make this required for construction
        return clazz;
    }

    @After public void cleanup() {
        try {
            testFile.delete();
        } catch (SecurityException ignored) {
        }
    }
}
