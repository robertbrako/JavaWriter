package com.rmbcorp.javawriter.clazz;

import com.rmbcorp.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rmbcorp.javawriter.clazz.ClazzImplManager.ClazzError.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**ClazzImplTest
 * Created by rmbdev on 10/1/2016.
 */
public class ClazzImplTest {

    public static final Class<Integer> INTEGER_CLASS = Integer.class;
    public static final Class<String> STRING_CLASS = String.class;
    public static final String CLASS_NAME = "ArbitraryName";
    ClazzImplManager clazzManager;
    Clazz clazz;

    @Before
    public void setUp() throws Exception {
        clazzManager = ClazzImplManager.getInstance();
        clazz = clazzManager.get("com.rmbcorp.javawriter", "ClazzImpl2");
        setupClass(Collections.<Class>singletonList(Clazz.class));
    }

    private Clazz setupClass(List<Class> impls) {
        clazz.setClassType(Clazz.ClassType.CLASS);
        clazz.addExtension(Object.class);
        clazz.addImplementations(impls);
        return clazz;
    }

    @Test
    public void cannotHaveEmptyNameTest() {
        Clazz nameless = clazzManager.get("com.rmbcorp.javawriter", "");
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
        Clazz packageless = clazzManager.get("", CLASS_NAME);
        String output = clazzManager.writeOut(packageless);
        assertTrue(StringUtil.containsAll(output.split("\\s"), new String[]{ "class", CLASS_NAME, "}" }));
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

        assertTrue(validator.containsError(MUST_BE_CLASS_OR_INTERFACE));
        validator.removeResult(MUST_BE_CLASS_OR_INTERFACE);
        assertFalse(validator.containsError(MUST_BE_CLASS_OR_INTERFACE));
    }

    @Test
    public void extendedKeywordReplacementTest() {
        String testString = "Goto";
        String result = JavaKeywords.replaceJavaKeyword(testString);
        assertTrue(testString.equalsIgnoreCase(result));

        result = JavaKeywords.replaceJavaKeywordSafe(testString);
        assertFalse(testString.equalsIgnoreCase(result));
    }

    @Test
    public void fieldNameValidationTest() {
        setupClass(Collections.<Class>singletonList(Clazz.class));
        String output = clazzManager.writeOut(clazz);
        assertFalse(output.contains("private Class class"));
        assertTrue(output.contains("private Class clazz"));
    }

    @Test
    public void forLoopGenerationTest() {
        setupClass(Collections.<Class>singletonList(Clazz.class));
        String output = clazzManager.writeOut(clazz);
        assertTrue(output.contains("for (Class clazz : list)"));
    }

    @Test public void testParamSameTypeTest() {
        setupClass(Collections.<Class>singletonList(ParamTest.class));
        Pattern pattern = Pattern.compile("String setFoo\\((.*)\\)");
        Matcher matcher = pattern.matcher(clazzManager.writeOut(clazz));
        if (matcher.find()) {
            String[] group = matcher.group(1).split(",");
            assertTrue(group.length == 3);
            assertTrue(!group[0].equalsIgnoreCase(group[1]) && !group[1].equalsIgnoreCase(group[2]) && !group[2].equalsIgnoreCase(group[0]));
        } else {
            fail();
        }
    }

    @Test public void returnStringTest() {
        setupClass(new ArrayList<Class>());
        String expectedString = "return new String";
        Pattern pattern = Pattern.compile(expectedString);
        Matcher matcher = pattern.matcher(clazzManager.writeOut(clazz));
        assertTrue(matcher.find() && matcher.group().contains(expectedString));
    }

    @Test public void insertIfIfBooleanParamTest() {
        Clazz clazz = setupClass(Collections.<Class>singletonList(ParamTest.class));
        Pattern pattern = Pattern.compile("if\\s*\\(.*\\)");
        Matcher matcher = pattern.matcher(clazzManager.writeOut(clazz));
        assertTrue(matcher.find());
    }

    interface ParamTest {
        @SuppressWarnings("unused")
        String setFoo(String bar, String notbar, String reallyNotBar);
        @SuppressWarnings("unused")
        void doBar(boolean visible, boolean troubler);
    }
}