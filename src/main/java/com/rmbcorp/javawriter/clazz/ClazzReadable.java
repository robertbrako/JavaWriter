package com.rmbcorp.javawriter.clazz;

import java.util.Set;

public interface ClazzReadable {

    String getPackagePath();

    Set<Class> getImports();

    Clazz.Visibility getVisibility();

    boolean isFinal();

    boolean isAbstract();

    Clazz.ClassType getClassType();

    String getClassName();

    Class getExtension();

    Set<Class> getImplementations();

    Set<JMethod> getMethods();

    Set<JVariable> getBeanVariables();

    String getComments();
}
