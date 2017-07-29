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

import com.rmbcorp.javawriter.clazz.ClazzReadable;

public class ProcessorProvider {

    private ProcessorProvider() { }

    public static ClazzProcessor<ClazzReadable> getBeanProcessor() {
        ClazzValidator validator = new ClazzValidator();
        ProcUtil procUtil = new ProcUtil();
        ClassStarter classStarter = new ClassStarter(validator, procUtil);
        return new BeanProcessor(validator, classStarter, procUtil);
    }

    public static ClazzProcessor<ClazzReadable> getClazzProcessor() {
        ClazzValidator validator = new ClazzValidator();
        ProcUtil procUtil = new ProcUtil();
        ClassStarter classStarter = new ClassStarter(validator, procUtil);
        return new ClazzImplProcessor(validator, classStarter, procUtil);
    }
}
