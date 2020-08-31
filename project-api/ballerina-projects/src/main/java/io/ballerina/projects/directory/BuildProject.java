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

import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.Project;
import io.ballerina.projects.utils.ProjectUtils;

import java.nio.file.Path;
import java.util.Optional;

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
    public static BuildProject loadProject(Path projectPath) {
        Path absProjectPath = Optional.of(projectPath.toAbsolutePath()).get();
        if (!absProjectPath.toFile().exists()) {
            throw new RuntimeException("project path does not exist:" + projectPath);
        }

        if (!ProjectUtils.isBallerinaProject(absProjectPath)) {
            throw new RuntimeException("provided path is not a valid Ballerina project: " + projectPath);
        }

        if (ProjectUtils.findProjectRoot(Optional.of(absProjectPath.getParent()).get()) != null) {
            throw new RuntimeException("provided path is already within a Ballerina project: " +
                    absProjectPath.getParent());
        }
        return new BuildProject(absProjectPath);
    }

    private BuildProject(Path projectPath) {
        super();
        this.sourceRoot = projectPath;
        addPackage(projectPath.toString());

        // Set default build options
        if (this.context.currentPackage().ballerinaToml().getBuildOptions() != null) {
            this.context.setBuildOptions(this.context.currentPackage().ballerinaToml().getBuildOptions());
        } else {
            this.context.setBuildOptions(new BuildOptions());
        }
    }

    /**
     * Loads a package in the provided project path.
     *
     * @param projectPath project path
     */
    private void addPackage(String projectPath) {
        final PackageConfig packageConfig = PackageLoader.loadPackage(projectPath, false);
        this.context.addPackage(packageConfig);
    }

    public BuildOptions getBuildOptions() {
        return (BuildOptions) this.context.getBuildOptions();
    }

    /**
     * {@code BuildOptions} represents build options specific to a build project.
     */
    public static class BuildOptions extends io.ballerina.projects.BuildOptions {

        private BuildOptions() {}

        public void setObservabilityEnabled(boolean observabilityEnabled) {
            observabilityIncluded = observabilityEnabled;
        }

        public void setSkipLock(boolean skipLock) {
            this.skipLock = skipLock;
        }

        public void setCodeCoverage(boolean codeCoverage) {
            this.codeCoverage = codeCoverage;
        }
    }
}
