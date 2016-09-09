import java.util.List;

/**interface to drive automated class creation
 * Created by rmbdev on 9/5/2016.
 */
public interface Clazz {
    enum Visibility {
        PUBLIC, PRIVATE, PACKAGE;

        @Override
        public String toString() {
            return PACKAGE.equals(this) ? "" : name().toLowerCase().concat(" ");
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
    String writeOut();
}
