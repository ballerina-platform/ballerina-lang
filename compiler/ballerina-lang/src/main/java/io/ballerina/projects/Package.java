package io.ballerina.projects;

import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.projects.internal.DependencyManifestBuilder;
import io.ballerina.projects.internal.ManifestBuilder;
import io.ballerina.projects.internal.model.CompilerPluginDescriptor;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@code Package} represents a Ballerina Package.
 *
 * @since 2.0.0
 */
public class Package {
    private final Project project;
    private final PackageContext packageContext;
    private final Map<ModuleId, Module> moduleMap;
    private final Function<ModuleId, Module> populateModuleFunc;
    // Following are not final since they will be lazy loaded
    private Optional<PackageMd> packageMd = Optional.empty();
    private Optional<PackageReadmeMd> readmeMd = Optional.empty();
    private Optional<BallerinaToml> ballerinaToml = Optional.empty();
    private Optional<DependenciesToml> dependenciesToml = Optional.empty();
    private Optional<CloudToml> cloudToml = Optional.empty();
    private Optional<CompilerPluginToml> compilerPluginToml = Optional.empty();
    private Optional<BalToolToml> balToolToml = Optional.empty();
    private final Map<DocumentId, Resource> resources;
    private final Map<DocumentId, Resource> testResources;
    private final Function<DocumentId, Resource> populateResourceFunc;

    private Package(PackageContext packageContext, Project project) {
        this.packageContext = packageContext;
        this.project = project;
        this.moduleMap = new ConcurrentHashMap<>();
        this.populateModuleFunc = moduleId -> Module.from(
                this.packageContext.moduleContext(moduleId), this);
        this.resources = new ConcurrentHashMap<>();
        this.testResources = new ConcurrentHashMap<>();
        this.populateResourceFunc = documentId -> new Resource(
                this.packageContext.resourceContext(documentId), this);
    }

    static Package from(Project project, PackageConfig packageConfig, CompilationOptions compilationOptions) {
        // TODO create package context here by giving the package config
        // do the same for modules and documents
        // il. package context creates modules contexts and modules context create document contexts

        // contexts need to hold onto the configs. Should we decouple config from tree information as follows.
        // package config has the tree information like modules.
        PackageContext packageContext = PackageContext.from(project, packageConfig, compilationOptions);
        return new Package(packageContext, project);
    }

    PackageContext packageContext() {
        return this.packageContext;
    }

    public Project project() {
        return this.project;
    }

    public PackageId packageId() {
        return this.packageContext.packageId();
    }

    public PackageName packageName() {
        return packageContext.packageName();
    }

    public PackageOrg packageOrg() {
        return packageContext.packageOrg();
    }

    public PackageVersion packageVersion() {
        return packageContext.packageVersion();
    }

    public PackageDescriptor descriptor() {
        return packageContext.descriptor();
    }

    public Optional<CompilerPluginDescriptor> compilerPluginDescriptor() {
        return packageContext.compilerPluginDescriptor();
    }

    public PackageManifest manifest() {
        return packageContext.packageManifest();
    }

    public DependencyManifest dependencyManifest() {
        return packageContext.dependencyManifest();
    }

    public Collection<ModuleId> moduleIds() {
        return this.packageContext.moduleIds();
    }

    public Iterable<Module> modules() {
        List<Module> moduleList = new ArrayList<>();
        for (ModuleId moduleId : this.packageContext.moduleIds()) {
            moduleList.add(module(moduleId));
        }
        return new ModuleIterable(moduleList);
    }

    public Module module(ModuleId moduleId) {
        // TODO Should we throw an error if the moduleId is not present
        return this.moduleMap.computeIfAbsent(moduleId, this.populateModuleFunc);
    }

    public Module module(ModuleName moduleName) {
        for (Module module : this.moduleMap.values()) {
            if (module.moduleName().equals(moduleName)) {
                return module;
            }
        }

        ModuleContext moduleContext = this.packageContext.moduleContext(moduleName);
        if (moduleContext != null) {
            return module(moduleContext.moduleId());
        }

        return null;
    }

    public boolean containsModule(ModuleId moduleId) {
        return this.moduleMap.containsKey(moduleId);
    }

    public Module getDefaultModule() {
        return module(this.packageContext.defaultModuleContext().moduleId());
    }

