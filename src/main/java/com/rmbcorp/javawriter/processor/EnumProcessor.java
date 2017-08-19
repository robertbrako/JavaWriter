package com.rmbcorp.javawriter.processor;

import com.rmbcorp.javawriter.clazz.Clazz;
import com.rmbcorp.javawriter.clazz.EnumReadable;

import java.util.HashSet;
import java.util.stream.Collectors;

final class EnumProcessor implements ClazzProcessor<EnumReadable> {

    private final ClassStarter classStarter;
    private final ProcUtil procUtil;
    private final ClassBuilder builder;

    EnumProcessor(ClassStarter classStarter, ProcUtil procUtil) {
        this.classStarter = classStarter;
        this.procUtil = procUtil;
        builder = new ClassBuilder(procUtil);
    }

    @Override
    public ProcessResult writeOut(EnumReadable clazz) {
        builder.reset();
        classStarter.buildHeader(clazz.getPackagePath(), builder);
        buildEnumHeader(clazz);
        buildEnumConstants(clazz);
        classStarter.buildImports(new HashSet<>(), builder);
        builder.append('}').appendln();
        return new ProcessResult(builder.toString(), builder.getErrors());
    }

    private void buildEnumHeader(EnumReadable clazz) {
        Clazz.Visibility visibility = clazz.getVisibility();
        if (!Clazz.Visibility.PACKAGE.equals(visibility)) {
            builder.append(visibility.toString()).append(' ');
        }
        builder.append("enum ").append(clazz.getClassName());
        builder.append(" {").appendln();
    }

    private void buildEnumConstants(EnumReadable clazz) {
        builder.append(procUtil.tab(1));
        builder.append(clazz.getEnumConstants().stream().map(JavaKeywords::replaceJavaKeyword).collect(Collectors.joining(", ")));
        builder.appendln();
    }
}