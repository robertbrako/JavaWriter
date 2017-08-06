package com.rmbcorp.javawriter.processor;

import com.rmbcorp.javawriter.clazz.ClazzError;

import java.util.List;

public class ProcessResult {
    private String contents;
    private List<ClazzError> errorCache;

    ProcessResult(String contents, List<ClazzError> errorCache) {
        this.contents = contents;
        this.errorCache = errorCache;
    }

    public String getContents() {
        return contents;
    }

    public List<ClazzError> getErrorCache() {
        return errorCache;
    }
}
