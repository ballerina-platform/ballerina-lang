/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semver.checker.util;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectPaths;
import io.ballerina.semver.checker.exception.SemverToolException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Map;

/**
 * Package (Project API) related utilities.
 *
 * @since 2201.2.0
 */
public class PackageUtils {

    public static final String QUALIFIER_PUBLIC = "public";

    /**
     * Loads the target ballerina source package instance using the Project API, from the file path of the open/active
     * editor instance in the client(plugin) side.
     *
     * @param filePath file path of the open/active editor instance in the plugin side.
     */
    public static Package loadPackage(Path filePath) throws SemverToolException {
        try {
            Project project = PackageUtils.loadProject(filePath);
            switch (project.kind()) {
                case BUILD_PROJECT:
                case BALA_PROJECT:
                    return project.currentPackage();
                case SINGLE_FILE_PROJECT:
                    throw new SemverToolException("semver checker tool is not applicable for single file projects.");
                default:
                    throw new SemverToolException("semver checker tool is not applicable for " + project.kind().name());
            }
        } catch (ProjectException e) {
            throw new SemverToolException(String.format("failed to load Ballerina package at: '%s'%sreason: %s",
                    filePath.toAbsolutePath(), System.lineSeparator(), e.getMessage()));
        }
    }

    /**
     * Loads the target ballerina source project instance using the Project API, from the file path of the open/active
     * editor instance in the client(plugin) side.
     *
     * @param filePath file path of the open/active editor instance in the plugin side.
     */
    private static Project loadProject(Path filePath) {
        Map.Entry<ProjectKind, Path> projectKindAndProjectRootPair = computeProjectKindAndRoot(filePath);
        ProjectKind projectKind = projectKindAndProjectRootPair.getKey();
        Path projectRoot = projectKindAndProjectRootPair.getValue();
        BuildOptions options = constructDefaultBuildOptions();
        if (projectKind == ProjectKind.BUILD_PROJECT) {
            return BuildProject.load(projectRoot, options);
        } else if (projectKind == ProjectKind.SINGLE_FILE_PROJECT) {
            return SingleFileProject.load(projectRoot, options);
        } else {
            return ProjectLoader.loadProject(projectRoot, options);
        }
    }

    /**
     * Computes the source root and the shape(kind) of the enclosing Ballerina project, using the given file path.
     *
     * @param path file path
     * @return A pair of project kind and the project root.
     */
    public static Map.Entry<ProjectKind, Path> computeProjectKindAndRoot(Path path) {
        if (ProjectPaths.isStandaloneBalFile(path)) {
            return new AbstractMap.SimpleEntry<>(ProjectKind.SINGLE_FILE_PROJECT, path);
        }
        // Following is a temp fix to distinguish Bala and Build projects.
        Path tomlPath = ProjectPaths.packageRoot(path).resolve(ProjectConstants.BALLERINA_TOML);
        if (Files.exists(tomlPath)) {
            return new AbstractMap.SimpleEntry<>(ProjectKind.BUILD_PROJECT, ProjectPaths.packageRoot(path));
        }
        return new AbstractMap.SimpleEntry<>(ProjectKind.BALA_PROJECT, ProjectPaths.packageRoot(path));
    }

    private static BuildOptions constructDefaultBuildOptions() {
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();
        buildOptionsBuilder
                .setCodeCoverage(false)
                .setExperimental(false)
                .setOffline(false)
                .setSkipTests(true)
                .setTestReport(false)
                .setObservabilityIncluded(false)
                .setSticky(false)
                .setDumpGraph(false)
                .setDumpRawGraphs(false)
                .setConfigSchemaGen(false);

        return buildOptionsBuilder.build();
    }
}
