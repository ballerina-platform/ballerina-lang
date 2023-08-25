package io.ballerina.cli.utils;

import java.io.PrintStream;

import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SYSTEM_PROP_PROFILE_DEBUG;

/**
 * Utilities related to ballerina program debugging.
 *
 * @since 2.0.0
 */
public class DebugUtils {

    private static final String DEBUG_ARGS_JAVA = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y";
    private static final String JAVA_VERSION_PROP = "java.version";
    private static final String COMPATIBLE_JRE_VERSION = "17";

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
        validateJavaVersion(errorStream);
        return String.format("%s,address=*:%s", DEBUG_ARGS_JAVA, System.getProperty(SYSTEM_PROP_BAL_DEBUG));
    }

    public static String getProfileDebugArg(PrintStream errorStream) {
        validateJavaVersion(errorStream);
        return String.format("%s,address=*:%s", DEBUG_ARGS_JAVA, System.getProperty(SYSTEM_PROP_PROFILE_DEBUG));
    }

    private static void validateJavaVersion(PrintStream errorStream) {
        String javaVersion = System.getProperty(JAVA_VERSION_PROP);
        if (javaVersion != null && !javaVersion.startsWith(COMPATIBLE_JRE_VERSION)) {
            errorStream.printf("WARNING: Incompatible JRE version '%s' found. Ballerina program debugging supports " +
                    "on JRE version '%s'%n", javaVersion, COMPATIBLE_JRE_VERSION);
        }
    }

    public static boolean isInProfileDebugMode() {
        return System.getProperty(SYSTEM_PROP_PROFILE_DEBUG) != null &&
                !System.getProperty(SYSTEM_PROP_PROFILE_DEBUG).isEmpty();
    }

}
