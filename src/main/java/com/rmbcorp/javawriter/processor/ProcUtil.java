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

import com.rmbcorp.javawriter.clazz.ClazzError;
import com.rmbcorp.javawriter.clazz.JMethod;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProcUtil {

    private final Pattern subclassFinder = Pattern.compile("(.*[A-Z]\\w*)\\.([A-Z]\\w*)");

    private Map<String, String> primitives;

    ProcUtil() {
        buildPrimitiveMap();
    }

    private void buildPrimitiveMap() {
        primitives = new HashMap<>(10);
        primitives.put("Void", "void");
        primitives.put("Integer", "int");
        primitives.put("Character", "char");
        primitives.put("Boolean", "boolean");
        primitives.put("Long", "long");
        primitives.put("Short", "short");
        primitives.put("Float", "float");
        primitives.put("Double", "double");
        primitives.put("Byte", "byte");
    }

    String getVarName(String param) {
        char[] paramCopy = JavaKeywords.replaceJavaKeyword(param).toCharArray();
        paramCopy[0] = Character.toLowerCase(paramCopy[0]);
        return new String(paramCopy);
    }

    String tab(int tabLevel) {
        String tab = "";
        for (int i = 0; i < tabLevel; i++) {
            tab = tab.concat("    ");
        }
        return tab;
    }

    String getScope(int modifier) {
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

    ReturnParams getReturnAndParams(JMethod method) {
        String[] contents = method.toGenericString().split(" ");
        int indexOfReturnType = getIndexOfReturnType(contents);

        String returnType = dollarToDot(toPrimitive(getClassSimpleName(contents[indexOfReturnType])));
        if (returnType.startsWith("<")) {
            returnType += " " + returnType.replaceAll("[<>]", "") + "[]";
        }
        JParam returnInfo = new JParam(contents[indexOfReturnType], returnType);
        Arrays.stream(contents[indexOfReturnType].replaceAll(".*<(.*)>.*", "$1").split(","))
                .filter(type -> !primitives.values().contains(type) && type.length() > 1) //don't import E from Set<E>
                .forEach(returnInfo::add);
        ReturnParams result = new ReturnParams();
        result.setReturnType(returnInfo);
        result.setParams(getParameters(method.toGenericString().replaceFirst("\\sthrows.*", "")));
        return result;
    }

    String getClassSimpleName(String object) {
        return object.replaceAll("(\\w+\\.)+(\\w+)", "$2");
    }

    String dollarToDot(String param) {
        return param.replaceAll("\\$", ".");
    }

    String toPrimitive(String classSimpleName) {
        return primitives.get(classSimpleName) != null ? primitives.get(classSimpleName) : classSimpleName;
    }

    private int getIndexOfReturnType(String[] contents) {
        int startIndex = 1;
        for (String modifier : Arrays.asList("abstract", "final", "native", "static", "default", "synchronized")) {
            for (String item : contents) {
                if (modifier.equalsIgnoreCase(item)) {
                    startIndex++;
                }
            }
        }
        return contents.length > 2 ? startIndex : 0;
    }

    private List<JParam> getParameters(String fullString) {
        String[] pieces = fullString.split("[(),]");
        if (pieces.length < 2) {
            return new ArrayList<>();
        }
        List<JParam> result = new ArrayList<>();
        boolean needNew = true;
        JParam current = null;
        for (int i = 1; i < pieces.length; i++) {
            if (needNew) {
                String[] paramType = pieces[i].split("<");
                current = new JParam(paramType[0].trim(), getClassSimpleName(paramType[0]));
                result.add(current);
                if (paramType.length > 1) {
                    current.add(paramType[1].replace(">", "").trim());
                }
                needNew = paramType.length == 1 || pieces[i].contains(">");
            } else {
                String trimmed = pieces[i].replaceAll("[> ]", "");
                current.add(trimmed);
                needNew = !trimmed.equals(pieces[i]);
            }
        }
        return result;
    }

    void insertImports(ClassBuilder builder, Set<Class> imports, JParam returnTypeInfo) {
        returnTypeInfo.fullTypes().forEach(type -> insertImport(builder, imports, type));
        insertImport(builder, imports, returnTypeInfo.getFullParamType());
    }

    void insertImport(ClassBuilder builder, Set<Class> imports, String param) {
        String cleanParam = dollarToDot(cleanParamString(param));
        if (!JavaKeywords.replaceJavaKeywordSafe(cleanParam).equals(cleanParam) || cleanParam.length() < 2
                || cleanParam.startsWith("?") || imports.stream().map(Class::getCanonicalName).anyMatch(param::equals))
            return;
        Matcher matcher = subclassFinder.matcher(cleanParam);
        if (matcher.find()) {
            cleanParam = matcher.group(1);
        }
        try {
            imports.add(Class.forName(cleanParam));
        } catch (ClassNotFoundException ignored) {
            Logger.getGlobal().log(Level.INFO, ignored.getLocalizedMessage(), ignored);
            builder.addResult(ClazzError.INVALID_CLASS_NAME);
        }
    }

    private String cleanParamString(String string) {
        return string.replaceAll("\\[]", "").replaceAll("<.*>", "");
    }

    class ReturnParams {
        JParam returnType;
        List<JParam> params = new ArrayList<>();

        void setReturnType(JParam returnType) {
            this.returnType = returnType;
        }

        void setParams(List<JParam> params) {
            this.params = params;
        }

        JParam getReturnType() {
            return returnType;
        }

        List<JParam> getParams() {
            return params;
        }
    }

    class JParam {

        private final String paramType;
        private final String fullyQualifiedParamType;
        private List<String> types = new ArrayList<>(2);
        private List<String> fullTypes = new ArrayList<>(2);

        JParam(String fullQualifiedParamType, String paramType) {
            this.fullyQualifiedParamType = fullQualifiedParamType.replace("...", "[]");
            this.paramType = paramType;
        }

        String getFullParamType() {
            return fullyQualifiedParamType;
        }

        String getParamType() {
            return paramType;
        }

        List<String> types() {
            return types;
        }

        List<String> fullTypes() {
            return fullTypes;
        }

        boolean add(String parametrizedType) {
            return fullTypes.add(parametrizedType) && types.add(getClassSimpleName(parametrizedType));
        }

        @Override
        public int hashCode() {
            return paramType.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof JParam && paramType.equals(((JParam) obj).getParamType());
        }
    }
}
