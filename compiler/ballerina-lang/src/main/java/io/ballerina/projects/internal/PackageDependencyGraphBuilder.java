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
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.SemanticVersion.VersionCompatibilityResult;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.environment.ResolutionResponse.ResolutionStatus;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;

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
    // TODO how about a multi-level map here Map<PackageOrg, Map<PackageName, StaticPackageDependency>>
    private final Map<Vertex, DependencyNode> vertices = new HashMap<>();
    private final Map<Vertex, Set<Vertex>> depGraph = new HashMap<>();

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

    public NodeStatus addNode(PackageDescriptor node,
                              PackageDependencyScope scope,
                              DependencyResolutionType dependencyResolvedType) {
        // Add the correct version of the dependent to the graph.
        Vertex dependentVertex = new Vertex(node.org(), node.name());
        return addNewVertex(dependentVertex,
                new DependencyNode(node, scope, dependencyResolvedType), true);
    }

    public NodeStatus addResolvedNode(PackageDescriptor node,
                                      PackageDependencyScope scope,
                                      DependencyResolutionType dependencyResolvedType) {
        // Add the correct version of the dependent to the graph.
        Vertex dependentVertex = new Vertex(node.org(), node.name());
        return addNewVertex(dependentVertex,
                new DependencyNode(node, scope, dependencyResolvedType), false);
    }

    public NodeStatus addDependency(PackageDescriptor dependent,
                                    PackageDescriptor dependency,
                                    PackageDependencyScope dependencyScope,
                                    DependencyResolutionType dependencyResolvedType) {
        return addDependencyInternal(dependent,
                new DependencyNode(dependency, dependencyScope, dependencyResolvedType),
                true);
    }

    public NodeStatus addResolvedDependency(PackageDescriptor dependent,
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

        if (!rootDepNode.pkgDesc().isLangLibPackage() && node.isBuiltInPackage()) {
            if (dependencyNode.pkgDesc().version() == null) {
                return false;
            }
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
                .map(vertices::get)
                .collect(Collectors.toList());
        this.dirtyVertices = new HashSet<>();
        return unresolvedNodes;
    }

    private NodeStatus addDependencyInternal(PackageDescriptor dependent,
                                             DependencyNode dependencyNode,
                                             boolean markAsDirty) {
        // Add the correct version of the dependent to the graph.
        Vertex dependentVertex = new Vertex(dependent.org(), dependent.name());
        if (!depGraph.containsKey(dependentVertex)) {
            throw new IllegalStateException("Dependent node does not exist in the graph: " + dependent);
        }

        Vertex dependencyVertex = new Vertex(dependencyNode.pkgDesc().org(), dependencyNode.pkgDesc().name());
        NodeStatus nodeStatus = addNewVertex(dependencyVertex, dependencyNode, markAsDirty);
        depGraph.get(dependentVertex).add(dependencyVertex);
        return nodeStatus;
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

    private NodeStatus addNewVertex(Vertex vertex, DependencyNode newPkgDep, boolean markAsDirty) {
        if (!vertices.containsKey(vertex)) {
            vertices.put(vertex, newPkgDep);
            depGraph.put(vertex, new HashSet<>());
            if (markAsDirty) {
                dirtyVertices.add(vertex);
            }
            return NodeStatus.ACCEPTED;
        }
        // There exists another version in the graph.
        if (!rootDepNode.pkgDesc().isLangLibPackage()
                && ProjectUtils.isBuiltInPackage(vertex.org, vertex.name.toString())) {
            // Built-in packages does not need version conflict resolution
            // since it is provided from the distribution
            vertices.put(vertex, newPkgDep);
            depGraph.put(vertex, new HashSet<>());
            return NodeStatus.ACCEPTED;
        }
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
        NodeStatus nodeStatus = addNewVertex(vertex, existingPkgDep, resolvedPkgDep);
        if (nodeStatus == NodeStatus.ACCEPTED) {
            // Update the vertex with the new version
            vertices.put(vertex, resolvedPkgDep);
            // The dependencies of the current version is no longer valid.
            depGraph.put(vertex, new HashSet<>());
        }
        return nodeStatus;
    }

    private NodeStatus addNewVertex(Vertex vertex, DependencyNode existingPkgDep, DependencyNode resolvedPkgDep) {
        if (resolvedPkgDep.equals(existingPkgDep)) {
            return NodeStatus.ACCEPTED;
        }

        SemanticVersion resolvedSemVer = resolvedPkgDep.pkgDesc().version().value();
        SemanticVersion existingSemVer = existingPkgDep.pkgDesc().version().value();
        if (resolvedSemVer.greaterThan(existingSemVer)) {
            // Mark it as dirty because there is version change
            dirtyVertices.add(vertex);
            return NodeStatus.ACCEPTED;
        } else if (resolvedSemVer.lessThan(existingSemVer)) {
            return NodeStatus.REJECTED;
        } else {
            // We have the same package version in existing dependency and in the resolved dependency
            // Let's compare the package repository
            Optional<String> resolvedRepoOptional = resolvedPkgDep.pkgDesc().repository();
            Optional<String> existingRepoOptional = existingPkgDep.pkgDesc().repository();
            if (resolvedRepoOptional.isPresent() &&
                    resolvedRepoOptional.get().equals(ProjectConstants.LOCAL_REPOSITORY_NAME)) {
                // Resolved package is coming from the local repository. Accept the new one
                return NodeStatus.ACCEPTED;
            }

            if (existingRepoOptional.isPresent() &&
                    existingRepoOptional.get().equals(ProjectConstants.LOCAL_REPOSITORY_NAME)) {
                // Resolved package is not coming from the local repository and the
                //  existing package is coming from the local repository. Reject the new one
                return NodeStatus.REJECTED;
            }

            // At this point,
            //  Both versions are same and both of them are not coming from the local repository
            //  But scope and/or the resolution type is different in the resolved version. Accept it.
            return NodeStatus.ACCEPTED;
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
            if (resolutionResponse.resolutionStatus().equals(ResolutionStatus.UNRESOLVED)) {
                continue;
            }
            if (resolutionResponse.responseDescriptor().equals(pkgDesc)) {
                return resolutionResponse;
            }
        }

        return null;
    }

    /**
     * Represents a Vertex in the DAG maintained by the PackageDependencyGraphBuilder.
     *
     * @since 2.0.0
     */
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

    /**
     * Indicates whether a node is added to the graph or not.
     *
     * @since 2.0.0
     */
    public enum NodeStatus {
        /**
         * Indicates that the node is added to the graph.
         * A node is accepted if:
         * 1) The graph does not new version,
         * 2) The new version is greater than the current version in the graph,
         * 3) Both versions are the same and the new version is coming from the local repo,
         * 4) Both versions are the same and both versions are not coming from the local repo,
         */
        ACCEPTED,

        /**
         * Indicates that the node is not added to the graph.
         * A node is rejected if:
         * 1) The new version is lower than the current version in the graph,
         * 2) Both versions are the same and the existing version is coming from the local repo
         * whereas the new version is not
         */
        REJECTED
    }
}
