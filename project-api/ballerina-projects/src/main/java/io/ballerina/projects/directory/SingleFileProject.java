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

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.Project;
import io.ballerina.projects.utils.ProjectConstants;
import org.ballerinalang.toml.model.Manifest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SingleFileProject extends Project {

    public static SingleFileProject loadProject(Path projectPath) {
        return new SingleFileProject(projectPath);
    }

    private SingleFileProject(Path projectPath) {
        super();
        packagePath = projectPath.toString();
        ballerinaToml = new Manifest();
        Path sourceRoot = createTempProjectRoot();
        this.context.setTargetPath(ProjectFiles.createTargetDirectoryStructure(sourceRoot));

        // Set default build options
        this.context.setBuildOptions(new BuildOptions());
    }

    private Path createTempProjectRoot() {
        try {
            return Files.createTempDirectory("ballerina-project" + System.nanoTime());
        } catch (IOException e) {
            throw new RuntimeException("error while creating project root directory for single file execution. ", e);
        }
    }

    public Package getPackage() {
        final PackageConfig packageConfig = PackageLoader.loadPackage(packagePath, true);
        this.context.addPackage(packageConfig);
        return this.context.currentPackage();
    }

    public BuildOptions getBuildOptions() {
        return (BuildOptions) this.context.getBuildOptions();
    }
    public void setBuildOptions(BuildOptions newBuildOptions) {
        BuildOptions buildOptions = (BuildOptions) this.context.getBuildOptions();
        buildOptions.setB7aConfigFile(newBuildOptions.getB7aConfigFile());
        buildOptions.setSourceRoot(newBuildOptions.getSourceRoot());
        buildOptions.setOutput(newBuildOptions.getOutput());
        this.context.setBuildOptions(newBuildOptions);
    }

    public static class BuildOptions extends io.ballerina.projects.BuildOptions {

        private BuildOptions() {
            this.sourceRoot = System.getProperty(ProjectConstants.USER_DIR);
            this.output = System.getProperty(ProjectConstants.USER_DIR);
            this.skipLock = true;
            this.codeCoverage = false;
            this.observabilityIncluded = false;
//            this.b7aConfigFile = ballerinaToml.getBuildOptions().getB7aConfig();
        }

        public void setOutput(String output) {
            this.output = output;
        }

        public String getOutput() {
            return output;
        }

    }
}
