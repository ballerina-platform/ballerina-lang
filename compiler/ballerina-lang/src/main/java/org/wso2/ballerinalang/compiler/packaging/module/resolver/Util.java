/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import java.util.regex.Pattern;

/**
 * Utility functions used by module resolver.
 */
public class Util {

    private Util() {
    }

    static boolean isAbsoluteVersion(String version) {
        final Pattern semVerPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
        return semVerPattern.matcher(version).matches();
    }

    static boolean isValidVersion(String version, String filter) {
        if ("".equals(version)) {
            return false;
        }

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

    static boolean isGreaterVersion(String version, String newVersion) {
        String[] levels1 = version.split("\\.");
        String[] levels2 = newVersion.split("\\.");

        int length = Math.max(levels1.length, levels2.length);
        for (int i = 0; i < length; i++) {
            Integer v1 = i < levels1.length ? Integer.parseInt(levels1[i]) : 0;
            Integer v2 = i < levels2.length ? Integer.parseInt(levels2[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0) {
                if (compare < 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
