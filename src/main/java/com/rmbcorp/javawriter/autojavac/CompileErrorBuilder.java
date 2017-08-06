package com.rmbcorp.javawriter.autojavac;

import java.util.ArrayList;
import java.util.List;

class CompileErrorBuilder {

    private List<CompileError> compileErrors = new ArrayList<>();
    private List<String> errorCache = new ArrayList<>(3);

    CompileErrorBuilder() { }

    void acceptError(String line, String relativePath) {
        if (normalize(line).startsWith(normalize(relativePath)) && !errorCache.isEmpty()) {
            compileErrors.add(new CompileError(errorCache));
            errorCache.clear();
        }
        if (!line.startsWith("Note")) {
            errorCache.add(line);
        }
    }

    private String normalize(String line) {
        return line.replaceAll("\\\\", "/");
    }

    List<CompileError> flushAndGetCompileErrors() {
        if (!errorCache.isEmpty()) {
            compileErrors.add(new CompileError(errorCache));
        }
        return compileErrors;
    }
}
