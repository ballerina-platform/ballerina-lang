package org.ballerinalang.debugadapter.variable;

/**
 * Ballerina JVM variable types.
 */
public enum JVMValueType {

    LONG("long"),
    BOOLEAN("boolean"),
    DOUBLE("double"),
    DECIMAL("org.ballerinalang.jvm.values.DecimalValue"), // todo - parent var name
    STRING("java.lang.String"),
    OBJECT("java.lang.Object"),
    OBJECT_TYPE("org.ballerinalang.jvm.types.BObjectType"),
    OBJECT_VALUE("org.ballerinalang.jvm.values.ObjectValue"),
    ARRAY_VALUE("org.ballerinalang.jvm.values.ArrayValue"),
    TUPLE_VALUE("org.ballerinalang.jvm.values.TupleValue"),
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
