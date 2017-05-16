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

import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Common class for providing internal/external repository implementations.
 *
 * @since 0.8.7
 */
public class BuiltinExtendedPackageRepository extends BuiltinPackageRepository {

    private static final String RELATIVE_PATH = "META-INF/natives/";
    private static final String BAL_FILE_EXT = ".bal";
    private static final String JAR_FILE_EXT = ".jar";


    public BuiltinExtendedPackageRepository(Class providerClass) {
        super(providerClass);
    }

    private List<String> getPackageNamesFromClasspath(String repoUrl) {
        List<String> builtInPackages = new ArrayList<String>();
        Path source = Paths.get(repoUrl);
        try {
            Files.walkFileTree(source, new PackageFinder(source, builtInPackages));
        } catch (IOException e) {
            throw new BallerinaException("error while reading built-in packages: " + e.getMessage());
        }
        return builtInPackages;
    }

    public String[] loadPackageNames() {
        List<String> builtInPackages = getFileNames(this.getClass(), RELATIVE_PATH);
        String[] array = new String[builtInPackages.size()];
        builtInPackages.toArray(array);
        return array;
    }

    private List<String> getFileNames(Class classLoader, String relativePath) {
        String repoUrl = classLoader.getProtectionDomain().getCodeSource().getLocation().getPath();
        String pkgRelPath = relativePath;
        if (repoUrl.endsWith(JAR_FILE_EXT)) {
            return getPackageNamesFromJar(repoUrl, pkgRelPath);
        } else {
            return getPackageNamesFromClasspath(repoUrl);
        }
    }

    private List<String> getPackageNamesFromJar(String repoUrl, String pkgRelPath) {
        List<String> fileNames = new ArrayList<String>();
        ZipInputStream jarInputStream = null;
        ZipEntry fileNameEntry;
        try {
            jarInputStream = new ZipInputStream(new FileInputStream(repoUrl));
            while ((fileNameEntry = jarInputStream.getNextEntry()) != null) {
                String filePath = fileNameEntry.getName();
                if (filePath.matches(pkgRelPath + ".*" + BAL_FILE_EXT)) {
                    String fileName = Paths.get(pkgRelPath).relativize(Paths.get(filePath).getParent())
                            .toString().replace(File.separator, ".");
                    fileNames.add(fileName);
                }
            }
        } catch (IOException e) {
            throw new BallerinaException("error while loading built-in packages: " + e.getMessage());
        } finally {
            if (jarInputStream != null) {
                try {
                    jarInputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
        return fileNames;
    }

    //TODO remove duplication with ballerina-native code
    class PackageFinder extends SimpleFileVisitor<Path> {

        private Path basePath;
        private List<String> builtInPackages;

        public PackageFinder(Path basePath, List<String> builtInPackages) {
            this.basePath = basePath;
            this.builtInPackages = builtInPackages;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String pkg = basePath.relativize(file.getParent()).toString().replace(File.separator, ".");
            builtInPackages.add(pkg);
            return FileVisitResult.CONTINUE;
        }
    }

}
