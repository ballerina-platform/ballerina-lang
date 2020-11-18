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
package io.ballerina.projects.internal;

import io.ballerina.projects.BallerinaToml;
import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleConfig;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageId;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.util.ProjectConstants;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates a {@code PackageConfig} instance from the given {@code PackageData} instance.
 *
 * @since 2.0.0
 */
public class PackageConfigCreator {

    public static PackageConfig createBuildProjectConfig(Path projectDirPath) {
        ProjectFiles.validateBuildProjectDirPath(projectDirPath);

        Path balTomlFilePath = projectDirPath.resolve(ProjectConstants.BALLERINA_TOML);
        BallerinaToml ballerinaToml = BallerinaToml.from(balTomlFilePath);
        // TODO Create the PackageDescriptor from the BallerinaToml file
        // TODO Validate the ballerinaToml content inside the Ballerina toml file
        PackageDescriptor packageDescriptor = ProjectFiles.createPackageDescriptor(
                balTomlFilePath);
        PackageData packageData = ProjectFiles.loadBuildProjectPackageData(projectDirPath);
        return createPackageConfig(packageData, packageDescriptor, ballerinaToml);
    }

    public static PackageConfig createSingleFileProjectConfig(Path filePath) {
        ProjectFiles.validateSingleFileProjectFilePath(filePath);

        // Create a PackageDescriptor instance
        PackageName packageName = PackageName.from(ProjectConstants.DOT);
        PackageOrg packageOrg = PackageOrg.from(ProjectConstants.ANON_ORG);
        PackageVersion packageVersion = PackageVersion.from(ProjectConstants.DEFAULT_VERSION);
        PackageDescriptor packageDescriptor = PackageDescriptor.from(packageName, packageOrg, packageVersion);

        PackageData packageData = ProjectFiles.loadSingleFileProjectPackageData(filePath);
        return createPackageConfig(packageData, packageDescriptor);
    }

    public static PackageConfig createBalrProjectConfig(Path balrPath) {
        ProjectFiles.validateBalrProjectPath(balrPath);
        PackageDescriptor packageDescriptor = BaloFiles.createPackageDescriptor(balrPath);

        PackageData packageData = BaloFiles.loadPackageData(balrPath, packageDescriptor);
        return createPackageConfig(packageData, packageDescriptor);
    }

    public static PackageConfig createPackageConfig(PackageData packageData, PackageDescriptor packageDescriptor) {
        return createPackageConfig(packageData, packageDescriptor, null);
    }

    public static PackageConfig createPackageConfig(PackageData packageData,
                                                    PackageDescriptor packageDescriptor,
                                                    BallerinaToml ballerinaToml) {
        // TODO PackageData should contain the packageName. This should come from the Ballerina.toml file.
        // TODO For now, I take the directory name as the project name. I am not handling the case where the
        //  directory name is not a valid Ballerina identifier.

        // TODO: Should replace with Ballerina Toml parser generic model.

        PackageName packageName = packageDescriptor.name();
        PackageId packageId = PackageId.create(packageName.value());

        List<ModuleConfig> moduleConfigs = packageData.otherModules()
                .stream()
                .map(moduleData -> createModuleConfig(packageDescriptor, moduleData, packageId))
                .collect(Collectors.toList());
        moduleConfigs.add(createDefaultModuleConfig(packageDescriptor, packageData.defaultModule(), packageId));
        return PackageConfig.from(packageId, packageData.packagePath(),
                packageDescriptor, ballerinaToml, moduleConfigs);
    }

    private static ModuleConfig createDefaultModuleConfig(PackageDescriptor pkgDescriptor,
                                                          ModuleData moduleData,
                                                          PackageId packageId) {
        ModuleName moduleName = ModuleName.from(pkgDescriptor.name());
        return createModuleConfig(createModuleDescriptor(pkgDescriptor, moduleName), moduleData, packageId);
    }

    private static ModuleDescriptor createModuleDescriptor(PackageDescriptor pkgDesc, ModuleName moduleName) {
        return ModuleDescriptor.from(pkgDesc.name(), pkgDesc.org(), pkgDesc.version(), moduleName);
    }

    private static ModuleConfig createModuleConfig(PackageDescriptor pkgDescriptor,
                                                   ModuleData moduleData,
                                                   PackageId packageId) {
        Path fileName = moduleData.moduleDirectoryPath().getFileName();
        if (fileName == null) {
            // TODO Proper error handling
            throw new IllegalStateException("This branch cannot be reached");
        }
        ModuleName moduleName = ModuleName.from(pkgDescriptor.name(), moduleData.moduleName());
        return createModuleConfig(createModuleDescriptor(pkgDescriptor, moduleName), moduleData, packageId);
    }

    private static ModuleConfig createModuleConfig(ModuleDescriptor moduleDescriptor,
                                                   ModuleData moduleData,
                                                   PackageId packageId) {
        ModuleId moduleId = ModuleId.create(moduleDescriptor.name().toString(), packageId);

        List<DocumentConfig> srcDocs = getDocumentConfigs(moduleId, moduleData.sourceDocs());
        List<DocumentConfig> testSrcDocs = getDocumentConfigs(moduleId, moduleData.testSourceDocs());
        return ModuleConfig.from(moduleId, moduleDescriptor, srcDocs, testSrcDocs);
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
