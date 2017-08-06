package com.rmbcorp.javawriter.processor;

@FunctionalInterface
public interface ClazzProcessor<T> {

    ProcessResult writeOut(T clazz);

}
