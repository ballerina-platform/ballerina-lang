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

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;

import java.util.Objects;

/**
 * Represents a semantic version according to the semvar specification.
 *
 * @since 2.0.0
 */
public class SemanticVersion {
    private final Version version;

    private SemanticVersion(Version version) {
        this.version = version;
    }

    public static SemanticVersion from(String versionString) {
        try {
            Version v = Version.valueOf(versionString);
            return new SemanticVersion(v);
        } catch (IllegalArgumentException e) {
            throw new ProjectException("Version cannot be empty");
        } catch (UnexpectedCharacterException e) {
            throw new ProjectException("Invalid version: '" + versionString + "'. " + e.toString());
        } catch (ParseException e) {
            throw new ProjectException("Invalid version: '" + versionString + "'. " + e.toString());
        }
    }

    public int major() {
        return version.getMajorVersion();
    }

    public int minor() {
        return version.getMinorVersion();
    }

    public int patch() {
        return version.getPatchVersion();
    }

    public String preReleasePart() {
        return version.getPreReleaseVersion();
    }

    public String buildMetadata() {
        return version.getBuildMetadata();
    }

    public boolean isStable() {
        if (this.major() == 0) {
            return false;
        }

        return !isPreReleaseVersion();
    }

    public boolean isPreReleaseVersion() {
        String preReleaseComp = version.getPreReleaseVersion();
        return preReleaseComp != null && !preReleaseComp.trim().isEmpty();
    }

    public boolean isInitialVersion() {
        return this.major() == 0;
    }

    public boolean greaterThan(SemanticVersion other) {
        return this.version.greaterThan(other.version);
    }

    public boolean greaterThanOrEqualTo(SemanticVersion other) {
        return this.version.greaterThanOrEqualTo(other.version);
    }

    public boolean lessThan(SemanticVersion other) {
        return this.version.lessThan(other.version);
    }

    public boolean lessThanOrEqualTo(SemanticVersion other) {
        return this.version.lessThanOrEqualTo(other.version);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        SemanticVersion otherSemVer = (SemanticVersion) other;
        return version.equals(otherSemVer.version);
    }

    @Override
    public String toString() {
        return version.toString();
    }

    @Override
    public int hashCode() {
        return version.hashCode();
    }

    public VersionCompatibilityResult compareTo(SemanticVersion other) {
        Objects.requireNonNull(other);

        if (this.equals(other)) {
            return VersionCompatibilityResult.EQUAL;
        }

        if (this.major() != other.major()) {
            return VersionCompatibilityResult.INCOMPATIBLE;
        }

        if (this.isInitialVersion() && (this.minor() != other.minor())) {
            return VersionCompatibilityResult.INCOMPATIBLE;
        }

        // We've eliminated initial versions and versions with different major component.
        // Now we just need to check minor, patch and pre-release components.
        int result = this.version.compareTo(other.version);
        if (result < 0) {
            return VersionCompatibilityResult.LESS_THAN;
        } else {
            return VersionCompatibilityResult.GREATER_THAN;
        }
    }

    /**
     * Represents the version compatibility between two {@code SemanticVersion} instances.
     *
     * @since 2.0.0
     */
    public enum VersionCompatibilityResult {
        INCOMPATIBLE,
        EQUAL,
        LESS_THAN,
        GREATER_THAN;
    }
}
