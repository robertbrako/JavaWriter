package com.rmbcorp.javawriter;

/**
 * Created by rmbdev on 10/29/2016.
 */
public interface BuildJob {

    String getFileName();

    void setFileName(String fileName);

    String getRelativePath();

    String getFileContents();

    void setFileContents(String fileContents);

    String getClassPath();

    void setClassPath(String classPath);

    String getBinPath();

    void setBinPath(String binPath);
}
