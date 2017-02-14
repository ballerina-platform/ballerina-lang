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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
import java.util.zip.ZipFile;

/**
 * @since 0.8.0
 */
public class BLangProgramArchive extends PackageRepository implements AutoCloseable {
    private Path archivePath;
    private ZipFile zipFile;
    private Map<String, List<Path>> packageFilesMap;
    private Properties bConfProps;
    private FileSystem zipFS;

    public BLangProgramArchive(Path archivePath) {
        this.archivePath = archivePath;
    }

    public String getEntryPoint() {
        return bConfProps.get("main-function").toString().trim();
    }

    public void loadArchive() throws IOException {
        try {
            archivePath.toRealPath(LinkOption.NOFOLLOW_LINKS);

            if (!Files.isReadable(archivePath)) {
                // TODO Handle Error
                throw new IllegalStateException("no read access provided for the archive ");
            }

            if (Files.isDirectory(archivePath, LinkOption.NOFOLLOW_LINKS)) {
                // TODO Handle Error
                throw new IllegalStateException("required ballerina program archive (.bpz) file");
            }

            Map<String, String> zipFSEnv = new HashMap<>();
            zipFSEnv.put("create", "false");

            URI zipFileURI = URI.create("jar:file:" + archivePath.toUri().getPath());
            zipFS = FileSystems.newFileSystem(zipFileURI, zipFSEnv);
            readZipFile();


            // TODO Perform validations for various sutff
        } catch (IOException e) {
            // TODO Handle error
            throw e;
        }
    }

    @Override
    public PackageSource loadPackage(Path packageDirPath) {
        Path zipPkgPath = zipFS.getPath("/", packageDirPath.toString());
        List<Path> pathList = packageFilesMap.get(zipPkgPath.toString());
        Map<String, InputStream> fileStreamMap;
        fileStreamMap = pathList.stream()
                .filter(filePath -> filePath.toString().endsWith(".bal"))
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

    private InputStream getInputStream(Path path) {
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            // TODO
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void readZipFile() throws IOException {
        final Path root = zipFS.getPath("/");
        List<Path> filePathStrs = new ArrayList<>();

        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                System.out.printf("Extracting file %s\n", filePath);

                if (filePath.toString().equals("/BAL_INF/ballerina.conf")) {
                    readBallerinaConfEntry(filePath);
                    return FileVisitResult.CONTINUE;
                }

                if (filePath.getFileName().toString().endsWith(".bal")) {
                    filePathStrs.add(filePath);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        packageFilesMap = filePathStrs.stream()
                .collect(Collectors.groupingBy(path -> path.getParent().toString()));
    }

    private void readBallerinaConfEntry(Path filePath) {
        bConfProps = new Properties();
        try {
            bConfProps.load(Files.newInputStream(filePath));
            // TODO throw an error if the main-function is not here..
            System.out.println(bConfProps.get("main-function"));
        } catch (IOException e) {
            /// TODO
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        if (zipFS != null) {
            zipFS.close();
        }
    }
}