    public PackageCompilation getCompilation() {
        return this.packageContext.getPackageCompilation();
    }

    PackageCompilation getCompilation(CompilationOptions compilationOptions) {
        return this.packageContext.getPackageCompilation(compilationOptions);
    }

    public PackageResolution getResolution() {
        return this.packageContext.getResolution();
    }

    public BuildToolResolution getBuildToolResolution() {
        return this.packageContext.getBuildToolResolution();
    }

    public PackageResolution getResolution(CompilationOptions compilationOptions) {
        return this.packageContext.getResolution(compilationOptions);
    }

    public DependencyGraph<ModuleDescriptor> moduleDependencyGraph() {
        // Each Package should know the packages that it depends on and packages that depends on it
        // Each Module should know the modules that it depends on and modules that depends on it
        return this.packageContext.moduleDependencyGraph();
    }

    public Collection<PackageDependency> packageDependencies() {
        return packageContext.packageDependencies();
    }

    public CompilationOptions compilationOptions() {
        return packageContext.compilationOptions();
    }

    public Optional<BallerinaToml> ballerinaToml() {
        if (this.ballerinaToml.isEmpty()) {
            this.ballerinaToml = this.packageContext.ballerinaTomlContext().map(c ->
                    BallerinaToml.from(c, this)
            );
        }
        return this.ballerinaToml;
    }

    public Optional<DependenciesToml> dependenciesToml() {
        if (this.dependenciesToml.isEmpty()) {
            this.dependenciesToml = this.packageContext.dependenciesTomlContext().map(c ->
                    DependenciesToml.from(c, this)
            );
        }
        return this.dependenciesToml;
    }

    public Optional<CloudToml> cloudToml() {
        if (this.cloudToml.isEmpty()) {
            this.cloudToml = this.packageContext.cloudTomlContext().map(c ->
                    CloudToml.from(c, this));
        }
        return this.cloudToml;
    }

    public Optional<CompilerPluginToml> compilerPluginToml() {
        if (this.compilerPluginToml.isEmpty()) {
            this.compilerPluginToml = this.packageContext.compilerPluginTomlContext()
                    .map(c -> CompilerPluginToml.from(c, this));
        }
        return this.compilerPluginToml;
    }

    public Optional<BalToolToml> balToolToml() {
        if (this.balToolToml.isEmpty()) {
            this.balToolToml = this.packageContext.balToolTomlContext()
                    .map(c -> BalToolToml.from(c, this));
        }
        return this.balToolToml;
    }

    /**
     * Returns the PackageMd document.
     *
     * @return PackageMd
     * @deprecated use {@link #readmeMd()} instead.
     */
    @Deprecated (forRemoval = true, since = "2.11.0")
    public Optional<PackageMd> packageMd() {
        if (this.packageMd.isEmpty()) {
            this.packageMd = this.packageContext.packageMdContext().map(c ->
                    PackageMd.from(c, this)
            );
        }
        return this.packageMd;
    }

    public Optional<PackageReadmeMd> readmeMd() {
        if (this.readmeMd.isEmpty()) {
            this.readmeMd = this.packageContext.readmeMdContext().map(c ->
                    PackageReadmeMd.from(c, this)
            );
        }
        return this.readmeMd;
    }

    public Collection<DocumentId> resourceIds() {
        return this.packageContext.resourceIds();
    }

    public Collection<DocumentId> testResourceIds() {
        return this.packageContext.testResourceIds();
    }

    public Resource resource(DocumentId documentId) {
        // TODO Should we throw an error if the documentId is not present
        if (resourceIds().contains(documentId)) {
            return this.resources.computeIfAbsent(documentId, this.populateResourceFunc);
        } else {
            return this.testResources.computeIfAbsent(documentId, this.populateResourceFunc);
        }
    }


    Package duplicate(Project project) {
        return new Package(packageContext.duplicate(project), project);
    }

