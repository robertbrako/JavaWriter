package com.rmbcorp.javawriter.processor;

import com.rmbcorp.javawriter.clazz.Clazz;
import com.rmbcorp.javawriter.clazz.ClazzImplManager.ClazzError;
import com.rmbcorp.javawriter.clazz.ClazzReadable;
import com.rmbcorp.javawriter.clazz.JMethod;
import com.rmbcorp.javawriter.clazz.JVariable;
import com.rmbcorp.util.StringUtil;
import com.rmbcorp.util.ValidationManager;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**ClazzImplProcessor
 * Created by rmbdev on 10/1/2016.
 */
final class ClazzImplProcessor implements ClazzProcessor {

    static final String EMPTY_BODY = "//empty";

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
    public String writeOut(Clazz clazz) {
        errorCache = "";
        validator.removeAllResults();
        if (clazz instanceof ClazzReadable) {
            String out = writeOut((ClazzReadable) clazz);
            if (validator.hasErrors()) {
                errorCache = ((ClazzValidator)validator).getErrorsAsCSV();

            }
            return out;
        }
        else return "";
    }

    String writeOut(ClazzReadable clazz) {
        builder = new StringBuilder("");
        setupImports(clazz.getImports(), clazz.getExtension(), clazz.getImplementations());
        buildClassOrInterface(clazz.getClassType(), clazz);
        buildBody(clazz.getExtension(), clazz.getImplementations(), clazz.getImports(), clazz.getMethods());
        closeBody();
        buildImports(clazz.getImports());
        buildPackage(clazz.getPackagePath());

        return validator.hasErrors() ? "" : builder.toString();
    }

    private void buildPackage(String packagePath) {
        if (!StringUtil.isEmpty(packagePath)) {
            builder.insert(0, "package " + packagePath + ';' + procUtil.ONE_LINE + procUtil.ONE_LINE);
        }
    }

    private void setupImports(Set<Class> imports, Class extension, Set<Class> implementations) {
        if (extension != null) {
            imports.add(extension);
        }
        imports.addAll(implementations);
    }

    private void buildImports(Set<Class> imports) {
        builder.insert(0, procUtil.IMPORT_PLACEHOLDER);
        int start = builder.length();
        for (Class object : imports) {
            String importName = splitClassString(object);
            if (importName.equals(JavaKeywords.replaceJavaKeyword(importName)) && !"void".equals(importName) && !importName.startsWith("[L")) {
                builder.append("import ").append(procUtil.dollarToDot(splitClassString(object))).append(';');
                builder.append(procUtil.ONE_LINE);
            }
        }
        builder.append(procUtil.ONE_LINE);
        String substring = builder.substring(start, builder.length());
        builder.replace(start, builder.length(), "");
        builder.replace(0, procUtil.IMPORT_PLACEHOLDER.length(), substring);
    }

    private String splitClassString(Object object) {
        return object.toString().replaceFirst("class ", "").replaceFirst("interface ", "");
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
        List<String> returnTypeAndParams;
        String returnType, varName, methodName, methodParamType;
        Map<JVariable, Boolean> variables;
        methodName = jMethod.getName();

        if (jMethod.isOverride()) {
            builder.append(procUtil.tab(lev)).append("@Override").append(procUtil.ONE_LINE);
        }
        ProcUtil.ReturnParams indexOfReturnType = procUtil.getReturnAndParams(jMethod);
        returnType = indexOfReturnType.getReturnType();
        returnTypeAndParams = indexOfReturnType.getParams();
        builder.append(procUtil.tab(lev)).append(procUtil.getScope(jMethod.getModifier()))
                .append(returnType).append(' ')
                .append(methodName).append('(');
        
        methodParamType = getParamType(jMethod.toGenericString());
        variables = getParams(returnTypeAndParams, methodName.startsWith("set"), imports);
        allVariables.addAll(variables.keySet());
        builder.append(" {").append(procUtil.ONE_LINE);
        lev++;
        for (Map.Entry<JVariable, Boolean> entry : variables.entrySet()) {
            if (entry.getValue()) {
                varName = entry.getKey().getName();
                builder.append(procUtil.tab(lev)).append("this.").append(varName).append(" = ").append(varName).append(";").append(procUtil.ONE_LINE);
            }
        }
        generateForLoopAndIfStatements(variables, methodParamType, lev);
        if (!"void".equalsIgnoreCase(returnType)) {
            builder.append(procUtil.tab(lev)).append("return ").append(conjureReturnObject(returnClass));
        }
        imports.add(returnClass);
        lev--;
        builder.append(procUtil.tab(lev)).append("}").append(procUtil.TWO_LINES);
    }

    private String conjureReturnObject(Class<?> returnClass) {
        String returnText = "null;";
        String simpleName = returnClass.getSimpleName();
        if (returnClass.isPrimitive()) {
            if (Arrays.asList("int", "long", "short", "float", "double", "byte").contains(simpleName)) {
                returnText = "0;";
            }
            if ("boolean".equals(simpleName)) {
                returnText = "false;";
            }
            if ("char".equals(simpleName)) {
                returnText = "'\\0';";
            }
        } else {
            if (returnClass.isInterface() || returnClass.getCanonicalName().contains("abstract")) {
                returnText = "null;";
            } else if (simpleName.contains("[]")) {
                returnText = "new " + simpleName + "{};";
            } else {
                returnText = "new " + simpleName + "();";
            }
        }
        return returnText + procUtil.ONE_LINE;
    }

    private Map<JVariable, Boolean> getParams(List<String> parameterTypes, boolean makeSetter, Set<Class> imports) {
        Map<JVariable, Boolean> variables = new HashMap<>();
        String param, simpleName;//param as String was like "java.lang.String" - get this from Class object instead.
        Map<Integer, Integer> paramCounts = new HashMap<>();
        int typeCount;
        for (int i = 0, parameterTypesLength = parameterTypes.size(); i < parameterTypesLength; i++) {
            param = parameterTypes.get(i);
            simpleName = procUtil.getClassSimpleName(procUtil.dollarToDot(param));
            builder.append(simpleName);
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
            boolean found = false;
            if (!param.equals(simpleName)) {
                for (Class clazz : imports) {//future: make more efficient
                    if (clazz.getSimpleName().contains(trimmedParam)) {
                        found = true;
                    }
                }
                if (!found) {
                    try {
                        imports.add(Class.forName(cleanParamString(param)));
                    } catch (ClassNotFoundException ignored) {
                        Logger.getGlobal().log(Level.INFO, ignored.getLocalizedMessage(), ignored);
                        validator.addResult(ClazzError.INVALID_CLASS_NAME);
                    }
                }
            }
        }
        builder.append(")");
        return variables;
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
                generateIfStatement(2, param, EMPTY_BODY); //caution, multiple boolean params will cause problem; hard-coded tab-lev
            }
        }
        if (useEmptyBody) {
            builder.append(procUtil.tab(tabLev)).append(EMPTY_BODY).append(procUtil.ONE_LINE);
        }
    }

    private String getParamType(String method) {
        int beginIndex = method.indexOf('<');
        int endIndex = method.lastIndexOf('>');
        if (beginIndex > -1) {
            String[] className = method.substring(beginIndex+1, endIndex).split("\\.");
            return className[className.length-1];
        }
        return "";
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
