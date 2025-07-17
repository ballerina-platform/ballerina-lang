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
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.WorkspaceBallerinaToml;
import io.ballerina.projects.WorkspaceManifest;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.environment.ProjectEnvironment;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.internal.WorkspaceDependencyGraphBuilder;
import io.ballerina.projects.internal.WorkspaceManifestBuilder;
import io.ballerina.projects.util.ProjectPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;

public class WorkspaceProject extends Project {
    private final Set<Project> projectSet;
    private final WorkspaceBallerinaToml workspaceBallerinaToml;
    private final BuildOptions buildOptions;
    private final WorkspaceManifest workspaceManifest;

    private WorkspaceProject(Path projectPath, BuildOptions buildOptions, TomlDocument tomlDocument,
                             EnvironmentBuilder environmentBuilder) {
        super(ProjectKind.WORKSPACE_PROJECT, projectPath, buildOptions);
        this.workspaceBallerinaToml = WorkspaceBallerinaToml.from(tomlDocument, this);
        this.buildOptions = buildOptions;
        this.workspaceManifest = WorkspaceManifestBuilder.from(tomlDocument, projectPath).manifest();
        this.projectSet = new HashSet<>();
        loadProjects(environmentBuilder.setWorkspace(this).build());
        this.dependencyGraph = buildDependencyGraph();
        setCurrentPackage(this.projectSet.iterator().next().currentPackage());
    }

    public static WorkspaceProject load(Path workspacePath) {
        return load(workspacePath, BuildOptions.builder().build());
    }

    public static WorkspaceProject load(Path path, BuildOptions buildOptions) {
        return load(path, EnvironmentBuilder.getBuilder(), buildOptions);
    }

    public static WorkspaceProject load(Path path, EnvironmentBuilder environmentBuilder) {
        return load(path, environmentBuilder, BuildOptions.builder().build());
    }

    public static WorkspaceProject load(Path path, EnvironmentBuilder environmentBuilder,
                                        BuildOptions buildOptions) {
        if (ProjectPaths.isWorkspaceProjectRoot(path)) {
            try {
                TomlDocument tomlDocument = TomlDocument.from(BALLERINA_TOML,
                        Files.readString(path.resolve(BALLERINA_TOML)));
                return new WorkspaceProject(path, buildOptions, tomlDocument, environmentBuilder);
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
        this.dependencyGraph = null;
    }

    @Override
    public Project duplicate() {
        return null;
    }

    @Override
    public DocumentId documentId(Path file) {
        return null;
    }

    @Override
    public Optional<Path> documentPath(DocumentId documentId) {
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

    public Set<Project> projects() {
        return Collections.unmodifiableSet(this.projectSet);
    }

    private void loadProjects(Environment environment) {
        for (Path packagePath : this.workspaceManifest.packages()) {
            Path ballerinaTomlPath = packagePath.resolve(BALLERINA_TOML);
            if (Files.exists(ballerinaTomlPath)) {
                ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
                BuildProject project = BuildProject.load(projectEnvironmentBuilder, packagePath, buildOptions);
                this.projectSet.add(project);
            }
        }
    }

    private DependencyGraph<Project> buildDependencyGraph() {
        WorkspaceDependencyGraphBuilder graphBuilder = new WorkspaceDependencyGraphBuilder();
        for (Project project : this.projectSet) {
            graphBuilder.addPackage(project);
            Collection<ResolvedPackageDependency> directDependencies = project.currentPackage()
                    .getResolution(ResolutionOptions.builder().setOffline(true).build())
                    .dependencyGraph()
                    .getDirectDependencies(new ResolvedPackageDependency(project.currentPackage(),
                            PackageDependencyScope.DEFAULT));
            addDependencies(project, directDependencies, graphBuilder);
        }
        return graphBuilder.buildGraph();
    }

    private static void addDependencies(Project pkg, Collection<ResolvedPackageDependency> directDependencies,
                                        WorkspaceDependencyGraphBuilder graphBuilder) {
        graphBuilder.addPackage(pkg);
        for (ResolvedPackageDependency directDependency : directDependencies) {
            if (directDependency.packageInstance().project().kind() == ProjectKind.BUILD_PROJECT) {
                graphBuilder.addDependency(pkg, directDependency.packageInstance().project());
                addDependencies(directDependency.packageInstance().project(), directDependency.packageInstance()
                        .getResolution(ResolutionOptions.builder().setOffline(true).build())
                        .dependencyGraph().getDirectDependencies(directDependency), graphBuilder);
            }
        }
    }
}
