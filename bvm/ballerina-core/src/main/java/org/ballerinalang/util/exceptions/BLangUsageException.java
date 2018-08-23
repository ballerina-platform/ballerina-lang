package org.ballerinalang.util.exceptions;

/**
 * Usage exception thrown with invalid args passed to the function to execute.
 *
 * @since 0.982.0
 */
public class BLangUsageException extends RuntimeException {
    public BLangUsageException(String message) {
        super(message);
    }
}
