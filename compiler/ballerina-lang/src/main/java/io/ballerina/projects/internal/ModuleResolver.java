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
package io.ballerina.projects.internal;

import io.ballerina.projects.DependencyResolutionType;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Find packages that contain imported modules.
 *
 * @since 2.0.0
 */
public class ModuleResolver {
    private final Map<ImportModuleRequest, ImportModuleResponse> responseMap = new HashMap<>();
    private final PackageDescriptor rootPkgDesc;
    private final Collection<ModuleName> moduleNames;
    private final BlendedManifest blendedManifest;
    private final PackageResolver packageResolver;
    private final ResolutionOptions resolutionOptions;

    public ModuleResolver(PackageDescriptor rootPkgDesc,
                          Collection<ModuleName> moduleNames,
                          BlendedManifest blendedManifest,
                          PackageResolver packageResolver,
                          ResolutionOptions resolutionOptions) {
        this.rootPkgDesc = rootPkgDesc;
        this.moduleNames = moduleNames;
        this.blendedManifest = blendedManifest;
        this.packageResolver = packageResolver;
        this.resolutionOptions = resolutionOptions;
    }

    public ImportModuleResponse getImportModuleResponse(ImportModuleRequest importModuleRequest) {
        return responseMap.get(importModuleRequest);
    }

    /**
     * Resolves the given list of module names to Packages and returns that list.
     * <p>
     * The returned package list does not contain langlib packages and the root package.
     */
    public PackageContainer<DirectPackageDependency> resolveModuleLoadRequests(
            Collection<ModuleLoadRequest> moduleLoadRequests) {
        PackageContainer<DirectPackageDependency> pkgContainer = new PackageContainer<>();
        List<ImportModuleRequest> unresolvedModuleRequests = new ArrayList<>();
        for (ModuleLoadRequest moduleLoadRequest : moduleLoadRequests) {
            resolveModuleLoadRequest(moduleLoadRequest, pkgContainer, unresolvedModuleRequests);
        }

        // Resolve unresolved import module declarations
        Collection<ImportModuleResponse> importModResponses =
                packageResolver.resolvePackageNames(unresolvedModuleRequests, resolutionOptions);
        for (ImportModuleResponse importModResp : importModResponses) {
            if (importModResp.resolutionStatus() == ResolutionResponse.ResolutionStatus.UNRESOLVED) {
                // TODO Report diagnostics
                // TODO Require a proper package.properties file.
                continue;
            }

            DirectPackageDependency newPkgDep;
            ImportModuleRequest importModuleRequest = importModResp.importModuleRequest();
            PackageDescriptor pkgDesc = importModResp.packageDescriptor();
            Optional<DirectPackageDependency> pkgDepOptional = pkgContainer.get(pkgDesc.org(), pkgDesc.name());
            ModuleLoadRequest moduleLoadRequest = importModuleRequest.moduleLoadRequest();
            if (pkgDepOptional.isEmpty()) {
                newPkgDep = new DirectPackageDependency(pkgDesc,
                        DirectPackageDependencyKind.NEW,
                        moduleLoadRequest.scope(),
                        moduleLoadRequest.dependencyResolvedType());
            } else {
                // This block is executed when there are two or more import module declarations requests
                //  for modules in the same package.
                // Say package foo contains two modules foo and foo.bar and
                //  module foo is already added to the pkgContainer,
                //  and in a new version of package foo there exists module foo.bar
                DirectPackageDependency currentPkgDep = pkgDepOptional.get();

                // Do not override the scope, if the current scope is PackageDependencyScope.DEFAULT,
                PackageDependencyScope scope =
                        currentPkgDep.scope() == PackageDependencyScope.DEFAULT ?
                                PackageDependencyScope.DEFAULT :
                                moduleLoadRequest.scope();

                // Do not override the resolutionType,
                // if the current resolutionType is DependencyResolutionType.SOURCE ,
                DependencyResolutionType resolutionType =
                        currentPkgDep.resolutionType() == DependencyResolutionType.SOURCE ?
                                DependencyResolutionType.SOURCE :
                                moduleLoadRequest.dependencyResolvedType();
                newPkgDep = new DirectPackageDependency(pkgDesc,
                        DirectPackageDependencyKind.NEW, scope, resolutionType);
            }
            pkgContainer.add(pkgDesc.org(), pkgDesc.name(), newPkgDep);
            responseMap.put(importModuleRequest, importModResp);
        }
        return pkgContainer;
    }

