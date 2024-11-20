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
import java.util.List;
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
    private final DocumentConfig balToolToml;
    private final Path packagePath;
    private final DependencyGraph<PackageDescriptor> packageDescDependencyGraph;
    private final Collection<ModuleConfig> otherModules;
    private final DocumentConfig readmeMd;
    private final boolean disableSyntaxTree;
    private final List<ResourceConfig> resources;
    private final List<ResourceConfig> testResources;

    private PackageConfig(PackageId packageId,
                          Path packagePath,
                          PackageManifest packageManifest,
                          DependencyManifest dependencyManifest,
                          DocumentConfig ballerinaToml,
                          DocumentConfig dependenciesToml,
                          DocumentConfig cloudToml,
                          DocumentConfig compilerPluginToml,
                          DocumentConfig balToolToml,
                          Collection<ModuleConfig> moduleConfigs,
                          DependencyGraph<PackageDescriptor> packageDescDependencyGraph,
                          DocumentConfig readmeMd,
                          boolean disableSyntaxTree,
                          List<ResourceConfig> resources,
                          List<ResourceConfig> testResources) {
        this.packageId = packageId;
        this.packagePath = packagePath;
        this.packageManifest = packageManifest;
        this.dependencyManifest = dependencyManifest;
        this.ballerinaToml = ballerinaToml;
        this.dependenciesToml = dependenciesToml;
        this.cloudToml = cloudToml;
        this.compilerPluginToml = compilerPluginToml;
        this.balToolToml = balToolToml;
        this.otherModules = moduleConfigs;
        this.packageDescDependencyGraph = packageDescDependencyGraph;
        this.readmeMd = readmeMd;
        this.disableSyntaxTree = disableSyntaxTree;
        this.resources = resources;
        this.testResources = testResources;
    }

    public static PackageConfig from(PackageId packageId,
                                     Path packagePath,
                                     PackageManifest packageManifest,
                                     DependencyManifest dependencyManifest,
                                     DocumentConfig ballerinaToml,
                                     DocumentConfig dependenciesToml,
                                     DocumentConfig cloudToml,
                                     DocumentConfig compilerPluginToml,
                                     DocumentConfig balToolToml,
                                     DocumentConfig packageMd,
                                     Collection<ModuleConfig> moduleConfigs,
                                     List<ResourceConfig> resources,
                                     List<ResourceConfig> testResources) {
        return new PackageConfig(packageId, packagePath, packageManifest, dependencyManifest, ballerinaToml,
                                 dependenciesToml, cloudToml, compilerPluginToml, balToolToml, moduleConfigs,
                                 DependencyGraph.emptyGraph(), packageMd, false, resources,
                                 testResources);
    }

    public static PackageConfig from(PackageId packageId,
                                     Path packagePath,
                                     PackageManifest packageManifest,
                                     DependencyManifest dependencyManifest,
                                     DocumentConfig ballerinaToml,
                                     DocumentConfig dependenciesToml,
                                     DocumentConfig cloudToml,
                                     DocumentConfig compilerPluginToml,
                                     DocumentConfig balToolToml,
                                     DocumentConfig packageMd,
                                     Collection<ModuleConfig> moduleConfigs,
                                     DependencyGraph<PackageDescriptor> packageDescDependencyGraph,
                                     List<ResourceConfig> resources,
                                     List<ResourceConfig> testResources) {
        return new PackageConfig(packageId, packagePath, packageManifest, dependencyManifest, ballerinaToml,
                                 dependenciesToml, cloudToml, compilerPluginToml, balToolToml, moduleConfigs,
                                 packageDescDependencyGraph, packageMd, false, resources,
                                 testResources);
    }

    public static PackageConfig from(PackageId packageId, Path path, PackageManifest packageManifest,
                                     DependencyManifest dependencyManifest, DocumentConfig ballerinaToml,
                                     DocumentConfig dependenciesToml, DocumentConfig cloudToml,
                                     DocumentConfig compilerPluginToml, DocumentConfig balToolToml,
                                     DocumentConfig readmeMd, List<ModuleConfig> moduleConfigs,
                                     DependencyGraph<PackageDescriptor> packageDependencyGraph,
                                     boolean disableSyntaxTree, List<ResourceConfig> resources,
                                     List<ResourceConfig> testResources) {
        return new PackageConfig(packageId, path, packageManifest, dependencyManifest, ballerinaToml,
                dependenciesToml, cloudToml, compilerPluginToml, balToolToml, moduleConfigs,
                packageDependencyGraph, readmeMd, disableSyntaxTree, resources,
                testResources);
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

    public boolean packageTemplate() {
        return packageManifest.template();
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

    public Optional<DocumentConfig> balToolToml() {
        return Optional.ofNullable(balToolToml);
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

    /**
     * Returns the PackageMd document config.
     *
     * @return DocumentConfig optionally.
     * @deprecated use {@link #readmeMd()} instead.
     */
    @Deprecated (forRemoval = true)
    public Optional<DocumentConfig> packageMd() {
        return Optional.ofNullable(readmeMd);
    }

    public Optional<DocumentConfig> readmeMd() {
        return Optional.ofNullable(readmeMd);
    }

    public Optional<DocumentConfig> dependenciesToml() {
        return Optional.ofNullable(this.dependenciesToml);
    }

    boolean isSyntaxTreeDisabled() {
        return disableSyntaxTree;
    }

    public List<ResourceConfig> resources() {
        return resources;
    }

    public List<ResourceConfig> testResources() {
        return testResources;
    }
}
