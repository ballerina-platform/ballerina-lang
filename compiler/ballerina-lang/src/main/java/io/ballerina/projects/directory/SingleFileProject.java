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
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.internal.PackageConfigCreator;

import java.nio.file.Path;
import java.util.Optional;

/**
 * {@code SingleFileProject} represents a Ballerina standalone file.
 */
public class SingleFileProject extends Project {

    /**
     * Loads a single file project from the provided path.
     *
     * @param filePath ballerina standalone file path
     * @return single file project
     */
    public static SingleFileProject load(ProjectEnvironmentBuilder environmentBuilder, Path filePath) {
        final BuildOptionsBuilder buildOptionsBuilder = new BuildOptionsBuilder();
        return load(environmentBuilder, filePath, buildOptionsBuilder.build());
    }

    public static SingleFileProject load(ProjectEnvironmentBuilder environmentBuilder, Path filePath,
                                          BuildOptions buildOptions) {
        PackageConfig packageConfig = PackageConfigCreator.createSingleFileProjectConfig(filePath);
        SingleFileProject singleFileProject = new SingleFileProject(
                environmentBuilder, filePath, buildOptions);
        singleFileProject.addPackage(packageConfig);
        return singleFileProject;
    }

    public static SingleFileProject load(Path filePath) {
        return load(filePath, new BuildOptionsBuilder().build());
    }

    public static SingleFileProject load(Path filePath, BuildOptions buildOptions) {
        PackageConfig packageConfig = PackageConfigCreator.createSingleFileProjectConfig(filePath);
        ProjectEnvironmentBuilder environmentBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        SingleFileProject singleFileProject = new SingleFileProject(environmentBuilder, filePath, buildOptions);
        singleFileProject.addPackage(packageConfig);
        return singleFileProject;
    }

    private SingleFileProject(ProjectEnvironmentBuilder environmentBuilder, Path filePath, BuildOptions buildOptions) {
        super(ProjectKind.SINGLE_FILE_PROJECT, filePath, environmentBuilder, buildOptions);
        populateCompilerContext();
    }

    @Override
    public Project duplicate() {
        BuildOptions duplicateBuildOptions = new BuildOptionsBuilder().build().acceptTheirs(buildOptions());
        SingleFileProject singleFileProject = new SingleFileProject(
                ProjectEnvironmentBuilder.getDefaultBuilder(), this.sourceRoot, duplicateBuildOptions);
        return cloneProject(singleFileProject);
    }

    @Override
    public DocumentId documentId(Path file) {
        if (!this.sourceRoot.toAbsolutePath().normalize().toString().equals(
                file.toAbsolutePath().normalize().toString())) {
            throw new ProjectException("provided path does not belong to the project");
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
}
