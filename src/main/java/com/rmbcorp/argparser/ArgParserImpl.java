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

import com.rmbcorp.util.StringUtil;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ArgParserImpl implements ArgParser {

    private Pattern p;
    private boolean ignoreCaseForKeys;
    private boolean useKeyEquals;

    @Override
    public <T extends Enum> Map<T, String> getArgs(String[] args, Class<T> type) {
        return getArgs(args, "", type);
    }

    @Override
    public <T extends Enum> Map<T, String> getArgs(String[] args, String delim, Class<T> type) {
        Map<T, String> map = new EnumMap<>(type);
        delim = StringUtil.isEmpty(delim) ? "(^)" : "(" + delim + "*)";
        p = Pattern.compile(delim + "(.*)");
        ignoreCaseForKeys = true;
        useKeyEquals = false;
        T[] unmatchedEnums = type.getEnumConstants();

        String trimmedArg;
        for (int i = 0; i < args.length; i++) {
            trimmedArg = trim(args[i]);
            T[] enumConstants = type.getEnumConstants();
            for (int j = 0; j < enumConstants.length; j++) {
                T key = enumConstants[j];
                if (customEquals(key, trimmedArg)) {
                    map.put(key, args[i + 1]);//ignore if value was already set, for now
                    unmatchedEnums[j] = null;
                    i++;
                    break;
                }
            }
        }
        for (T t : unmatchedEnums) {
            if (t instanceof ArgParserImpl.HasDefault) {
                map.put(t, ((ArgParserImpl.HasDefault)t).getDefault());
            }
        }
        return map;
    }

    private String trim(String arg) {
        Matcher m = p.matcher(arg);
        return m.find() ? m.group(2) : arg;
    }

    private boolean customEquals(Enum key, String trimmed) {
        if (useKeyEquals) return key.equals(trimmed);
        return ignoreCaseForKeys ? key.name().equalsIgnoreCase(trimmed) : key.name().equals(trimmed);
    }

}
