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
package io.ballerina.projects.internal;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.exceptions.InvalidBalaException;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.projects.util.ProjectConstants.DOT;
import static io.ballerina.projects.util.ProjectUtils.checkReadPermission;

/**
 * Contains a set of utility methods that create an in-memory representation of a Ballerina project directory.
 *
 * @since 2.0.0
 */
public class ProjectFiles {
    public static final PathMatcher BAL_EXTENSION_MATCHER =
            FileSystems.getDefault().getPathMatcher("glob:**.bal");
    public static final PathMatcher BALA_EXTENSION_MATCHER =
            FileSystems.getDefault().getPathMatcher("glob:**.bala");

    private ProjectFiles() {
    }

    public static PackageData loadSingleFileProjectPackageData(Path filePath) {
        DocumentData documentData = loadDocument(filePath);
        ModuleData defaultModule = ModuleData
                .from(filePath, DOT, Collections.singletonList(documentData), Collections.emptyList(), null,
                        Collections.emptyList(), Collections.emptyList());
        return PackageData.from(filePath, defaultModule, Collections.emptyList(),
                null, null, null, null, null);
    }

    public static PackageData loadBuildProjectPackageData(Path packageDirPath) {
        ModuleData defaultModule = loadModule(packageDirPath);
        List<ModuleData> otherModules = loadOtherModules(packageDirPath);

        DocumentData ballerinaToml = loadDocument(packageDirPath.resolve(ProjectConstants.BALLERINA_TOML));
        DocumentData dependenciesToml = loadDocument(packageDirPath.resolve(ProjectConstants.DEPENDENCIES_TOML));
        DocumentData cloudToml = loadDocument(packageDirPath.resolve(ProjectConstants.CLOUD_TOML));
        DocumentData compilerPluginToml = loadDocument(packageDirPath.resolve(ProjectConstants.COMPILER_PLUGIN_TOML));
        DocumentData packageMd = loadDocument(packageDirPath.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME));

