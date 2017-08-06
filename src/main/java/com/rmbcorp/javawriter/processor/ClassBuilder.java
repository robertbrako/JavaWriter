package com.rmbcorp.javawriter.processor;

import com.rmbcorp.javawriter.clazz.ClazzError;

import java.util.ArrayList;
import java.util.List;

class ClassBuilder {

    private StringBuilder builder;
    private List<ClazzError> errorCache;
    private int lineNo = 1;
    private int importMark;
    private int paramTypeMark;
    private int variablesMark;
    private ProcUtil procUtil;

    ClassBuilder(ProcUtil procUtil) {
        this.procUtil = procUtil;
        builder = new StringBuilder("");
        errorCache = new ArrayList<>();
    }

    ClassBuilder append(String string) {
        builder.append(string);
        return this;
    }

    ClassBuilder append(char ch) {
        builder.append(ch);
        return this;
    }

    ClassBuilder appendln() {
        builder.append("\n");
        lineNo++;
        return this;
    }

    void addResult(ClazzError error) {
        errorCache.add(error);
    }

    List<ClazzError> getErrors() {
        return errorCache;
    }

    void reset() {
        builder = new StringBuilder("");
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    ClassBuilder markImport() {
        importMark = builder.length();
        return this;
    }

    void insertImport(String s) {
        int length = s.length();
        builder.insert(importMark, "import ").insert(importMark + 7, s).insert(importMark + 7 + length, ";\n");
        lineNo++;
    }

    void markType() {
        paramTypeMark = builder.length();
    }

    void insertType(String typeInfo) {
        if (paramTypeMark > 0) {
            builder.insert(paramTypeMark, typeInfo);
        }
    }

    ClassBuilder markVariables() {
        variablesMark = builder.length();
        return this;
    }

    void insertVariable(String visibility, String type, String name, int tabLevel) {
        builder.insert(variablesMark, ";\n") //line of text is constructed from right to left
                .insert(variablesMark, name)
                .insert(variablesMark, ' ')
                .insert(variablesMark, type)
                .insert(variablesMark, ' ')
                .insert(variablesMark, visibility)
                .insert(variablesMark, procUtil.tab(tabLevel));
        lineNo++;
    }

    void trimComma() {
        int length = builder.lastIndexOf(", ");
        if (length != -1) {
            builder.replace(length, length + 1, "");
        }
    }
}
