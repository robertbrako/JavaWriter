package com.rmbcorp.javawriter.clazz;

import com.rmbcorp.util.ValidationManager;

/**ClazzImplManager
 * Created by rmbdev on 10/1/2016.
 */
public class ClazzImplManager {

    private static ClazzImplManager instance;

    private final ClazzValidator validator;
    private final ClazzImplProcessor clazzProcessor;
    private String errorCache;

    public static ClazzImplManager getInstance() {
        return instance == null ? instance = new ClazzImplManager() : instance;
    }

    private ClazzImplManager() {
        validator = new ClazzValidator();
        clazzProcessor = new ClazzImplProcessor(validator);
    }

    public Clazz get(String packagePath, String className) {
        return new ClazzImpl(packagePath, className);
    }

    public String writeOut(Clazz clazz) {
        errorCache = "";
        if (clazz instanceof ClazzImpl) {
            String out = clazzProcessor.writeOut((ClazzImpl) clazz);
            if (validator.hasErrors()) {
                errorCache = validator.getErrorsAsCSV();
                validator.removeAllResults();
            }
            return out;
        }
        else return "";
    }

    public boolean hasError(ClazzError error) {
        return errorCache.contains(error.name());
    }

    public enum ClazzError implements ValidationManager.ErrorType {
        CANNOT_HAVE_EMPTY_CLASS_NAME,
        MUST_BE_CLASS_OR_INTERFACE,
        CANNOT_HAVE_PRIVATE_INTERFACE,
        CANNOT_BE_ABSTRACT_AND_FINAL
    }
}
