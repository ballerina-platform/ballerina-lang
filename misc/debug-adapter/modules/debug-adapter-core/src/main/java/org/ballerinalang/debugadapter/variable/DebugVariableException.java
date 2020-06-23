package org.ballerinalang.debugadapter.variable;

/**
 * Error type definition for debugger variable implementation related exceptions.
 *
 * @since 2.0.0
 */
public class DebugVariableException extends Exception {

    public DebugVariableException(String message) {
        this(message, null);
    }

    public DebugVariableException(String message, Throwable cause) {
        super(message, cause);
    }
}


