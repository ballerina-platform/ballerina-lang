package org.ballerinalang.util.exceptions;

/**
 * Usage exception thrown with invalid args passed to function to execute.
 */
public class BLangUsageException extends RuntimeException {
    public BLangUsageException(String message) {
        super(message);
    }
}
