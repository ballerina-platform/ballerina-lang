package io.ballerina.plugins.idea.preloading;

/**
 * Exception thrown when a failure is occurred due to any ballerina command execution failure.
 */
public class BallerinaCmdException extends Exception {
    public BallerinaCmdException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public BallerinaCmdException(String message) {
        super(message);
    }
}
