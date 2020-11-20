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

import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.ModuleLoadResponse;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.projects.environment.PackageLoadRequest;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.PackageResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Resolves lang lib packages.
 *
 * @since 2.0.0
 */
public class LangLibResolver extends PackageResolver {
    private final PackageRepository distCache;
    private final WritablePackageCache globalPackageCache;

    public LangLibResolver(PackageRepository distCache, PackageCache globalPackageCache) {
        this.distCache = distCache;
        this.globalPackageCache = (WritablePackageCache) globalPackageCache;
    }

    @Override
    public Collection<ModuleLoadResponse> loadPackages(Collection<ModuleLoadRequest> moduleLoadRequests) {
        // TODO This is a dummy implementation that checks only the current package in the project.
        List<ModuleLoadResponse> modLoadResponses = new ArrayList<>();
        for (ModuleLoadRequest modLoadRequest : moduleLoadRequests) {
            Module module = loadFromCache(modLoadRequest);
            if (module == null) {
                module = loadFromDistributionCache(modLoadRequest);
            }

            if (module == null) {
                continue;
            }
            modLoadResponses.add(new ModuleLoadResponse(module.packageInstance().packageId(), module.moduleId(),
                    modLoadRequest));
        }
        return modLoadResponses;
    }

    private Module loadFromDistributionCache(ModuleLoadRequest modLoadRequest) {
        // If version is null load the latest package
        PackageLoadRequest loadRequest = PackageLoadRequest.from(modLoadRequest);
        if (loadRequest.version().isEmpty()) {
            // find the latest version
            List<PackageVersion> packageVersions = distCache.getPackageVersions(loadRequest);
            if (packageVersions.isEmpty()) {
                // no versions found.
                // todo handle package not found with exception
                return null;
            }
            PackageVersion latest = findlatest(packageVersions);
            loadRequest = PackageLoadRequest.from(
                    PackageDescriptor.from(loadRequest.packageName(),
                            loadRequest.orgName().orElse(null), latest));
        }

        Optional<Package> packageOptional = distCache.getPackage(loadRequest);

        return packageOptional.map(pkg -> {
            pkg.resolveDependencies();
            globalPackageCache.cache(pkg);
            // todo fetch the correct module and return
            return pkg.getDefaultModule();
        }).orElse(null);
    }

    private PackageVersion findlatest(List<PackageVersion> packageVersions) {
        // todo Fix me
        return packageVersions.get(0);
    }

    private Module loadFromCache(ModuleLoadRequest modLoadRequest) {
        // TODO improve the logic
        List<Package> packageList = globalPackageCache.getPackages(
                modLoadRequest.orgName().orElse(null), modLoadRequest.packageName());

        if (packageList.isEmpty()) {
            return null;
        }

        Package pkg = packageList.get(0);
        return pkg.module(modLoadRequest.moduleName());
    }
}
