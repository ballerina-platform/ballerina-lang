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

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DependencyManifest;
import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleConfig;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageId;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ResourceConfig;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.util.ProjectConstants;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Creates a {@code PackageConfig} instance from the given {@code PackageData} instance.
 *
 * @since 2.0.0
 */
public class PackageConfigCreator {

    public static PackageConfig createBuildProjectConfig(Path projectDirPath) {
        ProjectFiles.validateBuildProjectDirPath(projectDirPath);

        // TODO Create the PackageManifest from the BallerinaToml file
        // TODO Validate the ballerinaToml content inside the Ballerina toml file
        PackageData packageData = ProjectFiles.loadBuildProjectPackageData(projectDirPath);

        if (packageData.ballerinaToml().isEmpty()) {
            throw new ProjectException(ProjectConstants.BALLERINA_TOML + " not found");
        }

        TomlDocument ballerinaToml = packageData.ballerinaToml()
                .map(d -> TomlDocument.from(ProjectConstants.BALLERINA_TOML, d.content())).orElse(null);
        TomlDocument dependenciesToml = packageData.dependenciesToml()
                .map(d -> TomlDocument.from(ProjectConstants.DEPENDENCIES_TOML, d.content())).orElse(null);
        TomlDocument pluginToml = packageData.compilerPluginToml()
                .map(d -> TomlDocument.from(ProjectConstants.COMPILER_PLUGIN_TOML, d.content())).orElse(null);
        ManifestBuilder manifestBuilder = ManifestBuilder
                .from(ballerinaToml, pluginToml, projectDirPath);
        PackageManifest packageManifest = manifestBuilder.packageManifest();
        DependencyManifestBuilder dependencyManifestBuilder =
                DependencyManifestBuilder.from(dependenciesToml, packageManifest.descriptor());
        DependencyManifest dependencyManifest = dependencyManifestBuilder.dependencyManifest();

        return createPackageConfig(packageData, packageManifest, dependencyManifest);
    }

    public static PackageConfig createSingleFileProjectConfig(Path filePath) {
        ProjectFiles.validateSingleFileProjectFilePath(filePath);

        // Create a PackageManifest instance
        PackageDescriptor packageDesc = PackageDescriptor.from(PackageOrg.from(ProjectConstants.ANON_ORG),
                PackageName.from(ProjectConstants.DOT), PackageVersion.from(ProjectConstants.DEFAULT_VERSION));
        PackageManifest packageManifest = PackageManifest.from(packageDesc);
        DependencyManifest dependencyManifest = DependencyManifest.from(null, Collections.emptyList());

        PackageData packageData = ProjectFiles.loadSingleFileProjectPackageData(filePath);
        return createPackageConfig(packageData, packageManifest, dependencyManifest);
    }

    public static PackageConfig createBalaProjectConfig(Path balaPath) {
        ProjectFiles.validateBalaProjectPath(balaPath);
        PackageManifest packageManifest = BalaFiles.createPackageManifest(balaPath);
        DependencyManifest dependencyManifest = BalaFiles.createDependencyManifest(balaPath);
        PackageData packageData = BalaFiles.loadPackageData(balaPath, packageManifest);
        BalaFiles.DependencyGraphResult packageDependencyGraph = BalaFiles
                .createPackageDependencyGraph(balaPath);

        return createPackageConfig(packageData, packageManifest, dependencyManifest,
                packageDependencyGraph.packageDependencyGraph(), packageDependencyGraph.moduleDependencies());
    }

    public static PackageConfig createPackageConfig(PackageData packageData,
                                                    PackageManifest packageManifest,
                                                    DependencyManifest dependencyManifest) {
        return createPackageConfig(packageData, packageManifest, dependencyManifest, DependencyGraph.emptyGraph(),
                Collections.emptyMap());
    }

    public static PackageConfig createPackageConfig(PackageData packageData,
                                                    PackageManifest packageManifest,
                                                    DependencyManifest dependencyManifest,
                                                    DependencyGraph<PackageDescriptor> packageDependencyGraph,
                                                    Map<ModuleDescriptor, List<ModuleDescriptor>>
                                                            moduleDependencyGraph) {
        // TODO PackageData should contain the packageName. This should come from the Ballerina.toml file.
        // TODO For now, I take the directory name as the project name. I am not handling the case where the
        //  directory name is not a valid Ballerina identifier.

        // TODO: Should replace with Ballerina Toml parser generic model.

        PackageName packageName = packageManifest.name();
        PackageId packageId = PackageId.create(packageName.value());

        List<ModuleConfig> moduleConfigs = packageData.otherModules()
                .stream()
                .map(moduleData -> createModuleConfig(packageManifest.descriptor(), moduleData,
                        packageId, moduleDependencyGraph))
                .collect(Collectors.toList());

        moduleConfigs.add(createDefaultModuleConfig(packageManifest.descriptor(),
                packageData.defaultModule(), packageId, moduleDependencyGraph));

        DocumentConfig ballerinaToml = packageData.ballerinaToml()
                .map(data -> createDocumentConfig(data, null)).orElse(null);
        DocumentConfig dependenciesToml = packageData.dependenciesToml()
                .map(data -> createDocumentConfig(data, null)).orElse(null);
        DocumentConfig cloudToml = packageData.cloudToml()
                .map(data -> createDocumentConfig(data, null)).orElse(null);
        DocumentConfig compilerPluginToml = packageData.compilerPluginToml()
                .map(data -> createDocumentConfig(data, null)).orElse(null);
        DocumentConfig packageMd = packageData.packageMd()
                .map(data -> createDocumentConfig(data, null)).orElse(null);

        return PackageConfig
                .from(packageId, packageData.packagePath(), packageManifest, dependencyManifest, ballerinaToml,
                      dependenciesToml, cloudToml, compilerPluginToml, packageMd, moduleConfigs,
                      packageDependencyGraph);
    }

