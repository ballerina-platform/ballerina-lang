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
package org.ballerinalang.util.program;

import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.repository.PackageRepository;
import org.ballerinalang.util.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.util.BLangConstants.USER_HOME;
import static org.ballerinalang.util.BLangConstants.USER_REPO_ARTIFACTS_DIRNAME;
import static org.ballerinalang.util.BLangConstants.USER_REPO_DEFAULT_DIRNAME;
import static org.ballerinalang.util.BLangConstants.USER_REPO_ENV_KEY;
import static org.ballerinalang.util.BLangConstants.USER_REPO_OBJ_DIRNAME;
import static org.ballerinalang.util.BLangConstants.USER_REPO_SRC_DIRNAME;

/**
 * This class contains a set of static methods to operate on {@code BLangProgram} objects.
 *
 * @since 0.8.0
 */
public class BLangPrograms {

    public static Path validateAndResolveSourcePath(Path programDirPath, Path sourcePath) {
        if (sourcePath == null) {
            throw new IllegalArgumentException("source package/file cannot be null");
        }

        try {
            Path realSourcePath = programDirPath.resolve(sourcePath).toRealPath();

            if (Files.isDirectory(realSourcePath, LinkOption.NOFOLLOW_LINKS)) {
                return realSourcePath;
            }

            if (!realSourcePath.toString().endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
                throw new IllegalArgumentException("invalid file: " + sourcePath);
            }

            return realSourcePath;
        } catch (NoSuchFileException x) {
            throw new IllegalArgumentException("no such file or directory: " + sourcePath);
        } catch (IOException e) {
            throw new RuntimeException("error reading from file: " + sourcePath +
                    " reason: " + e.getMessage(), e);
        }
    }

    private static UserRepository initUserRepository(PackageRepository systemRepo, PackageRepository[] extRepos) {
        Path userRepoPath;
        String userRepoDir = System.getenv(USER_REPO_ENV_KEY);
        if (userRepoDir == null || userRepoDir.isEmpty()) {
            // User has not specified the user repo path.
            // We try to initialize if there exits a directory called ".ballerina" in user home.
            String userHomeDir = System.getProperty(USER_HOME);
            if (userHomeDir == null || userHomeDir.isEmpty()) {
                // Error creating Ballerina user repository;
                // But we ignore it assuming that user has never used the user repository.
                return null;
            }

            userRepoPath = Paths.get(userHomeDir, USER_REPO_DEFAULT_DIRNAME).toAbsolutePath();
            if (Files.exists(userRepoPath) && !Files.isDirectory(userRepoPath, LinkOption.NOFOLLOW_LINKS)) {
                // User repository exists, but it is not a directory
                // Error creating Ballerina user repository
                // But we ignore it assuming that user has never used the user repository
                return null;
            } else if (!Files.exists(userRepoPath)) {
                // User repository directory does not exists
                // But we ignore it assuming that user has never used the user repository
                return null;
            }

        } else {
            // User has specified the user repo path with env variable.
            userRepoPath = Paths.get(userRepoDir);
        }

        userRepoPath = createUserRepoDirStructure(userRepoPath);
        return new UserRepository(userRepoPath, systemRepo, extRepos);
    }

    public static Path createUserRepoDirStructure(Path userRepoPath) {
        // create directory structure, if not already done.
        userRepoPath = userRepoPath.toAbsolutePath();
        if (Files.exists(userRepoPath) && !Files.isDirectory(userRepoPath, LinkOption.NOFOLLOW_LINKS)) {
            throw new RuntimeException("user repository is not a directory: " + userRepoPath.toString());
        }

        // 1) create artifacts/src directory
        Path artifactsDirPath = userRepoPath.resolve(USER_REPO_ARTIFACTS_DIRNAME);
        Path srcDirPath = artifactsDirPath.resolve(USER_REPO_SRC_DIRNAME);
        Path objDirPath = artifactsDirPath.resolve(USER_REPO_OBJ_DIRNAME);
        createDirectory(userRepoPath);
        createDirectory(artifactsDirPath);
        createDirectory(srcDirPath);
        createDirectory(objDirPath);
        return userRepoPath;
    }

    public static void createDirectory(Path dirPath) {
        try {
            if (Files.exists(dirPath) && Files.isDirectory(dirPath, LinkOption.NOFOLLOW_LINKS)) {
                return;
            } else if (Files.exists(dirPath) && !Files.isDirectory(dirPath, LinkOption.NOFOLLOW_LINKS)) {
                throw new RuntimeException("error creating user repository: a file with same name as " +
                        "the directory exists: '" + dirPath.toString() + "'");
            }

            Files.createDirectory(dirPath);
        } catch (IOException e) {
            throw new RuntimeException("error creating Ballerina user repository: " + e.getMessage());
        }
    }
}
