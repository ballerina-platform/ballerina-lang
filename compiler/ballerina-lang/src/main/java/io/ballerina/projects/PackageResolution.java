/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects;

import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ProjectEnvironment;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.internal.PackageDependencyGraphBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves dependencies and handles version conflicts in the dependency graph.
 *
 * @since 2.0.0
 */
public class PackageResolution {
    private final PackageContext rootPackageContext;
    private final PackageCache packageCache;
    private final PackageResolver packageResolver;
    private final DependencyGraph<ResolvedPackageDependency> dependencyGraph;

    private List<ModuleContext> topologicallySortedModuleList;

    private PackageResolution(PackageContext rootPackageContext) {
        this.rootPackageContext = rootPackageContext;

        ProjectEnvironment projectEnvContext = rootPackageContext.project().projectEnvironmentContext();
        this.packageResolver = projectEnvContext.getService(PackageResolver.class);
        this.packageCache = projectEnvContext.getService(PackageCache.class);

        dependencyGraph = buildDependencyGraph();
        DependencyResolution dependencyResolution = new DependencyResolution(
                projectEnvContext.getService(PackageCache.class), dependencyGraph);
        resolveDependencies(dependencyResolution);
    }

    static PackageResolution from(PackageContext rootPackageContext) {
        return new PackageResolution(rootPackageContext);
    }

    /**
     * Returns the package dependency graph of this package.
     *
     * @return the package dependency graph of this package
     */
    public DependencyGraph<ResolvedPackageDependency> dependencyGraph() {
        return dependencyGraph;
    }

