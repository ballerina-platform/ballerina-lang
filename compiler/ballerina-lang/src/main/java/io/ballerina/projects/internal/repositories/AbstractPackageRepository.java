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
package io.ballerina.projects.internal.repositories;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.ResolutionRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class provides convenient utility methods to PackageRepository implementations.
 *
 * @since 2.0.0
 */
public abstract class AbstractPackageRepository implements PackageRepository {

    @Override
    public List<PackageMetadataResponse> resolvePackageMetadata(List<ResolutionRequest> resolutionRequests) {
        List<PackageMetadataResponse> descriptorSet = new ArrayList<>();
        for (ResolutionRequest resolutionRequest : resolutionRequests) {
            List<PackageVersion> versions = getCompatiblePackageVersions(
                    resolutionRequest.packageDescriptor(), resolutionRequest.packageLockingMode());
            PackageVersion latest = findLatest(versions);
            if (latest != null) {
                descriptorSet.add(createMetadataResponse(resolutionRequest, latest));
            } else {
                descriptorSet.add(PackageMetadataResponse.createUnresolvedResponse(resolutionRequest));
            }
        }
        return descriptorSet;
    }

    protected abstract List<PackageVersion> getPackageVersions(PackageOrg org,
                                                               PackageName name,
                                                               PackageVersion version);

    protected abstract DependencyGraph<PackageDescriptor> getDependencyGraph(PackageOrg org,
                                                                             PackageName name,
                                                                             PackageVersion version);

    protected List<PackageVersion> getCompatiblePackageVersions(PackageDescriptor packageDescriptor,
                                                                 PackageLockingMode packageLockingMode) {
        List<PackageVersion> packageVersions = getPackageVersions(
                packageDescriptor.org(), packageDescriptor.name(), packageDescriptor.version());
        CompatibleRange compatibilityRange = getCompatibilityRange(packageDescriptor.version(), packageLockingMode);

        if (compatibilityRange.equals(CompatibleRange.LATEST)) {
            return packageVersions;
        }

        SemanticVersion minVersion = SemanticVersion.from(packageDescriptor.version().toString());
        if (compatibilityRange.equals(CompatibleRange.LOCK_MAJOR)) {
            return packageVersions.stream().filter(packageVersion -> {
                SemanticVersion semVerOther = SemanticVersion.from(packageVersion.toString());
                return (minVersion.major() == semVerOther.major());
            }).collect(Collectors.toList());
        }

        if (compatibilityRange.equals(CompatibleRange.LOCK_MINOR)) {
            return packageVersions.stream().filter(packageVersion -> {
                SemanticVersion semVerOther = SemanticVersion.from(packageVersion.toString());
                return (minVersion.major() == semVerOther.major() && minVersion.minor() == semVerOther.minor());
            }).collect(Collectors.toList());
        }

        if (packageVersions.contains(packageDescriptor.version())) {
            return Collections.singletonList(packageDescriptor.version());
        }

        return Collections.emptyList();
    }

    private PackageMetadataResponse createMetadataResponse(ResolutionRequest resolutionRequest,
                                                           PackageVersion latest) {
        PackageDescriptor resolvedDescriptor = PackageDescriptor.from(
                resolutionRequest.orgName(), resolutionRequest.packageName(), latest,
                resolutionRequest.repositoryName().orElse(null));
        DependencyGraph<PackageDescriptor> dependencyGraph = getDependencyGraph(resolutionRequest.orgName(),
                resolutionRequest.packageName(), latest);
        return PackageMetadataResponse.from(resolutionRequest, resolvedDescriptor, dependencyGraph);
    }

    protected PackageVersion findLatest(List<PackageVersion> packageVersions) {
        if (packageVersions.isEmpty()) {
            return null;
        }

        PackageVersion latestVersion = packageVersions.get(0);
        for (PackageVersion pkgVersion : packageVersions) {
            latestVersion = getLatest(latestVersion, pkgVersion);
        }
        return latestVersion;
    }

    protected PackageVersion getLatest(PackageVersion v1, PackageVersion v2) {
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

    private CompatibleRange getCompatibilityRange(PackageVersion minVersion,
                                                  PackageLockingMode packageLockingMode) {
        if (minVersion != null) {
            SemanticVersion semVer = SemanticVersion.from(minVersion.toString());
            if (semVer.isInitialVersion()) {
                if (packageLockingMode.equals(PackageLockingMode.HARD)) {
                    return CompatibleRange.EXACT;
                }
                return CompatibleRange.LOCK_MINOR;
            }
            if (packageLockingMode.equals(PackageLockingMode.HARD)) {
                return CompatibleRange.EXACT;
            }
            if (packageLockingMode.equals(PackageLockingMode.MEDIUM)) {
                return CompatibleRange.LOCK_MINOR;
            }
            if (packageLockingMode.equals(PackageLockingMode.SOFT)) {
                return CompatibleRange.LOCK_MAJOR;
            }
        }
        return CompatibleRange.LATEST;
    }

    private enum CompatibleRange {
        /**
         * Latest stable (if any), else latest pre-release.
         */
        LATEST,
        /**
         * Latest minor version of the locked major version.
         */
        LOCK_MAJOR,
        /**
         * Latest patch version of the locked major and minor versions.
         */
        LOCK_MINOR,
        /**
         * Exact version provided.
         */
        EXACT
    }
}
