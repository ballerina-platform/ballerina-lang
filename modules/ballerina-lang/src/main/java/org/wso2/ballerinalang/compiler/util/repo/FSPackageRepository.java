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
package org.wso2.ballerinalang.compiler.util.repo;

import org.wso2.ballerinalang.compiler.util.BLangPackageFile;
import org.wso2.ballerinalang.compiler.util.BLangSourcePackageFile;
import org.wso2.ballerinalang.compiler.util.BLangSourcePackageFile.BLangSourceFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 0.94
 */
public class FSPackageRepository extends BasePackageRepository {
    private Path sourceRootPath;

    public FSPackageRepository(Path sourceRootPath) {
        this.sourceRootPath = sourceRootPath;
    }

    @Override
    public BLangPackageFile loadPackage(String packageName) {
        return new BLangSourcePackageFile(loadSourceFiles(Paths.get(packageName)));
    }

    @Override
    public BLangPackageFile loadPackage(String packageName, String packageVersion) {
        return null;
    }

    protected List<BLangSourceFile> loadSourceFiles(Path packageName) {
        List<BLangSourceFile> fileStreamMap = null;
        try {
            fileStreamMap = Files.list(sourceRootPath.resolve(packageName))
                    .filter(filePath -> filePath.toString().endsWith(".bal"))
                    .map(filePath -> new BLangSourceFile(
                            filePath.getFileName().toString(),
                            getInputStream(filePath)))
                    .collect(Collectors.toList());
        } catch (NoSuchFileException e) {
//            throw new RuntimeException("cannot resolve package: " +
//                    BLangPackages.convertToPackageName(packageDirPath).toString(), e);
        } catch (NotDirectoryException e) {
//            throw new RuntimeException("error while resolving package: " +
//                    BLangPackages.convertToPackageName(packageDirPath).toString() +
//                    ": a file exists with the same name as the package name: " + e.getMessage(), e);
        } catch (IOException e) {
//            throw new RuntimeException("error while resolving package: " +
//                    BLangPackages.convertToPackageName(packageDirPath).toString(), e);
        }

        return fileStreamMap;
    }
}
