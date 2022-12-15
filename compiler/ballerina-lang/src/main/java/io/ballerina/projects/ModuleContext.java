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

import io.ballerina.projects.PackageResolution.DependencyResolution;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ProjectEnvironment;
import io.ballerina.projects.internal.CompilerPhaseRunner;
import io.ballerina.projects.internal.ModuleContextDataHolder;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.BIRPackageSymbolEnter;
import org.wso2.ballerinalang.compiler.bir.writer.BIRBinaryWriter;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;
import org.wso2.ballerinalang.programfile.PackageFileWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.ballerinalang.model.tree.SourceKind.REGULAR_SOURCE;
import static org.ballerinalang.model.tree.SourceKind.TEST_SOURCE;

/**
 * Maintains the internal state of a {@code Module} instance.
 * <p>
 * Works as a module cache.
 *
 * @since 2.0.0
 */
class ModuleContext {
    private final ModuleId moduleId;
    private final ModuleDescriptor moduleDescriptor;
    private final Collection<DocumentId> srcDocIds;
    private final boolean isDefaultModule;
    private final Map<DocumentId, DocumentContext> srcDocContextMap;
    private final Collection<DocumentId> testSrcDocIds;
    private final MdDocumentContext moduleMdContext;
    private final Map<DocumentId, DocumentContext> testDocContextMap;
    private final Collection<DocumentId> resourceIds;
    private final Collection<DocumentId> testResourceIds;
    private final Map<DocumentId, ResourceContext> resourceContextMap;
    private final Map<DocumentId, ResourceContext> testResourceContextMap;
    private final Project project;
    private final CompilationCache compilationCache;
    private final List<ModuleDescriptor> moduleDescDependencies;

    private Set<ModuleDependency> moduleDependencies;
    private BLangPackage bLangPackage;
    private BPackageSymbol bPackageSymbol;
    private byte[] birBytes = new byte[0];
    private final Bootstrap bootstrap;
    private ModuleCompilationState moduleCompState;
    private Set<ModuleLoadRequest> allModuleLoadRequests = null;
    private Set<ModuleLoadRequest> allTestModuleLoadRequests = null;

    ModuleContext(Project project,
                  ModuleId moduleId,
                  ModuleDescriptor moduleDescriptor,
                  boolean isDefaultModule,
                  Map<DocumentId, DocumentContext> srcDocContextMap,
                  Map<DocumentId, DocumentContext> testDocContextMap,
                  MdDocumentContext moduleMd,
                  List<ModuleDescriptor> moduleDescDependencies,
                  Map<DocumentId, ResourceContext> resourceContextMap,
                  Map<DocumentId, ResourceContext> testResourceContextMap) {
        this.project = project;
        this.moduleId = moduleId;
        this.moduleDescriptor = moduleDescriptor;
        this.isDefaultModule = isDefaultModule;
        this.srcDocContextMap = srcDocContextMap;
        this.srcDocIds = Collections.unmodifiableCollection(srcDocContextMap.keySet());
        this.testDocContextMap = testDocContextMap;
        this.testSrcDocIds = Collections.unmodifiableCollection(testDocContextMap.keySet());
        this.moduleMdContext = moduleMd;
        this.moduleDescDependencies = Collections.unmodifiableList(moduleDescDependencies);
        this.resourceContextMap = resourceContextMap;
        this.testResourceContextMap = testResourceContextMap;
        this.resourceIds = Collections.unmodifiableCollection(resourceContextMap.keySet());
        this.testResourceIds = Collections.unmodifiableCollection(testResourceContextMap.keySet());

        ProjectEnvironment projectEnvironment = project.projectEnvironmentContext();
        this.bootstrap = new Bootstrap(projectEnvironment.getService(PackageResolver.class));
        this.compilationCache = projectEnvironment.getService(CompilationCache.class);
    }

