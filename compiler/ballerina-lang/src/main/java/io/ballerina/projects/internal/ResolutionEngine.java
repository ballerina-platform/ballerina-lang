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
import io.ballerina.projects.DependencyManifest;
import io.ballerina.projects.DependencyResolutionType;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.Project;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Responsible for creating the dependency graph with automatic version updates.
 *
 * @since 2.0.0
 */
public class ResolutionEngine {
    private final PackageDescriptor rootPkgDesc;
    private final DependencyManifest dependencyManifest;
    private final boolean offline;
    private final boolean sticky;

    private final PackageDependencyGraphBuilder graphBuilder;
    private final PackageResolver packageResolver;

    public ResolutionEngine(PackageDescriptor rootPkgDesc,
                            DependencyManifest dependencyManifest,
                            PackageResolver packageResolver,
                            boolean offline, // TODO Can we combine these two options into buildOptions
                            boolean sticky) {
        this.dependencyManifest = dependencyManifest;
        this.rootPkgDesc = rootPkgDesc;
        this.packageResolver = packageResolver;
        this.offline = offline;
        this.sticky = sticky;

        this.graphBuilder = new PackageDependencyGraphBuilder(rootPkgDesc);
    }

    public DependencyGraph<DependencyNode> resolveDependencies(Collection<DependencyNode> directDependencies) {
        populateGraphWithPreviousCompilationDependencies(dependencyManifest, graphBuilder);

        // Resolve dependency versions
        populateInitialDependencyGraph(directDependencies, graphBuilder);
        completeDependencyGraph(graphBuilder);

        // TODO we need to return the dependency graph and the diagnostics....
        return graphBuilder.buildGraph();
    }

    // TODO Move this method to the PackageResolution class.
    public DependencyGraph<ResolvedPackageDependency> getPackageDependencyGraph(Project currentProject) {
        return graphBuilder.buildPackageDependencyGraph(packageResolver, currentProject, offline);
    }

    private void populateInitialDependencyGraph(Collection<DependencyNode> directDependencies,
                                                PackageDependencyGraphBuilder graphBuilder) {
        List<PackageMetadataResponse> pkgMetadataResponses = resolveDirectDependencies(directDependencies);
        for (PackageMetadataResponse resolutionResp : pkgMetadataResponses) {
            if (resolutionResp.resolutionStatus() == ResolutionResponse.ResolutionStatus.UNRESOLVED) {
                // TODO Report diagnostics
                continue;
            }
            ResolutionRequest resolutionReq = resolutionResp.packageLoadRequest();
            graphBuilder.addResolvedDependency(rootPkgDesc, resolutionResp.resolvedDescriptor(),
                    resolutionReq.scope(), resolutionReq.dependencyResolutionType());
            mergeGraph(resolutionResp.resolvedDescriptor(), resolutionResp.dependencyGraph().orElseThrow(
                    () -> new IllegalStateException("Graph cannot be null in a resolved dependency")),
                    resolutionReq.scope(), resolutionReq.dependencyResolutionType(),
                    new HashSet<>(), graphBuilder);
        }
    }

    private List<PackageMetadataResponse> resolveDirectDependencies(Collection<DependencyNode> directDependencies) {
        // Set the default locking mode based on the sticky build option.
        PackageLockingMode lockingMode = sticky ? PackageLockingMode.HARD : PackageLockingMode.MEDIUM;
        List<ResolutionRequest> resolutionRequests = new ArrayList<>();
        for (DependencyNode directDependency : directDependencies) {
            PackageDescriptor pkgDesc = directDependency.pkgDesc();
            Optional<DependencyManifest.Package> dependency = dependencyManifest.dependency(
                    pkgDesc.org(), pkgDesc.name());
            if (dependency.isPresent() && dependency.get().isTransitive()) {
                // If the dependency is a direct dependency then use the version otherwise leave it.
                // The situation is that an indirect dependency(previous compilation) has become a
                // direct dependency (this compilation). Here we ignore the previous indirect dependency version and
                // look up Ballerina central repository for the latest version which is in the same compatible range.
                lockingMode = PackageLockingMode.SOFT;
            }

            resolutionRequests.add(ResolutionRequest.from(pkgDesc,
                    directDependency.scope(), directDependency.resolutionType(), offline, lockingMode));
        }

        return packageResolver.resolvePackageMetadata(resolutionRequests);
    }

    private void mergeGraph(PackageDescriptor rootNode,
                            DependencyGraph<PackageDescriptor> dependencyGraph,
                            PackageDependencyScope scope,
                            DependencyResolutionType resolutionType,
                            Set<PackageDescriptor> visitedNodes,
                            PackageDependencyGraphBuilder graphBuilder) {
        Collection<PackageDescriptor> directDependencies = dependencyGraph.getDirectDependencies(rootNode);
        for (PackageDescriptor directDep : directDependencies) {
            graphBuilder.addDependency(rootNode, directDep, scope, resolutionType);
            if (!visitedNodes.contains(directDep)) {
                visitedNodes.add(directDep);
                mergeGraph(directDep, dependencyGraph, scope, resolutionType, visitedNodes, graphBuilder);
            }
        }
    }

