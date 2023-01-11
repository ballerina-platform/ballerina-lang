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
package io.ballerina.projects.internal.model;

import io.ballerina.projects.Package;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents the target directory model.
 *
 * @since 2.0.0
 */
public class Target {
    private final Path targetPath;
    private Path outputPath = null;
    private Path cache;
    private Path jarCachePath;
    private Path balaCachePath;
    private Path birCachePath;
    private Path testsCachePath;
    private Path binPath;
    private Path reportPath;
    private Path docPath;
    private Path nativePath;
    private Path nativeConfigPath;

    public Target(Path targetPath) throws IOException {
        this.targetPath = targetPath;
        this.cache = this.targetPath.resolve(ProjectConstants.CACHES_DIR_NAME);
        this.balaCachePath = this.targetPath.resolve(ProjectConstants.TARGET_BALA_DIR_NAME);
        this.jarCachePath = this.cache.resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME);
        this.birCachePath = this.cache.resolve(ProjectConstants.BIR_CACHE_DIR_NAME);
        this.testsCachePath = this.cache.resolve(ProjectConstants.TESTS_CACHE_DIR_NAME);
        this.binPath = this.targetPath.resolve(ProjectConstants.BIN_DIR_NAME);
        this.reportPath = this.targetPath.resolve(ProjectConstants.REPORT_DIR_NAME);
        this.docPath = this.targetPath.resolve(ProjectConstants.TARGET_API_DOC_DIRECTORY);
        this.nativePath = this.targetPath.resolve(ProjectConstants.NATIVE_DIR_NAME);
        this.nativeConfigPath = this.testsCachePath.resolve(ProjectConstants.NATIVE_CONFIG_DIR_NAME);

        if (Files.exists(this.targetPath)) {
            ProjectUtils.checkWritePermission(this.targetPath);
        } else {
            Files.createDirectories(this.targetPath);
        }

        if (Files.exists(this.cache)) {
            ProjectUtils.checkWritePermission(this.cache);
        }
        if (Files.exists(this.binPath)) {
            ProjectUtils.checkWritePermission(this.binPath);
        }
        if (Files.exists(this.balaCachePath)) {
            ProjectUtils.checkWritePermission(this.balaCachePath);
        }
        if (Files.exists(this.docPath)) {
            ProjectUtils.checkWritePermission(this.docPath);
        }

        if (Files.exists(this.reportPath)) {
            ProjectUtils.checkWritePermission(this.reportPath);
        }
    }

    /**
     * Returns the doc target path.
     *
     * @return path of the api doc directory
     */
    public Path getDocPath() throws IOException {
        Files.createDirectories(docPath);
        return docPath;
    }

    /**
     * Returns the bala dir path.
     *
     * @return path of the bala file
     */
    public Path getBalaPath() throws IOException {
        Files.createDirectories(balaCachePath);
        return balaCachePath;
    }

    /**
     * Returns the jar-cache path.
     *
     * @return path of the executable
     */
    public Path getJarCachePath() throws IOException {
        Files.createDirectories(jarCachePath);
        return jarCachePath;
    }

    /**
     * Returns the path of the executable jar.
     *
     * @param pkg Package instance
     * @return the path of the executable
     */
    public Path getExecutablePath(Package pkg) throws IOException {
        if (outputPath != null) {
            return outputPath;
        }
        return getBinPath().resolve(ProjectUtils.getExecutableName(pkg));
    }

    /**
     * Returns the bin directory path.
     *
     * @return bin path
     */
    public Path getBinPath() throws IOException {
        Files.createDirectories(binPath);
        return binPath;
    }

    public Path getReportPath() throws IOException {
        Files.createDirectories(reportPath);
        return reportPath;
    }

    /**
     * Returns the caches directory path.
     *
     * @return caches path
     */
    public Path cachesPath() {
        return this.targetPath.resolve(ProjectConstants.CACHES_DIR_NAME);
    }

    /**
     * Returns the bir-cache directory path.
     *
     * @return caches path
     */
    public Path getBirCachePath() throws IOException {
        Files.createDirectories(birCachePath);
        return birCachePath;
    }

    /**
     * Returns the tests-cache directory path.
     *
     * @return caches path
     */
    public Path getTestsCachePath() throws IOException {
        Files.createDirectories(testsCachePath);
        return testsCachePath;
    }

    /**
     * Returns the path of the target directory.
     *
     * @return target path
     */
    public Path path() {
        return this.targetPath;
    }

    /**
     * Sets a custom path as the executable jar path.
     *
     * @param outputPath path to set for the executable jar
     * @throws IOException if directory creation fails
     */
    public void setOutputPath(Path outputPath) throws IOException {
        if (Files.exists(outputPath)) {
            ProjectUtils.checkWritePermission(outputPath);
            Files.delete(outputPath);
        }
        // create parent directories
        Path parent = outputPath.getParent();
        if (parent != null) {
            if (Files.exists(parent)) {
                ProjectUtils.checkWritePermission(parent);
            }
            Files.createDirectories(parent);
        }
        this.outputPath = outputPath;
    }

    /**
     * Clean any files that created from the build.
     */
    public void clean(boolean isModified, boolean cacheEnabled) throws IOException {
        if (isModified || !cacheEnabled) {
            // Remove from cache
            ProjectUtils.deleteDirectory(this.cache);
        }

        // Remove any generated bala
        ProjectUtils.deleteDirectory(this.balaCachePath);
        ProjectUtils.deleteDirectory(this.binPath);
        ProjectUtils.deleteDirectory(this.docPath);
        ProjectUtils.deleteDirectory(this.reportPath);
    }

    /**
     * Clean cache files that created from the build.
     */
    public void cleanCache() throws IOException {
        // Remove from cache
        ProjectUtils.deleteDirectory(this.cache);
    }

    public Path getNativePath() throws IOException {
        Files.createDirectories(nativePath);
        return nativePath;
    }

    public Path getNativeConfigPath() throws IOException {
        Files.createDirectories(nativeConfigPath);
        return nativeConfigPath;
    }
}
