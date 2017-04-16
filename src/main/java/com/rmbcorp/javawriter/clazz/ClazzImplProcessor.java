package com.rmbcorp.javawriter.clazz;

import com.rmbcorp.util.StringUtil;
import com.rmbcorp.util.ValidationManager;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.rmbcorp.javawriter.clazz.ClazzImplManager.ClazzError;

/**ClazzImplProcessor
 * Created by rmbdev on 10/1/2016.
 */
final class ClazzImplProcessor {

    private static final char ONE_LINE = '\n';
    private static final char[] TWO_LINES = { '\n', '\n' };
    public static final String VARIABLE_PLACEHOLDER = "//%%VARIABLES%%";
    public static final String IMPORT_PLACEHOLDER = "//%%IMPORTS%%";
    public static final String EMPTY_BODY = "//empty";

    private StringBuilder builder;
    private ValidationManager<ClazzError> validator;
    
    ClazzImplProcessor(ValidationManager<ClazzError> validator) {
        this.validator = validator;
    }

    String writeOut(ClazzImpl clazz) {
        builder = new StringBuilder("");
        setupImports(clazz.getImports(), clazz.getExtension(), clazz.getImplementations());
        buildClassOrInterface(clazz.getClassType(), clazz);
        buildBody(clazz.getExtension(), clazz.getImplementations(), clazz.getImports());
        closeBody();
        buildImports(clazz.getImports());
        buildPackage(clazz.getPackagePath());

        return validator.hasErrors() ? "" : builder.toString();
    }

    private void buildPackage(String packagePath) {
        if (!StringUtil.isEmpty(packagePath)) {
            builder.insert(0, "package " + packagePath + ';' + ONE_LINE + ONE_LINE);
        }
    }

    private void setupImports(Set<Class> imports, Class extension, Set<Class> implementations) {
        if (extension != null) {
            imports.add(extension);
        }
        imports.addAll(implementations);
    }

    private void buildImports(Set<Class> imports) {
        builder.insert(0, IMPORT_PLACEHOLDER);
        int start = builder.length();
        for (Class object : imports) {
            String importName = splitClassString(object);
            if (importName.equals(JavaKeywords.replaceJavaKeyword(importName)) && !"void".equals(importName) && !importName.startsWith("[L")) {
                builder.append("import ").append(dollarToDot(splitClassString(object))).append(';');
                builder.append(ONE_LINE);
            }
        }
        builder.append(ONE_LINE);
        String substring = builder.substring(start, builder.length());
        builder.replace(start, builder.length(), "");
        builder.replace(0, IMPORT_PLACEHOLDER.length(), substring);
    }

    private static String splitClassString(Object object) {
        return object.toString().replaceFirst("class ", "").replaceFirst("interface ", "");
    }

    private void buildClassOrInterface(Clazz.ClassType classType, ClazzImpl clazz) {
        String className = clazz.getClassName();
        if (StringUtil.isEmpty(className)) {
            validator.addResult(ClazzError.CANNOT_HAVE_EMPTY_CLASS_NAME);
        }
        if (!className.equals(JavaKeywords.replaceJavaKeywordSafe(className))) {
            validator.addResult(ClazzError.INVALID_CLASS_NAME);
        }
        if (Clazz.ClassType.INTERFACE.equals(classType)) {
            buildInterface(clazz.getVisibility(), className);
        } else if (Clazz.ClassType.CLASS.equals(classType)) {
            buildClass(className, clazz.getVisibility(), clazz.isFinal(), clazz.isAbstract(), clazz.getExtension());
        } else {
            validator.addResult(ClazzError.MUST_BE_CLASS_OR_INTERFACE);
        }
        buildImplementations(classType, clazz.getImplementations());
        builder.append(" {");
    }

    private void buildClass(String className, Clazz.Visibility visibility, boolean isFinal, boolean isAbstract, Class extension) {
        if (!(isFinal && isAbstract)) {
            builder.append(visibility.toString());
            builder.append(isFinal ? "final " : isAbstract ? "abstract " : "");
            builder.append("class ").append(className);
            if (extension != null) {
                builder.append(" extends ");
                builder.append(getClassSimpleName(extension.toString()));
            }
        } else {
            validator.addResult(ClazzError.CANNOT_BE_ABSTRACT_AND_FINAL);
        }
    }

