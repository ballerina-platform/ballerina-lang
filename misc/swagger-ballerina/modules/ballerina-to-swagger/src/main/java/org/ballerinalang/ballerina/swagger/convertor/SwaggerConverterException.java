package org.ballerinalang.ballerina.swagger.convertor;

/**
 * Exception definition for Ballerina to OpenApi converter errors.
 */
public class SwaggerConverterException extends Exception {
    public SwaggerConverterException(String message, Throwable cause) {
        super(message, cause);
    }

    public SwaggerConverterException(String message) {
        super(message);
    }

    public SwaggerConverterException(Throwable cause) {
        super(cause);
    }
}
