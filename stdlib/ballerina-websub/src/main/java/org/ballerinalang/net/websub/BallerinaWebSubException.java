package org.ballerinalang.net.websub;

import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Represents a Runtime Exception that could be thrown when performing WebSub actions.
 */
public class BallerinaWebSubException extends BallerinaException {

    public BallerinaWebSubException(String message) {
        super(message);
    }

    public BallerinaWebSubException(String message, Throwable cause) {
        super(message, cause);
    }

}
