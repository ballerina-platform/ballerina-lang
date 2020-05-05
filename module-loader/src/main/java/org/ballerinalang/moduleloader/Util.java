package org.ballerinalang.moduleloader;

public class Util {

    public static boolean isValidVersion(String version, String filter) {
        String[] versionParts = version.split("\\.");
        int majorVersion = Integer.parseInt(versionParts[0]);
        int minorVersion = Integer.parseInt(versionParts[1]);
        int patchVersion = Integer.parseInt(versionParts[2]);

        // _, *, 1.*, 1.2.*, 1.2.3, ~1.2.3

        if (filter == null || "".equals(filter)) {
            return true;
        } else if (filter.contains("*")) {
            String filterPart = filter.substring(0, filter.indexOf("*"));
            if ("".equals(filterPart)) {
                return true;
            }
            String[] filterParts = filterPart.split("\\.");
            if (filterParts.length == 1) {
                int filterMajorVersion = Integer.parseInt(filterParts[0]);
                return majorVersion == filterMajorVersion;
            } else if (filterParts.length == 2) {
                int filterMajorVersion = Integer.parseInt(filterParts[0]);
                int filterMinorVersion = Integer.parseInt(filterParts[1]);
                return majorVersion == filterMajorVersion && minorVersion == filterMinorVersion;
            }
        } else if (filter.contains("~")) {
            String[] filterParts = filter.split("\\.");
            int filterMajorVersion = Integer.parseInt(filterParts[0]);
            int filterMinorVersion = Integer.parseInt(filterParts[1]);
            return majorVersion == filterMajorVersion && minorVersion == filterMinorVersion;
        } else {
            String[] filterParts = filter.split("\\.");
            int filterMajorVersion = Integer.parseInt(filterParts[0]);
            int filterMinorVersion = Integer.parseInt(filterParts[1]);
            int filterPatchVersion = Integer.parseInt(filterParts[2]);
            return majorVersion == filterMajorVersion && minorVersion == filterMinorVersion
                    && patchVersion == filterPatchVersion;
        }
        return false;
    }
}
