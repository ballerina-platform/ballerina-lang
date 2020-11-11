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
package io.ballerina.projects;

import io.ballerina.projects.environment.ProjectEnvironment;

import java.nio.file.Path;

/**
 * The class {code Project} provides an abstract representation of a Ballerina project.
 *
 * @since 2.0.0
 */
public abstract class Project {
    protected final Path sourceRoot;
    private Package currentPackage;
    private BuildOptions buildOptions;
    private final ProjectEnvironment projectEnvironment;
    private final ProjectKind projectKind;

    protected Project(ProjectKind projectKind,
                      Path projectPath,
                      ProjectEnvironmentBuilder projectEnvironmentBuilder) {
        this.projectKind = projectKind;
        this.sourceRoot = projectPath;
        this.projectEnvironment = projectEnvironmentBuilder.build(this);
    }

    public ProjectKind kind() {
        return projectKind;
    }

    public Package currentPackage() {
        // TODO Handle concurrent read/write to the currentPackage variable
        return this.currentPackage;
    }

    public void addPackage(PackageConfig packageConfig) {
        Package newPackage = Package.from(this, packageConfig);
        setCurrentPackage(newPackage);
    }

    public Path sourceRoot() {
        return this.sourceRoot;
    }

    public BuildOptions getBuildOptions() {
        return buildOptions;
    }

    public void setBuildOptions(BuildOptions buildOptions) {
        this.buildOptions = buildOptions;
    }

    protected void setCurrentPackage(Package currentPackage) {
        // TODO Handle concurrent read/write to the currentPackage variable
        this.currentPackage = currentPackage;
    }

    public ProjectEnvironment projectEnvironmentContext() {
        return this.projectEnvironment;
    }
}
