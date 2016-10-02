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

/* Current implementation based on above will automatically output the following:
package com.rmbcorp.javawriter;

import java.lang.String;
import com.rmbcorp.javawriter.clazz.Clazz;
import java.util.AbstractQueue;
import java.lang.Integer;
import java.lang.Object;
import java.util.Collection;
import java.lang.Class;
import java.util.List;
import com.rmbcorp.javawriter.clazz.Clazz.ClassType;
import com.rmbcorp.javawriter.clazz.Clazz.Visibility;

public final class ClazzImpl2 extends AbstractQueue implements Clazz  {

    @Override
    public boolean add(Object object ) {
        //empty
    }

    @Override
    public Object remove() {
        //empty
    }

    @Override
    public void clear() {
        //empty
    }

    @Override
    public boolean addAll(Collection collection ) {
        for (Object object : collection) {
            //empty
        }
    }

    @Override
    public Object element() {
        //empty
    }

    @Override
    public void addExtension(Class clazz ) {
        //empty
    }

    @Override
    public void addImports(List list ) {
        //empty
    }

    @Override
    public void setClassType(Clazz.ClassType classType ) {
        this.classType = classType;
        //empty
    }

    @Override
    public void setFinal(boolean bool ) {
        this.bool = bool;
        //empty
    }

    @Override
    public void setVisibility(Clazz.Visibility visibility ) {
        this.visibility = visibility;
        //empty
    }

    @Override
    public void addImplementations(List list ) {
        //empty
    }

    @Override
    public void setAbstract(boolean bool ) {
        this.bool = bool;
        //empty
    }

}
 */
