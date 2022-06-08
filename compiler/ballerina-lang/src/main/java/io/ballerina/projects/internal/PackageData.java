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
package io.ballerina.projects.internal;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.PackageDescriptor;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * {@code PackageFileData} represents a Ballerina package directory.
 *
 * @since 2.0.0
 */
public class PackageData {
    private final Path packagePath;
    // Ballerina toml file config
    private final ModuleData defaultModule;
    private final List<ModuleData> otherModules;
    private final DependencyGraph<PackageDescriptor> packageDesDependencyGraph;
    private final DependencyGraph<ModuleDescriptor> moduleDependencyGraph;
    private final DocumentData ballerinaToml;
    private final DocumentData dependenciesToml;
    private final DocumentData cloudToml;
    private final DocumentData compilerPluginToml;
    private final DocumentData packageMd;


    private PackageData(Path packagePath,
                        ModuleData defaultModule,
                        List<ModuleData> otherModules,
                        DependencyGraph<PackageDescriptor> packageDesDependencyGraph,
                        DependencyGraph<ModuleDescriptor> moduleDependencyGraph,
                        DocumentData ballerinaToml,
                        DocumentData dependenciesToml,
                        DocumentData cloudToml,
                        DocumentData compilerPluginToml,
                        DocumentData packageMd) {
        this.packagePath = packagePath;
        this.defaultModule = defaultModule;
        this.otherModules = otherModules;
        this.packageDesDependencyGraph = packageDesDependencyGraph;
        this.moduleDependencyGraph = moduleDependencyGraph;
        this.packageMd = packageMd;
        this.ballerinaToml = ballerinaToml;
        this.dependenciesToml = dependenciesToml;
        this.cloudToml = cloudToml;
        this.compilerPluginToml = compilerPluginToml;
    }

    public static PackageData from(Path packagePath,
                                   ModuleData defaultModule,
                                   List<ModuleData> otherModules,
                                   DocumentData ballerinaToml,
                                   DocumentData dependenciesToml,
                                   DocumentData cloudToml,
                                   DocumentData compilerPluginToml,
                                   DocumentData packageMd) {
        return new PackageData(packagePath, defaultModule, otherModules, DependencyGraph.emptyGraph(),
                               DependencyGraph.emptyGraph(), ballerinaToml, dependenciesToml, cloudToml,
                               compilerPluginToml, packageMd);
    }

    public static PackageData from(Path packagePath,
                                   ModuleData defaultModule,
                                   List<ModuleData> otherModules,
                                   DependencyGraph<PackageDescriptor> packageDesDependencyGraph,
                                   DependencyGraph<ModuleDescriptor> moduleDependencyGraph,
                                   DocumentData ballerinaToml,
                                   DocumentData dependenciesToml,
                                   DocumentData cloudToml,
                                   DocumentData compilerPluginToml,
                                   DocumentData packageMd) {
        return new PackageData(packagePath, defaultModule, otherModules, packageDesDependencyGraph,
                moduleDependencyGraph, ballerinaToml, dependenciesToml, cloudToml, compilerPluginToml, packageMd);
    }

    public Path packagePath() {
        return packagePath;
    }

    public ModuleData defaultModule() {
        return defaultModule;
    }

    public List<ModuleData> otherModules() {
        return otherModules;
    }

    public DependencyGraph<PackageDescriptor> packageDescriptorDependencyGraph() {
        return packageDesDependencyGraph;
    }

    public DependencyGraph<ModuleDescriptor> moduleDependencyGraph() {
        return moduleDependencyGraph;
    }

    public Optional<DocumentData> ballerinaToml() {
        return Optional.ofNullable(ballerinaToml);
    }

    public Optional<DocumentData> dependenciesToml() {
        return Optional.ofNullable(dependenciesToml);
    }

    public Optional<DocumentData> cloudToml() {
        return Optional.ofNullable(cloudToml);
    }

    public Optional<DocumentData> compilerPluginToml() {
        return Optional.ofNullable(compilerPluginToml);
    }

    public Optional<DocumentData> packageMd() {
        return Optional.ofNullable(packageMd);
    }
}
