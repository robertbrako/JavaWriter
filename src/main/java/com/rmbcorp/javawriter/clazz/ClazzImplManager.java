package com.rmbcorp.javawriter.clazz;

import com.rmbcorp.util.ValidationManager;

/**ClazzImplManager
 * Created by rmbdev on 10/1/2016.
 */
public class ClazzImplManager {

    private static ClazzImplManager instance;

    public static ClazzImplManager getInstance() {
        return instance == null ? instance = new ClazzImplManager() : instance;
    }

    private ClazzImplManager() {
    }

    public Clazz get(String packagePath, String className) {
        return new ClazzImpl(packagePath, className);
    }

    public enum ClazzError implements ValidationManager.ErrorType {
        CANNOT_HAVE_EMPTY_CLASS_NAME,
        MUST_BE_CLASS_OR_INTERFACE,
        CANNOT_HAVE_PRIVATE_INTERFACE,
        CANNOT_BE_ABSTRACT_AND_FINAL,
        INVALID_CLASS_NAME
    }
}
