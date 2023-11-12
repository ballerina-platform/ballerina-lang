package io.ballerina.runtime.profiler.util;

import java.io.PrintStream;

/**
 * This class contains the constant variables for the Ballerina profiler.
 *
 * @since 2201.8.0
 */
public class Constants {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GRAY = "\033[37m";
    public static final String ANSI_CYAN = "\033[1;38;2;32;182;176m";
    public static final String ANSI_YELLOW = "\033[1;38;2;255;255;0m";

    public static final PrintStream OUT_STREAM = System.out;
    public static final PrintStream ERROR_STREAM = System.err;

    public static final String CLASS_SUFFIX = ".class";
    public   static final String PROFILE_ANALYZER = "io/ballerina/runtime/profiler/runtime/ProfileAnalyzer";
    public   static final String GET_INSTANCE_DESCRIPTOR = "()L" + PROFILE_ANALYZER + ";";
    public static final String CPU_PRE_JSON = "cpu_pre.json";
    public static final String PERFORMANCE_JSON =  "performance_report.json";
    public static final String TEMP_JAR_FILE_NAME = "temp.jar";
    public static final String STRAND_PROFILER_STACK_PROPERTY = "b7a.profile.stack";

    public static final String STRAND_CLASS = "io/ballerina/runtime/internal/scheduling/Strand";
    public static final String DATA_CLASS = "io/ballerina/runtime/profiler/runtime/Data";
    public static final String STRING_CLASS = "java/lang/String";
    public static final String STRAND_ARG = "(L" + STRAND_CLASS;
    public static final String START_PROFILE_DESCRIPTOR =
            "(L" + STRAND_CLASS + ";L" + STRING_CLASS + ";L" + STRING_CLASS + ";)L" + DATA_CLASS + ";";
    public static final String STOP_PROFILE_DESCRIPTOR =
            "(L" + STRAND_CLASS + ";L" + DATA_CLASS + ";)V";
    public static final String CURRENT_DIR_KEY = "current.dir";
    public static final String USER_DIR = "user.dir";

    private Constants() {
    }
}
