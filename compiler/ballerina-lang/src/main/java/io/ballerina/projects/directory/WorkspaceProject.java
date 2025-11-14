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
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.ProjectLoadResult;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.WorkspaceBallerinaToml;
import io.ballerina.projects.WorkspaceManifest;
import io.ballerina.projects.WorkspaceResolution;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.environment.ProjectEnvironment;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.projects.internal.WorkspaceManifestBuilder;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectPaths;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;

public class WorkspaceProject extends Project {
    private final List<BuildProject> projectList;
    private WorkspaceBallerinaToml workspaceBallerinaToml;
    private final BuildOptions buildOptions;
    private WorkspaceManifest workspaceManifest;
    private WorkspaceResolution workspaceResolution;

    private WorkspaceProject(Path projectPath, BuildOptions buildOptions, TomlDocument tomlDocument) {
        super(ProjectKind.WORKSPACE_PROJECT, projectPath, buildOptions);
        this.workspaceBallerinaToml = WorkspaceBallerinaToml.from(tomlDocument, this);
        this.buildOptions = buildOptions;
        this.workspaceManifest = WorkspaceManifestBuilder.from(tomlDocument, projectPath).manifest();
        this.projectList = new ArrayList<>();
    }

    private WorkspaceProject(Path projectPath, BuildOptions buildOptions, TomlDocument tomlDocument,
                             List<BuildProject> projects) {
        super(ProjectKind.WORKSPACE_PROJECT, projectPath, buildOptions);
        this.workspaceBallerinaToml = WorkspaceBallerinaToml.from(tomlDocument, this);
        this.buildOptions = buildOptions;
        this.workspaceManifest = WorkspaceManifestBuilder.from(tomlDocument, projectPath).manifest();
        this.projectList = projects;
    }

    static ProjectLoadResult loadProject(Path path, EnvironmentBuilder environmentBuilder,
                                                  BuildOptions buildOptions) {
        if (ProjectPaths.isWorkspaceProjectRoot(path)) {
            try {
                TomlDocument tomlDocument = TomlDocument.from(BALLERINA_TOML,
                        Files.readString(path.resolve(BALLERINA_TOML)));
                WorkspaceProject workspaceProject = new WorkspaceProject(path, buildOptions, tomlDocument);
                Environment environment = environmentBuilder.setWorkspace(workspaceProject).build();
                List<Diagnostic> diagnostics = new ArrayList<>(workspaceProject.manifest().diagnostics().diagnostics());
                String org = null;
                for (Path pkgPath : workspaceProject.manifest().packages()) {
                    ProjectLoadResult buildProjectLoadResult = loadBuildProject(environment, pkgPath,
                            workspaceProject, org);
                    if (org == null) {
                        org = buildProjectLoadResult.project().currentPackage().packageOrg().toString();
                    }
                    if (buildProjectLoadResult.diagnostics().hasErrors()) {
                        diagnostics.addAll(buildProjectLoadResult.diagnostics().diagnostics());
                        continue; // Skip adding this project if there are errors
                    }
                    workspaceProject.projectList.add((BuildProject) buildProjectLoadResult.project());
                }
                return new ProjectLoadResult(workspaceProject, new DefaultDiagnosticResult(diagnostics));
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
        if (this.buildOptions().getTargetPath() == null) {
            return this.sourceRoot.resolve(ProjectConstants.TARGET_DIR_NAME);
        } else {
            return Path.of(this.buildOptions().getTargetPath());
        }
    }

    @Override
    public Path generatedResourcesDir() {
        return this.projectList.iterator().next().generatedResourcesDir();
    }

    @Override
    public void clearCaches() {
        Environment environment = EnvironmentBuilder.getBuilder().setWorkspace(this).build();
        for (BuildProject buildProject : this.projectList) {
            buildProject.setEnvironment(environment);
            buildProject.clearCaches();
        }
        this.workspaceResolution = null;
    }

    @Override
    public Package currentPackage() {
        return this.projectList.iterator().next().currentPackage();
    }

    @Override
    public Project duplicate() {
        List<BuildProject> projects = new ArrayList<>();
        for (Project project : this.projectList) {
            projects.add((BuildProject) project.duplicate());
        }
        return new WorkspaceProject(this.sourceRoot, this.buildOptions,
                this.workspaceBallerinaToml.tomlDocument(), projects);
    }

    @Override
    public DocumentId documentId(Path file) {
        for (Project project : this.projectList) {
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
        for (Project project : this.projectList) {
            if (project.documentPath(documentId).isPresent()) {
                return project.documentPath(documentId);
            }
        }
        return Optional.empty();
    }

    @Override
    public void save() {
        this.projectList.forEach(Project::save);
    }

    @Override
    public ProjectEnvironment projectEnvironmentContext() {
        return this.projectList.iterator().next().projectEnvironmentContext();
    }

    public WorkspaceResolution getResolution() {
        if (this.workspaceResolution == null) {
            this.workspaceResolution = WorkspaceResolution.from(this);
        }
        return this.workspaceResolution;
    }

    public WorkspaceResolution getResolution(ResolutionOptions resolutionOptions) {
        return WorkspaceResolution.from(this, resolutionOptions);
    }

    public List<BuildProject> projects() {
        return Collections.unmodifiableList(this.projectList);
    }

    public WorkspaceManifest manifest() {
        return this.workspaceManifest;
    }

    public WorkspaceBallerinaToml ballerinaToml() {
        return this.workspaceBallerinaToml;
    }


    /**
     * Reloads the workspace project with the provided TomlDocument.
     * This method creates a new instance of WorkspaceProject with the updated TomlDocument.
     *
     * @param tomlDocument The new TomlDocument to reload the workspace project
     */
    public ProjectLoadResult reload(TomlDocument tomlDocument) {
        this.workspaceBallerinaToml = WorkspaceBallerinaToml.from(tomlDocument, this);
        this.workspaceManifest = WorkspaceManifestBuilder.from(tomlDocument, sourceRoot).manifest();
        this.projectList.clear();
        this.workspaceResolution = null; // Reset the workspace resolution

        Environment environment = EnvironmentBuilder.getBuilder().setWorkspace(this).build();
        List<Diagnostic> diagnostics = new ArrayList<>();
        String org = null;
        for (Path pkgPath : this.workspaceManifest.packages()) {
            ProjectLoadResult buildProjectLoadResult = loadBuildProject(environment, pkgPath, this, org);
            if (org == null) {
                org = buildProjectLoadResult.project().currentPackage().packageOrg().toString();
            }
            if (buildProjectLoadResult.diagnostics().hasErrors()) {
                diagnostics.addAll(buildProjectLoadResult.diagnostics().diagnostics());
                continue; // Skip adding this project if there are errors
            }
            this.projectList.add((BuildProject) buildProjectLoadResult.project());
            diagnostics.addAll(buildProjectLoadResult.diagnostics().diagnostics());
        }
        return new ProjectLoadResult(this, new DefaultDiagnosticResult(diagnostics));
    }

    private static ProjectLoadResult loadBuildProject(Environment environment, Path packagePath,
                                                      WorkspaceProject workspaceProject, String org) {
        ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
        return BuildProject.loadProject(packagePath, projectEnvironmentBuilder, workspaceProject.buildOptions,
                workspaceProject, org);
    }
}
