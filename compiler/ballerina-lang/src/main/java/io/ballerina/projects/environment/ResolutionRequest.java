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
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;

import java.util.Objects;
import java.util.Optional;

/**
 * {@code ResolutionRequest} is used to resolve a package from a repository.
 *
 * @since 2.0.0
 */
public final class ResolutionRequest {
    private final PackageDescriptor packageDesc;
    private final PackageDependencyScope scope;
    private final DependencyResolutionType dependencyResolutionType;

    // TODO rethink about this
    private final PackageLockingMode packageLockingMode;

    private ResolutionRequest(PackageDescriptor packageDescriptor,
                              PackageDependencyScope scope,
                              DependencyResolutionType dependencyResolutionType,
                              PackageLockingMode packageLockingMode) {
        this.packageDesc = packageDescriptor;
        this.scope = scope;
        this.dependencyResolutionType = dependencyResolutionType;
        this.packageLockingMode = packageLockingMode;
    }

    public static ResolutionRequest from(PackageDescriptor packageDescriptor) {
        return new ResolutionRequest(packageDescriptor, PackageDependencyScope.DEFAULT,
                DependencyResolutionType.SOURCE, PackageLockingMode.MEDIUM);
    }

    public static ResolutionRequest from(PackageDescriptor packageDescriptor,
                                         PackageDependencyScope scope) {
        return new ResolutionRequest(packageDescriptor, scope,
                DependencyResolutionType.SOURCE, PackageLockingMode.MEDIUM);
    }

    public static ResolutionRequest from(PackageDescriptor packageDescriptor,
                                         PackageDependencyScope scope,
                                         DependencyResolutionType resolutionType) {
        return new ResolutionRequest(packageDescriptor, scope,
                resolutionType, PackageLockingMode.MEDIUM);
    }

    public static ResolutionRequest from(PackageDescriptor packageDescriptor,
                                         PackageDependencyScope scope,
                                         DependencyResolutionType dependencyResolutionType,
                                         PackageLockingMode packageLockingMode) {
        return new ResolutionRequest(packageDescriptor, scope,
                dependencyResolutionType,
                packageLockingMode);
    }

    public PackageOrg orgName() {
        return packageDesc.org();
    }

    public PackageName packageName() {
        return packageDesc.name();
    }

    public Optional<PackageVersion> version() {
        return Optional.ofNullable(packageDesc.version());
    }

    public PackageDescriptor packageDescriptor() {
        return packageDesc;
    }

    public PackageDependencyScope scope() {
        return scope;
    }

    public Optional<String> repositoryName() {
        return packageDesc.repository();
    }

    public PackageLockingMode packageLockingMode() {
        return packageLockingMode;
    }

    public DependencyResolutionType resolutionType() {
        return dependencyResolutionType;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ResolutionRequest that = (ResolutionRequest) other;
        return Objects.equals(packageDesc, that.packageDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageDesc);
    }
}
