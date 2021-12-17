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

import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;
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
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.repos.FileSystemCache;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
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
    public Optional<Package> getPackage(ResolutionRequest request, ResolutionOptions options) {
        // if version and org name is empty we add empty string so we return empty package anyway
        String packageName = request.packageName().value();
        String orgName = request.orgName().value();
        String version = request.version().isPresent() ?
                request.version().get().toString() : "0.0.0";

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
    public boolean isPackageExists(PackageOrg org,
                                   PackageName name,
                                   PackageVersion version) {
        Path balaPath = getPackagePath(org.value(), name.value(), version.value().toString());
        return Files.exists(balaPath);
    }

    @Override
    public Collection<PackageVersion> getPackageVersions(ResolutionRequest request, ResolutionOptions options) {
        // if version and org name is empty we add empty string so we return empty package anyway
        return getPackageVersions(request.orgName(), request.packageName(),
                request.version().orElse(null));
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

        versions.removeAll(getIncompatibleVer(versions, org, name));
        return pathToVersions(versions);
    }

    protected List<Path> getIncompatibleVer(List<Path> versions, PackageOrg org, PackageName name) {
        List<Path> incompatibleVersions = new ArrayList<>();

        if (!versions.isEmpty()) {
            for (Path ver : versions) {
                Path pkgJsonPath = getPackagePath(org.value(), name.value(),
                        Optional.of(ver.getFileName()).get().toFile().getName()).resolve(ProjectConstants.PACKAGE_JSON);
                if (Files.exists(pkgJsonPath)) {
                    String packageVer = BalaFiles.readPkgJson(pkgJsonPath).getBallerinaVersion();
                    String packVer = RepoUtils.getBallerinaShortVersion();
                    if (!isCompatible(packageVer, packVer)) {
                        incompatibleVersions.add(ver);
                    }
                } else {
                    incompatibleVersions.add(ver);
                }
            }
        }

        return incompatibleVersions;
    }

    /**
     * Returns if a package is compatible with the current platform version
     * (ballerinaShortVersion of the current distribution).
     *
     * A package is considered to be compatible with the current platform
     * if the platform version that the package is built on has the same major
     * version and is not greater than the version of the current platform.
     *
     * slbeta versions are considered compatible which is a special case.
     * TODO: we can check if this is necessary after the SL GA
     *
     * @param pkgBalVer version of the platform that the package is built on
     * @param distBalVer version of the current platform
     *
     * @return true if compatible
     */
    private boolean isCompatible(String pkgBalVer, String distBalVer) {
        if (pkgBalVer.equals(distBalVer) || pkgBalVer.startsWith("slbeta")) {
            return true;
        }
        Version pkgSemVer;
        Version distSemVer;
        try {
            pkgSemVer = Version.valueOf(pkgBalVer);
            distSemVer = Version.valueOf(distBalVer);


            if (pkgSemVer.getMajorVersion() == distSemVer.getMajorVersion()) {
                return !pkgSemVer.greaterThan(distSemVer);
            }
        } catch (UnexpectedCharacterException ignore) {
            // this is mainly to avoid slalpha versions
        }

        return false;
    }

    @Override
    protected DependencyGraph<PackageDescriptor> getDependencyGraph(PackageOrg org,
                                                                    PackageName name,
                                                                    PackageVersion version) {
        Path balaPath = getPackagePath(org.toString(), name.toString(), version.toString());
        BalaFiles.DependencyGraphResult dependencyGraphResult = BalaFiles.createPackageDependencyGraph(balaPath);
        return dependencyGraphResult.packageDependencyGraph();
    }

    @Override
    public Collection<ModuleDescriptor> getModules(PackageOrg org,
                                                      PackageName name,
                                                      PackageVersion version) {
        Path balaPath = getPackagePath(org.toString(), name.toString(), version.toString());
        BalaFiles.DependencyGraphResult dependencyGraphResult = BalaFiles.createPackageDependencyGraph(balaPath);
        return dependencyGraphResult.moduleDependencies().keySet();
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
