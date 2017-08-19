package com.rmbcorp.javawriter.processor;

import com.rmbcorp.javawriter.clazz.Clazz;
import com.rmbcorp.javawriter.clazz.EnumReadable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnumImpl implements EnumReadable {
    private String packagePath;
    private String className;
    private Clazz.Visibility visibility;
    private HashSet<String> enumConstants = new HashSet<>();

    public EnumImpl(String packagePath, String className) {
        this.packagePath = packagePath;
        this.className = className;
        visibility = Clazz.Visibility.PACKAGE;
    }

    @Override
    public String getPackagePath() {
        return packagePath;
    }

    @Override
    public Clazz.Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Clazz.Visibility visibility) {
        this.visibility = visibility;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public Set<String> getEnumConstants() {
        return enumConstants;
    }

    public void addEnumConstants(List<String> constants) {
        enumConstants.addAll(constants);
    }
}
