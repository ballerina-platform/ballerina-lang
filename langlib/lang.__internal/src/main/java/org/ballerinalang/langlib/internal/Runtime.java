package org.ballerinalang.langlib.internal;

/**
 * Native implementation of lang.internal:exit.
 *
 * @since 2201.4.0
 */
public class Runtime {
    public static void exit() {
        java.lang.Runtime.getRuntime().exit(0);
    }

    private Runtime() {
    }

}
