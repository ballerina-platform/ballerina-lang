package org.ballerinalang.toml.util;

import java.util.Locale;

/**
 * This class can be used to handle different path possibilities in the toml for native-libs and dependencies.
 *
 */
public class PathUtils {
    private static final String OS = System.getProperty("os.name").toLowerCase(Locale.getDefault());

    public static String getPath(String path) {
        if (path != null) {
            if (OS.contains("win")) {
                return path.replace("/", "\\");
            } else {
                return path.replace("\\", "/");
            }
        } else {
            return null;
        }
    }
}
