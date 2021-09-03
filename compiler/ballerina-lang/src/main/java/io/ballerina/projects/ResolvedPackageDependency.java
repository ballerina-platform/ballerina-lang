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

/**
 * Represents a dependency of a package which already resolved from a repository.
 *
 * @since 2.0.0
 */
public class ResolvedPackageDependency extends PackageDependency {
    private final Package resolvedPackage;
    private final DependencyResolutionType dependencyResolvedType;

    public ResolvedPackageDependency(Package resolvedPackage, PackageDependencyScope scope) {
        super(resolvedPackage.packageId(), scope);
        this.resolvedPackage = resolvedPackage;
        this.dependencyResolvedType = DependencyResolutionType.SOURCE;
    }

    public ResolvedPackageDependency(Package resolvedPackage,
                                     PackageDependencyScope scope,
                                     DependencyResolutionType dependencyResolvedType) {
        super(resolvedPackage.packageId(), scope);
        this.resolvedPackage = resolvedPackage;
        this.dependencyResolvedType = dependencyResolvedType;
    }

    public Package packageInstance() {
        return resolvedPackage;
    }

    public DependencyResolutionType dependencyResolvedType() {
        return dependencyResolvedType;
    }

    public boolean isPlatformProvided() {
        return dependencyResolvedType == DependencyResolutionType.PLATFORM_PROVIDED;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
