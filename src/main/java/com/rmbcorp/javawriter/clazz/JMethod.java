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
    private String comment = "";


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
        return Arrays.<Class>stream(params)
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

    public String getComment() {
        return comment;
    }

    public void setComment(String commentText) {
        comment = commentText;
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
