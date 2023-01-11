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
import java.util.Optional;
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

    private final DependencyGraph<ModuleDescriptor> dependencyGraph;
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

    private DependencyGraph<ModuleDescriptor> buildDependencyGraph() {
        Map<ModuleDescriptor, Set<ModuleDescriptor>> dependencyDescriptorMap = new HashMap<>();
        addModuleDependencies(moduleContext.descriptor(), dependencyDescriptorMap);
        return DependencyGraph.from(dependencyDescriptorMap);
    }

    private void addModuleDependencies(ModuleDescriptor moduleDescriptor,
                                       Map<ModuleDescriptor, Set<ModuleDescriptor>> dependencyDescriptorMap) {
        Optional<Package> pkg = packageCache.getPackage(moduleDescriptor.org(),
                moduleDescriptor.packageName(), moduleDescriptor.version());
        if (pkg.isEmpty()) {
            throw new IllegalStateException("Cannot find a package for the given details, org: "
                    + moduleDescriptor.org() + ", name: " + moduleDescriptor.packageName()
                    + ", version: " + moduleDescriptor.version());
        }
        Collection<ModuleDescriptor> directDependencies = new HashSet<>(pkg.get().moduleDependencyGraph().
                getDirectDependencies(moduleDescriptor));

        ModuleContext moduleCtx = pkg.get().packageContext().moduleContext(moduleDescriptor.name());
        for (ModuleDependency moduleDependency : moduleCtx.dependencies()) {
            PackageId dependentPkgId = moduleDependency.packageDependency().packageId();
            if (dependentPkgId == pkg.get().packageId()) {
                continue;
            }
            ModuleDescriptor dependentModuleDescriptor = moduleDependency.descriptor();
            directDependencies.add(dependentModuleDescriptor);
            addModuleDependencies(dependentModuleDescriptor, dependencyDescriptorMap);
        }

        dependencyDescriptorMap.put(moduleDescriptor, new HashSet<>(directDependencies));
        for (ModuleDescriptor depModuleDescriptor : directDependencies) {
            addModuleDependencies(depModuleDescriptor, dependencyDescriptorMap);
        }
    }

    private void compile() {
        // Compile all the modules
        List<Diagnostic> diagnostics = new ArrayList<>();
        List<ModuleDescriptor> sortedModuleDescriptors = dependencyGraph.toTopologicallySortedList();
        for (ModuleDescriptor sortedModuleDescriptor : sortedModuleDescriptors) {
            Optional<Package> pkg = packageCache.getPackage(sortedModuleDescriptor.org(),
                    sortedModuleDescriptor.packageName(), sortedModuleDescriptor.version());
            if (pkg.isEmpty()) {
                throw new IllegalStateException("Cannot find a package for the given details, org: "
                        + sortedModuleDescriptor.org() + ", name: " + sortedModuleDescriptor.packageName()
                        + ", version: " + sortedModuleDescriptor.version());
            }
            ModuleContext moduleContext = pkg.get().module(sortedModuleDescriptor.name()).moduleContext();
            moduleContext.compile(compilerContext);
            for (Diagnostic diagnostic : moduleContext.diagnostics()) {
                diagnostics.add(new PackageDiagnostic(diagnostic, moduleContext.descriptor(), moduleContext.project()));
            }
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


