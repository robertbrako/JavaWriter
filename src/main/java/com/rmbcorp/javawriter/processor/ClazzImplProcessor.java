package com.rmbcorp.javawriter.processor;

import com.rmbcorp.javawriter.clazz.*;
import com.rmbcorp.javawriter.clazz.ClazzError;
import com.rmbcorp.util.StringUtil;
import com.rmbcorp.util.ValidationManager;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**ClazzImplProcessor
 * Created by rmbdev on 10/1/2016.
 */
final class ClazzImplProcessor implements ClazzProcessor<ClazzReadable> {

    private static final String EMPTY_BODY = "//empty";

    private StringBuilder builder;
    private ValidationManager<ClazzError> validator;
    private final ClassStarter classStarter;
    private final ProcUtil procUtil;
    private String errorCache;
    
    ClazzImplProcessor(ValidationManager<ClazzError> validator, ClassStarter classStarter, ProcUtil procUtil) {
        this.validator = validator;
        this.classStarter = classStarter;
        this.procUtil = procUtil;
    }

    @Override
    public boolean hasError(ClazzError error) {
        return errorCache.contains(error.name());
    }

    @Override
    public String writeOut(ClazzReadable clazz) {
        errorCache = "";
        validator.removeAllResults();
        String out = doWriteOut(clazz);
        if (validator.hasErrors()) {
            errorCache = ((ClazzValidator)validator).getErrorsAsCSV();
        }
        return out;
    }

    private String doWriteOut(ClazzReadable clazz) {
        builder = new StringBuilder("");
        classStarter.buildHeader(clazz.getPackagePath(), builder);
        Set<Class> allImports = clazz.getImports();
        setupImports(allImports, clazz.getExtension(), clazz.getImplementations());
        buildClassOrInterface(clazz.getClassType(), clazz);
        buildBody(clazz.getExtension(), clazz.getImplementations(), allImports, clazz.getMethods());
        closeBody();
        classStarter.buildImports(allImports, builder);

        return validator.hasErrors() ? "" : builder.toString();
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
        builder.append(procUtil.VARIABLE_PLACEHOLDER).append(procUtil.ONE_LINE);
        if (extension != null) {
            implementations.add(extension);
        }
        Set<JVariable> allVariables = new HashSet<>();
        for (Class clazz : implementations) {
            for (Method method : clazz.getDeclaredMethods()) {
                processMethods(imports, lev, allVariables, new JMethod(method));
            }
        }
        for (JMethod jMethod : jMethods) {
            processMethods(imports, lev, allVariables, jMethod);
        }
        implementations.remove(extension);
        buildVariables(allVariables, lev);
    }

