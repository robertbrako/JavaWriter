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

import java.util.*;


final class BeanProcessor implements ClazzProcessor<ClazzReadable> {

    private final ClassStarter classStarter;
    private final ProcUtil procUtil;
    private ClassBuilder builder;
    private Map<String, String> varNames = new HashMap<>();

    BeanProcessor(ClassStarter classStarter, ProcUtil procUtil) {
        this.classStarter = classStarter;
        this.procUtil = procUtil;
        builder = new ClassBuilder(procUtil);
    }

    @Override
    public ProcessResult writeOut(ClazzReadable clazz) {
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
        beanMethods.forEach(jMethod -> imports.add(jMethod.getReturnType()));

        openBody();
        buildBody(beanMethods, tabLevel + 1);
        buildVariables(tabLevel + 1);
        classStarter.buildImports(imports, builder);
        closeBody();
        return new ProcessResult(builder.toString(), builder.getErrors());
    }

    private void reset() {
        builder.reset();
        varNames.clear();
    }

    private void openBody() {
        builder.append(" {").appendln().appendln();
        builder.markVariables().appendln();
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
            String returnType = procUtil.getReturnType(returnParams);

            builder.processComments(method.getComment());
            builder.append(procUtil.tab(lev))
                    .append(procUtil.getScope(method.getModifier()))
                    .append(returnType).append(' ')
                    .append(method.getName()).append('(');

            buildParams(varCache, method, returnParams.getParams());

            builder.append(") {").appendln();
            buildMethod(varCache, method.getName(), lev + 1);
            builder.append(procUtil.tab(lev)).append("}").appendln();
        }
    }

    private void buildParams(List<String> varCache, JMethod method, List<ProcUtil.JParam> preParams) {
        for (Iterator<ProcUtil.JParam> iterator = preParams.iterator(); iterator.hasNext(); ) {
            ProcUtil.JParam variable = iterator.next();
            String varType = procUtil.toPrimitive(procUtil.getClassSimpleName(variable.getParamType()));
            String varName = procUtil.getVarName(method.getName().replaceFirst("set", ""));
            varNames.put(varName, varType);
            varCache.add(varName);
            builder.append(varType);
            classStarter.addParametrization(builder, variable);
            builder.append(' ').append(varName);
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
    }

    private void buildMethod(List<String> varCache, String methodName, int lev) {
        if (methodName.startsWith("get") || methodName.startsWith("is")) {
            builder.append(procUtil.tab(lev))
                    .append("return ")
                    .append(procUtil.getVarName(methodName.replaceFirst("get", "")))
                    .append(";").appendln();
        } else {
            for (String var : varCache) {
                builder.append(procUtil.tab(lev)).append("this.").append(var).append(" = ").append(var).append(";").appendln();
            }
        }
    }

    private void buildVariables(int lev) {
        varNames.forEach((varName, varType) -> builder.insertVariable("private ", varType, varName, lev));
    }

    private void closeBody() {
        builder.append("}").appendln();
    }
}
