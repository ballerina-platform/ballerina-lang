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
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.nio.file.Path;
import java.util.Optional;

import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * The class {code Project} provides an abstract representation of a Ballerina project.
 *
 * @since 2.0.0
 */
public abstract class Project {
    protected final Path sourceRoot;
    private Package currentPackage;
    private BuildOptions buildOptions;
    protected ProjectEnvironment projectEnvironment;
    private final ProjectKind projectKind;

    protected Project(ProjectKind projectKind,
                      Path projectPath,
                      ProjectEnvironmentBuilder projectEnvironmentBuilder, BuildOptions buildOptions) {
        this.projectKind = projectKind;
        this.sourceRoot = projectPath;
        this.buildOptions = buildOptions;
        this.projectEnvironment = projectEnvironmentBuilder.build(this);
    }

    protected Project(ProjectKind projectKind,
                      Path projectPath,
                      ProjectEnvironmentBuilder projectEnvironmentBuilder) {
        this.projectKind = projectKind;
        this.sourceRoot = projectPath;
        this.projectEnvironment = projectEnvironmentBuilder.build(this);
        this.buildOptions = BuildOptions.builder().build();
    }

    void setBuildOptions(BuildOptions buildOptions) {
        this.buildOptions = buildOptions;
    }

    public ProjectKind kind() {
        return projectKind;
    }

    public Package currentPackage() {
        // TODO Handle concurrent read/write to the currentPackage variable
        return this.currentPackage;
    }

    public void addPackage(PackageConfig packageConfig) {
        Package newPackage = Package.from(this, packageConfig, this.buildOptions.compilationOptions());
        setCurrentPackage(newPackage);
    }

    public Path sourceRoot() {
        return this.sourceRoot;
    }

    public abstract Path targetDir();

    protected void setCurrentPackage(Package currentPackage) {
        // TODO Handle concurrent read/write to the currentPackage variable
        this.currentPackage = currentPackage;
    }

    public ProjectEnvironment projectEnvironmentContext() {
        return this.projectEnvironment;
    }

    public BuildOptions buildOptions() {
        return buildOptions;
    }

    // Following project path was added to support old compiler extensions.
    // Currently this method is only called from Build and Single File projects
    // todo remove after introducing extension model
    protected void populateCompilerContext() {
        CompilerContext compilerContext = this.projectEnvironmentContext().getService(CompilerContext.class);
        CompilerOptions options = CompilerOptions.getInstance(compilerContext);
        options.put(PROJECT_DIR, this.sourceRoot().toAbsolutePath().toString());
    }

    /**
     * Clears all caches of this project.
     *
     * The current content and the structure will be preserved. In-memory caches
     * (i.e. package resolution caches, compilation caches)
     * generated during project compilation will be discarded.
     */
    public abstract void clearCaches();

    /**
     * Creates a new Project instance which has the same structure as this Project.
     *
     * The new project will have the same structure and content as this. The caches of
     * this project generated during project compilation will not be copied.
     *
     * @return The new Project instance.
     */
    public abstract Project duplicate();

    protected Project resetPackage(Project project) {
        Package clone = this.currentPackage.duplicate(project);
        project.setCurrentPackage(clone);
        return project;
    }

    public abstract DocumentId documentId(Path file);

    public abstract Optional<Path> documentPath(DocumentId documentId);

    public abstract void save();
}
