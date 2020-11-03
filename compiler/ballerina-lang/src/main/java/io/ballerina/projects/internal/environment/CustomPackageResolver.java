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
import io.ballerina.projects.PackageId;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.ModuleLoadResponse;
import io.ballerina.projects.environment.PackageResolver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a temporary package loader implementation.
 * This is done to demonstrate the PackageResolver concept
 */
public class CustomPackageResolver extends PackageResolver {
    private final Project project;
    private final Map<PackageId, Package> loadedPackages = new HashMap<>();

    public CustomPackageResolver(Project project) {
        this.project = project;
    }

    public Collection<ModuleLoadResponse> loadPackages(Collection<ModuleLoadRequest> moduleLoadRequests) {
        // TODO This is a dummy implementation that checks only the current package in the project.
        List<ModuleLoadResponse> modLoadResponses = new ArrayList<>();
        Package currentPkg = this.project.currentPackage();
        for (ModuleLoadRequest modLoadRequest : moduleLoadRequests) {
            Module module = null;
            if (currentPkg.packageName().equals(modLoadRequest.packageName())) {
                module = currentPkg.module(modLoadRequest.moduleName());
            }

            if (module == null) {
                // Try to get from the already loaded packages
                module = loadFromCache(modLoadRequest);
            }

            if (module == null) {
                // TODO load the module from the custom_project_dir
                module = loadFromCustomProjectDir(modLoadRequest);
            }
            modLoadResponses.add(new ModuleLoadResponse(module.packageInstance().packageId(), module.moduleId(),
                    modLoadRequest));
        }
        return modLoadResponses;
    }

    public Package getPackage(PackageId packageId) {
        if (project.currentPackage().packageId() == packageId) {
            return project.currentPackage();
        }
        return loadedPackages.get(packageId);
    }

    // TODO this is a temporary workaround
    private Module loadFromCustomProjectDir(ModuleLoadRequest modLoadRequest) {
        // TODO how about developing a BALRProject to load a package from a .balr file?
        Path testProjectsDirPath = Paths.get("src/test/resources/test_projects_temporary/http");
        BuildProject buildProject = BuildProject.load(testProjectsDirPath);
        Package pkg = buildProject.currentPackage();
        pkg.resolveDependencies();
        loadedPackages.put(pkg.packageId(), pkg);
        return pkg.module(modLoadRequest.moduleName());
    }

    private Module loadFromCache(ModuleLoadRequest modLoadRequest) {
        // TODO improve the logic
        for (Package pkg : loadedPackages.values()) {
            // TODO this logic is wrong. We need to take org name into the equation
            if (pkg.packageName().equals(modLoadRequest.packageName())) {
                return pkg.module(modLoadRequest.moduleName());
            }
        }
        return null;
    }
}
