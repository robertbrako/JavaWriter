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
package com.rmbcorp.javawriter;

class JavacJob implements BuildJob {
    private String fileName;
    private String packageName = "";
    private String relativePath;
    private String fileContents = "";
    private String classPath = "";
    private String binPath = "";

    JavacJob(String fileName, String relativePath) {
        this.fileName = fileName != null ? fileName : "";
        this.relativePath = relativePath != null ? relativePath : "";
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public String getFileContents() {
        return fileContents;
    }

    @Override
    public void setFileContents(String fileContents) {
        this.fileContents = fileContents;
    }

    @Override
    public String getClassPath() {
        return classPath;
    }

    @Override
    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    @Override
    public String getBinPath() {
        return binPath;
    }

    @Override
    public void setBinPath(String binPath) {
        this.binPath = binPath;
    }
}
