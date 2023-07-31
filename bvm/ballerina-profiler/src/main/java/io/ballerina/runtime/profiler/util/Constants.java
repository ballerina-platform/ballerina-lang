package io.ballerina.runtime.profiler.util;

import java.io.PrintStream;

/**
 * This class contains the constant variables for the ballerina profiler.
 *
 * @since 2201.7.0
 */
public class Constants {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GRAY = "\033[37m";
    public static final String ANSI_CYAN = "\033[1;38;2;32;182;176m";
    public static final String ANSI_YELLOW = "\033[1;38;2;255;255;0m";
    public static final String TEMP_JAR_FILE_NAME = "temp.jar";
    public static final String STRAND = "(Lio/ballerina/runtime/internal/scheduling/Strand";
    public static final PrintStream OUT = System.out;
    public static final PrintStream ERROR = System.err;
    public static final String CLASS_SUFFIX = ".class";
}