    /**
     * Run {@code CodeGenerator} and {@code CodeModifier} tasks in engaged {@code CompilerPlugin}s.
     * <p>
     * Returns a collected diagnostics reported by the code generator and code modifier tasks
     * in form of a {@code DiagnosticResult} instance.
     * <p>
     * Here is a sample usage of this API: <pre>
     *   Project project = BuildProject.load(Path.of(...));
     *   Package currentPackage = project.currentPackage();
     *   DiagnosticResult diagnosticsResult = currentPackage.runCodeGenAndModifyPlugins();
     *
     *   // Compile the package with generated files.
     *   PackageCompilation compilation = packageWithGenFiles.getCompilation();
     *   </pre>
     * <p>
     * This method does not run other tasks such as {@code CodeAnalyzer}s in engaged compiler plugins.
     *
     * @return a {@code DiagnosticResult} instance
     */
    public DiagnosticResult runCodeGenAndModifyPlugins() {
        Package pkg = this;
        PackageCompilation cachedCompilation = this.packageContext.cachedCompilation();
        if (cachedCompilation != null) {
            // Check whether there are engaged code modifiers, if not return
            CompilerPluginManager compilerPluginManager = cachedCompilation.compilerPluginManager();
            if (compilerPluginManager.engagedCodeGeneratorCount() == 0
                    && compilerPluginManager.engagedCodeModifierCount() == 0) {
                return new DefaultDiagnosticResult(Collections.emptyList());
            }
        }

        // There are engaged compiler plugins or there is no cached compilation. We have to compile anyway
        CompilationOptions compOptions = CompilationOptions.builder()
                .withCodeGenerators(true)
                .withCodeModifiers(true)
                .build();
        CompilerPluginManager compilerPluginManager = this.getCompilation(compOptions).compilerPluginManager();
        List<Diagnostic> diagnostics = new ArrayList<>();
        if (compilerPluginManager.engagedCodeGeneratorCount() > 0) {
            CodeGeneratorManager codeGeneratorManager = compilerPluginManager.getCodeGeneratorManager();
            CodeGeneratorResult codeGeneratorResult = codeGeneratorManager.runCodeGenerators(pkg);
            diagnostics.addAll(codeGeneratorResult.reportedDiagnostics().allDiagnostics);
            if (codeGeneratorResult.updatedPackage().isPresent()) {
                pkg = codeGeneratorResult.updatedPackage().get();
            }
        }

        if (compilerPluginManager.engagedCodeModifierCount() > 0) {
            compilerPluginManager = pkg.getCompilation(compOptions).compilerPluginManager();
            CodeModifierManager codeModifierManager = compilerPluginManager.getCodeModifierManager();
            CodeModifierResult codeModifierResult = codeModifierManager.runCodeModifiers(pkg);
            diagnostics.addAll(codeModifierResult.reportedDiagnostics().allDiagnostics);
        }
        return new DefaultDiagnosticResult(diagnostics);
    }

    /**
     * Run {@code CodeGenerator} tasks in engaged {@code CompilerPlugin}s.
     * <p>
     * Returns a new package instances with generated files and a collected diagnostics
     * reported by the code generator tasks in form of a {@code CodeGeneratorResult} instance.
     * <p>
     * Here is a sample usage of this API: <pre>
     *   Project project = BuildProject.load(Path.of(...));
     *   Package currentPackage = project.currentPackage();
     *   Package packageWithGenFiles = currentPackage.runCodeGeneratorPlugins();
     *
     *   // Compile the package with generated files.
     *   PackageCompilation compilation = packageWithGenFiles.getCompilation();
     *   </pre>
     * <p>
     * This method does not run other tasks such as {@code CodeAnalyzer}s in engaged compiler plugins.
     *
     * @return a {@code CodeGeneratorResult} instance
     */
    public CodeGeneratorResult runCodeGeneratorPlugins() {
        PackageCompilation cachedCompilation = this.packageContext.cachedCompilation();
        if (cachedCompilation != null) {
            // Check whether there are engaged code generators, if not return
            CompilerPluginManager compilerPluginManager = cachedCompilation.compilerPluginManager();
            if (compilerPluginManager.engagedCodeGeneratorCount() == 0) {
                return new CodeGeneratorResult(null, Collections.emptyList());
            }
        }

        // There are engaged code generators or there is no cached compilation. We have to compile anyway
        CompilationOptions compOptions = CompilationOptions.builder().withCodeGenerators(true).build();
        // TODO We can avoid this compilation. Move CompilerPluginManagers out of the PackageCompilation
        // TODO How about PackageResolution
        CompilerPluginManager compilerPluginManager = this.getCompilation(compOptions).compilerPluginManager();
        if (compilerPluginManager.engagedCodeGeneratorCount() == 0) {
            return new CodeGeneratorResult(null, Collections.emptyList());
        }

        CodeGeneratorManager codeGeneratorManager = compilerPluginManager.getCodeGeneratorManager();
        return codeGeneratorManager.runCodeGenerators(this);
    }