    private void resolveModuleLoadRequest(ModuleLoadRequest moduleLoadRequest,
                                          PackageContainer<DirectPackageDependency> pkgContainer,
                                          List<ImportModuleRequest> unresolvedModuleRequests) {
        PackageDescriptor pkgDesc;
        PackageOrg pkgOrg = getPackageOrg(moduleLoadRequest);
        String moduleName = moduleLoadRequest.moduleName();
        Collection<PackageName> possiblePkgNames = ProjectUtils.getPossiblePackageNames(
                pkgOrg, moduleLoadRequest.moduleName());
        if (ProjectUtils.isBuiltInPackage(pkgOrg, moduleName)) {
            pkgDesc = PackageDescriptor.from(pkgOrg, PackageName.from(moduleName));
        } else {
            if (possiblePkgNames.size() == 1) {
                // This is not a hierarchical module pkgName,
                // hence the package pkgName is same as the module pkgName
                PackageName pkgName = PackageName.from(moduleName);
                pkgDesc = createPkgDesc(pkgOrg, pkgName,
                        blendedManifest.dependency(pkgOrg, pkgName).orElse(null));
            } else {
                // This is a hierarchical module pkgName
                // This method returns a non-null pkgDesc if and only if that package contains the given module.
                pkgDesc = findHierarchicalModule(moduleName, pkgOrg, possiblePkgNames);
            }
        }

        if (pkgDesc == null) {
            List<PackageDescriptor> possiblePackages = new ArrayList<>();
            for (PackageName possiblePkgName : possiblePkgNames) {
                Optional<BlendedManifest.Dependency> dependency = blendedManifest.dependency(pkgOrg, possiblePkgName);
                dependency.ifPresent(value -> possiblePackages.add(createPkgDesc(pkgOrg, possiblePkgName, value)));
            }

            ImportModuleRequest importModuleRequest = new ImportModuleRequest(
                    pkgOrg, moduleLoadRequest, possiblePackages);
            unresolvedModuleRequests.add(importModuleRequest);
            return;
        }

        PackageName pkgName = pkgDesc.name();
        if (isRootPackage(pkgOrg, pkgName)) {
            // Do not add the root package to the dependencies list
            ImportModuleRequest importModuleRequest = new ImportModuleRequest(pkgOrg, moduleLoadRequest);
            responseMap.put(importModuleRequest, new ImportModuleResponse(
                    PackageDescriptor.from(pkgOrg, pkgName), importModuleRequest));
            return;
        }

        // pkgDesc.version() == null is always true
        Optional<DirectPackageDependency> pkgDepOptional = pkgContainer.get(pkgOrg, pkgName);
        if (pkgDepOptional.isEmpty()) {
            DirectPackageDependencyKind dependencyKind;
            if (blendedManifest.lockedDependency(pkgDesc.org(), pkgDesc.name()).isEmpty()) {
                // If the Dependencies.toml does not contain the package, it is a new one.
                dependencyKind = DirectPackageDependencyKind.NEW;
            } else {
                dependencyKind = DirectPackageDependencyKind.EXISTING;
            }
            pkgContainer.add(pkgOrg, pkgName, new DirectPackageDependency(pkgDesc,
                    dependencyKind, moduleLoadRequest.scope(),
                    moduleLoadRequest.dependencyResolvedType()));
        } else {
            // This block is executed when there are two or more import module declarations requests
            //  for modules in the same package.
            // Say package foo contains two modules foo and foo.bar and
            //  module foo is already added to the pkgContainer,
            //  and this block will be executed for the module foo.bar.

            // There exists a direct dependency in the container
            DirectPackageDependency currentPkgDep = pkgDepOptional.get();

            // Use the current resolutionType only if it is DependencyResolutionType.SOURCE,
            //  Override it otherwise.
            DependencyResolutionType resolutionType =
                    currentPkgDep.resolutionType() == DependencyResolutionType.SOURCE ?
                            DependencyResolutionType.SOURCE :
                            moduleLoadRequest.dependencyResolvedType();

            // Use the current scope only if it is PackageDependencyScope.DEFAULT,
            //  Override it otherwise.
            PackageDependencyScope scope =
                    currentPkgDep.scope() == PackageDependencyScope.DEFAULT ?
                            PackageDependencyScope.DEFAULT :
                            moduleLoadRequest.scope();

            // Use the current DirectPackageDependencyKind only if it is DirectPackageDependencyKind.EXISTING
            DirectPackageDependencyKind directDepKind =
                    currentPkgDep.dependencyKind() == DirectPackageDependencyKind.EXISTING ?
                            DirectPackageDependencyKind.EXISTING :
                            currentPkgDep.dependencyKind();
            pkgContainer.add(pkgOrg, pkgName,
                    new DirectPackageDependency(pkgDesc, directDepKind, scope, resolutionType));
        }

        ImportModuleRequest importModuleRequest = new ImportModuleRequest(pkgOrg, moduleLoadRequest);
        responseMap.put(importModuleRequest, new ImportModuleResponse(pkgDesc, importModuleRequest));
    }

