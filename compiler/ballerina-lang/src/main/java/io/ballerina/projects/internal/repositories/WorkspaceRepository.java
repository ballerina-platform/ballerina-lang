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
package io.ballerina.projects.internal.repositories;


import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.WorkspaceProject;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class WorkspaceRepository extends AbstractPackageRepository {

    private final WorkspaceProject workspaceProject;
    // Source roots of the packages this repository provides. Null provides all packages of the workspace.
    private Set<Path> memberRoots;

    public WorkspaceRepository(WorkspaceProject workspace) {
        this.workspaceProject = workspace;
    }

    /**
     * Restricts this repository to the given packages of the workspace.
     * <p>
     * The repository provides the packages themselves, which are bound to the environment they are loaded into.
     * Packages that depend on one another share an environment, and an environment only provides its own
     * packages, so that a package never resolves a package of another environment.
     *
     * @param packageRoots source roots of the packages to provide
     */
    public void restrictTo(Collection<Path> packageRoots) {
        this.memberRoots = packageRoots.stream().map(root -> root.toAbsolutePath().normalize())
                .collect(Collectors.toUnmodifiableSet());
    }

    private List<BuildProject> members() {
        Set<Path> roots = this.memberRoots;
        if (roots == null) {
            return this.workspaceProject.projects();
        }
        return this.workspaceProject.projects().stream()
                .filter(project -> roots.contains(project.sourceRoot().toAbsolutePath().normalize()))
                .toList();
    }

    @Override
    protected List<PackageVersion> getPackageVersions(PackageOrg org, PackageName name, PackageVersion version) {
        if (version == null) {
            return members().stream().filter(project ->
                    project.currentPackage().packageOrg().equals(org)
                            && project.currentPackage().packageName().equals(name)).findFirst().map(project ->
                    Collections.singletonList(project.currentPackage().packageVersion()))
                    .orElse(Collections.emptyList());
        }
        return members().stream().filter(project ->
                project.currentPackage().descriptor().equals(PackageDescriptor.from(org, name, version))).findFirst()
                .map(project -> Collections.singletonList(project.currentPackage().packageVersion()))
                .orElse(Collections.emptyList());
    }

    @Override
    protected DependencyGraph<PackageDescriptor> getDependencyGraph(
            PackageOrg org, PackageName name, PackageVersion version) {
        for (Project project : members()) {
            Package pkg = project.currentPackage();
            if (pkg.packageOrg().equals(org)
                    && pkg.packageName().equals(name)) {
                DependencyGraph<ResolvedPackageDependency> pkgDependencyGraph =
                        pkg.getResolution(ResolutionOptions.builder().setOffline(true).build()).dependencyGraph();
                // Convert to DependencyGraph<PackageDescriptor>
                DependencyGraph.DependencyGraphBuilder<PackageDescriptor> graphBuilder =
                        DependencyGraph.DependencyGraphBuilder.getBuilder();
                pkgDependencyGraph.getNodes().forEach(node -> {
                    List<PackageDescriptor> dependencies = pkgDependencyGraph.getDirectDependencies(node).stream()
                            .map(directDependency -> directDependency.packageInstance().descriptor())
                            .collect(Collectors.toList());
                    graphBuilder.addDependencies(node.packageInstance().descriptor(), dependencies);
                });
                return graphBuilder.build();
            }
        }
        return DependencyGraph.emptyGraph();
    }

    @Override
    public boolean isPackageExists(PackageOrg org, PackageName name, PackageVersion version) {
        if (version == null) {
            return members().stream()
                    .anyMatch(project -> project.currentPackage().packageOrg().equals(org)
                            && project.currentPackage().packageName().equals(name));

        }
        return members().stream()
                .anyMatch(project -> project.currentPackage().descriptor()
                .equals(PackageDescriptor.from(org, name, version)));
    }

    @Override
    public Collection<ModuleDescriptor> getModules(PackageOrg org, PackageName name, PackageVersion version) {
        List<ModuleDescriptor> moduleDescriptors = new ArrayList<>();
        if (version == null) {
            for (BuildProject buildProject : members()) {
                if (buildProject.currentPackage().packageOrg().equals(org)
                        && buildProject.currentPackage().packageName().equals(name)) {
                    buildProject.currentPackage().moduleIds().stream().map(moduleId -> buildProject.currentPackage().
                            module(moduleId).descriptor()).forEach(moduleDescriptors::add);
                    break;
                }
            }
            return moduleDescriptors;
        }
        for (BuildProject buildProject : members()) {
            if (buildProject.currentPackage().descriptor().equals(PackageDescriptor.from(org, name, version))) {
                buildProject.currentPackage().moduleIds().stream().map(moduleId ->
                        buildProject.currentPackage().module(moduleId).descriptor()).forEach(moduleDescriptors::add);
                break;
            }
        }
        return moduleDescriptors;
    }

    @Override
    public Optional<Package> getPackage(ResolutionRequest request, ResolutionOptions options) {
        if (request.version().isEmpty()) {
            return members().stream().filter(project ->
                    project.currentPackage().packageOrg().equals(request.orgName())
                            && project.currentPackage().packageName().equals(request.packageName())).findFirst()
                    .map(Project::currentPackage);
        }
        return members().stream().filter(project ->
                project.currentPackage().descriptor().equals(
                request.packageDescriptor())).findFirst().map(Project::currentPackage);
    }

    @Override
    public Collection<PackageVersion> getPackageVersions(ResolutionRequest request, ResolutionOptions options) {
        return getPackageVersions(request.orgName(), request.packageName(), request.version().orElse(null));
    }

    @Override
    public Map<String, List<String>> getPackages() {
        Map<String, List<String>> packageMap = new HashMap<>();
        for (Project project : members()) {
            Package pkg = project.currentPackage();
            String pkgEntry = pkg.descriptor().name() + ":" +
                    pkg.descriptor().version();
            if (!packageMap.containsKey(pkg.packageOrg().toString())) {
                packageMap.put(pkg.packageOrg().toString(), new ArrayList<>());
            }
            packageMap.get(pkg.packageOrg().toString()).add(pkgEntry);
        }
        return packageMap;
    }
}
