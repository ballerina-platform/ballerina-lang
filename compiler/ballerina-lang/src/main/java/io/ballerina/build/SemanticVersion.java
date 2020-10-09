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
package io.ballerina.build;

import java.util.Objects;

/**
 * Represents a semantic version according to the semvar specification.
 *
 * @since 2.0.0
 */
public class SemanticVersion {
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
        throw new UnsupportedOperationException("This method is not yet implemented");
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
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }
}
