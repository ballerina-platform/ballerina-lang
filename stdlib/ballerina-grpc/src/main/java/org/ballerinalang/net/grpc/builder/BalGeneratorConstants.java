package org.ballerinalang.net.grpc.builder;

/**
 * Constants that use in .bal file generation.
 */
public class BalGeneratorConstants {
    public static final String NEW_LINE_CHARACTER = System.getProperty("line.separator");
    public static final String FILE_SEPERATOR = System.getProperty("file.separator");
    public static final int FILE_INDEX = 0;
    static final int SERVICE_INDEX = 0;
    public static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
}
