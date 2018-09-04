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
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class provides util methods for uninstalling Ballerina packages.
 *
 * @since 0.982.0
 */
public class UninstallUtils {
    private static final Path BALLERINA_HOME_PATH = RepoUtils.createAndGetHomeReposPath();
    private static PrintStream outStream = System.out;


    /**
     * Uninstalls the package from the home repository.
     *
     * @param fullPkgPath package provided
     */
    public static void uninstallPackage(String fullPkgPath) {
        String orgName;
        String packageName;
        String version;
        // Get org-name
        int orgNameIndex = fullPkgPath.indexOf("/");
        if (orgNameIndex != -1) {
            orgName = fullPkgPath.substring(0, orgNameIndex);
        } else {
            throw new BLangCompilerException("no org-name is provided");
        }
        // Get package name
        int packageNameIndex = fullPkgPath.indexOf(":");
        if (packageNameIndex != -1) { // version is provided
            packageName = fullPkgPath.substring(orgNameIndex + 1, packageNameIndex);
            version = fullPkgPath.substring(packageNameIndex + 1, fullPkgPath.length());
        } else {
            throw new BLangCompilerException("no package version is provided");
        }
        Path homeRepoPath = BALLERINA_HOME_PATH.resolve(ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME);
        Path cacheRepoPath = BALLERINA_HOME_PATH.resolve(ProjectDirConstants.CACHES_DIR_NAME)
                                                .resolve(ProjectDirConstants.BALLERINA_CENTRAL_DIR_NAME);
        Path pkgPath = Paths.get(orgName, packageName, version);

        // Check if package is installed locally
        if (Files.exists(homeRepoPath.resolve(pkgPath), LinkOption.NOFOLLOW_LINKS)) {
            deletePackage(homeRepoPath, pkgPath, fullPkgPath);
        } else if (Files.exists(cacheRepoPath.resolve(pkgPath), LinkOption.NOFOLLOW_LINKS)) {
            deletePackage(cacheRepoPath, pkgPath, fullPkgPath);
        } else {
            // package to be uninstalled doesn't exists
            throw new BLangCompilerException("package does not exist " + fullPkgPath);
        }
    }

    private static void deletePackage(Path repoPath, Path pkgPath, String fullPkgPath) {
        try {
            Path path = repoPath.resolve(pkgPath);
            Files.list(path)
                 .filter(filePath -> !Files.isDirectory(filePath, LinkOption.NOFOLLOW_LINKS))
                 .forEach(filePath -> {
                     try {
                         Files.delete(filePath);
                     } catch (IOException e) {
                         throw new BLangCompilerException("error uninstalling package " + fullPkgPath);
                     }
                 });
            // Delete the empty directories
            deleteEmptyDirsUpTo(path, repoPath);
            outStream.println(fullPkgPath + " successfully uninstalled");
        } catch (IOException e) {
            throw new BLangCompilerException("error uninstalling package " + fullPkgPath);
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
