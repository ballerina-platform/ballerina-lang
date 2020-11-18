package io.ballerina.toml.validator;

public abstract class Schema {
    private TypeEnum type;

    public Schema(TypeEnum type) {
        this.type = type;
    }

    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }
}
