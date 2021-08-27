/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.projects.DependencyResolutionType;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.SemanticVersion.VersionCompatibilityResult;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    // TODO how about a multi-level map here Map<PackageOrg, Map<PackageName, StaticPackageDependency>>
    private Map<Vertex, DependencyNode> vertices = new HashMap<>();
    private Map<Vertex, Set<Vertex>> depGraph = new HashMap<>();

    private Set<Vertex> dirtyVertices = new HashSet<>();

    // TODO can we make this final
    private DependencyNode rootDepNode;
    private Vertex rootNodeVertex;

    public PackageDependencyGraphBuilder() {
    }

    public PackageDependencyGraphBuilder(PackageDescriptor rootNode) {
        this.rootNodeVertex = new Vertex(rootNode.org(), rootNode.name());
        this.rootDepNode = new DependencyNode(rootNode,
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE);

        Vertex dependentVertex = new Vertex(rootDepNode.pkgDesc().org(), rootDepNode.pkgDesc().name());
        addNewVertex(dependentVertex, rootDepNode, false);
    }

    public boolean addNode(PackageDescriptor node,
                           PackageDependencyScope scope,
                           DependencyResolutionType dependencyResolvedType) {
        // Add the correct version of the dependent to the graph.
        Vertex dependentVertex = new Vertex(node.org(), node.name());
        return addNewVertex(dependentVertex,
                new DependencyNode(node, scope, dependencyResolvedType), true);
    }

    public boolean addResolvedNode(PackageDescriptor node,
                                   PackageDependencyScope scope,
                                   DependencyResolutionType dependencyResolvedType) {
        // Add the correct version of the dependent to the graph.
        Vertex dependentVertex = new Vertex(node.org(), node.name());
        return addNewVertex(dependentVertex,
                new DependencyNode(node, scope, dependencyResolvedType), false);
    }

    public boolean addDependency(PackageDescriptor dependent,
                                 PackageDescriptor dependency,
                                 PackageDependencyScope dependencyScope,
                                 DependencyResolutionType dependencyResolvedType) {
        return addDependencyInternal(dependent,
                new DependencyNode(dependency, dependencyScope, dependencyResolvedType),
                true);
    }

    public boolean addResolvedDependency(PackageDescriptor dependent,
                                         PackageDescriptor dependency,
                                         PackageDependencyScope dependencyScope,
                                         DependencyResolutionType dependencyResolvedType) {
        return addDependencyInternal(dependent,
                new DependencyNode(dependency, dependencyScope, dependencyResolvedType),
                false);
    }

    public boolean containsNode(PackageDescriptor node) {
        DependencyNode dependencyNode = vertices.get(new Vertex(node.org(), node.name()));
        if (dependencyNode == null) {
            return false;
        }

        return dependencyNode.pkgDesc().version().equals(node.version());
    }

    public Collection<DependencyNode> getAllDependencies() {
        return vertices.values().stream()
                .filter(vertex -> !vertex.equals(rootDepNode))
                .collect(Collectors.toList());
    }

    public DependencyGraph<DependencyNode> buildGraph() {
        DependencyGraphBuilder<DependencyNode> graphBuilder = DependencyGraphBuilder.getBuilder();
        for (Map.Entry<Vertex, Set<Vertex>> dependencyMapEntry : depGraph.entrySet()) {
            Vertex graphNodeKey = dependencyMapEntry.getKey();
            Set<Vertex> graphNodeValues = dependencyMapEntry.getValue();

            DependencyNode pkgDescKey = vertices.get(graphNodeKey);
            Set<DependencyNode> pkgDescValues;
            if (graphNodeValues.isEmpty()) {
                pkgDescValues = Collections.emptySet();
            } else {
                pkgDescValues = new HashSet<>(graphNodeValues.size());
                for (Vertex vertex : graphNodeValues) {
                    pkgDescValues.add(vertices.get(vertex));
                }
            }

            graphBuilder.addDependencies(pkgDescKey, pkgDescValues);
        }
        return graphBuilder.build();
    }

    public Collection<DependencyNode> cleanUnresolvedNodes() {
        // Remove dangling nodes in the graph
        removeDanglingNodes();

        Collection<DependencyNode> unresolvedNodes = dirtyVertices.stream()
                .map(vertex -> vertices.get(vertex))
                .collect(Collectors.toList());
        this.dirtyVertices = new HashSet<>();
        return unresolvedNodes;
    }

    private boolean addDependencyInternal(PackageDescriptor dependent,
                                          DependencyNode dependencyNode,
                                          boolean markAsDirty) {
        // Skip langlib nodes if the root node is not a langlib
        if (!rootDepNode.pkgDesc().isLangLibPackage()) {
            if (dependencyNode.pkgDesc().isLangLibPackage()) {
                return true;
            }
        }
        // Add the correct version of the dependent to the graph.
        Vertex dependentVertex = new Vertex(dependent.org(), dependent.name());
        if (!depGraph.containsKey(dependentVertex)) {
            throw new IllegalStateException("Dependent node does not exist in the graph: " + dependent);
        }

        Vertex dependencyVertex = new Vertex(dependencyNode.pkgDesc().org(), dependencyNode.pkgDesc().name());
        boolean added = addNewVertex(dependencyVertex, dependencyNode, markAsDirty);
        depGraph.get(dependentVertex).add(dependencyVertex);
        return added;
    }

    /**
     * Clean up the dependency graph by cleaning up dangling dependencies.
     */
    private void removeDanglingNodes() {
        Set<Vertex> danglingVertices = new HashSet<>(vertices.keySet());
        removeDanglingNodes(rootNodeVertex, danglingVertices);

        for (Vertex danglingVertex : danglingVertices) {
            vertices.remove(danglingVertex);
            depGraph.remove(danglingVertex);
            dirtyVertices.remove(danglingVertex);
        }
    }

    private void removeDanglingNodes(Vertex nodeVertex, Set<Vertex> danglingVertices) {
        danglingVertices.remove(nodeVertex);
        Set<Vertex> dependencies = depGraph.get(nodeVertex);
        for (Vertex dep : dependencies) {
            if (!danglingVertices.contains(dep)) {
                continue;
            }
            removeDanglingNodes(dep, danglingVertices);
        }
    }

    private boolean addNewVertex(Vertex vertex, DependencyNode newPkgDep, boolean markAsDirty) {
        if (!vertices.containsKey(vertex)) {
            vertices.put(vertex, newPkgDep);
            depGraph.put(vertex, new HashSet<>());
            if (markAsDirty) {
                dirtyVertices.add(vertex);
            }
            return true;
        }

        // There exists another version in the graph.
        DependencyNode existingPkgDep = vertices.get(vertex);
        PackageDescriptor resolvedPkgDesc = handleDependencyConflict(newPkgDep, existingPkgDep);

        // If the existing dependency scope is DEFAULT, use it. Otherwise use the new dependency scope.
        PackageDependencyScope depScope =
                existingPkgDep.scope() == PackageDependencyScope.DEFAULT ?
                        PackageDependencyScope.DEFAULT :
                        newPkgDep.scope();

        // If the existing dependency scope is DEFAULT, use it. Otherwise use the new dependency scope.
        DependencyResolutionType resolutionType =
                existingPkgDep.resolutionType() == DependencyResolutionType.SOURCE ?
                        DependencyResolutionType.SOURCE :
                        newPkgDep.resolutionType();

        DependencyNode resolvedPkgDep = new DependencyNode(resolvedPkgDesc, depScope, resolutionType);
        return addNewVertex(vertex, existingPkgDep, resolvedPkgDep);
    }

    private boolean addNewVertex(Vertex vertex, DependencyNode existingPkgDep, DependencyNode resolvedPkgDep) {
        vertices.put(vertex, resolvedPkgDep);

        PackageVersion existingPkgDepVersion = existingPkgDep.pkgDesc().version();
        PackageVersion resolvedPkgDepVersion = resolvedPkgDep.pkgDesc().version();
        if (!existingPkgDepVersion.equals(resolvedPkgDepVersion)) {
            // TODO Check for minor version bump
            if (existingPkgDepVersion.value().minor() > resolvedPkgDepVersion.value().minor()) {
                // This this vertex as dirty anyway regardless of the markAsDirty attribute
                // This vertex will get updated soon.
                dirtyVertices.add(vertex);
            }

            // New version is has to be greater than the existing node's version in the graph.
            // Therefore the dependencies of the current version is no longer valid.
            depGraph.put(vertex, new HashSet<>());
            return true;
        } else {
            // We have the same package version in existing dependency and in the resolved dependency
            // If the existing repository is equal to the resolved repository, then we return false because
            //  there has been no change to the existing dependency
            // If the existing repository is different from the resolved repository, then there has been a change
            return !Objects.equals(existingPkgDep.pkgDesc().repository(), resolvedPkgDep.pkgDesc().repository());
        }
    }

    private PackageDescriptor handleDependencyConflict(DependencyNode newPkgDep,
                                                       DependencyNode existingPkgDep) {
        PackageDescriptor newPkgDesc = newPkgDep.pkgDesc();
        PackageDescriptor existingPkgDesc = existingPkgDep.pkgDesc();

        VersionCompatibilityResult compatibilityResult = newPkgDesc.version().compareTo(existingPkgDesc.version());
        switch (compatibilityResult) {
            case EQUAL:
                // Both packages have the same version
                // Give priority to the package coming from the local repository
                String repository = existingPkgDesc.repository().isPresent() ?
                        existingPkgDesc.repository().get() :
                        newPkgDesc.repository().orElse(null);
                return PackageDescriptor.from(existingPkgDesc.org(), existingPkgDesc.name(),
                        existingPkgDesc.version(), repository);
            case LESS_THAN:
                // New dependency version is less than the existing version
                // Use the existing package
                return existingPkgDesc;
            case GREATER_THAN:
                // New dependency version is greater than the existing version
                // Use the existing package
                return newPkgDesc;
            case INCOMPATIBLE:
                // Incompatible versions exist in the graph.
                // TODO can we report this issue with more information. dependency graph etc.
                // Convert this to a diagnostic
                throw new ProjectException("Two incompatible versions exist in the dependency graph: " +
                        existingPkgDesc.org() + "/" + existingPkgDesc.name() +
                        " versions: " + existingPkgDesc.version() + ", " + newPkgDesc.version());
            default:
                throw new IllegalStateException("Unsupported VersionCompatibilityResult: " + compatibilityResult);
        }
    }

    public DependencyGraph<ResolvedPackageDependency> buildPackageDependencyGraph(PackageResolver packageResolver,
                                                                                  Project rootProject,
                                                                                  boolean offline) {
        List<PackageDescriptor> packageDescriptors = new ArrayList<>();
        vertices.forEach((key, value) -> packageDescriptors.add(value.pkgDesc()));
        List<ResolutionResponse> resolutionResponses =
                packageResolver.resolvePackages(packageDescriptors, offline, rootProject);
        DependencyGraphBuilder<ResolvedPackageDependency> depGraphBuilder =
                DependencyGraphBuilder.getBuilder();

        for (Map.Entry<Vertex, Set<Vertex>> graphNodeEntrySet : depGraph.entrySet()) {
            Vertex graphNode = graphNodeEntrySet.getKey();
            DependencyNode pkgDep = vertices.get(graphNode);

            ResolutionResponse directDepResponse = findResolutionResponse(pkgDep.pkgDesc(), resolutionResponses);
            if (directDepResponse == null) {
                continue;
            }
            ResolvedPackageDependency directPackageDep = new ResolvedPackageDependency(
                    directDepResponse.resolvedPackage(),
                    pkgDep.scope(),
                    pkgDep.resolutionType());

            List<ResolvedPackageDependency> resolvedTransitiveDeps = new ArrayList<>();
            Set<Vertex> transitiveDepGraphNodes = graphNodeEntrySet.getValue();
            for (Vertex transitiveDepGraphNode : transitiveDepGraphNodes) {
                DependencyNode transitivePkgDep = vertices.get(transitiveDepGraphNode);

                ResolutionResponse transitiveDepResponse =
                        findResolutionResponse(transitivePkgDep.pkgDesc(), resolutionResponses);
                if (transitiveDepResponse == null) {
                    continue;
                }
                ResolvedPackageDependency transitivePackageDep =
                        new ResolvedPackageDependency(
                                transitiveDepResponse.resolvedPackage(),
                                transitivePkgDep.scope(),
                                transitivePkgDep.resolutionType());
                resolvedTransitiveDeps.add(transitivePackageDep);
            }
            depGraphBuilder.addDependencies(directPackageDep, resolvedTransitiveDeps);
        }
        return depGraphBuilder.build();
    }

    private ResolutionResponse findResolutionResponse(PackageDescriptor pkgDesc,
                                                      List<ResolutionResponse> resolutionResponses) {
        for (ResolutionResponse resolutionResponse : resolutionResponses) {
            if (resolutionResponse.resolutionStatus().equals(ResolutionResponse.ResolutionStatus.UNRESOLVED)) {
                continue;
            }
            if (resolutionResponse.responseDescriptor().equals(pkgDesc)) {
                return resolutionResponse;
            }
        }

        return null;
    }

    private static class Vertex {
        private final PackageOrg org;
        private final PackageName name;

        Vertex(PackageOrg org, PackageName name) {
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
}
