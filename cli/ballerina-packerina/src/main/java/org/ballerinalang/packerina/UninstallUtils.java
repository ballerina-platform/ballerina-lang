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
package org.ballerinalang.packerina;

import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class contains a set of utility methods to used by the uninstall command.
 *
 * @since 0.972.1
 */
public class UninstallUtils {
    private static final Path BALLERINA_HOME_PATH = RepoUtils.createAndGetHomeReposPath();

    /**
     * Uninstalls the package from the home repository.
     *
     * @param packageStr package provided
     */
    public static void uninstallPackage(String packageStr) {
        String orgName;
        String packageName;
        String version;
        // Get org-name
        int orgNameIndex = packageStr.indexOf("/");
        if (orgNameIndex != -1) {
            orgName = packageStr.substring(0, orgNameIndex);
        } else {
            throw new BLangCompilerException("no org-name is provided");
        }

        // Get package name
        int packageNameIndex = packageStr.indexOf(":");
        if (packageNameIndex != -1) { // version is provided
            packageName = packageStr.substring(orgNameIndex + 1, packageNameIndex);
            version = packageStr.substring(packageNameIndex + 1, packageStr.length());
        } else {
            throw new BLangCompilerException("no package version is provided");
        }
        Path homeRepoPath = Paths.get(BALLERINA_HOME_PATH.toString(), ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME);
        Path packagePath = homeRepoPath.resolve(Paths.get(orgName, packageName, version));

        if (Files.exists(packagePath, LinkOption.NOFOLLOW_LINKS) &&
                !Files.isDirectory(packagePath, LinkOption.NOFOLLOW_LINKS)) {
            throw new BLangCompilerException("a file exists with the same name as the package name: " +
                                                     packagePath.toString());
        } else if (!Files.exists(packagePath, LinkOption.NOFOLLOW_LINKS)) {
            // File doesn't exists
            throw new BLangCompilerException("package does not exist: " + packagePath.toString());
        }

        try {
            Files.list(packagePath)
                 .filter(filePath -> !Files.isDirectory(filePath, LinkOption.NOFOLLOW_LINKS))
                 .forEach(filePath -> {
                     try {
                         Files.delete(filePath);
                     } catch (IOException e) {
                         throw new BLangCompilerException("error uninstalling package: " + packageName +
                                                                  ": " + e.getMessage(), e);
                     }
                 });
            deleteEmptyDirsUpTo(packagePath, homeRepoPath);
        } catch (IOException e) {
            throw new BLangCompilerException("error uninstalling package: " + packageStr + ": " + e.getMessage(), e);
        }
    }

    /**
     * Delete directories.
     *
     * @param from package directory path
     * @param to   home repository path
     * @throws IOException throw an exception if an error occurs
     */
    private static void deleteEmptyDirsUpTo(Path from, Path to) throws IOException {
        Path pathsInBetween = to.relativize(from);
        for (int i = pathsInBetween.getNameCount(); i > 0; i--) {
            Path toRemove = to.resolve(pathsInBetween.subpath(0, i));
            if (Files.list(toRemove).findAny().isPresent()) {
                return;
            } else {
                Files.delete(toRemove);
            }
        }
    }
}
