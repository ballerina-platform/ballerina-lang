package org.ballerinalang.datamapper.utils;
import java.io.Serializable;

public class DefaultValueGenerator {
    public static Serializable generateDefaultValues(String type){
        switch (type) {
            case "INT":
                return 0;
            case "FLOAT":
                return 0.0;
            case "STRING":
                return "";
            case "BOOLEAN":
                return false;
            case "ANYDATA":
                return "()";
            case "ARRAY":
                return "[]";
            case "RECORD":
                return "{}";
            default:
                return "";
        }
    }
}
