package com.rmbcorp.javawriter;

import com.rmbcorp.javawriter.clazz.ClazzImpl;
import com.rmbcorp.javawriter.processor.ClazzProcessor;
import com.rmbcorp.javawriter.processor.ProcessorProvider;
import com.rmbcorp.util.argparser.ArgParser;
import com.rmbcorp.javawriter.autojavac.AutoJavacException;
import com.rmbcorp.javawriter.autojavac.Compiler;
import com.rmbcorp.javawriter.autojavac.JavaCompiler;
import com.rmbcorp.javawriter.clazz.Clazz;
import com.rmbcorp.javawriter.clazz.Clazz.Visibility;
import com.rmbcorp.javawriter.logman.LoggerManager;
import com.rmbcorp.javawriter.logman.TempLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**Need to produce classes on the fly somehow...
 * Created by rmbdev on 9/5/2016.
 */
class JavaWriter {

    static final String GEN_FOLDER = "src/gen/";
    private static final String DEFAULT_FILENAME = "default";

    enum JavacOpts { CLASSPATH, D }
    enum JWOpts implements Supplier<String> {
        DEBUGENV, USESOUT("false"), FILENAME, PACKAGE, GEN(GEN_FOLDER), CLASSTYPE("class"), VISIBILITY(Visibility.PACKAGE.toString());

        private final String defaultVal;

        JWOpts() { this.defaultVal = ""; }

        JWOpts(String defaultVal) {
            this.defaultVal = defaultVal;
        }

        @Override
        public String get() {
            return defaultVal;
        }
    }

    private static final Logger jLogger = Logger.getGlobal();
    private static TempLogger logger;
    private static ClazzProcessor clazzProcessor;
    private static boolean debugEnv = false;

    public static void main(String[] args) {
        ArgParser argParser = new ArgParser();
        Map<JWOpts, String> jwOptions = argParser.getArgs(args, "-", JWOpts.class);
        Map<JavacOpts, String> javacOptsMap = argParser.getArgs(args, "-", JavacOpts.class);
        debugEnv = Boolean.parseBoolean(jwOptions.get(JWOpts.DEBUGENV));
        boolean useSout = Boolean.parseBoolean(jwOptions.get(JWOpts.USESOUT));
        logger = new LoggerManager(useSout);
        Compiler compiler = new JavaCompiler(logger);
        clazzProcessor = ProcessorProvider.getClazzProcessor();

        JavacJob buildJob = getJavacJob(jwOptions, javacOptsMap);
        try {
            compiler.compile(buildJob);
        } catch (AutoJavacException ignored) {
            jLogger.log(Level.SEVERE, ignored.getLocalizedMessage(), ignored);
        }
        checkDebug();
    }

    private static JavacJob getJavacJob(Map<JWOpts, String> jwOptions, Map<JavacOpts, String> javacOptsMap) {
        JavacJob buildJob = new JavacJob(jwOptions.get(JWOpts.FILENAME), jwOptions.get(JWOpts.GEN));
        buildJob.setClassPath(javacOptsMap.get(JavacOpts.CLASSPATH));
        buildJob.setBinPath(javacOptsMap.get(JavacOpts.D));

        String filename = buildJob.getFileName();
        if (filename.isEmpty()) {
            filename = getFilename();
        }
        logger.logPlain("[info]Filename:" + filename);
        buildJob.setFileName(filename);
        jwOptions.put(JWOpts.FILENAME, filename);
        Clazz clazz = getClazz(jwOptions);
        buildJob.setFileContents(clazzProcessor.writeOut(clazz).getContents());
        return buildJob;
    }

    private static String getFilename() {
        String filename;
        logger.logPlain("filename? : ");
        try (BufferedReader bin = new BufferedReader(new InputStreamReader(System.in)) ) {
            filename = bin.readLine();
        } catch (IOException e) {
            jLogger.log(Level.INFO, e.getLocalizedMessage(), e);
            filename = DEFAULT_FILENAME;
        }
        return filename;
    }

    private static Clazz getClazz(Map<JWOpts, String> jwOptions) {
        Clazz clazz = new ClazzImpl(jwOptions.get(JWOpts.PACKAGE), jwOptions.get(JWOpts.FILENAME));
        clazz.setClassType(Clazz.ClassType.valueOf(jwOptions.get(JWOpts.CLASSTYPE).toUpperCase()));
        try {
            clazz.setVisibility(Visibility.valueOf(jwOptions.get(JWOpts.VISIBILITY).toUpperCase()));
        } catch (IllegalArgumentException e) {
            clazz.setVisibility(Visibility.PACKAGE);
            jLogger.log(Level.INFO, e.getLocalizedMessage(), e);
        }
        clazz.addImports(Arrays.asList(Integer.class, String.class, String.class));
        clazz.setFinal(true);
        clazz.addImplementations(Collections.singletonList(List.class));
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