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
package io.ballerina.projects.internal.repositories;

import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.bala.BalaProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponseDescriptor;
import io.ballerina.projects.repos.FileSystemCache;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Package Repository stored in file system.
 * The structure of the repository is as bellow
 * - bala
 *     - org
 *         - package-name
 *             - version
 *                 - platform (contains extracted bala)
 *
 * - cache[-<distShortVersion>]
 *     - org
 *         - package-name
 *             - version
 *                 - bir
 *                     - mod1.bir
 *                     - mod2.bir
 *                 - jar
 *                     - org-package-name-version.jar
 *
 * @since 2.0.0
 */
public class FileSystemRepository implements PackageRepository {
    Path bala;
    private final Path cacheDir;
    private final Environment environment;

    // TODO Refactor this when we do repository/cache split
    public FileSystemRepository(Environment environment, Path cacheDirectory) {
        this.cacheDir = cacheDirectory.resolve(ProjectConstants.CACHES_DIR_NAME);
        this.bala = cacheDirectory.resolve(ProjectConstants.REPO_BALA_DIR_NAME);
        this.environment = environment;
    }

    public FileSystemRepository(Environment environment, Path cacheDirectory, String distributionVersion) {
        this.cacheDir = cacheDirectory.resolve(ProjectConstants.CACHES_DIR_NAME + "-" + distributionVersion);
        this.bala = cacheDirectory.resolve(ProjectConstants.REPO_BALA_DIR_NAME);
        this.environment = environment;
    }

    @Override
    public Optional<Package> getPackage(ResolutionRequest resolutionRequest) {
        // if version and org name is empty we add empty string so we return empty package anyway
        String packageName = resolutionRequest.packageName().value();
        String orgName = resolutionRequest.orgName().value();
        String version = resolutionRequest.version().isPresent() ?
                resolutionRequest.version().get().toString() : "0.0.0";

        //First we will check for a bala that match any platform
        Path balaPath = this.bala.resolve(
                ProjectUtils.getRelativeBalaPath(orgName, packageName, version, null));
        if (!Files.exists(balaPath)) {
            //If bala for any platform not exist check for specific platform
            balaPath = this.bala.resolve(
                    ProjectUtils.getRelativeBalaPath(orgName, packageName, version, JvmTarget.JAVA_11.code()));
            if (!Files.exists(balaPath)) {
                return Optional.empty();
            }
        }

        ProjectEnvironmentBuilder environmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
        environmentBuilder = environmentBuilder.addCompilationCacheFactory(
                new FileSystemCache.FileSystemCacheFactory(cacheDir));
        Project project = BalaProject.loadProject(environmentBuilder, balaPath);
        return Optional.of(project.currentPackage());
    }

