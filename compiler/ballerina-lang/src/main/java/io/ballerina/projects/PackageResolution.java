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

import io.ballerina.projects.DependencyGraph.DependencyGraphBuilder;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.projects.environment.PackageLoadRequest;
import io.ballerina.projects.environment.PackageLoadResponse;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ProjectEnvironment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves dependencies and handles version conflicts in the dependency graph.
 *
 * @since 2.0.0
 */
public class PackageResolution {
    private final PackageContext rootPackageContext;
    private final PackageResolver packageResolver;
    private final DependencyGraph<Package> dependencyGraph;
    private final DependencyResolution dependencyResolution;

    private List<ModuleContext> topologicallySortedModuleList;

    private PackageResolution(PackageContext rootPackageContext) {
        this.rootPackageContext = rootPackageContext;

        ProjectEnvironment projectEnvContext = rootPackageContext.project().projectEnvironmentContext();
        this.packageResolver = projectEnvContext.getService(PackageResolver.class);

        this.dependencyGraph = buildDependencyGraph();
        this.dependencyResolution = new DependencyResolution(projectEnvContext.getService(PackageCache.class),
                dependencyGraph);
        resolveDependencies();
    }

    static PackageResolution from(PackageContext rootPackageContext) {
        return new PackageResolution(rootPackageContext);
    }

