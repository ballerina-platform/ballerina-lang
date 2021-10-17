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

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

/**
 * {@code PackageConfig} contains necessary configuration elements required to
 * create an instance of a {@code Package}.
 *
 * @since 2.0.0
 */
public class PackageConfig {
    // TODO this class represents the Ballerina.toml file

    // This class should contain Specific project-agnostic information
    private final PackageId packageId;
    private final PackageManifest packageManifest;
    private final DependencyManifest dependencyManifest;
    private final DocumentConfig ballerinaToml;
    private final DocumentConfig dependenciesToml;
    private final DocumentConfig cloudToml;
    private final DocumentConfig compilerPluginToml;
    private final Path packagePath;
    private final DependencyGraph<PackageDescriptor> packageDescDependencyGraph;
    private final Collection<ModuleConfig> otherModules;
    private final DocumentConfig packageMd;

    private PackageConfig(PackageId packageId,
                          Path packagePath,
                          PackageManifest packageManifest,
                          DependencyManifest dependencyManifest,
                          DocumentConfig ballerinaToml,
                          DocumentConfig dependenciesToml,
                          DocumentConfig cloudToml,
                          DocumentConfig compilerPluginToml,
                          Collection<ModuleConfig> moduleConfigs,
                          DependencyGraph<PackageDescriptor> packageDescDependencyGraph,
                          DocumentConfig packageMd) {
        this.packageId = packageId;
        this.packagePath = packagePath;
        this.packageManifest = packageManifest;
        this.dependencyManifest = dependencyManifest;
        this.ballerinaToml = ballerinaToml;
        this.dependenciesToml = dependenciesToml;
        this.cloudToml = cloudToml;
        this.compilerPluginToml = compilerPluginToml;
        this.otherModules = moduleConfigs;
        this.packageDescDependencyGraph = packageDescDependencyGraph;
        this.packageMd = packageMd;
    }

    public static PackageConfig from(PackageId packageId,
                                     Path packagePath,
                                     PackageManifest packageManifest,
                                     DependencyManifest dependencyManifest,
                                     DocumentConfig ballerinaToml,
                                     DocumentConfig dependenciesToml,
                                     DocumentConfig cloudToml,
                                     DocumentConfig compilerPluginToml,
                                     DocumentConfig packageMd,
                                     Collection<ModuleConfig> moduleConfigs) {
        return new PackageConfig(packageId, packagePath, packageManifest, dependencyManifest, ballerinaToml,
                                 dependenciesToml, cloudToml, compilerPluginToml, moduleConfigs,
                                 DependencyGraph.emptyGraph(), packageMd);
    }

    public static PackageConfig from(PackageId packageId,
                                     Path packagePath,
                                     PackageManifest packageManifest,
                                     DependencyManifest dependencyManifest,
                                     DocumentConfig ballerinaToml,
                                     DocumentConfig dependenciesToml,
                                     DocumentConfig cloudToml,
                                     DocumentConfig compilerPluginToml,
                                     DocumentConfig packageMd,
                                     Collection<ModuleConfig> moduleConfigs,
                                     DependencyGraph<PackageDescriptor> packageDescDependencyGraph) {
        return new PackageConfig(packageId, packagePath, packageManifest, dependencyManifest, ballerinaToml,
                                 dependenciesToml, cloudToml, compilerPluginToml, moduleConfigs,
                                 packageDescDependencyGraph, packageMd);
    }

    public PackageId packageId() {
        return packageId;
    }

    public PackageName packageName() {
        return packageManifest.name();
    }

    public PackageOrg packageOrg() {
        return packageManifest.org();
    }

    public PackageVersion packageVersion() {
        return packageManifest.version();
    }

    public PackageManifest packageManifest() {
        return packageManifest;
    }

    public DependencyManifest dependencyManifest() {
        return dependencyManifest;
    }

    public Optional<DocumentConfig> ballerinaToml() {
        return Optional.ofNullable(ballerinaToml);
    }

    public Optional<DocumentConfig> cloudToml() {
        return Optional.ofNullable(cloudToml);
    }

    public Optional<DocumentConfig> compilerPluginToml() {
        return Optional.ofNullable(compilerPluginToml);
    }

    public CompilationOptions compilationOptions() {
        return null;
    }

    public DependencyGraph<PackageDescriptor> packageDescDependencyGraph() {
        return packageDescDependencyGraph;
    }

    // TODO Check whether it makes sense to expose Java Path in the API
    // TODO Should I use a String here
    public Path packagePath() {
        return packagePath;
    }

    public Collection<ModuleConfig> otherModules() {
        return otherModules;
    }

    public Optional<DocumentConfig> packageMd() {
        return Optional.ofNullable(packageMd);
    }

    public Optional<DocumentConfig> dependenciesToml() {
        return Optional.ofNullable(this.dependenciesToml);
    }
}
