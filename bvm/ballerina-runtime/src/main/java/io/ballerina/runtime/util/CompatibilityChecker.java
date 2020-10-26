/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.util;

import java.io.PrintStream;

/**
 * Class contains utility methods to check the compatibility of
 * a ballerina program.
 *
 * @since 1.0.1
 */

public class CompatibilityChecker {

    private static final String JAVA_VERSION = "java.version";
    private static final String VERSION_ZERO = "0";
    private static final PrintStream stderr = System.err;

    /**
     * Check for the compatibility of a given java version against the current java runtime version.
     * This assumes the versions are in the following formats:
     * <ul>
     * <li>Java 8 or below: <i>1.8.x</i>, <i>1.7.x</i>, <i>1.6.x</i>, etc.</li>
     * <li>Java 9 or above: <i>9.x.y.z</i>, <i>10.x.y.z</i>, <i>11.x.y.z</i>, etc.</li>
     * </ul>
     * 
     * @param compiledVersion java version to check the compatibility
     */
    public static void verifyJavaCompatibility(String compiledVersion) {
        String runtimeVersion = System.getProperty(JAVA_VERSION);
        if (compiledVersion == null || runtimeVersion == null || compiledVersion.equals(runtimeVersion)) {
            return;
        }

        String[] compiledVersionParts = compiledVersion.split("\\.|_");
        String[] runtimeVersionParts = runtimeVersion.split("\\.|_");

        // check the major version.
        if (!compiledVersionParts[0].equals(runtimeVersionParts[0])) {
            String compiledMajorVersion = compiledVersionParts.length > 1 ? compiledVersionParts[1] : VERSION_ZERO;
            logJavaVersionMismatchError(runtimeVersion, compiledVersionParts[0], compiledMajorVersion);
            return;
        }

        // if both have only major versions, then stop checking further.
        if (compiledVersionParts.length == 1 && compiledVersionParts.length == 1) {
            return;
        }

        // if only one of them have a minor version, check whether the other one has
        // minor version as zero.
        // eg: v9 Vs v9.0.x
        if (compiledVersionParts.length == 1) {
            if (!runtimeVersionParts[1].equals(VERSION_ZERO)) {
                logJavaVersionMismatchError(runtimeVersion, compiledVersionParts[0], VERSION_ZERO);
            }
            return;
        }

        if (runtimeVersionParts.length == 1) {
            if (!compiledVersionParts[1].equals(VERSION_ZERO)) {
                logJavaVersionMismatchError(runtimeVersion, compiledVersionParts[0], compiledVersionParts[1]);
            }
            return;
        }

        // if both have minor versions, check for their equality.
        if (!compiledVersionParts[1].equals(runtimeVersionParts[1])) {
            logJavaVersionMismatchError(runtimeVersion, compiledVersionParts[0], compiledVersionParts[1]);
        }

        // ignore the patch versions.
    }

    private static void logJavaVersionMismatchError(String runtimeVersion, String compiledMajorVersion,
                                                    String compiledMinorVersion) {
        stderr.println("WARNING: Incompatible JRE version '" + runtimeVersion + "' found. This ballerina program " +
                "supports running on JRE version '" + compiledMajorVersion + "." + compiledMinorVersion + ".*'");
    }
}
