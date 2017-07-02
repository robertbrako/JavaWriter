package com.rmbcorp.javawriter.clazz;

import com.rmbcorp.javawriter.SampleInterface;
import com.rmbcorp.javawriter.processor.*;
import com.rmbcorp.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.DataInput;
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
    public static final String COM_RMBCORP_JAVAWRITER = "com.rmbcorp.javawriter";

    private ClazzProcessor clazzProcessor;
    private ClazzImplManager clazzManager;
    private Clazz clazz;

    @Before
    public void setUp() throws Exception {
        clazzManager = ClazzImplManager.getInstance();
        clazzProcessor = ProcessorProvider.getClazzProcessor();
        clazz = clazzManager.get(COM_RMBCORP_JAVAWRITER, "ClazzImpl2");
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
        Clazz nameless = clazzManager.get(COM_RMBCORP_JAVAWRITER, "");
        String output = clazzProcessor.writeOut(nameless);
        assertTrue("".equals(output));
        assertTrue(clazzProcessor.hasError(CANNOT_HAVE_EMPTY_CLASS_NAME));
    }

    @Test
    public void typeMustBeClassOrInterfaceTest() {
        clazz.setClassType(null);
        clazzProcessor.writeOut(clazz);
        assertTrue(clazzProcessor.hasError(MUST_BE_CLASS_OR_INTERFACE));
    }

    @Test
    public void outputContainsAllImportsTest() {
        List<Class> classList = Arrays.<Class>asList(INTEGER_CLASS, STRING_CLASS);
        clazz.addImports(classList);
        String[] output = clazzProcessor.writeOut(clazz).split(";|\\s");
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
        String output = clazzProcessor.writeOut(packageless);
        assertTrue(StringUtil.containsAll(output.split("\\s"), new String[]{ "class", CLASS_NAME, "}" }));
    }

    @Test
    public void classCannotBeAbstractAndFinalTest() {
        clazz.setAbstract(true);
        clazz.setFinal(true);
        assertTrue("".equals(clazzProcessor.writeOut(clazz)));
        assertTrue(clazzProcessor.hasError(CANNOT_BE_ABSTRACT_AND_FINAL));
    }

    @Test
    public void interfaceCannotBePrivateTest() {
        clazz.setClassType(Clazz.ClassType.INTERFACE);
        clazz.setVisibility(Clazz.Visibility.PRIVATE);
        clazzProcessor.writeOut(clazz);
        assertTrue(clazzProcessor.hasError(CANNOT_HAVE_PRIVATE_INTERFACE));
    }

    @Test
    public void fieldNameValidationTest() {
        setupClass(Collections.<Class>singletonList(SampleInterface.class));
        String output = clazzProcessor.writeOut(clazz);
        assertFalse(output.contains("private Class class"));
        assertTrue(output.contains("private Class clazz"));
    }

    @Test
    public void forLoopGenerationTest() {
        setupClass(Collections.<Class>singletonList(Clazz.class));
        String output = clazzProcessor.writeOut(clazz);
        assertTrue(output.contains("for (Class clazz : list)"));
    }

    @Test
    public void addCustomMethodTest() throws NoSuchMethodException {
        Clazz withMethod = clazzManager.get(COM_RMBCORP_JAVAWRITER, CLASS_NAME);
        withMethod.addMethod(new JMethod("setDestroyWorlds", Boolean.class, Clazz.Visibility.PUBLIC,
                COM_RMBCORP_JAVAWRITER, String.class, Integer.class));
        withMethod.addMethod(new JMethod("isDestroyWorlds", Void.class, Clazz.Visibility.PUBLIC,
                COM_RMBCORP_JAVAWRITER, String.class, Integer.class));
        String output = clazzProcessor.writeOut(withMethod);
        Pattern pattern = Pattern.compile("public [Bb]oolean setDestroyWorlds\\(String.*Integer.*\\)");
        Matcher matcher = pattern.matcher(output);
        assertTrue(matcher.find());
        pattern = Pattern.compile("public [Vv]oid isDestroyWorlds\\(String.*Integer.*\\)");
        matcher = pattern.matcher(output);
        assertTrue(matcher.find());
    }

    @Test public void testParamSameTypeTest() {
        setupClass(Collections.<Class>singletonList(SampleInterface.class));
        Pattern pattern = Pattern.compile("String setFoo\\((.*)\\)");
        Matcher matcher = pattern.matcher(clazzProcessor.writeOut(clazz));
        if (matcher.find()) {
            String[] group = matcher.group(1).split(",");
            assertTrue(group.length == 3);
            assertTrue(!group[0].equalsIgnoreCase(group[1]) && !group[1].equalsIgnoreCase(group[2]) && !group[2].equalsIgnoreCase(group[0]));
        } else {
            fail();
        }
    }

    @Test public void returnStringTest() {
        setupClass(new ArrayList<>());
        String expectedString = "return new String";
        Pattern pattern = Pattern.compile(expectedString);
        Matcher matcher = pattern.matcher(clazzProcessor.writeOut(clazz));
        assertTrue(matcher.find() && matcher.group().contains(expectedString));
    }

    @Test public void insertIfIfBooleanParamTest() {
        Clazz clazz = setupClass(Collections.<Class>singletonList(SampleInterface.class));
        Pattern pattern = Pattern.compile("if\\s*\\(.*\\)");
        Matcher matcher = pattern.matcher(clazzProcessor.writeOut(clazz));
        assertTrue(matcher.find());
    }

    @Test public void bytePrimitiveIsNotImportedTest() {
        Clazz clazz = setupClass(Collections.<Class>singletonList(DataInput.class));
        String output = clazzProcessor.writeOut(clazz);
        assertFalse(output.contains("import byte"));
        assertFalse(output.contains("byte[] byte"));
    }

}