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

import io.ballerina.build.Module;
import io.ballerina.build.Package;
import io.ballerina.projects.utils.ProjectConstants;
import io.ballerina.projects.utils.ProjectUtils;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.ballerina.projects.utils.ProjectConstants.BLANG_COMPILED_JAR_EXT;
import static io.ballerina.projects.utils.ProjectConstants.BLANG_COMPILED_PKG_BIR_EXT;

/**
 * Represents the target directory model.
 *
 * @since 2.0.0
 */
public class Target {

    private final Path targetPath;

    public Target(Path sourceRoot) {
        this.targetPath = sourceRoot.resolve(ProjectConstants.TARGET_DIR_NAME);
    }

    /**
     * Returns the balo cache path.
     *
     * @param pkg Package instance
     * @return path of the balo file
     */
    public Path getBaloCachePath(Package pkg) {
        Path baloCachePath = this.targetPath.resolve(ProjectConstants.CACHES_DIR_NAME)
                        .resolve(ProjectConstants.BALO_CACHE_DIR_NAME);
        String baloFileName = ProjectUtils.getBaloName(pkg.packageOrg().toString(), pkg.packageName().toString(),
                pkg.packageVersion().toString(), null);

        return baloCachePath.resolve(baloFileName);
    }

    /**
     * Returns the BIR cache path.
     *
     * @param module module instance
     * @return path of the BIR file
     * @throws IOException if directory creation fails
     */
    public Path getBirCachePath(Module module) throws IOException {
        Path birCachePath = this.targetPath.resolve(ProjectConstants.CACHES_DIR_NAME)
                .resolve(ProjectConstants.BIR_CACHE_DIR_NAME);
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

        return moduleBirCacheDirPrefix.resolve(
                moduleBirName + BLANG_COMPILED_PKG_BIR_EXT);
    }

    /**
     * Returns the executable path.
     *
     * @param module module instance
     * @return path of the executable
     * @throws IOException if directory creation fails
     */
    public Path getExecutablePath(Module module) throws IOException {
        //TODO: check if there will be only one executable per package
        Path jarCachePath = this.targetPath.resolve(ProjectConstants.CACHES_DIR_NAME)
                .resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME);
        Path moduleJarCacheDirPrefix = Files.createDirectories(jarCachePath)
                .resolve(module.packageInstance().packageOrg().toString())
                .resolve(module.moduleName().packageName().toString())
                .resolve(module.packageInstance().packageVersion().toString());
        String moduleJarName;
        if (!module.isDefaultModule()) {
            moduleJarName = module.moduleName().moduleNamePart();
            moduleJarCacheDirPrefix =
                    moduleJarCacheDirPrefix.resolve(module.moduleName().moduleNamePart());
        } else {
            moduleJarName = module.moduleName().packageName().toString();
        }

        return moduleJarCacheDirPrefix.resolve(moduleJarName + BLANG_COMPILED_JAR_EXT);
    }

}
