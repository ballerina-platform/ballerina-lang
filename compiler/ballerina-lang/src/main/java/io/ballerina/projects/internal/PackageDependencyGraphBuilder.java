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
    private final Map<GraphNode, PackageDescriptor> graphNodes = new HashMap<>();
    private final Map<GraphNode, Set<GraphNode>> dependenciesMap = new HashMap<>();

    public static PackageDependencyGraphBuilder getInstance() {
        // TODO Pass the diagnostic collector with the constructor.
        return new PackageDependencyGraphBuilder();
    }

    public PackageDependencyGraphBuilder addNode(PackageDescriptor pkgDesc) {
        GraphNode newNode = new GraphNode(pkgDesc, PackageDependencyScope.DEFAULT);
        addInternal(newNode);
        return this;
    }

    public PackageDependencyGraphBuilder addTestNode(PackageDescriptor pkgDesc) {
        GraphNode newNode = new GraphNode(pkgDesc, PackageDependencyScope.TEST_ONLY);
        addInternal(newNode);
        return this;
    }

    public PackageDependencyGraphBuilder addDependency(PackageDescriptor dependent,
                                                       PackageDescriptor dependency) {
        // Add the correct version of the dependent to the graph.
        GraphNode dependentNode = new GraphNode(dependent, PackageDependencyScope.DEFAULT);
        addInternal(dependentNode);

        // Add the correct version of the dependency to the graph.
        addDependencyInternal(dependentNode, new GraphNode(dependency, PackageDependencyScope.DEFAULT));
        return this;
    }

    public PackageDependencyGraphBuilder addTestDependency(PackageDescriptor dependent,
                                                           PackageDescriptor dependency) {
        // Add the correct version of the dependent to the graph.
        GraphNode dependentNode = new GraphNode(dependent, PackageDependencyScope.TEST_ONLY);
        addInternal(dependentNode);

        // Add the correct version of the dependency to the graph.
        addDependencyInternal(dependentNode, new GraphNode(dependency, PackageDependencyScope.TEST_ONLY));
        return this;
    }

    public PackageDependencyGraphBuilder addDependencies(PackageDescriptor dependent,
                                                         Collection<PackageDescriptor> dependencies) {
        // Add the correct version of the dependent to the graph.
        GraphNode dependentNode = new GraphNode(dependent, PackageDependencyScope.DEFAULT);
        addInternal(dependentNode);

        for (PackageDescriptor dependency : dependencies) {
            // Add the correct version of the dependency to the graph.
            addDependencyInternal(dependentNode, new GraphNode(dependency, PackageDependencyScope.DEFAULT));
        }
        return this;
    }

    public PackageDependencyGraphBuilder addTestDependencies(PackageDescriptor dependent,
                                                             Collection<PackageDescriptor> dependencies) {
        // Add the correct version of the dependent to the graph.
        GraphNode dependentNode = new GraphNode(dependent, PackageDependencyScope.TEST_ONLY);
        addInternal(dependentNode);

        for (PackageDescriptor dependency : dependencies) {
            // Add the correct version of the dependency to the graph.
            addDependencyInternal(dependentNode, new GraphNode(dependency, PackageDependencyScope.TEST_ONLY));
        }
        return this;
    }

    public PackageDependencyGraphBuilder mergeGraph(DependencyGraph<PackageDescriptor> theirGraph) {
        for (PackageDescriptor theirPkgDesc : theirGraph.getNodes()) {
            Collection<PackageDescriptor> theirPkgDescDeps = theirGraph.getDirectDependencies(theirPkgDesc);
            addDependencies(theirPkgDesc, theirPkgDescDeps);
        }
        return this;
    }

    public PackageDependencyGraphBuilder mergeTestDependencyGraph(DependencyGraph<PackageDescriptor> theirGraph) {
        for (PackageDescriptor theirPkgDesc : theirGraph.getNodes()) {
            Collection<PackageDescriptor> theirPkgDescDeps = theirGraph.getDirectDependencies(theirPkgDesc);
            addTestDependencies(theirPkgDesc, theirPkgDescDeps);
        }
        return this;
    }

    public DependencyGraph<PackageDescriptor> build() {
        DependencyGraphBuilder<PackageDescriptor> graphBuilder = DependencyGraphBuilder.getBuilder();
        for (Map.Entry<GraphNode, Set<GraphNode>> dependencyMapEntry : dependenciesMap.entrySet()) {
            GraphNode graphNodeKey = dependencyMapEntry.getKey();
            Set<GraphNode> graphNodeValues = dependencyMapEntry.getValue();

            PackageDescriptor pkgDescKey = graphNodes.get(graphNodeKey);
            Set<PackageDescriptor> pkgDescValues;
            if (graphNodeValues.isEmpty()) {
                pkgDescValues = Collections.emptySet();
            } else {
                pkgDescValues = new HashSet<>(graphNodeValues.size());
                for (GraphNode graphNodeValue : graphNodeValues) {
                    pkgDescValues.add(graphNodes.get(graphNodeValue));
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
        GraphNode rooPkgGraphNode = new GraphNode(rootPkgDesc, PackageDependencyScope.DEFAULT);
        Set<GraphNode> directDependencyNodes = dependenciesMap.get(rooPkgGraphNode);
        for (GraphNode directDependencyNode : directDependencyNodes) {
            Optional<Package> optionalPackage = packageCache.getPackage(directDependencyNode.org,
                    directDependencyNode.name, directDependencyNode.pkgDesc.version());
            if (optionalPackage.isPresent()) {
                packageDependencyMap.put(directDependencyNode.pkgDesc,
                        new ResolvedPackageDependency(optionalPackage.get(), directDependencyNode.scope));
            }
            // I am ignoring the direct dependency missing cases as it is handled by the SymbolEnter
        }

        // Get all transitive dependencies
        List<GraphNode> transitiveDependencyNodes = dependenciesMap.keySet()
                .stream()
                .filter(keyNode -> !keyNode.equals(rooPkgGraphNode))
                .filter(keyNode -> !directDependencyNodes.contains(keyNode))
                .collect(Collectors.toList());

        // Resolve transitive dependencies
        List<ResolutionRequest> resolutionRequests = new ArrayList<>();
        for (GraphNode transitiveDependencyNode : transitiveDependencyNodes) {
            PackageDescriptor pkgDesc = graphNodes.get(transitiveDependencyNode);
            resolutionRequests.add(ResolutionRequest.from(pkgDesc, transitiveDependencyNode.scope));
        }

        List<ResolutionResponse> resolutionResponses = packageResolver.resolvePackages(
                resolutionRequests, currentProject);

        int transitiveNodeIndex = 0;
        for (ResolutionResponse resolutionResponse : resolutionResponses) {
            GraphNode transitiveDepNode = transitiveDependencyNodes.get(transitiveNodeIndex);
            if (resolutionResponse.resolutionStatus() == ResolutionStatus.UNRESOLVED) {
                // TODO collect this as a diagnostic and move on to the next one.
                throw new ProjectException("Transitive dependency cannot be found:" +
                        " org=" + transitiveDepNode.org +
                        ", package=" + transitiveDepNode.name +
                        ", version=" + transitiveDepNode.pkgDesc.version());
            }

            packageDependencyMap.put(transitiveDepNode.pkgDesc, new ResolvedPackageDependency(
                    resolutionResponse.resolvedPackage(), transitiveDepNode.scope));
            transitiveNodeIndex++;
        }

        DependencyGraphBuilder<ResolvedPackageDependency> depGraphBuilder = DependencyGraphBuilder.getBuilder();
        for (Map.Entry<GraphNode, Set<GraphNode>> graphNodeEntrySet : dependenciesMap.entrySet()) {
            GraphNode graphNode = graphNodeEntrySet.getKey();
            ResolvedPackageDependency directPackageDep = packageDependencyMap.get(graphNode.pkgDesc);
            if (directPackageDep == null) {
                continue;
            }

            List<ResolvedPackageDependency> resolvedTransitiveDeps = new ArrayList<>();
            Set<GraphNode> transitiveDepGraphNodes = graphNodeEntrySet.getValue();
            for (GraphNode transitiveDepGraphNode : transitiveDepGraphNodes) {
                ResolvedPackageDependency transitivePackageDep =
                        packageDependencyMap.get(transitiveDepGraphNode.pkgDesc);
                if (transitivePackageDep == null) {
                    continue;
                }
                resolvedTransitiveDeps.add(transitivePackageDep);
            }
            depGraphBuilder.addDependencies(directPackageDep, resolvedTransitiveDeps);
        }

        return depGraphBuilder.build();
    }

    private void addInternal(GraphNode newNode) {
        PackageDescriptor newPkgDesc = newNode.pkgDesc;
        PackageDescriptor existingPkgDesc = graphNodes.get(newNode);
        if (existingPkgDesc == null) {
            graphNodes.put(newNode, newPkgDesc);
            dependenciesMap.put(newNode, new HashSet<>());
            return;
        }

        // There exists a PackageDesc with the same org name and package name.
        // This could be a version conflict.
        if (newNode.scope == PackageDependencyScope.TEST_ONLY) {
            // Ignore the test scope dependency here
            // TODO handle the case where both the existing node and the new node have the TEST_ONLY scope.
            //  in such situations, we need to pick the latest one.
            return;
        }

        handleVersionCompatibility(existingPkgDesc, newPkgDesc, newNode);
    }

    private void addDependencyInternal(GraphNode dependentNode, GraphNode dependencyNode) {
        addInternal(dependencyNode);

        // Record the dependency
        dependenciesMap.get(dependentNode).add(dependencyNode);
    }


    private void handleVersionCompatibility(PackageDescriptor existingPkgDesc,
                                            PackageDescriptor newPkgDesc,
                                            GraphNode graphNode) {
        PackageVersion existingVersion = existingPkgDesc.version();
        PackageVersion newVersion = newPkgDesc.version();
        VersionCompatibilityResult versionCompatibilityResult = existingVersion.compareTo(newVersion);
        if (versionCompatibilityResult == VersionCompatibilityResult.INCOMPATIBLE) {
            // Incompatible versions exist in the graph.
            throw new ProjectException("Two incompatible versions exist in the dependency graph: " +
                    newPkgDesc.org() + "/" + newPkgDesc.name() + " versions: " +
                    existingVersion + ", " + newVersion);
        }

        SemanticVersion semVerOld = existingVersion.value();
        SemanticVersion semVerNew = newVersion.value();
        if (semVerNew.greaterThan(semVerOld)) {
            // The new version is higher than the existing version
            graphNodes.put(graphNode, newPkgDesc);
        }
    }

    private static class GraphNode {
        private final PackageOrg org;
        private final PackageName name;
        private final PackageDescriptor pkgDesc;
        private final PackageDependencyScope scope;

        private GraphNode(PackageDescriptor pkgDesc, PackageDependencyScope scope) {
            this.org = pkgDesc.org();
            this.name = pkgDesc.name();
            this.pkgDesc = pkgDesc;
            this.scope = scope;
        }

        @Override
        public boolean equals(Object otherNode) {
            if (this == otherNode) {
                return true;
            }
            if (otherNode == null || getClass() != otherNode.getClass()) {
                return false;
            }

            GraphNode otherGraphNode = (GraphNode) otherNode;
            return org.equals(otherGraphNode.org) && name.equals(otherGraphNode.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(org, name);
        }
    }
}
