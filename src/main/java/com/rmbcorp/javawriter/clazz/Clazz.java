package com.rmbcorp.javawriter.clazz;

import java.util.List;

/**interface to drive automated class creation
 * Created by rmbdev on 9/5/2016.
 */
public interface Clazz {
    enum Visibility {
        PUBLIC(1), PRIVATE(2), PACKAGE(0);

        private final int modifier;

        Visibility(int modifier) {
            this.modifier = modifier;
        }

        @Override
        public String toString() {
            return PACKAGE.equals(this) ? "" : name().toLowerCase();
        }

        public int getModifier() {
            return modifier;
        }
    }
    enum ClassType { CLASS, INTERFACE }

    void addImports(List<Class> imports);
    void setVisibility(Visibility visibility);
    void setFinal(boolean isFinal);
    void setAbstract(boolean isAbstract);
    void setClassType(ClassType classType);
    void addExtension(Class extension);
    void addImplementations(List<Class> implementation);
    void addMethod(JMethod jMethod);
    void addBeanVariable(JVariable variable);
    void setComments(String classComments);
}
