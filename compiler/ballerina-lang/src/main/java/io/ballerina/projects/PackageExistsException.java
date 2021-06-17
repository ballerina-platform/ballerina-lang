package io.ballerina.projects;

/**
 *  A {@code ProjectException} is thrown when a package already exists in the home repo.
 */
public class PackageExistsException extends ProjectException {

    public PackageExistsException(String message) {
        super(message);
    }
}
