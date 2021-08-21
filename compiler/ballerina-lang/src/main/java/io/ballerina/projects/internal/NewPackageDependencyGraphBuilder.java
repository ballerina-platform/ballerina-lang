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
public class NewPackageDependencyGraphBuilder {
    // TODO how about a multi-level map here Map<PackageOrg, Map<PackageName, StaticPackageDependency>>
    private final Map<Vertex, StaticPackageDependency> vertices = new HashMap<>();
    private final Map<Vertex, Set<Vertex>> depGraph = new HashMap<>();

    public NewPackageDependencyGraphBuilder() {
    }

    public NewPackageDependencyGraphBuilder(PackageDescriptor rootNode) {
        this.addNode(rootNode, PackageDependencyScope.DEFAULT);
    }

    public void addNode(PackageDescriptor node, PackageDependencyScope scope) {
        // Add the correct version of the dependent to the graph.
        Vertex dependentVertex = new Vertex(node.org(), node.name());
        addNewVertex(dependentVertex, new StaticPackageDependency(node, scope));
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

        Vertex dependencyVertex = new Vertex(dependency.org(), dependency.name(), dependencyResolvedType);
        addNewVertex(dependencyVertex, new StaticPackageDependency(dependency, dependencyScope));
        depGraph.get(dependentVertex).add(dependencyVertex);
    }

    public void addDependencies(PackageDescriptor dependent,
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
    }

    public boolean isNodeExists(PackageOrg org, PackageName name) {
        return vertices.containsKey(new Vertex(org, name));
    }

    public void mergeGraph(DependencyGraph<PackageDescriptor> theirGraph, PackageDependencyScope scope) {
        for (PackageDescriptor theirPkgDesc : theirGraph.getNodes()) {
            addNode(theirPkgDesc, scope);
            Collection<PackageDescriptor> theirPkgDescDeps = theirGraph.getDirectDependencies(theirPkgDesc);
            addDependencies(theirPkgDesc, theirPkgDescDeps, scope);
        }
    }

    private void addNewVertex(Vertex vertex, StaticPackageDependency newStaticPkgDep) {
        if (!vertices.containsKey(vertex)) {
            vertices.put(vertex, newStaticPkgDep);
            depGraph.put(vertex, new HashSet<>());
            return;
        }

        // There exists another version in the graph.
        StaticPackageDependency existingStaticPkgDep = vertices.get(vertex);
        PackageDescriptor resolvedPkgDesc = handleDependencyConflict(newStaticPkgDep, existingStaticPkgDep);

        // If the existing dependency scope is DEFAULT, use it. Otherwise use the new dependency scope.
        PackageDependencyScope depScope = existingStaticPkgDep.scope() == PackageDependencyScope.DEFAULT ?
                PackageDependencyScope.DEFAULT : newStaticPkgDep.scope();

        vertices.put(vertex, new StaticPackageDependency(resolvedPkgDesc, depScope));
    }

    private PackageDescriptor handleDependencyConflict(StaticPackageDependency newStaticPkgDep,
                                                       StaticPackageDependency existingStaticPkgDep) {
        PackageDescriptor newPkgDesc = newStaticPkgDep.pkgDesc();
        PackageDescriptor existingPkgDesc = existingStaticPkgDep.pkgDesc();
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

    public DependencyGraph<PackageDescriptor> build() {
        DependencyGraph.DependencyGraphBuilder<PackageDescriptor> graphBuilder =
                DependencyGraph.DependencyGraphBuilder.getBuilder();
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

    public DependencyGraph<ResolvedPackageDependency> buildPackageDependencyGraph(PackageResolver packageResolver,
                                                                                  Project rootProject,
                                                                                  boolean offline) {
        List<PackageDescriptor> packageDescriptors = new ArrayList<>();
        vertices.forEach((key, value) -> packageDescriptors.add(value.pkgDesc));
        List<ResolutionResponse> resolutionResponses =
                packageResolver.newResolvePackages(packageDescriptors, offline, rootProject);
        DependencyGraph.DependencyGraphBuilder<ResolvedPackageDependency> depGraphBuilder =
                DependencyGraph.DependencyGraphBuilder.getBuilder();

        for (Map.Entry<Vertex, Set<Vertex>> graphNodeEntrySet : depGraph.entrySet()) {
            Vertex graphNode = graphNodeEntrySet.getKey();
            StaticPackageDependency staticPkgDep = vertices.get(graphNode);

            ResolutionResponse directDepResponse = findResolutionResponse(staticPkgDep.pkgDesc, resolutionResponses);
            if (directDepResponse == null) {
                continue;
            }
            ResolvedPackageDependency directPackageDep = new ResolvedPackageDependency(
                    directDepResponse.resolvedPackage(),
                    staticPkgDep.scope,
                    graphNode.dependencyResolvedType);

            List<ResolvedPackageDependency> resolvedTransitiveDeps = new ArrayList<>();
            Set<Vertex> transitiveDepGraphNodes = graphNodeEntrySet.getValue();
            for (Vertex transitiveDepGraphNode : transitiveDepGraphNodes) {
                StaticPackageDependency transitivePkgDep = vertices.get(transitiveDepGraphNode);

                ResolutionResponse transitiveDepResponse =
                        findResolutionResponse(transitivePkgDep.pkgDesc, resolutionResponses);
                if (transitiveDepResponse == null) {
                    continue;
                }
                ResolvedPackageDependency transitivePackageDep =
                        new ResolvedPackageDependency(
                                transitiveDepResponse.resolvedPackage(),
                                transitivePkgDep.scope,
                                graphNode.dependencyResolvedType);
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
            if (resolutionResponse.resolvedPackage().descriptor().equals(pkgDesc)) {
                return resolutionResponse;
            }
        }

        return null;
    }

    private static class Vertex {
        private final PackageOrg org;
        private final PackageName name;
        private final DependencyResolutionType dependencyResolvedType;

        Vertex(PackageOrg org, PackageName name) {
            this.org = org;
            this.name = name;
            this.dependencyResolvedType = DependencyResolutionType.SOURCE;
        }

        Vertex(PackageOrg org, PackageName name, DependencyResolutionType dependencyResolvedType) {
            this.org = org;
            this.name = name;
            this.dependencyResolvedType = dependencyResolvedType;
        }

        DependencyResolutionType dependencyResolvedType() {
            return dependencyResolvedType;
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
    private static class StaticPackageDependency {
        private final PackageDescriptor pkgDesc;
        private final PackageDependencyScope scope;
        private final boolean dirty; // touched, unresolved.

        public StaticPackageDependency(PackageDescriptor pkgDesc, PackageDependencyScope scope) {
            this.pkgDesc = pkgDesc;
            this.scope = scope;
            this.dirty = false;
        }

        public PackageDescriptor pkgDesc() {
            return pkgDesc;
        }

        public PackageDependencyScope scope() {
            return scope;
        }

        public boolean dirty() {
            return dirty;
        }
    }
}
