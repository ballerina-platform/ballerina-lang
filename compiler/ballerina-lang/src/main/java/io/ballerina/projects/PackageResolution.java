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

import com.google.gson.JsonSyntaxException;
import io.ballerina.projects.DependencyGraph.DependencyGraphBuilder;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ProjectEnvironment;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.internal.BlendedManifest;
import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import io.ballerina.projects.internal.ModuleResolver;
import io.ballerina.projects.internal.PackageContainer;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.PackageResolutionDiagnostic;
import io.ballerina.projects.internal.ResolutionEngine;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;
import io.ballerina.projects.internal.model.BuildJson;
import io.ballerina.projects.internal.repositories.LocalPackageRepository;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectUtils.readBuildJson;

/**
 * Resolves dependencies and handles version conflicts in the dependency graph.
 *
 * @since 2.0.0
 */
public class PackageResolution {
    private final PackageContext rootPackageContext;
    private final BlendedManifest blendedManifest;
    private final DependencyGraph<ResolvedPackageDependency> dependencyGraph;
    private final CompilationOptions compilationOptions;
    private final PackageResolver packageResolver;
    private final ModuleResolver moduleResolver;
    private final List<Diagnostic> diagnosticList;
    private DiagnosticResult diagnosticResult;
    private boolean autoUpdate;
    private String dependencyGraphDump;

    private List<ModuleContext> topologicallySortedModuleList;
    private Collection<ResolvedPackageDependency> dependenciesWithTransitives;

