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
package io.ballerina.projects.internal.repositories;

import io.ballerina.projects.CompilationCache;
import io.ballerina.projects.CompilationCacheFactory;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.Project;
import io.ballerina.projects.utils.ProjectConstants;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * An implementation of the {@code PackageCompilationCache} that is aware of the file system structure.
 * <p>
 * The structure of the repository is as bellow
 * - cache
 * - org
 * - package-name
 * - version
 * - bir
 * - mod1.bir
 * - mod2.bir
 * - jar
 * - org-package-name-version.jar
 *
 * @since 2.0.0
 */
public class FileSystemCache extends CompilationCache {
    private final Path cacheDirPath;
    private Path birPath;

    public FileSystemCache(Project project, Path cacheDirPath) {
        super(project);
        this.cacheDirPath = cacheDirPath.resolve(ProjectConstants.REPO_CACHE_DIR_NAME);
    }

    @Override
    public byte[] getBir(ModuleName moduleName) {
        Path birFilePath = getBirPath().resolve(moduleName.toString()
                + ProjectConstants.BLANG_COMPILED_PKG_BIR_EXT);
        if (Files.exists(birFilePath)) {
            try {
                return FileUtils.readFileToByteArray(birFilePath.toFile());
            } catch (IOException e) {
                // todo log
            }
        }
        return new byte[0];
    }

    @Override
    public void cacheBir(ModuleName moduleName, byte[] bir) {
        Path birFilePath = getBirPath().resolve(moduleName.toString()
                + ProjectConstants.BLANG_COMPILED_PKG_BIR_EXT);
        if (!Files.exists(birFilePath)) {
            try {
                FileUtils.writeByteArrayToFile(birFilePath.toFile(), bir);
            } catch (IOException e) {
                // todo log
            }
        }
    }

    @Override
    public Path getPlatformSpecificLibrary(ModuleName moduleName) {
        return null;
    }

    public Path getJarPath(Package aPackage) {
        String packageName = aPackage.packageName().value();
        String orgName = aPackage.packageOrg().toString();
        String version = aPackage.packageVersion().version().toString();
        return this.cacheDirPath.resolve(orgName).resolve(packageName).resolve(version)
                .resolve(ProjectConstants.REPO_JAR_CACHE_NAME);
    }

    private Path getBirPath() {
        if (birPath != null) {
            return birPath;
        }

        Package currentPkg = project.currentPackage();
        PackageDescriptor pkgDescriptor = currentPkg.packageDescriptor();
        birPath = cacheDirPath.resolve(pkgDescriptor.org().value())
                .resolve(pkgDescriptor.name().value())
                .resolve(pkgDescriptor.version().toString())
                .resolve(ProjectConstants.REPO_BIR_CACHE_NAME);
        return birPath;
    }

    /**
     * A factory that creates instances of {@code FileSystemCache} for the given project.
     *
     * @since 2.0.0
     */
    public static class FileSystemCacheFactory implements CompilationCacheFactory {
        private final Path cacheDirPath;

        public FileSystemCacheFactory(Path cacheDirPath) {
            this.cacheDirPath = cacheDirPath;
        }

        public CompilationCache createCompilationCache(Project project) {
            return new FileSystemCache(project, cacheDirPath);
        }
    }
}
