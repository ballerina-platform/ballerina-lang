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
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.Project;
import io.ballerina.projects.environment.PackageCache;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of Ballerina lang-lib dependency source resolver.
 *
 * @since 2.0.0
 */
public class LangLibSourceResolver extends DependencySourceResolver {

    LangLibSourceResolver(Project sourceProject) {
        super(sourceProject);
    }

    @Override
    public boolean isSupported(Location location) {
        try {
            LocationInfo locationInfo = new LocationInfo(location);
            if (!locationInfo.isValid()) {
                return false;
            }
            PackageCache packageCache = sourceProject.projectEnvironmentContext().environment()
                    .getService(PackageCache.class);
            List<Package> langLibPackage = packageCache.getPackages(
                    PackageOrg.from(locationInfo.orgName()),
                    PackageName.from(locationInfo.moduleName()));

            return !langLibPackage.isEmpty();
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

            PackageCache packageCache = sourceProject.projectEnvironmentContext().environment()
                    .getService(PackageCache.class);
            List<Package> langLibPackage = packageCache.getPackages(PackageOrg.from(locationInfo.orgName()),
                    PackageName.from(locationInfo.moduleName()));
            if (langLibPackage.isEmpty()) {
                return Optional.empty();
            }

            for (ModuleId moduleId : langLibPackage.get(0).moduleIds()) {
                Module module = langLibPackage.get(0).module(moduleId);
                for (DocumentId docId : module.documentIds()) {
                    if (module.document(docId).name().equals(locationInfo.fileName())) {
                        return module.project().documentPath(docId);
                    }
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
