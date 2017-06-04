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

import java.util.EnumMap;
import java.util.Map;

public enum ProcessorProvider {

    CLAZZIMPL;

    private static Map<ProcessorProvider, ClazzProcessor> processorMap = new EnumMap<>(ProcessorProvider.class);


    public static ClazzProcessor get(ProcessorProvider type) {
        if (processorMap.get(type) == null) {
            ProcUtil procUtil = new ProcUtil();
            switch (type) {
                case CLAZZIMPL:
                    ClazzValidator validator = new ClazzValidator();
                    processorMap.put(CLAZZIMPL,
                            new ClazzImplProcessor(validator, new ClassStarter(validator, procUtil), procUtil));
                    break;
                default:
                    break;
            }
        }
        return processorMap.get(type);
    }
}
