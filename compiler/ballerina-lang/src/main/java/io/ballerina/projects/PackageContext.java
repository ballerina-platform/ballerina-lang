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
import io.ballerina.projects.PackageResolution.DependencyResolution;
import io.ballerina.projects.internal.model.CompilerPluginDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Maintains the internal state of a {@code Package} instance.
 * <p>
 * Works as a package cache.
 *
 * @since 2.0.0
 */
class PackageContext {
    private final Map<ModuleId, ModuleContext> moduleContextMap;
    private final Collection<ModuleId> moduleIds;
    private final Project project;
    private final PackageId packageId;
    private final PackageManifest packageManifest;
    private final DependencyManifest dependencyManifest;
    private final TomlDocumentContext ballerinaTomlContext;
    private final TomlDocumentContext dependenciesTomlContext;
    private final TomlDocumentContext cloudTomlContext;
    private final TomlDocumentContext compilerPluginTomlContext;
    private final MdDocumentContext packageMdContext;

    private final CompilationOptions compilationOptions;
    private ModuleContext defaultModuleContext;
    /**
     * This variable holds the dependency graph cached in a project.
     * At the moment, we cache the dependency graph in a bala file.
     */
    private final DependencyGraph<PackageDescriptor> pkgDescDependencyGraph;

    private Set<PackageDependency> packageDependencies;
    private DependencyGraph<ModuleDescriptor> moduleDependencyGraph;
    private PackageResolution packageResolution;
    private PackageCompilation packageCompilation;

    // TODO Try to reuse the unaffected compilations if possible
    private final Map<ModuleId, ModuleCompilation> moduleCompilationMap;

    PackageContext(Project project,
                   PackageId packageId,
                   PackageManifest packageManifest,
                   DependencyManifest dependencyManifest,
                   TomlDocumentContext ballerinaTomlContext,
                   TomlDocumentContext dependenciesTomlContext,
                   TomlDocumentContext cloudTomlContext,
                   TomlDocumentContext compilerPluginTomlContext,
                   MdDocumentContext packageMdContext,
                   CompilationOptions compilationOptions,
                   Map<ModuleId, ModuleContext> moduleContextMap,
                   DependencyGraph<PackageDescriptor> pkgDescDependencyGraph) {
        this.project = project;
        this.packageId = packageId;
        this.packageManifest = packageManifest;
        this.dependencyManifest = dependencyManifest;
        this.ballerinaTomlContext = ballerinaTomlContext;
        this.dependenciesTomlContext = dependenciesTomlContext;
        this.cloudTomlContext = cloudTomlContext;
        this.compilerPluginTomlContext = compilerPluginTomlContext;
        this.packageMdContext = packageMdContext;
        this.compilationOptions = compilationOptions;
        this.moduleIds = Collections.unmodifiableCollection(moduleContextMap.keySet());
        this.moduleContextMap = moduleContextMap;
        // TODO Try to reuse previous unaffected compilations
        this.moduleCompilationMap = new HashMap<>();
        this.packageDependencies = Collections.emptySet();
        this.pkgDescDependencyGraph = pkgDescDependencyGraph;

    }

    static PackageContext from(Project project, PackageConfig packageConfig, CompilationOptions compilationOptions) {
        Map<ModuleId, ModuleContext> moduleContextMap = new HashMap<>();
        for (ModuleConfig moduleConfig : packageConfig.otherModules()) {
            moduleContextMap.put(moduleConfig.moduleId(), ModuleContext.from(project, moduleConfig));
        }

        return new PackageContext(project, packageConfig.packageId(), packageConfig.packageManifest(),
                          packageConfig.dependencyManifest(),
                          packageConfig.ballerinaToml().map(c -> TomlDocumentContext.from(c)).orElse(null),
                          packageConfig.dependenciesToml().map(c -> TomlDocumentContext.from(c)).orElse(null),
                          packageConfig.cloudToml().map(c -> TomlDocumentContext.from(c)).orElse(null),
                          packageConfig.compilerPluginToml().map(c -> TomlDocumentContext.from(c)).orElse(null),
                          packageConfig.packageMd().map(c -> MdDocumentContext.from(c)).orElse(null),
                          compilationOptions, moduleContextMap, packageConfig.packageDescDependencyGraph());
    }

    PackageId packageId() {
        return packageId;
    }

    PackageName packageName() {
        return packageManifest.name();
    }

    PackageOrg packageOrg() {
        return packageManifest.org();
    }

    PackageVersion packageVersion() {
        return packageManifest.version();
    }

    PackageDescriptor descriptor() {
        return packageManifest.descriptor();
    }

    Optional<CompilerPluginDescriptor> compilerPluginDescriptor() {
        return packageManifest.compilerPluginDescriptor();
    }

    PackageManifest packageManifest() {
        return packageManifest;
    }

    DependencyManifest dependencyManifest() {
        return dependencyManifest;
    }

    Optional<TomlDocumentContext> ballerinaTomlContext() {
        return Optional.ofNullable(ballerinaTomlContext);
    }

    Optional<TomlDocumentContext> dependenciesTomlContext() {
        return Optional.ofNullable(dependenciesTomlContext);
    }

    Optional<TomlDocumentContext> cloudTomlContext() {
        return Optional.ofNullable(cloudTomlContext);
    }

    Optional<TomlDocumentContext> compilerPluginTomlContext() {
        return Optional.ofNullable(compilerPluginTomlContext);
    }

    Optional<MdDocumentContext> packageMdContext() {
        return Optional.ofNullable(packageMdContext);
    }

