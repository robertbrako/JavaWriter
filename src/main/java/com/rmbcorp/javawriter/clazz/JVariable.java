package com.rmbcorp.javawriter.clazz;

/**JVariable
 * Created by rmbdev on 10/2/2016.
 */
public class JVariable {
    private String varName;
    private Clazz.Visibility visibility;
    private String classType;

    public JVariable(String varName, String classType) {
        visibility = Clazz.Visibility.PRIVATE;
        this.varName = varName;
        this.classType = classType;
    }

    public String writeOut() {
        return visibility + " " + classType + " " + varName;
    }

    public String getName() {
        return varName;
    }

    public String getType() {
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
