/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.directory;

import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleConfig;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.PackageId;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.utils.ProjectUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains a set of utility methods that creates the config hierarchy from the project directory.
 *
 * @since 2.0.0
 */
public class PackageLoader {
    private static final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.bal");

    public static PackageConfig loadPackage(String packageDir, boolean isSingleFile) {
        return createPackageConfig(Paths.get(packageDir).toAbsolutePath(), isSingleFile);
    }

    private static PackageConfig createPackageConfig(Path packagePath, boolean isSingleFile) {
        // TODO PackageData should contain the packageName. This should come from the Ballerina.toml file.
        // TODO For now, I take the directory name as the project name. I am not handling the case where the
        //  directory name is not a valid Ballerina identifier.

        // Validate that the provided package path exists
        if (packagePath == null) {
            throw new IllegalArgumentException("packageDir cannot be null");
        }
        Path fileName = packagePath.getFileName();
        if (fileName == null) {
            // TODO Proper error handling
            throw new IllegalStateException("This branch cannot be reached");
        }

        PackageName packageName = PackageName.from(fileName.toString());
        PackageId packageId = PackageId.create(packageName.toString());
        List<ModuleConfig> moduleConfigs = new ArrayList<>();

        if (isSingleFile) {
            moduleConfigs.add(createDefaultModuleConfig(packageName, packagePath, packageId));
        } else {
            // Check whether the directory exists
            if (!packagePath.toFile().canRead()
                    || !packagePath.toFile().canWrite() || !packagePath.toFile().canExecute()) {
                throw new RuntimeException("insufficient privileges to path: " + packagePath);
            }
            if (!Files.exists(packagePath)) {
                // TODO handle the error
                // TODO use a custom runtime error
                throw new RuntimeException("directory does not exists: " + packagePath);
            }

            if (!Files.isDirectory(packagePath)) {
                throw new RuntimeException("Not a directory: " + packagePath);
            }

            List<Path> otherModulePaths = loadOtherModulePaths(packagePath);
            moduleConfigs = otherModulePaths
                    .stream()
                    .map(moduleDirPath -> createModuleConfig(packageName, moduleDirPath, packageId))
                    .collect(Collectors.toList());
            ModuleConfig defaultModuleConfig = createDefaultModuleConfig(packageName,
                    packagePath, packageId);
            moduleConfigs.add(defaultModuleConfig);
        }

        return PackageConfig.from(packageId, packageName, packagePath, moduleConfigs);
    }

    private static ModuleConfig createDefaultModuleConfig(PackageName packageName,
                                                          Path moduleDirPath,
                                                          PackageId packageId) {
        ModuleName moduleName = ModuleName.from(packageName);
        return createModuleConfig(moduleName, moduleDirPath, packageId);
    }

    private static ModuleConfig createModuleConfig(PackageName packageName,
                                                   Path moduleDirPath,
                                                   PackageId packageId) {
        Path fileName = moduleDirPath.getFileName();
        if (fileName == null) {
            // TODO Proper error handling
            throw new IllegalStateException("This branch cannot be reached");
        }
        if (!ProjectUtils.validateModuleName(fileName.toString())) {
            throw new RuntimeException("Invalid module name : '" + fileName.toString() + "' :\n" +
                    "Module name can only contain alphanumerics, underscores and periods " +
                    "and the maximum length is 256 characters");
        }
        ModuleName moduleName = ModuleName.from(packageName, fileName.toString());
        return createModuleConfig(moduleName, moduleDirPath, packageId);
    }

    private static ModuleConfig createModuleConfig(ModuleName moduleName,
                                                   Path moduleDirPath,
                                                   PackageId packageId) {
        ModuleId moduleId = ModuleId.create(moduleName.toString(), packageId);

        List<Path> srcDocPaths = loadDocuments(moduleDirPath);
        List<Path> testSrcDocPaths;
        Path testDirPath = moduleDirPath.resolve("tests");
        if (Files.isDirectory(testDirPath)) {
            testSrcDocPaths = loadDocuments(testDirPath);
        } else {
            testSrcDocPaths = Collections.emptyList();
        }

        List<DocumentConfig> srcDocs = getDocumentConfigs(moduleId, srcDocPaths);
        List<DocumentConfig> testSrcDocs = getDocumentConfigs(moduleId, testSrcDocPaths);
        return ModuleConfig.from(moduleId, moduleName, moduleDirPath, srcDocs, testSrcDocs);
    }

    private static List<DocumentConfig> getDocumentConfigs(ModuleId moduleId, List<Path> documentPathList) {
        return documentPathList
                .stream()
                .map(srcDoc -> createDocumentConfig(srcDoc, moduleId))
                .collect(Collectors.toList());
    }

    private static DocumentConfig createDocumentConfig(Path documentPath, ModuleId moduleId) {
        if (documentPath == null) {
            throw new RuntimeException("document path cannot be null");
        }

        String name = documentPath.toFile().getName();
        String content;
        try {
            content = new String(Files.readAllBytes(documentPath), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final DocumentId documentId = DocumentId.create(name, moduleId);
        return DocumentConfig.from(documentId, content, name);
    }

    private static List<Path> loadDocuments(Path dirPath) {
        try (Stream<Path> pathStream = Files.walk(dirPath, 1)) {
            return pathStream
                    .filter(matcher::matches)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Path> loadOtherModulePaths(Path packageDirPath) {
        Path modulesDirPath = packageDirPath.resolve("modules");
        if (!Files.isDirectory(modulesDirPath)) {
            return Collections.emptyList();
        }

        try (Stream<Path> pathStream = Files.walk(modulesDirPath, 1)) {
            return pathStream
                    .filter(path -> !path.equals(modulesDirPath))
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
