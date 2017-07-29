package com.rmbcorp.javawriter.clazz;

import com.rmbcorp.util.ValidationManager;

public enum ClazzError implements ValidationManager.ErrorType {
    CANNOT_HAVE_EMPTY_CLASS_NAME,
    MUST_BE_CLASS_OR_INTERFACE,
    CANNOT_HAVE_PRIVATE_INTERFACE,
    CANNOT_BE_ABSTRACT_AND_FINAL,
    INVALID_CLASS_NAME
}