    static ModuleContext from(Project project, ModuleConfig moduleConfig) {
        Map<DocumentId, DocumentContext> srcDocContextMap = new LinkedHashMap<>();
        for (DocumentConfig sourceDocConfig : moduleConfig.sourceDocs()) {
            srcDocContextMap.put(sourceDocConfig.documentId(), DocumentContext.from(sourceDocConfig));
        }

        Map<DocumentId, DocumentContext> testDocContextMap = new LinkedHashMap<>();
        for (DocumentConfig testSrcDocConfig : moduleConfig.testSourceDocs()) {
            testDocContextMap.put(testSrcDocConfig.documentId(), DocumentContext.from(testSrcDocConfig));
        }

        Map<DocumentId, ResourceContext> resourceContextMap = new HashMap<>();
        for (ResourceConfig resourceConfig : moduleConfig.resources()) {
            resourceContextMap.put(resourceConfig.documentId(), ResourceContext.from(resourceConfig));
        }

        Map<DocumentId, ResourceContext> testResourceContextMap = new HashMap<>();
        for (ResourceConfig resourceConfig : moduleConfig.testResources()) {
            testResourceContextMap.put(resourceConfig.documentId(), ResourceContext.from(resourceConfig));
        }

        return new ModuleContext(project, moduleConfig.moduleId(), moduleConfig.moduleDescriptor(),
                moduleConfig.isDefaultModule(), srcDocContextMap, testDocContextMap,
                moduleConfig.moduleMd().map(c ->MdDocumentContext.from(c)).orElse(null),
                moduleConfig.dependencies(), resourceContextMap, testResourceContextMap);
    }

    ModuleId moduleId() {
        return this.moduleId;
    }

    ModuleDescriptor descriptor() {
        return moduleDescriptor;
    }

    ModuleName moduleName() {
        return moduleDescriptor.name();
    }

    Collection<DocumentId> srcDocumentIds() {
        return this.srcDocIds;
    }

    Collection<DocumentId> testSrcDocumentIds() {
        return this.testSrcDocIds;
    }

    Collection<DocumentId> resourceIds() {
        return this.resourceIds;
    }

    Collection<DocumentId> testResourceIds() {
        return this.testResourceIds;
    }

    DocumentContext documentContext(DocumentId documentId) {
        if (this.srcDocIds.contains(documentId)) {
            return this.srcDocContextMap.get(documentId);
        } else {
            return this.testDocContextMap.get(documentId);
        }
    }

    ResourceContext resourceContext(DocumentId documentId) {
        if (this.resourceIds.contains(documentId)) {
            return this.resourceContextMap.get(documentId);
        } else {
            return this.testResourceContextMap.get(documentId);
        }
    }

    Project project() {
        return this.project;
    }

    boolean isExported() {
        List<String> exports = this.project.currentPackage().manifest().exportedModules();
        return exports.contains(moduleDescriptor.name().toString());
    }

    boolean isDefaultModule() {
        return this.isDefaultModule;
    }

    Collection<ModuleDependency> dependencies() {
        return moduleDependencies;
    }

    List<ModuleDescriptor> moduleDescDependencies() {
        return moduleDescDependencies;
    }

    Set<ModuleLoadRequest> populateModuleLoadRequests() {
        if (allModuleLoadRequests != null) {
            return allModuleLoadRequests;
        }
        allModuleLoadRequests = new OverwritableLinkedHashSet();
        for (DocumentContext docContext : srcDocContextMap.values()) {
            allModuleLoadRequests.addAll(docContext.moduleLoadRequests(moduleDescriptor,
                    PackageDependencyScope.DEFAULT));
        }

        return allModuleLoadRequests;
    }

    Set<ModuleLoadRequest> populateTestSrcModuleLoadRequests() {
        if (allTestModuleLoadRequests != null) {
            return allTestModuleLoadRequests;
        }
        allTestModuleLoadRequests = new OverwritableLinkedHashSet();
        for (DocumentContext docContext : testDocContextMap.values()) {
            allTestModuleLoadRequests.addAll(
                    docContext.moduleLoadRequests(moduleDescriptor, PackageDependencyScope.TEST_ONLY));
        }

        return allTestModuleLoadRequests;
    }

