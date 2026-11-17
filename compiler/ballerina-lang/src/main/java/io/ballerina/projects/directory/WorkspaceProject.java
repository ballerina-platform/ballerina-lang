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
import io.ballerina.projects.internal.environment.DefaultEnvironment;
import io.ballerina.projects.internal.environment.ProjectDependencyCache;
import io.ballerina.projects.internal.repositories.WorkspaceRepository;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectPaths;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;

public class WorkspaceProject extends Project {
    private final List<BuildProject> projectList;
    private WorkspaceBallerinaToml workspaceBallerinaToml;
    private final BuildOptions buildOptions;
    private WorkspaceManifest workspaceManifest;
    private WorkspaceResolution workspaceResolution;
    private boolean dependencyCacheWritable;
    // Source roots of the packages that depend on one another, directly or transitively. Each group is compiled
    // in its own environment.
    private List<List<Path>> packageGroups = List.of();

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
        return loadProject(path, environmentBuilder, buildOptions, false);
    }

    static ProjectLoadResult loadProject(Path path, EnvironmentBuilder environmentBuilder,
                                         BuildOptions buildOptions, boolean dependencyCacheWritable) {
        if (ProjectPaths.isWorkspaceProjectRoot(path)) {
            try {
                TomlDocument tomlDocument = TomlDocument.from(BALLERINA_TOML,
                        Files.readString(path.resolve(BALLERINA_TOML)));
                WorkspaceProject workspaceProject = new WorkspaceProject(path, buildOptions, tomlDocument);
                workspaceProject.dependencyCacheWritable = dependencyCacheWritable;
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
                workspaceProject.assignEnvironments(environmentBuilder, environment);
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
        List<List<BuildProject>> groups = groupPackages();
        for (List<BuildProject> group : groups) {
            Environment environment = createEnvironment(EnvironmentBuilder.getBuilder(), this, group,
                    groups.size() > 1);
            for (BuildProject buildProject : group) {
                buildProject.setEnvironment(environment);
                buildProject.clearCaches();
            }
        }
        this.workspaceResolution = null;
    }

    /**
     * Assigns an environment to each group of packages that depend on one another.
     * <p>
     * The packages are loaded into one environment, as the groups are found from their imports. When the
     * workspace has a single group, the packages stay in that environment. Otherwise, each group is moved to an
     * environment of its own, so that each can use its own resolution of a shared dependency.
     */
    private void assignEnvironments(EnvironmentBuilder environmentBuilder, Environment loadEnvironment) {
        List<List<BuildProject>> groups = groupPackages();
        if (groups.size() <= 1) {
            addDependencyCache(loadEnvironment, this.projectList, this.dependencyCacheWritable);
            return;
        }
        for (List<BuildProject> group : groups) {
            Environment environment = createEnvironment(environmentBuilder, this, group, true);
            for (BuildProject buildProject : group) {
                buildProject.setEnvironment(environment);
                buildProject.clearCaches();
            }
        }
    }

    /**
     * Merges groups of packages when a package now depends on a package of another group, e.g. when a build tool
     * generated code or an edit added an import of another package of the workspace. Only the merged groups move
     * to new environments; the other groups keep their environments and compiled state.
     * <p>
     * Groups are not split when such a dependency is removed. Compiling the packages in one environment is still
     * correct, and they are regrouped from scratch when the workspace is loaded again or its caches are cleared.
     */
    private void refreshPackageGroups() {
        if (this.packageGroups.isEmpty() || withinCurrentGroups(WorkspacePackageGroups.of(this.projectList))) {
            return;
        }
        List<List<Path>> previousGroups = this.packageGroups;
        List<List<BuildProject>> groups = WorkspacePackageGroups.of(this.projectList, previousGroups);
        this.packageGroups = toSourceRoots(groups);
        for (List<BuildProject> group : groups) {
            if (previousGroups.contains(group.stream().map(BuildProject::sourceRoot).toList())) {
                continue;
            }
            Environment environment = createEnvironment(EnvironmentBuilder.getBuilder(), this, group,
                    groups.size() > 1);
            for (BuildProject buildProject : group) {
                buildProject.setEnvironment(environment);
                buildProject.clearCaches();
            }
        }
        this.workspaceResolution = null;
    }

    /** Returns true if each of the given groups is part of one of the current groups. */
    private boolean withinCurrentGroups(List<List<BuildProject>> groups) {
        Map<Path, Integer> currentGroupOf = new HashMap<>();
        for (int i = 0; i < this.packageGroups.size(); i++) {
            for (Path root : this.packageGroups.get(i)) {
                currentGroupOf.put(root, i);
            }
        }
        for (List<BuildProject> group : groups) {
            Integer currentGroup = currentGroupOf.get(group.get(0).sourceRoot());
            for (BuildProject buildProject : group) {
                Integer memberGroup = currentGroupOf.get(buildProject.sourceRoot());
                if (memberGroup == null || !memberGroup.equals(currentGroup)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Creates an environment for a copy of the given package, holding the packages of its group. Used when a single
     * package of the workspace is duplicated.
     *
     * @param buildProject package of this workspace
     * @return environment for the group of the package
     */
    Environment groupEnvironment(BuildProject buildProject) {
        List<BuildProject> group = this.packageGroups.stream()
                .filter(roots -> roots.contains(buildProject.sourceRoot()))
                .findFirst()
                .map(roots -> this.projectList.stream().filter(member -> roots.contains(member.sourceRoot()))
                        .toList())
                .orElse(this.projectList);
        return createEnvironment(EnvironmentBuilder.getBuilder(), this, group, this.packageGroups.size() > 1);
    }

    private static List<List<Path>> toSourceRoots(List<List<BuildProject>> groups) {
        return groups.stream().map(group -> group.stream().map(BuildProject::sourceRoot).toList()).toList();
    }

    private List<List<BuildProject>> groupPackages() {
        List<List<BuildProject>> groups = WorkspacePackageGroups.of(this.projectList);
        this.packageGroups = toSourceRoots(groups);
        return groups;
    }

    /**
     * Creates the environment of a group of packages. The environment only provides the packages of its group,
     * and caches the compiled dependencies in the targets of those packages.
     */
    private static Environment createEnvironment(EnvironmentBuilder environmentBuilder, WorkspaceProject workspace,
                                                 List<BuildProject> group, boolean restrictToGroup) {
        Environment environment = environmentBuilder.setWorkspace(workspace).build();
        if (restrictToGroup) {
            environment.getService(WorkspaceRepository.class)
                    .restrictTo(group.stream().map(BuildProject::sourceRoot).toList());
        }
        addDependencyCache(environment, group, workspace.dependencyCacheWritable);
        return environment;
    }

    private static void addDependencyCache(Environment environment, List<BuildProject> group, boolean writable) {
        if (group.isEmpty() || !(environment instanceof DefaultEnvironment defaultEnvironment)) {
            return;
        }
        ProjectDependencyCache dependencyCache = new ProjectDependencyCache(
                group.stream().map(BuildProject::targetDir).toList(), writable);
        defaultEnvironment.addService(ProjectDependencyCache.class, dependencyCache);
        for (BuildProject buildProject : group) {
            if (buildProject.currentPackage() != null) {
                dependencyCache.updateDistributionVersion(buildProject,
                        buildProject.currentPackage().dependencyManifest());
            }
        }
    }

    @Override
    public Package currentPackage() {
        return this.projectList.iterator().next().currentPackage();
    }

    @Override
    public Project duplicate() {
        List<BuildProject> projects = new ArrayList<>();
        WorkspaceProject duplicateWp = new WorkspaceProject(this.sourceRoot, this.buildOptions,
                this.workspaceBallerinaToml.tomlDocument(), projects);
        duplicateWp.dependencyCacheWritable = this.dependencyCacheWritable;
        duplicateWp.packageGroups = this.packageGroups;
        // Build one environment per group of packages for the copy, and duplicate each member into the
        // environment of its group, instead of letting each member build its own.
        Map<Path, Environment> environments = new HashMap<>();
        List<List<Path>> groups = this.packageGroups.isEmpty() ?
                List.of(this.projectList.stream().map(BuildProject::sourceRoot).toList()) : this.packageGroups;
        for (List<Path> groupRoots : groups) {
            List<BuildProject> group = this.projectList.stream()
                    .filter(project -> groupRoots.contains(project.sourceRoot())).toList();
            Environment environment = createEnvironment(EnvironmentBuilder.getBuilder(), duplicateWp, group,
                    groups.size() > 1);
            groupRoots.forEach(root -> environments.put(root, environment));
        }
        for (BuildProject project : this.projectList) {
            projects.add(project.duplicate(environments.get(project.sourceRoot())));
        }
        return duplicateWp;
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

    /**
     * Returns the environment of the first package of the workspace.
     * <p>
     * Packages that do not depend on one another are compiled in separate environments. Use
     * {@code projectEnvironmentContext()} of a package of the workspace to get the environment it is compiled in.
     *
     * @return environment of the first package
     */
    @Override
    public ProjectEnvironment projectEnvironmentContext() {
        return this.projectList.iterator().next().projectEnvironmentContext();
    }

    public WorkspaceResolution getResolution() {
        refreshPackageGroups();
        if (this.workspaceResolution == null) {
            this.workspaceResolution = WorkspaceResolution.from(this);
        }
        return this.workspaceResolution;
    }

    public WorkspaceResolution getResolution(ResolutionOptions resolutionOptions) {
        refreshPackageGroups();
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
