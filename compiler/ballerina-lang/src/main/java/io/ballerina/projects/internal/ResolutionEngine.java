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
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.internal.PackageDependencyGraphBuilder.NodeStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Responsible for creating the dependency graph with automatic version updates.
 *
 * @since 2.0.0
 */
public class ResolutionEngine {
    private final PackageDescriptor rootPkgDesc;
    private final BlendedManifest blendedManifest;
    private final PackageResolver packageResolver;
    private final ModuleResolver moduleResolver;
    private final ResolutionOptions resolutionOptions;

    private final DependencyManifest dependencyManifest;
    private final PackageDependencyGraphBuilder graphBuilder;

    public ResolutionEngine(PackageDescriptor rootPkgDesc,
                            BlendedManifest blendedManifest,
                            PackageResolver packageResolver,
                            ModuleResolver moduleResolver,
                            ResolutionOptions resolutionOptions) {
        this.rootPkgDesc = rootPkgDesc;
        this.blendedManifest = blendedManifest;
        this.packageResolver = packageResolver;
        this.moduleResolver = moduleResolver;
        this.resolutionOptions = resolutionOptions;

        this.dependencyManifest = blendedManifest.dependencyManifest();
        this.graphBuilder = new PackageDependencyGraphBuilder(rootPkgDesc);
    }

    public DependencyGraph<DependencyNode> resolveDependencies(Collection<ModuleLoadRequest> moduleLoadRequests) {
        // 1) Resolve import declarations into Packages.
        Collection<DependencyNode> directDependencies = resolvePackages(moduleLoadRequests);

        // 2) Pre-populate the graphBuilder with dependencies recorded in Dependencies.toml
        populateGraphWithPreviousCompilationDependencies(dependencyManifest, graphBuilder);

        // 3) TODO Should we add the dependencies specified in Ballerina.toml here

        // 4) Create the static/initial dependency graph.
        //    This graph contains direct dependencies and their transitives,
        //     but we don't update versions.
        populateStaticDependencyGraph(directDependencies, graphBuilder);

        // 5) Update the dependency versions if required
        //    This method traverse through the graph as many time as time until the graph is completed.
        //    Graph is complete when it contains latest compatible versions of all dependencies.
        updateDependencyVersions(graphBuilder);

        // 6) Build final the dependency graph.
        return graphBuilder.buildGraph();
    }

    private Collection<DependencyNode> resolvePackages(Collection<ModuleLoadRequest> moduleLoadRequests) {
        // Get the direct dependencies of the current package.
        // This list does not contain langlib and the root package.
        PackageContainer<ModuleResolver.DirectPackageDependency> directDepsContainer =
                moduleResolver.resolveModuleLoadRequests(moduleLoadRequests);

        List<ResolutionEngine.DependencyNode> directDeps = new ArrayList<>();
        for (ModuleResolver.DirectPackageDependency directPkgDependency : directDepsContainer.getAll()) {
            PackageVersion depVersion;
            String repository;
            PackageDescriptor depPkgDesc = directPkgDependency.pkgDesc();
            if (directPkgDependency.dependencyKind() == ModuleResolver.DirectPackageDependencyKind.NEW) {
                // This blendedDep may be resolved from the local repository as well.
                Optional<BlendedManifest.Dependency> blendedDepOptional = blendedManifest.dependency(
                        depPkgDesc.org(), depPkgDesc.name());

                // If the package version is not null, use it
                if (directPkgDependency.pkgDesc().version() != null) {
                    depVersion = directPkgDependency.pkgDesc().version();
                    repository = blendedDepOptional
                            .map(BlendedManifest.Dependency::repositoryName)
                            .orElse(null);
                } else if (blendedDepOptional.isPresent()) {
                    BlendedManifest.Dependency blendedDep = blendedDepOptional.get();
                    repository = blendedDep.repositoryName();
                    depVersion = blendedDep.version();
                } else {
                    depVersion = null;
                    repository = null;
                }
            } else if (directPkgDependency.dependencyKind() == ModuleResolver.DirectPackageDependencyKind.EXISTING) {
                BlendedManifest.Dependency blendedDep = blendedManifest.dependencyOrThrow(
                        depPkgDesc.org(), depPkgDesc.name());
                depVersion = blendedDep.version();
                repository = blendedDep.repositoryName();
            } else {
                throw new IllegalStateException("Unsupported direct dependency kind: " +
                        directPkgDependency.dependencyKind());
            }
            directDeps.add(new ResolutionEngine.DependencyNode(
                    PackageDescriptor.from(depPkgDesc.org(), depPkgDesc.name(), depVersion, repository),
                    directPkgDependency.scope(), directPkgDependency.resolutionType()));
        }

        return directDeps;
    }

