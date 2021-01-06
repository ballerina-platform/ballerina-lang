package org.ballerinalang.langserver.completions.toml;

/**
 * Represents a key value pair in snippet builder.
 *
 * @since 2.0.0
 */
public class KeyValuePair implements TomlNode {

    private String key;
    private String defaultValue;
    private int id;
    private ValueType type;

    public KeyValuePair(String key, String defaultValue, int id, ValueType type) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.id = id;
        this.type = type;
    }

    public KeyValuePair(String key, ValueType type) {
        this.key = key;
        this.type = type;
        this.id = 1;
        this.defaultValue = "";
    }

    @Override
    public String prettyPrint() {
        return key + "=" + type.getStartingSeparator() + "${" + id + ":" + defaultValue + "}" +
                type.getEndingSeparator() + "";
    }
}
