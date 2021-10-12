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
package io.ballerina.projects.internal.environment;

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageId;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;

import java.util.List;
import java.util.Optional;

/**
 * Per project package cache that delegates most requests to the global cache.
 *
 * @since 2.0.0
 */
public class ProjectPackageCache implements WritablePackageCache {

    private final Project project;
    private final WritablePackageCache globalPackageCache;

    public ProjectPackageCache(Project project, WritablePackageCache globalPackageCache) {
        this.project = project;
        this.globalPackageCache = globalPackageCache;
    }

    @Override
    public void cache(Package pkg) {
        globalPackageCache.cache(pkg);
    }

    @Override
    public Optional<Package> getPackage(PackageId packageId) {
        if (isCurrentPackage(packageId)) {
            return Optional.of(project.currentPackage());
        }

        return globalPackageCache.getPackage(packageId);
    }

    @Override
    public Package getPackageOrThrow(PackageId packageId) {
        if (isCurrentPackage(packageId)) {
            return project.currentPackage();
        }

        return globalPackageCache.getPackageOrThrow(packageId);
    }

    @Override
    public Optional<Package> getPackage(PackageOrg packageOrg,
                                        PackageName packageName,
                                        PackageVersion version) {
        if (isCurrentPackage(packageOrg, packageName, version)) {
            return Optional.of(project.currentPackage());
        }
        return globalPackageCache.getPackage(packageOrg, packageName, version);
    }

    @Override
    public List<Package> getPackages(PackageOrg packageOrg, PackageName packageName) {
        if (isCurrentPackage(packageOrg, packageName)) {
            return List.of(project.currentPackage());
        }
        return globalPackageCache.getPackages(packageOrg, packageName);
    }

    private boolean isCurrentPackage(PackageId packageId) {
        return project.currentPackage().packageId() == packageId;
    }

    private boolean isCurrentPackage(PackageOrg packageOrg, PackageName packageName) {
        PackageDescriptor descriptor = project.currentPackage().descriptor();
        return descriptor.org().equals(packageOrg) && descriptor.name().equals(packageName);
    }

    private boolean isCurrentPackage(PackageOrg packageOrg, PackageName packageName, PackageVersion version) {
        PackageDescriptor descriptor = project.currentPackage().descriptor();
        return descriptor.org().equals(packageOrg) && descriptor.name().equals(packageName) &&
                descriptor.version().equals(version);
    }

    @Override
    @Deprecated
    public void removePackage(PackageId packageId) {}
}
