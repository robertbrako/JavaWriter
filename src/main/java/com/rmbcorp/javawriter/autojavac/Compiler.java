package com.rmbcorp.javawriter.autojavac;

import com.rmbcorp.javawriter.BuildJob;

public interface Compiler {

    CompileResult compile(BuildJob buildJob) throws AutoJavacException;

    void setParams(JavacParams... params);
}
