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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static io.ballerina.projects.utils.ProjectConstants.BLANG_COMPILED_PKG_BINARY_EXT;

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
        Path parent = filePath.getParent();
        if (null != parent) {
            if (Files.exists(parent.resolve(ProjectConstants.BALLERINA_TOML))) {
                return parent;
            }
            return findProjectRoot(parent);
        }
        return null;
    }

    public static String getBaloName(String org, String pkgName, String version, String platform) {
        // <orgname>-<packagename>-<platform>-<version>.balo
        if (platform == null || "".equals(platform)) {
            platform = "any";
        }
        return org + "-" + pkgName + "-" + platform + "-" + version + BLANG_COMPILED_PKG_BINARY_EXT;
    }

}

