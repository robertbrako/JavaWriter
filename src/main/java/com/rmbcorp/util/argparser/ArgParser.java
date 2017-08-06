package com.rmbcorp.util.argparser;

import com.rmbcorp.util.StringUtil;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgParser {

    private Pattern p;

    public <T extends Enum<T>> Map<T, String> getArgs(String[] args, String delimiter, Class<T> type) {
        Map<T, String> map = new EnumMap<>(type);
        String delim = StringUtil.isEmpty(delimiter) ? "(^)" : "(" + delimiter + "*)";
        p = Pattern.compile(delim + "(.*)");
        T[] unmatchedEnums = type.getEnumConstants();
        T[] enumConstants = type.getEnumConstants();

        String trimmedArg;
        for (int i = 0; i < args.length; i++) {
            trimmedArg = trim(args[i]);
            for (int j = 0; j < enumConstants.length; j++) {
                T key = enumConstants[j];
                if (key.name().equalsIgnoreCase(trimmedArg)) {
                    map.put(key, args[i + 1]);//ignore if value was already set, for now
                    unmatchedEnums[j] = null;
                    i++;
                    break;
                }
            }
        }
        for (T t : unmatchedEnums) {
            if (t instanceof Supplier) {
                map.put(t, (String)((Supplier)t).get());
            }
        }
        return map;
    }

    private String trim(String arg) {
        Matcher m = p.matcher(arg);
        return m.find() ? m.group(2) : arg;
    }

}
