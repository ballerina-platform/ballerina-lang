package io.ballerina.toml.validator;

public class ArraySchema extends Schema {
    private Schema items;

    public ArraySchema(TypeEnum type) {
        super(type);
    }

    public ArraySchema(TypeEnum type, Schema items) {
        super(type);
        this.items = items;
    }

    public Schema getItems() {
        return items;
    }
}
