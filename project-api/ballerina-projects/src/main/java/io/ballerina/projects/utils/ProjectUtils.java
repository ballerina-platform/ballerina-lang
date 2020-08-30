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
package io.ballerina.projects.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Project related util methods.
 *
 * @since 2.0.0
 */
public class ProjectUtils {

    /**
     * Validates the org-name.
     *
     * @param orgName The org-name
     * @return True if valid org-name or package name, else false.
     */
    public static boolean validateOrgName(String orgName) {
        String validRegex = "^[a-z0-9_]*$";
        return Pattern.matches(validRegex, orgName);
    }

    /**
     * Validates the package name.
     *
     * @param packageName The package name.
     * @return True if valid org name, else false.
     */
    public static boolean validatePkgName(String packageName) {
        String validRegex = "^[a-z0-9_]*$";
        return Pattern.matches(validRegex, packageName);
    }

    /**
     * Validates the module name.
     *
     * @param moduleName The module name.
     * @return True if valid module name, else false.
     */
    public static boolean validateModuleName(String moduleName) {
        String validRegex = "^[a-zA-Z0-9_.]*$";
        return Pattern.matches(validRegex, moduleName);
    }

    /**
     * Find the project root by recursively up to the root.
     *
     * @param filePath project path
     * @return project root
     */
    public static Path findProjectRoot(Path filePath) {
        Path path = filePath.resolve(ProjectConstants.BALLERINA_TOML);
        if (Files.exists(path)) {
            return filePath;
        }
        Path parent = filePath.getParent();
        if (null != parent) {
            return findProjectRoot(parent);
        }
        return null;
    }

    /**
     * Checks if the path is a Ballerina project.
     *
     * @param sourceRoot source root of the project.
     * @return true if the directory is a project repo, false if its the home repo
     */
    public static boolean isBallerinaProject(Path sourceRoot) {
        Path ballerinaToml = sourceRoot.resolve(ProjectConstants.BALLERINA_TOML);
        return Files.isDirectory(sourceRoot)
                && Files.exists(ballerinaToml)
                && Files.isRegularFile(ballerinaToml)
                && (findProjectRoot(sourceRoot) != null);
    }

    /**
     * Creates the target directory structure in a given path.
     *
     * @param sourceRoot source root of the project
     * @return target path
     * @throws IOException if target creation fails
     */
    static Path createTargetDirectoryStructure(Path sourceRoot) throws IOException {
        Path targetDir;
        targetDir = sourceRoot.resolve(ProjectConstants.TARGET_DIR_NAME);
        if (targetDir.toFile().exists()) {
            Files.walk(targetDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectories(targetDir.resolve(ProjectConstants.CACHES_DIR_NAME)
                .resolve(ProjectConstants.BIR_CACHE_DIR_NAME));
        Files.createDirectories(targetDir.resolve(ProjectConstants.CACHES_DIR_NAME)
                .resolve(ProjectConstants.JAR_CACHE_DIR_NAME));
        Files.createDirectories(targetDir.resolve(ProjectConstants.TARGET_BALO_DIRECTORY));
        Files.createDirectories(targetDir.resolve(ProjectConstants.BIN_DIR_NAME));
        Files.createDirectories(targetDir.resolve(ProjectConstants.TEST_DIR_NAME)
                .resolve(ProjectConstants.JSON_CACHE_DIR_NAME));

        return targetDir;
    }

    /**
     * Guess organization name based on user name in system.
     *
     * @return organization name
     */
    public static String guessOrgName() {
        String guessOrgName = System.getProperty(ProjectConstants.USER_NAME);
        if (guessOrgName == null) {
            guessOrgName = "my_org";
        } else {
            guessOrgName = guessOrgName.toLowerCase(Locale.getDefault());
        }
        return guessOrgName;
    }

    /**
     * Guess package name with valid pattern.
     * @param packageName package name
     * @return package name
     */
    public static String guessPkgName (String packageName) {
        if (!validatePkgName(packageName)) {
            return packageName.replaceAll("-", "_");
        }
        return packageName;
    }

    /**
     * Check if a ballerina module exist.
     * @param projectPath project path
     * @param moduleName module name
     * @return module exist
     */
    public static boolean isModuleExist(Path projectPath, String moduleName) {
        Path modulePath = projectPath.resolve(ProjectConstants.MODULES_DIR_NAME).resolve(moduleName);
        return Files.exists(modulePath);
    }
}

