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
package com.rmbcorp.javawriter.autojavac;

import java.util.ArrayList;
import java.util.List;

class CompileErrorBuilder {

    private List<CompileError> compileErrors = new ArrayList<>();
    private List<String> errorCache = new ArrayList<>(3);

    CompileErrorBuilder() { }

    void acceptError(String line, String relativePath) {
        if (normalize(line).startsWith(normalize(relativePath)) && errorCache.size() > 0) {
            compileErrors.add(new CompileError(errorCache));
            errorCache.clear();
        }
        errorCache.add(line);
    }

    private String normalize(String line) {
        return line.replaceAll("\\\\", "/");
    }

    List<CompileError> getCompileErrors() {
        return compileErrors;
    }
}