    /**
     * Returns the package dependency graph of this package.
     *
     * @return the package dependency graph of this package
     */
    public DependencyGraph<Package> dependencyGraph() {
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
    private DependencyGraph<Package> buildDependencyGraph() {
        // TODO We should get diagnostics as well. Need to design that contract
        DependencyGraph<PackageDescriptor> pkgDescDepGraph = getDependencyGraphWithPackageDescriptors();
        // TODO Check for cycles
        // TODO Check for version conflicts and the perform further conflict resolution
        return createDependencyGraphWithResolvedPackages(pkgDescDepGraph);
    }

    private Set<PackageLoadRequest> getPackageLoadRequestsOfDirectDependencies() {
        Set<ModuleLoadRequest> allModuleLoadRequests = new HashSet<>();
        for (ModuleId moduleId : rootPackageContext.moduleIds()) {
            ModuleContext moduleContext = rootPackageContext.moduleContext(moduleId);
            allModuleLoadRequests.addAll(moduleContext.moduleLoadRequests());
        }

        return getPackageLoadRequestsOfDirectDependencies(allModuleLoadRequests);
    }

    private Set<PackageLoadRequest> getPackageLoadRequestsOfDirectDependencies(
            Set<ModuleLoadRequest> moduleLoadRequests) {
        Set<PackageLoadRequest> packageLoadRequests = new HashSet<>();
        for (ModuleLoadRequest moduleLoadRequest : moduleLoadRequests) {
            Optional<PackageOrg> optionalOrgName = moduleLoadRequest.orgName();
            if (optionalOrgName.isEmpty()) {
                // At the moment we don't check whether the requested module is available
                // in the current package or not. This error will be reported during the SymbolEnter pass.
                continue;
            } else if (optionalOrgName.get().equals(rootPackageContext.packageOrg()) &&
                    rootPackageContext.moduleContext(moduleLoadRequest.moduleName()) != null) {
                continue;
            }

            PackageOrg packageOrg = optionalOrgName.get();
            PackageName packageName = moduleLoadRequest.packageName();
            PackageVersion packageVersion = null;
            for (PackageManifest.Dependency dependency : rootPackageContext.manifest().dependencies()) {
                if (dependency.org().equals(packageOrg) && dependency.name().equals(packageName)) {
                    packageVersion = dependency.version();
                }
            }

            PackageDescriptor pkdDesc = PackageDescriptor.from(packageName, packageOrg, packageVersion);
            PackageLoadRequest packageLoadRequest = PackageLoadRequest.from(pkdDesc);
            packageLoadRequests.add(packageLoadRequest);
        }
        return packageLoadRequests;
    }

    private DependencyGraph<PackageDescriptor> getDependencyGraphWithPackageDescriptors() {
        if (rootPackageContext.project().kind() == ProjectKind.BALR_PROJECT) {
            return rootPackageContext.dependencyGraph();
        } else {
            return createDependencyGraphFromSources();
        }
    }

    private DependencyGraph<PackageDescriptor> createDependencyGraphFromSources() {
        // 1) Get PackageLoadRequests for all the direct dependencies of this package
        Set<PackageLoadRequest> packageLoadRequests = getPackageLoadRequestsOfDirectDependencies();
        // 2) Resolve direct dependencies. My assumption is that, all these dependencies comes from BALRs
        Collection<PackageLoadResponse> packageLoadResponses = packageResolver.resolvePackages(packageLoadRequests);

        PackageDescriptor rootPkgDesc = rootPackageContext.descriptor();
        DependencyGraphBuilder<PackageDescriptor> depGraphBuilder = DependencyGraphBuilder.getBuilder();
        depGraphBuilder.add(rootPkgDesc);
        for (PackageLoadResponse pkgLoadResp : packageLoadResponses) {
            Package directDependency = pkgLoadResp.resolvedPackage();
            PackageDescriptor dependencyDescriptor = directDependency.descriptor();
            depGraphBuilder.addDependency(rootPkgDesc, dependencyDescriptor);

            // Merge direct dependency's dependency graph with the current one.
            depGraphBuilder.mergeGraph(directDependency.packageContext().dependencyGraph());
        }

        // Now we have raw dependency graph that contains all the direct and transitive dependencies of this module
        return depGraphBuilder.build();
    }

    private DependencyGraph<Package> createDependencyGraphWithResolvedPackages(
            DependencyGraph<PackageDescriptor> pkgDescDepGraph) {
        // Now create PackageLoadRequests for all the remaining packages to be loaded in the graph
        Collection<PackageDescriptor> pkgDescGraphNodes = pkgDescDepGraph.getNodes();
        // TODO Filter out direct dependencies that we've already resolved.
        Set<PackageLoadRequest> pkgLoadRequests = pkgDescGraphNodes.stream()
                .map(PackageLoadRequest::from)
                .collect(Collectors.toSet());

        Collection<PackageLoadResponse> pkgLoadResponses = packageResolver.resolvePackages(pkgLoadRequests);

        // TODO Check there there are diagnostics reported by the PackageResolver.
        Map<PackageDescriptor, Package> packageIdMap = pkgLoadResponses.stream()
                .collect(Collectors.toMap(pkgLoadResp -> pkgLoadResp.packageLoadRequest().packageDescriptor(),
                        PackageLoadResponse::resolvedPackage));

        DependencyGraphBuilder<Package> dependencyGraphBuilder = DependencyGraphBuilder.getBuilder();
        for (PackageDescriptor pkgDescGraphNode : pkgDescGraphNodes) {
            Package resolvedPkg = packageIdMap.get(pkgDescGraphNode);
            if (resolvedPkg == null) {
                // TODO This situation cannot happen once we complete the test dependency implementation.
                continue;
            }

            Collection<PackageDescriptor> directDependencies = pkgDescDepGraph.getDirectDependencies(pkgDescGraphNode);
            List<Package> directDepPkgIds = new ArrayList<>(directDependencies.size());
            for (PackageDescriptor directDependency : directDependencies) {
                Package dependencyPkg = packageIdMap.get(directDependency);
                if (dependencyPkg == null) {
                    // TODO This situation cannot happen once we complete the test dependency implementation.
                    continue;
                }
                directDepPkgIds.add(dependencyPkg);
            }
            dependencyGraphBuilder.addDependencies(resolvedPkg, directDepPkgIds);
        }

        return dependencyGraphBuilder.build();
    }

    /**
     * Resolve dependencies of each package, which in turn resolves dependencies of each module.
     * <p>
     * This logic should get packages from the dependency graph, not from the PackageCache.
     * Because PackageCache may contain various versions of a single package,
     * but the dependency graph contains only the resolved version.
     */
    private void resolveDependencies() {
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
        List<Package> sortedPackages = dependencyGraph.toTopologicallySortedList();
        for (Package pkg : sortedPackages) {
            pkg.packageContext().resolveDependencies(dependencyResolution);
            DependencyGraph<ModuleId> moduleDependencyGraph = pkg.moduleDependencyGraph();
            List<ModuleId> sortedModuleIds = moduleDependencyGraph.toTopologicallySortedList();
            for (ModuleId moduleId : sortedModuleIds) {
                ModuleContext moduleContext = pkg.module(moduleId).moduleContext();
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
        private final DependencyGraph<Package> dependencyGraph;

        private DependencyResolution(PackageCache delegate, DependencyGraph<Package> dependencyGraph) {
            this.delegate = delegate;
            this.dependencyGraph = dependencyGraph;
        }

        public Optional<Package> getPackage(PackageOrg packageOrg, PackageName packageName) {
            List<Package> resolvedPackages = delegate.getPackages(packageOrg, packageName);
            for (Package resolvedPackage : resolvedPackages) {
                if (dependencyGraph.contains(resolvedPackage)) {
                    return Optional.of(resolvedPackage);
                }
            }

            // TODO convert this to a debug log
//            throw new IllegalStateException("Cannot find a Package with the organization '" + packageOrg +
//                    "' and name '" + packageName + "'");
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
//            throw new RuntimeException("Cannot find module '" + dependencyModDesc.name() + "' in package ");
            return Optional.of(resolvedModule);
        }
    }
}