    /**
     * Run {@code CodeModifier} tasks in engaged {@code CompilerPlugin}s.
     * <p>
     * Returns a new package instances with modified files and a collected diagnostics
     * reported by the code modifier tasks in form of a {@code CodeModifierResult} instance.
     * <p>
     * Here is a sample usage of this API: <pre>
     *   Project project = BuildProject.load(Path.of(...));
     *   Package currentPackage = project.currentPackage();
     *   Package packageWithGenFiles = currentPackage.runCodeModifierPlugins();
     *
     *   // Compile the package with generated files.
     *   PackageCompilation compilation = packageWithGenFiles.getCompilation();
     *   </pre>
     * <p>
     * This method does not run other tasks such as {@code CodeAnalyzer}s in engaged compiler plugins.
     *
     * @return a {@code CodeModifierResult} instance
     */
    public CodeModifierResult runCodeModifierPlugins() {
        PackageCompilation cachedCompilation = this.packageContext.cachedCompilation();
        if (cachedCompilation != null) {
            // Check whether there are engaged code modifiers, if not return
            CompilerPluginManager compilerPluginManager = cachedCompilation.compilerPluginManager();
            if (compilerPluginManager.engagedCodeModifierCount() == 0) {
                return new CodeModifierResult(null, Collections.emptyList());
            }
        }

        // There are engaged code modifiers or there is no cached compilation. We have to compile anyway
        CompilationOptions compOptions = CompilationOptions.builder().withCodeModifiers(true).build();
        // TODO We can avoid this compilation. Move CompilerPluginManagers out of the PackageCompilation
        // TODO How about PackageResolution
        CompilerPluginManager compilerPluginManager = this.getCompilation(compOptions).compilerPluginManager();
        if (compilerPluginManager.engagedCodeModifierCount() == 0) {
            return new CodeModifierResult(null, Collections.emptyList());
        }

        CodeModifierManager codeModifierManager = compilerPluginManager.getCodeModifierManager();
        return codeModifierManager.runCodeModifiers(this);
    }

    /**
     * Returns an instance of the Package.Modifier.
     *
     * @return module modifier
     */
    public Modifier modify() {
        return new Modifier(this);
    }

    public PackageResolution getResolution(ResolutionOptions resolutionOptions) {
        boolean offline = resolutionOptions.offline();
        boolean sticky = resolutionOptions.sticky();
        CompilationOptions newCompOptions = CompilationOptions.builder().setOffline(offline).setSticky(sticky).build();
        newCompOptions = newCompOptions.acceptTheirs(project.currentPackage().compilationOptions());
        return this.packageContext.getResolution(newCompOptions, true);
    }

    private static class ModuleIterable implements Iterable<Module> {

        private final Collection<Module> moduleList;

        public ModuleIterable(Collection<Module> moduleList) {
            this.moduleList = moduleList;
        }

        @Override
        public Iterator<Module> iterator() {
            return this.moduleList.iterator();
        }

        @Override
        public Spliterator<Module> spliterator() {
            return this.moduleList.spliterator();
        }
    }

    /**
     * Inner class that handles package modifications.
     */
    public static class Modifier {
        private final PackageId packageId;
        private PackageManifest packageManifest;
        private DependencyManifest dependencyManifest;
        private final Map<ModuleId, ModuleContext> moduleContextMap;
        private final Project project;
        private final DependencyGraph<ResolvedPackageDependency> dependencyGraph;
        private final CompilationOptions compilationOptions;
        private TomlDocumentContext ballerinaTomlContext;
        private TomlDocumentContext dependenciesTomlContext;
        private TomlDocumentContext cloudTomlContext;
        private TomlDocumentContext compilerPluginTomlContext;
        private TomlDocumentContext balToolTomlContext;
        private MdDocumentContext readmeMdContext;
        private final Map<DocumentId, ResourceContext> resourceContextMap;
        private final Map<DocumentId, ResourceContext> testResourceContextMap;

