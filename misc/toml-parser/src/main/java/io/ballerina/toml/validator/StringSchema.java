package io.ballerina.toml.validator;

import java.util.Optional;

public class StringSchema extends Schema {
    private String pattern;

    public StringSchema(TypeEnum type) {
        super(type);
    }

    public StringSchema(TypeEnum type, String pattern) {
        super(type);
        this.pattern = pattern;
    }

    public Optional<String> getPattern() {
        return Optional.ofNullable(pattern);
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
