package org.ballerinalang.semver.checker.exceptions;

/**
 * Base class for all exceptions in the Ballerina server checker tool.
 * All the exceptions that are thrown in this tool should be inherited from this base type and, all the other
 * exceptions will be unhandled exceptions.
 *
 * @since 2201.2.0
 */
public class BallerinaSemverToolException extends Exception {

    public BallerinaSemverToolException(String message) {
        super(message);
    }

    public BallerinaSemverToolException(String message, Throwable cause) {
        super(message, cause);
    }

    public BallerinaSemverToolException(Throwable e) {
        super(e);
    }
}
