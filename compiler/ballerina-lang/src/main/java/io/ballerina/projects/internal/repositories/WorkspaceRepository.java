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
import io.ballerina.projects.directory.WorkspaceProject;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class WorkspaceRepository extends AbstractPackageRepository {

    private final WorkspaceProject workspaceProject;

    public WorkspaceRepository(WorkspaceProject workspace) {
        this.workspaceProject = workspace;
    }

    @Override
    protected List<PackageVersion> getPackageVersions(PackageOrg org, PackageName name, PackageVersion version) {
        if (version == null) {
            return this.workspaceProject.projects().stream().filter(project ->
                    project.currentPackage().packageOrg().equals(org)
                            && project.currentPackage().packageName().equals(name)).findFirst().map(project ->
                    Collections.singletonList(project.currentPackage().packageVersion()))
                    .orElse(Collections.emptyList());
        }
        return this.workspaceProject.projects().stream().filter(project ->
                project.currentPackage().descriptor().equals(PackageDescriptor.from(org, name, version))).findFirst()
                .map(project -> Collections.singletonList(project.currentPackage().packageVersion()))
                .orElse(Collections.emptyList());
    }

    @Override
    protected DependencyGraph<PackageDescriptor> getDependencyGraph(
            PackageOrg org, PackageName name, PackageVersion version) {
        for (Project project : this.workspaceProject.projects()) {
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
            return this.workspaceProject.projects().stream()
                    .anyMatch(project -> project.currentPackage().packageOrg().equals(org)
                            && project.currentPackage().packageName().equals(name));

        }
        return this.workspaceProject.projects().stream()
                .anyMatch(project -> project.currentPackage().descriptor()
                .equals(PackageDescriptor.from(org, name, version)));
    }

    @Override
    public Collection<ModuleDescriptor> getModules(PackageOrg org, PackageName name, PackageVersion version) {

        if (version == null) {
            return this.workspaceProject.projects().stream().filter(project ->
                    project.currentPackage().packageOrg().equals(org)
                            && project.currentPackage().packageName().equals(name)).findFirst().map(project ->
                    project.currentPackage().moduleDependencyGraph().getNodes()).orElse(Collections.emptyList());
        }
        return this.workspaceProject.projects().stream().filter(project ->
                project.currentPackage().descriptor().equals(PackageDescriptor.from(org, name, version))).findFirst()
                .map(project -> project.currentPackage().moduleDependencyGraph().getNodes())
                .orElse(Collections.emptyList());
    }

    @Override
    public Optional<Package> getPackage(ResolutionRequest request, ResolutionOptions options) {
        if (request.version().isEmpty()) {
            return this.workspaceProject.projects().stream().filter(project ->
                    project.currentPackage().packageOrg().equals(request.orgName())
                            && project.currentPackage().packageName().equals(request.packageName())).findFirst()
                    .map(Project::currentPackage);
        }
        return this.workspaceProject.projects().stream().filter(project ->
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
        for (Project project : this.workspaceProject.projects()) {
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
