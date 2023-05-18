/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.repos;

import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.Project;
import io.ballerina.projects.util.ProjectConstants;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.ballerina.projects.util.ProjectConstants.CACHES_DIR_NAME;

/**
 * Default {@code CompilationCache} linked with the {@code BuildProject}.
 *
 * @since 2.0.0
 */
public class BuildProjectCompilationCache extends FileSystemCache {
    private Path birPath;
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private BuildProjectCompilationCache(Project project, Path cacheDirPath) {
        super(project, cacheDirPath.resolve(CACHES_DIR_NAME));
    }

    public static BuildProjectCompilationCache from(Project project) {
        Path targetPath = project.targetDir();
        return new BuildProjectCompilationCache(project, targetPath);
    }

    @Override
    public byte[] getBir(ModuleName moduleName) {
        Path birFilePath = getBirPath().resolve(moduleName.toString()
                + ProjectConstants.BLANG_COMPILED_PKG_BIR_EXT);
        if (Files.exists(birFilePath)) {
            try {
                return FileUtils.readFileToByteArray(birFilePath.toFile());
            } catch (IOException e) {
                return EMPTY_BYTE_ARRAY;
            }
        }
        return new byte[0];
    }

    private Path getBirPath() {
        if (birPath != null) {
            return birPath;
        }
        Package currentPkg = project.currentPackage();
        PackageManifest pkgDescriptor = currentPkg.manifest();
        Path targetCachePath = project.targetDir().resolve(CACHES_DIR_NAME);
        birPath = targetCachePath.resolve(pkgDescriptor.org().value())
                .resolve(pkgDescriptor.name().value())
                .resolve(pkgDescriptor.version().value().toString())
                .resolve(ProjectConstants.REPO_BIR_CACHE_NAME);
        return birPath;
    }
}
