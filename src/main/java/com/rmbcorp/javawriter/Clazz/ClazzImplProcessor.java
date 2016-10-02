package com.rmbcorp.javawriter.clazz;

import com.rmbcorp.util.StringUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**ClazzImplProcessor
 * Created by rmbdev on 10/1/2016.
 */
class ClazzImplProcessor {

    private static final char ONE_LINE = '\n';
    private static final char[] TWO_LINES = { '\n', '\n' };

    private StringBuilder builder;

    String writeOut(ClazzImpl clazz) {
        builder = new StringBuilder("");
        buildPackage(clazz.getPackagePath());
        setupImports(clazz.getImports(), clazz.getExtension(), clazz.getImplementations());
        buildImports(clazz.getImports());
        buildClassOrInterface(clazz.getClassType(), clazz);
        buildBody(clazz.getClassName(), clazz.getExtension(), clazz.getImplementations(), clazz.getImports());
        closeBody();

        return builder.toString();
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
        if (classType.equals(Clazz.ClassType.INTERFACE)) {
            buildInterface(clazz.getVisibility(), clazz.getClassName());
        } else if (classType.equals(Clazz.ClassType.CLASS)) {
            buildClass(clazz.getClassName(), clazz.getVisibility(), clazz.isFinal(), clazz.isAbstract(), clazz.getExtension());
        }
        builder.append(" {");
        buildImplementations(classType, clazz.getImplementations());
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
        }
    }

    private void buildInterface(Clazz.Visibility visibility, String className) {
        if (Clazz.Visibility.PUBLIC.equals(visibility)) {
            builder.append(visibility.toString()).append(' ');
        }
        builder.append("interface ").append(className);
    }

    private void buildImplementations(Clazz.ClassType classType, Set<Class> implementations) {
        if (!implementations.isEmpty()) {
            builder.append(classType.equals(Clazz.ClassType.CLASS) ? " implements " : " extends ");
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

    private void buildBody(String className, Class extension, Set<Class> implementations, Set<Class> imports) {
        int lev = 1;
        builder.append(TWO_LINES);
        if (extension != null) {
            implementations.add(extension);
        }
        for (Class clazz : implementations) {
            Method[] methods = clazz.getDeclaredMethods();
            String methodName;
            List<String> variables;
            for (Method method : methods) {
                methodName = method.getName();
                builder.append(tab(lev)).append("@Override").append(ONE_LINE);
                builder.append(tab(lev)).append(getScope(method.getModifiers()))
                        .append(getReturnType(method.getReturnType())).append(' ').append(methodName);
                variables = getParams(method.getParameterTypes(), builder, methodName.startsWith("set"), className, imports);
                builder.append(" {").append(ONE_LINE);
                lev++;
                for (String var : variables) {
                    builder.append(tab(lev)).append("this.").append(var).append(" = ").append(var).append(";").append(ONE_LINE);
                }
                generateForLoop(method.getParameterTypes(), lev);
                lev--;
                builder.append(tab(lev)).append("}").append(TWO_LINES);
            }
        }
        implementations.remove(extension);
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

    private List<String> getParams(Class<?>[] parameterTypes, StringBuilder builder, boolean makeSetter, String className, Set<Class> imports) {
        List<String> variables = new ArrayList<>();
        builder.append("(");
        for (Class<?> parameterType : parameterTypes) {
            String param = getClassSimpleName(parameterType.toString()); // need non-empty
            param = dollarToDot(param);
            builder.append(param);
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
                variables.add(varName);
            }
            if (imports.add(parameterType)) {
                sneakInImport(parameterType, className);
            }
        }
        trimComma(builder);
        builder.append(")");
        return variables;
    }

    private static String dollarToDot(String param) {
        return param.replaceAll("\\$", ".");
    }

    private void sneakInImport(Class<?> parameterType, String className) {
        String importSection = builder.substring(0, builder.indexOf(className));
        int index = importSection.lastIndexOf("import");
        index = importSection.indexOf(';', index) + 1;
        String importName = splitClassString(parameterType);
        if (importName.equals(JavaKeywords.replaceJavaKeyword(importName))) {
            builder.insert(index, String.valueOf(ONE_LINE).concat("import ").concat(dollarToDot(importName).concat(";")));
        }
    }

    private void generateForLoop(Class<?>[] parameterTypes, int tabLev) {
        boolean useEmptyBody = true;
        for (Class parameterType : parameterTypes) {
            if (parameterType.toString().contains("Collection")) {
                useEmptyBody = false;
                builder.append(tab(tabLev)).append("for (Object object : collection) {").append(ONE_LINE);
                builder.append(tab(tabLev+1)).append("//empty").append(ONE_LINE);
                builder.append(tab(tabLev)).append("}").append(ONE_LINE);
            }
        }
        if (useEmptyBody) {
            builder.append(tab(tabLev)).append("//empty").append(ONE_LINE);
        }
    }

    private void closeBody() {
        builder.append('}').append(ONE_LINE);
    }
}
