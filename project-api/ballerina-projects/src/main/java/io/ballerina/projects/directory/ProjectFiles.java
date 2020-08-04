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
package io.ballerina.projects.directory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains a set of utility methods that create an in-memory representation of a Ballerina project directory.
 *
 * @since 2.0.0
 */
public class ProjectFiles {
    private static final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.bal");

    private ProjectFiles() {
    }

    public static PackageFileData loadPackageData(String packageDir) {
        if (packageDir == null) {
            throw new IllegalArgumentException("packageDir cannot be null");
        }

        // Check whether the directory exists
        Path packageDirPath = Paths.get(packageDir).toAbsolutePath();
        if (!Files.exists(packageDirPath)) {
            // TODO handle the error
            // TODO use a custom runtime error
            throw new RuntimeException("directory does not exists: " + packageDir);
        }

        if (!Files.isDirectory(packageDirPath)) {
            throw new RuntimeException("Not a directory: " + packageDir);
        }

        // Check whether this is a project: Ballerina.toml file has to be there
        Path ballerinaTomlPath = packageDirPath.resolve("Ballerina.toml");
        if (!Files.exists(ballerinaTomlPath)) {
            // TODO handle the error
            // TODO use a custom runtime error
            throw new RuntimeException("Not a package directory: " + packageDir);
        }

        // Load Ballerina.toml
        // Load default module
        // load other modules
        ModuleFileData defaultModule = loadModule(packageDirPath);
        List<ModuleFileData> otherModules = loadOtherModules(packageDirPath);
        return PackageFileData.from(packageDirPath, defaultModule, otherModules);
    }

    private static List<ModuleFileData> loadOtherModules(Path packageDirPath) {
        Path modulesDirPath = packageDirPath.resolve("modules");
        if (!Files.isDirectory(modulesDirPath)) {
            return Collections.emptyList();
        }

        try (Stream<Path> pathStream = Files.walk(modulesDirPath, 1)) {
            return pathStream
                    .filter(path -> !path.equals(modulesDirPath))
                    .filter(Files::isDirectory)
                    .map(ProjectFiles::loadModule)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ModuleFileData loadModule(Path moduleDirPath) {
        List<DocumentFileData> srcDocs = loadDocuments(moduleDirPath);
        List<DocumentFileData> testSrcDocs;
        Path testDirPath = moduleDirPath.resolve("tests");
        if (Files.isDirectory(testDirPath)) {
            testSrcDocs = loadDocuments(testDirPath);
        } else {
            testSrcDocs = Collections.emptyList();
        }
        // TODO Read Module.md file. Do we need to? Balo creator may need to package Module.md
        return ModuleFileData.from(moduleDirPath, srcDocs, testSrcDocs);
    }

    private static List<DocumentFileData> loadDocuments(Path dirPath) {
        try (Stream<Path> pathStream = Files.walk(dirPath, 1)) {
            return pathStream
                    .filter(matcher::matches)
                    .map(ProjectFiles::loadDocument)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static DocumentFileData loadDocument(Path documentFilePath) {
        Path fileNamePath = documentFilePath.getFileName();
        // IMO, fileNamePath cannot be null in this case.
        String name = fileNamePath != null ? fileNamePath.toString() : "";
        return DocumentFileData.from(name, documentFilePath);
    }
}
