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
package io.ballerina.projects.test.resolution.packages.internal;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.internal.repositories.AbstractPackageRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Test implementation of the package repository.
 *
 * @since 2.0.0
 */
public class  DefaultPackageRepository extends AbstractPackageRepository {

    protected final PackageContainer<PackageDescWrapper> pkgContainer;
    protected final Map<PackageDescriptor, DependencyGraph<PackageDescriptor>> graphMap;
    public static final DefaultPackageRepository EMPTY_REPO = new DefaultPackageRepository(
            new PackageContainer<>(), new HashMap<>());

    public DefaultPackageRepository(PackageContainer<PackageDescWrapper> pkgContainer,
                                    Map<PackageDescriptor, DependencyGraph<PackageDescriptor>> graphMap) {
        this.pkgContainer = pkgContainer;
        this.graphMap = graphMap;
    }

    public DependencyGraph<PackageDescriptor> getDependencyGraph(PackageDescriptor pkgDesc) {
        return graphMap.get(pkgDesc);
    }

    @Override
    protected List<PackageVersion> getPackageVersions(PackageOrg org,
                                                      PackageName name,
                                                      PackageVersion version) {
        return pkgContainer.get(org, name)
                .stream()
                .map(PackageDescWrapper::pkgDesc)
                .map(PackageDescriptor::version)
                .collect(Collectors.toList());
    }

    @Override
    protected DependencyGraph<PackageDescriptor> getDependencyGraph(PackageOrg org,
                                                                    PackageName name,
                                                                    PackageVersion version) {
        return pkgContainer.get(org, name, version)
                .map(PackageDescWrapper::pkgDesc)
                .map(graphMap::get)
                .orElseThrow(() -> new IllegalStateException("Package cannot be found in dot graph files " +
                        "org: " + org + ", name: " + name + ", version: " + version));
    }

    @Override
    public Collection<ModuleDescriptor> getModules(PackageOrg org, PackageName name, PackageVersion version) {
        PackageDescWrapper pkgDescWrapper = pkgContainer.get(org, name, version)
                .orElseThrow(() -> new IllegalStateException("Package cannot be found in dot graph files " +
                        "org: " + org + ", name: " + name + ", version: " + version));
        PackageDescriptor pkgDesc = pkgDescWrapper.pkgDesc();
        return pkgDescWrapper.modules().stream()
                .map(modNameStr -> Utils.getModuleName(name, modNameStr))
                .map(moduleName -> ModuleDescriptor.from(moduleName, pkgDesc))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPackageExists(PackageOrg org, PackageName name, PackageVersion version) {
        Optional<PackageDescWrapper> packageDescWrapper = pkgContainer.get(org, name, version);
        return packageDescWrapper.isPresent();
    }

    @Override
    public Optional<Package> getPackage(ResolutionRequest request, ResolutionOptions options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<PackageVersion> getPackageVersions(ResolutionRequest request, ResolutionOptions options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, List<String>> getPackages() {
        throw new UnsupportedOperationException();
    }
}
