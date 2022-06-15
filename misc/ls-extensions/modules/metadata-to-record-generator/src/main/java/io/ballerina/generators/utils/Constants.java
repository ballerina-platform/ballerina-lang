package io.ballerina.generators.utils;

import java.util.List;

/**
 * This class represents Custom RECORD generator related constants.
 */
public class Constants {
    public static final String SF_BOOLEAN_TYPE = "boolean";
    public static final String SF_INTEGER_TYPE = "int";
    public static final String SF_DOUBLE_TYPE = "double";
    public static final String SF_CURRENCY_TYPE = "currency";
    public static final String SF_PERCENT_TYPE = "percent";
    public static final String SF_STRING_TYPE = "string";
    public static final String SF_DATE_TYPE = "date";
    public static final String SF_DATETIME_TYPE = "datetime";
    public static final String SF_BASE64_TYPE = "base64";
    public static final String SF_ID_TYPE = "id";
    public static final String SF_REFERENCE_TYPE = "reference";
    public static final String SF_TEXTAREA_TYPE = "textarea";
    public static final String SF_PHONE_TYPE = "phone";
    public static final String SF_URL_TYPE = "url";
    public static final String SF_EMAIL_TYPE = "email";
    public static final String SF_PICKLIST_TYPE = "picklist";
    public static final String SF_LOCATION_TYPE = "location";
    public static final String SF_WHITESPACE_TYPE = " ";
    public static final String SF_HASH_TOKEN_TYPE = "# ";

    public static final String ESCAPE_PATTERN = "([\\[\\]\\\\?!<>@#&~`*-=^+();:\\_{}\\s|.$])";

    public static final List<String> BAL_KEYWORDS = List.of("abort", "aborted", "abstract", "all", "annotation",
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
            "outer", "order", "null", "key", "let", "by");
}
