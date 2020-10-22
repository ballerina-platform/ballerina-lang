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
import io.ballerina.projects.balo.BaloProject;
import io.ballerina.projects.environment.GlobalPackageCache;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.ModuleLoadResponse;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.repos.PackageRepo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class LangLibResolver extends PackageResolver {
    private final PackageRepo distCache;
    private final GlobalPackageCache globalPackageCache;

    public LangLibResolver(PackageRepo distCache, GlobalPackageCache globalPackageCache) {
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
        Optional<Path> balo;
        if (modLoadRequest.version().isEmpty()) {
            balo = distCache.getLatestPackageBalo(modLoadRequest);
        } else {
            balo = distCache.getPackageBalo(modLoadRequest);
        }
        if (balo.isEmpty()) {
            return null;
        }
        BaloProject buildProject = BaloProject.loadProject(balo.get());
        Package pkg = buildProject.currentPackage();
        pkg.resolveDependencies();
        globalPackageCache.put(pkg);
        return pkg.module(modLoadRequest.moduleName());
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