    /**
     * Returns the module dependency graph of a given package.
     * <p>
     * This graph contains only the modules of the given package.
     *
     * @param packageId unique instance id of the package
     * @return module dependency graph
     */
    public DependencyGraph<ModuleId> moduleDependencyGraph(PackageId packageId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    PackageContext packageContext() {
        return rootPackageContext;
    }

    List<ModuleContext> topologicallySortedModuleList() {
        return topologicallySortedModuleList;
    }

    /**
     * The goal of this method is to build the complete package dependency graph of this package.
     * 1) Combine {@code ModuleLoadRequest}s of all the modules in this package.
     * 2) Filter out such requests that does not requests modules of this package.
     * 3) Create {@code PackageLoadRequest}s by incorporating the versions specified in Ballerina.toml file.
     * <p>
     * Now you have a set of PackageLoadRequests that contains all the direct dependencies of this package.
     * Load allbthese packages using the PackageResolver service. With this model PackageResolver does not
     * need to be aware of the current package. Once all the direct dependencies are loaded,
     * combine there dependency graphs into a single that contains all the transitives.
     * Now check for cycles and version conflicts. Once the version conflicts are resolved, return the graph.
     *
     * @return package dependency graph of this package
     */
    private DependencyGraph<ResolvedPackageDependency> buildDependencyGraph() {
        // TODO We should get diagnostics as well. Need to design that contract
        return getDependencyGraphWithPackageDescriptors();
        // TODO Check for cycles
    }

    private Set<ResolutionRequest> getPackageLoadRequestsOfDirectDependencies() {
        Set<ModuleLoadRequest> allModuleLoadRequests = new HashSet<>();
        for (ModuleId moduleId : rootPackageContext.moduleIds()) {
            ModuleContext moduleContext = rootPackageContext.moduleContext(moduleId);
            allModuleLoadRequests.addAll(moduleContext.moduleLoadRequests());
        }

        return getPackageLoadRequestsOfDirectDependencies(allModuleLoadRequests);
    }

    private Set<ResolutionRequest> getPackageLoadRequestsOfDirectDependencies(
            Set<ModuleLoadRequest> moduleLoadRequests) {
        Set<ResolutionRequest> resolutionRequests = new HashSet<>();
        for (ModuleLoadRequest moduleLoadRequest : moduleLoadRequests) {
            Optional<PackageOrg> optionalOrgName = moduleLoadRequest.orgName();
            if (optionalOrgName.isEmpty()) {
                // At the moment we don't check whether the requested module is available
                // in the current package or not. This error will be reported during the SymbolEnter pass.
                continue;
            }

            PackageOrg requestedPkgOrg = optionalOrgName.get();
            PackageName requestedPkgName = moduleLoadRequest.packageName();
            if (rootPackageContext.packageOrg().equals(requestedPkgOrg) &&
                    rootPackageContext.packageName().equals(requestedPkgName)) {
                // If the requested module belongs to this package, continue.
                // We don't have to check whether this module exists, an error will be logged later in SymbolEnter.
                continue;
            }

            PackageVersion requestedPkgVersion = getVersionFromPackageManifest(
                    requestedPkgOrg, requestedPkgName);
            PackageDescriptor pkdDesc = PackageDescriptor.from(requestedPkgOrg,
                    requestedPkgName, requestedPkgVersion);
            ResolutionRequest packageLoadRequest = ResolutionRequest.from(pkdDesc);
            resolutionRequests.add(packageLoadRequest);
        }
        return resolutionRequests;
    }

    private PackageVersion getVersionFromPackageManifest(PackageOrg requestedPkgOrg, PackageName requestedPkgName) {
        for (PackageManifest.Dependency dependency : rootPackageContext.manifest().dependencies()) {
            if (dependency.org().equals(requestedPkgOrg) && dependency.name().equals(requestedPkgName)) {
                return dependency.version();
            }
        }
        return null;
    }

    private DependencyGraph<ResolvedPackageDependency> getDependencyGraphWithPackageDescriptors() {
        PackageDependencyGraphBuilder depGraphBuilder = PackageDependencyGraphBuilder.getInstance();
        if (rootPackageContext.project().kind() == ProjectKind.BALR_PROJECT) {
            depGraphBuilder.mergeGraph(rootPackageContext.dependencyGraph());
        } else {
            createDependencyGraphFromSources(depGraphBuilder);
        }

        return depGraphBuilder.buildPackageDependencyGraph(rootPackageContext.descriptor(), packageResolver,
                packageCache, rootPackageContext.project());
    }

    private void createDependencyGraphFromSources(PackageDependencyGraphBuilder depGraphBuilder) {
        // 1) Get PackageLoadRequests for all the direct dependencies of this package
        Set<ResolutionRequest> packageLoadRequests = getPackageLoadRequestsOfDirectDependencies();

        // 2) Resolve direct dependencies. My assumption is that, all these dependencies comes from BALRs
        Collection<ResolutionResponse> resolutionResponses =
                packageResolver.resolvePackages(packageLoadRequests, rootPackageContext.project());

        PackageDescriptor rootPkgDesc = rootPackageContext.descriptor();
        depGraphBuilder.addNode(rootPkgDesc);
        for (ResolutionResponse resolutionResponse : resolutionResponses) {
            if (resolutionResponse.resolutionStatus() == ResolutionResponse.ResolutionStatus.UNRESOLVED) {
                // We don't log errors for unresolved direct dependencies
                continue;
            }
            Package directDependency = resolutionResponse.resolvedPackage();
            PackageDescriptor dependencyDescriptor = directDependency.descriptor();
            depGraphBuilder.addDependency(rootPkgDesc, dependencyDescriptor);

            // Merge direct dependency's dependency graph with the current one.
            depGraphBuilder.mergeGraph(directDependency.packageContext().dependencyGraph());
        }

        // Now we have raw dependency graph that contains all the direct and transitive dependencies of this module
    }

    /**
     * Resolve dependencies of each package, which in turn resolves dependencies of each module.
     * <p>
     * This logic should get packages from the dependency graph, not from the PackageCache.
     * Because PackageCache may contain various versions of a single package,
     * but the dependency graph contains only the resolved version.
     */
    private void resolveDependencies(DependencyResolution dependencyResolution) {
        // Topologically sort packages in the package dependency graph.
        // Iterate through the sorted package list
        // Resolve each package
        // Get the module dependency graph of the package.
        // This graph should only contain the modules in that particular package.
        // Topologically sort the module dependency graph.
        // Iterate through the sorted module list.
        // Compile the module and collect diagnostics.
        // Repeat this for each module in each package in the package dependency graph.
        List<ModuleContext> sortedModuleList = new ArrayList<>();
        List<ResolvedPackageDependency> sortedPackages = dependencyGraph.toTopologicallySortedList();
        for (ResolvedPackageDependency pkgDependency : sortedPackages) {
            Package resolvedPackage = pkgDependency.packageInstance();
            resolvedPackage.packageContext().resolveDependencies(dependencyResolution);
            DependencyGraph<ModuleId> moduleDependencyGraph = resolvedPackage.moduleDependencyGraph();
            List<ModuleId> sortedModuleIds = moduleDependencyGraph.toTopologicallySortedList();
            for (ModuleId moduleId : sortedModuleIds) {
                ModuleContext moduleContext = resolvedPackage.module(moduleId).moduleContext();
                sortedModuleList.add(moduleContext);
            }
        }
        this.topologicallySortedModuleList = Collections.unmodifiableList(sortedModuleList);
    }

    /**
     * This entity is used by packages and modules to resolve their dependencies from the dependency graph.
     *
     * @since 2.0.0
     */
    static class DependencyResolution {
        private final PackageCache delegate;
        private final DependencyGraph<ResolvedPackageDependency> dependencyGraph;

        private DependencyResolution(PackageCache delegate,
                                     DependencyGraph<ResolvedPackageDependency> dependencyGraph) {
            this.delegate = delegate;
            this.dependencyGraph = dependencyGraph;
        }

        public Optional<Package> getPackage(PackageOrg packageOrg, PackageName packageName) {
            List<Package> resolvedPackages = delegate.getPackages(packageOrg, packageName);
            for (Package resolvedPackage : resolvedPackages) {
                if (containsPackage(resolvedPackage)) {
                    return Optional.of(resolvedPackage);
                }
            }

            // TODO convert this to a debug log
            return Optional.empty();
        }

        public Optional<Module> getModule(PackageOrg packageOrg, PackageName packageName, ModuleName moduleName) {
            Optional<Package> resolvedPkg = getPackage(packageOrg, packageName);
            if (resolvedPkg.isEmpty()) {
                return Optional.empty();
            }

            Module resolvedModule = resolvedPkg.get().module(moduleName);
            if (resolvedModule == null) {
                return Optional.empty();
            }

            // TODO convert this to a debug log
            return Optional.of(resolvedModule);
        }

        private boolean containsPackage(Package pkg) {
            for (ResolvedPackageDependency graphNode : dependencyGraph.getNodes()) {
                if (graphNode.packageId() == pkg.packageId()) {
                    return true;
                }
            }
            return false;
        }
    }
}
