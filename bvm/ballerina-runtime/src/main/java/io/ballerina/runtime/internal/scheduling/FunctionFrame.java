package io.ballerina.runtime.internal.scheduling;

/**
 * This interface represents the function frame which saves the existing
 * state when a function yields.
 *
 * @since 2201.2.0
 */
public interface FunctionFrame {

    String getYieldLocation();

    String getYieldStatus();

}
