package com.rmbcorp.javawriter.clazz;

import java.util.Set;

public interface EnumReadable {
    String getPackagePath();

    Clazz.Visibility getVisibility();

    String getClassName();

    Set<String> getEnumConstants();

}
