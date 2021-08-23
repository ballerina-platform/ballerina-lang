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
import io.ballerina.projects.DependencyResolutionType;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
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

    public PackageDependencyGraphBuilder() {
    }

    public PackageDependencyGraphBuilder(PackageDescriptor rootNode) {
        this.addNode(rootNode, PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE);
    }

    public void addNode(PackageDescriptor node,
                        PackageDependencyScope scope,
                        DependencyResolutionType dependencyResolvedType) {
        // Add the correct version of the dependent to the graph.
        Vertex dependentVertex = new Vertex(node.org(), node.name());
        addNewVertex(dependentVertex, new DependencyNode(node, scope, dependencyResolvedType));
    }

    public void addDependency(PackageDescriptor dependent,
                              PackageDescriptor dependency,
                              PackageDependencyScope dependencyScope,
                              DependencyResolutionType dependencyResolvedType) {
        // Add the correct version of the dependent to the graph.
        Vertex dependentVertex = new Vertex(dependent.org(), dependent.name());
        if (!depGraph.containsKey(dependentVertex)) {
            throw new IllegalStateException("Dependent node does not exist in the graph: " + dependent);
        }

        Vertex dependencyVertex = new Vertex(dependency.org(), dependency.name());
        addNewVertex(dependencyVertex, new DependencyNode(dependency, dependencyScope, dependencyResolvedType));
        depGraph.get(dependentVertex).add(dependencyVertex);
    }

    public void addDependencies(PackageDescriptor dependent,
                                Collection<PackageDescriptor> dependencies,
                                PackageDependencyScope dependencyScope,
                                DependencyResolutionType dependencyResolvedType) {
        // Add the correct version of the dependent to the graph.
        Vertex dependentVertex = new Vertex(dependent.org(), dependent.name());
        if (!depGraph.containsKey(dependentVertex)) {
            throw new IllegalStateException("Dependent node does not exist in the graph: " + dependent);
        }

        for (PackageDescriptor dependency : dependencies) {
            // Add the correct version of the dependency to the graph.
            Vertex dependencyVertex = new Vertex(dependency.org(), dependency.name());
            addNewVertex(dependencyVertex, new DependencyNode(dependency, dependencyScope, dependencyResolvedType));
            depGraph.get(dependentVertex).add(dependencyVertex);
        }
    }

    public boolean isNodeExists(PackageOrg org, PackageName name) {
        return vertices.containsKey(new Vertex(org, name));
    }

    public void mergeGraph(DependencyGraph<PackageDescriptor> theirGraph,
                           PackageDependencyScope scope,
                           DependencyResolutionType dependencyResolvedType) {
        for (PackageDescriptor theirPkgDesc : theirGraph.getNodes()) {
            addNode(theirPkgDesc, scope, dependencyResolvedType);
            Collection<PackageDescriptor> theirPkgDescDeps = theirGraph.getDirectDependencies(theirPkgDesc);
            addDependencies(theirPkgDesc, theirPkgDescDeps, scope, dependencyResolvedType);
        }
    }

    private void addNewVertex(Vertex vertex, DependencyNode newPkgDep) {
        if (!vertices.containsKey(vertex)) {
            vertices.put(vertex, newPkgDep);
            depGraph.put(vertex, new HashSet<>());
            return;
        }

        // There exists another version in the graph.
        DependencyNode existingStaticPkgDep = vertices.get(vertex);
        PackageDescriptor resolvedPkgDesc = handleDependencyConflict(newPkgDep, existingStaticPkgDep);

        // If the existing dependency scope is DEFAULT, use it. Otherwise use the new dependency scope.
        PackageDependencyScope depScope =
                existingStaticPkgDep.scope() == PackageDependencyScope.DEFAULT ?
                        PackageDependencyScope.DEFAULT :
                        newPkgDep.scope();

        // If the existing dependency scope is DEFAULT, use it. Otherwise use the new dependency scope.
        DependencyResolutionType resolutionType =
                existingStaticPkgDep.resolutionType() == DependencyResolutionType.SOURCE ?
                        DependencyResolutionType.SOURCE :
                        newPkgDep.resolutionType();

        vertices.put(vertex, new DependencyNode(resolvedPkgDesc, depScope, resolutionType));
    }

    private PackageDescriptor handleDependencyConflict(DependencyNode newPkgDep,
                                                       DependencyNode existingPkgDep) {
        PackageDescriptor newPkgDesc = newPkgDep.pkgDesc();
        PackageDescriptor existingPkgDesc = existingPkgDep.pkgDesc();
        PackageOrg packageOrg = newPkgDesc.org();
        PackageName packageName = newPkgDesc.name();

        VersionCompatibilityResult compatibilityResult = newPkgDesc.version().compareTo(existingPkgDesc.version());
        switch (compatibilityResult) {
            case EQUAL:
            case LESS_THAN:
                return PackageDescriptor.from(packageOrg, packageName, existingPkgDesc.version(),
                        existingPkgDesc.repository().orElse(null));
            case GREATER_THAN:
                return PackageDescriptor.from(packageOrg, packageName, newPkgDesc.version(),
                        newPkgDesc.repository().orElse(null));
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

    public DependencyGraph<DependencyNode> buildGraph() {
        DependencyGraph.DependencyGraphBuilder<DependencyNode> graphBuilder =
                DependencyGraph.DependencyGraphBuilder.getBuilder();
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

    public DependencyGraph<ResolvedPackageDependency> buildPackageDependencyGraph(PackageResolver packageResolver,
                                                                                  Project rootProject,
                                                                                  boolean offline) {
        List<PackageDescriptor> packageDescriptors = new ArrayList<>();
        vertices.forEach((key, value) -> packageDescriptors.add(value.pkgDesc()));
        List<ResolutionResponse> resolutionResponses =
                packageResolver.resolvePackages(packageDescriptors, offline, rootProject);
        DependencyGraph.DependencyGraphBuilder<ResolvedPackageDependency> depGraphBuilder =
                DependencyGraph.DependencyGraphBuilder.getBuilder();

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
