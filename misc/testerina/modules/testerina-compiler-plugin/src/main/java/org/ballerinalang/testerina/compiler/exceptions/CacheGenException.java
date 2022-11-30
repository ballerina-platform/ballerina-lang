package org.ballerinalang.testerina.compiler.exceptions;
/**
 *  Exception class to throw exception when the test document - mock function map fails to generate.
 *
 * @since 2201.4.0
 */
public class CacheGenException extends RuntimeException {
    public CacheGenException(String msg) {
        super(msg);
    }
}
