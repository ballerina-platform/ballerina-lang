package org.ballerinalang.debugadapter.variable;

/**
 * Ballerina variable types(Language Specification v2020R1).
 */
public enum BVariableType {

    // basic, simple types.
    NIL("()"),
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
    MAP("tuple"),
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
    JSON("json");

    private final String value;

    BVariableType(String value) {
        this.value = value;
    }

    String getValue() {
        return this.value;
    }
}