    BLangPackage bLangPackage() {
        return getBLangPackageOrThrow();
    }

    ModuleCompilationState compilationState() {
        return moduleCompState;
    }

    private BLangPackage getBLangPackageOrThrow() {
        if (bLangPackage == null) {
            throw new IllegalStateException("Compile the module first!");
        }

        return bLangPackage;
    }

    /**
     * Returns the list of compilation diagnostics of this module.
     *
     * @return Returns the list of compilation diagnostics of this module
     */
    List<Diagnostic> diagnostics() {
        // Try to get the diagnostics from the bLangPackage, if the module is already compiled
        if (bLangPackage != null) {
            return bLangPackage.getDiagnostics();
        }

        return Collections.emptyList();
    }

    private void parseTestSources(BLangPackage pkgNode, PackageID pkgId, CompilerContext compilerContext) {
        BLangTestablePackage testablePkg = TreeBuilder.createTestablePackageNode();
        // TODO Not sure why we need to do this. It is there in the current implementation
        testablePkg.packageID = pkgId;
        testablePkg.flagSet.add(Flag.TESTABLE);
        testablePkg.parent = pkgNode;
        // TODO Why we need two different diagnostic positions. This is how it is done in the current compiler.
        //  So I kept this as is for now.
        testablePkg.pos = new BLangDiagnosticLocation(this.moduleName().toString(), 1, 1, 1, 1);
        pkgNode.addTestablePkg(testablePkg);
        for (DocumentContext documentContext : testDocContextMap.values()) {
            testablePkg.addCompilationUnit(documentContext.compilationUnit(compilerContext, pkgId, TEST_SOURCE));
        }
    }

    // TODO temp change
    ModuleCompilationState currentCompilationState() {
        if (moduleCompState != null) {
            return moduleCompState;
        }

        // TODO This logic needs to be updated. We need a proper way to decide on the initial state
        if (compilationCache.getBir(moduleDescriptor.name()).length == 0) {
            moduleCompState = ModuleCompilationState.LOADED_FROM_SOURCES;
        } else if (this.project().kind() == ProjectKind.BUILD_PROJECT
                && !this.project.buildOptions().enableCache()) {
            moduleCompState = ModuleCompilationState.LOADED_FROM_SOURCES;
        } else {
            moduleCompState = ModuleCompilationState.LOADED_FROM_CACHE;
        }
        return moduleCompState;
    }

    void setCompilationState(ModuleCompilationState moduleCompState) {
        this.moduleCompState = moduleCompState;
    }

    void parse() {
        currentCompilationState().parse(this);
    }

    void resolveDependencies(DependencyResolution dependencyResolution) {
        Set<ModuleDependency> moduleDependencies = new HashSet<>();
        if (this.project.kind() == ProjectKind.BALA_PROJECT) {
            for (ModuleDescriptor dependencyModDesc : moduleDescDependencies) {
                // Dependencies loaded from cache should not contain test dependencies
                addModuleDependency(dependencyModDesc.org(), dependencyModDesc.packageName(),
                        dependencyModDesc.name(), PackageDependencyScope.DEFAULT,
                        moduleDependencies, dependencyResolution);
            }
        } else {
            Set<ModuleLoadRequest> moduleLoadRequests = new OverwritableLinkedHashSet();
            moduleLoadRequests.addAll(this.allModuleLoadRequests);
            moduleLoadRequests.addAll(this.allTestModuleLoadRequests);
            for (ModuleLoadRequest modLoadRequest : moduleLoadRequests) {
                PackageOrg packageOrg;
                if (modLoadRequest.orgName().isEmpty()) {
                    if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
                        // This is an invalid import in a single file project.
                        continue;
                    }
                    packageOrg = descriptor().org();
                } else {
                    packageOrg = modLoadRequest.orgName().get();
                }

                addModuleDependency(packageOrg, modLoadRequest.moduleName(),
                        modLoadRequest.scope(), moduleDependencies, dependencyResolution);
            }
        }

