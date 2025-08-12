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

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.ProjectLoadResult;
import io.ballerina.projects.internal.PackageConfigCreator;
import io.ballerina.projects.repos.TempDirCompilationCache;
import io.ballerina.projects.util.ProjectConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * {@code SingleFileProject} represents a Ballerina standalone file.
 */
public class SingleFileProject extends Project implements Comparable<Project> {

    private Path targetDir;

    static ProjectLoadResult loadProject(Path projectPath, ProjectEnvironmentBuilder environmentBuilder,
                                         BuildOptions buildOptions) {
        PackageConfig packageConfig = PackageConfigCreator.createSingleFileProjectConfig(projectPath);
        SingleFileProject singleFileProject = new SingleFileProject(
                environmentBuilder, projectPath, buildOptions);
        singleFileProject.addPackage(packageConfig);
        return new ProjectLoadResult(singleFileProject, singleFileProject.currentPackage().manifest().diagnostics());
    }

    /**
     * @deprecated Use {@link io.ballerina.projects.directory.ProjectLoader#load(Path, ProjectEnvironmentBuilder)}
     * Loads a single file project from the provided path.
     *
     * @param filePath ballerina standalone file path
     * @return single file project
     */
    @Deprecated(forRemoval = true, since = "2201.0.0")
    public static SingleFileProject load(ProjectEnvironmentBuilder environmentBuilder, Path filePath) {
        final BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();
        return load(environmentBuilder, filePath, buildOptionsBuilder.build());
    }

    /**
     * @deprecated Use
     * {@link io.ballerina.projects.directory.ProjectLoader#load(Path, ProjectEnvironmentBuilder, BuildOptions)}
     * Loads a single file project from the provided path.
     *
     * @param environmentBuilder the project environment builder
     * @param filePath ballerina standalone file path
     * @param buildOptions build options
     * @return single file project
     */
    @Deprecated(forRemoval = true, since = "2201.0.0")
    public static SingleFileProject load(ProjectEnvironmentBuilder environmentBuilder, Path filePath,
                                         BuildOptions buildOptions) {
        PackageConfig packageConfig = PackageConfigCreator.createSingleFileProjectConfig(filePath);
        SingleFileProject singleFileProject = new SingleFileProject(
                environmentBuilder, filePath, buildOptions);
        singleFileProject.addPackage(packageConfig);
        return singleFileProject;
    }

    /**
     * @deprecated Use {@link io.ballerina.projects.directory.ProjectLoader#load(Path)}
     * Loads a single file project from the provided path.
     *
     * @param filePath ballerina standalone file path
     * @return single file project
     */
    @Deprecated(forRemoval = true, since = "2201.0.0")
    public static SingleFileProject load(Path filePath) {
        return load(filePath, BuildOptions.builder().build());
    }

    /**
     * @deprecated Use {@link io.ballerina.projects.directory.ProjectLoader#load(Path, BuildOptions)}
     * Loads a single file project from the provided path with build options.
     *
     * @param filePath ballerina standalone file path
     * @param buildOptions build options
     * @return single file project
     */
    @Deprecated(forRemoval = true, since = "2201.0.0")
    public static SingleFileProject load(Path filePath, BuildOptions buildOptions) {
        PackageConfig packageConfig = PackageConfigCreator.createSingleFileProjectConfig(filePath,
                buildOptions.disableSyntaxTree());
        ProjectEnvironmentBuilder environmentBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        SingleFileProject singleFileProject = new SingleFileProject(environmentBuilder, filePath, buildOptions);
        singleFileProject.addPackage(packageConfig);
        return singleFileProject;
    }

    private SingleFileProject(ProjectEnvironmentBuilder environmentBuilder, Path filePath, BuildOptions buildOptions) {
        super(ProjectKind.SINGLE_FILE_PROJECT, filePath, environmentBuilder, buildOptions, null);

        try {
            this.targetDir = Files.createTempDirectory("ballerina-cache" + System.nanoTime());
        } catch (IOException e) {
            // ignore
        }

        populateCompilerContext();
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
        BuildOptions duplicateBuildOptions = BuildOptions.builder().build().acceptTheirs(buildOptions());
        SingleFileProject singleFileProject = new SingleFileProject(
                ProjectEnvironmentBuilder.getDefaultBuilder(), this.sourceRoot, duplicateBuildOptions);
        return resetPackage(singleFileProject);
    }

    @Override
    public DocumentId documentId(Path file) {
        if (!this.sourceRoot.toAbsolutePath().normalize().toString().equals(
                file.toAbsolutePath().normalize().toString())) {
            throw new ProjectException("'" + file + "' does not belong to the current project");
        }
        return this.currentPackage().getDefaultModule().documentIds().iterator().next();
    }

    @Override
    public Optional<Path> documentPath(DocumentId documentId) {
        if (this.currentPackage().getDefaultModule().documentIds().iterator().next().equals(documentId)) {
            return Optional.of(sourceRoot.toAbsolutePath());
        }
        return Optional.empty();
    }

    @Override
    public void save() {
    }

    @Override
    public Path targetDir() {
        return this.targetDir;
    }

    @Override
    public Path generatedResourcesDir() {
        Path generatedResourcesPath = this.targetDir.resolve(ProjectConstants.RESOURCE_DIR_NAME);
        if (!Files.exists(generatedResourcesPath)) {
            try {
                Files.createDirectories(generatedResourcesPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return generatedResourcesPath;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SingleFileProject other)) {
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