    private void buildInterface(Clazz.Visibility visibility, String className) {
        if (Clazz.Visibility.PUBLIC.equals(visibility)) {
            builder.append(visibility.toString()).append(' ');
        } else if (Clazz.Visibility.PRIVATE.equals(visibility)) {
            validator.addResult(ClazzError.CANNOT_HAVE_PRIVATE_INTERFACE);
        }
        builder.append("interface ").append(className);
    }

    private void buildImplementations(Clazz.ClassType classType, Set<Class> implementations) {
        if (!implementations.isEmpty()) {
            builder.append(Clazz.ClassType.CLASS.equals(classType) ? " implements " : " extends ");
            for (Class object : implementations) {
                builder.append(dollarToDot(getClassSimpleName(object.toString())));
                builder.append(", ");
            }
            trimComma(builder);
        }
    }

    private static void trimComma(StringBuilder result) {
        int length = result.lastIndexOf(", ");
        if (length != -1) {
            result.replace(length, length + 1, "");
        }
    }

    private String getClassSimpleName(String object) {
        int begin = object.lastIndexOf('.') + 1;
        return object.substring(begin).replaceAll(";", "");
    }

    private void buildBody(Class extension, Set<Class> implementations, Set<Class> imports) {
        int lev = 1;
        builder.append(VARIABLE_PLACEHOLDER).append(ONE_LINE);
        if (extension != null) {
            implementations.add(extension);
        }
        Set<JVariable> allVariables = new HashSet<>();
        Map<JVariable, Boolean> variables;
        Class<?> returnClass;
        List<String> returnTypeAndParams;
        String returnType, varName, methodName, methodParamType;
        for (Class clazz : implementations) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                methodName = method.getName();
                builder.append(tab(lev)).append("@Override").append(ONE_LINE);
                returnClass = method.getReturnType();
                builder.append(tab(lev)).append(getScope(method.getModifiers()));
                returnTypeAndParams = getReturnTypeAndParams(method);
                returnType = returnTypeAndParams.get(0);
                builder.append(returnType).append(' ').append(methodName);
                methodParamType = getParamType(method.toGenericString());
                variables = getParams(returnTypeAndParams.subList(1, returnTypeAndParams.size()), methodName.startsWith("set"), imports);
                allVariables.addAll(variables.keySet());
                builder.append(" {").append(ONE_LINE);
                lev++;
                for (Map.Entry<JVariable, Boolean> entry : variables.entrySet()) {
                    if (entry.getValue()) {
                        varName = entry.getKey().getName();
                        builder.append(tab(lev)).append("this.").append(varName).append(" = ").append(varName).append(";").append(ONE_LINE);
                    }
                }
                generateForLoopAndIfStatements(variables, methodParamType, lev);
                if (!returnType.equals("void")) {
                    builder.append(tab(lev)).append("return ").append(conjureReturnObject(returnClass));
                }
                imports.add(returnClass);
                lev--;
                builder.append(tab(lev)).append("}").append(TWO_LINES);
            }
        }
        implementations.remove(extension);
        buildVariables(allVariables, lev);
    }

    private String conjureReturnObject(Class<?> returnClass) {
        String returnText = "null;";
        String simpleName = returnClass.getSimpleName();
        if (returnClass.isPrimitive()) {
            if ("int".equals(simpleName)) {
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
        return returnText + ONE_LINE;
    }

    private static String tab(int tabLevel) {
        String tab = "";
        for (int i = 0; i < tabLevel; i++) {
            tab = tab.concat("    ");
        }
        return tab;
    }

    private String getScope(int modifier) {
        String scope = "";
        if (Modifier.isPublic(modifier)) {
            scope = "public ";
        } else if (Modifier.isProtected(modifier)) {
            scope = "protected ";
        } else if (Modifier.isPrivate(modifier)) {
            scope = "private ";
        }
        return scope;
    }

    private List<String> getReturnTypeAndParams(Method method) {//needs more testing in different scenarios
        List<String> results = new ArrayList<>();
        String[] contents = method.toString().split(" ");
        int startIndex = 1; //refine...not sure about when we're extending concrete classes...
        for (String modifier : Arrays.asList("abstract", "final", "native", "static", "default")) {
            for (String item : contents) {
                if (modifier.equalsIgnoreCase(item)) {
                    startIndex++;
                }
            }
        }
        contents[startIndex] = getClassSimpleName(contents[startIndex]);
        //assume for now that contents[startIndex] is the return type; sample: "com.foo.Clazz.doMethod(java.lang.Object)"
        results.add(contents[startIndex]);
        if (contents.length > startIndex+1) {//remove this if always true
            String[] params = contents[startIndex + 1].split("\\(");
            params = params[1].split(",");
            String lastParam = params[params.length - 1];
            params[params.length - 1] = lastParam.substring(0, lastParam.length()-1);
            for (String item : params) {
                if (!"".equals(item)) {
                    if (Iterables.contains(getClassSimpleName(item)) && !method.toGenericString().contains("?")) {
                        item += ("<" + getParamType(method.toGenericString()) + ">");
                    }
                    results.add(item);
                }
            }
        }
        return results;
    }

    private Map<JVariable, Boolean> getParams(List<String> parameterTypes, boolean makeSetter, Set<Class> imports) {
        Map<JVariable, Boolean> variables = new HashMap<>();
        builder.append("(");
        String param, simpleName;//param as String was like "java.lang.String" - get this from Class object instead.
        Map<Integer, Integer> paramCounts = new HashMap<>();
        int typeCount;
        for (int i = 0, parameterTypesLength = parameterTypes.size(); i < parameterTypesLength; i++) {
            param = parameterTypes.get(i);
            simpleName = getClassSimpleName(dollarToDot(param));
            builder.append(simpleName);
            builder.append(" ");

            String trimmedParam = cleanParamString(simpleName);
            String varName = getVarName(trimmedParam);
            if (paramCounts.get(param.hashCode()) == null) {
                paramCounts.put(param.hashCode(), 0);
            } else {
                typeCount = paramCounts.get(param.hashCode())+1;
                paramCounts.put(param.hashCode(), typeCount);
                varName += typeCount;
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
                        //figure out what to report
                    }
                }
            }
        }
        builder.append(")");
        return variables;
    }

    private static String dollarToDot(String param) {
        return param.replaceAll("\\$", ".");
    }

    private String cleanParamString(String string) {
        return string.replace("[]", "").replaceAll("<.*>", "");
    }

    private String getVarName(String param) {
        char[] paramCopy = JavaKeywords.replaceJavaKeyword(param).toCharArray();
        paramCopy[0] = Character.toLowerCase(paramCopy[0]);
        return new String(paramCopy);
    }

    private void generateForLoopAndIfStatements(Map<JVariable, Boolean> variables, String methodParamType, int tabLev) {
        boolean useEmptyBody = true;
        String iterable;
        for (JVariable jVariable : variables.keySet()) {
            iterable = cleanParamString(jVariable.getType());
            if (!"".equals(methodParamType) && Iterables.contains(iterable) && !methodParamType.startsWith("?")) {//todo deal with ?
                useEmptyBody = false;
                builder.append(tab(tabLev)).append("for (")
                        .append(methodParamType).append(" ").append(getVarName(methodParamType))
                        .append(" : ")
                        .append(iterable.toLowerCase())
                        .append(") {").append(ONE_LINE);
                builder.append(tab(tabLev+1)).append(EMPTY_BODY).append(ONE_LINE);
                builder.append(tab(tabLev)).append("}").append(ONE_LINE);
            } else if (iterable.equalsIgnoreCase("boolean")) {
                String param = jVariable.getName();
                generateIfStatement(2, param, EMPTY_BODY); //caution, multiple boolean params will cause problem; hard-coded tab-lev
            }
        }
        if (useEmptyBody) {
            builder.append(tab(tabLev)).append(EMPTY_BODY).append(ONE_LINE);
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
        int lineBegin = builder.indexOf(VARIABLE_PLACEHOLDER);
        int lineEnd = lineBegin + VARIABLE_PLACEHOLDER.length();
        String newLine = String.valueOf(TWO_LINES);
        for (JVariable variable : variables) {
            newLine += (tab(tabLevel) + variable.writeOut() + ";\n");
        }
        builder.replace(lineBegin, lineEnd, newLine);
    }

    private void generateIfStatement(int tabLev, String statement, String body) {
        builder.append(tab(tabLev)).append("if (")
                .append(StringUtil.isEmpty(statement) ? "true" : statement)
                .append(") {")
                .append(ONE_LINE)
                .append(tab(tabLev+1)).append(body)
                .append(ONE_LINE)
                .append(tab(tabLev)).append("}").append(ONE_LINE);
    }

    private void closeBody() {
        builder.append('}').append(ONE_LINE);
    }

    private enum Iterables {

        COLLECTION("Collection"), LIST("List"), SET("Set");

        private final String refName;

        Iterables(String refName) {
            this.refName = refName;
        }

        static boolean contains(String className) {
            for (Iterables it : values()) {
                if (it.refName.equals(className)) {
                    return true;
                }
            }
           return false;
        }
    }

}
