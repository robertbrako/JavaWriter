/*
* Copyright 2017 by Robert M. Brako,
* All rights reserved.
*
* Permission is not granted to any person or entity to obtain a copy of this software and associated files
* (the "Software"), to deal in the Software, which includes without limitation any rights to use, copy, modify, merge,
* publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
* furnished to do so.  Permission may only be obtained by a signed and dated license agreement between licensor Robert
* Brako and prospective licensee.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
* WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
* COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.rmbcorp.javawriter.processor;

import com.rmbcorp.javawriter.clazz.*;
import com.rmbcorp.util.StringUtil;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ClassStarter {

    private static final String TYPE_PLACEHOLDER = "%%TYPE%%";
    private static final Pattern typedClass = Pattern.compile("\\w+(<.*>)");
    private final ClazzValidator validator;
    private final ProcUtil procUtil;

    ClassStarter(ClazzValidator validator, ProcUtil procUtil) {
        this.validator = validator;
        this.procUtil = procUtil;
    }

    void buildClassOrInterface(StringBuilder builder, Clazz.ClassType classType, ClazzReadable clazz) {
        String className = clazz.getClassName();
        if (StringUtil.isEmpty(className)) {
            validator.addResult(ClazzError.CANNOT_HAVE_EMPTY_CLASS_NAME);
        }
        if (!className.equals(JavaKeywords.replaceJavaKeywordSafe(className))) {
            validator.addResult(ClazzError.INVALID_CLASS_NAME);
        }
        if (Clazz.ClassType.INTERFACE.equals(classType)) {
            buildInterface(builder, clazz.getVisibility(), className);
        } else if (Clazz.ClassType.CLASS.equals(classType)) {
            buildClass(builder, className, clazz);
        } else {
            validator.addResult(ClazzError.MUST_BE_CLASS_OR_INTERFACE);
        }
    }

    private void buildClass(StringBuilder builder, String className, ClazzReadable clazz) {
        boolean isFinal = clazz.isFinal();
        boolean isAbstract = clazz.isAbstract();
        if (isFinal && isAbstract) {
            validator.addResult(ClazzError.CANNOT_BE_ABSTRACT_AND_FINAL);
            return;
        }
        Clazz.Visibility visibility = clazz.getVisibility();
        Class extension = clazz.getExtension();

        builder.append(visibility.toString()).append(' ');
        builder.append(isFinal ? "final " : isAbstract ? "abstract " : "");
        builder.append("class ").append(className).append(TYPE_PLACEHOLDER);
        if (extension != null) {
            builder.append(" extends ");
            builder.append(procUtil.getClassSimpleName(extension.toString()));
        }
    }

    private void buildInterface(StringBuilder builder, Clazz.Visibility visibility, String className) {
        if (Clazz.Visibility.PUBLIC.equals(visibility)) {
            builder.append(visibility.toString()).append(' ');
        } else if (Clazz.Visibility.PRIVATE.equals(visibility)) {
            validator.addResult(ClazzError.CANNOT_HAVE_PRIVATE_INTERFACE);
        }
        builder.append("interface ").append(className).append(TYPE_PLACEHOLDER);
    }

    Set<JMethod> buildImplementations(StringBuilder builder, Clazz.ClassType classType, Set<Class> implementations) {
        Set<JMethod> methods = new HashSet<>();
        String parametrization = "";
        if (!implementations.isEmpty()) {
            builder.append(Clazz.ClassType.CLASS.equals(classType) ? " implements " : " extends ");
            for (Class object : implementations) {
                Matcher matcher = typedClass.matcher(object.toGenericString());
                parametrization = matcher.find() ? matcher.group(1) : "";

                builder.append(procUtil.dollarToDot(procUtil.getClassSimpleName(object.toString())));
                builder.append(parametrization).append(", ");
                for (Method method : object.getDeclaredMethods()) {
                    methods.add(new JMethod(method));
                }
            }
            trimComma(builder);
        }
        createParametrization(builder, parametrization);
        return methods;
    }

    private void trimComma(StringBuilder builder) {
        int length = builder.lastIndexOf(", ");
        if (length != -1) {
            builder.replace(length, length + 1, "");
        }
    }

    void createParametrization(StringBuilder builder, String parametrization) {
        int startIndex = builder.indexOf(TYPE_PLACEHOLDER);
        if (startIndex > 0) {
            builder.replace(startIndex, startIndex + TYPE_PLACEHOLDER.length(), parametrization);
        }
    }

    void buildHeader(String packagePath, StringBuilder builder) {
        if (!StringUtil.isEmpty(packagePath)) {
            builder.append("package ").append(packagePath).append(';').append(procUtil.ONE_LINE).append(procUtil.ONE_LINE);
        }
        builder.append(procUtil.IMPORT_PLACEHOLDER).append(procUtil.ONE_LINE);
    }

    void buildImports(Set<Class> imports, StringBuilder builder) {
        StringBuilder importBuilder = new StringBuilder();
        for (Class aClass : imports) {
            String importName = aClass.getCanonicalName();
            if (!importName.startsWith("java.lang") && importName.equals(JavaKeywords.replaceJavaKeyword(importName))) {
                importBuilder.append("import ").append(procUtil.dollarToDot(importName)).append(';').append(procUtil.ONE_LINE);
            }
        }
        int start = builder.indexOf(procUtil.IMPORT_PLACEHOLDER);
        int end = start + procUtil.IMPORT_PLACEHOLDER.length();
        builder.replace(start, end, importBuilder.toString());
    }
}
