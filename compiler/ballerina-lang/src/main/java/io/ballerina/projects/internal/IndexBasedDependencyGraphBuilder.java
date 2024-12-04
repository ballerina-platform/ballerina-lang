/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package io.ballerina.projects.internal;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.ballerina.projects.DependencyResolutionType.SOURCE;
import static io.ballerina.projects.PackageDependencyScope.DEFAULT;

// TODO: Rename this to DependencyGraphBuilder
public class IndexBasedDependencyGraphBuilder {
    private final Vertex rootVertex;
    private final DependencyNode rootDepNode;
    private final Map<Vertex, DependencyNode> vertices = new HashMap<>();
    private final Map<Vertex, Set<Vertex>> depGraph = new HashMap<>();

    public IndexBasedDependencyGraphBuilder(PackageDescriptor root) {
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

    // TODO: streamline the below logic
    public void addDependency(DependencyNode sourceNode, DependencyNode targetNode) {
        PackageDescriptor source = sourceNode.pkgDesc();
        PackageDescriptor target = targetNode.pkgDesc();
        Vertex sourceVertex = new Vertex(source.org(), source.name());
        Vertex targetVertex = new Vertex(target.org(), target.name());

        DependencyNode existingSourceNode = vertices.get(sourceVertex);
        if (existingSourceNode != null && existingSourceNode.scope().equals(DEFAULT)) {
            sourceNode = new DependencyNode(source, DEFAULT, sourceNode.resolutionType());
        }
        DependencyNode existingTargetNode = vertices.get(targetVertex);
        if (existingTargetNode != null && existingTargetNode.scope().equals(DEFAULT)) {
            targetNode = new DependencyNode(target, DEFAULT, targetNode.resolutionType());
        }

        vertices.put(sourceVertex, sourceNode);
        vertices.put(targetVertex, targetNode);

        // if the source node is not in the vertices list, we need to add that to the graph, or
        // if the source node version is getting updated, the list of dependencies should be reset and re-added,
        // we reset the list of dependencies.
        if (existingSourceNode == null || !existingSourceNode.equals(sourceNode)) {
            depGraph.put(sourceVertex, new HashSet<>());
        }
        if (!depGraph.containsKey(targetVertex)) {
            depGraph.put(targetVertex, new HashSet<>());
        }
        depGraph.get(sourceVertex).add(targetVertex);
    }

    public DependencyGraph<DependencyNode> buildGraph() {
        removeDanglingNodes();
        DependencyGraph.DependencyGraphBuilder<DependencyNode> graphBuilder = DependencyGraph.DependencyGraphBuilder.getBuilder(rootDepNode);
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

    public void printGraph() {
        System.out.println("Dependency Graph:");
        for (Map.Entry<Vertex, Set<Vertex>> entry : depGraph.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }

    /**
         * Represents a Vertex in the DAG maintained by the PackageDependencyGraphBuilder.
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
    }
}