    @Override
    public List<PackageVersion> getPackageVersions(ResolutionRequest resolutionRequest) {
        // if version and org name is empty we add empty string so we return empty package anyway
        String packageName = resolutionRequest.packageName().value();
        String orgName = resolutionRequest.orgName().value();
        List<Path> versions = new ArrayList<>();
        try {
            Path balaPackagePath = bala.resolve(orgName).resolve(packageName);
            if (Files.exists(balaPackagePath)) {
                try (Stream<Path> collect = Files.list(balaPackagePath)) {
                    versions.addAll(collect.collect(Collectors.toList()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while accessing Distribution cache: " + e.getMessage());
        }

        return pathToVersions(versions);
    }

    /**
     * Get the list of packages in the bala cache.
     *
     * @return {@link List} of package names
     */
    public Map<String, List<String>> getPackages() {
        Map<String, List<String>> packagesMap = new HashMap<>();
        File[] orgDirs = this.bala.toFile().listFiles();
        if (orgDirs == null) {
            return packagesMap;
        }
        for (File file : orgDirs) {
            if (!file.isDirectory()) {
                continue;
            }
            String orgName = file.getName();
            File[] filesList = this.bala.resolve(orgName).toFile().listFiles();
            if (filesList == null) {
                return packagesMap;
            }
            List<String> pkgList = new ArrayList<>();
            for (File pkgDir : filesList) {
                if (!pkgDir.isDirectory() || pkgDir.isHidden()) {
                    continue;
                }
                File[] pkgs = this.bala.resolve(orgName).resolve(pkgDir.getName()).toFile().listFiles();
                if (pkgs == null) {
                    continue;
                }
                String version = "";
                for (File listFile : pkgs) {
                    if (listFile.isHidden() || !listFile.isDirectory()) {
                        continue;
                    }
                    version = listFile.getName();
                    break;
                }
                pkgList.add(pkgDir.getName() + ":" + version);
            }
            packagesMap.put(orgName, pkgList);
        }

        return packagesMap;
    }

    @Override
    public List<ResolutionResponseDescriptor> resolveDependencyVersions(
            List<ResolutionRequest> packageLoadRequests, PackageLockingMode packageLockingMode) {
        List<ResolutionResponseDescriptor> descriptorSet = new ArrayList<>();
        for (ResolutionRequest resolutionRequest : packageLoadRequests) {
            List<PackageVersion> versions = getPackageVersions(resolutionRequest);
            if (!versions.isEmpty()) {
                PackageVersion latest = findLatest(resolutionRequest, versions, packageLockingMode);
                if (latest != null) {
                    PackageDescriptor resolvedDescriptor = PackageDescriptor
                            .from(resolutionRequest.orgName(), resolutionRequest.packageName(), latest);
                    ResolutionRequest newResolutionRequest = ResolutionRequest
                            .from(resolvedDescriptor, resolutionRequest.scope(), resolutionRequest.offline());
                    Package resolvedPackage = getPackage(newResolutionRequest).orElseThrow();
                    ResolutionResponseDescriptor responseDescriptor = ResolutionResponseDescriptor
                            .from(resolutionRequest, resolvedDescriptor, resolvedPackage.descriptorDependencyGraph());
                    descriptorSet.add(responseDescriptor);
                    continue;
                }
            }
            descriptorSet.add(ResolutionResponseDescriptor.from(resolutionRequest));
        }

        return descriptorSet;
    }

    private List<PackageVersion> pathToVersions(List<Path> versions) {
        List<PackageVersion> availableVersions = new ArrayList<>();
        versions.stream().map(path -> Optional.ofNullable(path)
                .map(Path::getFileName)
                .map(Path::toString)
                .orElse("0.0.0")).forEach(version -> {
                    try {
                        availableVersions.add(PackageVersion.from(version));
                    } catch (ProjectException ignored) {
                        // We consider only the semver compatible versions as valid
                        // bala directories. Since we only allow building and pushing
                        // semver compatible packages, it is safe to pick only
                        // the semver compatible versions.
                    }
        });
        return availableVersions;
    }

    private PackageVersion findLatest(ResolutionRequest resolutionRequest, List<PackageVersion> packageVersions,
                                      PackageLockingMode packageLockingMode) {
        if (resolutionRequest.version().isEmpty()) {
            return findLatest(packageVersions);
        }
        if (packageLockingMode.equals(PackageLockingMode.SOFT)) {
            return findLatest(packageVersions);
        }
        if (packageLockingMode.equals(PackageLockingMode.MEDIUM)) {
            SemanticVersion semVer = SemanticVersion.from(resolutionRequest.version().get().toString());
            List<PackageVersion> filteredPackageVersions = packageVersions.stream().filter(packageVersion -> {
                SemanticVersion semVerOther = SemanticVersion.from(packageVersion.toString());
                return (semVer.major() == semVerOther.major()
                        && semVer.minor() == semVerOther.minor()
                        && !semVerOther.lessThan(semVer));
            }).collect(Collectors.toList());
            return findLatest(filteredPackageVersions);
        }
        if (packageLockingMode.equals(PackageLockingMode.HARD)) {
            if (packageVersions.contains(resolutionRequest.packageDescriptor().version())) {
                return resolutionRequest.packageDescriptor().version();
            }
        }
        return null;
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