        public Modifier(Package oldPackage) {
            this.packageId = oldPackage.packageId();
            this.packageManifest = oldPackage.manifest();
            this.dependencyManifest = oldPackage.dependencyManifest();
            this.moduleContextMap = copyModules(oldPackage);
            this.project = oldPackage.project;
            this.dependencyGraph = oldPackage.getResolution().dependencyGraph();
            this.compilationOptions = oldPackage.compilationOptions();
            this.ballerinaTomlContext = oldPackage.packageContext.ballerinaTomlContext().orElse(null);
            this.dependenciesTomlContext = oldPackage.packageContext.dependenciesTomlContext().orElse(null);
            this.cloudTomlContext = oldPackage.packageContext.cloudTomlContext().orElse(null);
            this.compilerPluginTomlContext = oldPackage.packageContext.compilerPluginTomlContext().orElse(null);
            this.balToolTomlContext = oldPackage.packageContext.balToolTomlContext().orElse(null);
            this.readmeMdContext = oldPackage.packageContext.readmeMdContext().orElse(null);
            this.resourceContextMap = copyResources(oldPackage, oldPackage.packageContext.resourceIds());
            this.testResourceContextMap = copyResources(oldPackage, oldPackage.packageContext.testResourceIds());
        }

        Modifier updateModules(Set<ModuleContext> newModuleContexts) {
            for (ModuleContext newModuleContext : newModuleContexts) {
                this.moduleContextMap.put(newModuleContext.moduleId(), newModuleContext);
            }
            return this;
        }

        /**
         * Adds a new module in a new package that is copied from the existing.
         *
         * @param moduleConfig configuration of the module to add
         * @return Package.Modifier which contains the updated package
         */
        public Modifier addModule(ModuleConfig moduleConfig) {
            ModuleContext newModuleContext = ModuleContext.from(this.project, moduleConfig, false);
            this.moduleContextMap.put(newModuleContext.moduleId(), newModuleContext);
            return this;
        }

        /**
         * Adds a Dependencies toml.
         *
         * @param documentConfig configuration of the toml document
         * @return Package.Modifier which contains the updated package
         */
        public Modifier addDependenciesToml(DocumentConfig documentConfig) {
            this.dependenciesTomlContext = TomlDocumentContext.from(documentConfig);
            updateDependencyManifest();
            return this;
        }


        /**
         * Remove Dependencies toml.
         *
         * @return Package.Modifier which contains the updated package
         */
        public Modifier removeDependenciesToml() {
            this.dependenciesTomlContext = null;
            return this;
        }

        /**
         * Adds a Cloud toml.
         *
         * @param documentConfig configuration of the toml document
         * @return Package.Modifier which contains the updated package
         */
        public Modifier addCloudToml(DocumentConfig documentConfig) {
            this.cloudTomlContext = TomlDocumentContext.from(documentConfig);
            updatePackageManifest();
            return this;
        }

        /**
         * Remove Cloud toml.
         *
         * @return Package.Modifier which contains the updated package
         */
        public Modifier removeCloudToml() {
            this.cloudTomlContext = null;
            return this;
        }

        /**
         * Adds a Compiler plugin toml.
         *
         * @param documentConfig configuration of the toml document
         * @return Package.Modifier which contains the updated package
         */
        public Modifier addCompilerPluginToml(DocumentConfig documentConfig) {
            this.compilerPluginTomlContext = TomlDocumentContext.from(documentConfig);
            updatePackageManifest();
            return this;
        }

        /**
         * Adds a Bal tool toml.
         *
         * @param documentConfig configuration of the toml document
         * @return Package.Modifier which contains the updated package
         */
        public Modifier addBalToolToml(DocumentConfig documentConfig) {
            this.balToolTomlContext = TomlDocumentContext.from(documentConfig);
            updatePackageManifest();
            return this;
        }

        /**
         * Remove Compiler plugin toml.
         *
         * @return Package.Modifier which contains the updated package
         */
        public Modifier removeCompilerPluginToml() {
            this.compilerPluginTomlContext = null;
            return this;
        }

