package org.ballerinalang.stdlib.crypto.util;

import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * This class will be used to throw exceptions related to hashing operations.
 */
public class BallerinaHashException extends BallerinaException {

    public BallerinaHashException(String message, Throwable cause) {
        super(message, cause);
    }

    public BallerinaHashException(String message) {
        super(message);
    }
}
