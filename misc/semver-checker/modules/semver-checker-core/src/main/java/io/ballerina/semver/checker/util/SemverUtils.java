/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semver.checker.util;

import io.ballerina.projects.SemanticVersion;
import io.ballerina.semver.checker.diff.PackageDiff;

/**
 * Semantic versioning related utilities.
 *
 * @since 2201.2.0
 */
public class SemverUtils {

    private static final String SEMVER_FORMAT = "%d.%d.%d";
    public static final String BAL_FILE_EXT = ".bal";

    public static String calculateSuggestedVersion(SemanticVersion prevVersion, PackageDiff packageDiff) {
        if (packageDiff == null) {
            return prevVersion.toString();
        } else {
            // Todo: add support for pre-release versions
            switch (packageDiff.getVersionImpact()) {
                case MAJOR:
                case AMBIGUOUS:
                    if (prevVersion.isInitialVersion()) {
                        return String.format(SEMVER_FORMAT, prevVersion.major(), prevVersion.minor() + 1, 0);
                    } else {
                        return String.format(SEMVER_FORMAT, prevVersion.major() + 1, 0, 0);
                    }
                case MINOR:
                    return String.format(SEMVER_FORMAT, prevVersion.major(), prevVersion.minor() + 1, 0);
                case PATCH:
                    return String.format(SEMVER_FORMAT, prevVersion.major(), prevVersion.minor(),
                            prevVersion.patch() + 1);
                case UNKNOWN:
                default:
                    return "N/A";
            }
        }
    }
}
