package io.ballerina.toml.validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ObjectSchema extends Schema {
    private String description;
    private boolean additionalProperties;
    private Map<String, Schema> properties;

    public ObjectSchema(TypeEnum type) {
        super(type);
        this.additionalProperties = true;
        this.properties = new HashMap<>();
    }

    public ObjectSchema(TypeEnum type, Map<String, Schema> properties) {
        super(type);
        this.additionalProperties = true;
        this.properties = properties;
    }

    public ObjectSchema(TypeEnum type, String description, boolean additionalProperties,
                        Map<String, Schema> properties) {
        super(type);
        this.description = description;
        this.additionalProperties = additionalProperties;
        this.properties = properties;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public boolean isAdditionalProperties() {
        return additionalProperties;
    }

    public Map<String, Schema> getProperties() {
        return properties;
    }
}
