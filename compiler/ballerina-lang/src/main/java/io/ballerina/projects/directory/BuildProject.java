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

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.internal.PackageConfigCreator;
import io.ballerina.projects.internal.ProjectFiles;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;
import static io.ballerina.projects.util.ProjectConstants.DOT;
import static io.ballerina.projects.util.ProjectUtils.getDependenciesTomlContent;

/**
 * {@code BuildProject} represents Ballerina project instance created from the project directory.
 *
 * @since 2.0.0
 */
public class BuildProject extends Project {

    /**
     * Loads a BuildProject from the provided path.
     *
     * @param projectPath Ballerina project path
     * @return build project
     */
    public static BuildProject load(ProjectEnvironmentBuilder environmentBuilder, Path projectPath) {
        return load(environmentBuilder, projectPath, new BuildOptionsBuilder().build());
    }

    public static BuildProject load(ProjectEnvironmentBuilder environmentBuilder, Path projectPath,
                                    BuildOptions buildOptions) {
        PackageConfig packageConfig = PackageConfigCreator.createBuildProjectConfig(projectPath);
        BuildProject buildProject = new BuildProject(environmentBuilder, projectPath, buildOptions);
        buildProject.addPackage(packageConfig);
        return buildProject;
    }

    /**
     * Loads a BuildProject from the provided path.
     *
     * @param projectPath Ballerina project path
     * @return build project
     */
    public static BuildProject load(Path projectPath) {
        return load(projectPath, new BuildOptionsBuilder().build());
    }

    public static BuildProject load(Path projectPath, BuildOptions buildOptions) {
        // todo this is an ugly hack to get the offline build working we need to refactor this later
        System.setProperty(ProjectConstants.BALLERINA_OFFLINE_FLAG, String.valueOf(buildOptions.offlineBuild()));

        ProjectEnvironmentBuilder environmentBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        PackageConfig packageConfig = PackageConfigCreator.createBuildProjectConfig(projectPath);
        BuildOptions mergedBuildOptions = ProjectFiles.createBuildOptions(packageConfig, buildOptions, projectPath);
        BuildProject buildProject = new BuildProject(environmentBuilder, projectPath, mergedBuildOptions);
        buildProject.addPackage(packageConfig);
        return buildProject;
    }

    private BuildProject(ProjectEnvironmentBuilder environmentBuilder, Path projectPath, BuildOptions buildOptions) {
        super(ProjectKind.BUILD_PROJECT, projectPath, environmentBuilder, buildOptions);
        populateCompilerContext();
    }

    private Optional<Path> modulePath(ModuleId moduleId) {
        if (currentPackage().moduleIds().contains(moduleId)) {
            if (currentPackage().getDefaultModule().moduleId() == moduleId) {
                return Optional.of(sourceRoot);
            } else {
                return Optional.of(sourceRoot.resolve(ProjectConstants.MODULES_ROOT).resolve(
                        currentPackage().module(moduleId).moduleName().moduleNamePart()));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Path> documentPath(DocumentId documentId) {
        for (ModuleId moduleId : currentPackage().moduleIds()) {
            Module module = currentPackage().module(moduleId);
            Optional<Path> modulePath = modulePath(moduleId);
            if (module.documentIds().contains(documentId)) {
                if (modulePath.isPresent()) {
                    return Optional.of(modulePath.get().resolve(module.document(documentId).name()));
                }
            } else if (module.testDocumentIds().contains(documentId)) {
                if (modulePath.isPresent()) {
                    return Optional.of(modulePath.get()
                            .resolve(ProjectConstants.TEST_DIR_NAME).resolve(
                                    module.document(documentId).name().split(ProjectConstants.TEST_DIR_NAME + "/")[1]));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public DocumentId documentId(Path file) {
        if (isFilePathInProject(file)) {
            Path parent = Optional.of(file.toAbsolutePath().getParent()).get();
            for (ModuleId moduleId : this.currentPackage().moduleIds()) {
                String moduleDirName;
                // Check for the module name contains a dot and not being the default module
                if (moduleId.moduleName().contains(DOT)
                        && !this.currentPackage().getDefaultModule().moduleId().equals(moduleId)) {
                    moduleDirName = moduleId.moduleName()
                            .split(this.currentPackage().packageName().toString() + "\\.")[1];
                } else {
                    moduleDirName = Optional.of(this.sourceRoot.getFileName()).get().toString();
                }

                if (Optional.of(parent.getFileName()).get().toString().equals(moduleDirName) || Optional.of(
                        Optional.of(parent.getParent()).get().getFileName()).get().toString().equals(moduleDirName)) {
                    Module module = this.currentPackage().module(moduleId);
                    for (DocumentId documentId : module.documentIds()) {
                        if (module.document(documentId).name().equals(
                                Optional.of(file.getFileName()).get().toString())) {
                            return documentId;
                        }
                    }
                    for (DocumentId documentId : module.testDocumentIds()) {
                        if (module.document(documentId).name().split(ProjectConstants.TEST_DIR_NAME + "/")[1]
                                .equals(Optional.of(file.getFileName()).get().toString())) {
                            return documentId;
                        }
                    }
                }
            }
        }
        throw new ProjectException("provided path does not belong to the project");
    }

    private boolean isFilePathInProject(Path filepath) {
        try {
            ProjectPaths.packageRoot(filepath);
        } catch (ProjectException e) {
            return false;
        }
        return true;
    }

    public void save() {
        writeDependencies();
    }

    private void writeDependencies() {
        Package currentPackage = this.currentPackage();
        if (currentPackage != null) {
            ResolvedPackageDependency resolvedPackageDependency =
                    new ResolvedPackageDependency(currentPackage, PackageDependencyScope.DEFAULT);
            Collection<ResolvedPackageDependency> pkgDependencies =
                    currentPackage.getResolution().dependencyGraph().getDirectDependencies(resolvedPackageDependency);

            String dependenciesContent = getDependenciesTomlContent(pkgDependencies,
                                                                    currentPackage.manifest().dependencies());
            if (!dependenciesContent.isEmpty()) {
                // write content to Dependencies.toml file
                createIfNotExistsAndWrite(currentPackage.project().sourceRoot().resolve(DEPENDENCIES_TOML),
                                          dependenciesContent);
            }
        }
    }

    private static void createIfNotExistsAndWrite(Path filePath, String content) {
        if (!filePath.toFile().exists()) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                throw new ProjectException("Failed to create 'Dependencies.toml' file to write dependencies");
            }
        }

        try {
            Files.write(filePath, Collections.singleton(content));
        } catch (IOException e) {
            throw new ProjectException("Failed to write dependencies to the 'Dependencies.toml' file");
        }

    }
}
