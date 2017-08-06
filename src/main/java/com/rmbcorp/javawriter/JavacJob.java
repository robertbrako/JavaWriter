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
