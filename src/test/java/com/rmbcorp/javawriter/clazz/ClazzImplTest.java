package com.rmbcorp.javawriter.clazz;

import com.rmbcorp.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.rmbcorp.javawriter.clazz.ClazzImplManager.ClazzError.*;
import static org.junit.Assert.assertFalse;
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
        clazz.addImplementations(Collections.<Class>singletonList(Clazz.class));
    }

    @Test
    public void cannotHaveEmptyNameTest() {
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
        List<Class> classList = Arrays.<Class>asList(INTEGER_CLASS, STRING_CLASS);
        clazz.addImports(classList);
        String[] output = clazzManager.writeOut(clazz).split(";|\\s");
        assertTrue(StringUtil.containsAll(output, nameMatch(classList)));
    }

    private String[] nameMatch(List<Class> classes) {
        String[] result = new String[classes.size()];
        for (int i = 0; i < classes.size(); i++) {
            result[i] = classes.get(i).getCanonicalName();
        }
        return result;
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

    @Test
    public void interfaceCannotBePrivateTest() {
        clazz.setClassType(Clazz.ClassType.INTERFACE);
        clazz.setVisibility(Clazz.Visibility.PRIVATE);
        clazzManager.writeOut(clazz);
        assertTrue(clazzManager.hasError(CANNOT_HAVE_PRIVATE_INTERFACE));
    }

    @Test
    public void removeResultTest() {
        ClazzValidator validator = new ClazzValidator();
        ClazzImplProcessor processor = new ClazzImplProcessor(validator);
        clazz.setClassType(null);
        processor.writeOut((ClazzImpl) clazz);

        assertTrue(validator.getErrorsAsCSV().contains(MUST_BE_CLASS_OR_INTERFACE.name()));
        validator.removeResult(MUST_BE_CLASS_OR_INTERFACE);
        assertFalse(validator.getErrorsAsCSV().contains(MUST_BE_CLASS_OR_INTERFACE.name()));
    }
}