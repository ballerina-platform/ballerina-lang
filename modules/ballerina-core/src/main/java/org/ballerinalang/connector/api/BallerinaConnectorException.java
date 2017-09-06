package org.ballerinalang.connector.api;

import org.ballerinalang.bre.Context;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Created by rajith on 9/5/17.
 */
public class BallerinaConnectorException extends BallerinaException {
    public BallerinaConnectorException() {
        super();
    }

    /**
     * Constructs a new {@link BallerinaException} with the specified detail message.
     *
     * @param message Error Message
     */
    public BallerinaConnectorException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link BallerinaException} with ballerina context.
     *
     * @param message Error message
     * @param context Ballerina context
     */
    public BallerinaConnectorException(String message, Context context) {
        super(message, context);
    }

    /**
     * Constructs a new {@link BallerinaException} with the specified detail message and cause.
     *
     * @param message Error message
     * @param cause   Cause
     */
    public BallerinaConnectorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link BallerinaException} with the specified detail message, cause and ballerina context.
     *
     * @param message Error message
     * @param cause   Cause
     * @param context Ballerina context
     */
    public BallerinaConnectorException(String message, Throwable cause, Context context) {
        super(message, cause, context);
    }

    /**
     * Constructs a new {@link BallerinaException} with the cause.
     *
     * @param cause Throwable to wrap by a ballerina exception
     */
    public BallerinaConnectorException(Throwable cause) {
        super(cause);
    }

    public BallerinaConnectorException(Context stack) {
        super(stack);
    }
}
