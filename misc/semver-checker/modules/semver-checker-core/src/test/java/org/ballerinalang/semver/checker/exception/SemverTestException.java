package org.ballerinalang.semver.checker.exception;

/**
 * Base exception class for all exceptions in the Ballerina semver checker test suite.
 *
 * @since 2.0.0
 */
public class SemverTestException extends Exception {

    public SemverTestException(String message) {
        super(message);
    }
}
