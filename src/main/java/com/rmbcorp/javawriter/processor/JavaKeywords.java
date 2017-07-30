package com.rmbcorp.javawriter.processor;

/**JavaKeywords
 * Created by rmbdev on 10/1/2016.
 */
enum JavaKeywords {
    BOOL("boolean", "bool"), CHAR("char", "ch"), INT("int", "i"), LONG("long", "l"), FLOAT("float", "f"),
    DOUBLE("double", "d"), CLASS("Class", "clazz"), CLASS2("class", "clazz"), ENUM("enum", "en"), BOOLEAN("Boolean", "bool"), SHORT("short", "srt"),
    FALSE("false", "fal"), TRUE("true", "tru"), NULL("null", "nul"), BYTE("byte", "bt"), VOID("void", "vd");

    private final String keyword;
    private final String replacement;

    JavaKeywords(String keyword, String replacement) {
        this.keyword = keyword;
        this.replacement = replacement;
    }

    /** @return original input String, or a javac-friendly replacement if input was a java keyword **/
    static String replaceJavaKeyword(String input) {
        String result = input;
        for (JavaKeywords keyword : JavaKeywords.values()) {
            if (keyword.toString().equals(input)) {
                result = keyword.getReplacement();
            }
        }
        return result;
    }

    static String replaceJavaKeywordSafe(String input) {
        for (JavaKeywordsExtended key : JavaKeywordsExtended.values()) {
            if (key.toString().equalsIgnoreCase(input)) {
                return key.getReplacement();
            }
        }
        return replaceJavaKeyword(input);
    }

    private String getReplacement() {
        return replacement;
    }

    @Override
    public String toString() {
        return keyword;
    }
}

enum JavaKeywordsExtended {
    ABSTRACT("abstr"), ASSERT("assrt"), BREAK("brk"),
    CASE("cas"), CATCH("cat"), CONST("constant"), CONTINUE("contin"),
    DEFAULT("def"), DO("d"), ELSE("els"), ENUM("enm"), EXTENDS("xtends"),
    FINAL("fin"), FINALLY("fnlly"), FOR("fr"), GOTO("gto"), IF("iF"),
    IMPLEMENTS("impls"), IMPORT("imprt"), INSTANCEOF("instof"), INTERFACE("iFace"),
    NATIVE("nat"), NEW("nw"), PACKAGE("pack"), PRIVATE("priv"), PROTECTED("prot"),
    PUBLIC("pub"), RETURN("ret"), STATIC("stat"), STRICTFP("sfp"), SUPER("sup"),
    SWITCH("swt"), SYNCHRONIZED("synced"), THIS("ths"), THROW("thw"), THROWS("tws"),
    TRANSIENT("trans"), TRY("tr"), VOLATILE("vol"), WHILE("whl");

    private final String keyword;
    private final String replacement;

    JavaKeywordsExtended(String replacement) {
        keyword = name().toLowerCase();
        this.replacement = replacement;
    }

    String getReplacement() {
        return replacement;
    }

    @Override
    public String toString() {
        return keyword;
    }
}