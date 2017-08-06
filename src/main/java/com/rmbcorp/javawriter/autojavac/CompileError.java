package com.rmbcorp.javawriter.autojavac;

import java.util.List;

public class CompileError {

    private String className;
    private int lineNo;
    private String reason;
    private String lineOfCode;

    CompileError(List<String> errorCache) {
        String[] parts = errorCache.get(0).split(":");
        className = parts[0];
        lineNo = Integer.parseInt(parts[1]);
        reason = parts[3].replaceFirst("\\s+", "");
        lineOfCode = errorCache.get(1);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLineOfCode() {
        return lineOfCode;
    }

    public void setLineOfCode(String lineOfCode) {
        this.lineOfCode = lineOfCode;
    }
}
