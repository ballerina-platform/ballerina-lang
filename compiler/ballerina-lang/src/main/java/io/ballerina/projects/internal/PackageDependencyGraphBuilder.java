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
package io.ballerina.projects.internal;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DependencyGraph.DependencyGraphBuilder;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.SemanticVersion.VersionCompatibilityResult;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.environment.ResolutionResponse.ResolutionStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is responsible for creating the Package dependency graph with no version conflicts.
 * <p>
 * Version conflict resolution logic is built into this graph build process.
 *
 * @since 2.0.0
 */
public class PackageDependencyGraphBuilder {
    private final Map<Vertex, StaticPackageDependency> vertices = new HashMap<>();
    private final Map<Vertex, Set<Vertex>> depGraph = new HashMap<>();

    public PackageDependencyGraphBuilder(PackageDescriptor rootNode) {
        this.addNode(rootNode, PackageDependencyScope.DEFAULT);
    }

    public PackageDependencyGraphBuilder() {
    }

    public PackageDependencyGraphBuilder addNode(PackageDescriptor node,
                                                 PackageDependencyScope scope) {
        // Add the correct version of the dependent to the graph.
        Vertex dependentVertex = new Vertex(node.org(), node.name());
        addNewVertex(dependentVertex, new StaticPackageDependency(node, scope));
        return this;
    }

    public PackageDependencyGraphBuilder addDependency(PackageDescriptor dependent,
                                                       PackageDescriptor dependency,
                                                       PackageDependencyScope dependencyScope) {
        // Add the correct version of the dependent to the graph.
        Vertex dependentVertex = new Vertex(dependent.org(), dependent.name());
        if (!depGraph.containsKey(dependentVertex)) {
            throw new IllegalStateException("Dependent node does not exist in the graph: " + dependent);
        }

        Vertex dependencyVertex = new Vertex(dependency.org(), dependency.name());
        addNewVertex(dependencyVertex, new StaticPackageDependency(dependency, dependencyScope));
        depGraph.get(dependentVertex).add(dependencyVertex);
        return this;
    }

    public PackageDependencyGraphBuilder addDependencies(PackageDescriptor dependent,
                                                         Collection<PackageDescriptor> dependencies,
                                                         PackageDependencyScope dependencyScope) {
        // Add the correct version of the dependent to the graph.
        Vertex dependentVertex = new Vertex(dependent.org(), dependent.name());
        if (!depGraph.containsKey(dependentVertex)) {
            throw new IllegalStateException("Dependent node does not exist in the graph: " + dependent);
        }

        for (PackageDescriptor dependency : dependencies) {
            // Add the correct version of the dependency to the graph.
            Vertex dependencyVertex = new Vertex(dependency.org(), dependency.name());
            addNewVertex(dependencyVertex, new StaticPackageDependency(dependency, dependencyScope));
            depGraph.get(dependentVertex).add(dependencyVertex);
        }
        return this;
    }

    public PackageDependencyGraphBuilder mergeGraph(DependencyGraph<PackageDescriptor> theirGraph,
                                                    PackageDependencyScope scope) {
        for (PackageDescriptor theirPkgDesc : theirGraph.getNodes()) {
            addNode(theirPkgDesc, scope);
            Collection<PackageDescriptor> theirPkgDescDeps = theirGraph.getDirectDependencies(theirPkgDesc);
            addDependencies(theirPkgDesc, theirPkgDescDeps, scope);
        }
        return this;
    }

    public DependencyGraph<PackageDescriptor> build() {
        DependencyGraphBuilder<PackageDescriptor> graphBuilder =
                DependencyGraphBuilder.getBuilder();
        for (Map.Entry<Vertex, Set<Vertex>> dependencyMapEntry : depGraph.entrySet()) {
            Vertex graphNodeKey = dependencyMapEntry.getKey();
            Set<Vertex> graphNodeValues = dependencyMapEntry.getValue();

            PackageDescriptor pkgDescKey = vertices.get(graphNodeKey).pkgDesc;
            Set<PackageDescriptor> pkgDescValues;
            if (graphNodeValues.isEmpty()) {
                pkgDescValues = Collections.emptySet();
            } else {
                pkgDescValues = new HashSet<>(graphNodeValues.size());
                for (Vertex vertex : graphNodeValues) {
                    pkgDescValues.add(vertices.get(vertex).pkgDesc);
                }
            }

            graphBuilder.addDependencies(pkgDescKey, pkgDescValues);
        }
        return graphBuilder.build();
    }

