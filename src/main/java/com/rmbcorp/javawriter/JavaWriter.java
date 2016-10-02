package com.rmbcorp.javawriter;

import com.rmbcorp.javawriter.clazz.Clazz;
import com.rmbcorp.javawriter.clazz.ClazzImplManager;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collections;

/**Need to produce classes on the fly somehow...
 * Created by rmbdev on 9/5/2016.
 */
class JavaWriter {

    public static void main(String[] args) {
        ClazzImplManager clazzManager = ClazzImplManager.getInstance();
        Clazz clazz = clazzManager.get("com.rmbcorp.javawriter", "ClazzImpl2");
        clazz.setClassType(Clazz.ClassType.CLASS);
        clazz.addImports(Arrays.<Class>asList(Integer.class, String.class, String.class));
        clazz.addExtension(AbstractQueue.class);
        clazz.setVisibility(Clazz.Visibility.PUBLIC);
        clazz.setFinal(true);
        clazz.addImplementations(Collections.<Class>singletonList(Clazz.class));

        System.out.println();
        System.out.println(clazzManager.writeOut(clazz));
    }
}