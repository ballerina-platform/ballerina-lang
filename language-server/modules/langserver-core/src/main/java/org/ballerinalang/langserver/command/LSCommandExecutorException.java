package org.ballerinalang.langserver.command;

/**
 * Exception for Language Server Command Execution.
 * 
 * @since 0.983.0
 */
public class LSCommandExecutorException extends Exception {
    public LSCommandExecutorException(String message) {
        super(message);
    }

    public LSCommandExecutorException(String message, Throwable cause) {
        super(message, cause);
    }

    public LSCommandExecutorException(Throwable cause) {
        super(cause);
    }

    public LSCommandExecutorException(String message, Throwable cause, boolean enableSuppression,
                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
