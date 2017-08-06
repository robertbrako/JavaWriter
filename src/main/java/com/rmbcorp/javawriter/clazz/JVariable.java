package com.rmbcorp.javawriter.clazz;

/**JVariable
 * Created by rmbdev on 10/2/2016.
 */
public class JVariable {
    private String varName;
    private Clazz.Visibility visibility;
    private Class<?> classType;
    private String classTypeSimple;

    public JVariable(String varName, String classType) {//deprecation is being considered
        visibility = Clazz.Visibility.PRIVATE;
        this.varName = varName;
        this.classTypeSimple = classType;
    }

    public JVariable(String varName, Class<?> classType) {
        visibility = Clazz.Visibility.PRIVATE;
        this.varName = varName;
        this.classType = classType;
        classTypeSimple = getClassSimpleName(classType.getSimpleName());
    }

    public String getName() {
        return varName;
    }

    public String getVisibility() {
        return visibility.toString();
    }

    public Class<?> getClassType() {
        return classType;
    }

    public String getType() {
        return classTypeSimple;
    }

    private String getClassSimpleName(String object) {
        int begin = object.lastIndexOf('.') + 1;
        return object.substring(begin).replaceAll(";", "");
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JVariable && varName.equals(((JVariable) obj).getName());
    }

    @Override
    public int hashCode() {
        return varName.hashCode();
    }
}
