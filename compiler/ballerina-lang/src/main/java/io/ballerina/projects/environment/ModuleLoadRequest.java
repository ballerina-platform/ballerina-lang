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

import io.ballerina.projects.DependencyResolutionType;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageOrg;
import io.ballerina.tools.diagnostics.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This class represents a Ballerina module load request received by the {@code PackageResolver}.
 *
 * @since 2.0.0
 */
public class ModuleLoadRequest {
    private final PackageOrg orgName;
    private final String moduleName;
    private final PackageDependencyScope scope;
    private final DependencyResolutionType dependencyResolvedType;
    private final List<Location> locations = new ArrayList<>();

    public ModuleLoadRequest(PackageOrg orgName,
                             String moduleName,
                             PackageDependencyScope scope,
                             DependencyResolutionType dependencyResolvedType) {
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.scope = scope;
        this.dependencyResolvedType = dependencyResolvedType;
    }

    public ModuleLoadRequest(PackageOrg orgName,
                             String moduleName,
                             PackageDependencyScope scope,
                             DependencyResolutionType dependencyResolvedType,
                             Location location) {
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.scope = scope;
        this.dependencyResolvedType = dependencyResolvedType;
        this.locations.add(location);
    }

    public Optional<PackageOrg> orgName() {
        return Optional.ofNullable(orgName);
    }

    public String moduleName() {
        return moduleName;
    }

    public PackageDependencyScope scope() {
        return scope;
    }

    public DependencyResolutionType dependencyResolvedType() {
        return dependencyResolvedType;
    }

    public boolean injected() {
        return dependencyResolvedType == DependencyResolutionType.PLATFORM_PROVIDED;
    }

    public List<Location> locations() {
        return locations;
    }

    public void addAllLocations(List<Location> locations) {
        this.locations.addAll(locations);
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
                moduleName.equals(that.moduleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgName, moduleName);
    }
}
