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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.projects.environment.ProjectEnvironment;
import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compilation at module level by resolving all the dependencies.
 *
 * @since 2.0.0
 */
public class ModuleCompilation {
    private final ModuleContext moduleContext;
    private final PackageContext packageContext;
    private final PackageCache packageCache;
    private final CompilerContext compilerContext;

    private final DependencyGraph<ModuleId> dependencyGraph;
    private DiagnosticResult diagnosticResult;

    ModuleCompilation(PackageContext packageContext, ModuleContext moduleContext) {
        this.packageContext = packageContext;
        this.moduleContext = moduleContext;

        // Resolving the dependencies of this package before the compilation
        packageContext.getResolution();

        // TODO Figure out a better way to handle this
        ProjectEnvironment projectEnvContext = packageContext.project().projectEnvironmentContext();
        this.packageCache = projectEnvContext.getService(PackageCache.class);
        this.compilerContext = projectEnvContext.getService(CompilerContext.class);
        this.dependencyGraph = buildDependencyGraph();
        compile();
    }

    private DependencyGraph<ModuleId> buildDependencyGraph() {
        Map<ModuleId, Set<ModuleId>> dependencyIdMap = new HashMap<>();
        addModuleDependencies(moduleContext.moduleId(), dependencyIdMap);
        return DependencyGraph.from(dependencyIdMap);
    }

    private void addModuleDependencies(ModuleId moduleId, Map<ModuleId, Set<ModuleId>> dependencyIdMap) {
        Package pkg = packageCache.getPackageOrThrow(moduleId.packageId());
        Collection<ModuleId> directDependencies = new HashSet<>(pkg.moduleDependencyGraph().
                getDirectDependencies(moduleId));

        ModuleContext moduleCtx = pkg.packageContext().moduleContext(moduleId);
        for (ModuleDependency moduleDependency : moduleCtx.dependencies()) {
            PackageId dependentPkgId = moduleDependency.packageDependency().packageId();
            if (dependentPkgId == pkg.packageId()) {
                continue;
            }
            ModuleId dependentModuleId = moduleDependency.moduleId();
            directDependencies.add(dependentModuleId);
            addModuleDependencies(dependentModuleId, dependencyIdMap);
        }

        dependencyIdMap.put(moduleId, new HashSet<>(directDependencies));
        for (ModuleId depModuleId : directDependencies) {
            addModuleDependencies(depModuleId, dependencyIdMap);
        }
    }

    private void compile() {
        // Compile all the modules
        List<Diagnostic> diagnostics = new ArrayList<>();
        List<ModuleId> sortedModuleIds = dependencyGraph.toTopologicallySortedList();
        for (ModuleId sortedModuleId : sortedModuleIds) {
            Package pkg = packageCache.getPackageOrThrow(sortedModuleId.packageId());
            ModuleContext moduleContext = pkg.module(sortedModuleId).moduleContext();
            moduleContext.compile(compilerContext);
            moduleContext.diagnostics().forEach(diagnostic ->
                    diagnostics.add(new PackageDiagnostic(diagnostic, moduleContext.moduleName())));
        }

        // Create an immutable list
        diagnosticResult = new DefaultDiagnosticResult(diagnostics);
    }

    public SemanticModel getSemanticModel() {
        return new BallerinaSemanticModel(this.moduleContext.bLangPackage(), this.compilerContext);
    }

    public DiagnosticResult diagnostics() {
        return diagnosticResult;
    }
}


