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

import io.ballerina.projects.BallerinaToml;
import io.ballerina.projects.BallerinaTomlException;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;

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
    public static final PathMatcher BALR_EXTENSION_MATCHER =
            FileSystems.getDefault().getPathMatcher("glob:**.balo");

    private ProjectFiles() {
    }

    public static PackageData loadSingleFileProjectPackageData(Path filePath) {
        DocumentData documentData = loadDocument(filePath);
        ModuleData defaultModule = ModuleData
                .from(filePath, DOT, Collections.singletonList(documentData), Collections.emptyList());
        return PackageData.from(filePath, defaultModule, Collections.emptyList());
    }

    public static PackageData loadBuildProjectPackageData(Path packageDirPath) {
        ModuleData defaultModule = loadModule(packageDirPath);
        List<ModuleData> otherModules = loadOtherModules(packageDirPath);
        return PackageData.from(packageDirPath, defaultModule, otherModules);
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
                                    "Module name can only contain alphanumerics, underscores and periods " +
                                    "and the maximum length is 256 characters");
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
        // TODO Read Module.md file. Do we need to? Balo creator may need to package Module.md
        return ModuleData.from(moduleDirPath, moduleDirPath.toFile().getName(), srcDocs, testSrcDocs);
    }

    public static List<DocumentData> loadDocuments(Path dirPath) {
        try (Stream<Path> pathStream = Files.walk(dirPath, 1)) {
            return pathStream
                    .filter(BAL_EXTENSION_MATCHER::matches)
                    .map(ProjectFiles::loadDocument)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ProjectException(e);
        }
    }

    private static List<DocumentData> loadTestDocuments(Path dirPath) {
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
        String content;
        try {
            content = Files.readString(documentFilePath, Charset.defaultCharset());
        } catch (IOException e) {
            throw new ProjectException(e);
        }
        return DocumentData.from(Optional.of(documentFilePath.getFileName()).get().toString(), content);
    }

    private static DocumentData loadTestDocument(Path documentFilePath) {
        String content;
        try {
            content = Files.readString(documentFilePath, Charset.defaultCharset());
        } catch (IOException e) {
            throw new ProjectException(e);
        }
        String documentName = Optional.of(documentFilePath.getFileName()).get().toString();
        return DocumentData.from(ProjectConstants.TEST_DIR_NAME + "/" + documentName, content);
    }

    public static PackageManifest createPackageManifest(BallerinaToml ballerinaToml) {
        return ballerinaToml.packageManifest();
    }

    public static BuildOptions createBuildOptions(Path projectPath, BuildOptions theirOptions) {
        Path ballerinaTomlFilePath = projectPath.resolve(ProjectConstants.BALLERINA_TOML);
        BallerinaToml ballerinaToml = BallerinaToml.from(ballerinaTomlFilePath);
        if (ballerinaToml.diagnostics().hasErrors()) {
            throw new BallerinaTomlException(ballerinaToml.getErrorMessage());
        }

        BuildOptions defaultBuildOptions = ballerinaToml.buildOptions();
        if (defaultBuildOptions == null) {
            defaultBuildOptions = new BuildOptionsBuilder().build();
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

    public static void validateBalrProjectPath(Path balrPath) {
        if (Files.notExists(balrPath)) {
            throw new ProjectException("Given .balr file does not exist: " + balrPath);
        }

        if (!Files.isRegularFile(balrPath) || !ProjectFiles.BALR_EXTENSION_MATCHER.matches(balrPath)) {
            throw new ProjectException("Invalid .balr file: " + balrPath);
        }

        if (!balrPath.toFile().canRead()) {
            throw new ProjectException("insufficient privileges to balo: " + balrPath);
        }
    }
}
