package com.rmbcorp.javawriter.clazz;

import com.rmbcorp.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.rmbcorp.javawriter.clazz.ClazzImplManager.ClazzError.CANNOT_BE_ABSTRACT_AND_FINAL;
import static com.rmbcorp.javawriter.clazz.ClazzImplManager.ClazzError.CANNOT_HAVE_EMPTY_CLASS_NAME;
import static com.rmbcorp.javawriter.clazz.ClazzImplManager.ClazzError.MUST_BE_CLASS_OR_INTERFACE;
import static org.junit.Assert.assertTrue;

/**ClazzImplTest
 * Created by rmbdev on 10/1/2016.
 */
public class ClazzImplTest {

    public static final Class<Integer> INTEGER_CLASS = Integer.class;
    public static final Class<String> STRING_CLASS = String.class;
    ClazzImplManager clazzManager;
    Clazz clazz;

    @Before
    public void setUp() throws Exception {
        clazzManager = ClazzImplManager.getInstance();
        clazz = clazzManager.get("com.rmbcorp.javawriter", "ClazzImpl2");
        setupClass();
    }

    private void setupClass() {
        clazz.setClassType(Clazz.ClassType.CLASS);
        clazz.addExtension(Object.class);
        clazz.setVisibility(Clazz.Visibility.PUBLIC);
        clazz.setFinal(true);
        clazz.addImplementations(Collections.<Class>singletonList(Clazz.class));
    }

    @Test
    public void classCannotHaveEmptyNameTest() {
        Clazz nameless = clazzManager.get("com.rmbcorp.javawriter", "");
        nameless.setClassType(Clazz.ClassType.CLASS);
        String output = clazzManager.writeOut(nameless);
        assertTrue("".equals(output));
        assertTrue(clazzManager.hasError(CANNOT_HAVE_EMPTY_CLASS_NAME));
    }

    @Test
    public void typeMustBeClassOrInterfaceTest() {
        clazz.setClassType(null);
        clazzManager.writeOut(clazz);
        assertTrue(clazzManager.hasError(MUST_BE_CLASS_OR_INTERFACE));
    }

    @Test
    public void outputContainsAllImportsTest() {
        clazz.addImports(Arrays.<Class>asList(INTEGER_CLASS, STRING_CLASS));
        String[] output = clazzManager.writeOut(clazz).split(";|\\s");
        assertTrue(StringUtil.containsAll(output, new String[]{ nameMatch(INTEGER_CLASS), nameMatch(STRING_CLASS) }));
    }

    private String nameMatch(Class<?> clazz) {
        return clazz.getCanonicalName();
    }

    @Test
    public void packageNameNotCurrentlyRequiredTest() {
        String arbitrary = "Arbitrary";
        Clazz packageless = clazzManager.get("", arbitrary);
        packageless.setClassType(Clazz.ClassType.CLASS);
        String output = clazzManager.writeOut(packageless);
        assertTrue(StringUtil.containsAll(output.split("\\s"), new String[]{ "class", arbitrary, "}" }));
    }

    @Test
    public void classCannotBeAbstractAndFinalTest() {
        clazz.setAbstract(true);
        clazz.setFinal(true);
        assertTrue("".equals(clazzManager.writeOut(clazz)));
        assertTrue(clazzManager.hasError(CANNOT_BE_ABSTRACT_AND_FINAL));
    }
}