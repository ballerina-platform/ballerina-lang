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
import io.ballerina.projects.environment.EnvironmentContext;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ProjectEnvironmentContext;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.IOException;
import java.nio.file.Path;
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
 *
 * @since 2.0.0
 */
public class PackageCompilation {

    private final PackageContext packageContext;
    private final PackageResolver packageResolver;
    private final CompilerContext compilerContext;

    private final DependencyGraph<PackageId> dependencyGraph;
    private List<Diagnostic> diagnostics;

    PackageCompilation(PackageContext packageContext) {
        this.packageContext = packageContext;

        // Resolving the dependencies of this package before the compilation
        packageContext.resolveDependencies();

        ProjectEnvironmentContext projectEnvContext = packageContext.project().environmentContext();
        EnvironmentContext environmentContext = projectEnvContext.getService(EnvironmentContext.class);
        this.packageResolver = projectEnvContext.getService(PackageResolver.class);
        this.dependencyGraph = buildDependencyGraph();
        this.compilerContext = environmentContext.compilerContext();
        compile(compilerContext);
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

    private void compile(CompilerContext compilerContext) {
        diagnostics = new ArrayList<>();
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
                moduleContext.compile(compilerContext, pkg.packageDescriptor());
                diagnostics.addAll(moduleContext.diagnostics());
            }
        }
        diagnostics = Collections.unmodifiableList(diagnostics);
    }

    public DependencyGraph<PackageId> packageDependencyGraph() {
        return this.dependencyGraph;
    }

    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    public void emit(OutputType outputType, Path filePath) {
        switch (outputType) {
            case EXEC:
                emitExecutable(filePath);
                break;
            case JAR:
                emitJar(filePath);
                break;
            case BIR:
                emitBirs(filePath);
                break;
            case BALO:
                emitBalo(filePath);
                break;
            default:
                throw new RuntimeException("Unexpected output type: " + outputType);
        }
    }

    private void emitBalo(Path filePath) {
        BaloWriter.write(packageResolver.getPackage(packageContext.packageId()), filePath);
    }

    private void emitBirs(Path filePath) {
        for (ModuleId moduleId : packageContext.moduleIds()) {
            BLangPackage bLangPackage = packageContext.moduleContext(moduleId).bLangPackage();
            if (bLangPackage != null) {
                String birName;
                if (packageContext.moduleContext(moduleId).moduleName().isDefaultModuleName()) {
                    birName = packageContext.moduleContext(moduleId).moduleName().packageName().toString();
                } else {
                    birName = packageContext.moduleContext(moduleId).moduleName().moduleNamePart();
                }
                BirWriter.write(bLangPackage,
                        filePath.resolve(birName + ProjectConstants.BLANG_COMPILED_PKG_BIR_EXT));
            }
        }
    }

    private void emitJar(Path filePath) {
        for (ModuleId moduleId : packageContext.moduleIds()) {
            BLangPackage bLangPackage = this.packageContext.moduleContext(moduleId).bLangPackage();
            String jarName;
            if (packageContext.moduleContext(moduleId).moduleName().isDefaultModuleName()) {
                jarName = packageContext.moduleContext(moduleId).moduleName().packageName().toString();
            } else {
                jarName = packageContext.moduleContext(moduleId).moduleName().moduleNamePart();
            }
            try {
                JarWriter.write(bLangPackage, filePath.resolve(jarName + ProjectConstants.BLANG_COMPILED_JAR_EXT));
            } catch (IOException e) {
                throw new RuntimeException("error while creating the jar file for package: " +
                        this.packageContext.packageName(), e);
            }
        }
    }

    private void emitExecutable(Path filePath) {
        if (this.packageContext.defaultModuleContext().bLangPackage().symbol.entryPointExists) {
            List<PackageId> sortedPackageIds = dependencyGraph.toTopologicallySortedList();
            List<BLangPackage> bLangPackageList = new ArrayList<>();
            BLangPackage entryBLangPackage = this.packageContext.defaultModuleContext().bLangPackage();
            sortedPackageIds.stream().map(packageResolver::getPackage).forEach(pkg -> {
                pkg.moduleIds().stream()
                        .filter(moduleId -> moduleId != this.packageContext.defaultModuleContext().moduleId())
                        .map(moduleId -> pkg.module(moduleId).getCompilation().bLangPackage())
                        .forEach(bLangPackageList::add);
            });
            try {
                JarWriter.write(entryBLangPackage, bLangPackageList, filePath);
                // copy the rt jar to the executable
                ProjectUtils.copyRuntimeJar(filePath);
            } catch (IOException e) {
                throw new RuntimeException("error while creating the executable jar file for package: " +
                        this.packageContext.packageName(), e);
            }
        } else {
            throw new UnsupportedOperationException("no entrypoint found in package: "
                    + this.packageContext.packageName());
        }
    }

    /**
     * Enum to represent output types.
     */
    public enum OutputType {

        BIR("bir"),
        EXEC("exec"),
        BALO("balo"),
        JAR("jar");

        private String value;

        OutputType(String value) {
            this.value = value;
        }
    }

    public SemanticModel getSemanticModel(ModuleId moduleId) {
        ModuleContext moduleContext = this.packageContext.moduleContext(moduleId);
        return new BallerinaSemanticModel(moduleContext.bLangPackage(), this.compilerContext);
    }
}
