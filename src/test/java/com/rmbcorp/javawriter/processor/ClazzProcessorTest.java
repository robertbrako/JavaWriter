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

import com.rmbcorp.javawriter.clazz.Clazz;
import com.rmbcorp.javawriter.clazz.ClazzImplManager;
import com.rmbcorp.javawriter.clazz.ClazzReadable;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static com.rmbcorp.javawriter.clazz.ClazzImplManager.ClazzError.MUST_BE_CLASS_OR_INTERFACE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClazzProcessorTest {

    public static final String COM_RMBCORP_JAVAWRITER = "com.rmbcorp.javawriter";

    private ClazzProcessor clazzProcessor;
    private ClazzImplManager clazzManager;
    private Clazz clazz;

    @Before
    public void setUp() throws Exception {
        clazzManager = ClazzImplManager.getInstance();
        clazzProcessor = ProcessorProvider.get(ProcessorProvider.CLAZZIMPL);
        clazz = clazzManager.get(COM_RMBCORP_JAVAWRITER, "ClazzImpl2");
    }

    @Test
    public void removeResultTest() {
        ClazzValidator validator = new ClazzValidator();
        ClazzImplProcessor processor = new ClazzImplProcessor(validator);
        clazz.setClassType(null);
        processor.writeOut((ClazzReadable) clazz);

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
}