    private static ModuleConfig createDefaultModuleConfig(PackageDescriptor pkgDesc,
                                                          ModuleData moduleData,
                                                          PackageId packageId,
                                                          Map<ModuleDescriptor, List<ModuleDescriptor>>
                                                                  moduleDepGraph) {
        ModuleName moduleName = ModuleName.from(pkgDesc.name());
        ModuleDescriptor moduleDescriptor = createModuleDescriptor(pkgDesc, moduleName);
        List<ModuleDescriptor> dependencies = getModuleDependencies(moduleDepGraph, moduleDescriptor);
        return createModuleConfig(moduleDescriptor, moduleData, packageId, dependencies);
    }

    private static ModuleDescriptor createModuleDescriptor(PackageDescriptor pkgDesc, ModuleName moduleName) {
        return ModuleDescriptor.from(moduleName, pkgDesc);
    }

    private static ModuleConfig createModuleConfig(PackageDescriptor pkgDesc,
                                                   ModuleData moduleData,
                                                   PackageId packageId,
                                                   Map<ModuleDescriptor, List<ModuleDescriptor>> moduleDepGraph) {
        Path fileName = moduleData.moduleDirectoryPath().getFileName();
        if (fileName == null) {
            // TODO Proper error handling
            throw new IllegalStateException("This branch cannot be reached");
        }
        ModuleName moduleName = ModuleName.from(pkgDesc.name(), moduleData.moduleName());
        ModuleDescriptor moduleDescriptor = createModuleDescriptor(pkgDesc, moduleName);
        List<ModuleDescriptor> dependencies = getModuleDependencies(moduleDepGraph, moduleDescriptor);
        return createModuleConfig(moduleDescriptor, moduleData, packageId, dependencies);
    }

    private static ModuleConfig createModuleConfig(ModuleDescriptor moduleDescriptor,
                                                   ModuleData moduleData,
                                                   PackageId packageId,
                                                   List<ModuleDescriptor> dependencies) {
        ModuleId moduleId = ModuleId.create(moduleDescriptor.name().toString(), packageId);

        List<DocumentConfig> srcDocs = getDocumentConfigs(moduleId, moduleData.sourceDocs());
        List<DocumentConfig> testSrcDocs = getDocumentConfigs(moduleId, moduleData.testSourceDocs());

        DocumentConfig moduleMd = moduleData.moduleMd()
                .map(data -> createDocumentConfig(data, null)).orElse(null);

        List<ResourceConfig> resources = getResourceConfigs(
                moduleId, moduleData.resources(), moduleData.moduleDirectoryPath());
        List<ResourceConfig> testResources = getResourceConfigs(
                moduleId, moduleData.testResources(), moduleData.moduleDirectoryPath()
                        .resolve(ProjectConstants.TEST_DIR_NAME));
        return ModuleConfig.from(moduleId, moduleDescriptor, srcDocs, testSrcDocs, moduleMd, dependencies, resources,
                testResources);
    }

    private static List<ResourceConfig> getResourceConfigs(ModuleId moduleId, List<Path> resources, Path modulePath) {
        return resources.stream().map(resource ->
                createResourceConfig(resource, modulePath, moduleId)).collect(Collectors.toList());
    }

    private static ResourceConfig createResourceConfig(Path path, Path modulePath, ModuleId moduleId) {
        final DocumentId documentId = DocumentId.create(path.toString(), moduleId);
        return ProvidedResourceConfig.from(documentId, path, modulePath);
    }

    private static List<DocumentConfig> getDocumentConfigs(ModuleId moduleId, List<DocumentData> documentData) {
        return documentData
                .stream()
                .sorted(Comparator.comparing(DocumentData::name))
                .map(srcDoc -> createDocumentConfig(srcDoc, moduleId))
                .collect(Collectors.toList());
    }

    static DocumentConfig createDocumentConfig(DocumentData documentData, ModuleId moduleId) {
        final DocumentId documentId = DocumentId.create(documentData.name(), moduleId);
        return DocumentConfig.from(documentId, documentData.content(), documentData.name());
    }

    private static List<ModuleDescriptor> getModuleDependencies(Map<ModuleDescriptor, List<ModuleDescriptor>>
                                                                        moduleDepGraph,
                                                                ModuleDescriptor moduleDescriptor) {
        List<ModuleDescriptor> moduleDependencies = moduleDepGraph.get(moduleDescriptor);
        return Objects.requireNonNullElse(moduleDependencies, Collections.emptyList());
    }
}
