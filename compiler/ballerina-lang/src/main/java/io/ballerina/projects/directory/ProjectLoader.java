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
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.bala.BalaProject;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.repos.TempDirCompilationCache;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Contains a set of utility methods to create a project.
 *
 * @since 2.0.0
 */
public final class ProjectLoader {

    private ProjectLoader() {
    }

    public static Project loadProject(Path path) {
        return loadProject(path, ProjectEnvironmentBuilder.getDefaultBuilder(), BuildOptions.builder().build());
    }

    public static Project loadProject(Path path, BuildOptions buildOptions) {
        return loadProject(path, ProjectEnvironmentBuilder.getDefaultBuilder(), buildOptions);
    }

    public static Project loadProject(Path path, ProjectEnvironmentBuilder projectEnvironmentBuilder) {
        return loadProject(path, projectEnvironmentBuilder, BuildOptions.builder().build());
    }

    public static Project loadProject(Path path, EnvironmentBuilder environmentBuilder, BuildOptions buildOptions) {
        ProjectEnvironmentBuilder projectEnvironmentBuilder =
                ProjectEnvironmentBuilder.getBuilder(environmentBuilder.build());
        return loadProject(path, projectEnvironmentBuilder, buildOptions);
    }

    /**
     * Returns a project by deriving the type from the path provided.
     *
     * @param path path of a .bal file or a .bala file
     * @return Project instance
     * @throws ProjectException if an invalid path is provided
     */
    public static Project loadProject(Path path, ProjectEnvironmentBuilder projectEnvironmentBuilder,
                                      BuildOptions buildOptions) throws ProjectException {
        if (ProjectPaths.workspaceRoot(path).isPresent()) {
            // If the path is in a workspace, load the workspace project
            return WorkspaceProject.load(path, buildOptions);
        }

        Path projectRoot = getProjectRoot(Optional.of(path.toAbsolutePath().normalize()).get());

        boolean isPackageRoot = ProjectPaths.isBuildProjectRoot(projectRoot);
        if (isPackageRoot) {
            Path parent = projectRoot.getParent();
            if (parent != null && ProjectPaths.isBuildProjectRoot(parent)) {
                return WorkspaceProject.load(projectRoot, buildOptions);
            }
        }
        if (isPackageRoot) {
            return BuildProject.load(projectEnvironmentBuilder, projectRoot, buildOptions);
        } else if (ProjectPaths.isBalaProjectRoot(projectRoot)) {
            projectEnvironmentBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
            return BalaProject.loadProject(projectEnvironmentBuilder, projectRoot, buildOptions);
        }

        return SingleFileProject.load(projectEnvironmentBuilder, path, buildOptions);
    }

    private static Path getProjectRoot(Path filePath) {
        Path projectRoot;
        if (!Files.exists(filePath)) {
            throw new ProjectException("provided file path does not exist");
        }
        if (filePath.toFile().isDirectory()) {
            if (ProjectConstants.MODULES_ROOT.equals(
                    Optional.of(filePath.getParent()).get().toFile().getName())) {
                projectRoot = Optional.of(Optional.of(filePath.getParent()).get().getParent()).get();
            } else if (ProjectConstants.GENERATED_MODULES_ROOT.equals(filePath.toFile().getName())) {
                // Generated default module
                projectRoot = Optional.of(filePath.getParent()).get();
            } else if (ProjectConstants.GENERATED_MODULES_ROOT.
                    equals(Optional.of(filePath.getParent()).get().toFile().getName())) {
                // Generated non default module
                projectRoot = Optional.of(Optional.of(filePath.getParent()).get().getParent()).get();
            } else {
                projectRoot = filePath;
            }
        } else if (ProjectPaths.isBalFile(filePath)) {
            projectRoot = filePath;
        } else {
            throw new ProjectException("'" + filePath + "' is not a valid Ballerina source file");
        }
        return projectRoot;
    }

}
