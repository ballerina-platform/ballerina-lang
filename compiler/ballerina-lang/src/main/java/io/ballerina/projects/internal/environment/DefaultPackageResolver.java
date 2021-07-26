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

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.environment.ResolutionResponse.ResolutionStatus;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Default Package resolver for Ballerina project.
 *
 * @since 2.0.0
 */
public class DefaultPackageResolver implements PackageResolver {
    private final PackageRepository ballerinaDistRepo;
    private final PackageRepository ballerinaCentralRepo;
    private final WritablePackageCache packageCache;
    private final Map<String, PackageRepository> customRepositories;

    public DefaultPackageResolver(PackageRepository ballerinaDistRepo,
                                  PackageRepository ballerinaCentralRepo,
                                  PackageCache packageCache) {
        this.ballerinaDistRepo = ballerinaDistRepo;
        this.ballerinaCentralRepo = ballerinaCentralRepo;
        this.packageCache = (WritablePackageCache) packageCache;
        this.customRepositories = Collections.emptyMap();
    }

    public DefaultPackageResolver(PackageRepository ballerinaDistRepo,
                                  PackageRepository ballerinaCentralRepo,
                                  PackageCache packageCache, Map<String, PackageRepository> customRepositories) {
        this.ballerinaDistRepo = ballerinaDistRepo;
        this.ballerinaCentralRepo = ballerinaCentralRepo;
        this.packageCache = (WritablePackageCache) packageCache;
        this.customRepositories = customRepositories;
    }

    @Override
    public List<ResolutionResponse> resolvePackages(List<ResolutionRequest> packageLoadRequests,
                                                    Project currentProject) {
        if (packageLoadRequests.isEmpty()) {
            return Collections.emptyList();
        }

        List<ResolutionResponse> resolutionResponses = new ArrayList<>();
        Package currentPkg = currentProject != null ? currentProject.currentPackage() : null;
        for (ResolutionRequest resolutionRequest : packageLoadRequests) {
            Package resolvedPackage = null;
            // Check whether the requested package is same as the current package
            if (currentPkg != null && resolutionRequest.packageDescriptor().equals(currentPkg.descriptor())) {
                resolvedPackage = currentPkg;
            }

            // If not try to load the package from the cache
            if (resolvedPackage == null) {
                resolvedPackage = loadFromCache(resolutionRequest);
            }

            // If not try to resolve from dist and central repositories
            if (resolvedPackage == null) {
                resolvedPackage = resolveFromRepository(resolutionRequest);
            }

            ResolutionStatus resolutionStatus;
            if (resolvedPackage == null) {
                resolutionStatus = ResolutionStatus.UNRESOLVED;
            } else {
                resolutionStatus = ResolutionStatus.RESOLVED;
                packageCache.cache(resolvedPackage);
            }
            resolutionResponses.add(ResolutionResponse.from(resolutionStatus, resolvedPackage, resolutionRequest));
        }

        return resolutionResponses;
    }

    @Override
    public Map<ImportModuleRequest, ImportModuleResponse> resolvePackageNames(
            List<ImportModuleRequest> importModuleRequests) {
        return null;
    }

    @Override
    public DependencyGraph<PackageDescriptor> resolveDependencies(List<ResolutionRequest> packageLoadRequests) {
        return null;
    }

    @Override
    public List<ResolutionResponse> resolvePackages(List<ResolutionRequest> resolutionRequests) {
        return resolvePackages(resolutionRequests, null);
    }

    private Package loadFromCache(ResolutionRequest resolutionRequest) {
        if (resolutionRequest.version().isEmpty()) {
            // We are skipping the cache look up if the version is empty. This is the get the latest version.
            return null;
        }

        Optional<Package> resolvedPackage = packageCache.getPackage(resolutionRequest.orgName(),
                resolutionRequest.packageName(), resolutionRequest.version().get());
        return resolvedPackage.orElse(null);
    }

