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
package org.wso2.ballerina.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 *
 * @since 0.8.0
 */
public abstract class PackageRepository {

    public abstract PackageSource loadPackage(Path packageDirPath);

    protected PackageSource loadPackageFromDirectory(Path baseDirPath, Path packageDirPath) {

        // TODO construct the package-path from the give Path object
        // E.g. org/sameera/calc -> org.sameera.calc
        String pkgPathStr = replaceDelimiterWithDots(packageDirPath);

        Map<String, InputStream> fileStreamMap;
        try {
            fileStreamMap = Files.list(baseDirPath.resolve(packageDirPath))
                    .filter(filePath -> filePath.toString().endsWith(".bal"))
                    .collect(Collectors.toMap(filePath -> filePath.getFileName().toString(), this::getInputStream));
        } catch (IOException e) {
            // TODO Handle error
            throw new IllegalStateException(e.getMessage(), e);
        }

        return new PackageSource(packageDirPath, fileStreamMap, this);
    }

    private InputStream getInputStream(Path filePath) {
        try {
            return Files.newInputStream(filePath, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            // TODO Handle error
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    // TODO Remove duplicates
    private static String replaceDelimiterWithDots(Path path) {
        if (path.getNameCount() == 1) {
            return path.toString();
        }

        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < path.getNameCount() - 1; i++) {
            strBuilder.append(path.getName(i)).append(".");
        }

        strBuilder.append(path.getName(path.getNameCount() - 1));
        return strBuilder.toString();
    }

    /**
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
