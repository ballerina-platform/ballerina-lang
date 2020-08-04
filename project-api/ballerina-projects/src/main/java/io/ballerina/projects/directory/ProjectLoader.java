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
        final PackageFileData packageFileData = ProjectFiles.loadPackageData(packageDir);
        return createPackageConfig(packageFileData);
    }

    public static PackageConfig createPackageConfig(PackageFileData packageFileData) {
        final Path packagePage = packageFileData.packagePath();
        final PackageId packageId = PackageId.create(packagePage.toString());
        ModuleConfig defaultModuleConfig = createModuleData(packageFileData.defaultModule(), packageId);
        List<ModuleConfig> otherModuleConfigs = packageFileData.otherModules()
                .stream()
                .map(moduleFileData -> createModuleData(moduleFileData, packageId))
                .collect(Collectors.toList());
        return PackageConfig.from(packageId, packagePage, defaultModuleConfig, otherModuleConfigs);
    }

    public static ModuleConfig createModuleData(ModuleFileData moduleFileData, PackageId packageId) {
        final ModuleId moduleId = ModuleId.create(moduleFileData.moduleDirectoryPath().toString(), packageId);
        List<DocumentConfig> srcDocs = moduleFileData.sourceDocs()
                .stream()
                .map(srcDoc -> createDocumentData(srcDoc, moduleId))
                .collect(Collectors.toList());
        List<DocumentConfig> testSrcDocs = moduleFileData.testSourceDocs()
                .stream()
                .map(testSrcDoc -> createDocumentData(testSrcDoc, moduleId))
                .collect(Collectors.toList());
        return ModuleConfig.from(moduleId, moduleFileData.moduleDirectoryPath(), srcDocs, testSrcDocs);
    }

    public static DocumentConfig createDocumentData(DocumentFileData documentFileData, ModuleId moduleId) {
        final DocumentId documentId = DocumentId.create(documentFileData.filePath().toString(), moduleId);
        return DocumentConfig.from(documentId, documentFileData.filePath());
    }
}
