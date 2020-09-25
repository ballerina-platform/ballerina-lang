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
import io.ballerina.projects.PackageId;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.model.BallerinaToml;
import io.ballerina.projects.model.BallerinaTomlProcessor;
import org.ballerinalang.toml.exceptions.TomlException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.projects.utils.ProjectConstants.BALLERINA_TOML;

/**
 * Contains a set of utility methods that creates the config hierarchy from the project directory.
 *
 * @since 2.0.0
 */
public class PackageLoader {

    public static PackageConfig loadPackage(String packageDir, boolean isSingleFile) {
        final PackageData packageData = ProjectFiles.loadPackageData(packageDir, isSingleFile);
        return createPackageConfig(packageData);
    }

    protected static PackageConfig createPackageConfig(PackageData packageData) {
        // TODO PackageData should contain the packageName. This should come from the Ballerina.toml file.
        // TODO For now, I take the directory name as the project name. I am not handling the case where the
        //  directory name is not a valid Ballerina identifier.
        Path packagePath = packageData.packagePath();
        Path fileName = packagePath.getFileName();
        if (fileName == null) {
            // TODO Proper error handling
            throw new IllegalStateException("This branch cannot be reached");
        }

        // load Ballerina.toml
        BallerinaToml ballerinaToml;
        try {
            ballerinaToml = BallerinaTomlProcessor.parse(packagePath.resolve(BALLERINA_TOML));
        } catch (IOException | TomlException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        PackageName packageName = PackageName.from(fileName.toString());
        PackageOrg packageOrg = PackageOrg.from(ballerinaToml.getPackage().getOrg());
        PackageVersion packageVersion = PackageVersion.from(ballerinaToml.getPackage().getVersion());
        PackageId packageId = PackageId.create(packageName.toString());

        List<ModuleConfig> moduleConfigs = packageData.otherModules()
                .stream()
                .map(moduleData -> createModuleConfig(packageName, moduleData, packageId))
                .collect(Collectors.toList());
        ModuleConfig defaultModuleConfig = createDefaultModuleData(packageName,
                packageData.defaultModule(), packageId);
        moduleConfigs.add(defaultModuleConfig);
        return PackageConfig.from(packageId, packageName, packageOrg, packageVersion, packagePath, moduleConfigs);
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
        return ModuleConfig.from(moduleId, moduleName, moduleDirPath, srcDocs, testSrcDocs);
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
