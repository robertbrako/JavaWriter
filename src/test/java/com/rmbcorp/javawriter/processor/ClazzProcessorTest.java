/*
* Copyright 2017 by Robert M. Brako,
* All rights reserved.
*
* Permission is not granted to any person or entity to obtain a copy of this software and associated files
* (the "Software"), to deal in the Software, which includes without limitation any rights to use, copy, modify, merge,
* publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
* furnished to do so.  Permission may only be obtained by a signed and dated license agreement between licensor Robert
* Brako and prospective licensee.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
* WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
* COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.rmbcorp.javawriter.processor;

import com.rmbcorp.javawriter.BuildJob;
import com.rmbcorp.javawriter.SampleInterface;
import com.rmbcorp.javawriter.autojavac.Compiler;
import com.rmbcorp.javawriter.clazz.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rmbcorp.javawriter.clazz.ClazzError.MUST_BE_CLASS_OR_INTERFACE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClazzProcessorTest {

    private static final String COM_RMBCORP_JAVAWRITER = "com.rmbcorp.javawriter";
    private static final String CLASS_NAME = "ClazzImpl2";

    private ClazzProcessor<ClazzReadable> clazzProcessor;
    private ClazzProcessor<ClazzReadable> beanProcessor;
    private ClazzImpl clazz;

    @Before
    public void setUp() throws Exception {
        clazzProcessor = ProcessorProvider.getClazzProcessor();
        beanProcessor = ProcessorProvider.getBeanProcessor();
        clazz = new ClazzImpl(COM_RMBCORP_JAVAWRITER, CLASS_NAME);
    }

    @Test
    public void mustBeClassOrInterfaceType() {
        clazz.setClassType(null);
        List<ClazzError> result = clazzProcessor.writeOut(clazz).getErrorCache();
        assertTrue(result.contains(MUST_BE_CLASS_OR_INTERFACE));
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
    public void makeBeanFromVariables() {
        clazz.addBeanVariable(new JVariable("username", String.class));
        String output = beanProcessor.writeOut(clazz).getContents();

        assertTrue(output.contains("public void setUsername(String username)"));
        assertTrue(output.contains("this.username = username"));
        assertTrue(output.contains("public String getUsername()"));
        assertTrue(output.contains("return username"));
    }

    @Test
    public void makeBeanFromMethods() {
        clazz.addMethod(new JMethod("setUserId", Void.class, Clazz.Visibility.PUBLIC, COM_RMBCORP_JAVAWRITER, Integer.class));
        clazz.addMethod(new JMethod("getUserId", Integer.class, Clazz.Visibility.PUBLIC, COM_RMBCORP_JAVAWRITER));
        String output = beanProcessor.writeOut(clazz).getContents();

        assertTrue(output.contains("public void setUserId(int userId)"));
        assertTrue(output.contains("this.userId = userId"));
        assertTrue(output.contains("public int getUserId()"));
        assertTrue(output.contains("return userId"));
    }

    @Test
    public void beanIsNotPublicByDefault() {
        String output = beanProcessor.writeOut(clazz).getContents();
        Pattern pattern = Pattern.compile("public.*class");
        Matcher matcher = pattern.matcher(output);
        assertFalse(matcher.find());
    }

    @Test
    public void beanProcessorDoesNotCurrentlyStubImplementations() {
        clazz.addImplementations(Collections.singletonList(ClazzReadable.class));
        String output = beanProcessor.writeOut(clazz).getContents();
        assertFalse(output.contains("getPackagePath"));
    }

    @Test
    public void paramTest() {
        clazz = new ClazzImpl(COM_RMBCORP_JAVAWRITER + ".processor", CLASS_NAME);
        clazz.addImplementations(Collections.singletonList(SampleInterface.class));
        String out = clazzProcessor.writeOut(clazz).getContents();
        assertTrue(out.contains("typedParam(List<Set> "));
    }

    @Test
    public void basicEnumTest() {
        ClazzProcessor<EnumReadable> clazzProcessor = ProcessorProvider.getEnumProcessor();
        EnumImpl enumImpl = new EnumImpl(COM_RMBCORP_JAVAWRITER, CLASS_NAME);
        enumImpl.setVisibility(Clazz.Visibility.PUBLIC);
        enumImpl.addEnumConstants(Arrays.asList("CLASS", "INTERFACE"));
        String out = clazzProcessor.writeOut(enumImpl).getContents();
        assertTrue(out.contains("package " + COM_RMBCORP_JAVAWRITER + ";"));
        assertTrue(out.contains(enumImpl.getVisibility() + " enum " + CLASS_NAME + " {"));
        assertTrue(out.contains("INTERFACE") && out.contains("CLASS"));
    }

    @Test
    public void commentTest() {
        clazz = new ClazzImpl(COM_RMBCORP_JAVAWRITER + ".processor", CLASS_NAME);
        JMethod jMethod = new JMethod("method", Void.class, Clazz.Visibility.PACKAGE, COM_RMBCORP_JAVAWRITER);
        String line1 = "This is a multiline comment";
        String line2 = "Hopefully it works";
        jMethod.setComment(String.format("%s\n%s", line1, line2));
        clazz.addMethod(jMethod);
        String out = clazzProcessor.writeOut(clazz).getContents();

        assertTrue(out.contains("/**"));
        assertTrue(out.indexOf("/**") < out.indexOf(line1));
        assertTrue(out.indexOf(line1) < out.indexOf(line2));
        assertTrue(out.indexOf(line2) < out.indexOf("**/"));
        assertTrue(out.contains("void method()"));
    }

    @Test
    public void wontExtendFinalClasses() {
        ClazzImpl clazz = new ClazzImpl("", CLASS_NAME);
        clazz.addExtension(ProcessorProvider.getBeanProcessor().getClass());
        String result = clazzProcessor.writeOut(clazz).getContents();
        assertFalse(result.contains("extends "));
    }

    @Test
    public void wontStubStaticInterfaceMethods() {
        ClazzImpl clazz = new ClazzImpl("", CLASS_NAME);
        clazz.addImplementations(Collections.singletonList(BuildJob.class));
        String contents = clazzProcessor.writeOut(clazz).getContents();
        assertFalse(contents.contains("BuildJob get"));
    }

    @Test
    public void returnNullInsteadOfNewClassDueToLackOfConstructor() {
        ClazzImpl clazz = new ClazzImpl("", CLASS_NAME);
        clazz.addImplementations(Collections.singletonList(ClazzReadable.class));
        String contents = clazzProcessor.writeOut(clazz).getContents();

        String returnLine = "";
        String[] split = contents.split("\n");
        for (int i = 0; i < split.length; i++) {
            if (split[i].endsWith("Class getExtension() {")) {
                returnLine = split[i+2];
            }
        }
        assertTrue(returnLine.matches("\\s*return null;"));
    }

    @Test
    public void importTypeParamAsWell() {
        ClazzImpl clazz = new ClazzImpl("", CLASS_NAME);
        clazz.addImplementations(Collections.singletonList(SampleInterface.class));
        String contents = clazzProcessor.writeOut(clazz).getContents();

        assertTrue(contents.contains("import java.util.List"));
        assertTrue(contents.contains("import java.util.Set"));
    }

    @Test
    public void finallySupportMultiTypedParams() {
        clazz = new ClazzImpl(COM_RMBCORP_JAVAWRITER + ".processor", CLASS_NAME);
        clazz.addImplementations(Collections.singletonList(SampleInterface.class));
        String out = clazzProcessor.writeOut(clazz).getContents();
        assertTrue(out.contains("multiTypedParam(Map<Integer, String>"));
    }

    @Test
    public void varArgsSupport() {
        ClazzImpl clazz = new ClazzImpl(COM_RMBCORP_JAVAWRITER, CLASS_NAME);
        clazz.addImplementations(Collections.singletonList(Compiler.class));
        String result = clazzProcessor.writeOut(clazz).getContents();
        assertTrue(result.contains("private JavacParams[] javacParams;"));
        assertTrue(result.contains("setParams(JavacParams... javacParams"));
    }

    @Test
    public void importSubclassesProperly() {
        ClazzImpl clazz = new ClazzImpl(COM_RMBCORP_JAVAWRITER, CLASS_NAME);
        clazz.addImplementations(Collections.singletonList(Clazz.class));
        String result = clazzProcessor.writeOut(clazz).getContents();
        assertTrue(result.contains("private Clazz.Visibility visibility;"));
    }
}
