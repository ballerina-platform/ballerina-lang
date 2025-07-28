/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.directory;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildProjectLoadResult;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.ProjectLoadResult;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.WorkspaceBallerinaToml;
import io.ballerina.projects.WorkspaceManifest;
import io.ballerina.projects.WorkspaceProjectLoadResult;
import io.ballerina.projects.WorkspaceResolution;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.environment.ProjectEnvironment;
import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.projects.internal.WorkspaceManifestBuilder;
import io.ballerina.projects.util.ProjectPaths;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;

public class WorkspaceProject extends Project {
    private final Set<BuildProject> projectSet;
    private final WorkspaceBallerinaToml workspaceBallerinaToml;
    private final BuildOptions buildOptions;
    private final WorkspaceManifest workspaceManifest;
    private WorkspaceResolution workspaceResolution;

    private WorkspaceProject(Path projectPath, BuildOptions buildOptions, TomlDocument tomlDocument) {
        super(ProjectKind.WORKSPACE_PROJECT, projectPath, buildOptions);
        this.workspaceBallerinaToml = WorkspaceBallerinaToml.from(tomlDocument, this);
        this.buildOptions = buildOptions;
        this.workspaceManifest = WorkspaceManifestBuilder.from(tomlDocument, projectPath).manifest();
        this.projectSet = new HashSet<>();
    }

    private WorkspaceProject(Path projectPath, BuildOptions buildOptions, TomlDocument tomlDocument,
                             Set<BuildProject> projects) {
        super(ProjectKind.WORKSPACE_PROJECT, projectPath, buildOptions);
        this.workspaceBallerinaToml = WorkspaceBallerinaToml.from(tomlDocument, this);
        this.buildOptions = buildOptions;
        this.workspaceManifest = WorkspaceManifestBuilder.from(tomlDocument, projectPath).manifest();
        this.projectSet = projects;
    }


    public static WorkspaceProjectLoadResult from(Path workspacePath) {
        return from(workspacePath, BuildOptions.builder().build());
    }

    public static WorkspaceProjectLoadResult from(Path path, BuildOptions buildOptions) {
        return from(path, EnvironmentBuilder.getBuilder(), buildOptions);
    }

    public static WorkspaceProjectLoadResult from(Path path, EnvironmentBuilder environmentBuilder) {
        return from(path, environmentBuilder, BuildOptions.builder().build());
    }

    public static WorkspaceProjectLoadResult from(Path path, EnvironmentBuilder environmentBuilder,
                                        BuildOptions buildOptions) {
        if (ProjectPaths.isWorkspaceProjectRoot(path)) {
            try {
                TomlDocument tomlDocument = TomlDocument.from(BALLERINA_TOML,
                        Files.readString(path.resolve(BALLERINA_TOML)));
                WorkspaceProject workspaceProject = new WorkspaceProject(path, buildOptions, tomlDocument);
                Environment environment = environmentBuilder.setWorkspace(workspaceProject).build();
                List<Diagnostic> diagnostics = new ArrayList<>(workspaceProject.manifest().diagnostics().diagnostics());
                for (Path pkgPath : workspaceProject.manifest().packages()) {
                    BuildProjectLoadResult buildProjectLoadResult = loadBuildProject(environment, pkgPath,
                            workspaceProject);
                    if (buildProjectLoadResult.diagnostics().hasErrors()) {
                        diagnostics.addAll(buildProjectLoadResult.diagnostics().diagnostics());
                        continue; // Skip adding this project if there are errors
                    }
                    workspaceProject.projectSet.add(buildProjectLoadResult.project());
                    diagnostics.addAll(buildProjectLoadResult.diagnostics().diagnostics());
                }
                return new WorkspaceProjectLoadResult(workspaceProject, new DefaultDiagnosticResult(diagnostics));
            } catch (IOException e) {
                throw new ProjectException("Error reading " + BALLERINA_TOML + " file in workspace: "
                        + path.toAbsolutePath(), e);
            }
        }

        // If the given path is not a workspace root or package root, throw an exception
        throw new ProjectException("The specified path is not a valid workspace: " + path.toAbsolutePath());
    }

    @Override
    public Path targetDir() {
        return this.projectSet.iterator().next().targetDir();
    }

    @Override
    public Path generatedResourcesDir() {
        return this.projectSet.iterator().next().generatedResourcesDir();
    }

    @Override
    public void clearCaches() {
        for (Project project : this.projectSet) {
            project.clearCaches();
        }
        this.workspaceResolution = null;
    }

    @Override
    public Project duplicate() {
        Set<BuildProject> projects = new HashSet<>();
        for (Project project : this.projectSet) {
            projects.add((BuildProject) project.duplicate());
        }
        return new WorkspaceProject(this.sourceRoot, this.buildOptions,
                this.workspaceBallerinaToml.tomlDocument(), projects);
    }

    @Override
    public DocumentId documentId(Path file) {
        for (Project project : this.projectSet) {
            try {
                return project.documentId(file);
            } catch (ProjectException e) {
                // Ignore the exception as we are checking all projects
            }
        }
        throw new ProjectException("'" + file.toString() + "' does not belong to the current workspace");
    }

    @Override
    public Optional<Path> documentPath(DocumentId documentId) {
        for (Project project : this.projectSet) {
            if (project.documentPath(documentId).isPresent()) {
                return project.documentPath(documentId);
            }
        }
        return Optional.empty();
    }

    @Override
    public void save() {
        this.projectSet.forEach(Project::save);
    }

    @Override
    public ProjectEnvironment projectEnvironmentContext() {
        return this.projectSet.iterator().next().projectEnvironmentContext();
    }

    public WorkspaceResolution getResolution() {
        if (this.workspaceResolution == null) {
            this.workspaceResolution = WorkspaceResolution.from(this);
        }
        return this.workspaceResolution;
    }

    public Set<BuildProject> projects() {
        return Collections.unmodifiableSet(this.projectSet);
    }

    public WorkspaceManifest manifest() {
        return this.workspaceManifest;
    }

    public WorkspaceBallerinaToml ballerinaToml() {
        return this.workspaceBallerinaToml;
    }

    private static BuildProjectLoadResult loadBuildProject(Environment environment, Path packagePath,
                                                           WorkspaceProject workspaceProject) {
        ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
        return BuildProject.from(packagePath, projectEnvironmentBuilder, workspaceProject.buildOptions, workspaceProject);
    }
}
