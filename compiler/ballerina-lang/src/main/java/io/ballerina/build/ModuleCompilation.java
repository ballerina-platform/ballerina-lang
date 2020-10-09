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
package io.ballerina.build;

import io.ballerina.build.environment.PackageResolver;
import io.ballerina.build.environment.ProjectEnvironmentContext;
import io.ballerina.build.internal.CompilerPhaseRunner;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.model.elements.PackageID.ANNOTATIONS;

/**
 *
 */
public class ModuleCompilation {
    private final ModuleContext moduleContext;
    private final PackageContext packageContext;
    private final PackageResolver packageResolver;
    private final CompilerContext compilerContext;

    private final DependencyGraph<ModuleId> dependencyGraph;

    ModuleCompilation(PackageContext packageContext, ModuleContext moduleContext) {
        this.packageContext = packageContext;
        this.moduleContext = moduleContext;

        // Resolving the dependencies of this package before the compilation
        packageContext.resolveDependencies();

        // TODO Figure out a better way to handle this
        ProjectEnvironmentContext projectEnvContext = packageContext.project().environmentContext();
        this.packageResolver = projectEnvContext.getService(PackageResolver.class);
        this.compilerContext = projectEnvContext.getService(CompilerContext.class);
        this.dependencyGraph = buildDependencyGraph();
        compile();
    }

    private DependencyGraph<ModuleId> buildDependencyGraph() {
        Map<ModuleId, Set<ModuleId>> dependencyIdMap = new HashMap<>();
        addModuleDependencies(moduleContext.moduleId(), dependencyIdMap);
        return new DependencyGraph<>(dependencyIdMap);
    }

    private void addModuleDependencies(ModuleId moduleId, Map<ModuleId, Set<ModuleId>> dependencyIdMap) {
        Package pkg = packageResolver.getPackage(moduleId.packageId());
        Collection<ModuleId> directDependencies = pkg.moduleDependencyGraph().getDirectDependencies(moduleId);

        ModuleContext moduleContext = pkg.packageContext().moduleContext(moduleId);
        for (ModuleDependency moduleDependency : moduleContext.dependencies()) {
            PackageId packageId = moduleDependency.packageDependency().packageId();
            if (packageId == pkg.packageId()) {
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
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);
        PackageCache packageCache = PackageCache.getInstance(compilerContext);

        // TODO This is a temporary workaround to compile and store the lang.annotations modules
        CompilerPhaseRunner compilerPhaseRunner = CompilerPhaseRunner.getInstance(compilerContext);
        symbolTable.langAnnotationModuleSymbol = compilerPhaseRunner.getLangModuleFromSource(ANNOTATIONS);
        packageCache.putSymbol(ANNOTATIONS, symbolTable.langAnnotationModuleSymbol);

        // Compile all the modules
        List<ModuleId> sortedModuleIds = dependencyGraph.toTopologicallySortedList();
        for (ModuleId sortedModuleId : sortedModuleIds) {
            Package pkg = packageResolver.getPackage(sortedModuleId.packageId());
            ModuleContext moduleContext = pkg.module(sortedModuleId).moduleContext();
            moduleContext.compile(compilerContext, pkg.packageName());
        }
    }

    public void getSemanticModel() {
        throw new UnsupportedOperationException();
    }

    BLangPackage bLangPackage() {
        return this.moduleContext.bLangPackage();
    }
}


