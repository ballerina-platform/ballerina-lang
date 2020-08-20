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

import org.wso2.ballerinalang.compiler.util.ProjectDirs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Home repository util methods.
 *
 * @since 2.0.0
 */
public class RepoUtils {

    /**
     * Checks if the path is a project.
     *
     * @param sourceRoot source root of the project.
     * @return true if the directory is a project repo, false if its the home repo
     */
    public static boolean isBallerinaProject(Path sourceRoot) {
        Path manifest = sourceRoot.resolve(ProjectConstants.BALLERINA_TOML);
        return Files.isDirectory(sourceRoot) && Files.exists(manifest) && Files.isRegularFile(manifest);
    }

    /**
     * Checks if the path is a standalone file.
     *
     * @param file path to bal file
     * @return true if the file is a standalone bal file
     */
    public static boolean isBallerinaStandaloneFile(Path file) {
        // Check if the file is a regular file
        if (!Files.isRegularFile(file)) {
            return false;
        }
        // Check if it is a file with bal extention.
        if (!file.toString().endsWith(ProjectConstants.BLANG_SOURCE_EXT)) {
            return false;
        }
        // Check if it is inside a project
        Path projectRoot = ProjectDirs.findProjectRoot(file.getParent());
        if (null != projectRoot) {
            // Check if it is inside a module
            Path src = projectRoot.resolve(ProjectConstants.SOURCE_DIR_NAME);
            Path parent = file.getParent();
            while (parent != null) {
                if (src.equals(parent)) {
                    return false;
                }
                parent = parent.getParent();
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * Validates the org-name.
     *
     * @param orgName The org name.
     * @return True if valid org name, else false.
     */
    public static boolean validateOrgName(String orgName) {
        String validRegex = "^[a-zA-Z0-9_.]*$";
        return Pattern.matches(validRegex, orgName);
    }

    /**
     * Validates the package name.
     *
     * @param packageName The package name.
     * @return True if valid org name, else false.
     */
    public static boolean validatePkgName(String packageName) {
        String validRegex = "^[a-zA-Z0-9_.]*$";
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

}