        /**
         * Remove Bal tool toml.
         *
         * @return Package.Modifier which contains the updated package
         */
        public Modifier removeBalToolToml() {
            this.balToolTomlContext = null;
            return this;
        }

        /**
         * Adds a package md.
         *
         * @param documentConfig configuration of the toml document
         * @return Package.Modifier which contains the updated package
         */
        public Modifier addPackageMd(DocumentConfig documentConfig) {
            this.readmeMdContext = MdDocumentContext.from(documentConfig);
            return this;
        }

        /**
         * Remove package md.
         *
         * @return Package.Modifier which contains the updated package
         */
        public Modifier removePackageMd() {
            this.readmeMdContext = null;
            return this;
        }


        Modifier updateBallerinaToml(BallerinaToml ballerinaToml) {
            this.ballerinaTomlContext = ballerinaToml.ballerinaTomlContext();
            updatePackageManifest();
            updateModules();
            return this;
        }

        Modifier updateDependenciesToml(DependenciesToml dependenciesToml) {
            this.dependenciesTomlContext = dependenciesToml.dependenciesTomlContext();
            updateDependencyManifest();
            updateModules();
            return this;
        }

        Modifier updateCloudToml(CloudToml cloudToml) {
            this.cloudTomlContext = cloudToml.cloudTomlContext();
            return this;
        }

        Modifier updateCompilerPluginToml(CompilerPluginToml compilerPluginToml) {
            this.compilerPluginTomlContext = compilerPluginToml.compilerPluginTomlContext();
            return this;
        }

        Modifier updateBalToolToml(BalToolToml balToolToml) {
            this.balToolTomlContext = balToolToml.balToolTomlContext();
            return this;
        }

        Modifier updatePackageMd(MdDocumentContext packageMd) {
            this.readmeMdContext = packageMd;
            return this;
        }

        /**
         * Returns the updated package created by a module add/remove/update operation.
         *
         * @return updated package
         */
        public Package apply() {
            return createNewPackage();
        }

        private Map<ModuleId, ModuleContext> copyModules(Package oldPackage) {
            Map<ModuleId, ModuleContext> moduleContextMap = new HashMap<>();
            for (ModuleId moduleId : oldPackage.packageContext.moduleIds()) {
                moduleContextMap.put(moduleId, oldPackage.packageContext.moduleContext(moduleId));
            }
            return moduleContextMap;
        }

        private Package createNewPackage() {
            Package oldPackage = this.project.currentPackage();
            PackageResolution oldResolution = oldPackage.getResolution();
            PackageContext newPackageContext = new PackageContext(this.project, this.packageId, this.packageManifest,
                    this.dependencyManifest, this.ballerinaTomlContext, this.dependenciesTomlContext,
                    this.cloudTomlContext, this.compilerPluginTomlContext, this.balToolTomlContext,
                    this.readmeMdContext, this.compilationOptions, this.moduleContextMap,
                    DependencyGraph.emptyGraph(), this.resourceContextMap,
                    this.testResourceContextMap);
            this.project.setCurrentPackage(new Package(newPackageContext, this.project));
            if (isOldDependencyGraphValid(oldPackage, this.project.currentPackage())) {
                this.project.currentPackage().packageContext().getResolution(oldResolution);
            } else {
                this.project.compilerPluginContexts().clear();
            }
            CompilationOptions offlineCompOptions = CompilationOptions.builder().setOffline(true).build();
            offlineCompOptions = offlineCompOptions.acceptTheirs(project.currentPackage().compilationOptions());
            DependencyGraph<ResolvedPackageDependency> newDepGraph = this.project.currentPackage().packageContext()
                    .getResolution(offlineCompOptions, true).dependencyGraph();
            cleanPackageCache(this.dependencyGraph, newDepGraph);
            if (this.project.kind() == ProjectKind.BUILD_PROJECT && this.project.workspaceProject().isPresent()) {
                Collection<BuildProject> wpDependents = this.project.workspaceProject().get().getResolution()
                        .dependencyGraph().getDirectDependents((BuildProject) this.project);
                for (BuildProject dependent : wpDependents) {
                    dependent.resetPackage(dependent);
                }
            }

            return this.project.currentPackage();
        }

