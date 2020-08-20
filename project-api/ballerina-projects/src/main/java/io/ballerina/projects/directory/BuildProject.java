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

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.Project;
import io.ballerina.projects.model.BallerinaToml;
import io.ballerina.projects.utils.ProjectConstants;
import io.ballerina.projects.utils.RepoUtils;
import org.ballerinalang.toml.model.LockFile;

import java.nio.file.Path;

/**
 * {@code BuildProject} represents Ballerina project instance created from the project directory.
 *
 * @since 2.0.0
 */
public class BuildProject extends Project {
     private LockFile lockFile; // related to build command?

    public static BuildProject loadProject(Path projectPath) throws Exception {
        return new BuildProject(projectPath);
    }

    private BuildProject(Path projectPath) throws Exception {
        super();
        if (!RepoUtils.isBallerinaProject(projectPath)) {
            throw new Exception("invalid Ballerina source path:" + projectPath);
        }
        packagePath = projectPath.toString();
        this.context.setTargetPath(ProjectFiles.createTargetDirectoryStructure(projectPath));

        // Set default build options
        this.context.setBuildOptions(new BuildProject.BuildOptions(this.context.currentPackage().ballerinaToml()));
    }

    public Package getPackage() {
        final PackageConfig packageConfig = PackageLoader.loadPackage(packagePath, false);
        this.context.addPackage(packageConfig);
        return this.context.currentPackage();
    }

    public BuildOptions getBuildOptions() {
        return (BuildOptions) this.context.getBuildOptions();
    }
    public void setBuildOptions(BuildOptions newBuildOptions) {
        BuildOptions buildOptions = (BuildOptions) this.context.getBuildOptions();
        buildOptions.setB7aConfigFile(newBuildOptions.getB7aConfigFile());
        buildOptions.setObservabilityEnabled(newBuildOptions.isObservabilityIncluded());
        buildOptions.setSkipLock(newBuildOptions.isSkipLock());
        buildOptions.setSourceRoot(newBuildOptions.getSourceRoot());
        this.context.setBuildOptions(newBuildOptions);
    }

    /**
     * {@code BuildOptions} represents build options.
     */
    public static class BuildOptions extends io.ballerina.projects.BuildOptions {

        private BuildOptions(BallerinaToml ballerinaToml) {
            this.sourceRoot = System.getProperty(ProjectConstants.USER_DIR);
            this.output = System.getProperty(ProjectConstants.USER_DIR);
            if (ballerinaToml.getBuildOptions() != null) {
                this.observabilityIncluded = ballerinaToml.getBuildOptions().isObservabilityIncluded();
            }
//            this.skipLock = ballerinaToml.getBuildOptions().skipLock();
//            this.b7aConfigFile = ballerinaToml.getBuildOptions().getB7aConfig();
        }

        public void setObservabilityEnabled(boolean observabilityEnabled) {
            observabilityIncluded = observabilityEnabled;
        }

        public void setSkipLock(boolean skipLock) {
            this.skipLock = skipLock;
        }

        public boolean isObservabilityIncluded() {
            return observabilityIncluded;
        }

        public boolean isSkipLock() {
            return skipLock;
        }

        public  boolean isCodeCoverage() {
            return this.codeCoverage;
        }

        public void setCodeCoverage(boolean codeCoverage) {
            this.codeCoverage = codeCoverage;
        }
    }
}
