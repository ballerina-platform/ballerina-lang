package org.ballerinalang.datamapper.utils;

import io.ballerina.compiler.api.symbols.TypeDescKind;

import java.io.Serializable;

import static io.ballerina.compiler.api.symbols.TypeDescKind.*;

public class DefaultValueGenerator {
    public static Serializable generateDefaultValues(String type) {
        switch (type) {
            case "int":
            case "Signed8":
            case "Unsigned8":
            case "Signed16":
            case "Unsigned16":
            case "Signed32":
            case "Unsigned32":
            case "byte":
                return 0;
            case "float":
            case "decimal":
                return 0.0;
            case "string":
            case "Char":
                return "\"\"";
            case "BOOLEAN":
                return false;
            case "nil":
            case "any":
            case "union":
            case "json":
                return "()";
            case "array":
            case "tuple":
                return "[]";
            case "object":
                return "new T()";
            case "record":
            case "map":
                return "{}";
            case "xml":
            case "Element":
            case "ProcessingInstruction":
            case "Comment":
            case "Text":
                return "``";
            default:
                return "";
        }
    }
}