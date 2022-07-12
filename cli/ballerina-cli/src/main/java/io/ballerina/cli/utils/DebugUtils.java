package io.ballerina.cli.utils;

import java.io.PrintStream;
import java.util.Objects;

import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_DEBUG_SUSPEND_MODE;

/**
 * Utilities related to ballerina program debugging.
 *
 * @since 2.0.0
 */
public class DebugUtils {

    private static final String DEBUG_SUSPEND_ARGS_JAVA11 = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y";
    private static final String DEBUG_UNSUSPEND_ARGS_JAVA11 = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n";
    private static final String JAVA_VERSION_PROP = "java.version";
    private static final String COMPATIBLE_JRE_VERSION = "11";

    /**
     * Evaluates whether the ballerina program should be running on debug mode.
     *
     * @return true if user has provided ballerina debug arguments.
     */
    public static boolean isInDebugMode() {
        return System.getProperty(SYSTEM_PROP_BAL_DEBUG) != null &&
                !System.getProperty(SYSTEM_PROP_BAL_DEBUG).isEmpty();
    }

    // Todo - Evaluate adding support for later JRE versions.
    /**
     * Returns current runtime aware debug arguments for the ballerina program to be executed.
     *
     * @param errorStream error stream
     * @return debug arguments as a string
     */
    public static String getDebugArgs(PrintStream errorStream) {
        String javaVersion = System.getProperty(JAVA_VERSION_PROP);
        if (javaVersion != null && !javaVersion.startsWith(COMPATIBLE_JRE_VERSION)) {
            errorStream.printf("WARNING: Incompatible JRE version '%s' found. Ballerina program debugging supports " +
                    "on JRE version '%s'%n", javaVersion, COMPATIBLE_JRE_VERSION);
        }

        if (System.getProperty(SYSTEM_PROP_DEBUG_SUSPEND_MODE) != null &&
                Objects.equals(System.getProperty(SYSTEM_PROP_DEBUG_SUSPEND_MODE), "false")) {
            return String.format("%s,address=*:%s", DEBUG_UNSUSPEND_ARGS_JAVA11,
                    System.getProperty(SYSTEM_PROP_BAL_DEBUG));
        } else {
            return String.format("%s,address=*:%s", DEBUG_SUSPEND_ARGS_JAVA11,
                    System.getProperty(SYSTEM_PROP_BAL_DEBUG));
        }
    }
}
