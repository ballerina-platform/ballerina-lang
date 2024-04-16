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

package io.ballerina.projects.environment;

import io.ballerina.projects.BuildTool;
import io.ballerina.projects.BuildToolId;
import io.ballerina.projects.PackageVersion;

import java.util.Objects;
import java.util.Optional;

/**
 * {@code ToolResolutionRequest} is used to resolve a tool from a repository.
 *
 * @since 2201.9.0
 */
public final class ToolResolutionRequest {
    private final BuildTool tool;
    private final PackageLockingMode packageLockingMode;

    private ToolResolutionRequest(BuildTool tool, PackageLockingMode packageLockingMode) {
        this.tool = tool;
        this.packageLockingMode = packageLockingMode;
    }

    public static ToolResolutionRequest from(BuildTool tool, PackageLockingMode packageLockingMode) {
        return new ToolResolutionRequest(tool, packageLockingMode);
    }

    public BuildToolId id() {
        return tool.id();
    }

    public Optional<PackageVersion> version() {
        return Optional.ofNullable(tool.version());
    }

    public PackageLockingMode packageLockingMode() {
        return packageLockingMode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ToolResolutionRequest that = (ToolResolutionRequest) other;
        return Objects.equals(tool, that.tool);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tool);
    }
}