    CompilationOptions compilationOptions() {
        return compilationOptions;
    }

    Collection<ModuleId> moduleIds() {
        return moduleIds;
    }

    ModuleContext moduleContext(ModuleId moduleId) {
        return moduleContextMap.get(moduleId);
    }

    ModuleContext moduleContext(ModuleName moduleName) {
        for (ModuleContext moduleContext : moduleContextMap.values()) {
            if (moduleContext.moduleName().equals(moduleName)) {
                return moduleContext;
            }
        }
        return null;
    }

    ModuleContext defaultModuleContext() {
        if (defaultModuleContext != null) {
            return defaultModuleContext;
        }

        for (ModuleContext moduleContext : moduleContextMap.values()) {
            if (moduleContext.isDefaultModule()) {
                defaultModuleContext = moduleContext;
                return defaultModuleContext;
            }
        }

        throw new IllegalStateException("Default module not found. This is a bug in the Project API");
    }

    DependencyGraph<ModuleDescriptor> moduleDependencyGraph() {
        return moduleDependencyGraph;
    }

    ModuleCompilation getModuleCompilation(ModuleContext moduleContext) {
        return moduleCompilationMap.computeIfAbsent(moduleContext.moduleId(),
                moduleId -> new ModuleCompilation(this, moduleContext));
    }

    PackageCompilation getPackageCompilation() {
        if (packageCompilation == null) {
            packageCompilation = PackageCompilation.from(this, this.compilationOptions());
        }
        return packageCompilation;
    }

    PackageCompilation getPackageCompilation(CompilationOptions compilationOptions) {
        CompilationOptions options = CompilationOptions.builder()
                .setOffline(this.compilationOptions.offlineBuild())
                .setObservabilityIncluded(this.compilationOptions.observabilityIncluded())
                .setDumpBir(this.compilationOptions.dumpBir())
                .setCloud(this.compilationOptions.getCloud())
                .setDumpBirFile(this.compilationOptions.dumpBirFile())
                .setDumpGraph(this.compilationOptions.dumpGraph())
                .setDumpRawGraphs(this.compilationOptions.dumpRawGraphs())
                .setListConflictedClasses(this.compilationOptions.listConflictedClasses())
                .setConfigSchemaGen(this.compilationOptions.configSchemaGen())
                .setEnableCache(this.compilationOptions.enableCache())
                .build();
        CompilationOptions mergedOptions = options.acceptTheirs(compilationOptions);
        return PackageCompilation.from(this, mergedOptions);
    }

    PackageCompilation cachedCompilation() {
        return packageCompilation;
    }

    PackageResolution getResolution() {
        if (packageResolution == null) {
            packageResolution = PackageResolution.from(this, this.compilationOptions);
        }
        return packageResolution;
    }

    PackageResolution getResolution(CompilationOptions compilationOptions) {
        packageResolution = PackageResolution.from(this, compilationOptions);
        return packageResolution;
    }
    Collection<PackageDependency> packageDependencies() {
        return packageDependencies;
    }

    Project project() {
        return this.project;
    }

    DependencyGraph<PackageDescriptor> dependencyGraph() {
        return pkgDescDependencyGraph;
    }

    void resolveDependencies(DependencyResolution dependencyResolution) {
        // This method mutate the internal state of the moduleContext instance. This is considered as lazy loading
        // TODO Figure out a way to handle concurrent modifications

        // This dependency graph should only contain modules in this package.
        DependencyGraphBuilder<ModuleDescriptor> moduleDepGraphBuilder = DependencyGraphBuilder.getBuilder();
        Set<PackageDependency> packageDependencies = new HashSet<>();
        for (ModuleContext moduleContext : this.moduleContextMap.values()) {
            moduleDepGraphBuilder.add(moduleContext.descriptor());
            resolveModuleDependencies(moduleContext, dependencyResolution,
                    moduleDepGraphBuilder, packageDependencies);
        }

        this.packageDependencies = packageDependencies;
        this.moduleDependencyGraph = moduleDepGraphBuilder.build();
    }

    private void resolveModuleDependencies(ModuleContext moduleContext,
                                           DependencyResolution dependencyResolution,
                                           DependencyGraphBuilder<ModuleDescriptor> moduleDepGraphBuilder,
                                           Set<PackageDependency> packageDependencies) {
        moduleContext.resolveDependencies(dependencyResolution);
        for (ModuleDependency moduleDependency : moduleContext.dependencies()) {
            // Check whether this dependency is in this package
            if (moduleDependency.packageDependency().packageId() == this.packageId()) {
                // Module dependency graph contains only the modules in this package
                moduleDepGraphBuilder.addDependency(moduleContext.descriptor(), moduleDependency.descriptor());
            } else {
                // Capture the package dependency if it is different from this package
                packageDependencies.add(moduleDependency.packageDependency());
            }
        }
    }

    PackageContext duplicate(Project project) {
        Map<ModuleId, ModuleContext> duplicatedModuleContextMap = new HashMap<>();
        for (ModuleId moduleId : this.moduleIds) {
            ModuleContext moduleContext = this.moduleContext(moduleId);
            duplicatedModuleContextMap.put(moduleId, moduleContext.duplicate(project));
        }

        return new PackageContext(project, this.packageId, this.packageManifest,
                this.dependencyManifest, this.ballerinaTomlContext, this.dependenciesTomlContext,
                this.cloudTomlContext, this.compilerPluginTomlContext, this.packageMdContext,
                this.compilationOptions, duplicatedModuleContextMap, this.pkgDescDependencyGraph);
    }
}
