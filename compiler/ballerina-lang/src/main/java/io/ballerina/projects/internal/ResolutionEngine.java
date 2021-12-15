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
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.SemanticVersion.VersionCompatibilityResult;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.internal.PackageDependencyGraphBuilder.NodeStatus;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    private final PackageDependencyGraphBuilder graphBuilder;
    private final List<Diagnostic> diagnostics;

    private final PrintStream console = System.out;

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

        this.graphBuilder = new PackageDependencyGraphBuilder(rootPkgDesc, resolutionOptions);
        this.diagnostics = this.graphBuilder.diagnostics();
    }

    public DependencyGraph<DependencyNode> resolveDependencies(Collection<ModuleLoadRequest> moduleLoadRequests) {
        // 1) Resolve import declarations into Packages.
        Collection<DependencyNode> directDependencies = resolvePackages(moduleLoadRequests);

        // 2) Create the static/initial dependency graph.
        //    This graph contains direct dependencies and their transitives,
        //     but we don't update versions.
        populateStaticDependencyGraph(directDependencies);

        // 3) Update the dependency versions if required
        //    This method traverse through the graph as many time as time until the graph is completed.
        //    Graph is complete when it contains latest compatible versions of all dependencies.
        updateDependencyVersions();

        // 4) Now the first round of update is done, but there may be more unresolved nodes in the graph builder.
        //    We need to keep resolving the unresolved nodes until the graph is complete.
        completeDependencyGraph();

        // 5) Build final the dependency graph.
        return buildFinalDependencyGraph();
    }

    public List<Diagnostic> diagnostics() {
        return this.diagnostics;
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
                            .map(BlendedManifest.Dependency::repository)
                            .orElse(null);
                } else if (blendedDepOptional.isPresent()) {
                    BlendedManifest.Dependency blendedDep = blendedDepOptional.get();
                    repository = blendedDep.repository();
                    depVersion = blendedDep.version();
                } else {
                    depVersion = null;
                    repository = null;
                }
            } else if (directPkgDependency.dependencyKind() == ModuleResolver.DirectPackageDependencyKind.EXISTING) {
                BlendedManifest.Dependency blendedDep = blendedManifest.dependencyOrThrow(
                        depPkgDesc.org(), depPkgDesc.name());
                depVersion = blendedDep.version();
                repository = blendedDep.repository();
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

    private void populateStaticDependencyGraph(Collection<DependencyNode> directDependencies) {
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
                        scope, resolutionType);
            }
        }

        dumpInitialGraph();
    }

    private Collection<PackageMetadataResponse> resolveDirectDependencies(Collection<DependencyNode> directDeps) {
        // Set the default locking mode based on the sticky build option.
        PackageLockingMode lockingMode = resolutionOptions.sticky() ?
                PackageLockingMode.HARD : PackageLockingMode.MEDIUM;
        List<ResolutionRequest> resolutionRequests = new ArrayList<>();
        for (DependencyNode directDependency : directDeps) {
            PackageDescriptor pkgDesc = directDependency.pkgDesc();
            Optional<BlendedManifest.Dependency> dependency = blendedManifest.lockedDependency(
                    pkgDesc.org(), pkgDesc.name());
            if (dependency.isPresent() &&
                    dependency.get().relation() == BlendedManifest.DependencyRelation.TRANSITIVE) {
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
                            DependencyResolutionType resolutionType) {
        Collection<PackageDescriptor> directDependencies = dependencyGraph.getDirectDependencies(rootNode);
        for (PackageDescriptor directDep : directDependencies) {
            NodeStatus nodeStatus;
            DependencyGraph<PackageDescriptor> dependencyGraphFinal;
            if (directDep.isBuiltInPackage()) {
                // a built-in dependency will have the same version (0.0.0) across Ballerina distributions
                // but their dependencies may change. Therefore,
                // we need to always get the dependency graph of built-in packages from the current distribution
                dependencyGraphFinal = getBuiltInPkgDescDepGraph(scope, directDep);
                // Builtin package versions are always resolved
                nodeStatus = graphBuilder.addResolvedDependency(rootNode, directDep, scope, resolutionType);
            } else {
                dependencyGraphFinal = dependencyGraph;
                nodeStatus = graphBuilder.addUnresolvedDependency(rootNode, directDep, scope, resolutionType);
            }

            // Merge the dependency graph only if the node is accepted by the graphBuilder
            if (nodeStatus == NodeStatus.ACCEPTED) {
                mergeGraph(directDep, dependencyGraphFinal, scope, resolutionType);
            }
        }
    }

    private DependencyGraph<PackageDescriptor> getBuiltInPkgDescDepGraph(
            PackageDependencyScope scope, PackageDescriptor directDep) {
        Collection<PackageMetadataResponse> packageMetadataResponses = packageResolver.resolvePackageMetadata(
                Collections.singletonList(ResolutionRequest.from(directDep, scope)), resolutionOptions);
        if (packageMetadataResponses.isEmpty()) {
            // This condition cannot be met since a built-in package is always expected to be available in the dist
            throw new IllegalStateException("built-in package not found in distribution: " + directDep.toString());
        }
        PackageMetadataResponse packageMetadataResponse = packageMetadataResponses.iterator().next();
        Optional<DependencyGraph<PackageDescriptor>> packageDescriptorDependencyGraph =
                packageMetadataResponse.dependencyGraph();

        return packageDescriptorDependencyGraph
                .orElseThrow(() -> new IllegalStateException("Graph cannot be null in a built-in package"));
    }

    private void updateDependencyVersions() {
        // Remove all dangling nodes in the graph builder.
        graphBuilder.removeDanglingNodes();
        // Get unresolved nodes. This list is based on the sticky option
        Collection<DependencyNode> unresolvedNodes = getUnresolvedNode();

        // Create ResolutionRequests for all unresolved nodes by looking at the blended nodes
        List<ResolutionRequest> unresolvedRequests = new ArrayList<>(unresolvedNodes.size());
        for (DependencyNode unresolvedNode : unresolvedNodes) {
            PackageDescriptor unresolvedPkgDes = unresolvedNode.pkgDesc();
            Optional<BlendedManifest.Dependency> blendedDepOptional =
                    blendedManifest.dependency(unresolvedPkgDes.org(), unresolvedPkgDes.name());
            ResolutionRequest resolutionRequest = getRequestForUnresolvedNode(unresolvedNode,
                    blendedDepOptional.orElse(null));
            if (resolutionRequest != null) {
                unresolvedRequests.add(resolutionRequest);
            }
        }

        // Resolve unresolved nodes to see whether there exist newer versions
        Collection<PackageMetadataResponse> pkgMetadataResponses =
                packageResolver.resolvePackageMetadata(unresolvedRequests, resolutionOptions);

        // Update the graph with new versions of dependencies (if any)
        addUpdatedPackagesToGraph(pkgMetadataResponses);

        dumpIntermediateGraph(1);
    }

    /**
     * Returns a ResolutionRequest instance for the given unresolved node by considering the details
     * recorded in BlendedManifest.
     *
     * @param unresolvedNode the unresolved node
     * @param blendedDep     the dependency recorded in either Dependencies.toml or Ballerina.toml
     * @return ResolutionRequest
     */
    private ResolutionRequest getRequestForUnresolvedNode(DependencyNode unresolvedNode,
                                                          BlendedManifest.Dependency blendedDep) {
        if (blendedDep == null) {
            return ResolutionRequest.from(unresolvedNode.pkgDesc(), unresolvedNode.scope(),
                    unresolvedNode.resolutionType(), PackageLockingMode.MEDIUM);
        }

        // Compare blendedDep version with the unresolved version
        VersionCompatibilityResult versionCompResult = blendedDep.version().compareTo(
                unresolvedNode.pkgDesc().version());
        if (versionCompResult == VersionCompatibilityResult.GREATER_THAN ||
                versionCompResult == VersionCompatibilityResult.EQUAL) {
            PackageLockingMode lockingMode = resolutionOptions.sticky() ?
                    PackageLockingMode.HARD : PackageLockingMode.MEDIUM;
            PackageDescriptor blendedDepPkgDesc = PackageDescriptor.from(blendedDep.org(), blendedDep.name(),
                    blendedDep.version(), blendedDep.repository());
            return ResolutionRequest.from(blendedDepPkgDesc, unresolvedNode.scope(),
                    unresolvedNode.resolutionType(), lockingMode);
        } else if (versionCompResult == VersionCompatibilityResult.LESS_THAN) {
            return ResolutionRequest.from(unresolvedNode.pkgDesc(), unresolvedNode.scope(),
                    unresolvedNode.resolutionType(), PackageLockingMode.MEDIUM);
        } else {
            // Blended Dep version is incompatible with the unresolved node.
            // TODO we can add the location to the diagnostic
            String depInfo = blendedDep.org() + "/" + blendedDep.name();
            String sourceFile = blendedDep.origin() == BlendedManifest.DependencyOrigin.USER_SPECIFIED ?
                    ProjectConstants.BALLERINA_TOML : ProjectConstants.DEPENDENCIES_TOML;
            Diagnostic diagnostic = ProjectUtils.createDiagnostic("Incompatible versions: " + depInfo + ". " +
                            "Version specified in " + sourceFile + ": " + blendedDep.version() +
                            " and the version resolved from other dependencies: " + unresolvedNode.pkgDesc.version(),
                    ProjectDiagnosticErrorCode.INCOMPATIBLE_DEPENDENCY_VERSIONS.diagnosticId(),
                    DiagnosticSeverity.ERROR
            );
            diagnostics.add(diagnostic);

            return null;
        }
    }

    private Collection<DependencyNode> getUnresolvedNode() {
        if (resolutionOptions.sticky()) {
            return graphBuilder.getUnresolvedNodes();
        } else {
            // Since sticky = false, we have to update all dependency nodes.
            return graphBuilder.getAllDependencies();
        }
    }

    private void completeDependencyGraph() {
        // This is the second attempt to update versions.
        int noOfUpdateAttempts = 2;

        graphBuilder.removeDanglingNodes();
        Collection<DependencyNode> unresolvedNodes;
        while (!(unresolvedNodes = graphBuilder.getUnresolvedNodes()).isEmpty()) {
            // Create ResolutionRequests for all unresolved nodes by looking at the blended nodes
            List<ResolutionRequest> unresolvedRequests = new ArrayList<>(unresolvedNodes.size());
            for (DependencyNode unresolvedNode : unresolvedNodes) {
                PackageDescriptor unresolvedPkgDes = unresolvedNode.pkgDesc();
                Optional<BlendedManifest.Dependency> blendedDepOptional =
                        blendedManifest.userSpecifiedDependency(unresolvedPkgDes.org(), unresolvedPkgDes.name());
                ResolutionRequest resolutionRequest = getRequestForUnresolvedNode(unresolvedNode,
                        blendedDepOptional.orElse(null));
                if (resolutionRequest != null) {
                    unresolvedRequests.add(resolutionRequest);
                }
            }

            Collection<PackageMetadataResponse> pkgMetadataResponses =
                    packageResolver.resolvePackageMetadata(unresolvedRequests, resolutionOptions);
            addUpdatedPackagesToGraph(pkgMetadataResponses);
            graphBuilder.removeDanglingNodes();

            dumpIntermediateGraph(noOfUpdateAttempts++);
        }
    }

    private void addUpdatedPackagesToGraph(Collection<PackageMetadataResponse> pkgMetadataResponses) {
        for (PackageMetadataResponse resolutionResp : pkgMetadataResponses) {
            if (resolutionResp.resolutionStatus() == ResolutionResponse.ResolutionStatus.UNRESOLVED) {
                // TODO Report diagnostics
                continue;
            }

            addNodeToGraph(resolutionResp);
        }
    }

    private void addNodeToGraph(PackageMetadataResponse resolutionResp) {
        ResolutionRequest resolutionReq = resolutionResp.packageLoadRequest();
        PackageDescriptor pkgDesc = resolutionResp.resolvedDescriptor();
        PackageDependencyScope scope = resolutionReq.scope();
        DependencyResolutionType resolvedType = resolutionReq.resolutionType();

        // Merge the dependency graph only if the node is accepted by the graphBuilder
        NodeStatus nodeStatus = graphBuilder.addResolvedNode(pkgDesc, scope, resolvedType);
        if (nodeStatus == NodeStatus.ACCEPTED) {
            mergeGraph(pkgDesc, resolutionResp.dependencyGraph().orElseThrow(
                    () -> new IllegalStateException("Graph cannot be null in a resolved dependency")),
                    scope, resolvedType);
        }
    }

    private DependencyGraph<DependencyNode> buildFinalDependencyGraph() {
        DependencyGraph<DependencyNode> dependencyGraph = graphBuilder.buildGraph();
        dumpFinalGraph(dependencyGraph);
        return dependencyGraph;
    }

    private void dumpInitialGraph() {
        if (!resolutionOptions.dumpRawGraphs()) {
            return;
        }

        String serializedGraph = serializeRawGraph("Initial");
        console.println("\nResolving dependencies");
        console.println(serializedGraph);
    }

    private void dumpIntermediateGraph(int noOfUpdateAttempts) {
        if (!resolutionOptions.dumpRawGraphs()) {
            return;
        }

        String serializedGraph = serializeRawGraph("Version update attempt " + noOfUpdateAttempts);
        console.println();
        console.println(serializedGraph);
    }

    private void dumpFinalGraph(DependencyGraph<DependencyNode> dependencyGraph) {
        if (!resolutionOptions.dumpGraph() && !resolutionOptions.dumpRawGraphs()) {
            return;
        }

        String serializedGraph;
        if (resolutionOptions.dumpRawGraphs()) {
            serializedGraph = DotGraphs.serializeDependencyNodeGraph(dependencyGraph, "Final");
            console.println();
        } else {
            serializedGraph = DotGraphs.serializeDependencyNodeGraph(dependencyGraph);
            console.println("\nResolving dependencies");
        }
        console.println(serializedGraph);
    }

    private String serializeRawGraph(String graphName) {
        DependencyGraph<DependencyNode> initialGraph = graphBuilder.rawGraph();
        return DotGraphs.serializeDependencyNodeGraph(initialGraph, graphName);
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
