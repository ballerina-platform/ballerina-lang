/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.projects;

import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;

import java.util.Objects;

/**
 * Represents a tool integrated with package build.
 *
 * @since 2201.9.0
 */
public class BuildTool {
    private final BuildToolId id;
    private PackageOrg org;
    private PackageName name;
    private PackageVersion version;
    private final TomlNodeLocation location;

    private BuildTool(
            BuildToolId id,
            PackageOrg org,
            PackageName name,
            PackageVersion version,
            TomlNodeLocation location) {
        this.id = id;
        this.org = org;
        this.name = name;
        this.version = version;
        this.location = location;
    }

    /**
     * Create a build tool instance.
     *
     * @param id      build tool id
     * @param version build tool version
     * @return build tool instance
     */
    public static BuildTool from(
            BuildToolId id,
            PackageOrg org,
            PackageName name,
            PackageVersion version,
            TomlNodeLocation location) {
        return new BuildTool(id, org, name, version, location);
    }

    /**
     * Get the id of a tool.
     *
     * @return build tool id
     */
    public BuildToolId id() {
        return id;
    }

    /**
     * Get the version of a tool.
     *
     * @return build tool version
     */
    public PackageVersion version() {
        return version;
    }

    /**
     * Get the name of a tool.
     *
     * @return build tool name
     */
    public PackageName name() {
        return name;
    }

    /**
     * Get the organization of a tool.
     *
     * @return build tool organization
     */
    public PackageOrg org() {
        return org;
    }

    /**
     * Get the location of the tool in Ballerina.toml.
     *
     * @return location of the tool
     */
    public TomlNodeLocation location() {
        return location;
    }

    /**
     * Set the version of a tool.
     *
     * @param version build tool version
     */
    public void setVersion(PackageVersion version) {
        this.version = version;
    }

    /**
     * Set the name of a tool.
     *
     * @param name build tool name
     */
    public void setName(PackageName name) {
        this.name = name;
    }

    /**
     * Set the organization of a tool.
     *
     * @param org build tool organization
     */
    public void setOrg(PackageOrg org) {
        this.org = org;
    }

    /**
     * Get the string representation of a tool.
     *
     * @return string representation of a tool
     */
    @Override
    public String toString() {
        return id.toString() + (version != null ? ":" + version : "");
    }

    /**
     * Check whether another tool instance is equal to the current tool instance.
     *
     * @return true if the tool instances are equal, false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        BuildTool that = (BuildTool) other;
        return id.equals(that.id) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Objects.hashCode(id),
                Objects.hashCode(org),
                Objects.hashCode(name),
                Objects.hashCode(version));
    }
}
