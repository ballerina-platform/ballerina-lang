package io.ballerina.projects;

/**
 * A {@code RemotePackageRepositoryException} is thrown for any unrecoverable errors that
 * occurs when pulling dependencies from Ballerina Cetnral.
 *
 * @since 2.0.0
 */
public class RemotePackageRepositoryException extends RuntimeException {

    public RemotePackageRepositoryException(String message) {
        super(message);
    }

    public RemotePackageRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public  RemotePackageRepositoryException(Throwable cause) {
        super(cause);
    }

}
