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

import io.ballerina.projects.JdkVersion;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.balo.BaloProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.repos.FileSystemCache;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Package Repository stored in file system.
 * The structure of the repository is as bellow
 * - balo
 *     - org
 *         - package-name
 *             - version
 *                 - org-package-name-version-any.balo
 * - cache
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
    Path balo;
    private final Path cacheDir;
    private final Environment environment;

    // TODO Refactor this when we do repository/cache split
    public FileSystemRepository(Environment environment, Path cacheDirectory) {
        this.cacheDir = cacheDirectory;
        this.balo = cacheDirectory.resolve(ProjectConstants.REPO_BALO_DIR_NAME);
        this.environment = environment;
    }

    @Override
    public Optional<Package> getPackage(ResolutionRequest resolutionRequest) {
        // if version and org name is empty we add empty string so we return empty package anyway
        String packageName = resolutionRequest.packageName().value();
        String orgName = resolutionRequest.orgName().value();
        String version = resolutionRequest.version().isPresent() ?
                resolutionRequest.version().get().toString() : "0.0.0";

        //First we will check for a balo that match any platform
        String baloName = ProjectUtils.getBaloName(orgName, packageName, version, null);
        Path baloPath = this.balo.resolve(orgName).resolve(packageName).resolve(version).resolve(baloName);
        if (!Files.exists(baloPath)) {
            //If balo for any platform not exist check for specific platform
            String javaBaloName = ProjectUtils.getBaloName(orgName, packageName, version, JdkVersion.JAVA_11.code());
            baloPath = this.balo.resolve(orgName).resolve(packageName).resolve(version).resolve(javaBaloName);
            if (!Files.exists(baloPath)) {
                return Optional.empty();
            }
        }

        ProjectEnvironmentBuilder environmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
        environmentBuilder = environmentBuilder.addCompilationCacheFactory(
                new FileSystemCache.FileSystemCacheFactory(cacheDir));
        Project project = BaloProject.loadProject(environmentBuilder, baloPath);
        return Optional.of(project.currentPackage());
    }

    @Override
    public List<PackageVersion> getPackageVersions(ResolutionRequest resolutionRequest) {
        // if version and org name is empty we add empty string so we return empty package anyway
        String packageName = resolutionRequest.packageName().value();
        String orgName = resolutionRequest.orgName().value();

        // Here we dont rely on directories we check for available balos
        String globFilePart = orgName + "-" + packageName + "-*.balo";
        String glob = "glob:**/" + orgName + "/" + packageName + "/*/" + globFilePart;
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);
        List<Path> versions = new ArrayList<>();
        try {
            Files.walkFileTree(balo.resolve(orgName).resolve(packageName)
                    , new SearchModules(pathMatcher, versions));
        } catch (IOException e) {
            // in any error we should report to the top
            throw new RuntimeException("Error while accessing Distribution cache: " + e.getMessage());
        }

        return pathToVersions(versions);
    }

    /**
     * Get the list of packages in the balo cache.
     *
     * @return {@link List} of package names
     */
    public Map<String, List<String>> getPackages() {
        Map<String, List<String>> packagesMap = new HashMap<>();
        File[] orgDirs = this.balo.toFile().listFiles();
        if (orgDirs == null) {
            return packagesMap;
        }
        for (File file : orgDirs) {
            if (!file.isDirectory()) {
                continue;
            }
            String orgName = file.getName();
            File[] filesList = this.balo.resolve(orgName).toFile().listFiles();
            if (filesList == null) {
                return packagesMap;
            }
            List<String> pkgList = new ArrayList<>();
            for (File pkgDir : filesList) {
                if (!pkgDir.isDirectory() || pkgDir.isHidden()) {
                    continue;
                }
                File[] pkgs = this.balo.resolve(orgName).resolve(pkgDir.getName()).toFile().listFiles();
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

    private List<PackageVersion> pathToVersions(List<Path> versions) {
        return versions.stream()
                .map(path -> {
                    String version = Optional.ofNullable(path.getParent())
                            .map(parent -> parent.getFileName())
                            .map(file -> file.toString())
                            .orElse("0.0.0");
                    return PackageVersion.from(version);
                })
                .collect(Collectors.toList());
    }

    private static class SearchModules extends SimpleFileVisitor<Path> {

        private final PathMatcher pathMatcher;
        private final List<Path> versions;

        public SearchModules(PathMatcher pathMatcher, List<Path> versions) {
            this.pathMatcher = pathMatcher;
            this.versions = versions;
        }

        @Override
        public FileVisitResult visitFile(Path path,
                                         BasicFileAttributes attrs) throws IOException {
            if (pathMatcher.matches(path)) {
                versions.add(path);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc)
                throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}
