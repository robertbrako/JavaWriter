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
package com.rmbcorp.javawriter.clazz;

import com.rmbcorp.javawriter.clazz.Clazz;

import java.lang.reflect.Method;
import java.util.List;

public class JMethod {

    private final Method method;
    private String name;
    private Class<?> returnType;
    private int modifier;
    private String asGenericString;


    public JMethod(Method method) {
        this.method = method;
    }

    public JMethod(String name, Class<?> returnType, Clazz.Visibility visibility, String packageName, List<Class<?>> params) {
        this(null);
        this.name = name;
        this.returnType = returnType;
        this.modifier = visibility.getModifier();
        this.asGenericString = visibility + " " + returnType.getCanonicalName() + " " + packageName + "." + name + String.format("(%s)", getParams(params));
    }

    private String getParams(List<Class<?>> params) {
        String result = "";
        for (int i = 0; i < params.size(); i++) {
            String comma = i == params.size() - 1 ? "" : ",";
            result += params.get(i).getCanonicalName() + comma;
        }
        return result;
    }

    public String getName() {
        return method != null ? method.getName() : name;
    }

    public Class<?> getReturnType() {
        return method != null ? method.getReturnType() : returnType;
    }

    public int getModifier() {
        return method != null ? method.getModifiers() : modifier;
    }

    public String toGenericString() {
        return method != null ? method.toGenericString() : asGenericString;
    }

    public boolean isOverride() {
        return method != null;
    }

    @Override
    public String toString() {
        return method != null ? method.toString() : asGenericString;
    }
}
