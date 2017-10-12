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

import org.ballerinalang.util.BLangConstants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@code BLangProgramArchive} reads package information from ballerina program archive files.
 *
 * @since 0.8.0
 */
public class BLangProgramArchive extends PackageRepository implements AutoCloseable {
    public static final String BAL_INF_DIR_NAME = "BAL_INF";
    public static final String BALLERINA_CONF = "ballerina.conf";
    public static final String BALLERINA_CONF_FILE_PATH = "/" + BAL_INF_DIR_NAME + "/" + BALLERINA_CONF;
    public static final String BAL_VERSION_TEXT = "ballerina-version: 0.8.0";
    public static final String MAIN_PACKAGE_LINE_PREFIX = "main-function";
    public static final String SERVICE_PACKAGE_PREFIX = "services";

    private Path archivePath;
    private Map<String, List<Path>> packageFilesMap;
    private FileSystem zipFS;
    private String[] entryPoints;

    public BLangProgramArchive(Path archivePath, BuiltinPackageRepository[] builtinPackageRepositories) {
        this.archivePath = archivePath;
        this.builtinPackageRepositories = builtinPackageRepositories;
    }

    public String[] getEntryPoints() {
        return entryPoints;
    }

    @Override
    public PackageSource loadPackage(Path packageDirPath) {
        // First try to load from the built-in repositories 
//        PackageSource pkgSource = loadPackageFromSystemRepo(packageDirPath);
//        if (pkgSource != null) {
//            return pkgSource;
//        }

        Path zipPkgPath = zipFS.getPath("/", packageDirPath.toString());
        List<Path> pathList = packageFilesMap.get(zipPkgPath.toString());
        Map<String, InputStream> fileStreamMap;
        fileStreamMap = pathList.stream()
                .filter(filePath -> filePath.toString().endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX))
                .collect(Collectors.toMap(filePath -> filePath.getFileName().toString(), this::getInputStream));

        return new PackageSource(packageDirPath, fileStreamMap, this);
    }

    @Override
    public PackageSource loadFile(Path filePath) {
        List<Path> pathList = packageFilesMap.get("/");
        Path path = pathList.get(0);

        InputStream inputStream = getInputStream(path);
        Map<String, InputStream> fileStreamMap = new HashMap<>(1);
        fileStreamMap.put(filePath.getFileName().toString(), inputStream);
        return new PackageSource(Paths.get("."), fileStreamMap, this);
    }

    @Override
    public void close() throws Exception {
        if (zipFS != null) {
            zipFS.close();
        }

        zipFS = null;
    }

    public InputStream getInputStream(Path path) {
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new RuntimeException("error reading from file: " + archivePath +
                    " reason: " + e.getMessage(), e);
        }
    }
}
