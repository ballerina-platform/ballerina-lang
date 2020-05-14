package org.ballerinalang.debugadapter.variable;

/**
 * Ballerina JVM variable types.
 */
public enum JVMValueType {

    LONG("java.lang.Long"),
    BOOLEAN("java.lang.Boolean"),
    DOUBLE("java.lang.Double"),
    STRING("java.lang.String"),
    OBJECT("java.lang.Object"),
    OBJECT_TYPE("org.ballerinalang.jvm.types.BObjectType"),
    OBJECT_VALUE("org.ballerinalang.jvm.values.ObjectValue"),
    ARRAY_VALUE("org.ballerinalang.jvm.values.ArrayValue"),
    MAP_VALUE("org.ballerinalang.jvm.values.MapValue"),
    ERROR_VALUE("org.ballerinalang.jvm.values.ErrorValue"),
    XML_ITEM("org.ballerinalang.jvm.values.XMLItem");

    private final String value;

    JVMValueType(String value) {
        this.value = value;
    }

    public String getString() {
        return this.value;
    }
}
