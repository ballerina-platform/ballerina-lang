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

import io.ballerina.projects.plugins.CompilerPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 2.0.0
 */
class CompilerPluginManager {
    private final PackageCompilation compilation;
    private final List<CompilerPluginContextIml> compilerPluginContexts;

    private CodeAnalyzerManager codeAnalyzerManager;

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
        List<Package> directDependencies = dependencyGraph.getDirectDependencies(rootPkgNode)
                .stream()
                .map(ResolvedPackageDependency::packageInstance)
                .collect(Collectors.toList());

        // TODO Iterate through the direct dependencies and filter out dependencies with compiler plugins
        // TODO Initialize the compiler plugin classes
        // TODO Create class loader

        List<CompilerPlugin> compilerPlugins = getEngagedCompilerPlugins(directDependencies);
        List<CompilerPluginContextIml> compilerPluginContexts = initializePlugins(compilerPlugins);
        return new CompilerPluginManager(compilation, compilerPluginContexts);
    }

    private static List<CompilerPluginContextIml> initializePlugins(List<CompilerPlugin> compilerPlugins) {
        List<CompilerPluginContextIml> compilerPluginContexts = new ArrayList<>(compilerPlugins.size());
        for (CompilerPlugin compilerPlugin : compilerPlugins) {
            CompilerPluginContextIml pluginContext = new CompilerPluginContextIml(compilerPlugin);
            compilerPlugin.init(pluginContext);
            compilerPluginContexts.add(pluginContext);
        }
        return compilerPluginContexts;
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

    private static List<CompilerPlugin> getEngagedCompilerPlugins(List<Package> dependencies) {
        List<CompilerPlugin> compilerPlugins = new ArrayList<>();

        // TODO write inline compiler plugins and test the logic
//        for (Package pkgDep : dependencies) {
//            pkgDep.
//        }
        return compilerPlugins;
    }
}
