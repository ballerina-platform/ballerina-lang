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
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.Project;
import io.ballerina.projects.ResolvedPackageDependency;

import java.nio.file.Path;
import java.util.Collection;
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
    public boolean isSupported(Location location) {
        try {
            LocationInfo locationInfo = new LocationInfo(location);
            if (!locationInfo.isValid()) {
                return false;
            }

            PackageResolution resolution = sourceProject.currentPackage().getResolution();
            for (ResolvedPackageDependency packageDependency : resolution.dependencyGraph().getNodes()) {
                Package depPackage = packageDependency.packageInstance();
                if (!depPackage.packageOrg().value().equals(locationInfo.orgName())) {
                    continue;
                }
                for (ModuleId moduleId : depPackage.moduleIds()) {
                    if (depPackage.module(moduleId).moduleName().toString().equals(locationInfo.moduleName())) {
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

            PackageResolution resolution = sourceProject.currentPackage().getResolution();
            Collection<ResolvedPackageDependency> dependencies = resolution.dependencyGraph().getNodes();
            for (ResolvedPackageDependency dependency : dependencies) {
                Package depPackage = dependency.packageInstance();
                if (!depPackage.packageOrg().value().equals(locationInfo.orgName())) {
                    continue;
                }

                for (ModuleId moduleId : depPackage.moduleIds()) {
                    if (!depPackage.module(moduleId).moduleName().toString().equals(locationInfo.moduleName())) {
                        continue;
                    }
                    Module module = depPackage.module(moduleId);
                    for (DocumentId docId : module.documentIds()) {
                        if (module.document(docId).name().equals(locationInfo.fileName())) {
                            return module.project().documentPath(docId);
                        }
                    }
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
