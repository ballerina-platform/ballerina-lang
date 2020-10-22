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
package io.ballerina.projects.repos;

import io.ballerina.projects.*;
import io.ballerina.projects.Package;
import io.ballerina.projects.balo.BaloProject;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.PackageLoadRequest;
import io.ballerina.projects.environment.Repository;
import io.ballerina.projects.utils.ProjectConstants;
import io.ballerina.projects.utils.ProjectUtils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Package Repository stored in file system.
 * The structure of the repository is as bellow
 *   - balo
 *       - org
 *           - package-name
 *               - version
 *                   - org-package-name-version-any.balo
 *   - cache
 *       - org
 *           - package-name
 *               - version
 *                   - bir
 *                       - mod1.bir
 *                       - mod2.bir
 *                   - jar
 *                       - org-package-name-version.jar
 *
 * @since 2.0.0
 */
public class FileSystemRepository implements Repository {

    Path root;
    Path balo;
    Path cache;

    public FileSystemRepository(Path cacheDirectory) {
        this.root = cacheDirectory;
        this.balo = this.root.resolve(ProjectConstants.REPO_BALO_DIR_NAME);
        this.cache = this.root.resolve(ProjectConstants.REPO_CACHE_DIR_NAME);
        // todo check if the directories are readable
    }



    public Optional<Path> getPackageBalo(ModuleLoadRequest pkg) {
        String baloName = ProjectUtils.getBaloName(pkg.orgName().orElse(""),
                pkg.packageName().toString(),
                pkg.version().orElse(SemanticVersion.from("0.0.0")).toString(),
                null);
        Path baloPath = balo.resolve(baloName);
        if (Files.exists(baloPath)) {
            return Optional.of(baloPath);
        }
        return Optional.empty();
    }

    public Optional<Path> getLatestPackageBalo(ModuleLoadRequest pkg) {
        String orgName = pkg.orgName().orElse("");
        String pkgName = pkg.packageName().value();
        String glob = "glob:**/" + orgName + "-" + pkgName + "-*.balo";
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);
        List<Path> modules = new ArrayList<>();
        try {
            Files.walkFileTree(balo, new SearchModules(pathMatcher, modules));
        } catch (IOException e) {
            throw new RuntimeException("Error while accessing Distribution cache: " + e.getMessage());
        }

        if (modules.isEmpty()) {
            return Optional.empty();
        } else {
            modules.sort(Comparator.comparing(Path::toString));
            return Optional.of(modules.get(modules.size() - 1));
        }
    }

    @Override
    public Optional<Package> getPackage(PackageLoadRequest packageLoadRequest) {
        // if version and org name is empty we add empty string so we return empty package anyway
        String packageName = packageLoadRequest.packageName().value();
        String orgName = packageLoadRequest.orgName().orElse("");
        String version = packageLoadRequest.version().orElse( SemanticVersion.from("0.0.0")).toString();
        String baloName = ProjectUtils.getBaloName(orgName,packageName,version,null);

        Path baloPath = this.balo.resolve(orgName).resolve(packageName).resolve(version).resolve(baloName);

        if (!Files.exists(baloPath)){
            return Optional.empty();
        }

        Project project = BaloProject.loadProject(baloPath);
        return Optional.of(project.currentPackage());
    }

    @Override
    public List<SemanticVersion> getPackageVersions(PackageLoadRequest packageLoadRequest) {
        // if version and org name is empty we add empty string so we return empty package anyway
        String packageName = packageLoadRequest.packageName().value();
        String orgName = packageLoadRequest.orgName().orElse("");

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

    private List<SemanticVersion> pathToVersions(List<Path> versions) {
        return versions.stream()
                .map(path -> {
                    return SemanticVersion.from(path.getParent().getFileName().toString());})
                .collect(Collectors.toList());
    }

    @Override
    public void cacheCompilation(PackageCompilation packageCompilation) {

    }

    @Override
    public Map<ModuleId, Path> getCachedBirs(Package aPackage) {
        return null;
    }

    @Override
    public Map<ModuleId, Path> getCachedJar(Package aPackage) {
        return null;
    }

    @Override
    public void cachePackageCompilation(PackageCompilation packageCompilation) {

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
