package com.rmbcorp.javawriter.processor;

import com.rmbcorp.javawriter.clazz.ClazzReadable;
import com.rmbcorp.javawriter.clazz.EnumReadable;

public class ProcessorProvider {

    private static ProcUtil procUtil = new ProcUtil();

    private ProcessorProvider() { }

    public static ClazzProcessor<ClazzReadable> getBeanProcessor() {
        ClassStarter classStarter = new ClassStarter(procUtil);
        return new BeanProcessor(classStarter, procUtil);
    }

    public static ClazzProcessor<ClazzReadable> getClazzProcessor() {
        ClassStarter classStarter = new ClassStarter(procUtil);
        return new ClazzImplProcessor(classStarter, procUtil);
    }

    public static ClazzProcessor<EnumReadable> getEnumProcessor() {
        ClassStarter classStarter = new ClassStarter(procUtil);
        return new EnumProcessor(classStarter, procUtil);
    }
}
