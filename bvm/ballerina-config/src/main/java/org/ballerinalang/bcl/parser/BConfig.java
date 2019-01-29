package org.ballerinalang.bcl.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper for config entries map, so that existence of encrypted entries can also be tracked.
 *
 * @since 0.966.0
 */
public class BConfig {

    private Map<String, Object> configEntries = new HashMap<>();
    private boolean hasEncryptedValues;

    public void addConfiguration(String key, Object value) {
        configEntries.put(key, value);
    }

    public void addConfigurations(Map<String, Object> configs) {
        configEntries.putAll(configs);
    }

    public Map<String, Object> getConfigurations() {
        return configEntries;
    }

    public boolean hasEncryptedValues() {
        return hasEncryptedValues;
    }

    public void setHasEncryptedValues(boolean hasEncryptedValues) {
        this.hasEncryptedValues = hasEncryptedValues;
    }
}
