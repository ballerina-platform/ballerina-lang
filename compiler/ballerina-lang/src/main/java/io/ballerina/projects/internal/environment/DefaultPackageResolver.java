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
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.projects.environment.PackageLoadRequest;
import io.ballerina.projects.environment.PackageLoadResponse;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.PackageResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Default Package resolver for Ballerina project.
 *
 * @since 2.0.0
 */
public class DefaultPackageResolver extends PackageResolver {
    private final Project project;
    private final PackageRepository distRepository;
    private final WritablePackageCache globalPackageCache;

    public DefaultPackageResolver(Project project, PackageRepository distCache, PackageCache globalPackageCache) {
        this.project = project;
        this.distRepository = distCache;
        this.globalPackageCache = (WritablePackageCache) globalPackageCache;
    }

    @Override
    public Collection<PackageLoadResponse> resolvePackages(Collection<PackageLoadRequest> packageLoadRequests) {
        if (packageLoadRequests.isEmpty()) {
            return Collections.emptyList();
        }

        List<PackageLoadResponse> packageLoadResponses = new ArrayList<>();
        Package currentPkg = this.project.currentPackage();
        for (PackageLoadRequest packageLoadRequest : packageLoadRequests) {
            Package resolvedPackage;
            if (packageLoadRequest.packageDescriptor().equals(currentPkg.descriptor())) {
                resolvedPackage = currentPkg;
            } else {
                resolvedPackage = loadPackageFromCache(packageLoadRequest);
                if (resolvedPackage == null) {
                    resolvedPackage = loadPackageFromDistributionCache(packageLoadRequest);
                }

                if (resolvedPackage == null) {
                    continue;
                }
            }
            packageLoadResponses.add(PackageLoadResponse.from(resolvedPackage, packageLoadRequest));
        }
        return packageLoadResponses;
    }

    private PackageVersion findlatest(List<PackageVersion> packageVersions) {
        // todo Fix me
        return packageVersions.get(0);
    }

    private Package loadPackageFromCache(PackageLoadRequest packageLoadRequest) {
        // TODO improve the logic
        List<Package> packageList = globalPackageCache.getPackages(packageLoadRequest.orgName(),
                packageLoadRequest.packageName());

        if (packageList.isEmpty()) {
            return null;
        }

        return packageList.get(0);
    }

    private Package loadPackageFromDistributionCache(PackageLoadRequest packageLoadRequest) {
        // If version is null load the latest package
        if (packageLoadRequest.version().isEmpty()) {
            // find the latest version
            List<PackageVersion> packageVersions = distRepository.getPackageVersions(packageLoadRequest);
            if (packageVersions.isEmpty()) {
                // no versions found.
                // todo handle package not found with exception
                return null;
            }
            PackageVersion latest = findlatest(packageVersions);
            packageLoadRequest = PackageLoadRequest.from(
                    PackageDescriptor.from(packageLoadRequest.packageName(), packageLoadRequest.orgName(), latest));
        }

        Optional<Package> packageOptional = distRepository.getPackage(packageLoadRequest);
        packageOptional.ifPresent(globalPackageCache::cache);
        return packageOptional.orElse(null);
    }
}
