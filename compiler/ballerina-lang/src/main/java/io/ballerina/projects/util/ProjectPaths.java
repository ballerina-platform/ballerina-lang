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
package io.ballerina.projects.util;

import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;

/**
 * Consists of static methods that may be used to obtain {@link Project}
 * information using file paths.
 *
 * @since 2.0.0
 */
public class ProjectPaths {

    /**
     * Finds the root directory of a Ballerina package using the filepath provided.
     *
     * @param filepath ballerina file that belongs to a package
     * @return path to the package root directory
     * @throws ProjectException if the provided path is invalid or if it is a standalone ballerina file
     */
    public static Path packageRoot(Path filepath) throws ProjectException {
        // check if the file exists
        if (!Files.exists(filepath)) {
            throw new ProjectException("provided path does not exist:" + filepath);
        }

        if (Files.isDirectory(filepath)) {
            if (hasBallerinaToml(filepath) || hasPackageJson(filepath)) {
                return filepath;
            }
            if (isModulesRoot(filepath)) {
                return findProjectRoot(filepath).orElseThrow();
            }
            if (isAModuleRoot(filepath)) {
                return findProjectRoot(filepath).orElseThrow();
            }
            if (isAModuleTestsRoot(filepath)) {
                return findProjectRoot(filepath).orElseThrow();
            }
            throw new ProjectException("provided directory does not belong to a Ballerina package: " + filepath);
        }

        // check if the file is a regular file
        if (!Files.isRegularFile(filepath)) {
            throw new ProjectException("provided path is not a regular file: " + filepath);
        }

        // Check if the file is inside a Ballerina package directory
        Optional<Path> projectRoot = findProjectRoot(filepath);
        if (projectRoot.isEmpty()) {
            throw new ProjectException("provided file path does not belong to a Ballerina package: " + filepath);
        }

        Path absFilePath = filepath.toAbsolutePath().normalize();
        if (hasBallerinaToml(projectRoot.get())) {
            // check if the file is a ballerina project related toml file
            if (isBallerinaRelatedToml(filepath)) {
                return filepath.getParent();
            }

            if (!isBalFile(filepath)) {
                throw new ProjectException("provided path is not a valid Ballerina source file: " + filepath);
            }

            // check if the file is a source file in the default module
            if (isDefaultModuleSrcFile(absFilePath)) {
                return absFilePath.getParent();
            }
            // check if the file is a test file in the default module
            if (isDefaultModuleTestFile(absFilePath)) {
                Path testsRoot = Optional.of(absFilePath.getParent()).get();
                return testsRoot.getParent();
            }
            // check if the file is a source file in a non-default module
            if (isNonDefaultModuleSrcFile(filepath)) {
                Path modulesRoot = Optional.of(Optional.of(absFilePath.getParent()).get().getParent()).get();
                return modulesRoot.getParent();
            }
            // check if the file is a test file in a non-default module
            if (isNonDefaultModuleTestFile(filepath)) {
                Path testsRoot = Optional.of(absFilePath.getParent()).get();
                Path modulesRoot = Optional.of(Optional.of(testsRoot.getParent()).get().getParent()).get();
                return modulesRoot.getParent();
            }
        } else {
            // check if the file is a source file in a bala project
            if (isBalaProjectSrcFile(filepath)) {
                Path modulesRoot = Optional.of(Optional.of(absFilePath.getParent()).get().getParent()).get();
                return modulesRoot.getParent();
            }
        }

        throw new ProjectException("provided file path does not belong to a Ballerina package: " + filepath);
    }

    private static boolean isModulesRoot(Path filepath) {
        Path absFilePath = filepath.toAbsolutePath().normalize();
        Optional<Path> projectRoot = findProjectRoot(absFilePath);
        if (projectRoot.isPresent()) {
            Path modulesRoot = projectRoot.get().resolve(ProjectConstants.MODULES_ROOT);
            return modulesRoot.toAbsolutePath().normalize().toString().equals(absFilePath.toString());
        }
        return false;
    }

    private static boolean isAModuleTestsRoot(Path filepath) {
        Path absFilePath = filepath.toAbsolutePath().normalize();
        Optional<Path> projectRoot = findProjectRoot(filepath);
        if (projectRoot.isPresent()) {
            Path fileName = absFilePath.getFileName();
            if (fileName != null) {
                if (fileName.toString().equals(ProjectConstants.TEST_DIR_NAME)) {
                    Path parent = filepath.getParent();
                    if (parent != null) {
                        // Check if it is the tests root of the default module
                        if (projectRoot.get().resolve(ProjectConstants.TEST_DIR_NAME).toString()
                                .equals(absFilePath.toString())) {
                            return true;
                        }
                        // Check if it is the root of the default module
                        Path moduleRoot = projectRoot.get().resolve(ProjectConstants.MODULES_ROOT).resolve(
                                Optional.of(parent.getFileName()).get());
                        return moduleRoot.toAbsolutePath().normalize().toString().equals(parent.toString());
                    }
                }
            }
        }
        return false;
    }

