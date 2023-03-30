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
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.SemanticVersion.VersionCompatibilityResult;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

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

    // We are maintaining the raw graph for trouble shooting purposes.
    private final DependencyGraphBuilder<DependencyNode> rawGraphBuilder;
    private final ResolutionOptions resolutionOptions;

    private Set<Vertex> unresolvedVertices = new HashSet<>();

    private final DependencyNode rootDepNode;
    private final Vertex rootNodeVertex;

    private final List<Diagnostic> diagnosticList;

    public PackageDependencyGraphBuilder(PackageDescriptor rootNode, ResolutionOptions resolutionOptions) {
        diagnosticList = new ArrayList<>();
        this.rootNodeVertex = new Vertex(rootNode.org(), rootNode.name());
        this.rootDepNode = new DependencyNode(rootNode,
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE);
        this.resolutionOptions = resolutionOptions;
        this.rawGraphBuilder = DependencyGraphBuilder.getBuilder(rootDepNode);

        Vertex dependentVertex = new Vertex(rootDepNode.pkgDesc().org(), rootDepNode.pkgDesc().name());
        addNewVertex(dependentVertex, rootDepNode, false);
    }

    public NodeStatus addUnresolvedNode(PackageDescriptor node,
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

    public NodeStatus addErrorNode(PackageDescriptor node,
                                   PackageDependencyScope scope,
                                   DependencyResolutionType dependencyResolvedType) {
        // Add the correct version of the dependent to the graph.
        Vertex dependentVertex = new Vertex(node.org(), node.name());
        return addNewVertex(dependentVertex,
                new DependencyNode(node, scope, dependencyResolvedType, true), false);
    }

    public NodeStatus addUnresolvedDependency(PackageDescriptor dependent,
                                              PackageDescriptor dependency,
                                              PackageDependencyScope dependencyScope,
                                              DependencyResolutionType dependencyResolvedType) {
        return addDependencyInternal(dependent,
                new DependencyNode(dependency, dependencyScope, dependencyResolvedType),
                true);
    }

    public NodeStatus addErroneousDependency(PackageDescriptor dependent,
                                              PackageDescriptor dependency,
                                              PackageDependencyScope dependencyScope,
                                              DependencyResolutionType dependencyResolvedType) {
        return addDependencyInternal(dependent,
                new DependencyNode(dependency, dependencyScope, dependencyResolvedType, true),
                false);
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
        return dependencyNode.pkgDesc().version().equals(node.version());
    }

    public Collection<DependencyNode> getAllDependencies() {
        return vertices.values().stream()
                .filter(vertex -> !vertex.equals(rootDepNode))
                .collect(Collectors.toList());
    }

    public DependencyGraph<DependencyNode> buildGraph() {
        // Remove dangling nodes in the graph
        removeDanglingNodes();

        DependencyGraphBuilder<DependencyNode> graphBuilder = DependencyGraphBuilder.getBuilder(rootDepNode);
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

    public Collection<DependencyNode> getUnresolvedNodes() {
        Collection<DependencyNode> unresolvedNodes = unresolvedVertices.stream()
                .map(vertices::get)
                .collect(Collectors.toList());
        this.unresolvedVertices = new HashSet<>();
        return unresolvedNodes;
    }

    List<Diagnostic> diagnostics() {
        return diagnosticList;
    }

    /**
     * Clean up the dependency graph by cleaning up dangling dependencies.
     */
    public void removeDanglingNodes() {
        Set<Vertex> danglingVertices = new HashSet<>(vertices.keySet());
        removeDanglingNodes(rootNodeVertex, danglingVertices);

        for (Vertex danglingVertex : danglingVertices) {
            vertices.remove(danglingVertex);
            depGraph.remove(danglingVertex);
            unresolvedVertices.remove(danglingVertex);
        }
    }

    public DependencyGraph<DependencyNode> rawGraph() {
        return rawGraphBuilder.build();
    }

    void addUnresolvedDirectDepToRawGraph(DependencyNode unresolvedDirectDep) {
        rawGraphBuilder.addDependency(this.rootDepNode, unresolvedDirectDep);
    }

    private NodeStatus addDependencyInternal(PackageDescriptor dependent,
                                             DependencyNode dependencyNode,
                                             boolean unresolved) {
        // Add the correct version of the dependent to the graph.
        Vertex dependentVertex = new Vertex(dependent.org(), dependent.name());
        if (!depGraph.containsKey(dependentVertex)) {
            throw new IllegalStateException("Dependent node does not exist in the graph: " + dependent);
        }

        Vertex dependencyVertex = new Vertex(dependencyNode.pkgDesc().org(), dependencyNode.pkgDesc().name());
        NodeStatus nodeStatus = addNewVertex(dependencyVertex, dependencyNode, unresolved);
        depGraph.get(dependentVertex).add(dependencyVertex);

        // Recording every dependency relation in raw graph for troubleshooting purposes.
        if (resolutionOptions.dumpRawGraphs()) {
            DependencyNode dependentNode = vertices.get(dependentVertex);
            rawGraphBuilder.addDependency(dependentNode, dependencyNode);
        }
        return nodeStatus;
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

    /**
     * Adds a new vertex if it is not added to the graph yet.
     * Replaces/rejects the new vertex by comparing with the existing node
     * in the dependency graph. When accepting or rejecting the new vertex the
     * following rules are followed:
     * 1. If the existing node is an erroneous node, reject the new node.
     * 2. If the new node is an erroneous node, accept it.
     * 3. If there new node version is incompatible with the existing,
     *    create an error node and add to the graph.
     * 4. If the new node version is greater than the existing, accept it. Else reject.
     *
     * @param vertex existing vertex in the graph
     * @param newPkgDep new node to compare
     * @param unresolved whether the vertex is unresolved
     * @return
     */
    private NodeStatus addNewVertex(Vertex vertex, DependencyNode newPkgDep, boolean unresolved) {
        // Adding every node to the raw graph
        if (resolutionOptions.dumpRawGraphs()) {
            rawGraphBuilder.add(newPkgDep);
        }

        if (!vertices.containsKey(vertex)) {
            vertices.put(vertex, newPkgDep);
            depGraph.put(vertex, new HashSet<>());

            // TODO Re think about the unresolved Property
            if (unresolved) {
                unresolvedVertices.add(vertex);
            }
            return NodeStatus.ACCEPTED;
        }

        // There exists another version in the graph.
        DependencyNode existingPkgDep = vertices.get(vertex);

        // If the existing dependency is an error node, we continue with the error node.
        if (existingPkgDep.errorNode()) {
            return NodeStatus.REJECTED;
        }

        DependencyNode resolvedPkgDep;
        NodeStatus nodeStatus;

        if (newPkgDep.errorNode()) {
            resolvedPkgDep = newPkgDep;
            nodeStatus = NodeStatus.ACCEPTED;
        } else {
            PackageDescriptor resolvedPkgDesc = handleDependencyConflict(newPkgDep, existingPkgDep);
            if (resolvedPkgDesc == null) { // A version conflict exists. We add the a new error node.
                resolvedPkgDep = new DependencyNode(
                        existingPkgDep.pkgDesc(),
                        existingPkgDep.scope(),
                        existingPkgDep.resolutionType(),
                        true);
                nodeStatus = NodeStatus.ACCEPTED;
            } else {
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

                resolvedPkgDep = new DependencyNode(resolvedPkgDesc, depScope, resolutionType);
                nodeStatus = getNodeStatus(vertex, existingPkgDep, newPkgDep, unresolved);
            }
        }

        // Accept or reject the new package dependency depending on the node status
        if (nodeStatus == NodeStatus.ACCEPTED) {
            // Update the vertex with the new version
            vertices.put(vertex, resolvedPkgDep);
            // The dependencies of the current version is no longer valid.
            depGraph.put(vertex, new HashSet<>());
        } else {
            // Update the vertex anyway
            // This step will update the correct scope, resolution type and repository
            vertices.put(vertex, resolvedPkgDep);
        }
        return nodeStatus;
    }

    private NodeStatus getNodeStatus(Vertex vertex,
                                     DependencyNode existingPkgDep,
                                     DependencyNode newPkgDep,
                                     boolean unresolved) {
        if (newPkgDep.equals(existingPkgDep)) {
            return NodeStatus.ACCEPTED;
        }

        SemanticVersion newSemVer = newPkgDep.pkgDesc().version().value();
        SemanticVersion existingSemVer = existingPkgDep.pkgDesc().version().value();
        if (newSemVer.greaterThan(existingSemVer)) {
            if (unresolved) {
                // Mark it as unresolved because there is version change
                unresolvedVertices.add(vertex);
            }
            return NodeStatus.ACCEPTED;
        } else if (newSemVer.lessThan(existingSemVer)) {
            return NodeStatus.REJECTED;
        } else {
            // We have the same package version in existing dependency and in the resolved dependency
            // Let's compare the package repository
            Optional<String> newRepoOptional = newPkgDep.pkgDesc().repository();
            Optional<String> existingRepoOptional = existingPkgDep.pkgDesc().repository();
            if (newRepoOptional.isPresent() &&
                    newRepoOptional.get().equals(ProjectConstants.LOCAL_REPOSITORY_NAME)) {
                // New package is coming from the local repository. Accept the new one
                return NodeStatus.ACCEPTED;
            }

            if (existingRepoOptional.isPresent() &&
                    existingRepoOptional.get().equals(ProjectConstants.LOCAL_REPOSITORY_NAME)) {
                // New package is not coming from the local repository and the
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
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                        ProjectDiagnosticErrorCode.INCOMPATIBLE_DEPENDENCY_VERSIONS.diagnosticId(),
                        "Two incompatible versions exist in the dependency graph: " +
                                existingPkgDesc.org() + "/" + existingPkgDesc.name() +
                                " versions: " + existingPkgDesc.version() + ", " + newPkgDesc.version(),
                        DiagnosticSeverity.ERROR);
                PackageDiagnostic diagnostic = new PackageDiagnostic(
                        diagnosticInfo, this.rootDepNode.pkgDesc().name().toString());
                diagnosticList.add(diagnostic);
                return null;
            default:
                throw new IllegalStateException("Unsupported VersionCompatibilityResult: " + compatibilityResult);
        }
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
