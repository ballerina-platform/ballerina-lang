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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static io.ballerina.identifier.Utils.decodeIdentifier;
import static org.ballerinalang.debugadapter.utils.PackageUtils.GEN_MODULE_DIR;
import static org.ballerinalang.debugadapter.utils.PackageUtils.USER_MODULE_DIR;
import static org.ballerinalang.debugadapter.utils.PackageUtils.getDefaultModuleName;
import static org.ballerinalang.debugadapter.utils.PackageUtils.getOrgName;

/**
 * Concrete implementation of Ballerina project(package) source resolver.
 *
 * @since 2.0.0
 */
public class ProjectSourceResolver extends SourceResolver {

    ProjectSourceResolver(Project sourceProject) {
        super(sourceProject);
    }

    @Override
    public boolean isSupported(Location location) {
        try {
            if (sourceProject.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                DocumentId docId = sourceProject.currentPackage().getDefaultModule().documentIds().iterator().next();
                Document document = sourceProject.currentPackage().getDefaultModule().document(docId);
                return document.name().equals(location.sourcePath()) && document.name().equals(location.sourceName());
            } else if (sourceProject.kind() == ProjectKind.BUILD_PROJECT) {
                String projectOrg = getOrgName(sourceProject);
                DebugSourceLocation debugSourceLocation = new DebugSourceLocation(location);
                return debugSourceLocation.isValid() && debugSourceLocation.orgName().equals(projectOrg);
            } else {
                return false;
            }
        } catch (AbsentInformationException e) {
            return false;
        }
    }

    @Override
    public Optional<Path> resolve(Location location) {
        try {
            String projectRoot = sourceProject.sourceRoot().toAbsolutePath().toString();

            if (sourceProject.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                DocumentId docId = sourceProject.currentPackage().getDefaultModule().documentIds().iterator().next();
                Document document = sourceProject.currentPackage().getDefaultModule().document(docId);
                if (!document.name().equals(location.sourcePath()) || !document.name().equals(location.sourceName())) {
                    return Optional.empty();
                }
                return Optional.of(Paths.get(projectRoot));
            } else if (sourceProject.kind() == ProjectKind.BUILD_PROJECT) {
                String projectOrg = getOrgName(sourceProject);
                String defaultModuleName = getDefaultModuleName(sourceProject);
                String locationName = location.sourceName();
                DebugSourceLocation debugSourceLocation = new DebugSourceLocation(location);

                if (!debugSourceLocation.isValid() || !debugSourceLocation.orgName().equals(projectOrg)) {
                    return Optional.empty();
                }

                String modulePart = decodeIdentifier(debugSourceLocation.moduleName());
                modulePart = modulePart.replaceFirst(defaultModuleName, "");
                if (modulePart.startsWith(".")) {
                    modulePart = modulePart.replaceFirst("\\.", "");
                }

                if (modulePart.isBlank()) {
                    // default module
                    return Optional.of(Paths.get(projectRoot, locationName));
                } else {
                    // other modules
                    // 1. check and return if there's a user module source matching to the location information.
                    File moduleFile = Paths.get(projectRoot, USER_MODULE_DIR, modulePart, locationName).toFile();
                    if (moduleFile.isFile()) {
                        return Optional.of(moduleFile.toPath().toAbsolutePath());
                    }

                    // 2. if not, check and return if there's a generated module source matching to the location
                    // information.
                    File generatedFile = Paths.get(projectRoot, GEN_MODULE_DIR, modulePart, locationName).toFile();
                    if (generatedFile.isFile()) {
                        return Optional.of(generatedFile.toPath().toAbsolutePath());
                    }
                }
            }
            return Optional.empty();
        } catch (AbsentInformationException e) {
            return Optional.empty();
        }
    }
}
