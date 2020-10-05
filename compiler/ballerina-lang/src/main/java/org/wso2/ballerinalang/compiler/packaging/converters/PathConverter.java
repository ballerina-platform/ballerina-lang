/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.packaging.converters;

//import com.sun.nio.zipfs.ZipFileSystem;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provide functions need to covert a patten to steam of sources.
 */
public class PathConverter implements Converter<Path> {

    private final Path root;
    private PathMatcher isResourceFile;
    private PathMatcher isTestResourceFile;

    public PathConverter(Path root) {
        this.root = root;
        this.isResourceFile = root.getFileSystem()
                // glob:src/*/resources/**
                .getPathMatcher("glob:" + ProjectDirConstants.SOURCE_DIR_NAME
                        + "/*/" + ProjectDirConstants.RESOURCE_DIR_NAME + "/**");
        this.isTestResourceFile = root.getFileSystem()
                // glob:src/*/tests/resources/**
                .getPathMatcher("glob:" + ProjectDirConstants.SOURCE_DIR_NAME
                        + "/*/" + ProjectDirConstants.TEST_DIR_NAME + "/"
                        + ProjectDirConstants.RESOURCE_DIR_NAME + "/**");
    }

    private boolean isBalWithTest(Path path, BasicFileAttributes attributes) {
        Path fileName = path.getFileName();
        // Ignore bal files in resources directory.
        Path relativeToRoot = root.relativize(path);
        if (isResourceFile.matches(relativeToRoot) || isTestResourceFile.matches(relativeToRoot)) {
            return false;
        }
        return attributes.isRegularFile() && fileName != null && fileName.toString().endsWith(".bal");
    }

    @Override
    public Path combine(Path path, String pathPart) {
        return path.resolve(pathPart);
    }

    @Override
    public Stream<Path> getLatestVersion(Path path, PackageID packageID) {
        if (Files.isDirectory(path)) {
            try {
                List<Path> pathList;
                try (Stream<Path> stream = Files.list(path)) {
                    pathList = stream.map(SortablePath::new)
                            .filter(SortablePath::valid)
                            .sorted(Comparator.reverseOrder())
                            .limit(1)
                            .map(SortablePath::getPath)
                            .collect(Collectors.toList());
                }
                if (packageID != null) {
                    if (packageID.version.value.isEmpty() && !packageID.orgName.equals(Names.BUILTIN_ORG)
                            && !packageID.orgName.equals(Names.ANON_ORG) && pathList.size() > 0) {
                        packageID.version = new Name(pathList.get(0).toFile().getName());
                    }
                }
                return pathList.stream();

            } catch (IOException ignore) {
            }
        }
        return Stream.of();
    }

    @Override
    public Stream<Path> expandBalWithTest(Path path) {
        if (Files.isDirectory(path)) {
            try {
                return Files.find(path, Integer.MAX_VALUE, this::isBalWithTest).sorted();
            } catch (IOException ignore) {
            }
        }
        return Stream.of();
    }

    @Override
    public Stream<Path> expandBal(Path path) {
        if (Files.isDirectory(path)) {
            try {
                List<Path> excludePaths = new ArrayList<>();
                excludePaths.add(Paths.get(ProjectDirConstants.TEST_DIR_NAME));
                excludePaths.add(Paths.get(ProjectDirConstants.RESOURCE_DIR_NAME));
                FilterSearch filterSearch = new FilterSearch(excludePaths);
                Files.walkFileTree(path, filterSearch);
                return filterSearch.getPathList().stream().sorted();
            } catch (IOException ignore) {
            }
        }
        return Stream.of();
    }

    @Override
    public Path start() {
        return root;
    }

    @Override
    public Stream<CompilerInput> finalize(Path path, PackageID pkgId) {
        // Set package version if its empty
        if (pkgId.version.value.isEmpty() && !pkgId.orgName.equals(Names.BUILTIN_ORG)
                && !pkgId.orgName.equals(Names.ANON_ORG)) {
            Manifest manifest = TomlParserUtils.getManifest(root);
            pkgId.version = new Name(manifest.getProject().getVersion());
        }

        if ((!ProjectDirs.isProject(root) || RepoUtils.isBallerinaStandaloneFile(path))
                && Files.isRegularFile(path)) {
            return Stream.of(new FileSystemSourceInput(path, root.resolve(pkgId.name.value)));
        } else if (Files.isRegularFile(path)) {
            return Stream.of(new FileSystemSourceInput(path,
                    root.resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                            .resolve(pkgId.name.value)));
        } else {
            return Stream.of();
        }
    }

    @Override
    public String toString() {
//        FileSystem fs = root.getFileSystem();
//        if (fs instanceof ZipFileSystem) {
//            return fs.toString();
//        } else {
            return root.toString();
//        }
    }

    public Path getRoot() {
        return root;
    }
}
