/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.directory;

import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleConfig;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageId;
import io.ballerina.projects.PackageName;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains a set of utility methods that creates the config hierarchy from the project directory.
 *
 * @since 2.0.0
 */
public class PackageLoader {

    public static PackageConfig loadPackage(Path packageDir,
                                            boolean isSingleFile,
                                            PackageDescriptor packageDescriptor) {
        final PackageData packageData = ProjectFiles.loadPackageData(packageDir, isSingleFile);
        return createPackageConfig(packageData, packageDescriptor);
    }

    protected static PackageConfig createPackageConfig(PackageData packageData,
                                                       PackageDescriptor packageDescriptor) {
        // TODO PackageData should contain the packageName. This should come from the Ballerina.toml file.
        // TODO For now, I take the directory name as the project name. I am not handling the case where the
        //  directory name is not a valid Ballerina identifier.

        // TODO: Should replace with Ballerina Toml parser generic model.

        PackageName packageName = packageDescriptor.name();
        PackageId packageId = PackageId.create(packageDescriptor.name().value());

        List<ModuleConfig> moduleConfigs = packageData.otherModules()
                .stream()
                .map(moduleData -> createModuleConfig(packageName, moduleData, packageId))
                .collect(Collectors.toList());
        ModuleConfig defaultModuleConfig = createDefaultModuleData(packageName,
                packageData.defaultModule(), packageId);
        moduleConfigs.add(defaultModuleConfig);
        return PackageConfig.from(packageId, packageData.packagePath(), packageDescriptor, moduleConfigs);
    }

    private static ModuleConfig createDefaultModuleData(PackageName packageName,
                                                        ModuleData moduleData,
                                                        PackageId packageId) {
        ModuleName moduleName = ModuleName.from(packageName);
        return createModuleConfig(moduleName, moduleData, packageId);
    }

    private static ModuleConfig createModuleConfig(PackageName packageName,
                                                   ModuleData moduleData,
                                                   PackageId packageId) {
        Path fileName = moduleData.moduleDirectoryPath().getFileName();
        if (fileName == null) {
            // TODO Proper error handling
            throw new IllegalStateException("This branch cannot be reached");
        }
        ModuleName moduleName = ModuleName.from(packageName, fileName.toString());
        return createModuleConfig(moduleName, moduleData, packageId);
    }

    private static ModuleConfig createModuleConfig(ModuleName moduleName,
                                                   ModuleData moduleData,
                                                   PackageId packageId) {
        Path moduleDirPath = moduleData.moduleDirectoryPath();
        ModuleId moduleId = ModuleId.create(moduleName.toString(), packageId);

        List<DocumentConfig> srcDocs = getDocumentConfigs(moduleId, moduleData.sourceDocs());
        List<DocumentConfig> testSrcDocs = getDocumentConfigs(moduleId, moduleData.testSourceDocs());
        final ModuleConfig moduleConfig = ModuleConfig.from(moduleId, moduleName, moduleDirPath, srcDocs, testSrcDocs);
        moduleConfig.birBytes = moduleData.birBytes();
        return moduleConfig;
    }

    private static List<DocumentConfig> getDocumentConfigs(ModuleId moduleId, List<DocumentData> documentData) {
        return documentData
                .stream()
                .map(srcDoc -> createDocumentConfig(srcDoc, moduleId))
                .collect(Collectors.toList());
    }

    static DocumentConfig createDocumentConfig(DocumentData documentData, ModuleId moduleId) {
        final DocumentId documentId = DocumentId.create(documentData.name(), moduleId);
        return DocumentConfig.from(documentId, documentData.content(), documentData.name());
    }
}
