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
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.ballerina.projects.DependencyResolutionType.SOURCE;
import static io.ballerina.projects.PackageDependencyScope.DEFAULT;

/**
 * This class is responsible for creating the Package dependency graph with no version conflicts.
 * <p>
 * Version conflict resolution logic is built into this graph build process.
 *
 * @since 2.0.0
 */
public class PackageDependencyGraphBuilder {
    private final Vertex rootVertex;
    private final DependencyNode rootDepNode;
    private final Map<Vertex, DependencyNode> vertices = new HashMap<>();
    private final Map<Vertex, Set<Vertex>> depGraph = new HashMap<>();
    private final PrintStream outStream = System.out;

    public PackageDependencyGraphBuilder(PackageDescriptor root) {
        rootVertex = new Vertex(root.org(), root.name());
        rootDepNode = new DependencyNode(root, DEFAULT, SOURCE);
        vertices.put(rootVertex, rootDepNode);
        depGraph.put(rootVertex, new HashSet<>());
    }

    public void addDirectDependency(DependencyNode targetNode) {
        PackageDescriptor target = targetNode.pkgDesc();
        Vertex targetVertex = new Vertex(target.org(), target.name());
        vertices.put(targetVertex, targetNode);
        depGraph.get(rootVertex).add(targetVertex);
        depGraph.put(targetVertex, new HashSet<>());
    }

    public void addVertex(DependencyNode node) {
        PackageDescriptor pkgDesc = node.pkgDesc();
        Vertex vertex = new Vertex(pkgDesc.org(), pkgDesc.name());
        DependencyNode existingNode = vertices.get(vertex);
        if (existingNode != null && existingNode.scope().equals(DEFAULT)) {
            node = new DependencyNode(pkgDesc, DEFAULT, node.resolutionType());
        }
        vertices.put(vertex, node);

        // if the source node is not in the vertices list, we need to add that to the graph, or
        // if the source node version is getting updated, the list of dependencies should be reset and re-added,
        // we reset the list of dependencies.
        if (existingNode == null || !existingNode.equals(node)) {
            depGraph.put(vertex, new HashSet<>());
        }
    }

    public void addDependency(DependencyNode sourceNode, DependencyNode targetNode) {
        PackageDescriptor source = sourceNode.pkgDesc();
        PackageDescriptor target = targetNode.pkgDesc();
        Vertex sourceVertex = new Vertex(source.org(), source.name());
        Vertex targetVertex = new Vertex(target.org(), target.name());

        DependencyNode existingTargetNode = vertices.get(targetVertex);
        if (existingTargetNode != null && existingTargetNode.scope().equals(DEFAULT)) {
            targetNode = new DependencyNode(target, DEFAULT, targetNode.resolutionType());
        }
        vertices.put(targetVertex, targetNode);
        if (!depGraph.containsKey(targetVertex)) {
            depGraph.put(targetVertex, new HashSet<>());
        }
        depGraph.get(sourceVertex).add(targetVertex);
    }

    public PackageDescriptor getDependency(PackageOrg org, PackageName name) {
        Vertex vertexToFetch = new Vertex(org, name);
        DependencyNode node = vertices.get(vertexToFetch);
        return node != null ? node.pkgDesc() : null;
    }

    public DependencyGraph<DependencyNode> buildGraph() {
        removeDanglingNodes();
        DependencyGraph.DependencyGraphBuilder<DependencyNode> graphBuilder = DependencyGraph
                .DependencyGraphBuilder.getBuilder(rootDepNode);
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

    private void removeDanglingNodes() {
        Set<Vertex> danglingVertices = new HashSet<>(vertices.keySet());
        removeDanglingNodes(rootVertex, danglingVertices);

        for (Vertex danglingVertex : danglingVertices) {
            vertices.remove(danglingVertex);
            depGraph.remove(danglingVertex);
            vertices.remove(danglingVertex);
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

    /**
     * Print the dependency graph.
     */
    public void printGraph() {
        outStream.println("Dependency Graph:");
        for (Map.Entry<Vertex, Set<Vertex>> entry : depGraph.entrySet()) {
            outStream.println(entry.getKey() + " -> " + entry.getValue());
        }
    }

    /**
     * Represents a Vertex in the DAG maintained by the PackageDependencyGraphBuilder.
     * @param org  organization
     * @param name package name
     *
     * @since 2.0.0
     */
    private record Vertex(PackageOrg org, PackageName name) {

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
