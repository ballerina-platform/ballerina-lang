package org.ballerinalang.langserver.completions.toml;

/**
 * Represents a value type in toml.
 *
 * @since 2.0.0
 */
public enum ValueType {
    STRING("\"", "\""),
    NUMBER("", ""),
    ARRAY("[", "]");

    private String startingSeparator;
    private String endingSeparator;

    private ValueType(String startingSeparator, String endingSeparator) {
        this.startingSeparator = startingSeparator;
        this.endingSeparator = endingSeparator;
    }

    public String getStartingSeparator() {
        return startingSeparator;
    }

    public String getEndingSeparator() {
        return endingSeparator;
    }
}
