/*
* Copyright 2016 by Robert M. Brako,
* All rights reserved.
*
* Permission is not granted to any person or entity to obtain a copy of this software and associated files
* (the "Software"), to deal in the Software, which includes without limitation any rights to use, copy, modify, merge,
* publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
* furnished to do so.  Permission may only be obtained by a signed and dated license agreement between licensor Robert
* Brako and prospective licensee.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
* WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
* COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.rmbcorp.javawriter.autojavac;

import com.rmbcorp.javawriter.BuildJob;
import com.rmbcorp.javawriter.logman.TempLogger;
import com.rmbcorp.util.StringUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JavaCompiler implements Compiler {

    private static final String OS_NAME = "os.name";
    public static final String SL = System.getProperty(OS_NAME).toLowerCase().contains("win") ? "\\" : "/";
    public static final String QT = System.getProperty(OS_NAME).toLowerCase().contains("win") ? "\"" : "";
    private static final String WIN_JAVA = "C:\\Program Files\\Java\\jdk1.8.0_121\\bin\\";
    private static final String UNIX_JAVA = "/opt/jdk1.8.0_111/bin/";

    private final TempLogger logger;
    private List<JavacParams> javacParams;
    private final String javaHome;

    public JavaCompiler(TempLogger logger) {
        this.logger = logger;
        javacParams = new ArrayList<>();
        javaHome = setJavaHome();
    }

    private String setJavaHome() {
        String home = System.getenv("JAVA_HOME");
        return home != null ? home + SL + "bin" + SL :
                System.getProperty(OS_NAME).toLowerCase().contains("win") ? WIN_JAVA : UNIX_JAVA;
    }

    @Override
    public CompileResult compile(BuildJob javacJob) throws AutoJavacException {
        if (StringUtil.isEmpty(javacJob.getFileName())) {
            throw new AutoJavacException(AutoJavacException.EMPTY_FILENAME);
        }
        try {
            mkdirs(javacJob.getRelativePath());
            mkdirs(javacJob.getBinPath());
            createFile(javacJob);
            List<CompileError> compileErrors = spawnJavac(javacJob);
            return saveErrors(javacJob, compileErrors);
        } catch (InterruptedException | IOException e) {
            throw new AutoJavacException(e);
        }
    }

    private boolean mkdirs(String folder) {
        File f = new File(getValidPath(folder));
        return f.exists() || f.mkdir();
    }

    private void createFile(BuildJob javacJob) throws IOException {
        if (!javacParams.contains(JavacParams.NO_CREATE_SRC_FILES)) {
            File f = new File(getValidPath(javacJob.getRelativePath()) + dotJava(javacJob.getFileName()));
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(javacJob.getFileContents().getBytes());
            }
        }
    }

    private String getValidPath(String relativePath) {
        return relativePath == null || relativePath.trim().isEmpty() ? "" :
                relativePath.endsWith("/") ? relativePath : relativePath + "/";
    }

    private List<CompileError> spawnJavac(BuildJob javacJob) throws IOException, InterruptedException {
        CompileErrorBuilder errorBuilder = new CompileErrorBuilder();
        String command = javaHome + "javac " +
                getValidCmdArg("-classpath ", javacJob.getClassPath()) +
                getValidCmdArg(" -d ", javacJob.getBinPath()) + " " +
                getValidPath(javacJob.getRelativePath()) +
                dotJava(javacJob.getFileName());
        logger.logPlain("[info]Executing: " + command);

        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command);
        try (
                BufferedReader bis = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
                BufferedReader ber = new BufferedReader(new InputStreamReader(proc.getErrorStream()))
        ) {
            String line;
            while ( (line = ber.readLine()) != null) {
                logger.logPlain("[error]" + line);
                errorBuilder.acceptError(line, javacJob.getRelativePath());
            }
            while ( (line = bis.readLine()) != null) {
                logger.logPlain("[debug]" + line);
            }
            logger.logPlain("[info]" + (proc.waitFor() == 0 ? "javac success" : "javac failure"));
        }
        return errorBuilder.flushAndGetCompileErrors();
    }

    private String getValidCmdArg(String arg, String classPath) {
        return StringUtil.isEmpty(classPath) ? "" : arg + classPath;
    }

    private String dotJava(String fileName) {
        return fileName.endsWith(".java") ? fileName : fileName.concat(".java");
    }

    private CompileResult saveErrors(BuildJob buildJob, List<CompileError> compileErrors) {
        CompileResult compileResult = new CompileResult(compileErrors);
        if (!javacParams.contains(JavacParams.NO_SAVE_ERRORS)) {
            compileResult.setClassName(buildJob.getFileName());
        }
        return compileResult;
    }

    @Override
    public void setParams(JavacParams... javacParams) {
        Collections.addAll(this.javacParams, javacParams);
    }

}
