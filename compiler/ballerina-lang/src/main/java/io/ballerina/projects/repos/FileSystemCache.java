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

import io.ballerina.projects.CompilationCache;
import io.ballerina.projects.CompilationCacheFactory;
import io.ballerina.projects.CompilerBackend;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.Project;
import io.ballerina.projects.util.ProjectConstants;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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
    private Path packageCacheDirPath;

    public FileSystemCache(Project project, Path cacheDirPath) {
        super(project);
        this.cacheDirPath = cacheDirPath;
    }

    @Override
    public byte[] getBir(ModuleName moduleName) {
        Path birFilePath = getBirPath().resolve(moduleName.toString()
                + ProjectConstants.BLANG_COMPILED_PKG_BIR_EXT);
        if (Files.exists(birFilePath)) {
            try {
                return FileUtils.readFileToByteArray(birFilePath.toFile());
            } catch (IOException e) {
                // TODO proper error handling
                throw new RuntimeException("Failed to read the cached bir of module: " + moduleName, e);
            }
        }
        return new byte[0];
    }

    @Override
    public void cacheBir(ModuleName moduleName, ByteArrayOutputStream birContent) {
        Path birFilePath = getBirPath().resolve(moduleName.toString() + ProjectConstants.BLANG_COMPILED_PKG_BIR_EXT);
        if (!Files.exists(birFilePath)) {
            try {
                File tempBirFile = birPath.resolve(".tmp").toFile();
                // TODO Can we improve this logic
                FileUtils.writeByteArrayToFile(tempBirFile, birContent.toByteArray());
                FileUtils.moveFile(tempBirFile, birFilePath.toFile());
            } catch (IOException e) {
                // TODO proper error handling
                throw new RuntimeException("Failed to cache the bir of module: " + moduleName, e);
            }
        }
    }

    @Override
    public Optional<Path> getPlatformSpecificLibrary(CompilerBackend compilerBackend, String libraryName) {
        String libraryFileName = libraryName + compilerBackend.libraryFileExtension();
        Path targetPlatformCacheDirPath = getTargetPlatformCacheDirPath(compilerBackend);
        Path jarFilePath = targetPlatformCacheDirPath.resolve(libraryFileName);
        return Files.exists(jarFilePath) ? Optional.of(jarFilePath) : Optional.empty();
    }

    @Override
    public void cachePlatformSpecificLibrary(CompilerBackend compilerBackend,
                                             String libraryName,
                                             ByteArrayOutputStream libraryContent) {
        String libraryFileName = libraryName + compilerBackend.libraryFileExtension();
        Path targetPlatformCacheDirPath = getTargetPlatformCacheDirPath(compilerBackend);
        // Create directories
        createDirectories(targetPlatformCacheDirPath);
        Path jarFilePath = targetPlatformCacheDirPath.resolve(libraryFileName);

        // TODO Can we improve this logic
        try {
            FileUtils.writeByteArrayToFile(jarFilePath.toFile(), libraryContent.toByteArray());
        } catch (IOException e) {
            // TODO improve the error handling
            throw new RuntimeException("Failed to write library: " + jarFilePath, e);
        }
    }

    private Path getTargetPlatformCacheDirPath(CompilerBackend compilerBackend) {
        String targetPlatformCode = compilerBackend.targetPlatform().code();
        return packageCacheDirPath().resolve(targetPlatformCode);
    }

    private void createDirectories(Path dirPath) {
        if (Files.exists(dirPath)) {
            return;
        }

        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            // TODO improve the error handling
            throw new RuntimeException("Failed to create directory: " + dirPath, e);
        }
    }

    private Path getBirPath() {
        if (birPath != null) {
            return birPath;
        }

        birPath = packageCacheDirPath().resolve(ProjectConstants.REPO_BIR_CACHE_NAME);
        return birPath;
    }

    private Path packageCacheDirPath() {
        if (packageCacheDirPath != null) {
            return packageCacheDirPath;
        }

        // TODO Update the following logic to support singleBalFileCase
        Package currentPkg = project.currentPackage();
        PackageManifest pkgDescriptor = currentPkg.manifest();
        packageCacheDirPath = cacheDirPath.resolve(pkgDescriptor.org().value())
                .resolve(pkgDescriptor.name().value())
                .resolve(pkgDescriptor.version().toString());
        return packageCacheDirPath;
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
