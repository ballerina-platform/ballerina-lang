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
package io.ballerina.projects.environment;

import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;

import java.util.Objects;
import java.util.Optional;

/**
 * This class represents a Ballerina module load request received by the {@code PackageResolver}.
 *
 * @since 2.0.0
 */
public class ModuleLoadRequest {
    private final PackageOrg orgName;
    private final PackageName packageName;
    private final ModuleName moduleName;
    private final PackageVersion version;

    public ModuleLoadRequest(PackageOrg orgName,
                             PackageName packageName,
                             ModuleName moduleName,
                             PackageVersion version) {
        if (orgName != null && orgName.value().isEmpty()) {
            throw new IllegalArgumentException("The orgName cannot be an empty string. " +
                    "It should be either null or a non-empty string value");
        }
        this.orgName = orgName;
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.version = version;
    }

    public Optional<PackageOrg> orgName() {
        return Optional.ofNullable(orgName);
    }

    public PackageName packageName() {
        return packageName;
    }

    public ModuleName moduleName() {
        return moduleName;
    }

    public Optional<PackageVersion> version() {
        return Optional.ofNullable(version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ModuleLoadRequest that = (ModuleLoadRequest) o;
        return Objects.equals(orgName, that.orgName) &&
                packageName.equals(that.packageName) &&
                moduleName.equals(that.moduleName) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgName, packageName, moduleName, version);
    }
}
