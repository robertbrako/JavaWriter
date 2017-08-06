package com.rmbcorp.javawriter.autojavac;

public class AutoJavacException extends Exception {

    public static final String EMPTY_FILENAME = "JavacJob file name cannot be empty";

    public AutoJavacException(Exception e) {
        super(e);
    }

    public AutoJavacException(String message) {
        super(message);
    }
}
