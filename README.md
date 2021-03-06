# com.rmbcorp.javawriter.JavaWriter

[![Build Status](https://travis-ci.org/robertbrako/JavaWriter.svg?branch=master)](https://travis-ci.org/robertbrako/JavaWriter)
<br>
Utility to dynamically write and compile java code.
![Sample Server](./sampleServer.png)
Goals for 0.2-SNAPSHOT:
  1. Validation backed by Unit Tests
  2. Auto-creation of fields
  3. Separating class bean from class processing
  4. 100% method coverage for tests

Goals for 0.3:
  1. Try implementing automated javac for JavaWriter output
  2. Accept command-line parameters.  Minimum scope:
     1. Class name
     2. Class type
     3. Visibility
     4. .java source destination directory
     5. .class javac destination directory
  3. Ability to create if statements

Goals for 0.4:
  1. Improve API; easy package name setting and option to compile only
  2. Support adding custom methods
  3. Support bean creation: getters and setters

Goals for 0.5
  1. Refinements needed for robust class building, to include:
     1. enum creation
     2. inner/anonymous class creation
     3. pre-method and inline comments
  2. Save compilation errors to CSV file
  3. Handle parametrized types

Goals for 0.6 and above might not be disclosed.

EOF