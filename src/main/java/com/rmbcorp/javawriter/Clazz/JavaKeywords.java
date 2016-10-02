package com.rmbcorp.javawriter.clazz;

/**JavaKeywords
 * Created by rmbdev on 10/1/2016.
 */
enum JavaKeywords {
    BOOL("boolean", "bool"), CHAR("char", "ch"), INT("int", "i"), LONG("long", "l"), FLOAT("float", "f"), DOUBLE("double", "d"),
    CLASS("Class", "clazz"), ENUM("enum", "en"), BOOLEAN("Boolean", "bool");

    private final String keyword;
    private final String replacement;

    JavaKeywords(String keyword, String replacement) {
        this.keyword = keyword;
        this.replacement = replacement;
    }

    static String replaceJavaKeyword(String input) {
        String result = input;
        for (JavaKeywords keyword : JavaKeywords.values()) {
            if (keyword.toString().equals(input)) {
                result = keyword.getReplacement();
            }
        }
        return result;
    }

    private String getReplacement() {
        return replacement;
    }

    @Override
    public String toString() {
        return keyword;
    }
}
