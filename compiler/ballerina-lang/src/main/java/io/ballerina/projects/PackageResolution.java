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
import io.ballerina.projects.environment.ResolutionResponse.ResolutionStatus;
import io.ballerina.projects.internal.BlendedManifest;
import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import io.ballerina.projects.internal.PackageContainer;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ResolutionEngine;
import io.ballerina.projects.internal.repositories.LocalPackageRepository;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Resolves dependencies and handles version conflicts in the dependency graph.
 *
 * @since 2.0.0
 */
public class PackageResolution {
    private final PackageContext rootPackageContext;
    private final DependencyManifest dependencyManifest;
    private final BlendedManifest blendedManifest;
    private final DependencyGraph<ResolvedPackageDependency> dependencyGraph;
    private final CompilationOptions compilationOptions;
    private final PackageResolver packageResolver;
    private final ModuleResolver moduleResolver;
    private final List<Diagnostic> diagnosticList;
    private DiagnosticResult diagnosticResult;

    private List<ModuleContext> topologicallySortedModuleList;
    private Collection<ResolvedPackageDependency> dependenciesWithTransitives;

    private PackageResolution(PackageContext rootPackageContext, CompilationOptions compilationOptions) {
        this.rootPackageContext = rootPackageContext;
        this.dependencyManifest = rootPackageContext.dependencyManifest();
        this.diagnosticList = new ArrayList<>();
        this.compilationOptions = compilationOptions;

        ProjectEnvironment projectEnvContext = rootPackageContext.project().projectEnvironmentContext();
        this.packageResolver = projectEnvContext.getService(PackageResolver.class);
        this.blendedManifest = BlendedManifest.from(rootPackageContext.packageManifest(),
                rootPackageContext.dependencyManifest(), projectEnvContext.getService(LocalPackageRepository.class));

        this.moduleResolver = new ModuleResolver(projectEnvContext.getService(PackageResolver.class));

        boolean sticky = rootPackageContext.project().buildOptions().sticky();
        dependencyGraph = buildDependencyGraph(sticky, compilationOptions.offlineBuild());
        DependencyResolution dependencyResolution = new DependencyResolution(
                projectEnvContext.getService(PackageCache.class), moduleResolver, dependencyGraph);
        resolveDependencies(dependencyResolution);
    }

