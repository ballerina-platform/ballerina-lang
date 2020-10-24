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
package io.ballerina.projects.env;

import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageId;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.environment.GlobalPackageCache;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.ModuleLoadResponse;
import io.ballerina.projects.environment.PackageLoadRequest;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.Repository;

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
    private final Repository distCache;
    private final GlobalPackageCache globalPackageCache;

    public LangLibResolver(Repository distCache, GlobalPackageCache globalPackageCache) {
        this.distCache = distCache;
        this.globalPackageCache = globalPackageCache;
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

    @Override
    public Package getPackage(PackageId packageId) {
        return globalPackageCache.get(packageId);
    }

    private Module loadFromDistributionCache(ModuleLoadRequest modLoadRequest) {
        // If version is null load the latest package
        PackageLoadRequest loadRequest = PackageLoadRequest.from(modLoadRequest);
        if (loadRequest.version().isEmpty()) {
            // find the latest version
            List<SemanticVersion> packageVersions = distCache.getPackageVersions(loadRequest);
            if (packageVersions.isEmpty()) {
                // no versions found.
                // todo handle package not found with exception
                return null;
            }
            SemanticVersion latest = findlatest(packageVersions);
            loadRequest = new PackageLoadRequest(loadRequest.orgName()
                    .orElse(null), loadRequest.packageName(), latest);
        }

        Optional<Package> packageOptional = distCache.getPackage(loadRequest);

        return packageOptional.map(pkg -> {
            pkg.resolveDependencies();
            globalPackageCache.put(pkg);
            // todo fetch the correct module and return
            return pkg.getDefaultModule();
        }).orElse(null);
    }

    private SemanticVersion findlatest(List<SemanticVersion> packageVersions) {
        // todo Fix me
        return packageVersions.get(0);
    }

    private Module loadFromCache(ModuleLoadRequest modLoadRequest) {
        // TODO improve the logic
        for (Package pkg : globalPackageCache.values()) {
            // TODO this logic is wrong. We need to take org name into the equation
            if (pkg.packageName().equals(modLoadRequest.packageName())) {
                return pkg.module(modLoadRequest.moduleName());
            }
        }
        return null;
    }
}