    private PackageResolution(PackageContext rootPackageContext, CompilationOptions compilationOptions) {
        this.rootPackageContext = rootPackageContext;
        this.diagnosticList = new ArrayList<>();
        this.compilationOptions = compilationOptions;
        ResolutionOptions resolutionOptions = getResolutionOptions(rootPackageContext, compilationOptions);
        ProjectEnvironment projectEnvContext = rootPackageContext.project().projectEnvironmentContext();
        this.packageResolver = projectEnvContext.getService(PackageResolver.class);
        this.blendedManifest = createBlendedManifest(rootPackageContext, projectEnvContext);
        diagnosticList.addAll(this.blendedManifest.diagnosticResult().allDiagnostics);

        this.moduleResolver = createModuleResolver(rootPackageContext, projectEnvContext, resolutionOptions);

        this.dependencyGraph = buildDependencyGraph(resolutionOptions);
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

    /**
     * Print the final dependency graph to the provided print stream.
     *
     * @param printStream print stream
     */
    public void dumpGraphs(PrintStream printStream) {
        printStream.append(this.dependencyGraphDump);
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

    public boolean autoUpdate() {
        return autoUpdate;
    }

    private boolean getSticky(PackageContext rootPackageContext) {
        boolean sticky = rootPackageContext.project().buildOptions().sticky();
        if (sticky) {
            this.autoUpdate = false;
            return true;
        }

        // set sticky if `build` file exists and `last_update_time` not passed 24 hours
        if (rootPackageContext.project().kind() == ProjectKind.BUILD_PROJECT) {
            Path buildFilePath = this.rootPackageContext.project().targetDir().resolve(BUILD_FILE);

            if (Files.exists(buildFilePath) && buildFilePath.toFile().length() > 0) {
                try {
                    BuildJson buildJson = readBuildJson(buildFilePath);
                    // if distribution is not same, we anyway return sticky as false
                    if (buildJson != null && buildJson.distributionVersion() != null &&
                            buildJson.distributionVersion().equals(RepoUtils.getBallerinaShortVersion()) &&
                            !buildJson.isExpiredLastUpdateTime()) {
                        this.autoUpdate = false;
                        return true;
                    } else {
                        this.autoUpdate = true;
                        return false;
                    }
                } catch (IOException | JsonSyntaxException e) {
                    this.autoUpdate = true;
                    return false;
                }
            }
            this.autoUpdate = true;
            return false;
        }
        this.autoUpdate = true;
        return false;
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
    private DependencyGraph<ResolvedPackageDependency> buildDependencyGraph(ResolutionOptions resolutionOptions) {
        // TODO We should get diagnostics as well. Need to design that contract
        if (rootPackageContext.project().kind() == ProjectKind.BALA_PROJECT) {
            return resolveBALADependencies(resolutionOptions);
        } else {
            return resolveSourceDependencies(resolutionOptions);
        }
    }

    private LinkedHashSet<ModuleLoadRequest> getModuleLoadRequestsOfDirectDependencies() {
        LinkedHashSet<ModuleLoadRequest> allModuleLoadRequests = new ModuleContext.OverwritableLinkedHashSet();
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
        if ("k8s".equals(compilationOptions.getCloud()) || "docker".equals(compilationOptions.getCloud()) ||
                "choreo".equals(compilationOptions.getCloud())) {
            String moduleName = Names.CLOUD.getValue();
            ModuleLoadRequest c2cModuleLoadReq = new ModuleLoadRequest(
                    PackageOrg.from(Names.BALLERINA_ORG.value), moduleName,
                    PackageDependencyScope.DEFAULT, DependencyResolutionType.COMPILER_PLUGIN);
            allModuleLoadRequests.add(c2cModuleLoadReq);
        }

        return allModuleLoadRequests;
    }

    private DependencyGraph<ResolvedPackageDependency> resolveBALADependencies(ResolutionOptions resolutionOptions) {
        // 1) Convert package descriptor graph to DependencyNode graph
        DependencyGraph<DependencyNode> dependencyNodeGraph = createDependencyNodeGraph(
                rootPackageContext.dependencyGraph());

        //2 ) Create the package dependency graph by downloading packages if necessary.
        return buildPackageGraph(dependencyNodeGraph, rootPackageContext.project().currentPackage(),
                packageResolver, resolutionOptions);
    }

    private DependencyGraph<ResolvedPackageDependency> resolveSourceDependencies(ResolutionOptions resolutionOptions) {
        // 1) Get PackageLoadRequests for all the direct dependencies of this package
        LinkedHashSet<ModuleLoadRequest> moduleLoadRequests = getModuleLoadRequestsOfDirectDependencies();

        // 2) Resolve imports to packages and create the complete dependency graph with package metadata
        ResolutionEngine resolutionEngine = new ResolutionEngine(rootPackageContext.descriptor(),
                blendedManifest, packageResolver, moduleResolver, resolutionOptions);
        DependencyGraph<DependencyNode> dependencyNodeGraph =
                resolutionEngine.resolveDependencies(moduleLoadRequests);
        this.dependencyGraphDump = resolutionEngine.dumpGraphs();

        diagnosticList.addAll(resolutionEngine.diagnosticResult().allDiagnostics);

        //3 ) Create the package dependency graph by downloading packages if necessary.
        return buildPackageGraph(dependencyNodeGraph, rootPackageContext.project().currentPackage(),
                packageResolver, resolutionOptions);
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

    private DependencyGraph<ResolvedPackageDependency> buildPackageGraph(DependencyGraph<DependencyNode> depGraph,
                                                                         Package rootPackage,
                                                                         PackageResolver packageResolver,
                                                                         ResolutionOptions resolutionOptions) {
        PackageContainer<ResolvedPackageDependency> resolvedPkgContainer = new PackageContainer<>();

        // Add root node to the container
        DependencyNode rootNode = depGraph.getRoot();
        ResolvedPackageDependency rootResolvedPackage = new ResolvedPackageDependency(rootPackage,
                rootNode.scope(), rootNode.resolutionType());
        resolvedPkgContainer.add(rootNode.pkgDesc().org(), rootNode.pkgDesc().name(), rootResolvedPackage);

        // Resolve rest of the packages in the graph
        List<ResolutionRequest> resolutionRequests = depGraph.getNodes().stream()
                .filter(depNode -> !depNode.equals(rootNode) // Remove root node from the requests
                        && !depNode.errorNode()) // Remove error nodes from the requests
                .map(this::createFromDepNode)
                .collect(Collectors.toList());
        Collection<ResolutionResponse> resolutionResponses =
                packageResolver.resolvePackages(resolutionRequests, resolutionOptions);

        // Add resolved packages to the container
        for (ResolutionResponse resolutionResp : resolutionResponses) {
            if (resolutionResp.resolutionStatus().equals(ResolutionResponse.ResolutionStatus.RESOLVED)) {
                PackageDescriptor pkgDesc = resolutionResp.responseDescriptor();
                ResolutionRequest resolutionReq = resolutionResp.resolutionRequest();
                ResolvedPackageDependency resolvedPkg = new ResolvedPackageDependency(
                        resolutionResp.resolvedPackage(),
                        resolutionReq.scope(),
                        resolutionReq.resolutionType());
                resolvedPkgContainer.add(pkgDesc.org(), pkgDesc.name(), resolvedPkg);
            }
        }

        // Build the resolved package dependency graph
        DependencyGraphBuilder<ResolvedPackageDependency> depGraphBuilder =
                DependencyGraphBuilder.getBuilder(rootResolvedPackage);
        for (DependencyNode depNode : depGraph.getNodes()) {
            Optional<ResolvedPackageDependency> resolvedPkgOptional = resolvedPkgContainer.get(
                    depNode.pkgDesc().org(), depNode.pkgDesc().name());
            if (resolvedPkgOptional.isPresent()) {
                ResolvedPackageDependency resolvedPkg = resolvedPkgOptional.get();
                depGraphBuilder.add(resolvedPkg);

                List<ResolvedPackageDependency> directPkgDependencies =
                        depGraph.getDirectDependencies(depNode)
                                .stream()
                                .map(directDepNode -> resolvedPkgContainer.get(
                                        directDepNode.pkgDesc().org(), directDepNode.pkgDesc().name()))
                                .flatMap(Optional::stream)
                                .collect(Collectors.toList());
                depGraphBuilder.addDependencies(resolvedPkg, directPkgDependencies);
            }
        }
        return depGraphBuilder.build();
    }

    private ResolutionRequest createFromDepNode(DependencyNode depNode) {
        return ResolutionRequest.from(depNode.pkgDesc(), depNode.scope(), depNode.resolutionType());
    }

    private DependencyGraph<DependencyNode> createDependencyNodeGraph(
            DependencyGraph<PackageDescriptor> pkgDescDepGraph) {
        DependencyNode rootNode = new DependencyNode(rootPackageContext.descriptor());

        DependencyGraphBuilder<DependencyNode> graphBuilder = DependencyGraphBuilder.getBuilder(rootNode);
        for (PackageDescriptor pkgDesc : pkgDescDepGraph.getNodes()) {
            DependencyNode dependencyNode = new DependencyNode(pkgDesc);
            graphBuilder.add(dependencyNode);
            for (PackageDescriptor directDepPkgDesc : pkgDescDepGraph.getDirectDependencies(pkgDesc)) {
                graphBuilder.addDependency(dependencyNode, new DependencyNode(directDepPkgDesc));
            }
        }

        return graphBuilder.build();
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
        if (dependencyGraph.findCycles().size() > 0) {
            for (List<ResolvedPackageDependency> cycle: dependencyGraph.findCycles()) {
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                        DiagnosticErrorCode.CYCLIC_MODULE_IMPORTS_DETECTED.diagnosticId(),
                        "cyclic module imports detected ''"
                                + cycle.stream()
                                .map(dependency -> dependency.packageInstance().descriptor().toString())
                                .collect(Collectors.joining(" -> ")) + "''",
                        DiagnosticErrorCode.CYCLIC_MODULE_IMPORTS_DETECTED.severity());
                PackageResolutionDiagnostic diagnostic = new PackageResolutionDiagnostic(diagnosticInfo,
                        rootPackageContext.descriptor().name().toString());
                diagnosticList.add(diagnostic);
            }
        }
        for (ResolvedPackageDependency pkgDependency : sortedPackages) {
            Package resolvedPackage = pkgDependency.packageInstance();
            resolvedPackage.packageContext().resolveDependencies(dependencyResolution);
            DependencyGraph<ModuleDescriptor> moduleDependencyGraph = resolvedPackage.moduleDependencyGraph();
            List<ModuleDescriptor> sortedModuleDescriptors
                    = moduleDependencyGraph.toTopologicallySortedList();
            if (moduleDependencyGraph.findCycles().size() > 0) {
                for (List<ModuleDescriptor> cycle: moduleDependencyGraph.findCycles()) {
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                            DiagnosticErrorCode.CYCLIC_MODULE_IMPORTS_DETECTED.diagnosticId(),
                            "cyclic module imports detected ''"
                                    + cycle.stream()
                                    .map(desc -> desc.org().toString() + "/" + desc.name().toString() + ":"
                                            + desc.version().toString())
                                    .collect(Collectors.joining(" -> ")) + "''",
                            DiagnosticErrorCode.CYCLIC_MODULE_IMPORTS_DETECTED.severity());
                    PackageResolutionDiagnostic diagnostic = new PackageResolutionDiagnostic(diagnosticInfo,
                            resolvedPackage.descriptor().name().toString());
                    diagnosticList.add(diagnostic);
                }
            }
            for (ModuleDescriptor moduleDescriptor : sortedModuleDescriptors) {
                ModuleContext moduleContext = resolvedPackage.module(moduleDescriptor.name()).moduleContext();
                sortedModuleList.add(moduleContext);
            }
        }
        this.topologicallySortedModuleList = Collections.unmodifiableList(sortedModuleList);
    }

    private ModuleResolver createModuleResolver(PackageContext rootPackageContext,
                                                ProjectEnvironment projectEnvContext,
                                                ResolutionOptions resolutionOptions) {
        List<ModuleName> moduleNames = rootPackageContext.moduleIds().stream()
                .map(rootPackageContext::moduleContext)
                .map(ModuleContext::moduleName)
                .collect(Collectors.toList());
        return new ModuleResolver(rootPackageContext.descriptor(), moduleNames, blendedManifest,
                projectEnvContext.getService(PackageResolver.class), resolutionOptions);
    }

    private BlendedManifest createBlendedManifest(PackageContext rootPackageContext,
                                                  ProjectEnvironment projectEnvContext) {
        return BlendedManifest.from(rootPackageContext.dependencyManifest(),
                rootPackageContext.packageManifest(),
                projectEnvContext.getService(LocalPackageRepository.class));
    }

    private ResolutionOptions getResolutionOptions(PackageContext rootPackageContext,
                                                   CompilationOptions compilationOptions) {
        return ResolutionOptions.builder()
                .setOffline(compilationOptions.offlineBuild())
                .setSticky(getSticky(rootPackageContext))
                .setDumpGraph(compilationOptions.dumpGraph())
                .setDumpRawGraphs(compilationOptions.dumpRawGraphs())
                .build();
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
}
