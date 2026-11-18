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
    // Source roots of the packages that depend on one another, directly or transitively. The packages of a group
    // are always compiled in one environment.
    private List<List<Path>> packageGroups = List.of();
    // Source roots of the packages compiled in each environment. An environment holds one or more groups that
    // resolve every dependency they share to the same version and dependency versions.
    private List<List<Path>> contexts = List.of();

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
                workspaceProject.assignEnvironments(environment);
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

    /**
     * Clears the caches of all packages and moves them to new environments.
     * <p>
     * Groups of packages that depend on one another are found again from their imports, and placed in environments
     * by the dependency versions resolved for them so far: groups that resolve every shared dependency to the same
     * version and dependency versions share an environment, and the others get environments of their own.
     */
    @Override
    public void clearCaches() {
        List<List<BuildProject>> groups = groupPackages();
        List<List<BuildProject>> contextMembers = placeInContexts(groups);
        this.contexts = toSourceRoots(contextMembers);
        for (List<BuildProject> context : contextMembers) {
            Environment environment = createEnvironment(EnvironmentBuilder.getBuilder(), this, context,
                    contextMembers.size() > 1);
            for (BuildProject buildProject : context) {
                buildProject.setEnvironment(environment);
                buildProject.clearCaches();
            }
        }
        this.workspaceResolution = null;
    }

    /**
     * Assigns the environment the packages are loaded into to all of them. The dependency versions of the packages
     * are not known before they are resolved, so they start in one environment.
     */
    private void assignEnvironments(Environment loadEnvironment) {
        groupPackages();
        this.contexts = List.of(this.projectList.stream().map(BuildProject::sourceRoot).toList());
        addDependencyCache(loadEnvironment, this.projectList, this.dependencyCacheWritable);
    }

    /**
     * Places the given groups of packages in environments. A group joins the first environment whose packages
     * resolved every dependency it shares with the group to the same variant, i.e. the same version compiled
     * against the same dependency versions. Groups not resolved yet join the first environment.
     */
    private List<List<BuildProject>> placeInContexts(List<List<BuildProject>> groups) {
        List<Map<String, String>> contextVariants = new ArrayList<>();
        List<List<BuildProject>> contextMembers = new ArrayList<>();
        for (List<BuildProject> group : groups) {
            Map<String, String> groupVariants = new HashMap<>();
            for (BuildProject buildProject : group) {
                resolvedVariants(buildProject).forEach(groupVariants::putIfAbsent);
            }
            int context = 0;
            while (context < contextVariants.size() && !compatible(contextVariants.get(context), groupVariants)) {
                context++;
            }
            if (context == contextVariants.size()) {
                contextVariants.add(new HashMap<>());
                contextMembers.add(new ArrayList<>());
            }
            groupVariants.forEach(contextVariants.get(context)::putIfAbsent);
            contextMembers.get(context).addAll(group);
        }
        // Keep the packages in the order of the workspace.
        contextMembers.forEach(members -> members.sort((first, second) ->
                Integer.compare(this.projectList.indexOf(first), this.projectList.indexOf(second))));
        return contextMembers;
    }

    private static Map<String, String> resolvedVariants(BuildProject buildProject) {
        if (buildProject.projectEnvironmentContext() == null) {
            return Map.of();
        }
        ProjectDependencyCache dependencyCache = buildProject.projectEnvironmentContext().environment()
                .getService(ProjectDependencyCache.class);
        return dependencyCache == null ? Map.of() : dependencyCache.resolvedVariants(buildProject);
    }

    private static boolean compatible(Map<String, String> contextVariants, Map<String, String> groupVariants) {
        for (Map.Entry<String, String> variant : groupVariants.entrySet()) {
            String contextVariant = contextVariants.get(variant.getKey());
            if (contextVariant != null && !contextVariant.equals(variant.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Merges groups of packages when a package now depends on a package of another group, e.g. when a build tool
     * generated code or an edit added an import of another package of the workspace. When the merged groups were
     * compiled in different environments, they move to a new environment of their own; the other packages keep
     * their environments and compiled state.
     * <p>
     * Groups are not split when such a dependency is removed. Compiling the packages in one environment is still
     * correct, and they are regrouped from scratch when the workspace is loaded again or its caches are cleared.
     */
    private void refreshPackageGroups() {
        if (this.packageGroups.isEmpty() || withinCurrentGroups(WorkspacePackageGroups.of(this.projectList))) {
            return;
        }
        List<List<BuildProject>> groups = WorkspacePackageGroups.of(this.projectList, this.packageGroups);
        this.packageGroups = toSourceRoots(groups);
        for (List<BuildProject> group : groups) {
            List<Path> roots = group.stream().map(BuildProject::sourceRoot).toList();
            if (this.contexts.stream().anyMatch(context -> context.containsAll(roots))) {
                continue;
            }
            List<List<Path>> contexts = new ArrayList<>();
            for (List<Path> context : this.contexts) {
                List<Path> remaining = context.stream().filter(root -> !roots.contains(root)).toList();
                if (!remaining.isEmpty()) {
                    contexts.add(remaining);
                }
            }
            contexts.add(roots);
            this.contexts = contexts;
            Environment environment = createEnvironment(EnvironmentBuilder.getBuilder(), this, group,
                    this.contexts.size() > 1);
            for (BuildProject buildProject : group) {
                buildProject.setEnvironment(environment);
                buildProject.clearCaches();
            }
        }
        this.workspaceResolution = null;
    }

    /**
     * Moves the given package, with the packages it is grouped with by dependencies on one another, to an
     * environment of their own when the dependency versions last resolved for them conflict with those of the other
     * packages of their environment, i.e. when they share a dependency resolved to another version, or compiled
     * against other dependency versions.
     * <p>
     * Call this after resolving the package and before compiling it, e.g. after an edit changes its dependencies.
     * The other packages keep their environments and compiled state.
     *
     * @param buildProject package of this workspace
     * @return true if the package moved to a new environment
     */
    public boolean ensureCompatibleEnvironment(BuildProject buildProject) {
        Path root = buildProject.sourceRoot();
        Optional<List<Path>> context = this.contexts.stream().filter(roots -> roots.contains(root)).findFirst();
        if (context.isEmpty()) {
            return false;
        }
        List<Path> groupRoots = this.packageGroups.stream().filter(roots -> roots.contains(root)).findFirst()
                .orElse(List.of(root));
        if (context.get().stream().allMatch(groupRoots::contains)) {
            return false;
        }
        Map<String, String> groupVariants = new HashMap<>();
        Map<String, String> otherVariants = new HashMap<>();
        for (BuildProject member : this.projectList) {
            if (groupRoots.contains(member.sourceRoot())) {
                resolvedVariants(member).forEach(groupVariants::putIfAbsent);
            } else if (context.get().contains(member.sourceRoot())) {
                resolvedVariants(member).forEach(otherVariants::putIfAbsent);
            }
        }
        if (compatible(otherVariants, groupVariants)) {
            return false;
        }
        List<List<Path>> contexts = new ArrayList<>();
        for (List<Path> roots : this.contexts) {
            List<Path> remaining = roots.stream().filter(contextRoot -> !groupRoots.contains(contextRoot)).toList();
            if (!remaining.isEmpty()) {
                contexts.add(remaining);
            }
        }
        contexts.add(groupRoots);
        this.contexts = contexts;
        List<BuildProject> group = this.projectList.stream()
                .filter(member -> groupRoots.contains(member.sourceRoot())).toList();
        Environment environment = createEnvironment(EnvironmentBuilder.getBuilder(), this, group, true);
        for (BuildProject member : group) {
            member.setEnvironment(environment);
            member.clearCaches();
        }
        this.workspaceResolution = null;
        return true;
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
     * Creates an environment for a copy of the given package, holding the packages compiled in its environment.
     * Used when a single package of the workspace is duplicated.
     *
     * @param buildProject package of this workspace
     * @return environment for the packages compiled with the package
     */
    Environment groupEnvironment(BuildProject buildProject) {
        List<BuildProject> context = this.contexts.stream()
                .filter(roots -> roots.contains(buildProject.sourceRoot()))
                .findFirst()
                .map(roots -> this.projectList.stream().filter(member -> roots.contains(member.sourceRoot()))
                        .toList())
                .orElse(this.projectList);
        return createEnvironment(EnvironmentBuilder.getBuilder(), this, context, this.contexts.size() > 1);
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
     * Creates the environment of the given packages. When the workspace has several environments, the environment
     * only provides its own packages. It caches the compiled dependencies in the targets of its packages.
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
        duplicateWp.contexts = this.contexts;
        // Build one environment per environment of this workspace for the copy, and duplicate each member into the
        // environment of its packages, instead of letting each member build its own.
        Map<Path, Environment> environments = new HashMap<>();
        List<List<Path>> groups = this.contexts.isEmpty() ?
                List.of(this.projectList.stream().map(BuildProject::sourceRoot).toList()) : this.contexts;
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
