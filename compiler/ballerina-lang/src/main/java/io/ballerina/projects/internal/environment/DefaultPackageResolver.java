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

package io.ballerina.projects.internal.environment;

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.environment.ResolutionResponse.ResolutionStatus;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default Package resolver for Ballerina project.
 *
 * @since 2.0.0
 */
public class DefaultPackageResolver implements PackageResolver {
    private final PackageRepository distributionRepo;
    private final PackageRepository centralRepo;
    private final PackageRepository localRepo;
    private final WritablePackageCache packageCache;

    public DefaultPackageResolver(PackageRepository distributionRepo,
                                  PackageRepository centralRepo,
                                  PackageRepository localRepo,
                                  PackageCache packageCache) {
        this.distributionRepo = distributionRepo;
        this.centralRepo = centralRepo;
        this.localRepo = localRepo;
        this.packageCache = (WritablePackageCache) packageCache;
    }

    @Override
    public List<ImportModuleResponse> resolvePackageNames(List<ImportModuleRequest> importModuleRequests) {
        // TODO Update this logic to lookup packages in the local repo.

        // We will only receive hierarchical imports in importModuleRequests
        List<ImportModuleResponse> responseListInDist = distributionRepo.resolvePackageNames(importModuleRequests);
        List<ImportModuleResponse> responseListInCentral =
                centralRepo.resolvePackageNames(importModuleRequests);

        List<ImportModuleResponse> responseList = new ArrayList<>(
                Stream.of(responseListInDist, responseListInCentral).flatMap(List::stream).collect(Collectors.toMap(
                        ImportModuleResponse::importModuleRequest, Function.identity(),
                        (ImportModuleResponse x, ImportModuleResponse y) -> {
                            if (y.resolutionStatus().equals(ResolutionStatus.UNRESOLVED)) {
                                return x;
                            }
                            if (x.resolutionStatus().equals(ResolutionStatus.UNRESOLVED)) {
                                return y;
                            }
                            if (!x.packageDescriptor().name().equals(y.packageDescriptor().name())) {
                                ResolutionRequest resolutionRequest = ResolutionRequest
                                        .from(y.packageDescriptor(), PackageDependencyScope.DEFAULT, true);
                                List<PackageVersion> packageVersions =
                                        distributionRepo.getPackageVersions(resolutionRequest);
                                // If module exists in both repos, then we check if a newer version of
                                // y (package in central) in dist repo.
                                // If yes, we assume that the latest version of y does not contain the
                                // module. Hence, return x.
                                // Else, there is no newer package of y in dist. We assume that there exist a newer
                                // version of x in central which does not have this module. Hence, return y.
                                if (packageVersions.isEmpty()) {
                                    return y;
                                }
                            }
                            return x;
                        })).values());
        return responseList;
    }

    @Override
    public List<PackageMetadataResponse> resolvePackageMetadata(List<ResolutionRequest> resolutionRequests) {
        List<ResolutionRequest> localRepoPkgLoadRequest = new ArrayList<>();
        for (ResolutionRequest pkgLoadRequest : resolutionRequests) {
            Optional<String> repository = pkgLoadRequest.packageDescriptor().repository();
            if (repository.isPresent() && repository.get().equals(ProjectConstants.LOCAL_REPOSITORY_NAME)) {
                localRepoPkgLoadRequest.add(pkgLoadRequest);
            }
        }

        List<PackageMetadataResponse> responseFrmLocalRepo;
        if (!localRepoPkgLoadRequest.isEmpty()) {
            responseFrmLocalRepo = localRepo.resolvePackageMetadata(localRepoPkgLoadRequest);
        } else {
            responseFrmLocalRepo = Collections.emptyList();
        }

        // TODO Send ballerina* org names to dist repo
        List<PackageMetadataResponse> latestVersionsInDist = distributionRepo
                .resolvePackageMetadata(resolutionRequests);


        // Send non built in packages to central
        List<ResolutionRequest> centralLoadRequests = resolutionRequests.stream()
                .filter(r -> !ProjectUtils.isBuiltInPackage(r.orgName(), r.packageName().value()))
                .collect(Collectors.toList());
        List<PackageMetadataResponse> latestVersionsInCentral = centralRepo
                .resolvePackageMetadata(centralLoadRequests);

        // TODO Local package should get priority over the same version in central or dist repo
        // TODO Unit test following merge
        List<PackageMetadataResponse> responseDescriptors = new ArrayList<>(
                Stream.of(latestVersionsInDist, latestVersionsInCentral, responseFrmLocalRepo)
                        .flatMap(List::stream).collect(Collectors.toMap(
                        PackageMetadataResponse::packageLoadRequest, Function.identity(),
                        (PackageMetadataResponse x, PackageMetadataResponse y) -> {
                            if (y.resolutionStatus().equals(ResolutionStatus.UNRESOLVED)) {
                                return x;
                            }
                            if (x.resolutionStatus().equals(ResolutionStatus.UNRESOLVED)) {
                                return y;
                            }
                            if (x.resolvedDescriptor().version().compareTo(
                                    y.resolvedDescriptor().version()).equals(
                                    SemanticVersion.VersionCompatibilityResult.LESS_THAN)) {
                                return y;
                            }
                            return x;
                        })).values());

        return responseDescriptors;
    }

