package com.rmbcorp.javawriter.clazz;

/**JVariable
 * Created by rmbdev on 10/2/2016.
 */
class JVariable {
    private String varName;
    private Clazz.Visibility visibility;
    private String classType;

    JVariable(String varName, String classType) {
        visibility = Clazz.Visibility.PRIVATE;
        this.varName = varName;
        this.classType = classType;
    }

    String writeOut() {
        return visibility + classType + " " + varName;
    }

    String getName() {
        return varName;
    }

    String getType() {
        return classType;
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