    private void populateStaticDependencyGraph(Collection<DependencyNode> directDependencies,
                                               PackageDependencyGraphBuilder graphBuilder) {
        Collection<PackageMetadataResponse> pkgMetadataResponses = resolveDirectDependencies(directDependencies);
        for (PackageMetadataResponse resolutionResp : pkgMetadataResponses) {
            if (resolutionResp.resolutionStatus() == ResolutionResponse.ResolutionStatus.UNRESOLVED) {
                // TODO Report diagnostics
                continue;
            }

            ResolutionRequest resolutionReq = resolutionResp.packageLoadRequest();
            PackageDescriptor resolvedPkgDesc = resolutionResp.resolvedDescriptor();
            DependencyResolutionType resolutionType = resolutionReq.resolutionType();
            PackageDependencyScope scope = resolutionReq.scope();

            // Merge the dependency graph only if the node is accepted by the graphBuilder
            NodeStatus nodeStatus = graphBuilder.addResolvedDependency(rootPkgDesc,
                    resolvedPkgDesc, scope, resolutionType);
            if (nodeStatus == NodeStatus.ACCEPTED) {
                mergeGraph(resolvedPkgDesc,
                        resolutionResp.dependencyGraph().orElseThrow(
                                () -> new IllegalStateException("Graph cannot be null in a resolved dependency")),
                        scope, resolutionType, graphBuilder);
            }
        }
    }

    private Collection<PackageMetadataResponse> resolveDirectDependencies(Collection<DependencyNode> directDeps) {
        // Set the default locking mode based on the sticky build option.
        PackageLockingMode lockingMode = resolutionOptions.sticky() ?
                PackageLockingMode.HARD : PackageLockingMode.MEDIUM;
        List<ResolutionRequest> resolutionRequests = new ArrayList<>();
        for (DependencyNode directDependency : directDeps) {
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

            resolutionRequests.add(ResolutionRequest.from(pkgDesc, directDependency.scope(),
                    directDependency.resolutionType(), lockingMode));
        }

        return packageResolver.resolvePackageMetadata(resolutionRequests, resolutionOptions);
    }

    private void mergeGraph(PackageDescriptor rootNode,
                            DependencyGraph<PackageDescriptor> dependencyGraph,
                            PackageDependencyScope scope,
                            DependencyResolutionType resolutionType,
                            PackageDependencyGraphBuilder graphBuilder) {
        Collection<PackageDescriptor> directDependencies = dependencyGraph.getDirectDependencies(rootNode);
        for (PackageDescriptor directDep : directDependencies) {
            // Merge the dependency graph only if the node is accepted by the graphBuilder
            NodeStatus nodeStatus = graphBuilder.addDependency(rootNode, directDep, scope, resolutionType);
            if (nodeStatus == NodeStatus.ACCEPTED) {
                mergeGraph(directDep, dependencyGraph, scope, resolutionType, graphBuilder);
            }
        }
    }

    private void updateDependencyVersions(PackageDependencyGraphBuilder graphBuilder) {
        Collection<DependencyNode> unresolvedNodes = graphBuilder.cleanUnresolvedNodes();
        if (!resolutionOptions.sticky()) {
            // Discard the previous unresolved nodes,
            // Since sticky = false, we have to update all dependency nodes.
            unresolvedNodes = graphBuilder.getAllDependencies();
        }
        updateDependencyVersions(graphBuilder, unresolvedNodes);
    }

    private void updateDependencyVersions(PackageDependencyGraphBuilder graphBuilder,
                                          Collection<DependencyNode> unresolvedNodes) {
        PackageLockingMode lockingMode = PackageLockingMode.MEDIUM;
        while (!unresolvedNodes.isEmpty()) {
            List<ResolutionRequest> resRequests = new ArrayList<>(unresolvedNodes.size());
            for (DependencyNode unresolvedNode : unresolvedNodes) {
                resRequests.add(ResolutionRequest.from(unresolvedNode.pkgDesc(),
                        unresolvedNode.scope(), unresolvedNode.resolutionType(), lockingMode));
            }

            Collection<PackageMetadataResponse> pkgMetadataResponses =
                    packageResolver.resolvePackageMetadata(resRequests, resolutionOptions);
            resolvePackageMetadata(pkgMetadataResponses, graphBuilder);
            unresolvedNodes = graphBuilder.cleanUnresolvedNodes();
        }
    }

    private void resolvePackageMetadata(Collection<PackageMetadataResponse> pkgMetadataResponses,
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
        DependencyResolutionType resolvedType = resolutionReq.resolutionType();

        // Merge the dependency graph only if the node is accepted by the graphBuilder
        NodeStatus nodeStatus = graphBuilder.addResolvedNode(pkgDesc, scope, resolvedType);
        if (nodeStatus == NodeStatus.ACCEPTED) {
            mergeGraph(pkgDesc, resolutionResp.dependencyGraph().orElseThrow(
                    () -> new IllegalStateException("Graph cannot be null in a resolved dependency")),
                    scope, resolvedType, graphBuilder);
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

        public DependencyNode(PackageDescriptor pkgDesc) {
            this(pkgDesc, PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE);
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
