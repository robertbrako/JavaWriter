package com.rmbcorp.javawriter.logman;

/**Temp class until migrating to logging facility
 * Created by rmbdev on 10/29/2016.
 */
public class LoggerManager implements TempLogger {

    private boolean useSout;

    public LoggerManager(boolean useSout) {
        this.useSout = useSout;
    }

    @Override
    public void logPlain(String arg) {
        if (useSout) {
            System.out.println(arg);
        }
    }

    @Override
    public void logError(StackTraceElement[] args) {
        if (useSout) {
            for (StackTraceElement element : args) {
                System.err.println(element.toString());
            }
        }
    }

}
