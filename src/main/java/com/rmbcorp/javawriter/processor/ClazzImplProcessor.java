package com.rmbcorp.javawriter.processor;

import com.rmbcorp.javawriter.clazz.*;
import com.rmbcorp.util.StringUtil;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**ClazzImplProcessor
 * Created by rmbdev on 10/1/2016.
 */
final class ClazzImplProcessor implements ClazzProcessor<ClazzReadable> {

    private static final String EMPTY_BODY = "//empty";

    private ClassBuilder builder;
    private final ClassStarter classStarter;
    private final ProcUtil procUtil;

    ClazzImplProcessor(ClassStarter classStarter, ProcUtil procUtil) {
        this.classStarter = classStarter;
        this.procUtil = procUtil;
        builder = new ClassBuilder(procUtil);
    }

    @Override
    public ProcessResult writeOut(ClazzReadable clazz) {
        builder.reset();
        classStarter.buildHeader(clazz.getPackagePath(), builder);
        Set<Class> allImports = clazz.getImports();
        setupImports(allImports, clazz.getExtension(), clazz.getImplementations());
        buildClassOrInterface(clazz.getClassType(), clazz);
        buildBody(clazz.getExtension(), clazz.getImplementations(), allImports, clazz.getMethods());
        closeBody();
        classStarter.buildImports(allImports, builder);

        return new ProcessResult(builder.toString(), builder.getErrors());
    }

    private void setupImports(Set<Class> imports, Class extension, Set<Class> implementations) {
        if (extension != null) {
            imports.add(extension);
        }
        imports.addAll(implementations);
    }

    private void buildClassOrInterface(Clazz.ClassType classType, ClazzReadable clazz) {
        classStarter.buildClassOrInterface(builder, classType, clazz);
        classStarter.buildImplementations(builder, classType, clazz.getImplementations());
        builder.append(" {");
    }

    private void buildBody(Class extension, Set<Class> implementations, Set<Class> imports, Set<JMethod> jMethods) {
        int lev = 1;
        builder.appendln().markVariables().appendln().appendln();
        if (extension != null && (extension.getModifiers() & Modifier.FINAL) == 0) {
            implementations.add(extension);
        }
        Set<JVariable> allVariables = new HashSet<>();
        for (Class clazz : implementations) {
            Arrays.stream(clazz.getDeclaredMethods())
                    .filter(method -> (method.getModifiers() & Modifier.PRIVATE) == 0)
                    .filter(method -> (method.getModifiers() & Modifier.STATIC) == 0)
                    .filter(method -> (method.getModifiers() & Modifier.FINAL) == 0)
                    .forEach(method -> processMethods(imports, lev, allVariables, new JMethod(method)));
        }
        for (JMethod jMethod : jMethods) {
            processMethods(imports, lev, allVariables, jMethod);
        }
        implementations.remove(extension);
        buildVariables(allVariables, lev);
    }

