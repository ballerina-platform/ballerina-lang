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

import java.nio.file.Files;
import java.nio.file.Path;

import static org.ballerinalang.util.BLangConstants.USER_REPO_ARTIFACTS_DIRNAME;
import static org.ballerinalang.util.BLangConstants.USER_REPO_SRC_DIRNAME;

/**
 * Represents the Ballerina user repository.
 *
 * @since 0.90
 */
public class UserRepository extends PackageRepository {

    private Path repositoryPath;
    private Path srcRepoPath;

    public UserRepository(Path userRepositoryPath,
                          PackageRepository systemRepository,
                          PackageRepository[] extensionRepos) {
        this.repositoryPath = userRepositoryPath;
        this.systemRepo = systemRepository;
        this.extensionRepos = extensionRepos;

        srcRepoPath = repositoryPath.resolve(USER_REPO_ARTIFACTS_DIRNAME).resolve(USER_REPO_SRC_DIRNAME);
    }

    @Override
    public PackageSource loadPackage(Path packageDirPath) {
        PackageSource pkgSource = loadPackageFromBuiltinRepos(packageDirPath);
        if (pkgSource != null) {
            return pkgSource;
        }

        // Replace file separator with dots.
        Path packageNamePath = BLangPackages.convertToPackageName(packageDirPath);
        Path absPath = srcRepoPath.resolve(packageNamePath);
        if (!Files.exists(absPath)) {
            return null;
        }

        return loadPackageFromDirectory(packageNamePath, srcRepoPath);
    }

    @Override
    public PackageSource loadFile(Path filePath) {
        return null;
    }
}