    private static boolean isAModuleRoot(Path filepath) {
        Path absFilePath = filepath.toAbsolutePath().normalize();
        Optional<Path> projectRoot = findProjectRoot(absFilePath);
        if (projectRoot.isPresent()) {
            Path fileName = absFilePath.getFileName();
            if (fileName != null) {
                Path moduleRoot = projectRoot.get().resolve(ProjectConstants.MODULES_ROOT).resolve(fileName);
                return moduleRoot.toAbsolutePath().normalize().toString().equals(absFilePath.toString());
            }
        }
        return false;
    }

    /**
     * Returns whether the provided path is a valid Ballerina source file.
     *
     * @param filepath Ballerina file path
     * @return true if the path is a Ballerina source file
     */
    public static boolean isBalFile(Path filepath) {
        return Files.exists(filepath)
                && Files.isRegularFile(filepath)
                && filepath.toString().endsWith(ProjectDirConstants.BLANG_SOURCE_EXT);
    }

    /**
     * Returns whether the provided path is a ballerina project related toml file.
     *
     * @param filepath Ballerina file path
     * @return true if the path is a ballerina project related toml file.
     */
    private static boolean isBallerinaRelatedToml(Path filepath) {
        String fileName = Optional.of(filepath.getFileName()).get().toString();
        switch (fileName) {
            case ProjectConstants.BALLERINA_TOML:
            case ProjectConstants.CLOUD_TOML:
            case ProjectConstants.CONFIGURATION_TOML:
            case ProjectConstants.DEPENDENCIES_TOML:
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks if the provided path is a standalone filepath.
     *
     * @param filepath path to bal filepath
     * @return true if the filepath is a standalone bal filepath
     */
    public static boolean isStandaloneBalFile(Path filepath) {
        // Check if the filepath is a valid Ballerina source filepath
        if (!isBalFile(filepath)) {
            return false;
        }

        // Check if the file is inside a Ballerina package directory
        if (findProjectRoot(filepath).isEmpty()) {
            return true;
        }

        // check if the filepath is a source filepath in the default module
        if (isDefaultModuleSrcFile(filepath)) {
            return false;
        }
        // check if the filepath is a test filepath in the default module
        if (isDefaultModuleTestFile(filepath)) {
            return false;
        }
        // check if the filepath is a source filepath in a non-default module
        if (isNonDefaultModuleSrcFile(filepath)) {
            return false;
        }
        // check if the filepath is a test filepath in a non-default module
        if (isNonDefaultModuleTestFile(filepath)) {
            return false;
        }

        if (isBalaProjectSrcFile(filepath)) {
            return false;
        }

        return true;
    }

    static boolean isDefaultModuleSrcFile(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        return hasBallerinaToml(Optional.of(absFilePath.getParent()).get());
    }

    static boolean isDefaultModuleTestFile(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        Path testsRoot = Optional.of(absFilePath.getParent()).get();
        if (!ProjectConstants.TEST_DIR_NAME.equals(testsRoot.toFile().getName())) {
            return false;
        }
        Path projectRoot = Optional.of(testsRoot.getParent()).get();
        return ProjectConstants.TEST_DIR_NAME.equals(testsRoot.toFile().getName())
                && hasBallerinaToml(projectRoot);
    }

    static boolean isNonDefaultModuleSrcFile(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        Path modulesRoot = Optional.of(Optional.of(absFilePath.getParent()).get().getParent()).get();
        Path projectRoot = modulesRoot.getParent();
        return ProjectConstants.MODULES_ROOT.equals(modulesRoot.toFile().getName())
                && hasBallerinaToml(projectRoot);
    }

    static boolean isBalaProjectSrcFile(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        Path modulesRoot = Optional.of(Optional.of(absFilePath.getParent()).get().getParent()).get();
        Path projectRoot = modulesRoot.getParent();
        return ProjectConstants.MODULES_ROOT.equals(modulesRoot.toFile().getName())
                && hasPackageJson(projectRoot);
    }

    static boolean isNonDefaultModuleTestFile(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        Path testsRoot = Optional.of(absFilePath.getParent()).get();
        if (!ProjectConstants.TEST_DIR_NAME.equals(testsRoot.toFile().getName())) {
            return false;
        }
        Path modulesRoot = Optional.of(Optional.of(testsRoot.getParent()).get().getParent()).get();
        Path projectRoot = modulesRoot.getParent();
        return ProjectConstants.MODULES_ROOT.equals(modulesRoot.toFile().getName())
                && hasBallerinaToml(projectRoot);
    }

    private static boolean hasBallerinaToml(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        return absFilePath.resolve(BALLERINA_TOML).toFile().exists();
    }

    private static boolean hasPackageJson(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        return absFilePath.resolve(ProjectConstants.PACKAGE_JSON).toFile().exists();
    }

    private static Optional<Path> findProjectRoot(Path filePath) {
        if (filePath != null) {
            filePath = filePath.toAbsolutePath().normalize();
            if (filePath.toFile().isDirectory()) {
                if (hasBallerinaToml(filePath) || hasPackageJson(filePath)) {
                    return Optional.of(filePath);
                }
            }
            return findProjectRoot(filePath.getParent());
        }
        return Optional.empty();
    }
}