    static PackageResolution from(PackageContext rootPackageContext, CompilationOptions compilationOptions) {
        return new PackageResolution(rootPackageContext, compilationOptions);
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

    PackageContext packageContext() {
        return rootPackageContext;
    }

    List<ModuleContext> topologicallySortedModuleList() {
        return topologicallySortedModuleList;
    }

    public DiagnosticResult diagnosticResult() {
        if (this.diagnosticResult == null) {
            this.diagnosticResult = new DefaultDiagnosticResult(this.diagnosticList);
        }
        return diagnosticResult;
    }

    void reportDiagnostic(String message, String diagnosticErrorCode, DiagnosticSeverity severity, Location location,
                          ModuleDescriptor moduleDescriptor) {
        var diagnosticInfo = new DiagnosticInfo(diagnosticErrorCode, message, severity);
        var diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo, location);
        var packageDiagnostic = new PackageDiagnostic(diagnostic, moduleDescriptor, rootPackageContext.project());
        this.diagnosticList.add(packageDiagnostic);
        this.diagnosticResult = new DefaultDiagnosticResult(this.diagnosticList);
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
    private DependencyGraph<ResolvedPackageDependency> buildDependencyGraph(boolean sticky, boolean offline) {
        // TODO We should get diagnostics as well. Need to design that contract
        if (rootPackageContext.project().kind() == ProjectKind.BALA_PROJECT) {
            return createDependencyGraphFromBALA(offline);
        } else {
            return createDependencyGraphFromSources(sticky, offline);
        }
    }

    private LinkedHashSet<ModuleLoadRequest> getModuleLoadRequestsOfDirectDependencies() {
        LinkedHashSet<ModuleLoadRequest> allModuleLoadRequests = new LinkedHashSet<>();
        for (ModuleId moduleId : rootPackageContext.moduleIds()) {
            ModuleContext moduleContext = rootPackageContext.moduleContext(moduleId);
            allModuleLoadRequests.addAll(moduleContext.populateModuleLoadRequests());
        }

        for (ModuleId moduleId : rootPackageContext.moduleIds()) {
            ModuleContext moduleContext = rootPackageContext.moduleContext(moduleId);
            allModuleLoadRequests.addAll(moduleContext.populateTestSrcModuleLoadRequests());
        }

        // TODO: Move to compiler extension once new Compiler Extension model is introduced
        if (compilationOptions.observabilityIncluded()) {
            {
                String moduleName = Names.OBSERVE.getValue();
                ModuleLoadRequest observeModuleLoadReq = new ModuleLoadRequest(
                        PackageOrg.from(Names.BALLERINA_INTERNAL_ORG.value), moduleName,
                        PackageDependencyScope.DEFAULT, DependencyResolutionType.PLATFORM_PROVIDED);
                allModuleLoadRequests.add(observeModuleLoadReq);
            }
        }

        // TODO Can we make this a builtin compiler plugin
        if ("k8s".equals(compilationOptions.getCloud()) || "docker".equals(compilationOptions.getCloud())) {
            String moduleName = Names.CLOUD.getValue();
            ModuleLoadRequest c2cModuleLoadReq = new ModuleLoadRequest(
                    PackageOrg.from(Names.BALLERINA_ORG.value), moduleName,
                    PackageDependencyScope.DEFAULT, DependencyResolutionType.COMPILER_PLUGIN);
            allModuleLoadRequests.add(c2cModuleLoadReq);
        }

        return allModuleLoadRequests;
    }

    private DependencyGraph<ResolvedPackageDependency> createDependencyGraphFromBALA(boolean offline) {
        DependencyGraph<PackageDescriptor> dependencyGraphStoredInBALA = rootPackageContext.dependencyGraph();
        Collection<PackageDescriptor> directDependenciesOfBALA =
                dependencyGraphStoredInBALA.getDirectDependencies(rootPackageContext.descriptor());

        List<ResolutionEngine.DependencyNode> directDeps = new ArrayList<>();
        for (PackageDescriptor pkgDesc : directDependenciesOfBALA) {
            directDeps.add(new ResolutionEngine.DependencyNode(pkgDesc, PackageDependencyScope.DEFAULT,
                    DependencyResolutionType.SOURCE));
        }

        ResolutionEngine resolutionEngine = new ResolutionEngine(rootPackageContext.descriptor(), dependencyManifest,
                packageResolver, offline, true);
        resolutionEngine.resolveDependencies(directDeps);
        return resolutionEngine.getPackageDependencyGraph(rootPackageContext.project());
    }

    DependencyGraph<ResolvedPackageDependency> createDependencyGraphFromSources(boolean sticky, boolean offline) {
        // 1) Get PackageLoadRequests for all the direct dependencies of this package
        LinkedHashSet<ModuleLoadRequest> moduleLoadRequests = getModuleLoadRequestsOfDirectDependencies();

        // Get the direct dependencies of the current package.
        // This list does not contain langlib and the root package.
        PackageContainer<DirectPackageDependency> directDepsContainer =
                moduleResolver.resolveModuleLoadRequests(moduleLoadRequests);

        List<ResolutionEngine.DependencyNode> directDeps = new ArrayList<>();
        for (DirectPackageDependency directPkgDependency : directDepsContainer.getAll()) {
            PackageVersion depVersion;
            String repository;
            PackageDescriptor depPkgDesc = directPkgDependency.pkgDesc();
            if (directPkgDependency.dependencyKind() == DirectPackageDependencyKind.NEW) {
                // This blendedDep may be resolved from the local repository as well.
                Optional<BlendedManifest.Dependency> blendedDepOptional = blendedManifest.dependency(
                        depPkgDesc.org(), depPkgDesc.name());

                // If the package version is not null, use it
                if (directPkgDependency.pkgDesc.version() != null) {
                    depVersion = directPkgDependency.pkgDesc.version();
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
            } else if (directPkgDependency.dependencyKind() == DirectPackageDependencyKind.EXISTING) {
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

        ResolutionEngine resolutionEngine = new ResolutionEngine(rootPackageContext.descriptor(), dependencyManifest,
                packageResolver, offline, sticky);
        resolutionEngine.resolveDependencies(directDeps);
        return resolutionEngine.getPackageDependencyGraph(rootPackageContext.project());
    }

    static Optional<ModuleContext> findModuleInPackage(PackageContext resolvedPackage, String moduleNameStr) {
        PackageName packageName = resolvedPackage.packageName();
        ModuleName moduleName;
        if (packageName.value().equals(moduleNameStr)) {
            moduleName = ModuleName.from(packageName);
        } else {
            String moduleNamePart = moduleNameStr.substring(packageName.value().length() + 1);
            if (moduleNamePart.isEmpty()) {
                moduleNamePart = null;
            }
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

            PackageName packageName;
            // TODO remove the null check and else block once the new resolution is fully done
            packageName = importModuleResponse.packageDescriptor().name();

            Optional<Package> optionalPackage = getPackage(packageOrg,
                                                           packageName);
            if (optionalPackage.isEmpty()) {
                return Optional.empty();
                // This branch cannot be executed since the package is resolved before hand
//                throw new IllegalStateException("Cannot find the resolved package for org: " + packageOrg +
//                        ", name: " + packageName);
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
        private final PackageResolver packageResolver;

        private ModuleResolver(PackageResolver packageResolver) {
            this.packageResolver = packageResolver;
        }

        ImportModuleResponse getImportModuleResponse(ImportModuleRequest importModuleRequest) {
            return responseMap.get(importModuleRequest);
        }

        PackageOrg getPackageOrg(ModuleLoadRequest moduleLoadRequest) {
            Optional<PackageOrg> optionalOrgName = moduleLoadRequest.orgName();
            return optionalOrgName.orElseGet(rootPackageContext::packageOrg);
        }

        /**
         * Resolves the given list of module names to Packages and returns that list.
         * <p>
         * The returned package list does not contain langlib packages and the root package.
         */
        PackageContainer<DirectPackageDependency> resolveModuleLoadRequests(
                Collection<ModuleLoadRequest> moduleLoadRequests) {
            PackageContainer<DirectPackageDependency> pkgContainer = new PackageContainer<>();
            List<ImportModuleRequest> unresolvedModuleRequests = new ArrayList<>();
            for (ModuleLoadRequest moduleLoadRequest : moduleLoadRequests) {
                resolveModuleLoadRequest(moduleLoadRequest, pkgContainer, unresolvedModuleRequests);
            }

            // Resolve unresolved import module declarations
            List<ImportModuleResponse> importModResponses =
                    packageResolver.resolvePackageNames(unresolvedModuleRequests);
            for (ImportModuleResponse importModResp : importModResponses) {
                if (importModResp.resolutionStatus() == ResolutionStatus.UNRESOLVED) {
                    // TODO Report diagnostics
                    // TODO Require a proper package.properties file.
                    continue;
                }

                DirectPackageDependency newPkgDep;
                ImportModuleRequest importModuleRequest = importModResp.importModuleRequest();
                PackageDescriptor pkgDesc = importModResp.packageDescriptor();
                Optional<DirectPackageDependency> pkgDepOptional = pkgContainer.get(pkgDesc.org(), pkgDesc.name());
                ModuleLoadRequest moduleLoadRequest = importModuleRequest.moduleLoadRequest();
                if (pkgDepOptional.isEmpty()) {
                    newPkgDep = new DirectPackageDependency(pkgDesc,
                            DirectPackageDependencyKind.NEW,
                            moduleLoadRequest.scope(),
                            moduleLoadRequest.dependencyResolvedType());
                } else {
                    // This block is executed when there are two or more import module declarations requests
                    //  for modules in the same package.
                    // Say package foo contains two modules foo and foo.bar and
                    //  module foo is already added to the pkgContainer,
                    //  and in a new version of package foo there exists module foo.bar
                    DirectPackageDependency currentPkgDep = pkgDepOptional.get();

                    // Do not override the scope, if the current scope is PackageDependencyScope.DEFAULT,
                    PackageDependencyScope scope =
                            currentPkgDep.scope() == PackageDependencyScope.DEFAULT ?
                                    PackageDependencyScope.DEFAULT :
                                    moduleLoadRequest.scope();

                    // Do not override the resolutionType,
                    // if the current resolutionType is DependencyResolutionType.SOURCE ,
                    DependencyResolutionType resolutionType =
                            currentPkgDep.resolutionType() == DependencyResolutionType.SOURCE ?
                                    DependencyResolutionType.SOURCE :
                                    moduleLoadRequest.dependencyResolvedType();
                    newPkgDep = new DirectPackageDependency(pkgDesc,
                            DirectPackageDependencyKind.NEW, scope, resolutionType);
                }
                pkgContainer.add(pkgDesc.org(), pkgDesc.name(), newPkgDep);
                responseMap.put(importModuleRequest, importModResp);
            }
            return pkgContainer;
        }

        private void resolveModuleLoadRequest(ModuleLoadRequest moduleLoadRequest,
                                              PackageContainer<DirectPackageDependency> pkgContainer,
                                              List<ImportModuleRequest> unresolvedModuleRequests) {
            PackageDescriptor pkgDesc;
            PackageOrg pkgOrg = getPackageOrg(moduleLoadRequest);
            String moduleName = moduleLoadRequest.moduleName();
            Collection<PackageName> possiblePkgNames = ProjectUtils.getPossiblePackageNames(
                    pkgOrg, moduleLoadRequest.moduleName());
            if (ProjectUtils.isBuiltInPackage(pkgOrg, moduleName)) {
                pkgDesc = PackageDescriptor.from(pkgOrg, PackageName.from(moduleName));
            } else {
                if (possiblePkgNames.size() == 1) {
                    // This is not a hierarchical module pkgName,
                    // hence the package pkgName is same as the module pkgName
                    PackageName pkgName = PackageName.from(moduleName);
                    pkgDesc = createPkgDesc(pkgOrg, pkgName,
                            blendedManifest.dependency(pkgOrg, pkgName).orElse(null));
                } else {
                    // This is a hierarchical module pkgName
                    // This method returns a non-null pkgDesc if and only if that package contains the given module.
                    pkgDesc = findHierarchicalModule(moduleName, pkgOrg, possiblePkgNames);
                }
            }

            if (pkgDesc == null) {
                // TODO How can we use use possiblePackages?
                List<PackageDescriptor> possiblePackages = Collections.emptyList();
                ImportModuleRequest importModuleRequest = new ImportModuleRequest(
                        pkgOrg, moduleLoadRequest, possiblePackages);
                unresolvedModuleRequests.add(importModuleRequest);
                return;
            }

            PackageName pkgName = pkgDesc.name();
            if (isRootPackage(pkgOrg, pkgName)) {
                // Do not add the root package to the dependencies list
                ImportModuleRequest importModuleRequest = new ImportModuleRequest(pkgOrg, moduleLoadRequest);
                responseMap.put(importModuleRequest, new ImportModuleResponse(
                        PackageDescriptor.from(pkgOrg, pkgName), importModuleRequest));
                return;
            }

            // pkgDesc.version() == null is always true
            Optional<DirectPackageDependency> pkgDepOptional = pkgContainer.get(pkgOrg, pkgName);
            if (pkgDepOptional.isEmpty()) {
                DirectPackageDependencyKind dependencyKind;
                if (dependencyManifest.dependency(pkgDesc.org(), pkgDesc.name()).isEmpty()) {
                    // If the Dependencies.toml does not contain the package, it is a new one.
                    dependencyKind = DirectPackageDependencyKind.NEW;
                } else {
                    dependencyKind = DirectPackageDependencyKind.EXISTING;
                }
                pkgContainer.add(pkgOrg, pkgName, new DirectPackageDependency(pkgDesc,
                        dependencyKind, moduleLoadRequest.scope(),
                        moduleLoadRequest.dependencyResolvedType()));
            } else {
                // This block is executed when there are two or more import module declarations requests
                //  for modules in the same package.
                // Say package foo contains two modules foo and foo.bar and
                //  module foo is already added to the pkgContainer,
                //  and this block will be executed for the module foo.bar.

                // There exists a direct dependency in the container
                DirectPackageDependency currentPkgDep = pkgDepOptional.get();

                // Use the current resolutionType only if it is DependencyResolutionType.SOURCE,
                //  Override it otherwise.
                DependencyResolutionType resolutionType =
                        currentPkgDep.resolutionType() == DependencyResolutionType.SOURCE ?
                                DependencyResolutionType.SOURCE :
                                moduleLoadRequest.dependencyResolvedType();

                // Use the current scope only if it is PackageDependencyScope.DEFAULT,
                //  Override it otherwise.
                PackageDependencyScope scope =
                        currentPkgDep.scope() == PackageDependencyScope.DEFAULT ?
                                PackageDependencyScope.DEFAULT :
                                moduleLoadRequest.scope();

                // Use the current DirectPackageDependencyKind only if it is DirectPackageDependencyKind.EXISTING
                DirectPackageDependencyKind directDepKind =
                        currentPkgDep.dependencyKind() == DirectPackageDependencyKind.EXISTING ?
                                DirectPackageDependencyKind.EXISTING :
                                currentPkgDep.dependencyKind();
                pkgContainer.add(pkgOrg, pkgName,
                        new DirectPackageDependency(pkgDesc, directDepKind, scope, resolutionType));
            }

            ImportModuleRequest importModuleRequest = new ImportModuleRequest(pkgOrg, moduleLoadRequest);
            responseMap.put(importModuleRequest,
                    new ImportModuleResponse(pkgDesc, importModuleRequest));
        }

        private PackageDescriptor findHierarchicalModule(String moduleName,
                                                         PackageOrg packageOrg,
                                                         Collection<PackageName> possiblePkgNames) {
            for (PackageName possiblePkgName : possiblePkgNames) {
                PackageDescriptor pkgDesc = findHierarchicalModule(moduleName, packageOrg, possiblePkgName);
                if (pkgDesc != null) {
                    return pkgDesc;
                }
            }

            return null;
        }

        /**
         * Find the given module name in the dependencies.toml file recorded during the previous compilation.
         *
         * @param moduleName  Module name to be found
         * @param packageOrg  organization name
         * @param packageName Possible package name
         * @return PackageDescriptor or null
         */
        private PackageDescriptor findHierarchicalModule(String moduleName,
                                                         PackageOrg packageOrg,
                                                         PackageName packageName) {
            PackageDescriptor pkgDesc = findModuleInRootPackage(moduleName, packageOrg, packageName);
            if (pkgDesc != null) {
                return pkgDesc;
            }

            return findModuleInBlendedManifest(moduleName, packageOrg, packageName);
        }

        private PackageDescriptor findModuleInRootPackage(String moduleName,
                                                          PackageOrg packageOrg,
                                                          PackageName packageName) {
            if (packageOrg.equals(rootPackageContext.packageOrg()) &&
                    packageName.equals(rootPackageContext.packageName())) {
                Optional<ModuleContext> moduleInPackage = PackageResolution.findModuleInPackage(
                        rootPackageContext, moduleName);
                if (moduleInPackage.isPresent()) {
                    return rootPackageContext.descriptor();
                }
                // There is no such module in root package
            }
            return null;
        }

        private boolean isRootPackage(PackageOrg pkgOrg, PackageName pkgName) {
            return pkgOrg.equals(rootPackageContext.packageOrg()) &&
                    pkgName.equals(rootPackageContext.packageName());
        }

        private PackageDescriptor findModuleInBlendedManifest(String moduleName,
                                                              PackageOrg packageOrg,
                                                              PackageName packageName) {
            // Check whether this package is already defined in the package manifest, if so get the version
            Optional<BlendedManifest.Dependency> blendedDep =
                    PackageResolution.this.blendedManifest.dependency(packageOrg, packageName);
            if (blendedDep.isPresent() && blendedDep.get().moduleNames().contains(moduleName)) {
                return createPkgDesc(packageOrg, packageName, blendedDep.get());
            } else {
                return null;
            }
        }

        private PackageDescriptor createPkgDesc(PackageOrg packageOrg,
                                                PackageName packageName,
                                                BlendedManifest.Dependency blendedDep) {
            if (blendedDep != null && blendedDep.isFromLocalRepository()) {
                return PackageDescriptor.from(packageOrg, packageName, blendedDep.version(),
                        ProjectConstants.LOCAL_REPOSITORY_NAME);
            } else {
                return PackageDescriptor.from(packageOrg, packageName,
                        blendedDep != null ? blendedDep.version() : null);
            }
        }
    }

    private static class DirectPackageDependency {
        private final PackageDescriptor pkgDesc;
        private final DirectPackageDependencyKind dependencyKind;
        private final PackageDependencyScope scope;
        private final DependencyResolutionType resolutionType;

        public DirectPackageDependency(PackageDescriptor pkgDesc,
                                       DirectPackageDependencyKind dependencyKind,
                                       PackageDependencyScope scope,
                                       DependencyResolutionType resolutionType) {
            this.pkgDesc = pkgDesc;
            this.dependencyKind = dependencyKind;
            this.scope = scope;
            this.resolutionType = resolutionType;
        }

        public PackageDescriptor pkgDesc() {
            return pkgDesc;
        }

        public DirectPackageDependencyKind dependencyKind() {
            return dependencyKind;
        }

        public PackageDependencyScope scope() {
            return scope;
        }

        public DependencyResolutionType resolutionType() {
            return resolutionType;
        }
    }

    private enum DirectPackageDependencyKind {
        /**
         * A dependency already recorded in Dependencies.toml.
         */
        EXISTING,
        /**
         * A new package dependency introduced via a new import declaration.
         */
        NEW
    }
}
