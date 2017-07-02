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

import com.rmbcorp.javawriter.clazz.Clazz;
import com.rmbcorp.javawriter.clazz.ClazzImplManager;
import com.rmbcorp.javawriter.clazz.ClazzReadable;
import com.rmbcorp.javawriter.clazz.JMethod;
import com.rmbcorp.javawriter.clazz.JVariable;
import com.rmbcorp.util.ValidationManager;

import java.util.*;


final class BeanProcessor implements ClazzProcessor {

    private final ValidationManager<ClazzImplManager.ClazzError> validator;
    private final ClassStarter classStarter;
    private final ProcUtil procUtil;
    private StringBuilder builder;
    private Map<String, String> varNames = new HashMap<>();
    private String errorCache;

    BeanProcessor(ValidationManager<ClazzImplManager.ClazzError> validator, ClassStarter classStarter, ProcUtil procUtil) {
        this.validator = validator;
        this.classStarter = classStarter;
        this.procUtil = procUtil;
    }

    @Override
    public boolean hasError(ClazzImplManager.ClazzError error) {
        return errorCache.contains(error.toString());
    }

    @Override
    public String writeOut(Clazz clazz) {
        if (clazz instanceof ClazzReadable) {
            String out = writeOut((ClazzReadable) clazz);
            if (validator.hasErrors()) {
                errorCache = ((ClazzValidator)validator).getErrorsAsCSV();
                validator.removeAllResults();
            }
            return out;
        }
        else return "";
    }

    String writeOut(ClazzReadable clazz) {
        reset();
        int tabLevel = 0;
        String packagePath = clazz.getPackagePath();
        Set<Class> imports = new HashSet<>(clazz.getImports());
        imports.addAll(clazz.getImplementations());
        classStarter.buildHeader(packagePath, builder);
        classStarter.buildClassOrInterface(builder, clazz.getClassType(), clazz);
        classStarter.createParametrization(builder, "");

        Set<JMethod> beanMethods = getFromBeanVariables(packagePath, clazz.getBeanVariables());
        beanMethods.addAll(clazz.getMethods());
        beanMethods.stream().forEach(jMethod -> imports.add(jMethod.getReturnType()));

        openBody();
        buildBody(beanMethods, tabLevel + 1);
        buildVariables(tabLevel + 1);
        buildImports(imports);
        closeBody();
        return builder.toString();
    }

    private void reset() {
        builder = new StringBuilder("");
        errorCache = "";
        varNames.clear();
    }

    private void openBody() {
        builder.append(" {").append(procUtil.TWO_LINES);
        builder.append(procUtil.VARIABLE_PLACEHOLDER).append(procUtil.ONE_LINE);
    }

    private Set<JMethod> getFromBeanVariables(String packagePath, Set<JVariable> beanVariables) {
        Set<JMethod> methods = new HashSet<>();
        for (JVariable variable : beanVariables) {
            methods.add(new JMethod("set" + firstUpper(variable.getName()), Void.class, Clazz.Visibility.PUBLIC, packagePath, variable.getClassType()));
            if ("boolean".equalsIgnoreCase(variable.getType())) {
                methods.add(new JMethod("is" + firstUpper(variable.getName()), variable.getClassType(), Clazz.Visibility.PUBLIC, packagePath));
            } else {
                methods.add(new JMethod("get" + firstUpper(variable.getName()), variable.getClassType(), Clazz.Visibility.PUBLIC, packagePath));
            }
        }
        return methods;
    }

    private String firstUpper(String name) {
        char[] paramCopy = JavaKeywords.replaceJavaKeyword(name).toCharArray();
        paramCopy[0] = Character.toUpperCase(paramCopy[0]);
        return new String(paramCopy);
    }

    private void buildBody(Set<JMethod> methods, int lev) {
        List<String> varCache = new ArrayList<>(2);
        for (JMethod method : methods) {
            varCache.clear();
            ProcUtil.ReturnParams returnParams = procUtil.getReturnAndParams(method);
            String returnType = procUtil.dollarToDot(returnParams.getReturnType());
            List<ProcUtil.JParam> preParams = returnParams.getParams();

            builder.append(procUtil.tab(lev))
                    .append(procUtil.getScope(method.getModifier()))
                    .append(returnType).append(' ')
                    .append(method.getName()).append('(');
            for (Iterator<ProcUtil.JParam> iterator = preParams.iterator(); iterator.hasNext(); ) {
                ProcUtil.JParam variable = iterator.next();
                String varType = procUtil.toPrimitive(procUtil.getClassSimpleName(variable.getParamType()));
                String varName = procUtil.getVarName(method.getName().replaceFirst("set", ""));
                varNames.put(varName, varType);
                varCache.add(varName);
                builder.append(varType).append(' ').append(varName);
                if (iterator.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append(") {\n");
            buildMethod(varCache, method.getName(), lev + 1);
            builder.append(procUtil.tab(lev)).append("}\n");
        }
    }

    private void buildMethod(List<String> varCache, String methodName, int lev) {
        if (methodName.startsWith("get") || methodName.startsWith("is")) {
            builder.append(procUtil.tab(lev))
                    .append("return ")
                    .append(procUtil.getVarName(methodName.replaceFirst("get", "")))
                    .append(";\n");
        } else {
            for (String var : varCache) {
                builder.append(procUtil.tab(lev)).append("this.").append(var).append(" = ").append(var).append(";\n");
            }
        }
    }

    private void buildVariables(int lev) {
        StringBuilder varBuilder = new StringBuilder();
        varNames.forEach((varName, varType) -> varBuilder.append(procUtil.tab(lev)).append("private ").append(varType).append(' ').append(varName).append(";\n"));
        int varIndex = builder.indexOf(procUtil.VARIABLE_PLACEHOLDER);
        builder.replace(varIndex, varIndex + procUtil.VARIABLE_PLACEHOLDER.length(), varBuilder.toString());
    }

    private void buildImports(Set<Class> imports) {
        StringBuilder importBuilder = new StringBuilder();
        for (Class aClass : imports) {
            String importName = aClass.getCanonicalName();
            if (!importName.startsWith("java.lang")) {
                importBuilder.append("import ").append(procUtil.dollarToDot(importName)).append(';').append(procUtil.ONE_LINE);
            }
        }
        int start = builder.indexOf(procUtil.IMPORT_PLACEHOLDER);
        int end = start + procUtil.IMPORT_PLACEHOLDER.length();
        builder.replace(start, end, importBuilder.toString());
    }

    private void closeBody() {
        builder.append("}\n");
    }
}