    private void processMethods(Set<Class> imports, int lev, Set<JVariable> allVariables, JMethod jMethod) {
        Class<?> returnClass = jMethod.getReturnType();
        List<ProcUtil.JParam> returnTypeAndParams;
        String returnType, varName, methodName, methodParamType;
        Map<JVariable, Boolean> variables;
        methodName = jMethod.getName();

        if (jMethod.isOverride()) {
            builder.append(procUtil.tab(lev)).append("@Override").append(procUtil.ONE_LINE);
        }

        ProcUtil.ReturnParams returnAndParams = procUtil.getReturnAndParams(jMethod);
        returnType = returnAndParams.getReturnType();
        if (returnType.startsWith("<")) {
            returnType += " " + returnType.replaceAll("[<>]", "") + "[]";
        }
        returnTypeAndParams = returnAndParams.getParams();
        builder.append(procUtil.tab(lev)).append(procUtil.getScope(jMethod.getModifier()))
                .append(returnType).append(' ')
                .append(methodName).append('(');
        
        methodParamType = returnTypeAndParams.isEmpty() ? "" : getFirstTypeOnly(returnTypeAndParams);
        variables = getParams(returnTypeAndParams, methodName.startsWith("set"), imports);
        builder.append(")");
        allVariables.addAll(variables.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));
        builder.append(" {").append(procUtil.ONE_LINE);
        lev++;
        for (Map.Entry<JVariable, Boolean> entry : variables.entrySet()) {
            if (entry.getValue()) {
                varName = entry.getKey().getName();
                builder.append(procUtil.tab(lev)).append("this.").append(varName).append(" = ").append(varName).append(";").append(procUtil.ONE_LINE);
            }
        }
        generateForLoopAndIfStatements(variables, methodParamType, lev);
        generateReturnStatement(lev, returnClass, returnType);
        imports.add(returnClass);
        lev--;
        builder.append(procUtil.tab(lev)).append("}").append(procUtil.TWO_LINES);
    }

    private String getFirstTypeOnly(List<ProcUtil.JParam> returnTypeAndParams) {
        List<String> parametrizedTypes = returnTypeAndParams.get(0).types();
        return parametrizedTypes.isEmpty() ? "" : parametrizedTypes.get(0);
    }

    private void generateReturnStatement(int lev, Class<?> returnClass, String returnType) {
        if (!"void".equalsIgnoreCase(returnType)) {
            String simpleName = returnClass.getSimpleName();
            String returnText = returnClass.isPrimitive() ? returnPrimitive(simpleName) : returnObject(returnClass, returnType, simpleName);
            builder.append(procUtil.tab(lev)).append("return ").append(returnText).append(procUtil.ONE_LINE);
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
                || returnType.startsWith("<") || returnType.length() < 2)) {
            if (simpleName.contains("[]")) {
                returnText = "new " + simpleName + "{};";
            } else {
                returnText = "new " + simpleName + "();";
            }
        }
        return returnText;
    }

    private Map<JVariable, Boolean> getParams(List<ProcUtil.JParam> parameterTypes, boolean makeSetter, Set<Class> imports) {
        Map<JVariable, Boolean> variables = new HashMap<>();
        ProcUtil.JParam param;//param as String was like "java.lang.String" - get this from Class object instead.
        String simpleName;
        Map<Integer, Integer> paramCounts = new HashMap<>();
        int typeCount;
        for (int i = 0, parameterTypesLength = parameterTypes.size(); i < parameterTypesLength; i++) {
            param = parameterTypes.get(i);
            simpleName = procUtil.getClassSimpleName(procUtil.dollarToDot(param.getParamType()));
            builder.append(simpleName);
            if (!param.types().isEmpty()) {
                builder.append("<");
                String parametrizedTypes = param.types().stream().collect(Collectors.joining(","));
                builder.append(parametrizedTypes);
                builder.append(">");
            }
            builder.append(" ");

            String trimmedParam = cleanParamString(simpleName);
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
            resolveParamImports(imports, param.getParamType(), simpleName, trimmedParam);
        }
        return variables;
    }

    private void resolveParamImports(Set<Class> imports, String param, String simpleName, String trimmedParam) {
        if (!param.equals(simpleName) &&
                imports.stream().noneMatch(clazz -> clazz.getSimpleName().contains(trimmedParam))) {
            insertImport(imports, param);
        }
    }

    private void insertImport(Set<Class> imports, String param) {
        try {
            imports.add(Class.forName(cleanParamString(param)));
        } catch (ClassNotFoundException ignored) {
            Logger.getGlobal().log(Level.INFO, ignored.getLocalizedMessage(), ignored);
            validator.addResult(ClazzError.INVALID_CLASS_NAME);
        }
    }

    private String cleanParamString(String string) {
        return string.replace("[]", "").replaceAll("<.*>", "");
    }

    private void generateForLoopAndIfStatements(Map<JVariable, Boolean> variables, String methodParamType, int tabLev) {
        boolean useEmptyBody = true;
        String iterable;
        for (JVariable jVariable : variables.keySet()) {
            iterable = cleanParamString(jVariable.getType());
            if (!"".equals(methodParamType) && Iterables.contains(iterable) && !methodParamType.startsWith("?")) {//todo deal with ?
                useEmptyBody = false;
                builder.append(procUtil.tab(tabLev)).append("for (")
                        .append(methodParamType).append(" ").append(procUtil.getVarName(methodParamType))
                        .append(" : ")
                        .append(iterable.toLowerCase())
                        .append(") {").append(procUtil.ONE_LINE);
                builder.append(procUtil.tab(tabLev+1)).append(EMPTY_BODY).append(procUtil.ONE_LINE);
                builder.append(procUtil.tab(tabLev)).append("}").append(procUtil.ONE_LINE);
            } else if ("boolean".equalsIgnoreCase(iterable)) {
                String param = jVariable.getName();
                generateIfStatement(tabLev, param, EMPTY_BODY);
            }
        }
        if (useEmptyBody) {
            builder.append(procUtil.tab(tabLev)).append(EMPTY_BODY).append(procUtil.ONE_LINE);
        }
    }

    private void buildVariables(Set<JVariable> variables, int tabLevel) {
        int lineBegin = builder.indexOf(procUtil.VARIABLE_PLACEHOLDER);
        int lineEnd = lineBegin + procUtil.VARIABLE_PLACEHOLDER.length();
        StringBuilder newLine = new StringBuilder(String.valueOf(procUtil.TWO_LINES));
        for (JVariable variable : variables) {
            newLine.append(procUtil.tab(tabLevel)).append(variable.writeOut()).append(";\n");
        }
        builder.replace(lineBegin, lineEnd, newLine.toString());
    }

    private void generateIfStatement(int tabLev, String statement, String body) {
        builder.append(procUtil.tab(tabLev)).append("if (")
                .append(StringUtil.isEmpty(statement) ? "true" : statement)
                .append(") {")
                .append(procUtil.ONE_LINE)
                .append(procUtil.tab(tabLev+1)).append(body)
                .append(procUtil.ONE_LINE)
                .append(procUtil.tab(tabLev)).append("}").append(procUtil.ONE_LINE);
    }

    private void closeBody() {
        builder.append('}').append(procUtil.ONE_LINE);
    }

}
