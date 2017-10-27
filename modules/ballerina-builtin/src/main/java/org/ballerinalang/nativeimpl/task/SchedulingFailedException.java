package org.ballerinalang.nativeimpl.task;

/**
 * Task scheduler exception
 */
public class SchedulingFailedException extends Exception {
    public SchedulingFailedException(String message) {
        super(message);
    }
}