    private void processMethods(Set<Class> imports, int lev, Set<JVariable> allVariables, JMethod jMethod) {
        Class<?> returnClass = jMethod.getReturnType();
        List<ProcUtil.JParam> params;
        String returnType, varName, methodName, methodParamType;
        Map<JVariable, Boolean> variables;
        methodName = jMethod.getName();

        if (jMethod.isOverride()) {
            builder.append(procUtil.tab(lev)).append("@Override").appendln();
        }
        builder.processComments(jMethod.getComment());

        ProcUtil.ReturnParams returnAndParams = procUtil.getReturnAndParams(jMethod);
        ProcUtil.JParam returnTypeInfo = returnAndParams.getReturnType();
        returnType = returnTypeInfo.getParamType();
        params = returnAndParams.getParams();

        procUtil.insertImports(builder, imports, returnTypeInfo);

        builder.append(procUtil.tab(lev)).append(procUtil.getScope(jMethod.getModifier()))
                .append(returnType).append(' ')
                .append(methodName).append('(');
        
        methodParamType = params.isEmpty() ? "" : getFirstTypeOnly(params);
        variables = getParams(params, methodName.startsWith("set"), imports);
        builder.append(")");
        allVariables.addAll(variables.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));
        builder.append(" {").appendln();
        lev++;
        for (Map.Entry<JVariable, Boolean> entry : variables.entrySet()) {
            if (entry.getValue()) {
                varName = entry.getKey().getName();
                builder.append(procUtil.tab(lev)).append("this.").append(varName).append(" = ").append(varName).append(";").appendln();
            }
        }
        generateForLoopAndIfStatements(variables, methodParamType, lev);
        generateReturnStatement(lev, returnClass, returnType);
        procUtil.insertImport(builder, imports, returnClass.getCanonicalName());
        lev--;
        builder.append(procUtil.tab(lev)).append("}").appendln().appendln();
    }

    private String getFirstTypeOnly(List<ProcUtil.JParam> returnTypeAndParams) {
        List<String> parametrizedTypes = returnTypeAndParams.get(0).types();
        return parametrizedTypes.isEmpty() ? "" : parametrizedTypes.get(0);
    }

    private void generateReturnStatement(int lev, Class<?> returnClass, String returnType) {
        if (!"void".equalsIgnoreCase(returnType)) {
            String simpleName = returnClass.getSimpleName();
            String returnText = returnClass.isPrimitive() ? returnPrimitive(simpleName) : returnObject(returnClass, returnType, simpleName);
            builder.append(procUtil.tab(lev)).append("return ").append(returnText).appendln();
        }
    }

    private String returnPrimitive(String simpleName) {
        String returnText = "null;";
        if (Arrays.asList("int", "long", "short", "float", "double", "byte").contains(simpleName)) {
            returnText = "0;";
        }
        if ("boolean".equals(simpleName)) {
            returnText = "false;";
        }
        if ("char".equals(simpleName)) {
            returnText = "'\\0';";
        }
        return returnText;
    }

    private String returnObject(Class<?> returnClass, String returnType, String simpleName) {
        String returnText = "null;";
        if (!(returnClass.isInterface() || returnClass.getCanonicalName().contains("abstract")
                || returnType.startsWith("<") || returnType.length() < 2
                || returnClass.isEnum()|| "Class".equals(returnType))) {
            if (simpleName.contains("[]")) {
                returnText = "new " + simpleName + "{};";
            } else {
                if (Arrays.stream(returnClass.getConstructors()).filter(con -> con.getParameterCount() == 0).count() > 0) {
                    returnText = "new " + simpleName + "();";
                }
            }
        }
        return returnText;
    }

    private Map<JVariable, Boolean> getParams(List<ProcUtil.JParam> parameterTypes, boolean makeSetter, Set<Class> imports) {
        Map<JVariable, Boolean> variables = new HashMap<>();
        ProcUtil.JParam param;
        String simpleName;
        Map<Integer, Integer> paramCounts = new HashMap<>();
        int typeCount;
        for (int i = 0, parameterTypesLength = parameterTypes.size(); i < parameterTypesLength; i++) {
            param = parameterTypes.get(i);
            String printedName = procUtil.dollarToDot(param.getParamType());
            builder.append(printedName);
            classStarter.addParametrization(builder, param);
            builder.append(" ");
            simpleName = printedName.replace("...", "[]");

            String trimmedParam = procUtil.getClassSimpleName(cleanParamString(simpleName));
            String varName = procUtil.getVarName(trimmedParam);
            if (paramCounts.get(param.hashCode()) == null) {
                paramCounts.put(param.hashCode(), 0);
            } else {
                typeCount = paramCounts.get(param.hashCode())+1;
                paramCounts.put(param.hashCode(), typeCount);
                varName += Integer.toString(typeCount);
            }
            builder.append(varName);
            if (i < parameterTypesLength - 1) {
                builder.append(", ");
            }
            variables.put(new JVariable(varName, simpleName), makeSetter);
            procUtil.insertImports(builder, imports, param);
        }
        return variables;
    }

    private String cleanParamString(String string) {
        return string.replace("[]", "").replaceAll("<.*>", "");
    }

    private void generateForLoopAndIfStatements(Map<JVariable, Boolean> variables, String methodParamType, int tabLev) {
        boolean useEmptyBody = true;
        String iterable;
        String currentParamType = methodParamType;
        for (JVariable jVariable : variables.keySet()) {
            iterable = cleanParamString(jVariable.getType());
            if (!"".equals(methodParamType) && Iterables.contains(iterable)) {
                useEmptyBody = false;
                if ("?".equals(methodParamType)) currentParamType = "Object";
                else if (methodParamType.startsWith("? extends")) currentParamType = methodParamType.replace("? extends ", "");
                builder.append(procUtil.tab(tabLev)).append("for (")
                        .append(currentParamType).append(" ").append(procUtil.getVarName(currentParamType))
                        .append(" : ")
                        .append(iterable.toLowerCase())
                        .append(") {").appendln();
                builder.append(procUtil.tab(tabLev+1)).append(EMPTY_BODY).appendln();
                builder.append(procUtil.tab(tabLev)).append("}").appendln();
            } else if ("boolean".equalsIgnoreCase(iterable)) {
                String param = jVariable.getName();
                generateIfStatement(tabLev, param, EMPTY_BODY);
            }
        }
        if (useEmptyBody) {
            builder.append(procUtil.tab(tabLev)).append(EMPTY_BODY).appendln();
        }
    }

    private void buildVariables(Set<JVariable> variables, int tabLevel) {
        for (JVariable variable : variables) {
            builder.insertVariable(variable.getVisibility(), variable.getType(), variable.getName(), tabLevel);
        }
    }

    private void generateIfStatement(int tabLev, String statement, String body) {
        builder.append(procUtil.tab(tabLev)).append("if (")
                .append(StringUtil.isEmpty(statement) ? "true" : statement)
                .append(") {")
                .appendln()
                .append(procUtil.tab(tabLev+1)).append(body)
                .appendln()
                .append(procUtil.tab(tabLev)).append("}").appendln();
    }

    private void closeBody() {
        builder.append('}').appendln();
    }

}