        this.moduleDependencies = Collections.unmodifiableSet(moduleDependencies);
    }

    private void addModuleDependency(PackageOrg org,
                                     String moduleName,
                                     PackageDependencyScope scope,
                                     Set<ModuleDependency> moduleDependencies,
                                     DependencyResolution dependencyResolution) {
        Optional<ModuleContext> resolvedModuleOptional = dependencyResolution.getModule(org, moduleName);
        if (resolvedModuleOptional.isEmpty()) {
            return;
        }

        ModuleContext resolvedModule = resolvedModuleOptional.get();
        ModuleDependency moduleDependency = new ModuleDependency(
                new PackageDependency(resolvedModule.moduleId().packageId(), scope), resolvedModule.descriptor());
        moduleDependencies.add(moduleDependency);
    }

    private void addModuleDependency(PackageOrg org,
                                     PackageName packageName,
                                     ModuleName moduleName,
                                     PackageDependencyScope scope,
                                     Set<ModuleDependency> moduleDependencies,
                                     DependencyResolution dependencyResolution) {
        Optional<Module> resolvedModuleOptional = dependencyResolution.getModule(org, packageName, moduleName);
        if (resolvedModuleOptional.isEmpty()) {
            return;
        }

        Module resolvedModule = resolvedModuleOptional.get();
        ModuleDependency moduleDependency = new ModuleDependency(
                new PackageDependency(resolvedModule.packageInstance().packageId(), scope),
                resolvedModule.descriptor());
        moduleDependencies.add(moduleDependency);
    }

    void compile(CompilerContext compilerContext) {
        currentCompilationState().compile(this, compilerContext);
    }

    void generatePlatformSpecificCode(CompilerContext compilerContext, CompilerBackend compilerBackend) {
        currentCompilationState().generatePlatformSpecificCode(this, compilerContext, compilerBackend);
    }

    static void parseInternal(ModuleContext moduleContext) {
        for (DocumentContext docContext : moduleContext.srcDocContextMap.values()) {
            docContext.parse();
        }
    }

    static void resolveDependenciesInternal(ModuleContext moduleContext) {
    }

    static void compileInternal(ModuleContext moduleContext, CompilerContext compilerContext) {
        PackageID moduleCompilationId = moduleContext.descriptor().moduleCompilationId();
        String bootstrapLangLibName = System.getProperty("BOOTSTRAP_LANG_LIB");
        if (bootstrapLangLibName != null) {
            moduleContext.bootstrap.loadLangLib(compilerContext, moduleCompilationId);
        }

        org.wso2.ballerinalang.compiler.PackageCache packageCache =
                org.wso2.ballerinalang.compiler.PackageCache.getInstance(compilerContext);
        SymbolEnter symbolEnter = SymbolEnter.getInstance(compilerContext);
        CompilerPhaseRunner compilerPhaseRunner = CompilerPhaseRunner.getInstance(compilerContext);

        BLangPackage pkgNode = (BLangPackage) TreeBuilder.createPackageNode();
        pkgNode.moduleContextDataHolder = new ModuleContextDataHolder(
                moduleContext.isExported(),
                moduleContext.descriptor(),
                moduleContext.project.kind(),
                moduleContext.project.buildOptions().skipTests());
        packageCache.put(moduleCompilationId, pkgNode);

        // Parse source files
        for (DocumentContext documentContext : moduleContext.srcDocContextMap.values()) {
            pkgNode.addCompilationUnit(documentContext.compilationUnit(compilerContext, moduleCompilationId,
                                                                       REGULAR_SOURCE));
        }

        if (!moduleContext.testSrcDocumentIds().isEmpty()) {
            PackageID moduleTestCompilationId = moduleContext.descriptor().moduleTestCompilationId();
            moduleContext.parseTestSources(pkgNode, moduleTestCompilationId, compilerContext);
        }

        pkgNode.pos = new BLangDiagnosticLocation(moduleContext.moduleName().toString(), 0, 0, 0, 0);
        try {
            symbolEnter.definePackage(pkgNode);
            packageCache.putSymbol(pkgNode.packageID, pkgNode.symbol);
            compilerPhaseRunner.performTypeCheckPhases(pkgNode);
        } catch (Throwable t) {
            assert false : "Compilation failed due to" +
                    (t.getMessage() != null ? ": " + t.getMessage() : " an unhandled exception");
            compilerPhaseRunner.addDiagnosticForUnhandledException(pkgNode, t);
        }
        moduleContext.bLangPackage = pkgNode;
    }

    static void generateCodeInternal(ModuleContext moduleContext,
                                     CompilerBackend compilerBackend,
                                     CompilerContext compilerContext) {
        // Perform the rest of the compilation phases before generating platform-specific code
        String bootstrapLangLibName = System.getProperty("BOOTSTRAP_LANG_LIB");
        CompilerPhaseRunner compilerPhaseRunner = CompilerPhaseRunner.getInstance(compilerContext);
        if (bootstrapLangLibName != null) {
            compilerPhaseRunner.performLangLibBirGenPhases(moduleContext.bLangPackage);
        } else {
            try {
                compilerPhaseRunner.performBirGenPhases(moduleContext.bLangPackage);
            } catch (Throwable t) {
                assert false : "Compilation failed due to" +
                        (t.getMessage() != null ? ": " + t.getMessage() : " an unhandled exception");
                compilerPhaseRunner.addDiagnosticForUnhandledException(moduleContext.bLangPackage, t);
                return;
            }
        }

        // Note: The BIR and JAR caching should be atomic and so either both should be created or none.
        ByteArrayOutputStream birContent;

        // Skip caching BIR and JAR if there are diagnostics
        if (Diagnostics.hasErrors(moduleContext.diagnostics())) {
            return;
        }

        // Serialize the BIR  model
        birContent = generateBIR(moduleContext, compilerContext);

        // Skip the code generation phase if there are diagnostics
        if (Diagnostics.hasErrors(moduleContext.diagnostics())) {
            return;
        }

        // Generate and write the thin JAR to the file system
        compilerBackend.performCodeGen(moduleContext, moduleContext.compilationCache);

        if (birContent == null) {
            return;
        }

        // Write the bir to the file system
        // This code will execute only if JAR caching is successful
        // TODO: check the filesystem cache and delete if the cache is incomplete (if BIR or JAR is missing)
        moduleContext.compilationCache.cacheBir(moduleContext.moduleName(), birContent);
    }

    private static boolean shouldGenerateBir(ModuleContext moduleContext, CompilerContext compilerContext) {
        if (moduleContext.project.kind().equals(ProjectKind.BALA_PROJECT)) {
            return true;
        }
        if (ProjectUtils.isBuiltInPackage(
                moduleContext.descriptor().org(), moduleContext.descriptor().packageName().toString())) {
            return true;
        }
        CompilerOptions compilerOptions = CompilerOptions.getInstance(compilerContext);
        if (Boolean.parseBoolean(compilerOptions.get(CompilerOptionName.DUMP_BIR_FILE))) {
            return true;
        }
        if (moduleContext.project.kind().equals(ProjectKind.BUILD_PROJECT)
                && moduleContext.project().buildOptions().enableCache()) {
            return true;
        }

        return false;
    }

    private static ByteArrayOutputStream generateBIR(ModuleContext moduleContext, CompilerContext compilerContext) {
        if (!shouldGenerateBir(moduleContext, compilerContext)) {
            return null;
        }
        // Can we improve this logic
        ByteArrayOutputStream birContent = new ByteArrayOutputStream();
        try {
            CompiledBinaryFile.BIRPackageFile birPackageFile = moduleContext.bLangPackage.symbol.birPackageFile;
            if (birPackageFile == null) {
                birPackageFile = new CompiledBinaryFile
                        .BIRPackageFile(new BIRBinaryWriter(moduleContext.bLangPackage.symbol.bir).serialize());
                moduleContext.bLangPackage.symbol.birPackageFile = birPackageFile;
            }
            byte[] pkgBirBinaryContent = PackageFileWriter.writePackage(birPackageFile);
            birContent.writeBytes(pkgBirBinaryContent);
            return birContent;
        } catch (IOException e) {
            // This path may never be executed
            throw new RuntimeException("Failed to convert BIR model to a byte array", e);
        }
    }

    static void loadBirBytesInternal(ModuleContext moduleContext) {
        moduleContext.birBytes = moduleContext.compilationCache.getBir(moduleContext.moduleName());
    }

    static void resolveDependenciesFromBALAInternal(ModuleContext moduleContext) {
        // TODO implement
    }

    static void loadPackageSymbolInternal(ModuleContext moduleContext, CompilerContext compilerContext) {
        org.wso2.ballerinalang.compiler.PackageCache packageCache =
                org.wso2.ballerinalang.compiler.PackageCache.getInstance(compilerContext);
        BIRPackageSymbolEnter birPackageSymbolEnter = BIRPackageSymbolEnter.getInstance(compilerContext);

        PackageID moduleCompilationId = moduleContext.descriptor().moduleCompilationId();
        moduleContext.bPackageSymbol = birPackageSymbolEnter.definePackage(moduleCompilationId, moduleContext.birBytes);
        moduleContext.bPackageSymbol.exported = moduleContext.isExported();
        moduleContext.bPackageSymbol.descriptor = moduleContext.descriptor();
        packageCache.putSymbol(moduleCompilationId, moduleContext.bPackageSymbol);
    }

    static void loadPlatformSpecificCodeInternal(ModuleContext moduleContext, CompilerBackend compilerBackend) {
        // TODO implement
    }

    Optional<MdDocumentContext> moduleMdContext() {
        return Optional.ofNullable(this.moduleMdContext);
    }

    ModuleContext duplicate(Project project) {
        Map<DocumentId, DocumentContext> srcDocContextMap = new LinkedHashMap<>();
        for (DocumentId documentId : this.srcDocumentIds()) {
            DocumentContext documentContext = this.documentContext(documentId);
            srcDocContextMap.put(documentId, documentContext.duplicate());
        }

        Map<DocumentId, DocumentContext> testDocContextMap = new LinkedHashMap<>();
        for (DocumentId documentId : this.testSrcDocumentIds()) {
            DocumentContext documentContext = this.documentContext(documentId);
            testDocContextMap.put(documentId, documentContext.duplicate());
        }
        return new ModuleContext(project, this.moduleId, this.moduleDescriptor, this.isDefaultModule,
                srcDocContextMap, testDocContextMap, this.moduleMdContext().orElse(null),
                this.moduleDescDependencies, this.resourceContextMap, this.testResourceContextMap);
    }

    /**
     * An extended LinkedHashSet which can overwrite existing elements.
     */
    static class OverwritableLinkedHashSet extends LinkedHashSet<ModuleLoadRequest> {

        private static final long serialVersionUID = 1L;

        @Override
        public boolean add(ModuleLoadRequest moduleLoadRequest) {
            if (this.contains(moduleLoadRequest)) {
                Set<Location> locations = new HashSet<>();
                ModuleLoadRequest finalModuleLoadRequest = moduleLoadRequest;
                ModuleLoadRequest oldLoadRequest = this.stream().filter(
                        oldRequest -> oldRequest.equals(finalModuleLoadRequest)).findFirst().orElseThrow();
                locations.addAll(oldLoadRequest.locations());
                locations.addAll(moduleLoadRequest.locations());

                PackageDependencyScope scope = oldLoadRequest.scope() == PackageDependencyScope.DEFAULT ?
                        oldLoadRequest.scope() : moduleLoadRequest.scope();
                moduleLoadRequest = new ModuleLoadRequest(
                        oldLoadRequest.orgName().orElse(null),
                        oldLoadRequest.moduleName(),
                        scope,
                        oldLoadRequest.dependencyResolvedType(),
                        locations);
                this.remove(oldLoadRequest);
            }
            return super.add(moduleLoadRequest);
        }
    }
}
