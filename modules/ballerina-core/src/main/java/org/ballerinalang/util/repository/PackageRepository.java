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
package org.ballerinalang.util.repository;

import org.ballerinalang.util.program.BLangPackages;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.util.BLangConstants.BLANG_SRC_FILE_SUFFIX;

/**
 * {@code PackageRepository} represents a repository of ballerina packages.
 *
 * @since 0.8.0
 */
public abstract class PackageRepository {
    protected BuiltinPackageRepository[] builtinPackageRepositories;
    protected PackageRepository systemRepo;
    protected PackageRepository[] extensionRepos = new PackageRepository[0];

    public abstract PackageSource loadPackage(Path packageDirPath);

    public abstract PackageSource loadFile(Path filePath);

    public void setSystemRepository(PackageRepository systemRepository) {
        this.systemRepo = systemRepository;
    }

    protected InputStream getInputStream(Path filePath) {
        try {
            return Files.newInputStream(filePath, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            throw new RuntimeException("error reading from file: " + filePath +
                    " reason: " + e.getMessage(), e);
        }
    }

    protected PackageSource loadPackageFromDirectory(Path packageDirPath, Path baseDirPath) {
        Map<String, InputStream> fileStreamMap;
        try {
            fileStreamMap = Files.list(baseDirPath.resolve(packageDirPath))
                    .filter(filePath -> filePath.toString().endsWith(BLANG_SRC_FILE_SUFFIX))
                    .collect(Collectors.toMap(filePath -> filePath.getFileName().toString(),
                            this::getInputStream));
        } catch (NoSuchFileException e) {
            throw new RuntimeException("cannot resolve package: " +
                    BLangPackages.convertToPackageName(packageDirPath).toString(), e);
        } catch (NotDirectoryException e) {
            throw new RuntimeException("error while resolving package: " +
                    BLangPackages.convertToPackageName(packageDirPath).toString() +
                    ": a file exists with the same name as the package name: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("error while resolving package: " +
                    BLangPackages.convertToPackageName(packageDirPath).toString(), e);
        }

        return new PackageSource(packageDirPath, fileStreamMap, this);
    }

    protected PackageSource loadPackageFromBuiltinRepos(Path packageDirPath) {
        // Load from the system repository if the first part of the package is "ballerina"
        PackageSource pkgSource;
        if (packageDirPath.getName(0).toString().equals(BALLERINA_BUILTIN_PKG_PREFIX) &&
                systemRepo != null) {
            pkgSource = systemRepo.loadPackage(packageDirPath);
            if (pkgSource != null) {
                return pkgSource;
            }
        }

        for (PackageRepository extRepo : extensionRepos) {
            pkgSource = extRepo.loadPackage(packageDirPath);
            if (pkgSource != null) {
                return pkgSource;
            }
        }
        return null;
    }

    /**
     * This class contains data that are required to load packages.
     *
     * @since 0.8.0
     */
    public class PackageSource {
        private Path packagePath;
        private Map<String, InputStream> sourceFileStreamMap;
        private PackageRepository packageRepository;

        public PackageSource(Path packagePath,
                             Map<String, InputStream> sourceFileStreamMap,
                             PackageRepository packageRepository) {
            this.packagePath = packagePath;
            this.sourceFileStreamMap = sourceFileStreamMap;
            this.packageRepository = packageRepository;
        }

        public PackageRepository getPackageRepository() {
            return packageRepository;
        }

        public Path getPackagePath() {
            return packagePath;
        }

        public Map<String, InputStream> getSourceFileStreamMap() {
            return sourceFileStreamMap;
        }
    }
}
