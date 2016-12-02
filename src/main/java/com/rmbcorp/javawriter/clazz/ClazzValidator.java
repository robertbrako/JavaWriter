package com.rmbcorp.javawriter.clazz;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.rmbcorp.javawriter.clazz.ClazzImplManager.ClazzError;

/**ClazzValidator
 * Created by rmbdev on 10/1/2016.
 */
public class ClazzValidator implements com.rmbcorp.util.ValidationManager<ClazzError> {

    Set<ClazzError> errorTypes = new HashSet<>();

    @Override
    public void addResult(ClazzError errorType) {
        errorTypes.add(errorType);
    }

    @Override
    public boolean hasErrors() {
        return errorTypes.size() != 0;
    }

    @Override
    public boolean removeResult(ClazzError errorType) { //plan to add more custom logic
        boolean removed = false;
        Iterator<ClazzError> it = errorTypes.iterator();
        ClazzError found;
        while (it.hasNext()) {
            found = it.next();
            if (found.equals(errorType)) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }

    @Override
    public void removeAllResults() {
        errorTypes.clear();
    }

    boolean containsError(ClazzError error) {
        return errorTypes.contains(error);
    }

    String getErrorsAsCSV() {
        StringBuilder builder = new StringBuilder(errorTypes.size() * 32);
        for (ClazzError errorType : errorTypes) {
            builder.append(errorType.toString()).append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
}
