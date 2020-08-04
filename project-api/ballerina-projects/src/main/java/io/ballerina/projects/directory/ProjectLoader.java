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
package io.ballerina.projects.directory;

import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleConfig;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.PackageId;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains a set of utility methods that creates the config hierarchy from the project directory.
 *
 * @since 2.0.0
 */
class ProjectLoader {
    public static PackageConfig loadPackage(String packageDir) {
        final PackageData packageData = ProjectFiles.loadPackageData(packageDir);
        return createPackageConfig(packageData);
    }

    public static PackageConfig createPackageConfig(PackageData packageData) {
        final Path packagePage = packageData.packagePath();
        final PackageId packageId = PackageId.create(packagePage.toString());
        ModuleConfig defaultModuleConfig = createModuleData(packageData.defaultModule(), packageId);
        List<ModuleConfig> otherModuleConfigs = packageData.otherModules()
                .stream()
                .map(moduleData -> createModuleData(moduleData, packageId))
                .collect(Collectors.toList());
        return PackageConfig.from(packageId, packagePage, defaultModuleConfig, otherModuleConfigs);
    }

    public static ModuleConfig createModuleData(ModuleData moduleData, PackageId packageId) {
        final ModuleId moduleId = ModuleId.create(moduleData.moduleDirectoryPath().toString(), packageId);
        List<DocumentConfig> srcDocs = moduleData.sourceDocs()
                .stream()
                .map(srcDoc -> createDocumentData(srcDoc, moduleId))
                .collect(Collectors.toList());
        List<DocumentConfig> testSrcDocs = moduleData.testSourceDocs()
                .stream()
                .map(testSrcDoc -> createDocumentData(testSrcDoc, moduleId))
                .collect(Collectors.toList());
        return ModuleConfig.from(moduleId, moduleData.moduleDirectoryPath(), srcDocs, testSrcDocs);
    }

    public static DocumentConfig createDocumentData(DocumentData documentData, ModuleId moduleId) {
        final DocumentId documentId = DocumentId.create(documentData.filePath().toString(), moduleId);
        return DocumentConfig.from(documentId, documentData.filePath());
    }
}
