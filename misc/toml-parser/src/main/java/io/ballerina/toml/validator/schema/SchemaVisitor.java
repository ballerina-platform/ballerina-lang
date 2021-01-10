package io.ballerina.toml.validator.schema;

/**
 * Schema Visitor for Toml Schema.
 *
 * @since 2.0.0
 */
public abstract class SchemaVisitor {
    public void visit(Schema rootSchema) {
        throw new AssertionError();
    }

    public void visit(ArraySchema arraySchema) {
        throw new AssertionError();
    }

    public void visit(BooleanSchema booleanSchema) {
        throw new AssertionError();
    }

    public void visit(NumericSchema numericSchema) {
        throw new AssertionError();
    }

    public void visit(ObjectSchema objectSchema) {
        throw new AssertionError();
    }

    public void visit(StringSchema stringSchema) {
        throw new AssertionError();
    }
}
