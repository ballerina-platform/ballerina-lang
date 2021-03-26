/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.utils;

import com.sun.jdi.Location;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.Project;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.util.ProjectUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of Ballerina package dependency (stdlib, central modules) source resolver.
 *
 * @since 2.0.0
 */
public class DependencySourceResolver extends SourceResolver {

    DependencySourceResolver(Project sourceProject) {
        super(sourceProject);
    }

    @Override
    public boolean canResolve(Location location) {
        try {
            LocationInfo locationInfo = new LocationInfo(location);
            if (!locationInfo.isValid()) {
                return false;
            }

            List<PackageName> possiblePackageNames = ProjectUtils.getPossiblePackageNames(locationInfo.moduleName());
            for (PackageName packageName : possiblePackageNames) {
                PackageResolution resolution = sourceProject.currentPackage().getResolution();
                for (ResolvedPackageDependency packageDependency : resolution.dependencyGraph().getNodes()) {
                    if (packageDependency.packageInstance().packageOrg().value().equals(locationInfo.orgName())
                            && packageDependency.packageInstance().packageName().equals(packageName)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Optional<Path> resolve(Location location) {
        try {
            LocationInfo locationInfo = new LocationInfo(location);
            if (!locationInfo.isValid()) {
                return Optional.empty();
            }
            List<PackageName> possiblePkgNames = ProjectUtils.getPossiblePackageNames(locationInfo.moduleName());
            for (PackageName packageName : possiblePkgNames) {
                PackageResolution resolution = sourceProject.currentPackage().getResolution();
                for (ResolvedPackageDependency dependency : resolution.dependencyGraph().getNodes()) {
                    if (!dependency.packageInstance().packageOrg().value().equals(locationInfo.orgName())
                            || !dependency.packageInstance().packageName().equals(packageName)) {
                        continue;
                    }

                    ModuleName modName = findModuleName(packageName, locationInfo.moduleName());
                    Module module = dependency.packageInstance().module(modName);
                    if (module == null) {
                        continue;
                    }

                    for (DocumentId documentId : module.documentIds()) {
                        if (!module.document(documentId).name().equals(locationInfo.fileName())) {
                            continue;
                        }
                        Project dependencyProject = dependency.packageInstance().project();
                        return dependencyProject.documentPath(documentId);
                    }
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static ModuleName findModuleName(PackageName packageName, String moduleNameStr) {
        if (packageName.value().equals(moduleNameStr)) {
            return ModuleName.from(packageName);
        } else {
            String moduleNamePart = moduleNameStr.substring(packageName.value().length() + 1);
            return ModuleName.from(packageName, moduleNamePart);
        }
    }
}
