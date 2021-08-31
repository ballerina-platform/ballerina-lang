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
import java.util.Collection;
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
        // We will only receive hierarchical imports in importModuleRequests
        List<ImportModuleResponse> responseListInDist = distributionRepo.resolvePackageNames(importModuleRequests);
        List<ImportModuleResponse> responseListInCentral =
                centralRepo.resolvePackageNames(importModuleRequests);

        return new ArrayList<>(
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
    public Collection<ResolutionResponse> resolvePackages(Collection<ResolutionRequest> resolutionRequests,
                                                          boolean offline) {
        if (resolutionRequests.isEmpty()) {
            return Collections.emptyList();
        }

        return resolutionRequests.stream()
                .map(request -> resolvePackage(request, offline))
                .collect(Collectors.toList());
    }

    private ResolutionResponse resolvePackage(ResolutionRequest resolutionReq, boolean offline) {
        // 1) Load the package from the cache
        Optional<Package> resolvedPackage = loadFromCache(resolutionReq);
        if (resolvedPackage.isEmpty()) {
            // 2) If not try to resolve from local, dist and central repositories
            resolvedPackage = resolveFromRepository(resolutionReq, offline);
            resolvedPackage.ifPresent(packageCache::cache);
        }

        ResolutionStatus resolutionStatus = resolvedPackage.isPresent() ?
                ResolutionStatus.RESOLVED :
                ResolutionStatus.UNRESOLVED;
        return ResolutionResponse.from(resolutionStatus, resolvedPackage.orElse(null), resolutionReq);
    }

    private Optional<Package> loadFromCache(ResolutionRequest resolutionReq) {
        PackageDescriptor pkgDesc = resolutionReq.packageDescriptor();
        return packageCache.getPackage(pkgDesc.org(), pkgDesc.name(), pkgDesc.version());
    }

    private Optional<Package> resolveFromRepository(ResolutionRequest resolutionReq, boolean offline) {
        PackageDescriptor pkgDesc = resolutionReq.packageDescriptor();

        // 1) Try to load from the distribution repo, if the requested package is a built-in package.
        if (pkgDesc.isBuiltInPackage()) {
            return distributionRepo.getPackage(resolutionReq);
        }

        // 2) Try to load from the local repo, if it is requested from the local repo.
        if (pkgDesc.repository().isPresent()) {
            String repository = pkgDesc.repository().get();
            if (!ProjectConstants.LOCAL_REPOSITORY_NAME.equals(repository)) {
                return Optional.empty();
            }
            return localRepo.getPackage(resolutionReq);
        }

        // 3) Try to load from the dist repo
        // TODO update this route only ballerina/* and Ballerinax/* stuff to dist repo
        Optional<Package> resolvedPackage = distributionRepo.getPackage(resolutionReq);

        // 4) Load from the central repo as the last attempt
        if (resolvedPackage.isEmpty()) {
            resolvedPackage = centralRepo.getPackage(resolutionReq);
        }

        return resolvedPackage;
    }
}
