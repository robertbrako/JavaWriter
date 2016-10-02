package com.rmbcorp.javawriter.clazz;

/**ClazzImplManager
 * Created by rmbdev on 10/1/2016.
 */
public class ClazzImplManager {

    private static ClazzImplManager instance;

    private final ClazzImplProcessor clazzProcessor;

    public static ClazzImplManager getInstance() {
        return instance == null ? instance = new ClazzImplManager() : instance;
    }

    private ClazzImplManager() {
        clazzProcessor = new ClazzImplProcessor();
    }

    public Clazz get(String packagePath, String className) {
        return new ClazzImpl(packagePath, className);
    }

    public String writeOut(Clazz clazz) {
        return clazz instanceof ClazzImpl ? clazzProcessor.writeOut((ClazzImpl) clazz) : "";
    }
}