        return PackageData.from(packageDirPath, defaultModule, otherModules,
                ballerinaToml, dependenciesToml, cloudToml, compilerPluginToml, packageMd);
    }

    private static List<ModuleData> loadOtherModules(Path packageDirPath) {
        Path modulesDirPath = packageDirPath.resolve("modules");
        if (!Files.isDirectory(modulesDirPath)) {
            return Collections.emptyList();
        }

        try (Stream<Path> pathStream = Files.walk(modulesDirPath, 1)) {
            return pathStream
                    .filter(path -> !path.equals(modulesDirPath))
                    .filter(Files::isDirectory)
                    .filter(path -> {
                        // validate moduleName
                        if (!ProjectUtils.validateModuleName(path.toFile().getName())) {
                            throw new ProjectException("Invalid module name : '" + path.getFileName() + "' :\n" +
                                    "Module name can only contain alphanumerics, underscores and periods");
                        }
                        if (!ProjectUtils.validateNameLength(path.toFile().getName())) {
                            throw new ProjectException("Invalid module name : '" + path.getFileName() + "' :\n" +
                                    "Maximum length of module name is 256 characters");
                        }
                        return true;
                    })
                    .map(ProjectFiles::loadModule)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ProjectException(e);
        }
    }

    private static ModuleData loadModule(Path moduleDirPath) {
        List<DocumentData> srcDocs = loadDocuments(moduleDirPath);
        List<DocumentData> testSrcDocs;
        Path testDirPath = moduleDirPath.resolve("tests");
        if (Files.isDirectory(testDirPath)) {
            testSrcDocs = loadTestDocuments(testDirPath);
        } else {
            testSrcDocs = Collections.emptyList();
        }

        DocumentData moduleMd = loadDocument(moduleDirPath.resolve(ProjectConstants.MODULE_MD_FILE_NAME));
        List<Path> resources = loadResources(moduleDirPath);
        List<Path> testResources = loadResources(moduleDirPath.resolve(ProjectConstants.TEST_DIR_NAME));
        // TODO Read Module.md file. Do we need to? Bala creator may need to package Module.md
        return ModuleData.from(moduleDirPath, moduleDirPath.toFile().getName(), srcDocs, testSrcDocs, moduleMd,
                resources, testResources);
    }

    public static List<Path> loadResources(Path modulePath) {
        Path resourcesPath = modulePath.resolve(ProjectConstants.RESOURCE_DIR_NAME);
        if (Files.notExists(resourcesPath)) {
            return Collections.emptyList();
        }

        try {
            checkReadPermission(modulePath);
        } catch (UnsupportedOperationException ignore) {
            // ignore for zip entries
        }
        try (Stream<Path> pathStream = Files.walk(resourcesPath, 10)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ProjectException(e);
        }
    }

    public static List<DocumentData> loadDocuments(Path dirPath) {
        try {
            checkReadPermission(dirPath);
        } catch (UnsupportedOperationException ignore) {
            // ignore for zip entries
        }
        try (Stream<Path> pathStream = Files.walk(dirPath, 1)) {
            return pathStream
                    .filter(BAL_EXTENSION_MATCHER::matches)
                    .filter(Files::isRegularFile)
                    .map(ProjectFiles::loadDocument)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ProjectException(e);
        }
    }

    private static List<DocumentData> loadTestDocuments(Path dirPath) {
        try {
            checkReadPermission(dirPath);
        } catch (UnsupportedOperationException ignore) {
            // ignore for zip entries
        }
        try (Stream<Path> pathStream = Files.walk(dirPath, 1)) {
            return pathStream
                    .filter(BAL_EXTENSION_MATCHER::matches)
                    .map(ProjectFiles::loadTestDocument)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ProjectException(e);
        }
    }

    public static DocumentData loadDocument(Path documentFilePath) {
        if (Files.notExists(documentFilePath)) {
            return null;
        }
        try {
            checkReadPermission(documentFilePath);
        } catch (UnsupportedOperationException ignore) {
            // ignore for zip entries
        }

        String content;
        try {
            content = Files.readString(documentFilePath, Charset.defaultCharset());
        } catch (IOException e) {
            throw new ProjectException(e);
        }
        return DocumentData.from(Optional.of(documentFilePath.getFileName()).get().toString(), content);
    }

    private static DocumentData loadTestDocument(Path documentFilePath) {
        try {
            checkReadPermission(documentFilePath);
        } catch (UnsupportedOperationException ignore) {
            // ignore for zip entries
        }
        String content;
        try {
            content = Files.readString(documentFilePath, Charset.defaultCharset());
        } catch (IOException e) {
            throw new ProjectException(e);
        }
        String documentName = Optional.of(documentFilePath.getFileName()).get().toString();
        return DocumentData.from(ProjectConstants.TEST_DIR_NAME + "/" + documentName, content);
    }

    public static BuildOptions createBuildOptions(PackageConfig packageConfig, BuildOptions theirOptions,
                                                  Path projectDirPath) {
        // Todo figure out how to pass the build options without a performance hit
        TomlDocument ballerinaToml = TomlDocument.from(ProjectConstants.BALLERINA_TOML,
                packageConfig.ballerinaToml().map(t -> t.content()).orElse(""));
        TomlDocument pluginToml = TomlDocument.from(ProjectConstants.COMPILER_PLUGIN_TOML,
                packageConfig.dependenciesToml().map(t -> t.content()).orElse(""));
        ManifestBuilder manifestBuilder = ManifestBuilder
                .from(ballerinaToml, pluginToml, projectDirPath);
        BuildOptions defaultBuildOptions = manifestBuilder.buildOptions();
        if (defaultBuildOptions == null) {
            defaultBuildOptions = BuildOptions.builder().build();
        }
        return defaultBuildOptions.acceptTheirs(theirOptions);
    }

    public static void validateBuildProjectDirPath(Path projectDirPath) {
        if (Files.notExists(projectDirPath)) {
            throw new ProjectException("The directory does not exist: " + projectDirPath);
        }

        if (!Files.isDirectory(projectDirPath)) {
            throw new ProjectException("Invalid Ballerina package directory: " + projectDirPath);
        }

        if (!ProjectUtils.isBallerinaProject(projectDirPath)) {
            throw new ProjectException("Invalid Ballerina package directory: " + projectDirPath +
                    ", cannot find 'Ballerina.toml' file.");
        }

        if (ProjectUtils.findProjectRoot(projectDirPath.toAbsolutePath().getParent()) != null) {
            throw new ProjectException("Provided path is already within a Ballerina package: " + projectDirPath);
        }

        checkReadPermission(projectDirPath);
    }

    public static void validateSingleFileProjectFilePath(Path filePath) {
        if (Files.notExists(filePath)) {
            throw new ProjectException("The file does not exist: " + filePath);
        }

        if (!Files.isRegularFile(filePath) || !ProjectFiles.BAL_EXTENSION_MATCHER.matches(filePath)) {
            throw new ProjectException("Invalid Ballerina source file(.bal): " + filePath);
        }

        // Check if it is inside a project
        Path projectRoot = ProjectUtils.findProjectRoot(filePath);
        if (null != projectRoot) {
            Path absFilePath = filePath.toAbsolutePath();
            if (projectRoot.equals(Optional.of(absFilePath.getParent()).get())) {
                throw new ProjectException("The source file '" + filePath + "' belongs to a Ballerina package.");
            }
            // Check if it is inside a module
            Path modulesRoot = projectRoot.resolve(ProjectConstants.MODULES_ROOT);
            Path parent = absFilePath.getParent();
            if (parent != null) {
                if (modulesRoot.equals(Optional.of(parent.getParent()).get())) {
                    throw new ProjectException("The source file '" + filePath + "' belongs to a Ballerina package.");
                }
            }
        }
        checkReadPermission(filePath);
    }

    public static void validateBalaProjectPath(Path balaPath) {
        if (Files.notExists(balaPath)) {
            throw new ProjectException("Given bala path does not exist: " + balaPath);
        }

        if (!isValidBalaFile(balaPath) && !isValidBalaDir(balaPath)) {
            throw new InvalidBalaException("invalid bala file: " + balaPath);
        }

        if (!balaPath.toFile().canRead()) {
            throw new ProjectException("insufficient privileges to bala: " + balaPath);
        }
    }

    private static boolean isValidBalaDir(Path balaPath) {
        if (Files.notExists(balaPath.resolve(ProjectConstants.DEPENDENCY_GRAPH_JSON))) {
            return false;
        }
        if (Files.notExists(balaPath.resolve(ProjectConstants.PACKAGE_JSON))) {
            return false;
        }
        if (Files.notExists(balaPath.resolve(ProjectConstants.BALA_JSON))) {
            return false;
        }
        Path modulesRoot = balaPath.resolve(ProjectConstants.MODULES_ROOT);
        File[] files = modulesRoot.toFile().listFiles();
        if (files == null) {
            return false;
        }
        return Files.isDirectory(modulesRoot) && files.length >= 1;
    }

    private static boolean isValidBalaFile(Path balaPath) {
        return Files.isRegularFile(balaPath) && ProjectFiles.BALA_EXTENSION_MATCHER.matches(balaPath);
    }
}
