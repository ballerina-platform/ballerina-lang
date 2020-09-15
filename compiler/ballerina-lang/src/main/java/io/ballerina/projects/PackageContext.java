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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Maintains the internal state of a {@code Package} instance.
 * <p>
 * Works as a package cache.
 *
 * @since 2.0.0
 */
class PackageContext {
    private final PackageConfig packageConfig;
    private final Map<ModuleId, ModuleContext> moduleContextMap;
    private final Collection<ModuleId> moduleIds;
    private final Project project;
    private ModuleContext defaultModuleContext;
    private boolean dependenciesResolved;

    private Set<PackageDependency> packageDependencies;
    private DependencyGraph<ModuleId> moduleDependencyGraph;

    // TODO Try to reuse the unaffected compilations if possible
    private final Map<ModuleId, ModuleCompilation> moduleCompilationMap;
    //    private final BallerinaToml ballerinaToml;

    private PackageContext(Project project, PackageConfig packageConfig,
                           Map<ModuleId, ModuleContext> moduleContextMap) {
        this.project = project;
        this.packageConfig = packageConfig;
        this.moduleIds = Collections.unmodifiableCollection(moduleContextMap.keySet());
        this.moduleContextMap = moduleContextMap;
        // TODO Try to reuse previous unaffected compilations
        this.moduleCompilationMap = new HashMap<>();
        this.packageDependencies = Collections.emptySet();
        this.moduleDependencyGraph = DependencyGraph.emptyGraph();

//        // load Ballerina.toml
//        Path ballerinaTomlPath = packageConfig.packagePath().resolve(ProjectConstants.BALLERINA_TOML);
//        if (ballerinaTomlPath.toFile().exists()) {
//            try {
//                this.ballerinaToml = BallerinaTomlProcessor.parse(ballerinaTomlPath);
//            } catch (IOException | TomlException e) {
//                throw new RuntimeException(e.getMessage(), e);
//            }
//        } else {
//            this.ballerinaToml = new BallerinaToml();
//        }
    }

    private PackageContext(Project project, PackageConfig packageConfig,
                           Map<ModuleId, ModuleContext> moduleContextMap,
                           Set<PackageDependency> packageDependencies,
                           DependencyGraph<ModuleId> moduleDependencyGraph) {
        this.project = project;
        this.packageConfig = packageConfig;
        this.moduleIds = Collections.unmodifiableCollection(moduleContextMap.keySet());
        this.moduleContextMap = moduleContextMap;
        // TODO Try to reuse previous unaffected compilations
        this.moduleCompilationMap = new HashMap<>();
        this.packageDependencies = Collections.unmodifiableSet(packageDependencies);
        this.moduleDependencyGraph = moduleDependencyGraph;
    }

    static PackageContext from(Project project, PackageConfig packageConfig) {
        final Map<ModuleId, ModuleContext> moduleContextMap = new HashMap<>();
        for (ModuleConfig moduleConfig : packageConfig.otherModules()) {
            moduleContextMap.put(moduleConfig.moduleId(), ModuleContext.from(project, moduleConfig));
        }

        // Create module dependency graph
        return new PackageContext(project, packageConfig, moduleContextMap);
    }

    PackageId packageId() {
        return this.packageConfig.packageId();
    }

    PackageName packageName() {
        return packageConfig.packageName();
    }

    Collection<ModuleId> moduleIds() {
        return this.moduleIds;
    }

    ModuleContext moduleContext(ModuleId moduleId) {
        return this.moduleContextMap.get(moduleId);
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

    Collection<PackageDependency> packageDependencies() {
        return packageDependencies;
    }

    Project project() {
        return this.project;
    }

    void resolveDependencies() {
        // This method mutate the internal state of the moduleContext instance. This is considered as lazy loading
        // TODO Figure out a way to handle concurrent modifications
        // We should not mutate the object model for any modifications originated from the user
        if (dependenciesResolved) {
            return;
        }

        Map<ModuleId, Set<ModuleId>> moduleDependencyIdMap = new HashMap<>();
        Set<PackageDependency> packageDependencies = new HashSet<>(this.packageDependencies);
        for (ModuleContext moduleContext : this.moduleContextMap.values()) {
            ModuleId moduleId = moduleContext.moduleId();
            Set<ModuleId> moduleDependencyIds;
            if (moduleContext.resolveDependencies()) {
                // Update package dependencies
                moduleDependencyIds = new HashSet<>();
                for (ModuleDependency moduleDependency : moduleContext.dependencies()) {
                    // Check whether this dependency is in this package
                    if (moduleDependency.packageDependency().packageId() == this.packageId()) {
                        // Module dependency graph contains only the modules in this package
                        moduleDependencyIds.add(moduleDependency.moduleId());
                    } else {
                        // Capture the package dependency if it is different from this package
                        packageDependencies.add(moduleDependency.packageDependency());
                    }
                }
            } else {
                Collection<ModuleId> moduleDependencies = moduleDependencyGraph.getDirectDependencies(
                        moduleId);
                moduleDependencyIds = new HashSet<>(moduleDependencies);
            }
            moduleDependencyIdMap.put(moduleId, moduleDependencyIds);
        }

        DependencyGraph<ModuleId> moduleDependencyGraph = new DependencyGraph<>(moduleDependencyIdMap);
        this.packageDependencies = packageDependencies;
        this.moduleDependencyGraph = moduleDependencyGraph;
        this.dependenciesResolved = true;
    }

//    BallerinaToml ballerinaToml() {
//        return this.ballerinaToml;
//    }
}