        private static boolean isOldDependencyGraphValid(Package oldPackage, Package currentPackage) {
            Set<String> oldPackageImports = ProjectUtils.getPackageImports(oldPackage);
            Set<String> currentPackageImports = ProjectUtils.getPackageImports(currentPackage);
            String oldDependencyTomlContent = oldPackage.packageContext.dependenciesTomlContext()
                    .map(d -> d.tomlDocument().textDocument().toString()).orElse("");
            String currentDependencyTomlContent = currentPackage.packageContext.dependenciesTomlContext()
                    .map(d -> d.tomlDocument().textDocument().toString()).orElse("");
            String oldBallerinaTomlContent = oldPackage.packageContext.ballerinaTomlContext()
                    .map(d -> d.tomlDocument().textDocument().toString()).orElse("");
            String currentBallerinaTomlContent = currentPackage.packageContext.ballerinaTomlContext()
                    .map(d -> d.tomlDocument().textDocument().toString()).orElse("");
            return oldPackageImports.equals(currentPackageImports) &&
                    oldDependencyTomlContent.equals(currentDependencyTomlContent) &&
                    oldBallerinaTomlContent.equals(currentBallerinaTomlContent);
        }

        private void cleanPackageCache(DependencyGraph<ResolvedPackageDependency> oldGraph,
                                       DependencyGraph<ResolvedPackageDependency> newGraph) {
            io.ballerina.projects.environment.PackageCache environmentPackageCache =
                    this.project.projectEnvironmentContext().environment().getService(
                            io.ballerina.projects.environment.PackageCache.class);
            CompilerContext compilerContext = project.projectEnvironmentContext()
                    .getService(CompilerContext.class);

            Set<ResolvedPackageDependency> diff = oldGraph.difference(newGraph);
            if (!diff.isEmpty()) {
                // A non-empty diff means deletion of nodes from the old graph is required
                // to get the new graph, hence we remove these modules and its dependants from the package cache.
                for (ResolvedPackageDependency dependency : diff) {
                    environmentPackageCache.removePackage(dependency.packageInstance().packageId());
                    deleteCaches(dependency, oldGraph, compilerContext);
                }
            }
            diff = newGraph.difference(oldGraph);
            if (!diff.isEmpty()) {
                // A non-empty diff means there can be dependant nodes in the old graph that
                // need to be recompiled. Hence we remove the dependant modules from the package cache.
                for (ResolvedPackageDependency dependency : diff) {
                    for (ResolvedPackageDependency directDependent : newGraph.getDirectDependents(dependency)) {
                        deleteCaches(directDependent, newGraph, compilerContext);
                    }
                }
            }
        }

        private void deleteCaches(ResolvedPackageDependency dependency,
                                  DependencyGraph<ResolvedPackageDependency> depGraph,
                                  CompilerContext compilerContext) {
            if (dependency.equals(depGraph.getRoot())) {
                return;
            }
            PackageCache packageCache = PackageCache.getInstance(compilerContext);
            for (ModuleId moduleId : dependency.packageInstance().moduleIds()) {
                if (!dependency.packageInstance().descriptor().isLangLibPackage()) {
                    Module module = dependency.packageInstance().module(moduleId);
                    PackageID packageID = module.descriptor().moduleCompilationId();
                    // remove the module from the compiler packageCache
                    packageCache.remove(packageID);
                    // reset the module in the project environment packageCache to make the module recompile
                    // and add symbols
                    module.moduleContext().setCompilationState(null);
                }
            }
            for (ResolvedPackageDependency directDependent : depGraph.getDirectDependents(dependency)) {
                deleteCaches(directDependent, depGraph, compilerContext);
            }
        }

        private void updatePackageManifest() {
            ManifestBuilder manifestBuilder = ManifestBuilder.from(this.ballerinaTomlContext.tomlDocument(),
                    Optional.ofNullable(this.compilerPluginTomlContext)
                            .map(TomlDocumentContext::tomlDocument).orElse(null),
                    Optional.ofNullable(this.balToolTomlContext)
                            .map(TomlDocumentContext::tomlDocument).orElse(null),
                    this.project.sourceRoot());
            this.packageManifest = manifestBuilder.packageManifest();
            BuildOptions newBuildOptions;
            if (manifestBuilder.buildOptions() == null) {
                newBuildOptions = BuildOptions.builder().build();
            } else {
                newBuildOptions = manifestBuilder.buildOptions();
            }
            // The build options passed during project loading takes priority.
            this.project.setBuildOptions(newBuildOptions.acceptTheirs(this.project.buildOptions()));
        }

