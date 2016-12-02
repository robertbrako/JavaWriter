package com.rmbcorp.javawriter.clazz;

import com.rmbcorp.util.StringUtil;
import com.rmbcorp.util.ValidationManager;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import static com.rmbcorp.javawriter.clazz.ClazzImplManager.ClazzError;

/**ClazzImplProcessor
 * Created by rmbdev on 10/1/2016.
 */
final class ClazzImplProcessor {

    private static final char ONE_LINE = '\n';
    private static final char[] TWO_LINES = { '\n', '\n' };
    public static final String VARIABLE_PLACEHOLDER = "//%%VARIABLES%%";

    private StringBuilder builder;
    private ValidationManager<ClazzError> validator;
    
    ClazzImplProcessor(ValidationManager<ClazzError> validator) {
        this.validator = validator;
    }

    String writeOut(ClazzImpl clazz) {
        builder = new StringBuilder("");
        buildPackage(clazz.getPackagePath());
        setupImports(clazz.getImports(), clazz.getExtension(), clazz.getImplementations());
        buildImports(clazz.getImports());
        buildClassOrInterface(clazz.getClassType(), clazz);
        buildBody(clazz.getExtension(), clazz.getImplementations(), clazz.getImports());
        closeBody();

        return validator.hasErrors() ? "" : builder.toString();
    }

    private void buildPackage(String packagePath) {
        if (!StringUtil.isEmpty(packagePath)) {
            builder.append("package ").append(packagePath).append(';');
            builder.append(TWO_LINES);
        }
    }

    private void setupImports(Set<Class> imports, Class extension, Set<Class> implementations) {
        if (extension != null) {
            imports.add(extension);
        }
        imports.addAll(implementations);
    }

    private void buildImports(Set<Class> imports) {
        for (Object object : imports) {
            builder.append("import ").append(splitClassString(object)).append(';');
            builder.append(ONE_LINE);
        }
        builder.append(ONE_LINE);
    }

    private static String splitClassString(Object object) {
        return object.toString().replaceFirst("class ", "").replaceFirst("interface ", "");
    }

    private void buildClassOrInterface(Clazz.ClassType classType, ClazzImpl clazz) {
        if (StringUtil.isEmpty(clazz.getClassName())) {
            validator.addResult(ClazzError.CANNOT_HAVE_EMPTY_CLASS_NAME);
            return;
        }
        if (Clazz.ClassType.INTERFACE.equals(classType)) {
            buildInterface(clazz.getVisibility(), clazz.getClassName());
        } else if (Clazz.ClassType.CLASS.equals(classType)) {
            buildClass(clazz.getClassName(), clazz.getVisibility(), clazz.isFinal(), clazz.isAbstract(), clazz.getExtension());
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
                builder.append(getClassSimpleName(object.toString()));
                builder.append(", ");
            }
            trimComma(builder);
        }
    }

    private static void trimComma(StringBuilder result) {
        int length = result.lastIndexOf(",");
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
        Set<JVariable> variables;
        for (Class clazz : implementations) {
            Method[] methods = clazz.getDeclaredMethods();
            String methodName, methodParamType;
            for (Method method : methods) {
                methodName = method.getName();
                builder.append(tab(lev)).append("@Override").append(ONE_LINE);
                builder.append(tab(lev)).append(getScope(method.getModifiers()))
                        .append(getReturnType(method.getReturnType())).append(' ').append(methodName);
                methodParamType = getParamType(method.toGenericString());
                variables = getParams(method.getParameterTypes(), methodParamType, methodName.startsWith("set"), imports);
                allVariables.addAll(variables);
                builder.append(" {").append(ONE_LINE);
                lev++;
                String varName;
                for (JVariable var : variables) {
                    varName = var.getName();
                    builder.append(tab(lev)).append("this.").append(varName).append(" = ").append(varName).append(";").append(ONE_LINE);
                }
                generateForLoop(method.getParameterTypes(), methodParamType, lev);
                lev--;
                builder.append(tab(lev)).append("}").append(TWO_LINES);
            }
        }
        implementations.remove(extension);
        buildVariables(allVariables, lev);
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

    private String getReturnType(Class<?> returnType) {
        String result = returnType.toString();
        return result.contains("[L") ? "List<".concat(getClassSimpleName(result)).concat(">") :
                getClassSimpleName(result);
    }

    private Set<JVariable> getParams(Class<?>[] parameterTypes, String methodParamType, boolean makeSetter, Set<Class> imports) {
        Set<JVariable> variables = new HashSet<>();
        builder.append("(");
        for (Class<?> parameterType : parameterTypes) {
            String param = getClassSimpleName(parameterType.toString()); // need non-empty
            param = dollarToDot(param);
            builder.append(param);
            if (!"".equals(methodParamType)) {
                builder.append('<').append(methodParamType).append('>');
            }
            builder.append(" ");
            param = param.substring(param.lastIndexOf('.') + 1);

            String varName = JavaKeywords.replaceJavaKeyword(param);
            if (varName.equals(param)) {
                char[] paramCopy = varName.toCharArray();
                paramCopy[0] = Character.toLowerCase(paramCopy[0]);
                varName = new String(paramCopy);
            }
            builder.append(varName);
            builder.append(", ");
            if (makeSetter) {
                variables.add(new JVariable(varName, param));
            }
            if (imports.add(parameterType)) {
                sneakInImport(parameterType);
            }
        }
        trimComma(builder);
        builder.append(")");
        return variables;
    }

    private static String dollarToDot(String param) {
        return param.replaceAll("\\$", ".");
    }

    private void sneakInImport(Class<?> parameterType) {
        String importSection = builder.substring(0, builder.indexOf("{"));
        int index = importSection.lastIndexOf("import");
        index = importSection.indexOf(';', index) + 1;
        String importName = splitClassString(parameterType);
        if (importName.equals(JavaKeywords.replaceJavaKeyword(importName))) {
            builder.insert(index, String.valueOf(ONE_LINE).concat("import ").concat(dollarToDot(importName).concat(";")));
        }
    }

    private void generateForLoop(Class<?>[] parameterTypes, String methodParamType, int tabLev) {
        boolean useEmptyBody = true;
        String iterable;
        for (Class<?> parameterType : parameterTypes) {
            iterable = Iterables.contains(parameterType.getSimpleName());
            if (!"".equals(methodParamType)) {
                useEmptyBody = false;
                builder.append(tab(tabLev)).append("for (")
                        .append(methodParamType).append(" ").append(JavaKeywords.replaceJavaKeyword(methodParamType))
                        .append(" : ")
                        .append(iterable.toLowerCase())
                        .append(") {").append(ONE_LINE);
                builder.append(tab(tabLev+1)).append("//empty").append(ONE_LINE);
                builder.append(tab(tabLev)).append("}").append(ONE_LINE);
            }
        }
        if (useEmptyBody) {
            builder.append(tab(tabLev)).append("//empty").append(ONE_LINE);
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

    private void closeBody() {
        builder.append('}').append(ONE_LINE);
    }

    private enum Iterables {

        COLLECTION("Collection"), LIST("List"), SET("Set");

        private final String refName;

        Iterables(String refName) {
            this.refName = refName;
        }

        static String contains(String className) {
            for (Iterables it : values()) {
                if (it.refName.equals(className)) {
                    return className;
                }
            }
           return "Object";
        }
    }

}
