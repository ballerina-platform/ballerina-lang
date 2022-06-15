package io.ballerina.generators.exceptions;

/**
 *
 */
public class MetadataToRecordGeneratorException extends Exception {
    public MetadataToRecordGeneratorException(String message, Throwable e) {
        super(message, e);
    }

    public MetadataToRecordGeneratorException(String message) {
        super(message);
    }
}
