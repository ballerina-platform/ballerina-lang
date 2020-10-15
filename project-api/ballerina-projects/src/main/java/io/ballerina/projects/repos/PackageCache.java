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

import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.utils.ProjectUtils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Package cache where balo files are stored.
 *
 * @since 2.0.0
 */
public class PackageCache implements PackageRepo {

    Path baloDirectory;

    public PackageCache(Path cacheDirectory) {
        this.baloDirectory = cacheDirectory;
    }

    @Override
    public Optional<Path> getPackageBalo(ModuleLoadRequest pkg) {
        String baloName = ProjectUtils.getBaloName(pkg.orgName().orElse(""),
                pkg.packageName().toString(),
                pkg.version().orElse(SemanticVersion.from("0.0.0")).toString(),
                null);
        Path baloPath = baloDirectory.resolve(baloName);
        if (Files.exists(baloPath)) {
            return Optional.of(baloPath);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Path> getLatestPackageBalo(ModuleLoadRequest pkg) {
        String orgName = pkg.orgName().orElse("");
        String pkgName = pkg.packageName().value();
        String glob = "glob:**/" + orgName + "-" + pkgName + "-*.balo";
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);
        List<Path> modules = new ArrayList<>();
        try {
            Files.walkFileTree(baloDirectory, new SearchModules(pathMatcher, modules));
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

    private static class SearchModules extends SimpleFileVisitor<Path> {

        private final PathMatcher pathMatcher;
        private final List<Path> modules;

        public SearchModules(PathMatcher pathMatcher, List<Path> modules) {
            this.pathMatcher = pathMatcher;
            this.modules = modules;
        }

        @Override
        public FileVisitResult visitFile(Path path,
                                         BasicFileAttributes attrs) throws IOException {
            if (pathMatcher.matches(path)) {
                modules.add(path);
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
