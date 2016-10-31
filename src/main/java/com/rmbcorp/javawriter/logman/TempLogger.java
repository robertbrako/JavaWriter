package com.rmbcorp.javawriter.logman;

/**Temp interface until migrating to logging facility
 * Created by rmbdev on 10/29/2016.
 */
public interface TempLogger {

    void logPlain(String arg);
    void logError(StackTraceElement[] args);
}
