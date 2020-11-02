/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ProjectEnvironmentContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Compilation at package level by resolving all the dependencies.
 * <p>
 * This class compiles a module up to the codegen phase at the moment.
 *
 * @since 2.0.0
 */
public class PackageCompilation {
    private final PackageContext packageContext;
    private final PackageResolver packageResolver;
    private final CompilerContext compilerContext;

    private final DependencyGraph<PackageId> dependencyGraph;
    private final List<ModuleContext> sortedModuleContextList;
    private List<Diagnostic> diagnostics;

    private boolean compiled;

    PackageCompilation(PackageContext packageContext) {
        this.packageContext = packageContext;

        ProjectEnvironmentContext projectEnvContext = packageContext.project().projectEnvironmentContext();
        this.packageResolver = projectEnvContext.getService(PackageResolver.class);
        this.compilerContext = projectEnvContext.getService(CompilerContext.class);

        // Resolving the dependencies of this package before the compilation
        packageContext.resolveDependencies();
        this.dependencyGraph = buildDependencyGraph();
        this.sortedModuleContextList = populateSortedModuleList();
    }

    private DependencyGraph<PackageId> buildDependencyGraph() {
        Map<PackageId, Set<PackageId>> dependencyIdMap = new HashMap<>();
        addPackageDependencies(packageContext.packageId(), dependencyIdMap);
        return new DependencyGraph<>(dependencyIdMap);
    }

    private void addPackageDependencies(PackageId packageId, Map<PackageId, Set<PackageId>> dependencyIdMap) {
        Package pkg = packageResolver.getPackage(packageId);
        Collection<PackageId> directDependencies = pkg.packageDependencies().stream()
                .map(PackageDependency::packageId)
                .collect(Collectors.toList());
        dependencyIdMap.put(packageId, new HashSet<>(directDependencies));
        for (PackageId dependentPackageId : directDependencies) {
            addPackageDependencies(dependentPackageId, dependencyIdMap);
        }
    }

    private List<ModuleContext> populateSortedModuleList() {
        List<ModuleContext> sortedModuleContextList = new ArrayList<>();
        // Topologically sort packages in the package dependency graph.
        // Iterate through the sorted package list
        // Get the module dependency graph of the package.
        // This graph should only contain the modules in that particular package.
        // Topologically sort the module dependency graph.
        // Iterate through the sorted module list.
        // Compile the module and collect diagnostics.
        // Repeat this for each module in each package in the package dependency graph.
        List<PackageId> sortedPackageIds = dependencyGraph.toTopologicallySortedList();
        for (PackageId packageId : sortedPackageIds) {
            Package pkg = packageResolver.getPackage(packageId);
            DependencyGraph<ModuleId> moduleDependencyGraph = pkg.moduleDependencyGraph();
            List<ModuleId> sortedModuleIds = moduleDependencyGraph.toTopologicallySortedList();
            for (ModuleId moduleId : sortedModuleIds) {
                ModuleContext moduleContext = pkg.module(moduleId).moduleContext();
                sortedModuleContextList.add(moduleContext);
            }
        }
        return Collections.unmodifiableList(sortedModuleContextList);
    }

    List<ModuleContext> sortedModuleContextList() {
        return this.sortedModuleContextList;
    }

    private void compile() {
        diagnostics = new ArrayList<>();
        for (ModuleContext moduleContext : sortedModuleContextList) {
            moduleContext.compile(compilerContext);
            diagnostics.addAll(moduleContext.diagnostics());
        }
        diagnostics = Collections.unmodifiableList(diagnostics);
        compiled = true;
    }

    PackageContext packageContext() {
        return packageContext;
    }

    public DependencyGraph<PackageId> packageDependencyGraph() {
        return this.dependencyGraph;
    }

    public List<Diagnostic> diagnostics() {
        // TODO think about parallel invocations of this method
        if (!compiled) {
            compile();
        }
        return diagnostics;
    }

    public boolean hasDiagnostics() {
        return !diagnostics().isEmpty();
    }

    public SemanticModel getSemanticModel(ModuleId moduleId) {
        // TODO think about parallel invocations of this method
        if (!compiled) {
            compile();
        }

        ModuleContext moduleContext = this.packageContext.moduleContext(moduleId);
        return new BallerinaSemanticModel(moduleContext.bLangPackage(), this.compilerContext);
    }

    // TODO Remove this method. We should not expose BLangPackage from this class
    public BLangPackage defaultModuleBLangPackage() {
        return this.packageContext.defaultModuleContext().bLangPackage();
    }
}