    private void completeDependencyGraph(PackageDependencyGraphBuilder graphBuilder) {
        Collection<DependencyNode> unresolvedNodes = graphBuilder.cleanUnresolvedNodes();
        if (!sticky) {
            // Discard the previous unresolved nodes,
            // Since sticky = false, we have to update all dependency nodes.
            unresolvedNodes = graphBuilder.getAllDependencies();
        }
        completeDependencyGraph(graphBuilder, unresolvedNodes);
    }

    private void completeDependencyGraph(PackageDependencyGraphBuilder graphBuilder,
                                         Collection<DependencyNode> unresolvedNodes) {
        PackageLockingMode lockingMode = PackageLockingMode.MEDIUM;
        while (!unresolvedNodes.isEmpty()) {
            List<ResolutionRequest> resRequests = new ArrayList<>(unresolvedNodes.size());
            for (DependencyNode unresolvedNode : unresolvedNodes) {
                resRequests.add(ResolutionRequest.from(unresolvedNode.pkgDesc(),
                        unresolvedNode.scope(), unresolvedNode.resolutionType(), offline, lockingMode));
            }

            List<PackageMetadataResponse> pkgMetadataResponses = packageResolver.resolvePackageMetadata(resRequests);
            resolvePackageMetadata(pkgMetadataResponses, graphBuilder);
            unresolvedNodes = graphBuilder.cleanUnresolvedNodes();
        }
    }

    private void resolvePackageMetadata(List<PackageMetadataResponse> pkgMetadataResponses,
                                        PackageDependencyGraphBuilder graphBuilder) {
        for (PackageMetadataResponse resolutionResp : pkgMetadataResponses) {
            if (resolutionResp.resolutionStatus() == ResolutionResponse.ResolutionStatus.UNRESOLVED) {
                // TODO Report diagnostics
                continue;
            }

            if (!graphBuilder.containsNode(resolutionResp.resolvedDescriptor())) {
                addNodeToGraph(graphBuilder, resolutionResp);
            }
        }
    }

    private void addNodeToGraph(PackageDependencyGraphBuilder graphBuilder,
                                PackageMetadataResponse resolutionResp) {
        ResolutionRequest resolutionReq = resolutionResp.packageLoadRequest();
        PackageDescriptor pkgDesc = resolutionResp.resolvedDescriptor();
        PackageDependencyScope scope = resolutionReq.scope();
        DependencyResolutionType resolvedType = resolutionReq.dependencyResolutionType();
        if (graphBuilder.addResolvedNode(pkgDesc, scope, resolvedType)) {
            mergeGraph(pkgDesc, resolutionResp.dependencyGraph().orElseThrow(
                    () -> new IllegalStateException("Graph cannot be null in a resolved dependency")),
                    scope, resolvedType,
                    new HashSet<>(), graphBuilder);
        }
    }

    private void populateGraphWithPreviousCompilationDependencies(DependencyManifest dependencyManifest,
                                                                  PackageDependencyGraphBuilder graphBuilder) {
        for (DependencyManifest.Package pkg : dependencyManifest.packages()) {
            graphBuilder.addResolvedNode(PackageDescriptor.from(pkg.org(), pkg.name(), pkg.version()),
                    PackageDependencyScope.fromString(pkg.scope()), DependencyResolutionType.SOURCE);
        }
    }

    /**
     * Represents a PackageDependency in the context of dependency resolution.
     *
     * @since 2.0.0
     */
    public static class DependencyNode {
        private final PackageDescriptor pkgDesc;
        private final PackageDependencyScope scope;
        private final DependencyResolutionType resolutionType;

        public DependencyNode(PackageDescriptor pkgDesc,
                              PackageDependencyScope scope,
                              DependencyResolutionType resolutionType) {
            this.pkgDesc = Objects.requireNonNull(pkgDesc);
            this.scope = Objects.requireNonNull(scope);
            this.resolutionType = Objects.requireNonNull(resolutionType);
        }

        public PackageDescriptor pkgDesc() {
            return pkgDesc;
        }

        public PackageDependencyScope scope() {
            return scope;
        }

        public DependencyResolutionType resolutionType() {
            return resolutionType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DependencyNode that = (DependencyNode) o;
            return Objects.equals(pkgDesc.org(), that.pkgDesc.org()) &&
                    Objects.equals(pkgDesc.name(), that.pkgDesc.name()) &&
                    Objects.equals(pkgDesc.version(), that.pkgDesc.version()) &&
                    Objects.equals(pkgDesc.repository(), that.pkgDesc.repository()) &&
                    scope == that.scope &&
                    resolutionType == that.resolutionType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pkgDesc, scope, resolutionType);
        }

        @Override
        public String toString() {
            String attr = " [scope=" + scope + ",kind=" + resolutionType +
                    ",repo=" + pkgDesc.repository().orElse(null) + "]";
            return pkgDesc.toString() + attr;
        }
    }
}
