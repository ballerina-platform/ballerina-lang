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
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.SemanticVersion.VersionCompatibilityResult;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

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
    private String dependencyGraphDump;
    private DiagnosticResult diagnosticResult;
    private Collection<DependencyNode> directDependencies;

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
        this.graphBuilder = new PackageDependencyGraphBuilder(rootPkgDesc);
        this.diagnostics = new ArrayList<>();
        this.dependencyGraphDump = "";
        this.directDependencies = new ArrayList<>();
    }

    public DiagnosticResult diagnosticResult() {
        if (diagnosticResult == null) {
            diagnosticResult = new DefaultDiagnosticResult(diagnostics);
        }
        return diagnosticResult;
    }

    public DependencyGraph<DependencyNode> resolveDependencies(Collection<ModuleLoadRequest> moduleLoadRequests) {
        // 1) Resolve import declarations into Packages.
        directDependencies = resolvePackages(moduleLoadRequests);

        PackageLockingModeMatrix lockingModes = getLockingModes(directDependencies);
        Queue<UnresolvedNode> unresolvedNodes = new LinkedList<>();
        initializeDirectDependencies(directDependencies, lockingModes, unresolvedNodes);
        processUnresolvedNodes(lockingModes, unresolvedNodes);
        return buildFinalDependencyGraph();
    }

    private PackageLockingModeMatrix getLockingModes(Collection<DependencyNode> directDependencies) {
        boolean importAddition = areNewDirectDependenciesAdded(directDependencies);
        PackageLockingModeResolver packageLockingModeResolver = new PackageLockingModeResolver(
                resolutionOptions.lockingModeResolutionOptions());
        return packageLockingModeResolver.resolveLockingModes(importAddition);
    }

    private void initializeDirectDependencies(
            Collection<DependencyNode> directDependencies,
            PackageLockingModeMatrix lockingModeMap,
            Queue<UnresolvedNode> unresolvedNodes) {
        for (DependencyNode directDependency : directDependencies) {
            PackageLockingMode directDepLockingMode = isDirectDependencyNewlyAdded(directDependency) ?
                    lockingModeMap.newDirectDepMode() : lockingModeMap.existingDirectDepMode();
            graphBuilder.addDirectDependency(directDependency);
            unresolvedNodes.add(new UnresolvedNode(directDependency, directDepLockingMode));
        }
    }

    // TODO: this method is ugly :/
    private void processUnresolvedNodes(
            PackageLockingModeMatrix lockingModeMap,
            Queue<UnresolvedNode> unresolvedNodes) {
        while (!unresolvedNodes.isEmpty()) {
            // TODO: improvement: built-in packages go through the queue unnecessarily.
            //  Make a cache of them when gone through the queue once and use it.
            UnresolvedNode unresolvedNode = unresolvedNodes.remove();
            DependencyNode pkgNode = unresolvedNode.dependencyNode();
            PackageDescriptor packageToResolve = pkgNode.pkgDesc();

            PackageLockingMode packageLockingMode = unresolvedNode.packageLockingMode();
            PackageVersion nodeVersion = packageToResolve.version();
            BlendedManifest.Dependency manifestPackage = blendedManifest.dependency(
                    packageToResolve.org(), packageToResolve.name()).orElse(null);
            PackageVersion manifestVersion = manifestPackage != null ? manifestPackage.version() : null;

            PackageVersion graphVersion = Optional.ofNullable(
                    graphBuilder.getDependency(packageToResolve.org(), packageToResolve.name()))
                    .map(PackageDescriptor::version)
                    .orElse(null); // Replace `DependencyType` with the actual type of the returned object
            boolean isCustomOrLocalRepo = manifestPackage != null && (manifestPackage.isFromLocalRepository() ||
                    manifestPackage.isFromCustomRepository());
            // look at pkg, manifestPkg, packageLockingMode, graphBuilder and decide the reference version to be passed.
            Optional<PackageVersion> version = getCurrentlyBestVersion(nodeVersion, manifestVersion, graphVersion,
                    packageLockingMode, isCustomOrLocalRepo);
            if (version.isPresent()) {
                if (packageToResolve.repository().isEmpty()) { // central repo
                    packageToResolve = PackageDescriptor.from(packageToResolve.org(), packageToResolve.name(),
                            version.get(), packageToResolve.getDeprecated(), packageToResolve.getDeprecationMsg());
                } else { // custom/ local repo
                    packageToResolve = PackageDescriptor.from(packageToResolve.org(), packageToResolve.name(),
                            version.get(), packageToResolve.repository().get());
                }
            }
            ResolutionRequest resolutionRequest = ResolutionRequest.from(
                    packageToResolve, pkgNode.scope(), pkgNode.resolutionType(), packageLockingMode);
            PackageMetadataResponse response = packageResolver.resolvePackageMetadata(
                    resolutionRequest, resolutionOptions);
            if (response.resolutionStatus() == ResolutionResponse.ResolutionStatus.UNRESOLVED) {
                // TODO add diagnostic
                DependencyNode errorNode = new DependencyNode(
                        pkgNode.pkgDesc(),
                        pkgNode.scope(),
                        pkgNode.resolutionType(),
                        true);
                graphBuilder.addVertex(errorNode);
                continue;
            }
            DependencyNode updatedPkgNode = new DependencyNode(
                    response.resolvedDescriptor(),
                    pkgNode.scope(),
                    pkgNode.resolutionType());
            graphBuilder.addVertex(updatedPkgNode);
            for (PackageDescriptor dep : response.directDependencies()) {
                // If there is a higher version of the dependency is already in the graph, we use that.
                PackageVersion depVersion = dep.version();
                PackageDescriptor currentIndexDependency = graphBuilder.getDependency(dep.org(), dep.name());
                if (currentIndexDependency != null
                        &&  currentIndexDependency.version() != null
                        && currentIndexDependency.version().value().greaterThanOrEqualTo(dep.version().value())) {
                    depVersion = currentIndexDependency.version();
                }
                PackageDescriptor depDesc = PackageDescriptor.from(dep.org(), dep.name(), depVersion);
                // TODO: handle different resolution types
                // TODO: the scope of the dependency is currently not recorded in the index. Can we safely do that?
                //  The testOnly scoped packages won't be needed by any transitive dependencies.
                DependencyNode depNode = new DependencyNode(depDesc, pkgNode.scope(), pkgNode.resolutionType());
                PackageLockingMode transitiveDepLockingMode = isNewDependency(depNode) ?
                        lockingModeMap.newTransitiveDepMode() : lockingModeMap.existingTransitiveDepMode();
                unresolvedNodes.add(new UnresolvedNode(depNode, transitiveDepLockingMode));
                graphBuilder.addDependency(updatedPkgNode, depNode);
            }
        }
    }

    private DependencyGraph<DependencyNode> buildFinalDependencyGraph() {
        DependencyGraph<DependencyNode> dependencyGraph = graphBuilder.buildGraph();
//        this.diagnostics.addAll(graphBuilder.diagnostics()); // TODO: add diagnostics to the graph
        dumpDependencyGraph(dependencyGraph);
        return dependencyGraph;
    }

    private Optional<PackageVersion> getCurrentlyBestVersion(
            PackageVersion queuedVersion,
            PackageVersion manifestVersion,
            PackageVersion graphVersion,
            PackageLockingMode packageLockingMode,
            boolean isCustomOrLocalRepo) {
        if (packageLockingMode.equals(PackageLockingMode.INVALID)) {
            throw new ProjectException("Invalid state");
        } // TODO: have proper errors with the reason for the invalid state.
        if (packageLockingMode.equals(PackageLockingMode.LOCKED)) {
            if (manifestVersion == null) {
                throw new ProjectException("Cannot have new dependencies with the LOCKED update policy");
            }
            return Optional.of(manifestVersion);
        }
        if (isCustomOrLocalRepo) {
            return Optional.ofNullable(manifestVersion);
        }

        Optional<PackageVersion> candidatePkg = Optional.ofNullable(queuedVersion);

        candidatePkg = compareAndUpdate(candidatePkg, manifestVersion, "manifest");
        candidatePkg = compareAndUpdate(candidatePkg, graphVersion, "graph");

        return candidatePkg;
    }

    private Optional<PackageVersion> compareAndUpdate(
            Optional<PackageVersion> candidatePkg,
            PackageVersion newVersion,
            String versionType) {

        if (newVersion == null) {
            return candidatePkg;
        }

        if (candidatePkg.isEmpty()) {
            return Optional.of(newVersion);
        }

        SemanticVersion.VersionCompatibilityResult comparisonResult = candidatePkg.get().value()
                .compareTo(newVersion.value());

        if (comparisonResult == VersionCompatibilityResult.LESS_THAN) {
            return Optional.of(newVersion);
        } else if (comparisonResult == VersionCompatibilityResult.INCOMPATIBLE) {
            throw new ProjectException("Incompatible versions '" + newVersion + "', '"
                    + candidatePkg.get() + "' found in the index for package type: '" + versionType + "'");
        }

        return candidatePkg;
    }

    private boolean areNewDirectDependenciesAdded(Collection<DependencyNode> directDependencies) {
        for (DependencyNode directDependency: directDependencies) {
            if (isDirectDependencyNewlyAdded(directDependency)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given direct dependency is a newly added dependency. This will return true if the
     * dependency is added newly as a direct dependency or a previous transitive dependency is now a direct dependency.
     * Note that the passed dependency will be considered as one of the direct dependencies without validation.
     *
     * @param directDependency direct dependency that needs to be checked for novelty.
     * @return if the dependency is new or not
     */
    private boolean isDirectDependencyNewlyAdded(DependencyNode directDependency) {
        if (isNewDependency(directDependency)) {
            return true;
        }
        // If the dependency is not new, check if it was a transitive dependency and now a direct dependency.
        BlendedManifest.Dependency manifestDep = blendedManifest.dependency(
                directDependency.pkgDesc().org(), directDependency.pkgDesc().name()).orElseThrow();
        return manifestDep.relation().equals(BlendedManifest.DependencyRelation.TRANSITIVE) ||
                !manifestDep.version().equals(directDependency.pkgDesc().version());
    }

    private boolean isNewDependency(DependencyNode dependency) {
        Optional<BlendedManifest.Dependency> manifestDep = blendedManifest.dependency(
                dependency.pkgDesc().org(), dependency.pkgDesc().name());
        return manifestDep.isEmpty();
    }

    private Collection<DependencyNode> resolvePackages(Collection<ModuleLoadRequest> moduleLoadRequests) {
        // Get the direct dependencies of the current package.
        // This list does not contain langlib and the root package.
        PackageContainer<ModuleResolver.DirectPackageDependency> directDepsContainer =
                moduleResolver.resolveModuleLoadRequests(moduleLoadRequests);

        List<DependencyNode> directDeps = new ArrayList<>();
        for (ModuleResolver.DirectPackageDependency directPkgDependency : directDepsContainer.getAll()) {
            PackageVersion depVersion;
            String repository;
            boolean errorNode = false;
            PackageDescriptor depPkgDesc = directPkgDependency.pkgDesc();
            if (directPkgDependency.dependencyKind() == ModuleResolver.DirectPackageDependencyKind.NEW) {
                // This blendedDep may be resolved from the local repository as well.
                Optional<BlendedManifest.Dependency> blendedDepOptional = blendedManifest.dependency(
                        depPkgDesc.org(), depPkgDesc.name());
                if (blendedDepOptional.isPresent()) {
                    errorNode = blendedDepOptional.get().isError();
                }

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
                errorNode = blendedDep.isError();
            } else {
                throw new IllegalStateException("Unsupported direct dependency kind: " +
                        directPkgDependency.dependencyKind());
            }
            directDeps.add(new DependencyNode(
                    PackageDescriptor.from(depPkgDesc.org(), depPkgDesc.name(), depVersion, repository),
                    directPkgDependency.scope(), directPkgDependency.resolutionType(), errorNode));
        }

        return directDeps;
    }

    private void dumpDependencyGraph(DependencyGraph<DependencyNode> dependencyGraph) {
        if (!resolutionOptions.dumpGraph() && !resolutionOptions.dumpRawGraphs()) {
            return;
        }
        // get all unresolved dependencies
       Collection<DependencyNode> unresolvedDeps = dependencyGraph
               .getAllDependencies(dependencyGraph.getRoot())
               .stream().filter(DependencyNode::errorNode)
               .toList();

        // get unresolved direct dependencies
        Collection<DependencyNode> unresolvedDirectDeps = dependencyGraph
                .getDirectDependencies(dependencyGraph.getRoot())
                .stream().filter(DependencyNode::errorNode)
                .toList();

        String serializedGraph = DotGraphs.serializeDependencyNodeGraph(
                dependencyGraph, unresolvedDeps, unresolvedDirectDeps);
        dependencyGraphDump += "\n";
        dependencyGraphDump += (serializedGraph + "\n");
    }

    public String dumpGraphs() {
        return dependencyGraphDump;
    }

    /**
     * Represents a PackageDependency in the context of dependency resolution.
     *
     * @since 2.0.0
     */
    public static class DependencyNode implements Comparable<DependencyNode> {
        private final PackageDescriptor pkgDesc;
        private final PackageDependencyScope scope;
        private final DependencyResolutionType resolutionType;
        private final boolean isError;

        public DependencyNode(PackageDescriptor pkgDesc,
                              PackageDependencyScope scope,
                              DependencyResolutionType resolutionType) {
            this.pkgDesc = Objects.requireNonNull(pkgDesc);
            this.scope = Objects.requireNonNull(scope);
            this.resolutionType = Objects.requireNonNull(resolutionType);
            this.isError = false;
        }

        public DependencyNode(PackageDescriptor pkgDesc,
                              PackageDependencyScope scope,
                              DependencyResolutionType resolutionType,
                              boolean errorNode) {
            this.pkgDesc = Objects.requireNonNull(pkgDesc);
            this.scope = Objects.requireNonNull(scope);
            this.resolutionType = Objects.requireNonNull(resolutionType);
            this.isError = errorNode;
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

        public boolean errorNode() {
            return isError;
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
                    resolutionType == that.resolutionType &&
                    isError == that.isError;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pkgDesc, scope, resolutionType);
        }

        @Override
        public String toString() {
            String attr = " [scope=" + scope + ",kind=" + resolutionType +
                    ",repo=" + pkgDesc.repository().orElse(null) + ",error=" + isError + "]";
            return pkgDesc + attr;
        }

        @Override
        public int compareTo(DependencyNode other) {
            return this.pkgDesc.toString().compareTo(other.pkgDesc.toString());
        }
    }

    /**
     * Represents an unresolved node that goes in the queue in the context of dependency resolution.
     *
     * @param dependencyNode The dependency node that needs to be resolved
     * @param packageLockingMode The locking mode of the package
     */
    private record UnresolvedNode(
            ResolutionEngine.DependencyNode dependencyNode,
            PackageLockingMode packageLockingMode) {
    }
}