    private Package resolveFromRepository(ResolutionRequest resolutionRequest) {
        Optional<Package> resolvedPackage;
        PackageDescriptor requestedPkgDesc = resolutionRequest.packageDescriptor();
        if (requestedPkgDesc.isLangLibPackage()) {
            return resolveLangLibPackage(resolutionRequest);
        }

        // if version is not empty
        //   Try custom repo, if specified and return null if not found
        //   Try local repos
        //       1) dist
        //       2) central --> if the version is not in local, then make a remote call
        if (requestedPkgDesc.version() != null) {
            if (resolutionRequest.repositoryName().isPresent()) {
                if (!customRepositories.containsKey(resolutionRequest.repositoryName().get())) {
                    return null;
                }
                resolvedPackage = customRepositories.get(resolutionRequest.repositoryName().get())
                        .getPackage(resolutionRequest);
                if (resolvedPackage.isEmpty()) {
                    return null;
                }
            } else {
                resolvedPackage = ballerinaDistRepo.getPackage(resolutionRequest);
                if (resolvedPackage.isEmpty()) {
                    resolvedPackage = ballerinaCentralRepo.getPackage(resolutionRequest);
                }
            }
            return resolvedPackage.orElse(null);
        }

        PackageVersion latestVersion;
        PackageRepository pkgRepoThatContainsLatestVersion;

        // Resolve from custom local repository if specified, and return null
        // We don't look up the dist and central repos if package is not found
        if (resolutionRequest.repositoryName().isPresent()) {
            pkgRepoThatContainsLatestVersion = customRepositories.get(resolutionRequest.repositoryName().get());
            List<PackageVersion> versionsInCustomRepo = pkgRepoThatContainsLatestVersion
                    .getPackageVersions(resolutionRequest);
            if (versionsInCustomRepo.isEmpty()) {
                return null;
            }
            latestVersion = findLatest(versionsInCustomRepo);
        } else {
            // Version is not present in the ResolutionRequest
            //   call both repos to get the latest version
            //   get the latest version from the correct repo
            List<PackageVersion> versionsInDistRepo = ballerinaDistRepo.getPackageVersions(resolutionRequest);
            List<PackageVersion> versionsInCentralRepo = ballerinaCentralRepo.getPackageVersions(resolutionRequest);
            if (versionsInDistRepo.isEmpty() && versionsInCentralRepo.isEmpty()) {
                return null;
            }

            PackageVersion latestVersionInDistRepo = findLatest(versionsInDistRepo);
            PackageVersion latestVersionInCentralRepo = findLatest(versionsInCentralRepo);
            if (latestVersionInDistRepo == null) {
                latestVersion = latestVersionInCentralRepo;
                pkgRepoThatContainsLatestVersion = ballerinaCentralRepo;
            } else if (latestVersionInCentralRepo == null) {
                latestVersion = latestVersionInDistRepo;
                pkgRepoThatContainsLatestVersion = ballerinaDistRepo;
            } else {
                latestVersion = getLatest(latestVersionInDistRepo, latestVersionInCentralRepo);
                pkgRepoThatContainsLatestVersion = latestVersion.equals(latestVersionInDistRepo) ?
                        ballerinaDistRepo : ballerinaCentralRepo;
            }
        }

        // Load the latest version
        ResolutionRequest newResolutionReq = ResolutionRequest.from(
                PackageDescriptor.from(resolutionRequest.orgName(), resolutionRequest.packageName(),
                        latestVersion), resolutionRequest.scope(), resolutionRequest.offline());

        // Check if the package is already in cache to avoid
        // duplicating package instances in the WritablePackageCache.
        Optional<Package> packageOptional = Optional.ofNullable(loadFromCache(newResolutionReq));
        if (packageOptional.isEmpty()) {
            packageOptional = pkgRepoThatContainsLatestVersion.getPackage(newResolutionReq);
        }
        return packageOptional.orElse(null);
    }

    private Package resolveLangLibPackage(ResolutionRequest resolutionRequest) {
        Optional<Package> resolvedPackage;

        if (resolutionRequest.version().isPresent()) {
            resolvedPackage = ballerinaDistRepo.getPackage(resolutionRequest);
        } else {
            List<PackageVersion> versionList = ballerinaDistRepo.getPackageVersions(resolutionRequest);
            if (versionList.isEmpty()) {
                resolvedPackage = Optional.empty();
            } else {
                ResolutionRequest newResolutionReq = ResolutionRequest.from(
                        PackageDescriptor.from(resolutionRequest.orgName(), resolutionRequest.packageName(),
                                versionList.get(0)), resolutionRequest.scope(), resolutionRequest.offline());
                resolvedPackage = Optional.ofNullable(loadFromCache(newResolutionReq));
                if (resolvedPackage.isEmpty()) {
                    resolvedPackage = ballerinaDistRepo.getPackage(newResolutionReq);
                }
            }
        }

        return resolvedPackage.orElse(null);
    }

    private PackageVersion findLatest(List<PackageVersion> packageVersions) {
        if (packageVersions.isEmpty()) {
            return null;
        }

        PackageVersion latestVersion = packageVersions.get(0);
        for (PackageVersion pkgVersion : packageVersions) {
            latestVersion = getLatest(latestVersion, pkgVersion);
        }
        return latestVersion;
    }

    private static PackageVersion getLatest(PackageVersion v1, PackageVersion v2) {
        SemanticVersion semVer1 = v1.value();
        SemanticVersion semVer2 = v2.value();
        boolean isV1PreReleaseVersion = semVer1.isPreReleaseVersion();
        boolean isV2PreReleaseVersion = semVer2.isPreReleaseVersion();
        if (isV1PreReleaseVersion ^ isV2PreReleaseVersion) {
            // Only one version is a pre-release version
            // Return the version which is not a pre-release version
            return isV1PreReleaseVersion ? v2 : v1;
        } else {
            // Both versions are pre-release versions or both are not pre-release versions
            // Find the the latest version
            return semVer1.greaterThanOrEqualTo(semVer2) ? v1 : v2;
        }
    }
}
