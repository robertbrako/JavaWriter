import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collections;

/**Need to produce classes on the fly somehow...
 * Created by rmbdev on 9/5/2016.
 */
class JavaWriter {

    public static void main(String[] args) {
        Clazz clazzImpl = new ClazzImpl("com.rmbcorp.javawriter", "ClazzImpl2");
        clazzImpl.setClassType(Clazz.ClassType.CLASS);
        clazzImpl.addImports(Arrays.<Class>asList(Integer.class, String.class, String.class));
        clazzImpl.addExtension(AbstractQueue.class);
        clazzImpl.setVisibility(Clazz.Visibility.PUBLIC);
        clazzImpl.setFinal(true);
        clazzImpl.addImplementations(Collections.<Class>singletonList(Clazz.class));

        System.out.println();
        System.out.println(clazzImpl.writeOut());
    }
}

/* Current implementation based on above will automatically output the following:
package com.rmbcorp.javawriter;

import java.lang.String;
import com.rmbcorp.javawriter.Clazz;
import java.util.AbstractQueue;
import java.lang.Integer;
import java.lang.Object;
import java.util.Collection;
import java.lang.Class;
import java.util.List;
import com.rmbcorp.javawriter.Clazz.ClassType;
import com.rmbcorp.javawriter.Clazz.Visibility;

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
