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

import io.ballerina.projects.Module;
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
import static io.ballerina.projects.utils.ProjectConstants.BLANG_COMPILED_PKG_BIR_EXT;

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

    public Target(Path sourceRoot) throws IOException {
        this.targetPath = sourceRoot.resolve(ProjectConstants.TARGET_DIR_NAME);
        this.cache = this.targetPath.resolve(ProjectConstants.CACHES_DIR_NAME);
        this.baloCachePath = this.targetPath.resolve(ProjectConstants.TARGET_BALO_DIR_NAME);
        this.jarCachePath = this.cache.resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME);
        this.birCachePath = this.cache.resolve(ProjectConstants.BIR_CACHE_DIR_NAME);
        Files.createDirectories(this.targetPath);
    }

    /**
     * Returns the balo cache path.
     *
     * @param pkg Package instance
     * @return path of the balo file
     */
    public Path getBaloPath(Package pkg) throws IOException {
        // todo move balo name to a util
        /* String baloFileName = ProjectUtils.getBaloName(pkg.packageOrg().toString(), pkg.packageName().toString(),
                pkg.packageVersion().toString(), null);*/
        Files.createDirectories(baloCachePath);
        return baloCachePath;
    }

    /**
     * Returns the BIR cache path.
     *
     * @param module module instance
     * @return path of the BIR file
     * @throws IOException if directory creation fails
     */
    public Path getBirCachePath(Module module) throws IOException {

        Path moduleBirCacheDirPrefix = Files.createDirectories(birCachePath)
                .resolve(module.packageInstance().packageOrg().toString())
                .resolve(module.moduleName().packageName().toString())
                .resolve(module.packageInstance().packageVersion().toString());
        String moduleBirName;
        if (!module.isDefaultModule()) {
            moduleBirName = module.moduleName().moduleNamePart();
            moduleBirCacheDirPrefix = moduleBirCacheDirPrefix.resolve(module.moduleName().moduleNamePart());
        } else {
            moduleBirName = module.moduleName().packageName().toString();
        }
        Files.createDirectories(moduleBirCacheDirPrefix);
        return moduleBirCacheDirPrefix.resolve(
                moduleBirName + BLANG_COMPILED_PKG_BIR_EXT);
    }

    /**
     * Returns the jar path.
     *
     * @param pkg package instance
     * @return path of the executable
     */
    public Path getJarPath(Package pkg) {
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
        return this.targetPath.resolve(ProjectConstants.BIN_DIR_NAME).resolve(ProjectUtils.getExecutableName(pkg));
    }

    /**
     * Returns the bin directory path.
     *
     * @return bin path
     */
    public Path binPath() {
        return this.targetPath.resolve(ProjectConstants.BIN_DIR_NAME);
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
     * Returns the caches directory path.
     *
     * @return caches path
     */
    public Path birCachePath() {
        return this.targetPath.resolve(ProjectConstants.CACHES_DIR_NAME).resolve(ProjectConstants.BIR_CACHE_DIR_NAME);
    }

    /**
     * Returns the balo directory path.
     *
     * @return balo path
     */
    public Path baloPath() {
        return targetPath.resolve(ProjectConstants.BALO_DIR_NAME);
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
    }
}
