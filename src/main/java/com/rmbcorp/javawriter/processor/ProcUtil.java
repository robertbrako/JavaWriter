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

public class ProcUtil {

    public static final String GEN_FOLDER = "src/gen/";
    public static final String BIN_FOLDER = "bin/";

    final String IMPORT_PLACEHOLDER = "//%%IMPORTS%%";
    final String VARIABLE_PLACEHOLDER = "//%%VARIABLES%%";
    final char ONE_LINE = '\n';
    final char[] TWO_LINES = { '\n', '\n' };
    Map<String, String> primitives;

    public ProcUtil() {
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
        String[] contents = method.toString().split(" ");
        int indexOfReturnType = getIndexOfReturnType(contents);
        ReturnParams result = new ReturnParams();
        result.setReturnType(toPrimitive(getClassSimpleName(contents[indexOfReturnType])));
        result.setParams(getPreParams(contents, indexOfReturnType + 1));
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

    private List<String> getPreParams(String[] contents, int startIndex) {
        List<String> preParams = new ArrayList<>();//see if we can use a sensible default size
        String[] params = contents[startIndex].split("\\(");
        params = params[1].split(",");
        String lastParam = params[params.length - 1];
        params[params.length - 1] = lastParam.substring(0, lastParam.length()-1);
        for (String item : params) {
            if (!"".equals(item)) {
                preParams.add(item);
            }
        }
        return preParams;
    }

    class ReturnParams {
        String returnType = "";
        List<String> params = new ArrayList<>();

        void setReturnType(String returnType) {
            this.returnType = returnType;
        }

        void setParams(List<String> params) {
            this.params = params;
        }

        String getReturnType() {
            return returnType;
        }

        List<String> getParams() {
            return params;
        }
    }
}
