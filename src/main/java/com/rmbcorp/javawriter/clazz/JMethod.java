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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JMethod {

    private final Method method;
    private String name;
    private Class<?> returnType;
    private int modifier;
    private String asGenericString;
    private List<Class<?>> params;


    public JMethod(Method method) {
        this.method = method;
        name = method.getName();
        returnType = method.getReturnType();
        modifier = method.getModifiers();
        asGenericString = method.toGenericString();
        params = Arrays.asList(method.getParameterTypes());
    }

    public JMethod(String name, Class<?> returnType, Clazz.Visibility visibility, String packageName, Class<?>... params) {
        this.method = null;
        this.name = name;
        this.returnType = returnType;
        this.modifier = visibility.getModifier();
        this.asGenericString = visibility + " " + returnType.getCanonicalName() + " " + packageName + "." + name + String.format("(%s)", getParams(params));
        this.params = Arrays.asList(params);
    }

    private String getParams(Class<?>[] params) {
        return Arrays.<Class>asList(params).stream()
                .map(Class::getCanonicalName)
                .collect(Collectors.joining(","));
    }

    public String getName() {
        return name;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public int getModifier() {
        return modifier;
    }

    public String toGenericString() {
        return asGenericString;
    }

    public boolean isOverride() {
        return method != null;
    }

    @Override
    public String toString() {
        return method != null ? method.toString() : asGenericString;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JMethod && name.equals(((JMethod) obj).name) && params.size() == ((JMethod)obj).params.size();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
