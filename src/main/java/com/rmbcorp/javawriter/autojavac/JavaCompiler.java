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

    private final TempLogger logger;
    private List<JavacParams> javacParams;

    public JavaCompiler(TempLogger logger) {
        this.logger = logger;
        javacParams = new ArrayList<>();
    }

    @Override
    public void compile(BuildJob javacJob) throws AutoJavacException {
        if (StringUtil.isEmpty(javacJob.getFileName())) {
            throw new AutoJavacException(AutoJavacException.EMPTY_FILENAME);
        }
        try {
            mkdirs(javacJob.getRelativePath());
            mkdirs(javacJob.getBinPath());
            createFile(javacJob);
            spawnJavac(javacJob);
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
            File f = new File(getValidPath(javacJob.getRelativePath()) + javacJob.getFileName() + ".java");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(javacJob.getFileContents().getBytes());
            fos.close();
        }
    }

    private String getValidPath(String relativePath) {
        return relativePath == null || relativePath.trim().isEmpty() ? "" :
                relativePath.endsWith("/") ? relativePath : relativePath + "/";
    }

    private void spawnJavac(BuildJob javacJob) throws IOException, InterruptedException {
        String command = "javac " +
                getValidCmdArg("-classpath ", javacJob.getClassPath()) +
                getValidCmdArg(" -d ", javacJob.getBinPath()) + " " +
                getValidPath(javacJob.getRelativePath()) +
                javacJob.getFileName() + ".java";
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
            }
            while ( (line = bis.readLine()) != null) {
                logger.logPlain("[debug]" + line);
            }
            logger.logPlain("[info]" + (proc.waitFor() == 0 ? "javac success" : "javac failure"));
        }
    }

    private String getValidCmdArg(String arg, String classPath) {
        return StringUtil.isEmpty(classPath) ? "" : arg + classPath;
    }

    @Override
    public void setParams(JavacParams... javacParams) {
        Collections.addAll(this.javacParams, javacParams);
    }
}