    public DependencyGraph<ResolvedPackageDependency> buildPackageDependencyGraph(PackageDescriptor rootPkgDesc,
                                                                                  PackageResolver packageResolver,
                                                                                  PackageCache packageCache,
                                                                                  Project currentProject) {
        // TODO The following algorithm can be improved a lot.
        Map<PackageDescriptor, ResolvedPackageDependency> packageDependencyMap = new HashMap<>();
        // Add the root package to the map.
        packageDependencyMap.put(rootPkgDesc, new ResolvedPackageDependency(currentProject.currentPackage(),
                PackageDependencyScope.DEFAULT));

        // These direct dependencies are already resolved
        Vertex rooPkgGraphNode = new Vertex(rootPkgDesc.org(), rootPkgDesc.name());
        Set<Vertex> directDependencyNodes = depGraph.get(rooPkgGraphNode);
        for (Vertex directDependencyNode : directDependencyNodes) {
            StaticPackageDependency directPkgDep = vertices.get(directDependencyNode);
            Optional<Package> optionalPackage = packageCache.getPackage(directDependencyNode.org,
                    directDependencyNode.name, directPkgDep.pkgDesc.version());
            if (optionalPackage.isPresent()) {
                packageDependencyMap.put(directPkgDep.pkgDesc,
                        new ResolvedPackageDependency(optionalPackage.get(), directPkgDep.scope));
            }
            // I am ignoring the direct dependency missing cases as it is handled by the SymbolEnter
        }

        // Get all transitive dependencies
        List<Vertex> transitiveDependencyNodes = vertices.keySet()
                .stream()
                .filter(keyNode -> !keyNode.equals(rooPkgGraphNode))
                .filter(keyNode -> !directDependencyNodes.contains(keyNode))
                .collect(Collectors.toList());

        // Resolve transitive dependencies
        List<ResolutionRequest> resolutionRequests = new ArrayList<>();
        for (Vertex transitiveDependencyNode : transitiveDependencyNodes) {
            StaticPackageDependency transitivePkgDep = vertices.get(transitiveDependencyNode);
            resolutionRequests.add(ResolutionRequest.from(transitivePkgDep.pkgDesc, transitivePkgDep.scope));
        }

        List<ResolutionResponse> resolutionResponses = packageResolver.resolvePackages(
                resolutionRequests, currentProject);

        int transitiveNodeIndex = 0;
        for (ResolutionResponse resolutionResponse : resolutionResponses) {
            Vertex transitiveDepNode = transitiveDependencyNodes.get(transitiveNodeIndex);
            StaticPackageDependency transitivePkgDep = vertices.get(transitiveDepNode);
            if (resolutionResponse.resolutionStatus() == ResolutionStatus.UNRESOLVED) {
                // TODO collect this as a diagnostic and move on to the next one.
                throw new ProjectException("Transitive dependency cannot be found:" +
                        " org=" + transitiveDepNode.org +
                        ", package=" + transitiveDepNode.name +
                        ", version=" + transitivePkgDep.pkgDesc.version());
            }

            packageDependencyMap.put(transitivePkgDep.pkgDesc, new ResolvedPackageDependency(
                    resolutionResponse.resolvedPackage(), transitivePkgDep.scope));
            transitiveNodeIndex++;
        }

        DependencyGraphBuilder<ResolvedPackageDependency> depGraphBuilder = DependencyGraphBuilder.getBuilder();
        for (Map.Entry<Vertex, Set<Vertex>> graphNodeEntrySet : depGraph.entrySet()) {
            Vertex graphNode = graphNodeEntrySet.getKey();
            StaticPackageDependency staticPkgDep = vertices.get(graphNode);
            ResolvedPackageDependency directPackageDep = packageDependencyMap.get(staticPkgDep.pkgDesc);
            if (directPackageDep == null) {
                continue;
            }

            List<ResolvedPackageDependency> resolvedTransitiveDeps = new ArrayList<>();
            Set<Vertex> transitiveDepGraphNodes = graphNodeEntrySet.getValue();
            for (Vertex transitiveDepGraphNode : transitiveDepGraphNodes) {
                StaticPackageDependency transitivePkgDep = vertices.get(transitiveDepGraphNode);
                ResolvedPackageDependency transitivePackageDep =
                        packageDependencyMap.get(transitivePkgDep.pkgDesc);
                if (transitivePackageDep == null) {
                    continue;
                }
                resolvedTransitiveDeps.add(transitivePackageDep);
            }
            depGraphBuilder.addDependencies(directPackageDep, resolvedTransitiveDeps);
        }

        return depGraphBuilder.build();
    }

