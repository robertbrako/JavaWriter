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
import com.rmbcorp.util.StringUtil;

import java.util.Set;

public class ClassStarter {

    private final ClazzValidator validator;
    private final ProcUtil procUtil;

    public ClassStarter(ClazzValidator validator, ProcUtil procUtil) {
        this.validator = validator;
        this.procUtil = procUtil;
    }

    void buildClassOrInterface(StringBuilder builder, Clazz.ClassType classType, ClazzReadable clazz) {
        String className = clazz.getClassName();
        if (StringUtil.isEmpty(className)) {
            validator.addResult(ClazzImplManager.ClazzError.CANNOT_HAVE_EMPTY_CLASS_NAME);
        }
        if (!className.equals(JavaKeywords.replaceJavaKeywordSafe(className))) {
            validator.addResult(ClazzImplManager.ClazzError.INVALID_CLASS_NAME);
        }
        if (Clazz.ClassType.INTERFACE.equals(classType)) {
            buildInterface(builder, clazz.getVisibility(), className);
        } else if (Clazz.ClassType.CLASS.equals(classType)) {
            buildClass(builder, className, clazz.getVisibility(), clazz.isFinal(), clazz.isAbstract(), clazz.getExtension());
        } else {
            validator.addResult(ClazzImplManager.ClazzError.MUST_BE_CLASS_OR_INTERFACE);
        }
    }

    private void buildClass(StringBuilder builder, String className, Clazz.Visibility visibility, boolean isFinal, boolean isAbstract, Class extension) {
        if (!(isFinal && isAbstract)) {
            builder.append(visibility.toString()).append(' ');
            builder.append(isFinal ? "final " : isAbstract ? "abstract " : "");
            builder.append("class ").append(className);
            if (extension != null) {
                builder.append(" extends ");
                builder.append(procUtil.getClassSimpleName(extension.toString()));
            }
        } else {
            validator.addResult(ClazzImplManager.ClazzError.CANNOT_BE_ABSTRACT_AND_FINAL);
        }
    }

    private void buildInterface(StringBuilder builder, Clazz.Visibility visibility, String className) {
        if (Clazz.Visibility.PUBLIC.equals(visibility)) {
            builder.append(visibility.toString()).append(' ');
        } else if (Clazz.Visibility.PRIVATE.equals(visibility)) {
            validator.addResult(ClazzImplManager.ClazzError.CANNOT_HAVE_PRIVATE_INTERFACE);
        }
        builder.append("interface ").append(className);
    }

    void buildImplementations(StringBuilder builder, Clazz.ClassType classType, Set<Class> implementations) {
        if (!implementations.isEmpty()) {
            builder.append(Clazz.ClassType.CLASS.equals(classType) ? " implements " : " extends ");
            for (Class object : implementations) {
                builder.append(procUtil.dollarToDot(procUtil.getClassSimpleName(object.toString())));
                builder.append(", ");
            }
            trimComma(builder);
        }
    }

    private void trimComma(StringBuilder result) {
        int length = result.lastIndexOf(", ");
        if (length != -1) {
            result.replace(length, length + 1, "");
        }
    }
}
