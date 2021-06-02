package io.ballerina.converters.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Contains keywords and other constants.
 */
public class Constants {
    public static final String INTEGER = "integer";
    public static final String NUMBER = "number";
    public static final String STRING = "string";
    public static final String BOOLEAN = "boolean";
    public static final String DECIMAL = "decimal";
    public static final String ARRAY = "array";
    public static final String OBJECT = "object";
    public static final String FLOAT = "float";
    public static final String DOUBLE = "double";
    public static final String ESCAPE_PATTERN = "([\\[\\]\\\\?!<>@#&~`*-=^+();:\\_{}\\s|.$])";

    private static final String[] KEYWORDS = new String[]{"abort", "aborted", "abstract", "all", "annotation",
            "any", "anydata", "boolean", "break", "byte", "catch", "channel", "check", "checkpanic", "client",
            "committed", "const", "continue", "decimal", "else", "error", "external", "fail", "final", "finally",
            "float", "flush", "fork", "function", "future", "handle", "if", "import", "in", "int", "is", "join",
            "json", "listener", "lock", "match", "new", "object", "OBJECT_INIT", "onretry", "parameter", "panic",
            "private", "public", "record", "remote", "resource", "retries", "retry", "return", "returns", "service",
            "source", "start", "stream", "string", "table", "transaction", "try", "type", "typedesc", "typeof",
            "trap", "throw", "wait", "while", "with", "worker", "var", "version", "xml", "xmlns", "BOOLEAN_LITERAL",
            "NULL_LITERAL", "ascending", "descending", "foreach", "map", "group", "from", "default", "field",
            "limit", "as", "on", "isolated", "readonly", "distinct", "where", "select", "do", "transactional"
            , "commit", "enum", "base16", "base64", "rollback", "configurable",  "class", "module", "never",
            "outer", "order", "null", "key", "let", "by"};

    public static final List<String> BAL_KEYWORDS;
    static {
        BAL_KEYWORDS = Collections.unmodifiableList(Arrays.asList(KEYWORDS));
    }
}
