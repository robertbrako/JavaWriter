package com.rmbcorp.javawriter;

import com.rmbcorp.javawriter.processor.ClazzProcessor;
import com.rmbcorp.javawriter.processor.ProcessorProvider;
import com.rmbcorp.util.argparser.ArgParser;
import com.rmbcorp.util.argparser.ParserProv;
import com.rmbcorp.javawriter.autojavac.AutoJavacException;
import com.rmbcorp.javawriter.autojavac.Compiler;
import com.rmbcorp.javawriter.autojavac.JavaCompiler;
import com.rmbcorp.javawriter.clazz.Clazz;
import com.rmbcorp.javawriter.clazz.Clazz.Visibility;
import com.rmbcorp.javawriter.clazz.ClazzImplManager;
import com.rmbcorp.javawriter.logman.LoggerManager;
import com.rmbcorp.javawriter.logman.TempLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**Need to produce classes on the fly somehow...
 * Created by rmbdev on 9/5/2016.
 */
class JavaWriter {

    public static final String DEFAULT_FILENAME = "default";

    enum JavacOpts { CLASSPATH, D }
    enum JWOpts implements ArgParser.HasDefault {
        DEBUGENV, USESOUT("false"), FILENAME, PACKAGE, GEN(Compiler.GEN_FOLDER), CLASSTYPE("class"), VISIBILITY(Visibility.PACKAGE.toString());

        private final String defaultVal;

        JWOpts() { this.defaultVal = ""; }

        JWOpts(String defaultVal) {
            this.defaultVal = defaultVal;
        }

        @Override
        public String getDefault() {
            return defaultVal;
        }
    }

    private static final Logger jLogger = Logger.getGlobal();
    private static TempLogger logger;
    private static Compiler compiler;
    private static ClazzProcessor clazzProcessor;
    private static ClazzImplManager clazzManager;
    private static ArgParser argParser;
    private static boolean debugEnv = false;
    private static boolean useSout;

    public static void main(String[] args) {
        argParser = new ParserProv().getDefault();
        Map<JWOpts, String> jwOptions = argParser.getArgs(args, "-", JWOpts.class);
        Map<JavacOpts, String> javacOptsMap = argParser.getArgs(args, "-", JavacOpts.class);
        debugEnv = Boolean.parseBoolean(jwOptions.get(JWOpts.DEBUGENV));
        useSout = Boolean.parseBoolean(jwOptions.get(JWOpts.USESOUT));
        logger = new LoggerManager(useSout);
        compiler = new JavaCompiler(logger);
        clazzManager = ClazzImplManager.getInstance();
        clazzProcessor = ProcessorProvider.get(ProcessorProvider.CLAZZIMPL);

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
        Clazz clazz = getClazz(clazzManager, jwOptions);
        buildJob.setFileContents(clazzProcessor.writeOut(clazz));
        return buildJob;
    }

    private static String getFilename() {
        String filename;
        logger.logPlain("filename? (omit .java please; it will be added): ");
        try (BufferedReader bin = new BufferedReader(new InputStreamReader(System.in)) ) {
            filename = bin.readLine();
        } catch (IOException e) {
            jLogger.log(Level.INFO, e.getLocalizedMessage(), e);
            filename = DEFAULT_FILENAME;
        }
        return filename;
    }

    private static Clazz getClazz(ClazzImplManager clazzManager, Map<JWOpts, String> jwOptions) {
        Clazz clazz = clazzManager.get(jwOptions.get(JWOpts.PACKAGE), jwOptions.get(JWOpts.FILENAME));
        clazz.setClassType(Clazz.ClassType.valueOf(jwOptions.get(JWOpts.CLASSTYPE).toUpperCase()));
        try {
            clazz.setVisibility(Visibility.valueOf(jwOptions.get(JWOpts.VISIBILITY).toUpperCase()));
        } catch (IllegalArgumentException e) {
            clazz.setVisibility(Visibility.PACKAGE);
            jLogger.log(Level.INFO, e.getLocalizedMessage(), e);
        }
        clazz.addImports(Arrays.<Class>asList(Integer.class, String.class, String.class));
        clazz.setFinal(true);
        clazz.addImplementations(Collections.<Class>singletonList(List.class));
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