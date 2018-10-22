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

import org.ballerinalang.launcher.LauncherUtils;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class provides util methods for uninstalling Ballerina modules.
 *
 * @since 0.982.0
 */
public class UninstallUtils {
    private static final Path BALLERINA_HOME_PATH = RepoUtils.createAndGetHomeReposPath();
    private static PrintStream outStream = System.out;

    /**
     * Uninstalls the module from the home repository.
     *
     * @param fullPkgPath module provided
     */
    public static void uninstallPackage(String fullPkgPath) {
        String orgName;
        String packageName;
        String version;
        // Get org-name
        int orgNameIndex = fullPkgPath.indexOf("/");
        if (orgNameIndex == -1) {
            throw LauncherUtils.createLauncherException("no org-name is provided");
        }
        orgName = fullPkgPath.substring(0, orgNameIndex);

        // Get package name
        int packageNameIndex = fullPkgPath.indexOf(":");
        if (packageNameIndex == -1) { // version is not provided
            throw LauncherUtils.createLauncherException("no module version is provided");
        }
        packageName = fullPkgPath.substring(orgNameIndex + 1, packageNameIndex);
        version = fullPkgPath.substring(packageNameIndex + 1, fullPkgPath.length());

        Path homeRepoPath = BALLERINA_HOME_PATH.resolve(ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME);
        Path cacheRepoPath = BALLERINA_HOME_PATH.resolve(ProjectDirConstants.CACHES_DIR_NAME)
                                                .resolve(ProjectDirConstants.BALLERINA_CENTRAL_DIR_NAME);
        Path pkgDirPath = Paths.get(orgName, packageName, version);

        // Check if module is installed locally
        if (Files.exists(homeRepoPath.resolve(pkgDirPath), LinkOption.NOFOLLOW_LINKS)) {
            deletePackage(homeRepoPath, pkgDirPath, fullPkgPath, packageName);
        } else if (Files.exists(cacheRepoPath.resolve(pkgDirPath), LinkOption.NOFOLLOW_LINKS)) {
            deletePackage(cacheRepoPath, pkgDirPath, fullPkgPath, packageName);
        } else {
            // module to be uninstalled doesn't exists
            throw LauncherUtils.createLauncherException("incorrect module signature provided " + fullPkgPath);
        }
    }

    /**
     * Remove module from home repository.
     *
     * @param repoPath    path to the repository which contains the installed and pulled modules
     * @param pkgDirPath  package directory path
     * @param fullPkgPath full package path as given by user which is used for logging purposes
     * @param pkgName     package name
     */
    private static void deletePackage(Path repoPath, Path pkgDirPath, String fullPkgPath, String pkgName) {
        try {
            Path path = repoPath.resolve(pkgDirPath);
            // Delete the package zip
            Files.deleteIfExists(path.resolve(pkgName + ProjectDirConstants.BLANG_COMPILED_PKG_EXT));

            // Delete the empty directories
            deleteEmptyParentDirs(path, repoPath);

            // Print that the package was successfully uninstalled
            outStream.println(fullPkgPath + " successfully uninstalled");
        } catch (IOException e) {
            throw LauncherUtils.createLauncherException("uninstalling module " + fullPkgPath + " was unsuccessful");
        }
    }

    /**
     * Delete empty parent directories.
     * After deleting the package.zip from the home repository we should delete the empty directories as well. We need
     * to delete all folders from the package path to the home directory only if they are empty (going backwards).
     * If the directory path is empty i.e. does not contain any files we simply delete the folder else we don't delete.
     *
     * @param pkgDirPath package directory path
     * @param repoPath   home repository path
     * @throws IOException throw an exception if an error occurs
     */
    private static void deleteEmptyParentDirs(Path pkgDirPath, Path repoPath) throws IOException {
        Path pathsInBetween = repoPath.relativize(pkgDirPath);
        for (int i = pathsInBetween.getNameCount(); i > 0; i--) {
            Path toRemove = repoPath.resolve(pathsInBetween.subpath(0, i));
            if (!Files.list(toRemove).findAny().isPresent()) {
                Files.delete(toRemove);
            }
        }
    }
}
