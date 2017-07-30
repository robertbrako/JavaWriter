package com.rmbcorp.javawriter.processor;

import com.rmbcorp.javawriter.clazz.Clazz;
import com.rmbcorp.javawriter.clazz.ClazzError;
import com.rmbcorp.javawriter.clazz.EnumReadable;
import com.rmbcorp.util.ValidationManager;

import java.util.HashSet;
import java.util.stream.Collectors;

final class EnumProcessor implements ClazzProcessor<EnumReadable> {

    private final ValidationManager<ClazzError> validator;
    private final ClassStarter classStarter;
    private final ProcUtil procUtil;
    private final StringBuilder builder = new StringBuilder();
    private String errorCache = "";

    EnumProcessor(ValidationManager<ClazzError> validator, ClassStarter classStarter, ProcUtil procUtil) {
        this.validator = validator;
        this.classStarter = classStarter;
        this.procUtil = procUtil;
    }

    @Override
    public boolean hasError(ClazzError error) {
        return errorCache.contains(error.toString());
    }

    @Override
    public String writeOut(EnumReadable clazz) {
        String out = doWriteOut(clazz);
        if (validator.hasErrors()) {
            errorCache = ((ClazzValidator)validator).getErrorsAsCSV();
            validator.removeAllResults();
        }
        return out;
    }

    private String doWriteOut(EnumReadable clazz) {
        classStarter.buildHeader(clazz.getPackagePath(), builder);
        buildEnumHeader(clazz);
        buildEnumConstants(clazz);
        classStarter.buildImports(new HashSet<>(), builder);
        builder.append('}').append(procUtil.ONE_LINE);
        return builder.toString();
    }

    private void buildEnumHeader(EnumReadable clazz) {
        Clazz.Visibility visibility = clazz.getVisibility();
        if (!Clazz.Visibility.PACKAGE.equals(visibility)) {
            builder.append(visibility.toString()).append(' ');
        }
        builder.append("enum ").append(clazz.getClassName());
        builder.append(" {").append(procUtil.ONE_LINE);
    }

    private void buildEnumConstants(EnumReadable clazz) {
        builder.append(procUtil.tab(1));
        builder.append(clazz.getEnumConstants().stream().map(JavaKeywords::replaceJavaKeyword).collect(Collectors.joining(", ")));
        builder.append(procUtil.ONE_LINE);
    }
}