    @Override
    public List<ResolutionResponse> resolvePackages(List<PackageDescriptor> packageDescriptors,
                                                    boolean offline,
                                                    Project currentProject) {
        if (packageDescriptors.isEmpty()) {
            return Collections.emptyList();
        }

        List<ResolutionResponse> resolutionResponses = new ArrayList<>();
        Package currentPkg = currentProject != null ? currentProject.currentPackage() : null;
        for (PackageDescriptor packageDescriptor : packageDescriptors) {
            Package resolvedPackage = null;
            // Check whether the requested package is same as the current package
            if (currentPkg != null && packageDescriptor.equals(currentPkg.descriptor())) {
                resolvedPackage = currentPkg;
            }

            // If not try to load the package from the cache
            if (resolvedPackage == null) {
                resolvedPackage = loadFromCache(packageDescriptor);
            }

            // If not try to resolve from dist and central repositories
            if (resolvedPackage == null) {
                resolvedPackage = resolveFromRepository(packageDescriptor, offline);
            }

            ResolutionResponse.ResolutionStatus resolutionStatus;
            if (resolvedPackage == null) {
                resolutionStatus = ResolutionResponse.ResolutionStatus.UNRESOLVED;
            } else {
                resolutionStatus = ResolutionResponse.ResolutionStatus.RESOLVED;
                packageCache.cache(resolvedPackage);
            }
            resolutionResponses.add(ResolutionResponse.from(resolutionStatus, resolvedPackage, packageDescriptor));
        }

        return resolutionResponses;
    }

    @Override
    public List<ResolutionResponse> resolvePackages(List<PackageDescriptor> packageDescriptors, boolean offline) {
        return resolvePackages(packageDescriptors, false, null);
    }

    private Package loadFromCache(PackageDescriptor packageDescriptor) {
        Optional<Package> resolvedPackage = packageCache.getPackage(packageDescriptor.org(),
                packageDescriptor.name(), packageDescriptor.version());
        return resolvedPackage.orElse(null);
    }

    private Package resolveFromRepository(PackageDescriptor requestedPkgDesc, boolean offline) {
        Optional<Package> resolvedPackage;

        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                requestedPkgDesc, PackageDependencyScope.DEFAULT, offline);

        if (requestedPkgDesc.isLangLibPackage()) {
            return distributionRepo.getPackage(resolutionRequest).orElse(null);
        }

        Optional<String> repositoryOptional = requestedPkgDesc.repository();
        if (repositoryOptional.isPresent()) {
            String repository = repositoryOptional.get();
            if (!ProjectConstants.LOCAL_REPOSITORY_NAME.equals(repository)) {
                return null;
            }
            resolvedPackage = localRepo.getPackage(resolutionRequest);
            if (resolvedPackage.isEmpty()) {
                return null;
            }
        } else {
            resolvedPackage = distributionRepo.getPackage(resolutionRequest);
            if (resolvedPackage.isEmpty()) {
                resolvedPackage = centralRepo.getPackage(resolutionRequest);
            }
        }
        return resolvedPackage.orElse(null);
    }
}
