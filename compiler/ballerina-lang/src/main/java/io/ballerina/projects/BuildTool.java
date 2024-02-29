/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import java.util.Objects;

/**
 * Represents a tool integrated with package build.
 *
 * @since 2201.9.0
 */
public class BuildTool {
    private final BuildToolId id;
    private PackageVersion version;
    private PackageName name;
    private PackageOrg org;

    private BuildTool(BuildToolId id, PackageVersion version, PackageName name, PackageOrg org) {
        this.id = id;
        this.version = version;
        this.name = name;
        this.org = org;
    }

    /**
     * Create a build tool instance.
     *
     * @param id      build tool id
     * @param version build tool version
     * @return build tool instance
     */
    public static BuildTool from(BuildToolId id, PackageVersion version, PackageName name, PackageOrg org) {
        return new BuildTool(id, version, name, org);
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
    public String toString() {
        return id.toString() + (version != null ? ":" + version : "");
    }

    /**
     * Check whether another tool instance is equal to the current tool instance.
     *
     * @return true if the tool instances are equal, false otherwise
     */
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
}