    private PackageDescriptor findHierarchicalModule(String moduleName,
                                                     PackageOrg packageOrg,
                                                     Collection<PackageName> possiblePkgNames) {
        for (PackageName possiblePkgName : possiblePkgNames) {
            PackageDescriptor pkgDesc = findHierarchicalModule(moduleName, packageOrg, possiblePkgName);
            if (pkgDesc != null) {
                return pkgDesc;
            }
        }

        return null;
    }

    /**
     * Find the given module name in the dependencies.toml file recorded during the previous compilation.
     *
     * @param moduleName  Module name to be found
     * @param packageOrg  organization name
     * @param packageName Possible package name
     * @return PackageDescriptor or null
     */
    private PackageDescriptor findHierarchicalModule(String moduleName,
                                                     PackageOrg packageOrg,
                                                     PackageName packageName) {
        PackageDescriptor pkgDesc = findModuleInRootPackage(moduleName, packageOrg, packageName);
        if (pkgDesc != null) {
            return pkgDesc;
        }

        return findModuleInBlendedManifest(moduleName, packageOrg, packageName);
    }

    private PackageDescriptor findModuleInRootPackage(String moduleName,
                                                      PackageOrg packageOrg,
                                                      PackageName packageName) {
        if (packageOrg.equals(rootPkgDesc.org()) && packageName.equals(rootPkgDesc.name())) {
            for (ModuleName rootPkgModName : moduleNames) {
                if (rootPkgModName.toString().equals(moduleName)) {
                    return rootPkgDesc;
                }
            }
        }
        return null;
    }

    private boolean isRootPackage(PackageOrg pkgOrg, PackageName pkgName) {
        return pkgOrg.equals(rootPkgDesc.org()) &&
                pkgName.equals(rootPkgDesc.name());
    }

    private PackageDescriptor findModuleInBlendedManifest(String moduleName,
                                                          PackageOrg packageOrg,
                                                          PackageName packageName) {
        // Check whether this package is already defined in the package manifest, if so get the version
        Optional<BlendedManifest.Dependency> blendedDep = blendedManifest.dependency(packageOrg, packageName);
        if (blendedDep.isPresent() && blendedDep.get().moduleNames().contains(moduleName)) {
            return createPkgDesc(packageOrg, packageName, blendedDep.get());
        } else {
            return null;
        }
    }

    private PackageOrg getPackageOrg(ModuleLoadRequest moduleLoadRequest) {
        Optional<PackageOrg> optionalOrgName = moduleLoadRequest.orgName();
        return optionalOrgName.orElseGet(rootPkgDesc::org);
    }

    private PackageDescriptor createPkgDesc(PackageOrg packageOrg,
                                            PackageName packageName,
                                            BlendedManifest.Dependency blendedDep) {
        if (blendedDep != null && blendedDep.isFromLocalRepository()) {
            return PackageDescriptor.from(packageOrg, packageName, blendedDep.version(),
                    ProjectConstants.LOCAL_REPOSITORY_NAME);
        } else {
            return PackageDescriptor.from(packageOrg, packageName,
                    blendedDep != null ? blendedDep.version() : null);
        }
    }

    /**
     * Represents a new or existing direct package dependency.
     *
     * @since 2.0.0
     */
    public static class DirectPackageDependency {
        private final PackageDescriptor pkgDesc;
        private final DirectPackageDependencyKind dependencyKind;
        private final PackageDependencyScope scope;
        private final DependencyResolutionType resolutionType;

        public DirectPackageDependency(PackageDescriptor pkgDesc,
                                       DirectPackageDependencyKind dependencyKind,
                                       PackageDependencyScope scope,
                                       DependencyResolutionType resolutionType) {
            this.pkgDesc = pkgDesc;
            this.dependencyKind = dependencyKind;
            this.scope = scope;
            this.resolutionType = resolutionType;
        }

        public PackageDescriptor pkgDesc() {
            return pkgDesc;
        }

        public DirectPackageDependencyKind dependencyKind() {
            return dependencyKind;
        }

        public PackageDependencyScope scope() {
            return scope;
        }

        public DependencyResolutionType resolutionType() {
            return resolutionType;
        }
    }

    /**
     * This internal enum is used to indicate whether a given dependency is a new one or an exsiting one.
     *
     * @since 2.0.0
     */
    public enum DirectPackageDependencyKind {
        /**
         * A dependency already recorded in Dependencies.toml.
         */
        EXISTING,
        /**
         * A new package dependency introduced via a new import declaration.
         */
        NEW
    }
}
