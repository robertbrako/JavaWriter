package com.rmbcorp.javawriter.processor;

enum Iterables {

    COLLECTION("Collection"), LIST("List"), SET("Set");

    private final String refName;

    Iterables(String refName) {
        this.refName = refName;
    }

    public static boolean contains(String className) {
        for (Iterables it : values()) {
            if (className.startsWith(it.refName)) {
                return true;
            }
        }
        return false;
    }
}
