/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.projects.internal.model.CompilerPluginDescriptor;
import io.ballerina.projects.internal.plugins.CompilerPlugins;
import io.ballerina.projects.plugins.CompilerPlugin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Responsible for loading and maintaining engaged compiler plugins.
 *
 * @since 2.0.0
 */
class CompilerPluginManager {
    private final PackageCompilation compilation;
    private final List<CompilerPluginContextIml> compilerPluginContexts;

    private CodeAnalyzerManager codeAnalyzerManager;
    private CodeGeneratorManager codeGeneratorManager;
    private CompilerLifecycleManager compilerLifecycleListenerManager;
    private CodeActionManager codeActionManager;

    private CompilerPluginManager(PackageCompilation compilation,
                                  List<CompilerPluginContextIml> compilerPluginContexts) {
        this.compilation = compilation;
        this.compilerPluginContexts = compilerPluginContexts;
    }

    static CompilerPluginManager from(PackageCompilation compilation) {
        // TODO We need to update the DependencyGraph API. Right now it is a mess
        PackageResolution packageResolution = compilation.getResolution();
        ResolvedPackageDependency rootPkgNode = new ResolvedPackageDependency(
                packageResolution.packageContext().project().currentPackage(), PackageDependencyScope.DEFAULT);
        DependencyGraph<ResolvedPackageDependency> dependencyGraph = packageResolution.dependencyGraph();
        List<Package> directDependencies = getDirectDependencies(rootPkgNode, dependencyGraph);
        List<CompilerPluginInfo> compilerPlugins = loadEngagedCompilerPlugins(directDependencies);
        List<CompilerPluginInfo> inBuiltCompilerPlugins = loadInBuiltCompilerPlugins(rootPkgNode.packageInstance());
        compilerPlugins.addAll(inBuiltCompilerPlugins);
        List<CompilerPluginContextIml> compilerPluginContexts = initializePlugins(compilerPlugins);
        return new CompilerPluginManager(compilation, compilerPluginContexts);
    }

    private static List<CompilerPluginInfo> loadInBuiltCompilerPlugins(Package rootPackage) {
        List<CompilerPluginInfo> compilerPluginInfoList = new ArrayList<>();
        for (CompilerPlugin plugin : CompilerPlugins.getBuiltInPlugins()) {
            CompilerPlugin compilerPlugin;
            try {
                if (!rootPackage.manifest().descriptor().isBuiltInPackage() &&
                        !rootPackage.project().kind().equals(ProjectKind.BALA_PROJECT)) {
                    compilerPlugin = CompilerPlugins.loadCompilerPlugin(
                            plugin.getClass().getName(), Collections.emptyList());
                    compilerPluginInfoList.add(new BuiltInCompilerPluginInfo(compilerPlugin));
                }
            } catch (Throwable e) {
                // Used Throwable here to catch any sort of error produced by the in-built compiler plugin code
                throw new ProjectException("Failed to load the compiler plugin class: '"
                        + plugin.getClass().getName()  + "'. " + e.getMessage());
            }
        }
        return compilerPluginInfoList;
    }

    PackageCompilation compilation() {
        return compilation;
    }

    CodeAnalyzerManager getCodeAnalyzerManager() {
        if (codeAnalyzerManager != null) {
            return codeAnalyzerManager;
        }

        codeAnalyzerManager = CodeAnalyzerManager.from(compilation, compilerPluginContexts);
        return codeAnalyzerManager;
    }

    CodeGeneratorManager getCodeGeneratorManager() {
        if (codeGeneratorManager != null) {
            return codeGeneratorManager;
        }

        codeGeneratorManager = CodeGeneratorManager.from(compilation, compilerPluginContexts);
        return codeGeneratorManager;
    }

    CodeActionManager getCodeActionManager() {
        if (codeActionManager != null) {
            return codeActionManager;
        }

        codeActionManager = CodeActionManager.from(compilerPluginContexts);
        return codeActionManager;
    }