        private void updateDependencyManifest() {
            DependencyManifestBuilder manifestBuilder = DependencyManifestBuilder.from(
                    Optional.ofNullable(this.dependenciesTomlContext)
                            .map(TomlDocumentContext::tomlDocument).orElse(null),
                    project.currentPackage().descriptor());
            this.dependencyManifest = manifestBuilder.dependencyManifest();
        }

        private void updateModules() {
            Set<ModuleContext> moduleContextSet = new HashSet<>();
            for (Map.Entry<ModuleId, ModuleContext> moduleIdModuleContextEntry : moduleContextMap.entrySet()) {
                ModuleId moduleId = moduleIdModuleContextEntry.getKey();
                ModuleContext oldModuleContext = moduleIdModuleContextEntry.getValue();

                PackageDescriptor packageDescriptor = this.packageManifest.descriptor();
                ModuleName moduleName = ModuleName.from(
                        packageDescriptor.name(), oldModuleContext.moduleName().moduleNamePart());
                ModuleDescriptor moduleDescriptor = ModuleDescriptor.from(moduleName, packageDescriptor);

                Map<DocumentId, DocumentContext> srcDocContextMap = new LinkedHashMap<>();
                for (DocumentId documentId : oldModuleContext.srcDocumentIds()) {
                    srcDocContextMap.put(documentId, oldModuleContext.documentContext(documentId));
                }

                Map<DocumentId, DocumentContext> testDocContextMap = new LinkedHashMap<>();
                for (DocumentId documentId : oldModuleContext.testSrcDocumentIds()) {
                    testDocContextMap.put(documentId, oldModuleContext.documentContext(documentId));
                }

                moduleContextSet.add(new ModuleContext(this.project, moduleId, moduleDescriptor,
                        oldModuleContext.isDefaultModule(), srcDocContextMap, testDocContextMap,
                        oldModuleContext.readmeMdContext().orElse(null),
                        oldModuleContext.moduleDescDependencies()));
                // Remove the module with old PackageID from the compilation cache
                PackageCache.getInstance(project.projectEnvironmentContext().getService(CompilerContext.class)).
                        remove(oldModuleContext.descriptor().moduleCompilationId());
            }
            updateModules(moduleContextSet);
        }

        private Map<DocumentId, ResourceContext> copyResources(Package oldPackage, Collection<DocumentId> documentIds) {
            Map<DocumentId, ResourceContext> resourceContextMap = new HashMap<>();
            for (DocumentId documentId : documentIds) {
                resourceContextMap.put(documentId, oldPackage.packageContext.resourceContext(documentId));
            }
            return resourceContextMap;
        }

        /**
         * Creates a copy of the existing module and adds a new resource to the new module.
         *
         * @param resourceConfig configurations to create the resource
         * @return an instance of the Module.Modifier
         */
        public Package.Modifier addResource(ResourceConfig resourceConfig) {
            ResourceContext newResourceContext = ResourceContext.from(resourceConfig);
            this.resourceContextMap.put(newResourceContext.documentId(), newResourceContext);
            return this;
        }

        /**
         * Creates a copy of the existing module and adds a new test resource to the new module.
         *
         * @param resourceConfig configurations to create the test resource
         * @return an instance of the Module.Modifier
         */
        public Package.Modifier addTestResource(ResourceConfig resourceConfig) {
            ResourceContext newResourceContext = ResourceContext.from(resourceConfig);
            this.testResourceContextMap.put(newResourceContext.documentId(), newResourceContext);
            return this;
        }

        /**
         * Creates a copy of the existing module and removes the specified resource from the new module.
         *
         * @param documentId documentId of the resource to remove
         * @return an instance of the Module.Modifier
         */
        public Package.Modifier removeResource(DocumentId documentId) {

            if (this.resourceContextMap.containsKey(documentId)) {
                this.resourceContextMap.remove(documentId);
            } else {
                this.testResourceContextMap.remove(documentId);
            }
            return this;
        }

    }
}
