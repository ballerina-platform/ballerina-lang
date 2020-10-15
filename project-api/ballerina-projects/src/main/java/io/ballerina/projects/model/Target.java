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
package io.ballerina.projects.model;

import io.ballerina.projects.Package;
import io.ballerina.projects.utils.ProjectConstants;
import io.ballerina.projects.utils.ProjectUtils;
import org.apache.commons.io.FileUtils;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

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
    private Path baloCachePath;
    private Path birCachePath;
    private Path binPath;

    public Target(Path sourceRoot) throws IOException {
        this.targetPath = sourceRoot.resolve(ProjectConstants.TARGET_DIR_NAME);
        this.cache = this.targetPath.resolve(ProjectConstants.CACHES_DIR_NAME);
        this.baloCachePath = this.targetPath.resolve(ProjectConstants.TARGET_BALO_DIR_NAME);
        this.jarCachePath = this.cache.resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME);
        this.birCachePath = this.cache.resolve(ProjectConstants.BIR_CACHE_DIR_NAME);
        this.binPath = this.targetPath.resolve(ProjectConstants.BIN_DIR_NAME);
        Files.createDirectories(this.targetPath);
    }

    /**
     * Returns the balo cache path.
     *
     * @return path of the balo file
     */
    public Path getBaloPath() throws IOException {
        Files.createDirectories(baloCachePath);
        return baloCachePath;
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
    public Path getExecutablePath(Package pkg) {
        if (outputPath != null) {
            return outputPath;
        }
        return this.binPath.resolve(ProjectUtils.getExecutableName(pkg));
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
     * @param pkg Package instance
     * @param outputPath path to set for the executable jar
     * @throws IOException if directory creation fails
     */
    public void setOutputPath(Package pkg, Path outputPath) throws IOException {
        if (this.outputPath.isAbsolute()) {
            if (Files.isDirectory(this.outputPath)) {
                if (Files.notExists(this.outputPath)) {
                    Files.createDirectories(this.outputPath);
                }

                String outputFileName = ProjectUtils.getJarName(pkg);
                this.outputPath = this.outputPath.resolve(outputFileName);
            } else {
                if (!this.outputPath.toString().endsWith(BLANG_COMPILED_JAR_EXT)) {
                    this.outputPath = Paths.get(this.outputPath.toString() + BLANG_COMPILED_JAR_EXT);
                }
            }
        } else {
            Path userDir = Paths.get(System.getProperty("user.dir"));
            if (io.ballerina.projects.utils.FileUtils.hasExtension(this.outputPath)) {
                this.outputPath = userDir.resolve(this.outputPath);
            } else {
                this.outputPath = userDir.resolve(this.outputPath + BLANG_COMPILED_JAR_EXT);
            }
        }
        this.outputPath = outputPath;
    }
    /**
     * Clean any files that created from the build.
     *
     */
    public void clean() throws IOException {
        // Remove from cache
        FileUtils.deleteDirectory(this.cache.toFile());
        // Remove any generated balo
        FileUtils.deleteDirectory(this.baloCachePath.toFile());
        FileUtils.deleteDirectory(this.binPath.toFile());
    }
}
