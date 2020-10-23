package org.ballerinalang.packerina.utils;

import org.ballerinalang.packerina.buildcontext.BuildContext;

import static io.ballerina.runtime.util.RuntimeConstants.SYSTEM_PROP_BAL_DEBUG;

/**
 * Utilities related to ballerina program debugging.
 */
public class DebugUtils {

    private static final String DEBUG_ARGS_JAVA11 = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y";
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
     * @param context Build context
     * @return debug arguments as a string
     */
    public static String getDebugArgs(BuildContext context) {
        String javaVersion = System.getProperty(JAVA_VERSION_PROP);
        if (javaVersion != null && !javaVersion.startsWith(COMPATIBLE_JRE_VERSION)) {
            context.err().printf("WARNING: Incompatible JRE version '%s' found. Ballerina program debugging supports " +
                    "on JRE version '%s'%n", javaVersion, COMPATIBLE_JRE_VERSION);
        }
        return String.format("%s,address=*:%s", DEBUG_ARGS_JAVA11, System.getProperty(SYSTEM_PROP_BAL_DEBUG));
    }
}
