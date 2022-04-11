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

import static io.ballerina.projects.util.ProjectConstants.BUILTIN_PACKAGE_VERSION_STR;

/**
 * Represents the name of a {@code Package}.
 *
 * @since 2.0.0
 */
public class PackageVersion {

    private final SemanticVersion version;

    /**
     * The version of all built-in packages.
     */
    public static final PackageVersion BUILTIN_PACKAGE_VERSION = PackageVersion.from(BUILTIN_PACKAGE_VERSION_STR);

    private PackageVersion(SemanticVersion version) {
        this.version = version;
    }

    public static PackageVersion from(String versionString) {
        // TODO Check whether the packageOrg is a valid Ballerina identifier
        return PackageVersion.from(SemanticVersion.from(versionString));
    }

    public static PackageVersion from(SemanticVersion version) {
        // TODO Check whether the packageOrg is a valid Ballerina identifier
        return new PackageVersion(version);
    }

    public SemanticVersion value() {
        return this.version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PackageVersion that = (PackageVersion) o;
        return version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }

    @Override
    public String toString() {
        return version.toString();
    }

    public SemanticVersion.VersionCompatibilityResult compareTo(PackageVersion other) {
        return this.version.compareTo(other.version);
    }
}
