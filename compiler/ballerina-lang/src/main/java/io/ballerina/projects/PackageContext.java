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
    private final BallerinaToml ballerinaToml;
    private ModuleContext defaultModuleContext;
    /**
     * This variable holds the dependency graph cached in a project.
     * At the moment, we cache the dependency graph in a balr file.
     */
    private final DependencyGraph<PackageDescriptor> pkgDescDependencyGraph;

    private Set<PackageDependency> packageDependencies;
    private DependencyGraph<ModuleId> moduleDependencyGraph;
    private PackageResolution packageResolution;
    private PackageCompilation packageCompilation;

    // TODO Try to reuse the unaffected compilations if possible
    private final Map<ModuleId, ModuleCompilation> moduleCompilationMap;

    PackageContext(Project project,
                   PackageId packageId,
                   PackageManifest packageManifest,
                   BallerinaToml ballerinaToml,
                   Map<ModuleId, ModuleContext> moduleContextMap,
                   DependencyGraph<PackageDescriptor> pkgDescDependencyGraph) {
        this.project = project;
        this.packageId = packageId;
        this.packageManifest = packageManifest;
        this.ballerinaToml = ballerinaToml;
        this.moduleIds = Collections.unmodifiableCollection(moduleContextMap.keySet());
        this.moduleContextMap = moduleContextMap;
        // TODO Try to reuse previous unaffected compilations
        this.moduleCompilationMap = new HashMap<>();
        this.packageDependencies = Collections.emptySet();
        this.pkgDescDependencyGraph = pkgDescDependencyGraph;
    }

    static PackageContext from(Project project, PackageConfig packageConfig) {
        Map<ModuleId, ModuleContext> moduleContextMap = new HashMap<>();
        for (ModuleConfig moduleConfig : packageConfig.otherModules()) {
            moduleContextMap.put(moduleConfig.moduleId(), ModuleContext.from(project, moduleConfig));
        }

        return new PackageContext(project, packageConfig.packageId(), packageConfig.packageManifest(),
                packageConfig.ballerinaToml(), moduleContextMap, packageConfig.packageDescDependencyGraph());
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

    PackageManifest manifest() {
        return packageManifest;
    }

    public Optional<BallerinaToml> ballerinaToml() {
        return Optional.ofNullable(ballerinaToml);
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

    DependencyGraph<ModuleId> moduleDependencyGraph() {
        return moduleDependencyGraph;
    }

    ModuleCompilation getModuleCompilation(ModuleContext moduleContext) {
        return moduleCompilationMap.computeIfAbsent(moduleContext.moduleId(),
                moduleId -> new ModuleCompilation(this, moduleContext));
    }

    PackageCompilation getPackageCompilation() {
        if (packageCompilation == null) {
            packageCompilation = PackageCompilation.from(this);
        }
        return packageCompilation;
    }

    PackageResolution getResolution() {
        if (packageResolution == null) {
            packageResolution = PackageResolution.from(this);
        }
        return packageResolution;
    }

    Collection<PackageDependency> packageDependencies() {
        return packageDependencies;
    }

    CompilationOptions compilationOptions() {
        return null;
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
        DependencyGraphBuilder<ModuleId> moduleDepGraphBuilder = DependencyGraphBuilder.getBuilder();
        Set<PackageDependency> packageDependencies = new HashSet<>();
        for (ModuleContext moduleContext : this.moduleContextMap.values()) {
            moduleDepGraphBuilder.add(moduleContext.moduleId());
            resolveModuleDependencies(moduleContext, dependencyResolution,
                    moduleDepGraphBuilder, packageDependencies);
        }

        this.packageDependencies = packageDependencies;
        this.moduleDependencyGraph = moduleDepGraphBuilder.build();
    }

    private void resolveModuleDependencies(ModuleContext moduleContext,
                                           DependencyResolution dependencyResolution,
                                           DependencyGraphBuilder<ModuleId> moduleDepGraphBuilder,
                                           Set<PackageDependency> packageDependencies) {
        moduleContext.resolveDependencies(dependencyResolution);
        for (ModuleDependency moduleDependency : moduleContext.dependencies()) {
            // Check whether this dependency is in this package
            if (moduleDependency.packageDependency().packageId() == this.packageId()) {
                // Module dependency graph contains only the modules in this package
                moduleDepGraphBuilder.addDependency(moduleContext.moduleId(), moduleDependency.moduleId());
            } else {
                // Capture the package dependency if it is different from this package
                packageDependencies.add(moduleDependency.packageDependency());
            }
        }
    }
}
