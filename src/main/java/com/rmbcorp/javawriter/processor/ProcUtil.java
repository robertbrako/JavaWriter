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

import com.rmbcorp.javawriter.clazz.JMethod;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProcUtil {

    private final Pattern paramFinder = Pattern.compile("\\((.+)\\)");
    private final Pattern paramTypeFinder = Pattern.compile("(.*)<(.*)>");

    private Map<String, String> primitives;

    ProcUtil() {
        buildPrimitiveMap();
    }

    private void buildPrimitiveMap() {
        primitives = new HashMap<>(8);
        primitives.put("Void", "void");
        primitives.put("Integer", "int");
        primitives.put("Character", "char");
        primitives.put("Boolean", "boolean");
        primitives.put("Long", "long");
        primitives.put("Short", "short");
        primitives.put("Float", "float");
        primitives.put("Double", "double");
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
        ReturnParams result = new ReturnParams();
        result.setReturnType(toPrimitive(getClassSimpleName(contents[indexOfReturnType])));
        result.setParams(getParameters(method.toGenericString()));
        return result;
    }

    String getClassSimpleName(String object) {
        int begin = object.lastIndexOf('.') + 1;
        return object.substring(begin).replaceAll(";", "");
    }

    String dollarToDot(String param) {
        return param.replaceAll("\\$", ".");
    }

    String toPrimitive(String classSimpleName) {
        return primitives.get(classSimpleName) != null ? primitives.get(classSimpleName) : classSimpleName;
    }

    private int getIndexOfReturnType(String[] contents) {
        int startIndex = 1;
        for (String modifier : Arrays.asList("abstract", "final", "native", "static", "default")) {
            for (String item : contents) {
                if (modifier.equalsIgnoreCase(item)) {
                    startIndex++;
                }
            }
        }
        return startIndex;
    }

    private List<JParam> getParameters(String fullString) {
        List<JParam> jParams = new ArrayList<>(4);
        Matcher matcher = paramFinder.matcher(fullString);
        if(matcher.find()) {
            String[] paramStrings = matcher.group(1).split(",");
            for (String param : paramStrings) {
                jParams.add(getNewParam(param));
            }
        }
        return jParams;
    }

    private JParam getNewParam(String param) {
        JParam newParam;
        Matcher typeMatcher = paramTypeFinder.matcher(param);
        if (typeMatcher.find()) {
            newParam = new JParam(typeMatcher.group(1));
            String[] paramTypes = typeMatcher.group(2).split(",");
            for (String item : paramTypes) {
                newParam.add(getClassSimpleName(item));
            }
        } else {
            newParam = new JParam(param.replace("...", "[]"));
        }
        return newParam;
    }

    String getReturnType(ReturnParams returnParams) {
        String returnType = dollarToDot(returnParams.getReturnType());
        if (returnType.startsWith("<")) {
            returnType += " " + returnType.replaceAll("[<>]", "") + "[]";
        }
        return returnType;
    }

    class ReturnParams {
        String returnType = "";
        List<JParam> params = new ArrayList<>();

        void setReturnType(String returnType) {
            this.returnType = returnType;
        }

        void setParams(List<JParam> params) {
            this.params = params;
        }

        String getReturnType() {
            return returnType;
        }

        List<JParam> getParams() {
            return params;
        }
    }

    class JParam {

        private String paramType;
        private List<String> types = new ArrayList<>(2);

        JParam(String paramType) {
            this.paramType = paramType;
        }

        String getParamType() {
            return paramType;
        }

        List<String> types() {
            return types;
        }

        boolean add(String parametrizedType) {
            return types.add(parametrizedType);
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
