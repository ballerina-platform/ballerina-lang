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

import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.util.program.BLangPrograms;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
    private BLangProgram.Category programCategory;

    public BLangProgramArchive(Path archivePath) {
        this.archivePath = archivePath;
    }

    public String[] getEntryPoints() {
        return entryPoints;
    }

    public void loadArchive() throws IOException {
        try {
            Map<String, String> zipFSEnv = new HashMap<>();
            zipFSEnv.put("create", "false");
            URI zipFileURI = URI.create("jar:file:" + archivePath.toUri().getPath());
            zipFS = FileSystems.newFileSystem(zipFileURI, zipFSEnv);

            if (archivePath.toString().endsWith(BLangProgram.Category.MAIN_PROGRAM.getExtension())) {
                programCategory = BLangProgram.Category.MAIN_PROGRAM;
            } else {
                programCategory = BLangProgram.Category.SERVICE_PROGRAM;
            }

            // Read ballerina.conf file and get all the entry points
            // Also load all packages and the files in them.
            processArchive();
        } catch (IOException e) {
            throw new RuntimeException("error reading from file: " + archivePath +
                    " reason: " + e.getMessage(), e);
        }
    }

    @Override
    public PackageSource loadPackage(Path packageDirPath) {
        Path zipPkgPath = zipFS.getPath("/", packageDirPath.toString());
        List<Path> pathList = packageFilesMap.get(zipPkgPath.toString());
        Map<String, InputStream> fileStreamMap;
        fileStreamMap = pathList.stream()
                .filter(filePath -> filePath.toString().endsWith(BLangPrograms.BSOURCE_FILE_EXT))
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

    private InputStream getInputStream(Path path) {
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new RuntimeException("error reading from file: " + archivePath +
                    " reason: " + e.getMessage(), e);
        }
    }

    private void processArchive() throws IOException {
        final Path rootPathInArchive = zipFS.getPath("/");
        List<Path> filePathList = new ArrayList<>();

        Files.walkFileTree(rootPathInArchive, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attributes) throws IOException {
                if (filePath.toString().equals(BALLERINA_CONF_FILE_PATH)) {
                    readBallerinaConfEntry(filePath);
                    return FileVisitResult.CONTINUE;
                }

                if (filePath.getFileName().toString().endsWith(BLangPrograms.BSOURCE_FILE_EXT)) {
                    filePathList.add(filePath);
                }

                return FileVisitResult.CONTINUE;
            }
        });

        if (filePathList.isEmpty()) {
            throw new IllegalArgumentException("invalid program archive file: " + archivePath);
        }

        packageFilesMap = filePathList.stream()
                .collect(Collectors.groupingBy(path -> path.getParent().toString()));
    }

    private void readBallerinaConfEntry(Path filePath) {
        String errorMsg = "invalid program archive file: " + archivePath;

        Properties bConfProps = new Properties();
        try {
            bConfProps.load(Files.newInputStream(filePath));
        } catch (IOException e) {
            throw new IllegalArgumentException(errorMsg, e);
        }

        // Check whether there exist entry point line..
        Object entryPointLineObj;
        if (programCategory == BLangProgram.Category.MAIN_PROGRAM) {
            entryPointLineObj = bConfProps.get(MAIN_PACKAGE_LINE_PREFIX);
        } else {
            entryPointLineObj = bConfProps.get(SERVICE_PACKAGE_PREFIX);
        }

        if (entryPointLineObj == null) {
            throw new IllegalArgumentException(errorMsg);
        }

        String entryPointLine = (String) entryPointLineObj;
        if (entryPointLine.isEmpty()) {
            throw new IllegalArgumentException(errorMsg);
        }

        entryPoints = entryPointLine.trim().split("\\s*,\\s*");
        for (String entryPoint : entryPoints) {
            if (entryPoint.isEmpty()) {
                throw new IllegalArgumentException(errorMsg);
            }
        }
    }
}
