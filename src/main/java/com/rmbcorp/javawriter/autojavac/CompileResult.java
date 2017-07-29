package com.rmbcorp.javawriter.autojavac;

import java.util.List;

class CompileResult {

    private String className = "";
    private List<CompileError> compileErrors;

    CompileResult(List<CompileError> errors) {
        compileErrors = errors;
    }

    String getClassName() {
        return className;
    }

    void setClassName(String className) {
        this.className = className;
    }

    List<CompileError> getCompileErrors() {
        return compileErrors;
    }
}
