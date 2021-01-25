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
import io.ballerina.projects.environment.ResolutionResponse.ResolutionStatus;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import io.ballerina.projects.internal.PackageDependencyGraphBuilder;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

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
    private final CompilationOptions compilationOptions;
    private final ModuleResolver moduleResolver;
    private final PackageDependencyGraphBuilder depGraphBuilder;

    private List<ModuleContext> topologicallySortedModuleList;
    private Collection<ResolvedPackageDependency> dependenciesWithTransitives;

    private PackageResolution(PackageContext rootPackageContext) {
        this.rootPackageContext = rootPackageContext;
        this.compilationOptions = rootPackageContext.compilationOptions();

        ProjectEnvironment projectEnvContext = rootPackageContext.project().projectEnvironmentContext();
        this.packageResolver = projectEnvContext.getService(PackageResolver.class);
        this.packageCache = projectEnvContext.getService(PackageCache.class);

        this.depGraphBuilder = new PackageDependencyGraphBuilder(rootPackageContext.descriptor());
        this.moduleResolver = new ModuleResolver(packageResolver, rootPackageContext, depGraphBuilder);
        dependencyGraph = buildDependencyGraph();
        DependencyResolution dependencyResolution = new DependencyResolution(
                projectEnvContext.getService(PackageCache.class), moduleResolver, dependencyGraph);
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
     * Returns all the dependencies of this package including it's transitive dependencies.
     *
     * @return all the dependencies of this package including it's transitive dependencies
     */
    public Collection<ResolvedPackageDependency> allDependencies() {
        if (dependenciesWithTransitives != null) {
            return dependenciesWithTransitives;
        }

        dependenciesWithTransitives = dependencyGraph.toTopologicallySortedList()
                .stream()
                // Remove root package from this list.
                .filter(resolvedPkg -> resolvedPkg.packageId() != rootPackageContext.packageId())
                .collect(Collectors.toList());
        return dependenciesWithTransitives;
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
        if (rootPackageContext.project().kind() == ProjectKind.BALR_PROJECT) {
            createDependencyGraphFromBALR();
        } else {
            createDependencyGraphFromSources();
        }

        // Once we reach this section, all the direct dependencies have been resolved
        // Here we resolve all transitive dependencies
        // TODO Check for cycles
        return depGraphBuilder.buildPackageDependencyGraph(rootPackageContext.descriptor(), packageResolver,
                packageCache, rootPackageContext.project());
    }

    private LinkedHashSet<ModuleLoadRequest> getModuleLoadRequestsOfDirectDependencies() {
        LinkedHashSet<ModuleLoadRequest> allModuleLoadRequests = new LinkedHashSet<>();
        for (ModuleId moduleId : rootPackageContext.moduleIds()) {
            ModuleContext moduleContext = rootPackageContext.moduleContext(moduleId);
            allModuleLoadRequests.addAll(moduleContext.populateModuleLoadRequests());
        }

        if (!compilationOptions.skipTests()) {
            for (ModuleId moduleId : rootPackageContext.moduleIds()) {
                ModuleContext moduleContext = rootPackageContext.moduleContext(moduleId);
                allModuleLoadRequests.addAll(moduleContext.populateTestSrcModuleLoadRequests());
            }
        }

        // TODO: Move to compiler extension once new Compiler Extension model is introduced
        if (compilationOptions.observabilityIncluded()) {
            {
                PackageName packageName = PackageName.from(Names.OBSERVE.getValue());
                ModuleLoadRequest observeModuleLoadReq = new ModuleLoadRequest(
                        PackageOrg.from(Names.BALLERINA_INTERNAL_ORG.value), packageName, ModuleName.from(packageName),
                        null, PackageDependencyScope.DEFAULT);
                allModuleLoadRequests.add(observeModuleLoadReq);
            }
            {
                PackageName packageName = PackageName.from(Names.OBSERVE.getValue());
                ModuleLoadRequest observeModuleLoadReq = new ModuleLoadRequest(
                        PackageOrg.from(Names.BALLERINA_ORG.value), packageName, ModuleName.from(packageName),
                        null, PackageDependencyScope.DEFAULT);
                allModuleLoadRequests.add(observeModuleLoadReq);
            }
        }

        return allModuleLoadRequests;
    }

    PackageVersion getVersionFromPackageManifest(PackageOrg requestedPkgOrg, PackageName requestedPkgName) {
        for (PackageManifest.Dependency dependency : rootPackageContext.manifest().dependencies()) {
            if (dependency.org().equals(requestedPkgOrg) && dependency.name().equals(requestedPkgName)) {
                return dependency.version();
            }
        }
        return null;
    }

    private void createDependencyGraphFromBALR() {
        DependencyGraph<PackageDescriptor> dependencyGraphStoredInBALR = rootPackageContext.dependencyGraph();
        Collection<PackageDescriptor> directDependenciesOfBALR =
                dependencyGraphStoredInBALR.getDirectDependencies(rootPackageContext.descriptor());

        // 1) Create ResolutionRequest instances for each direct dependency of the balr
        LinkedHashSet<ResolutionRequest> resolutionRequests = new LinkedHashSet<>();
        for (PackageDescriptor packageDescriptor : directDependenciesOfBALR) {
            resolutionRequests.add(ResolutionRequest.from(packageDescriptor, PackageDependencyScope.DEFAULT));
        }

        // 2) Resolve direct dependencies. My assumption is that, all these dependencies comes from BALRs
        List<ResolutionResponse> resolutionResponses =
                packageResolver.resolvePackages(new ArrayList<>(resolutionRequests), rootPackageContext.project());
        for (ResolutionResponse resolutionResponse : resolutionResponses) {
            if (resolutionResponse.resolutionStatus() == ResolutionStatus.UNRESOLVED) {
                PackageDescriptor dependencyPkgDesc = resolutionResponse.packageLoadRequest().packageDescriptor();
                throw new ProjectException("Dependency cannot be found:" +
                        " org=" + dependencyPkgDesc.org() +
                        ", package=" + dependencyPkgDesc.name() +
                        ", version=" + dependencyPkgDesc.version());
            }
        }

        depGraphBuilder.mergeGraph(rootPackageContext.dependencyGraph(), PackageDependencyScope.DEFAULT);
    }

    private void createDependencyGraphFromSources() {
        // 1) Get PackageLoadRequests for all the direct dependencies of this package
        LinkedHashSet<ModuleLoadRequest> moduleLoadRequests = getModuleLoadRequestsOfDirectDependencies();
        for (ModuleLoadRequest moduleLoadRequest : moduleLoadRequests) {
            PackageOrg packageOrg;
            Optional<PackageOrg> optionalOrgName = moduleLoadRequest.orgName();
            if (optionalOrgName.isEmpty()) {
                if (rootPackageContext.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                    // This is an invalid import in a single file project.
                    continue;
                }
                // At the moment we don't check whether the requested module is available
                // in the current package or not. This error will be reported during the SymbolEnter pass.
                packageOrg = rootPackageContext.packageOrg();
            } else {
                packageOrg = optionalOrgName.get();
            }

            ImportModuleRequest importModuleRequest = new ImportModuleRequest(packageOrg,
                    moduleLoadRequest.moduleName().toString());
            moduleResolver.resolve(importModuleRequest, moduleLoadRequest.scope());
        }
    }

    static Optional<ModuleContext> findModuleInPackage(PackageContext resolvedPackage, String moduleNameStr) {
        PackageName packageName = resolvedPackage.packageName();
        ModuleName moduleName;
        if (packageName.value().equals(moduleNameStr)) {
            moduleName = ModuleName.from(packageName);
        } else {
            String moduleNamePart = moduleNameStr.substring(packageName.value().length() + 1);
            moduleName = ModuleName.from(packageName, moduleNamePart);
        }
        ModuleContext resolvedModule = resolvedPackage.moduleContext(moduleName);
        if (resolvedModule == null) {
            return Optional.empty();
        }

        // TODO convert this to a debug log
        return Optional.of(resolvedModule);
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
        private final ModuleResolver moduleResolver;
        private final DependencyGraph<ResolvedPackageDependency> dependencyGraph;

        private DependencyResolution(PackageCache delegate,
                                     ModuleResolver moduleResolver,
                                     DependencyGraph<ResolvedPackageDependency> dependencyGraph) {
            this.delegate = delegate;
            this.moduleResolver = moduleResolver;
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

        public Optional<ModuleContext> getModule(PackageOrg packageOrg, String moduleNameStr) {
            ImportModuleRequest importModuleRequest = new ImportModuleRequest(packageOrg, moduleNameStr);
            ImportModuleResponse importModuleResponse = moduleResolver.getImportModuleResponse(importModuleRequest);
            if (importModuleResponse == null) {
                return Optional.empty();
            }

            PackageName packageName = importModuleResponse.packageName();
            Optional<Package> optionalPackage = getPackage(packageOrg,
                    packageName);
            if (optionalPackage.isEmpty()) {
                // This branch cannot be executed since the package is resolved before hand
                throw new IllegalStateException("Cannot find the resolved package for org: " + packageOrg +
                        " name: " + packageName);
            }

            return PackageResolution.findModuleInPackage(optionalPackage.get().packageContext(), moduleNameStr);
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

    /**
     * Find packages that contain imported modules.
     *
     * @since 2.0.0
     */
    private class ModuleResolver {
        private final Map<ImportModuleRequest, ImportModuleResponse> responseMap = new HashMap<>();
        private final PackageDependencyGraphBuilder depGraphBuilder;
        private final PackageResolver packageResolver;
        private final PackageContext rootPkgContext;

        private ModuleResolver(PackageResolver packageResolver,
                               PackageContext rootPkgContext,
                               PackageDependencyGraphBuilder depGraphBuilder) {
            this.packageResolver = packageResolver;
            this.rootPkgContext = rootPkgContext;
            this.depGraphBuilder = depGraphBuilder;
        }

        ImportModuleResponse getImportModuleResponse(ImportModuleRequest importModuleRequest) {
            return responseMap.get(importModuleRequest);
        }

        void resolve(ImportModuleRequest importModuleRequest, PackageDependencyScope scope) {
            ImportModuleResponse importModuleResponse = responseMap.get(importModuleRequest);
            if (importModuleResponse != null) {
                return;
            }

            PackageOrg packageOrg = importModuleRequest.packageOrg();
            List<PackageName> possiblePkgNames = getPossiblePackageNames(importModuleRequest);
            for (PackageName possiblePkgName : possiblePkgNames) {
                if (packageOrg.equals(rootPackageContext.packageOrg()) &&
                        possiblePkgName.equals(rootPackageContext.packageName())) {
                    Optional<ModuleContext> moduleInPackage = PackageResolution.findModuleInPackage(
                            rootPackageContext, importModuleRequest.moduleName());
                    if (moduleInPackage.isEmpty()) {
                        // There is no such module in this package
                        // Continue to the next package
                        continue;
                    }
                } else {
                    // Check whether this package is already defined in the package manifest, if so get the version
                    PackageVersion packageVersion = PackageResolution.this.getVersionFromPackageManifest(
                            packageOrg, possiblePkgName);

                    // Try to resolve the package via repositories
                    PackageDescriptor pkgDesc = PackageDescriptor.from(packageOrg, possiblePkgName, packageVersion);
                    ResolutionResponse resolutionResponse = resolvePackage(pkgDesc, scope);
                    if (resolutionResponse.resolutionStatus() == ResolutionStatus.UNRESOLVED) {
                        // There is no such package exists
                        // Continue to the next possible package name
                        continue;
                    }

                    Package resolvedPackage = resolutionResponse.resolvedPackage();
                    Optional<ModuleContext> moduleInPackage = PackageResolution.findModuleInPackage(
                            resolvedPackage.packageContext(), importModuleRequest.moduleName());
                    if (moduleInPackage.isEmpty()) {
                        // There is no such module in this package
                        // Continue to the next package
                        continue;
                    }

                    // The requested module is available in the resolvedPackage
                    // Let's add it to the dependency graph.
                    addPackageToGraph(resolutionResponse);
                }
                responseMap.put(importModuleRequest, new ImportModuleResponse(packageOrg, possiblePkgName));
                return;
            }

            // 3) Imported module cannot be resolved. Ignore the error for now.
            // This will be caught at a different level
        }

        private ResolutionResponse resolvePackage(PackageDescriptor pkgDesc, PackageDependencyScope scope) {
            ResolutionRequest resolutionRequest = ResolutionRequest.from(pkgDesc, scope);
            return packageResolver.resolvePackages(
                    List.of(resolutionRequest), rootPkgContext.project()).get(0);
        }

        private void addPackageToGraph(ResolutionResponse resolutionResponse) {
            // Adding the resolved package to the graph and merge its dependencies
            Package resolvedPackage = resolutionResponse.resolvedPackage();
            ResolutionRequest resolutionRequest = resolutionResponse.packageLoadRequest();
            if (resolutionRequest.scope() == PackageDependencyScope.DEFAULT) {
                depGraphBuilder.addDependency(rootPkgContext.descriptor(),
                        resolvedPackage.descriptor(), PackageDependencyScope.DEFAULT);

                // Merge direct dependency's dependency graph with the current one.
                depGraphBuilder.mergeGraph(resolvedPackage.packageContext().dependencyGraph(),
                        PackageDependencyScope.DEFAULT);
            } else if (resolutionRequest.scope() == PackageDependencyScope.TEST_ONLY) {
                depGraphBuilder.addDependency(rootPkgContext.descriptor(),
                        resolvedPackage.descriptor(), PackageDependencyScope.TEST_ONLY);

                // Merge direct dependency's dependency graph with the current one.
                depGraphBuilder.mergeGraph(resolvedPackage.packageContext().dependencyGraph(),
                        PackageDependencyScope.TEST_ONLY);
            }
        }

        private List<PackageName> getPossiblePackageNames(ImportModuleRequest importModuleRequest) {
            String[] modNameParts = importModuleRequest.moduleName().split("\\.");
            StringJoiner pkgNameBuilder = new StringJoiner(".");
            List<PackageName> possiblePkgNames = new ArrayList<>(modNameParts.length);
            for (String modNamePart : modNameParts) {
                pkgNameBuilder.add(modNamePart);
                possiblePkgNames.add(PackageName.from(pkgNameBuilder.toString()));
            }
            return possiblePkgNames;
        }
    }
}
