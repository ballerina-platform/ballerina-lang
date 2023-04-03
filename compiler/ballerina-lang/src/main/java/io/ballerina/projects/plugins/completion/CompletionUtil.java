package io.ballerina.projects.plugins.completion;

/**
 * Util class for completion providers.
 *
 * @since 2201.7.0
 */
public class CompletionUtil {

    public static final String LINE_BREAK = System.lineSeparator();
    public static final String PADDING = "\t";

    public static String getPlaceHolderText(int index, String defaultValue) {
        return "${" + index + ":" + defaultValue + "}";
    }

    public static String getPlaceHolderText(int index) {
        return "${" + index + "}";
    }
}
