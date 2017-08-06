package com.rmbcorp.util;

import java.util.Arrays;

public class StringUtil {

    private StringUtil() { }

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static boolean containsAll(String[] superSet, String[] subSet) {
        return Arrays.stream(subSet)
                .filter(Arrays.asList(superSet)::contains)
                .count() == subSet.length;
    }
}
