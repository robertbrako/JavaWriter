package com.rmbcorp.javawriter.clazz;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**Homebrew wrapper for classes
 * Created by rmbdev on 8/11/2016.
 */
class ClazzImpl implements Clazz, ClazzReadable {

    private final String packagePath;
    private final Set<Class> imports = new HashSet<>();
    private Clazz.Visibility visibility = Clazz.Visibility.PACKAGE;
    private boolean isFinal = false;
    private boolean isAbstract = false;
    private Clazz.ClassType classType;
    private final String className;
    private Class extension;
    private final Set<Class> implementations = new HashSet<>();
    private final Set<JMethod> methods = new HashSet<>();
    private final Set<JVariable> variables = new HashSet<>();


    ClazzImpl(String packagePath, String className) {
        this(packagePath, className, ClassType.CLASS);
    }

    ClazzImpl(String packagePath, String className, ClassType classType) {
        this.packagePath = packagePath;
        this.className = className;
        this.classType = classType;
    }

    @Override
    public void addImports(List<Class> imports) {
        this.imports.addAll(imports);
    }

    @Override
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    @Override
    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    @Override
    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    @Override
    public void setClassType(ClassType classType) {
        this.classType = classType;
    }

    @Override
    public void addExtension(Class extension) {
        this.extension = extension;
    }

    @Override
    public void addImplementations(List<Class> implementations) {
        this.implementations.addAll(implementations);
    }

    @Override
    public void addMethod(JMethod jMethod) {
        methods.add(jMethod);
    }

    @Override
    public void addBeanVariable(JVariable variable) {
        variables.add(variable);
    }

    @Override
    public String getPackagePath() {
        return packagePath;
    }
    
    @Override
    public Set<Class> getImports() {
        return imports;
    }
    
    @Override
    public Visibility getVisibility() {
        return visibility;
    }
    
    @Override
    public boolean isFinal() {
        return isFinal;
    }
    
    @Override
    public boolean isAbstract() {
        return isAbstract;
    }
    
    @Override
    public ClassType getClassType() {
        return classType;
    }
    
    @Override
    public String getClassName() {
        return className;
    }
    
    @Override
    public Class getExtension() {
        return extension;
    }
    
    @Override
    public Set<Class> getImplementations() {
        return implementations;
    }

    @Override
    public Set<JMethod> getMethods() {
        return methods;
    }

    @Override
    public Set<JVariable> getBeanVariables() {
        return variables;
    }
}
