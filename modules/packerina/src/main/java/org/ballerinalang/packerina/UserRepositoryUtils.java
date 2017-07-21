/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.BLangCompiler;
import org.ballerinalang.util.program.BLangPackages;
import org.ballerinalang.util.program.BLangPrograms;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.ballerinalang.util.BLangConstants.BLANG_SRC_FILE_SUFFIX;
import static org.ballerinalang.util.BLangConstants.USER_HOME;
import static org.ballerinalang.util.BLangConstants.USER_REPO_ARTIFACTS_DIRNAME;
import static org.ballerinalang.util.BLangConstants.USER_REPO_DEFAULT_DIRNAME;
import static org.ballerinalang.util.BLangConstants.USER_REPO_ENV_KEY;
import static org.ballerinalang.util.BLangConstants.USER_REPO_OBJ_DIRNAME;
import static org.ballerinalang.util.BLangConstants.USER_REPO_SRC_DIRNAME;
import static org.ballerinalang.util.program.BLangPrograms.createDirectory;

/**
 * This class contains a set of utility methods to handle Ballerina user repository.
 *
 * @since 0.90
 */
public class UserRepositoryUtils {

    public static Path initializeUserRepository() {
        Path userRepoPath = getUserRepositoryPath();

        // create directory structure, if not already done.
        Path artifactsDirPath = userRepoPath.resolve(USER_REPO_ARTIFACTS_DIRNAME);
        Path srcDirPath = artifactsDirPath.resolve(USER_REPO_SRC_DIRNAME);
        Path objDirPath = artifactsDirPath.resolve(USER_REPO_OBJ_DIRNAME);
        createDirectory(userRepoPath);
        createDirectory(artifactsDirPath);
        createDirectory(srcDirPath);
        createDirectory(objDirPath);
        return userRepoPath;
    }

    public static Path getUserRepositoryPath() {
        Path userRepoPath;
        String userRepoDir = System.getenv(USER_REPO_ENV_KEY);
        if (userRepoDir == null || userRepoDir.isEmpty()) {
            String userHomeDir = System.getProperty(USER_HOME);
            if (userHomeDir == null || userHomeDir.isEmpty()) {
                throw new RuntimeException("error creating user repository: unable to get user home directory");
            }
            userRepoPath = Paths.get(userHomeDir, USER_REPO_DEFAULT_DIRNAME);
        } else {
            // User has specified the user repo path with env variable.
            userRepoPath = Paths.get(userRepoDir);
        }

        userRepoPath = userRepoPath.toAbsolutePath();
        if (Files.exists(userRepoPath) && !Files.isDirectory(userRepoPath, LinkOption.NOFOLLOW_LINKS)) {
            throw new RuntimeException("user repository is not a directory: " + userRepoPath.toString());
        }
        return userRepoPath;
    }

    public static void installSourcePackage(Path sourceRootPath, String packageStr) {
        // First, we need to validate what is in their.
        // Let's try to compile and see.
        // If it won't compile, we won't install the package
        Path packagePath = Paths.get(packageStr);
        BLangCompiler.compile(sourceRootPath, packagePath);
        Path srcDirectoryPath = BLangPrograms.validateAndResolveSourcePath(sourceRootPath, packagePath);

        Path packageName = BLangPackages.convertToPackageName(packagePath);
        Path targetDirectoryPath = initializeUserRepository()
                .resolve(USER_REPO_ARTIFACTS_DIRNAME)
                .resolve(USER_REPO_SRC_DIRNAME)
                .resolve(packageName);

        try {
            Files.list(srcDirectoryPath)
                    .filter(filePath -> !Files.isDirectory(filePath, LinkOption.NOFOLLOW_LINKS))
                    .filter(filePath -> filePath.toString().endsWith(BLANG_SRC_FILE_SUFFIX))
                    .forEach(filePath -> {
                        // Here we get the absolute path.
                        // Just get the file name and copy.
                        Path srcNamePath = filePath.getFileName();
                        Path targetFilePath = targetDirectoryPath.resolve(srcNamePath);

                        CopyOption[] options = new CopyOption[]{
                                StandardCopyOption.REPLACE_EXISTING,
                                StandardCopyOption.COPY_ATTRIBUTES,
                                LinkOption.NOFOLLOW_LINKS
                        };
                        try {
                            if (Files.exists(targetDirectoryPath, LinkOption.NOFOLLOW_LINKS) &&
                                    !Files.isDirectory(targetDirectoryPath, LinkOption.NOFOLLOW_LINKS)) {
                                throw new RuntimeException("a file exists with the same name as the package name: " +
                                        targetDirectoryPath.toString());
                            } else if (!Files.exists(targetDirectoryPath, LinkOption.NOFOLLOW_LINKS)) {
                                Files.createDirectory(targetDirectoryPath);
                            }

                            Files.copy(filePath, targetFilePath, options);
                        } catch (IOException e) {
                            throw new RuntimeException("error installing package: " + packageStr +
                                    ": " + e.getMessage(), e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("error installing package: " + packageStr +
                    ": " + e.getMessage(), e);
        }
    }

    public static void uninstallSourcePackage(String packageStr) {
        Path packageName = BLangPackages.convertToPackageName(Paths.get(packageStr));
        Path userRepoSrcPath = initializeUserRepository()
                .resolve(USER_REPO_ARTIFACTS_DIRNAME)
                .resolve(USER_REPO_SRC_DIRNAME);

        Path dirPathToDelete = userRepoSrcPath.resolve(packageName);
        if (Files.exists(dirPathToDelete, LinkOption.NOFOLLOW_LINKS) &&
                !Files.isDirectory(dirPathToDelete, LinkOption.NOFOLLOW_LINKS)) {
            throw new RuntimeException("a file exists with the same name as the package name: " +
                    dirPathToDelete.toString());
        } else if (!Files.exists(dirPathToDelete, LinkOption.NOFOLLOW_LINKS)) {
            // File doesn't exists
            throw new RuntimeException("package does not exist: " +
                    dirPathToDelete.toString());
        }

        try {
            Files.list(dirPathToDelete)
                    .filter(filePath -> !Files.isDirectory(filePath, LinkOption.NOFOLLOW_LINKS))
                    .filter(filePath -> filePath.toString().endsWith(BLANG_SRC_FILE_SUFFIX))
                    .forEach(filePath -> {
                        try {
                            Files.delete(filePath);
                        } catch (IOException e) {
                            throw new RuntimeException("error uninstalling package: " + packageStr +
                                    ": " + e.getMessage(), e);
                        }
                    });
            
            Files.delete(dirPathToDelete);
        } catch (DirectoryNotEmptyException e) {
            throw new RuntimeException("error uninstalling package: " + packageStr +
                    ": directory not empty: " + dirPathToDelete.toString(), e);
        } catch (IOException e) {
            throw new RuntimeException("error uninstalling package: " + packageStr +
                    ": " + e.getMessage(), e);
        }
    }
}
