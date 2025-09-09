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
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.ProjectLoadResult;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.internal.PackageConfigCreator;
import io.ballerina.projects.repos.TempDirCompilationCache;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * {@code BalaProject} represents a Ballerina project instance created from a bala.
 *
 * @since 2.0.0
 */
public class BalaProject extends Project implements Comparable<Project> {
    private final String platform;
    private final String balaVersion;

    static ProjectLoadResult load(Path projectPath, ProjectEnvironmentBuilder environmentBuilder,
                                  BuildOptions buildOptions) {
        PackageConfig packageConfig = PackageConfigCreator.createBalaProjectConfig(projectPath);
        BalaProject balaProject = new BalaProject(environmentBuilder, projectPath, buildOptions);
        balaProject.addPackage(packageConfig);
        return new ProjectLoadResult(balaProject, balaProject.currentPackage().manifest().diagnostics());
    }

    private BalaProject(ProjectEnvironmentBuilder environmentBuilder, Path balaPath, BuildOptions buildOptions) {
        super(ProjectKind.BALA_PROJECT, balaPath, environmentBuilder, buildOptions, null);
        this.platform = BalaFiles.readPackageJson(balaPath).getPlatform();
        this.balaVersion = BalaFiles.readBalaJson(balaPath).getBala_version();
    }

    @Override
    public void clearCaches() {
        resetPackage(this);
        ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        projectEnvironmentBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
        this.projectEnvironment = projectEnvironmentBuilder.build(this);
    }

    @Override
    public Project duplicate() {
        ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        projectEnvironmentBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
        BuildOptions duplicateBuildOptions = BuildOptions.builder().build().acceptTheirs(buildOptions());
        BalaProject balaProject = new BalaProject(projectEnvironmentBuilder, this.sourceRoot, duplicateBuildOptions);
        return resetPackage(balaProject);
    }

    @Override
    public DocumentId documentId(Path file) {
        if (file == null) {
            throw new ProjectException("file path cannot be null");
        }
        if (!Files.isDirectory(this.sourceRoot)) {
            throw new UnsupportedOperationException("retrieving the DocumentId from " +
                    ProjectConstants.BLANG_COMPILED_PKG_BINARY_EXT + " is not supported");
        }
        if (isFilePathInProject(file)) {
            Path modulesRoot = this.sourceRoot.resolve(ProjectConstants.MODULES_ROOT);
            for (ModuleId moduleId : this.currentPackage().moduleIds()) {
                Module module = this.currentPackage().module(moduleId);
                Path modulePath = modulesRoot.resolve(module.moduleName().toString());
                for (DocumentId documentId : module.documentIds()) {
                    Document document = module.document(documentId);
                    if (modulePath.resolve(document.name()).toAbsolutePath().toString().equals(
                            file.toAbsolutePath().toString())) {
                        return documentId;
                    }
                }
            }
        }
        throw new ProjectException("provided path '" + file.toAbsolutePath()
                + "' does not belong to the bala: " + this.sourceRoot);
    }

    @Override
    public Optional<Path> documentPath(DocumentId documentId) {
        for (ModuleId moduleId : currentPackage().moduleIds()) {
            Module module = currentPackage().module(moduleId);
            Path modulePath = sourceRoot.resolve(ProjectConstants.MODULES_ROOT).resolve(module.moduleName().toString());
            if (module.documentIds().contains(documentId)) {
                return Optional.of(modulePath.resolve(module.document(documentId).name()));
            }
        }
        return Optional.empty();
    }

    @Override
    public void save() {
    }

    public String platform() {
        return platform;
    }

    public String balaVersion() {
        return balaVersion;
    }

    private boolean isFilePathInProject(Path filepath) {
        try {
            ProjectPaths.packageRoot(filepath);
        } catch (ProjectException e) {
            return false;
        }
        return true;
    }

    @Override
    public Path targetDir() {
        throw new UnsupportedOperationException("target directory is not supported for BalaProject");
    }

    @Override
    public Path generatedResourcesDir() {
        throw new UnsupportedOperationException("generated resources directory is not supported for BalaProject");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BalaProject other)) {
            return false;
        }

        return this.sourceRoot.equals(other.sourceRoot());
    }

    @Override
    public int hashCode() {
        return sourceRoot.hashCode();
    }


    @Override
    public int compareTo(Project other) {
        return this.sourceRoot.compareTo(other.sourceRoot());
    }
}
