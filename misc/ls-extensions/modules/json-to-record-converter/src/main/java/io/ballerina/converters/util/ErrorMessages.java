package io.ballerina.converters.util;

/**
 * Container for error messages of the JSON converter exception.
 */
public class ErrorMessages {

    public static String parserException(String json) {
        return String.format("Couldn't read the JSON Schema from the given string: %s", json);
    }

    public static String unsupportedType() {
        return "Unsupported, Null or Missing type in Json";
    }

    public static String multipleTypes(String property) {
        return String.format("Properties must have a single non-null type. The property:%n'%s'%n" +
                        "has a type which is not one of: 'string','object','array','boolean' or numeric types",
                property);
    }

    public static String invalidReference(String property) {
        return String.format("Invalid reference value :'%s'%nBallerina only supports local reference values.",
                property);
    }
}
