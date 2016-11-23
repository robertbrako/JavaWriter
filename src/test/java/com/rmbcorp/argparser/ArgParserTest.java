/*
* Copyright 2016 by Robert M. Brako,
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
package com.rmbcorp.argparser;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ArgParserTest {

    public static final String DEFAULT_VALUE = "defaultValue";

    enum Vals { CLASSPATH, D }
    enum OtherVals implements ArgParser.HasDefault {
        KEY(DEFAULT_VALUE), PARAM2("");

        private final String defaultVal;

        OtherVals(String defaultValue) {
            this.defaultVal = defaultValue;
        }

        @Override
        public String getDefault() {
            return defaultVal;
        }
    }

    private ArgParser parser;

    @Before public void setup() {
        parser = new ArgParserImpl();

    }

    @Test public void nullArgsGivesNPETest() {
        Map<Vals, String> argMap = null;
        try {
            argMap = parser.getArgs(null, Vals.class);
        } catch (NullPointerException ignored) {
        }
        assertNull(argMap);
    }

    @Test public void emptyArgsGivesEmptyMapTest() {
        String[] args = {};
        Map<Vals, String> argMap = parser.getArgs(args, Vals.class);
        assertNotNull(argMap);
        assertTrue(argMap.size() == 0);
    }

    @Test public void keyWithoutDashPrefixIsOkTest() {
        String value = "C:\\Users\\";
        String[] args = {Vals.CLASSPATH.name(), value};
        Map<Vals, String> argMap = parser.getArgs(args, Vals.class);
        assertTrue(value.equals(argMap.get(Vals.CLASSPATH)));
    }

    @Test public void implementHasDefaultAllowsDefaultValuesTest() {
        String customValue = "customValue";
        String[] args = { OtherVals.PARAM2.name() , customValue};
        Map<OtherVals, String> argMap = parser.getArgs(args, OtherVals.class);
        assertTrue(customValue.equals(argMap.get(OtherVals.PARAM2)));
        assertTrue(DEFAULT_VALUE.equals(argMap.get(OtherVals.KEY)));
    }
}