    int engagedCodeGeneratorCount() {
        int count = 0;
        for (CompilerPluginContextIml compilerPluginContext : compilerPluginContexts) {
            count += compilerPluginContext.codeGenerators().size();
        }
        return count;
    }

    private static List<CompilerPluginInfo> loadEngagedCompilerPlugins(List<Package> dependencies) {
        List<CompilerPluginInfo> compilerPlugins = new ArrayList<>();
        for (Package pkgDependency : dependencies) {
            PackageManifest pkgManifest = pkgDependency.manifest();
            pkgManifest.compilerPluginDescriptor()
                    .ifPresent(pluginDesc -> compilerPlugins.add(loadCompilerPlugin(pluginDesc, pkgManifest)));
        }
        return compilerPlugins;
    }

    private static CompilerPluginInfo loadCompilerPlugin(CompilerPluginDescriptor pluginDescriptor,
                                                         PackageManifest pkgManifest) {
        String pluginClassName = pluginDescriptor.plugin().getClassName();
        List<Path> jarLibraryPaths = pluginDescriptor.getCompilerPluginDependencies()
                .stream()
                .map(Paths::get)
                .collect(Collectors.toList());

        CompilerPlugin compilerPlugin;
        try {
            compilerPlugin = CompilerPlugins.loadCompilerPlugin(pluginClassName, jarLibraryPaths);
        } catch (Throwable e) {
            // Used Throwable here catch any sort of error produced by the third-party compiler plugin code
            throw new ProjectException("Failed to load the compiler plugin in package: '"
                    + pkgManifest.org() +
                    ":" + pkgManifest.name() +
                    ":" + pkgManifest.version() + "'. " + e.getMessage(), e);
        }

        return new PackageProvidedCompilerPluginInfo(compilerPlugin, pkgManifest.descriptor(), pluginDescriptor);
    }

    private static List<Package> getDirectDependencies(ResolvedPackageDependency rootPkgNode,
                                                       DependencyGraph<ResolvedPackageDependency> dependencyGraph) {
        return dependencyGraph.getDirectDependencies(rootPkgNode)
                .stream()
                .map(ResolvedPackageDependency::packageInstance)
                .collect(Collectors.toList());
    }

    private static List<CompilerPluginContextIml> initializePlugins(List<CompilerPluginInfo> compilerPlugins) {
        List<CompilerPluginContextIml> compilerPluginContexts = new ArrayList<>(compilerPlugins.size());
        for (CompilerPluginInfo compilerPluginInfo : compilerPlugins) {
            CompilerPluginContextIml pluginContext = new CompilerPluginContextIml(compilerPluginInfo);
            initializePlugin(compilerPluginInfo, pluginContext);
            compilerPluginContexts.add(pluginContext);
        }
        return compilerPluginContexts;
    }

    private static void initializePlugin(CompilerPluginInfo compilerPluginInfo,
                                         CompilerPluginContextIml pluginContext) {
        try {
            compilerPluginInfo.compilerPlugin().init(pluginContext);
        } catch (Throwable e) {
            // Used Throwable here catch any sort of error produced by the third-party compiler plugin code
            String message;
            if (compilerPluginInfo.kind().equals(CompilerPluginKind.PACKAGE_PROVIDED)) {
                PackageDescriptor pkgDesc = ((PackageProvidedCompilerPluginInfo) compilerPluginInfo).packageDesc();
                message = "Failed to initialize the compiler plugin in package: '"
                        + pkgDesc.org() +
                        ":" + pkgDesc.name() +
                        ":" + pkgDesc.version() + "'. ";

            } else {
                message = "Failed to initialize the compiler plugin: '"
                        + compilerPluginInfo.compilerPlugin().getClass().getName()  + "'. ";
            }
            throw new ProjectException(message + e.getMessage(), e);

        }
    }

    public CompilerLifecycleManager getCompilerLifecycleListenerManager() {
        if (compilerLifecycleListenerManager == null) {
            return compilerLifecycleListenerManager =
                    CompilerLifecycleManager.from(compilation, compilerPluginContexts);
        }

        return compilerLifecycleListenerManager;
    }
}
