package io.ballerina.converters.exception;

/**
 * JSON converter exception.
 */
public class ConverterException extends Exception {
    public ConverterException(String message, Throwable e) {
        super(message, e);
    }

    public ConverterException(String message) {
        super(message);
    }
}
