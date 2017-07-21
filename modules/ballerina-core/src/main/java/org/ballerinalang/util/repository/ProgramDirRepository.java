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

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * This class loads ballerina packages and files from the file system.
 *
 * @since 0.8.0
 */
public class ProgramDirRepository extends PackageRepository {
    private Path sourceRootPath;
    private UserRepository userRepo;

    public ProgramDirRepository(Path sourceRootPath,
                                PackageRepository systemRepository,
                                PackageRepository[] extensionRepos,
                                UserRepository userRepository) {
        this.sourceRootPath = sourceRootPath;
        this.systemRepo = systemRepository;
        this.extensionRepos = extensionRepos;
        this.userRepo = userRepository;
    }

    @Override
    public PackageSource loadPackage(Path packageDirPath) {
        PackageSource pkgSource = loadPackageFromBuiltinRepos(packageDirPath);
        if (pkgSource != null) {
            return pkgSource;
        }

        if (userRepo != null) {
            pkgSource = userRepo.loadPackage(packageDirPath);
            if (pkgSource != null) {
                return pkgSource;
            }
        }

        return loadPackageFromDirectory(packageDirPath, sourceRootPath);
    }

    @Override
    public PackageSource loadFile(Path filePath) {
        InputStream inputStream = getInputStream(sourceRootPath.resolve(filePath));
        Map<String, InputStream> fileStreamMap = new HashMap<>(1);
        fileStreamMap.put(filePath.getFileName().toString(), inputStream);
        return new PackageSource(Paths.get("."), fileStreamMap, this);
    }
}
