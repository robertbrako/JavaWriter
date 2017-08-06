package com.rmbcorp.javawriter;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SampleInterface {
    @SuppressWarnings("unused")
    String setFoo(String bar, String notbar, String reallyNotBar);
    @SuppressWarnings("unused")
    void doBar(boolean visible, boolean troubler);
    @SuppressWarnings("unused")
    void typedParam(List<Set> types);
    @SuppressWarnings("unused")
    void multiTypedParam(Map<Integer, String> types);
    @SuppressWarnings("unused")
    void setClass(Class clz);
}
