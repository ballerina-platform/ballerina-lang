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
import io.ballerina.projects.environment.ModuleLoadResponse;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.internal.CompilerPhaseRunner;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Maintains the internal state of a {@code Module} instance.
 * <p>
 * Works as a module cache.
 *
 * @since 2.0.0
 */
class ModuleContext {
    private final ModuleId moduleId;
    private ModuleName moduleName;
    private final Collection<DocumentId> srcDocIds;
    private final boolean isDefaultModule;
    private final Map<DocumentId, DocumentContext> srcDocContextMap;
    private final Collection<DocumentId> testSrcDocIds;
    private final Map<DocumentId, DocumentContext> testDocContextMap;
    private final Project project;

    private Set<ModuleDependency> moduleDependencies;
    private BLangPackage bLangPackage;

    // TODO How about introducing a ModuleState concept. ModuleState.DEPENDENCIES_RESOLVED
    private boolean dependenciesResolved;

    ModuleContext(Project project, ModuleId moduleId, ModuleName moduleName, boolean isDefaultModule,
                          Map<DocumentId, DocumentContext> srcDocContextMap,
                          Map<DocumentId, DocumentContext> testDocContextMap,
                          Set<ModuleDependency> moduleDependencies) {
        this.project = project;
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.isDefaultModule = isDefaultModule;
        this.srcDocContextMap = srcDocContextMap;
        this.srcDocIds = Collections.unmodifiableCollection(srcDocContextMap.keySet());
        this.testDocContextMap = testDocContextMap;
        this.testSrcDocIds = Collections.unmodifiableCollection(testDocContextMap.keySet());
        this.moduleDependencies = Collections.unmodifiableSet(moduleDependencies);
    }

    private ModuleContext(Project project, ModuleId moduleId, ModuleName moduleName, boolean isDefaultModule,
                          Map<DocumentId, DocumentContext> srcDocContextMap,
                          Map<DocumentId, DocumentContext> testDocContextMap) {
        this(project, moduleId, moduleName, isDefaultModule, srcDocContextMap, testDocContextMap,
                Collections.emptySet());
    }

    static ModuleContext from(Project project, ModuleConfig moduleConfig) {
        Map<DocumentId, DocumentContext> srcDocContextMap = new HashMap<>();
        for (DocumentConfig sourceDocConfig : moduleConfig.sourceDocs()) {
            srcDocContextMap.put(sourceDocConfig.documentId(), DocumentContext.from(sourceDocConfig));
        }

        Map<DocumentId, DocumentContext> testDocContextMap = new HashMap<>();
        for (DocumentConfig testSrcDocConfig : moduleConfig.testSourceDocs()) {
            testDocContextMap.put(testSrcDocConfig.documentId(), DocumentContext.from(testSrcDocConfig));
        }

        return new ModuleContext(project, moduleConfig.moduleId(), moduleConfig.moduleName(),
                moduleConfig.isDefaultModule(),
                srcDocContextMap,
                testDocContextMap);
    }

    ModuleId moduleId() {
        return this.moduleId;
    }

    ModuleName moduleName() {
        return this.moduleName;
    }

    Collection<DocumentId> srcDocumentIds() {
        return this.srcDocIds;
    }

    Collection<DocumentId> testSrcDocumentIds() {
        return this.testSrcDocIds;
    }

    DocumentContext documentContext(DocumentId documentId) {
        if (this.srcDocIds.contains(documentId)) {
            return this.srcDocContextMap.get(documentId);
        } else {
            return this.testDocContextMap.get(documentId);
        }
    }

    Project project() {
        return this.project;
    }

    boolean isDefaultModule() {
        return this.isDefaultModule;
    }

    boolean resolveDependencies() {
        // This method mutate the internal state of the moduleContext instance. This is considered as lazy loading
        // TODO Figure out a way to handle concurrent modifications
        // We should not mutate the object model for any modifications originated from the user
        if (dependenciesResolved) {
            return false;
        }

        // 1) Combine all the moduleLoadRequests of documents
        Set<ModuleLoadRequest> moduleLoadRequests = new HashSet<>();
        for (DocumentContext docContext : srcDocContextMap.values()) {
            moduleLoadRequests.addAll(docContext.moduleLoadRequests());
        }

        // 2) Resolve all the dependencies of this module
        PackageResolver packageResolver = project.environmentContext().getService(PackageResolver.class);
        Collection<ModuleLoadResponse> moduleLoadResponses = packageResolver.loadPackages(moduleLoadRequests);

        // The usage of Set eliminates duplicates
        Set<ModuleDependency> moduleDependencies = new HashSet<>();
        for (ModuleLoadResponse moduleLoadResponse : moduleLoadResponses) {
            ModuleDependency moduleDependency = new ModuleDependency(
                    new PackageDependency(moduleLoadResponse.packageId()),
                    moduleLoadResponse.moduleId());
            moduleDependencies.add(moduleDependency);
        }

        this.moduleDependencies = Collections.unmodifiableSet(moduleDependencies);
        this.dependenciesResolved = true;
        return true;
    }

    Collection<ModuleDependency> dependencies() {
        return moduleDependencies;
    }

    void compile(CompilerContext compilerContext, PackageName packageName) {
        // TODO use ModuleState enum
        if (bLangPackage != null) {
            return;
        }

        PackageCache packageCache = PackageCache.getInstance(compilerContext);
        SymbolEnter symbolEnter = SymbolEnter.getInstance(compilerContext);
        CompilerPhaseRunner compilerPhaseRunner = CompilerPhaseRunner.getInstance(compilerContext);

        PackageID pkgID = new PackageID(new Name(packageName.toString()),
                new Name(this.moduleName.toString()), new Name("0.1.0"));
        BLangPackage pkgNode = (BLangPackage) TreeBuilder.createPackageNode();
        packageCache.put(pkgID, pkgNode);

        for (DocumentContext documentContext : srcDocContextMap.values()) {
            pkgNode.addCompilationUnit(documentContext.compilationUnit(compilerContext, pkgID));
        }
        pkgNode.pos = new DiagnosticPos(new BDiagnosticSource(pkgID, this.moduleName.toString()), 1, 1, 1, 1);

        symbolEnter.definePackage(pkgNode);
        packageCache.putSymbol(pkgNode.packageID, pkgNode.symbol);
        compilerPhaseRunner.compile(pkgNode);
        this.bLangPackage = pkgNode;
    }
}
