/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.test.resolution.packages.internal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Provide access to resolved file paths.
 *
 * @since 2.0.0
 */
public class TestCaseFilePaths {

    private final Path centralRepoPath;
    private final Path distRepoPath;
    private final Path localRepoDirPath;
    private final Path appPath;
    private final Path dependenciesTomlPath;
    private final Path ballerinaTomlPath;
    private final Path expectedGraphStickyPath;
    private final Path expectedGraphNoStickyPath;

    TestCaseFilePaths(Path centralRepoPath,
                      Path distRepoPath,
                      Path localRepoDirPath,
                      Path appPath,
                      Path dependenciesTomlPath,
                      Path ballerinaTomlPath,
                      Path expectedGraphStickyPath,
                      Path expectedGraphNoStickyPath) {
        this.centralRepoPath = centralRepoPath;
        this.distRepoPath = distRepoPath;
        this.localRepoDirPath = localRepoDirPath;
        this.appPath = appPath;
        this.dependenciesTomlPath = dependenciesTomlPath;
        this.ballerinaTomlPath = ballerinaTomlPath;
        this.expectedGraphStickyPath = expectedGraphStickyPath;
        this.expectedGraphNoStickyPath = expectedGraphNoStickyPath;
    }

    public Optional<Path> centralRepoPath() {
        return Optional.ofNullable(centralRepoPath);
    }

    public Optional<Path> distRepoPath() {
        return Optional.ofNullable(distRepoPath);
    }

    public Optional<Path> localRepoDirPath() {
        return Optional.ofNullable(localRepoDirPath);
    }

    public Path appPath() {
        return appPath;
    }

    public Optional<Path> ballerinaTomlPath() {
        return Optional.ofNullable(ballerinaTomlPath);
    }

    public Optional<Path> dependenciesTomlPath() {
        return Optional.ofNullable(dependenciesTomlPath);
    }

    public Optional<Path> expectedGraphStickyPath() {
        return Optional.ofNullable(expectedGraphStickyPath);
    }

    public Optional<Path> expectedGraphNoStickyPath() {
        return Optional.ofNullable(expectedGraphNoStickyPath);
    }

    /**
     * A builder class for the TestCaseFilePaths.
     * <p>
     * For each file, try to get it from the test case directory,
     * if not try to get from the test suite directory.
     *
     * @since 2.0.0
     */
    public static class TestCaseFilePathsBuilder {

        public static TestCaseFilePaths build(Path testSuitePath, Path testCasePath) {
            Path appPath = getFilePath(testSuitePath, testCasePath, Paths.get(Constants.APP_FILE_NAME));
            if (appPath == null) {
                throw new IllegalStateException(Constants.APP_FILE_NAME +
                        " cannot be found in neither testcase dir `" +
                        testCasePath + "` nor testsuite dir `" +
                        testSuitePath + "`");
            }

            Path centralRepoPath = getFilePath(testSuitePath, testCasePath,
                    Paths.get(Constants.REPO_DIR_NAME).resolve(Constants.CENTRAL_REPO_FILE_NAME));
            Path distRepoPath = getFilePath(testSuitePath, testCasePath,
                    Paths.get(Constants.REPO_DIR_NAME).resolve(Constants.DIST_REPO_FILE_NAME));
            Path localRepoDirPath = getFilePath(testSuitePath, testCasePath,
                    Paths.get(Constants.REPO_DIR_NAME).resolve(Constants.LOCAL_REPO_DIR_NAME));

            Path depsTomlPath = getFilePath(testSuitePath, testCasePath, Paths.get(Constants.DEPS_TOML_FILE_NAME));
            Path balTomlPath = getFilePath(testSuitePath, testCasePath, Paths.get(Constants.BAL_TOML_FILE_NAME));
            Path expGraphStickyPath = getFilePath(testSuitePath, testCasePath,
                    Paths.get(Constants.EXP_GRAPH_STICKY_FILE_NAME));
            Path expGraphNoStickyPath = getFilePath(testSuitePath, testCasePath,
                    Paths.get(Constants.EXP_GRAPH_NO_STICKY_FILE_NAME));

            return new TestCaseFilePaths(centralRepoPath, distRepoPath, localRepoDirPath,
                    appPath, depsTomlPath, balTomlPath, expGraphStickyPath, expGraphNoStickyPath);
        }

        private static Path getFilePath(Path testSuitePath, Path testCasePath, Path relativeFilePath) {
            // Try to get from the test case directory
            Path filePath = testCasePath.resolve(relativeFilePath);
            if (Files.exists(filePath)) {
                return filePath;
            } else {
                // If not try to get from the test suite directory
                filePath = testSuitePath.resolve(relativeFilePath);
            }

            return Files.exists(filePath) ? filePath : null;
        }
    }
}
