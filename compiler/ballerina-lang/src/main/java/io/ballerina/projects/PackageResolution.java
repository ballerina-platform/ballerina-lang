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
import io.ballerina.projects.environment.PackageLockingMode;
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
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.internal.ResolutionEngine;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;
import io.ballerina.projects.internal.model.BuildJson;
import io.ballerina.projects.internal.repositories.CustomPkgRepositoryContainer;
import io.ballerina.projects.internal.repositories.LocalPackageRepository;
import io.ballerina.projects.internal.repositories.MavenPackageRepository;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.projects.util.ProjectConstants.BALLERINA_HOME;
import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;
import static io.ballerina.projects.util.ProjectConstants.EQUAL;
import static io.ballerina.projects.util.ProjectConstants.LOCKING_MODE_OPTION;
import static io.ballerina.projects.util.ProjectConstants.OFFLINE_FLAG;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORY_FLAG;
import static io.ballerina.projects.util.ProjectUtils.getWarningForHigherDistribution;
import static io.ballerina.projects.util.ProjectUtils.isNewUpdateDistribution;

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
    private ResolutionOptions resolutionOptions;
    private final PackageResolver packageResolver;
    private final ModuleResolver moduleResolver;
    private final List<Diagnostic> diagnosticList;
    private DiagnosticResult diagnosticResult;
    private String dependencyGraphDump;

    private List<ModuleContext> topologicallySortedModuleList;
    private Collection<ResolvedPackageDependency> dependenciesWithTransitives;
    private List<String> imports;

    private static final Boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.getDefault())
            .contains("win");
    private PackageResolution(PackageContext rootPackageContext, CompilationOptions compilationOptions) {
        this.rootPackageContext = rootPackageContext;
        this.diagnosticList = new ArrayList<>();
        this.compilationOptions = compilationOptions;
        this.resolutionOptions = getResolutionOptions(compilationOptions);
        ProjectEnvironment projectEnvContext = rootPackageContext.project().projectEnvironmentContext();
        this.packageResolver = projectEnvContext.getService(PackageResolver.class);
        this.blendedManifest = createBlendedManifest(rootPackageContext, projectEnvContext,
                this.resolutionOptions.offline());
        diagnosticList.addAll(this.blendedManifest.diagnosticResult().allDiagnostics);
        this.moduleResolver = createModuleResolver(rootPackageContext, projectEnvContext);
        this.dependencyGraph = buildDependencyGraph();
        DependencyResolution dependencyResolution = new DependencyResolution(
                projectEnvContext.getService(PackageCache.class), moduleResolver, dependencyGraph);
        resolveDependencies(dependencyResolution);
        if (compilationOptions.optimizeDependencyCompilation()) {
            generateCaches();
        }
    }

    private PackageResolution(PackageResolution packageResolution, PackageContext rootPackageContext,
                              CompilationOptions compilationOptions) {
        this.rootPackageContext = rootPackageContext;
        this.diagnosticList = new ArrayList<>();
        this.compilationOptions = compilationOptions;
        this.resolutionOptions = packageResolution.resolutionOptions;
        ProjectEnvironment projectEnvContext = rootPackageContext.project().projectEnvironmentContext();
        this.packageResolver = projectEnvContext.getService(PackageResolver.class);
        this.blendedManifest = createBlendedManifest(rootPackageContext, projectEnvContext,
                this.resolutionOptions.offline());
        diagnosticList.addAll(this.blendedManifest.diagnosticResult().allDiagnostics);
        this.moduleResolver = createModuleResolver(rootPackageContext, projectEnvContext);
        LinkedHashSet<ModuleLoadRequest> moduleLoadRequests = getModuleLoadRequestsOfDirectDependencies();
        moduleResolver.resolveModuleLoadRequests(moduleLoadRequests);
        this.dependencyGraph = cloneDependencyGraphNewRoot(packageResolution.dependencyGraph,
                rootPackageContext.project().currentPackage());
        this.dependencyGraphDump = packageResolution.dependencyGraphDump;
        DependencyResolution dependencyResolution = new DependencyResolution(
                projectEnvContext.getService(PackageCache.class), moduleResolver, dependencyGraph);
        resolveDependencies(dependencyResolution);
    }

    /**
     * This method performs the BIR generation before the compilation by
     * spinning up a new process.
     * This is typically useful for large packages to avoid OOM issues.
     */
    private void generateCaches() {
        for (ResolvedPackageDependency resolvedPackageDependency : this.dependencyGraph.toTopologicallySortedList()) {
            Package packageInstance = resolvedPackageDependency.packageInstance();

            // If the package instance is the current package, we have reached the root of the dependency graph.
            // We skip the generation of the cache for the current package.
            PackageDescriptor packageDescriptor = packageInstance.descriptor();
            if (packageDescriptor == this.rootPackageContext.descriptor()) {
                break;
            }

            // If the dependency is not loaded from sources, then we assume that the BIR is already generated.
            // We skip the cache generation for the particular dependency.
            if (packageInstance.getDefaultModule().moduleContext()
                    .currentCompilationState() != ModuleCompilationState.LOADED_FROM_SOURCES) {
                continue;
            }

            if (packageInstance.project().kind().equals(ProjectKind.BUILD_PROJECT)) {
                continue;
            }

            // We use the pull command to generate the BIR of the dependency.
            List<String> cmdArgs = new ArrayList<>();
            String balExecutable = isWindows ? "bal.bat" : "bal";
            cmdArgs.add(Paths.get(System.getProperty(BALLERINA_HOME), "bin", balExecutable).toString());
            cmdArgs.add("pull");
            cmdArgs.add(LOCKING_MODE_OPTION + EQUAL + resolutionOptions.packageLockingMode().toString());
            cmdArgs.add(OFFLINE_FLAG + EQUAL + resolutionOptions.offline());

            // Specify which repository to resolve the dependency from
            Optional<BlendedManifest.Dependency> dependency =
                    blendedManifest.userSpecifiedDependency(packageDescriptor.org(), packageDescriptor.name());
            if (dependency.isPresent() && dependency.get().repository() != null) {
                cmdArgs.add(REPOSITORY_FLAG + EQUAL + dependency.get().repository());
            }
            cmdArgs.add(packageDescriptor.toString());

            ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs);
            try {
                Process process = processBuilder.start();
                int i = process.waitFor();
                if (i != 0) {
                    String errMessage = packageDescriptor.toString();
                    if (dependency.isPresent()) {
                        errMessage += " [repository=" + dependency.get().repository() + "]";
                    }
                    throw new ProjectException("failed to compile " + errMessage);
                }
            } catch (IOException | InterruptedException e) {
                throw new ProjectException(e);
            }

            // Finally, we set the compilation state of the dependency to LOADED_FROM_CACHE
            for (ModuleId moduleId : packageInstance.moduleIds()) {
                packageInstance.module(moduleId).moduleContext()
                        .setCompilationState(ModuleCompilationState.LOADED_FROM_CACHE);
            }
        }
    }

    private DependencyGraph<ResolvedPackageDependency> cloneDependencyGraphNewRoot
            (DependencyGraph<ResolvedPackageDependency> depGraph, Package rootPackage) {
        ResolvedPackageDependency oldRoot = depGraph.getRoot();
        ResolvedPackageDependency newRoot = new ResolvedPackageDependency(rootPackage,
                oldRoot.scope(), oldRoot.dependencyResolvedType());
        DependencyGraphBuilder<ResolvedPackageDependency> depGraphBuilder =
                DependencyGraphBuilder.getBuilder(newRoot);
        for (ResolvedPackageDependency depNode : depGraph.getNodes()) {
            if (depNode == oldRoot) {
                depGraphBuilder.add(newRoot);
            } else {
                depGraphBuilder.add(depNode);
            }
            List<ResolvedPackageDependency> directPkgDependencies =
                    depGraph.getDirectDependencies(depNode)
                            .stream()
                            .map(directDepNode -> directDepNode == oldRoot ? newRoot : directDepNode)
                            .collect(Collectors.toList());
            depGraphBuilder.addDependencies(depNode, directPkgDependencies);
        }
        return depGraphBuilder.build();
    }

    static PackageResolution from(PackageContext rootPackageContext, CompilationOptions compilationOptions) {
        return new PackageResolution(rootPackageContext, compilationOptions);
    }

    static PackageResolution from(PackageResolution packageResolution, PackageContext
            packageContext, CompilationOptions compilationOptions) {
        return new PackageResolution(packageResolution, packageContext, compilationOptions);
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
                .toList();
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
                          PackageDescriptor packageDescriptor) {
        var diagnosticInfo = new DiagnosticInfo(diagnosticErrorCode, message, severity);
        PackageDiagnostic diagnostic = new PackageDiagnostic(diagnosticInfo,
                packageDescriptor.name().toString());
        this.diagnosticList.add(diagnostic);
        this.diagnosticResult = new DefaultDiagnosticResult(this.diagnosticList);
    }

    @Deprecated(forRemoval = true, since = "2201.13.0")
    public boolean autoUpdate() {
        return true;
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
        if (rootPackageContext.project().kind() == ProjectKind.BALA_PROJECT &&
                (PackageLockingMode.HARD.equals(this.resolutionOptions.packageLockingMode()) ||
                        PackageLockingMode.LOCKED.equals(this.resolutionOptions.packageLockingMode()))) {
            return resolveBALADependencies();
        } else {
            return resolveSourceDependencies();
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
        if (rootPackageContext.project().buildOptions().observabilityIncluded()) {
            String moduleName = Names.OBSERVE.getValue();
            ModuleLoadRequest observeModuleLoadReq = new ModuleLoadRequest(
                        PackageOrg.from(Names.BALLERINA_INTERNAL_ORG.value), moduleName,
                        PackageDependencyScope.DEFAULT, DependencyResolutionType.PLATFORM_PROVIDED);
            allModuleLoadRequests.add(observeModuleLoadReq);
        }

        // TODO Can we make this a builtin compiler plugin
        List<String> cloudOptions = List.of("k8s", "docker", "choreo", "openshift");
        if (cloudOptions.contains(compilationOptions.getCloud())) {
            String moduleName = Names.CLOUD.getValue();
            ModuleLoadRequest c2cModuleLoadReq = new ModuleLoadRequest(
                    PackageOrg.from(Names.BALLERINA_ORG.value), moduleName,
                    PackageDependencyScope.DEFAULT, DependencyResolutionType.COMPILER_PLUGIN);
            allModuleLoadRequests.add(c2cModuleLoadReq);
        }
        return allModuleLoadRequests;
    }

    private DependencyGraph<ResolvedPackageDependency> resolveBALADependencies() {
        // 1) Convert package descriptor graph to DependencyNode graph
        DependencyGraph<DependencyNode> dependencyNodeGraph = createDependencyNodeGraph(
                rootPackageContext.dependencyGraph());

        //2 ) Create the package dependency graph by downloading packages if necessary.
        return buildPackageGraph(dependencyNodeGraph, rootPackageContext.project().currentPackage(),
                packageResolver);
    }

    private void updateResolutionOptions() {
        Project project = this.rootPackageContext.project();

        if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            this.resolutionOptions = ResolutionOptions.builder()
                    .setOffline(resolutionOptions.offline())
                    .setDumpGraph(resolutionOptions.dumpGraph())
                    .setDumpRawGraphs(resolutionOptions.dumpRawGraphs())
                    .setPackageLockingMode(PackageLockingMode.SOFT)
                    .build();
            return;
        }

        PackageLockingMode packageLockingMode = resolutionOptions.packageLockingMode();
        boolean isExistingPackage = rootPackageContext.dependenciesTomlContext().isPresent();
        if (isExistingPackage) {
            SemanticVersion prevDistributionVersion = rootPackageContext.dependencyManifest().distributionVersion();
            SemanticVersion currentDistributionVersion = SemanticVersion.from(RepoUtils.getBallerinaShortVersion());
            if (prevDistributionVersion == null) {
                // Built with Update 4 or less
                if (PackageLockingMode.SOFT != packageLockingMode) {
                    //  issue a warning unless the update policy is set to SOFT
                    getWarningForHigherDistribution(project, packageLockingMode).ifPresent(diagnosticList::add);
                    if (packageLockingMode == null) {
                        packageLockingMode = PackageLockingMode.SOFT;
                    }
                }
            } else if (isNewUpdateDistribution(prevDistributionVersion, currentDistributionVersion)) {
                // Built with Update 5 or above, but older than the current Update distribution
                if (PackageLockingMode.SOFT != packageLockingMode) {
                    //  issue a warning unless the update policy is set to SOFT
                    getWarningForHigherDistribution(project, packageLockingMode).ifPresent(diagnosticList::add);
                    if (packageLockingMode == null) {
                        packageLockingMode = PackageLockingMode.SOFT;
                    }
                }
            } else if (packageLockingMode == null) {
                packageLockingMode = PackageLockingMode.MEDIUM;
            }
        } else {
            // new package
            if (packageLockingMode == null) {
                if (project.kind() == ProjectKind.BALA_PROJECT) {
                    packageLockingMode = PackageLockingMode.MEDIUM;
                    this.resolutionOptions = ResolutionOptions.builder()
                            .setOffline(resolutionOptions.offline())
                            .setDumpGraph(resolutionOptions.dumpGraph())
                            .setDumpRawGraphs(resolutionOptions.dumpRawGraphs())
                            .setPackageLockingMode(packageLockingMode)
                            .build();
                    return;
                }
                packageLockingMode = PackageLockingMode.SOFT;
            } else if (PackageLockingMode.LOCKED.equals(packageLockingMode) || compilationOptions.sticky()) {
                packageLockingMode = PackageLockingMode.HARD;
            }
        }

        // set to hard if the build is within 24 hours
        if (packageLockingMode == PackageLockingMode.SOFT || packageLockingMode == PackageLockingMode.MEDIUM) {
            packageLockingMode = ProjectUtils.getPackageLockingMode(project.targetDir(), project, packageLockingMode);
        }

        this.resolutionOptions = ResolutionOptions.builder()
                .setOffline(resolutionOptions.offline())
                .setDumpGraph(resolutionOptions.dumpGraph())
                .setDumpRawGraphs(resolutionOptions.dumpRawGraphs())
                .setPackageLockingMode(packageLockingMode)
                .build();
    }

    private DependencyGraph<ResolvedPackageDependency> resolveSourceDependencies() {
        // 1) Get PackageLoadRequests for all the direct dependencies of this package
        LinkedHashSet<ModuleLoadRequest> moduleLoadRequests = getModuleLoadRequestsOfDirectDependencies();

        if (PackageLockingMode.LOCKED.equals(this.resolutionOptions.packageLockingMode())) {
            if (this.rootPackageContext.dependenciesTomlContext().isEmpty()) {
                reportDiagnostic("'LOCKED' locking mode cannot be used without the " + DEPENDENCIES_TOML + " file",
                        ProjectDiagnosticErrorCode.MISSING_DEPENDENCIES_TOML_WITH_LOCKED_MODE.diagnosticId(),
                        DiagnosticSeverity.ERROR, null, rootPackageContext.descriptor());

                return DependencyGraph.emptyGraph();
            } else {

                List<String> previousImports = new ArrayList<>();
                try {
                    BuildJson buildJson = ProjectUtils.readBuildJson(
                            this.rootPackageContext.project().targetDir().resolve(BUILD_FILE));
                    List<String> imports = buildJson.imports();
                    if (imports != null) {
                        previousImports.addAll(imports);
                    }
                } catch (IOException | JsonSyntaxException e) {
                    // ignore
                }
                List<String> currentImports = new ArrayList<>(moduleLoadRequests.stream().filter(
                        moduleLoadRequest -> moduleLoadRequest.orgName().isPresent()
                                && !moduleLoadRequest.orgName().get().equals(PackageOrg.BALLERINA_I_ORG)).map(
                        moduleLoadRequest -> moduleLoadRequest.orgName()
                                .orElse(this.rootPackageContext.packageOrg())
                                + "/" + moduleLoadRequest.moduleName()).toList());
                currentImports.removeAll(previousImports);
                if (!currentImports.isEmpty()) {
                    reportDiagnostic("cannot add new imports with --locking-mode=locked. " +
                                    "Use one of [soft, medium, hard] to build with new imports",
                            ProjectDiagnosticErrorCode.NEW_IMPORTS_WITH_LOCKED_MODE.diagnosticId(),
                            DiagnosticSeverity.ERROR, null, rootPackageContext.descriptor());

                    return DependencyGraph.emptyGraph();
                }
            }
        }

        // 2 Update the package locking mode if necessary
        updateResolutionOptions();

        // 3) Resolve imports to packages and create the complete dependency graph with package metadata
        ResolutionEngine resolutionEngine = new ResolutionEngine(rootPackageContext.descriptor(),
                blendedManifest, packageResolver, moduleResolver, resolutionOptions);
        DependencyGraph<DependencyNode> dependencyNodeGraph =
                resolutionEngine.resolveDependencies(moduleLoadRequests);
        this.dependencyGraphDump = resolutionEngine.dumpGraphs();

        diagnosticList.addAll(resolutionEngine.diagnosticResult().allDiagnostics);

        //3 ) Create the package dependency graph by downloading packages if necessary.
        return buildPackageGraph(dependencyNodeGraph, rootPackageContext.project().currentPackage(),
                packageResolver);
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
                                                                         PackageResolver packageResolver) {
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
                .toList();

        Collection<ResolutionResponse> resolutionResponses =
                packageResolver.resolvePackages(resolutionRequests, resolutionOptions);
        List<PackageDescriptor> oldDependencies = new ArrayList<>();
        List<PackageDescriptor> directDependencies = depGraph.getDirectDependencies(rootNode)
                .stream()
                .map(DependencyNode::pkgDesc)
                .toList();

        // Add resolved packages to the container
        for (ResolutionResponse resolutionResp : resolutionResponses) {
            if (resolutionResp.resolutionStatus().equals(ResolutionResponse.ResolutionStatus.RESOLVED)) {
                PackageDescriptor pkgDesc = resolutionResp.resolvedPackage().packageContext().
                        packageManifest().descriptor();
                if (Optional.ofNullable(pkgDesc.getDeprecated()).orElse(false)) {
                    addDeprecationDiagnostic(pkgDesc);
                }
                try {
                    String ballerinaVersion = resolutionResp.resolvedPackage().manifest().ballerinaVersion();
                    SemanticVersion distVersion = SemanticVersion.from(ballerinaVersion);
                    if (distVersion.lessThan(SemanticVersion.from("2201.12.0"))) {
                        if (!directDependencies.contains(pkgDesc)) {
                            oldDependencies.add(pkgDesc);
                        }
                    }
                } catch (ProjectException e) {
                    // ignore version exception
                }

                ResolutionRequest resolutionReq = resolutionResp.resolutionRequest();
                ResolvedPackageDependency resolvedPkg = new ResolvedPackageDependency(
                        resolutionResp.resolvedPackage(),
                        resolutionReq.scope(),
                        resolutionReq.resolutionType());
                resolvedPkgContainer.add(pkgDesc.org(), pkgDesc.name(), resolvedPkg);
            }
        }
        if (!oldDependencies.isEmpty()) {
            StringBuilder warning = new StringBuilder("The following transitive dependencies were published with a " +
                    "distribution older than Swan Lake Update 12. It is recommended to execute " +
                    "'bal build --locking-mode=soft' to ensure compatibility:");
            // Append the list of old dependencies to the warning message
            for (PackageDescriptor pkgDesc : oldDependencies) {
                warning.append("\n\t- ").append(pkgDesc);
            }
            reportDiagnostic(warning.toString(), ProjectDiagnosticErrorCode.OLD_IMPORTS.diagnosticId(),
                    DiagnosticSeverity.WARNING, null, rootPackageContext.descriptor());
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
                                .toList();
                depGraphBuilder.addDependencies(resolvedPkg, directPkgDependencies);
            }
        }
        return depGraphBuilder.build();
    }

    private void addDeprecationDiagnostic(PackageDescriptor pkgDesc) {
        String deprecationMsg = Optional.ofNullable(pkgDesc.getDeprecationMsg()).orElse("");
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                ProjectDiagnosticErrorCode.DEPRECATED_PACKAGE.diagnosticId(), pkgDesc.toString() +
                " is deprecated: " + deprecationMsg, DiagnosticSeverity.WARNING);
        PackageDiagnostic diagnostic = new PackageDiagnostic(
                diagnosticInfo, this.rootPackageContext.descriptor().name().toString());
        this.diagnosticList.add(diagnostic);
    }

    private ResolutionRequest createFromDepNode(DependencyNode depNode) {
        return ResolutionRequest.from(depNode.pkgDesc(), depNode.scope(), depNode.resolutionType(),
                resolutionOptions.packageLockingMode(), depNode.skipWorkspace());
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
        if (!dependencyGraph.findCycles().isEmpty()) {
            for (List<ResolvedPackageDependency> cycle : dependencyGraph.findCycles()) {
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                        DiagnosticErrorCode.CYCLIC_MODULE_IMPORTS_DETECTED.diagnosticId(),
                        "cyclic module imports detected ''"
                                + cycle.stream()
                                .map(dependency -> dependency.packageInstance().descriptor().toString())
                                .collect(Collectors.joining(" -> ")) + "''",
                        DiagnosticErrorCode.CYCLIC_MODULE_IMPORTS_DETECTED.severity());
                PackageDiagnostic diagnostic = new PackageDiagnostic(diagnosticInfo,
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
            if (!moduleDependencyGraph.findCycles().isEmpty()) {
                for (List<ModuleDescriptor> cycle : moduleDependencyGraph.findCycles()) {
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                            DiagnosticErrorCode.CYCLIC_MODULE_IMPORTS_DETECTED.diagnosticId(),
                            "cyclic module imports detected ''"
                                    + cycle.stream()
                                    .map(desc -> desc.org().toString() + "/" + desc.name().toString() + ":"
                                            + desc.version().toString())
                                    .collect(Collectors.joining(" -> ")) + "''",
                            DiagnosticErrorCode.CYCLIC_MODULE_IMPORTS_DETECTED.severity());
                    PackageDiagnostic diagnostic = new PackageDiagnostic(diagnosticInfo,
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
                                                ProjectEnvironment projectEnvContext) {
        List<ModuleName> moduleNames = rootPackageContext.moduleIds().stream()
                .map(rootPackageContext::moduleContext)
                .map(ModuleContext::moduleName)
                .toList();
        return new ModuleResolver(rootPackageContext.descriptor(), moduleNames, blendedManifest,
                projectEnvContext.getService(PackageResolver.class), resolutionOptions);
    }

    private BlendedManifest createBlendedManifest(PackageContext rootPackageContext,
                                                  ProjectEnvironment projectEnvContext, boolean offline) {
        Map<String, MavenPackageRepository> customPackageRepositoryMap =
                projectEnvContext.getService(CustomPkgRepositoryContainer.class).getCustomPackageRepositories();
        return BlendedManifest.from(rootPackageContext.dependencyManifest(),
                rootPackageContext.packageManifest(),
                projectEnvContext.getService(LocalPackageRepository.class), customPackageRepositoryMap, offline);
    }

    private ResolutionOptions getResolutionOptions(CompilationOptions compilationOptions) {
        PackageLockingMode packageLockingMode;
        if (compilationOptions.sticky()) {
            packageLockingMode = PackageLockingMode.HARD;
        } else {
            packageLockingMode = compilationOptions.lockingMode();
        }
        return ResolutionOptions.builder()
                .setOffline(compilationOptions.offlineBuild())
                .setDumpGraph(compilationOptions.dumpGraph())
                .setDumpRawGraphs(compilationOptions.dumpRawGraphs())
                .setPackageLockingMode(packageLockingMode)
                .build();
    }

    public ResolutionOptions resolutionOptions() {
        return resolutionOptions;
    }

    public List<String> imports() {
        if (imports == null) {
            imports = new ArrayList<>();
            imports = getModuleLoadRequestsOfDirectDependencies().stream()
                    .filter(moduleLoadRequest -> moduleLoadRequest.orgName().isPresent()
                            && !moduleLoadRequest.orgName().get().equals(PackageOrg.BALLERINA_I_ORG))
                    .map(moduleLoadRequest -> moduleLoadRequest.orgName()
                            .orElse(this.rootPackageContext.packageOrg())
                            + "/" + moduleLoadRequest.moduleName())
                    .toList();
        }
        return imports;
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