    private void addNewVertex(Vertex vertex, StaticPackageDependency newPkgDescWithScope) {
        if (!vertices.containsKey(vertex)) {
            vertices.put(vertex, newPkgDescWithScope);
            depGraph.put(vertex, new HashSet<>());
            return;
        }

        // If the node is already in the graph:
        // #new-scope #old-scope #solution
        // Default Default Pick the latest version and update the graph node with scope Default
        // Test Test Pick the latest version and update the graph node with scope Test
        // Default Test Pick the default version and update the graph node with scope Default
        // Test Default Do nothing
        StaticPackageDependency olgPkgDescWithScope = vertices.get(vertex);
        PackageDependencyScope oldScope = olgPkgDescWithScope.scope;
        PackageDependencyScope newScope = newPkgDescWithScope.scope;

        if (newScope == oldScope && newScope == PackageDependencyScope.DEFAULT) {
            // Pick the latest version and update the graph node with scope Default
            updateGraphWithLatestCompatibleVersion(olgPkgDescWithScope, newPkgDescWithScope, vertex);
        } else if (newScope == oldScope && newScope == PackageDependencyScope.TEST_ONLY) {
            // Pick the latest version and update the graph node with scope Test
            updateGraphWithLatestCompatibleVersion(olgPkgDescWithScope, newPkgDescWithScope, vertex);
        } else if (newScope == PackageDependencyScope.DEFAULT && oldScope == PackageDependencyScope.TEST_ONLY) {
            // Pick the default version and update the graph node with scope Default
            vertices.put(vertex, newPkgDescWithScope);
        } else if (newScope == PackageDependencyScope.TEST_ONLY && oldScope == PackageDependencyScope.DEFAULT) {
            // TODO do nothing
        } else {
            // This branch cannot be executed
            throw new ProjectException("Invalid package dependency scopes");
        }
    }

    private void updateGraphWithLatestCompatibleVersion(StaticPackageDependency olgPkgDescWithScope,
                                                        StaticPackageDependency newPkgDescWithScope,
                                                        Vertex vertex) {
        PackageVersion existingVersion = olgPkgDescWithScope.pkgDesc.version();
        PackageVersion newVersion = newPkgDescWithScope.pkgDesc.version();
        VersionCompatibilityResult versionCompatibilityResult = existingVersion.compareTo(newVersion);
        if (versionCompatibilityResult == VersionCompatibilityResult.INCOMPATIBLE) {
            // Incompatible versions exist in the graph.
            throw new ProjectException("Two incompatible versions exist in the dependency graph: " +
                    olgPkgDescWithScope.pkgDesc.org() + "/" + newPkgDescWithScope.pkgDesc.name() + " versions: " +
                    existingVersion + ", " + newVersion);
        }

        SemanticVersion semVerOld = existingVersion.value();
        SemanticVersion semVerNew = newVersion.value();
        if (semVerNew.greaterThan(semVerOld)) {
            // The new version is higher than the existing version
            vertices.put(vertex, newPkgDescWithScope);
        }
    }

    private static class Vertex {
        private final PackageOrg org;
        private final PackageName name;

        public Vertex(PackageOrg org, PackageName name) {
            this.org = org;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Vertex vertex = (Vertex) o;
            return org.equals(vertex.org) && name.equals(vertex.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(org, name);
        }
    }

    /**
     * Package dependency that includes the {@code PackageDescriptor} and the {@code PackageDependencyScope}.
     *
     * @since 2.0.0
     */
    public static class StaticPackageDependency {
        private final PackageDescriptor pkgDesc;
        private final PackageDependencyScope scope;

        public StaticPackageDependency(PackageDescriptor pkgDesc, PackageDependencyScope scope) {
            this.pkgDesc = pkgDesc;
            this.scope = scope;
        }
    }
}
