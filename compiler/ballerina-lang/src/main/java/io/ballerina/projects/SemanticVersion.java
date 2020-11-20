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
package io.ballerina.projects;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a semantic version according to the semvar specification.
 *
 * @since 2.0.0
 */
public class SemanticVersion {
    private static final Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.)?(\\d*)");
    private final int major;
    private final int minor;
    private final int patch;

    public SemanticVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public static SemanticVersion from(int major, int minor, int patch) {
        return new SemanticVersion(major, minor, patch);
    }

    public static SemanticVersion from(String versionString) {
        Matcher matcher = pattern.matcher(versionString);
        if (!matcher.matches()) {
            // TODO Proper error handling
            throw new IllegalArgumentException("Specified version: '" + versionString + "' is not semvar compatible");
        }

        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        String patchStr = matcher.group(3);
        int patch = (patchStr != null && !patchStr.isEmpty()) ? Integer.parseInt(patchStr) : 0;
        return SemanticVersion.from(major, minor, patch);
    }

    public int major() {
        return major;
    }

    public int minor() {
        return minor;
    }

    public int patch() {
        return patch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SemanticVersion that = (SemanticVersion) o;
        return major == that.major &&
                minor == that.minor &&
                patch == that.patch;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }
}
