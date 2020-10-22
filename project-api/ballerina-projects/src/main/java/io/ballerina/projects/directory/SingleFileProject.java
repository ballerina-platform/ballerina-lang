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

import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.env.BuildEnvContext;
import io.ballerina.projects.utils.ProjectConstants;
import io.ballerina.projects.utils.ProjectUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

/**
 * {@code SingleFileProject} represents a Ballerina standalone file.
 */
public class SingleFileProject extends Project {

    /**
     * Loads a single file project from the provided path.
     *
     * @param projectPath ballerina standalone file path
     * @return single file project
     */
    public static SingleFileProject loadProject(Path projectPath) {
        Path absProjectPath = Optional.of(projectPath.toAbsolutePath()).get();
        if (!absProjectPath.toFile().exists()) {
            throw new RuntimeException("project path does not exist:" + projectPath);
        }
        if (!isBallerinaStandaloneFile(absProjectPath)) {
            throw new RuntimeException("provided path is not a valid Ballerina standalone file: " + projectPath);
        }
        return new SingleFileProject(projectPath);
    }

    private SingleFileProject(Path projectPath) {
        super(BuildEnvContext.getInstance());

        this.sourceRoot = createTempProjectRoot(); // create a temp directory and assign to source root
        addPackage(projectPath);
        this.setBuildOptions(new BuildOptions()); // Set default build options
    }

    private Path createTempProjectRoot() {
        try {
            return Files.createTempDirectory("ballerina-project" + System.nanoTime());
        } catch (IOException e) {
            throw new RuntimeException("error while creating project root directory for single file execution. ", e);
        }
    }

    /**
     * Loads a package in the provided project path.
     *
     * @param projectPath project path
     */
    private void addPackage(Path projectPath) {
        PackageName packageName = PackageName.from(ProjectConstants.DOT);
        PackageOrg packageOrg = PackageOrg.from(ProjectConstants.ANON_ORG);
        PackageVersion packageVersion = PackageVersion.from(ProjectConstants.DEFAULT_VERSION);
        PackageDescriptor packageDescriptor = new PackageDescriptor(packageName,
                packageOrg, packageVersion, Collections.emptyList());
        PackageConfig packageConfig = PackageLoader.loadPackage(projectPath, true, packageDescriptor);
        this.addPackage(packageConfig);
    }

    /**
     * Checks if the path is a standalone file.
     *
     * @param file path to bal file
     * @return true if the file is a standalone bal file
     */
    private static boolean isBallerinaStandaloneFile(Path file) {
        // Check if the file is a regular file
        if (!Files.isRegularFile(file)) {
            return false;
        }
        // Check if it is a file with bal extention.
        if (!file.toString().endsWith(ProjectConstants.BLANG_SOURCE_EXT)) {
            return false;
        }
        // Check if it is inside a project
        Path projectRoot = ProjectUtils.findProjectRoot(file);
        if (null != projectRoot) {
            if (projectRoot.equals(file.getParent())) {
                return false;
            }
            // Check if it is inside a module
            Path modulesRoot = projectRoot.resolve(ProjectConstants.MODULES_ROOT);
            Path parent = file.getParent();
            while (parent != null) {
                if (modulesRoot.equals(parent)) {
                    return false;
                }
                parent = parent.getParent();
            }
        }
        return true;
    }

    /**
     * Returns build options of the project.
     *
     * @return build options
     */
    public BuildOptions getBuildOptions() {
        return (BuildOptions) super.getBuildOptions();
    }

    /**
     * {@code BuildOptions} represents the build options specific to a Ballerina standalone file.
     */
    public static class BuildOptions extends io.ballerina.projects.BuildOptions {

        private BuildOptions() {
            this.skipLock = true;
        }

    }
}
