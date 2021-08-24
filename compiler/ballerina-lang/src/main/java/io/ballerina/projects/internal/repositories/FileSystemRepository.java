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

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.bala.BalaProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import io.ballerina.projects.repos.FileSystemCache;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
public class FileSystemRepository extends AbstractPackageRepository {
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

        Path balaPath = getPackagePath(orgName, packageName, version);
        if (!Files.exists(balaPath)) {
            return Optional.empty();
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
        return getPackageVersions(resolutionRequest.orgName(), resolutionRequest.packageName(),
                resolutionRequest.version().orElse(null));
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
    public List<ImportModuleResponse> resolvePackageNames(List<ImportModuleRequest> importModuleRequests) {
        List<ImportModuleResponse> importModuleResponseList = new ArrayList<>();
        for (ImportModuleRequest importModuleRequest : importModuleRequests) {
            ImportModuleResponse importModuleLoadResponse = getImportModuleLoadResponse(importModuleRequest);
            importModuleResponseList.add(importModuleLoadResponse);
        }
        return importModuleResponseList;
    }

    private ImportModuleResponse getImportModuleLoadResponse(ImportModuleRequest importModuleRequest) {
        // Check if the imported module is available in a possible package locked in the Dependencies.toml
        for (PackageDescriptor possiblePackage : importModuleRequest.possiblePackages()) {
            List<PackageVersion> packageVersions = getCompatiblePackageVersions(
                    possiblePackage, PackageLockingMode.SOFT);
            ImportModuleResponse importModuleResponse = getImportModuleResponse(
                    importModuleRequest, possiblePackage.name(), packageVersions);
            if (importModuleResponse != null) {
                return importModuleResponse;
            }
        }

        // If the module is not found in the possible packages locked in the Dependencies.toml
        // we continue looking for the module in the remaining possible packages.
        List<PackageName> existing = importModuleRequest.possiblePackages().stream().map(PackageDescriptor::name)
                .collect(Collectors.toList());
        List<PackageName> remainingPackageNames = ProjectUtils.getPossiblePackageNames(
                importModuleRequest.packageOrg(), importModuleRequest.moduleName()).stream()
                .filter(o -> !existing.contains(o)).collect(Collectors.toList());

        for (PackageName possiblePackageName : remainingPackageNames) {
            List<PackageVersion> packageVersions = getPackageVersions(importModuleRequest.packageOrg(),
                    possiblePackageName, null);

            ImportModuleResponse importModuleResponse = getImportModuleResponse(
                    importModuleRequest, possiblePackageName, packageVersions);
            if (importModuleResponse != null) {
                return importModuleResponse;
            }
        }
        return new ImportModuleResponse(importModuleRequest);
    }

    private ImportModuleResponse getImportModuleResponse(ImportModuleRequest importModuleRequest,
                                                         PackageName packageName,
                                                         List<PackageVersion> packageVersions) {
        Comparator<PackageVersion> comparator = (v1, v2) -> {

            PackageVersion latest = getLatest(v1, v2);
            if (v1 == latest) {
                return -1;
            }
            return 1;
        };
        packageVersions.sort(comparator);

        for (PackageVersion packageVersion : packageVersions) {
            Path balaPath = getPackagePath(importModuleRequest.packageOrg().toString(),
                    packageName.toString(), packageVersion.toString());
            BalaFiles.DependencyGraphResult packageDependencyGraph =
                    BalaFiles.createPackageDependencyGraph(balaPath);
            Set<ModuleDescriptor> moduleDescriptors = packageDependencyGraph.moduleDependencies().keySet();
            for (ModuleDescriptor moduleDescriptor : moduleDescriptors) {
                if (importModuleRequest.moduleName().equals(moduleDescriptor.name().toString())) {
                    PackageDescriptor packageDescriptor = PackageDescriptor
                            .from(importModuleRequest.packageOrg(), packageName, packageVersion);
                    return new ImportModuleResponse(packageDescriptor, importModuleRequest);
                }
            }
        }
        return null;
    }

    protected List<PackageVersion> getPackageVersions(PackageOrg org, PackageName name, PackageVersion version) {
        List<Path> versions = new ArrayList<>();
        try {
            Path balaPackagePath = bala.resolve(org.value()).resolve(name.value());
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

    @Override
    protected DependencyGraph<PackageDescriptor> getDependencyGraph(PackageOrg org,
                                                                    PackageName name,
                                                                    PackageVersion version) {
        Path balaPath = getPackagePath(org.toString(), name.toString(), version.toString());
        BalaFiles.DependencyGraphResult dependencyGraphResult = BalaFiles.createPackageDependencyGraph(balaPath);
        return dependencyGraphResult.packageDependencyGraph();
    }

    protected Path getPackagePath(String org, String name, String version) {
        //First we will check for a bala that match any platform
        Path balaPath = this.bala.resolve(
                ProjectUtils.getRelativeBalaPath(org, name, version, null));
        if (!Files.exists(balaPath)) {
            // If bala for any platform not exist check for specific platform
            balaPath = this.bala.resolve(
                    ProjectUtils.getRelativeBalaPath(org, name, version, JvmTarget.JAVA_11.code()));
        }
        return balaPath;
    }

    protected List<PackageVersion> pathToVersions(List<Path> versions) {
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
}
