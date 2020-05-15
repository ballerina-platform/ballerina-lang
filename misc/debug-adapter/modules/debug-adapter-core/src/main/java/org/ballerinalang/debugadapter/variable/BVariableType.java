package org.ballerinalang.debugadapter.variable;

/**
 * Ballerina variable types(Language Specification v2020R1).
 */
public enum BVariableType {

    // basic, simple types.
    NIL("nil"),
    BOOLEAN("boolean"),
    INT("int"),
    FLOAT("float"),
    DECIMAL("decimal"),
    // basic, sequence types.
    STRING("string"),
    XML("xml"),
    // basic, structured
    ARRAY("array"),
    TUPLE("tuple"),
    MAP("map"),
    RECORD("record"),
    TABLE("table"),
    ERROR("error"),
    // basic, behavioral types.
    FUNCTION("function"),
    FUTURE("future"),
    OBJECT("object"),
    SERVICE("service"),
    TYPE_DESC("typedesc"),
    HANDLE("handle"),
    STREAM("stream"),
    // other types.
    SINGLETON("singleton"),
    UNION("union"),
    OPTIONAL("optional"),
    ANY("any"),
    ANYDATA("anydata"),
    NEVER("never"),
    BYTE("byte"),
    JSON("json"),

    // Note: This is not a valid ballerina variable type. This is only used for labeling jvm variables, which don't
    // map to any of the above ballerina standard variable types.
    UNKNOWN("unknown");

    private final String value;

    BVariableType(String value) {
        this.value = value;
    }

    public String getString() {
        return this.value;
    }
}