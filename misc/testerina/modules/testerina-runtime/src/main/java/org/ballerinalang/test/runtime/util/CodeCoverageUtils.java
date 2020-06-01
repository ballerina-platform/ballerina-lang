/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.runtime.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Class containing utility methods required to generate the coverage report.
 *
 * @since 1.2.0
 */
public class CodeCoverageUtils {

    /**
     * Util method to extract required class files for code coverage analysis.
     *
     * @param source path of testable jar
     * @param destination path to extract the classes
     * @param orgName org name of the project being executed
     * @param moduleName name of the module being executed
     * @param version version of the module being executed
     */
    public static void unzipCompiledSource(Path source, Path destination, String orgName, String moduleName,
                                           String version) {
        String destJarDir = destination.resolve(source.getFileName()).toString();

        try (JarFile jarFile = new JarFile(source.toFile())) {
            Enumeration<JarEntry> enu = jarFile.entries();
            while (enu.hasMoreElements()) {
                JarEntry entry = enu.nextElement();
                File file = new File(destJarDir, entry.getName());
                if (isRequiredFile(entry.getName(), orgName, moduleName, version)) {
                    if (!file.exists()) {
                        Files.createDirectories(file.getParentFile().toPath());
                    }
                    if (entry.isDirectory()) {
                        continue;
                    }
                    InputStream is = jarFile.getInputStream(entry);
                    Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

            }
            copyClassFilesToBinPath(destination, destJarDir, orgName, moduleName, version);
            deleteDirectory(new File(destJarDir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void copyClassFilesToBinPath(Path destination, String destJarDir, String orgName,
                                                String moduleName, String version) throws IOException {
        Path extractedPath;
        Path binClassDirPath;
        if (TesterinaConstants.DOT.equals(moduleName)) {
            extractedPath = Paths.get(destJarDir);
            binClassDirPath = destination.resolve(TesterinaConstants.BIN_DIR);
        } else {
            extractedPath = Paths.get(destJarDir).resolve(orgName).resolve(moduleName);
            binClassDirPath = destination.resolve(TesterinaConstants.BIN_DIR).resolve(moduleName);
        }

        if (binClassDirPath.toFile().exists()) {
            deleteDirectory(binClassDirPath.toFile());
        }
        Files.createDirectories(binClassDirPath);
        Files.move(extractedPath, binClassDirPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private static boolean isRequiredFile(String path, String orgName, String moduleName, String version) {
        if (path.contains("___init") || path.contains("META-INF") || path.contains("tests/")) {
            return false;
        } else if (path.contains("Frame") && path.contains("module")) {
            return false;
        } else if (path.contains("Frame") && path.contains(orgName)) {
            return false;
        } else if (path.contains(orgName + "/" + moduleName + "/" + version.replace(".", "_") + "/" + moduleName +
                                         ".class")) {
            return false;
        }
        return true;
    }

    /**
     * Deletes a provided directory.
     *
     * @param dir directory to delete
     * @throws IOException if deletion fails
     */
    public static void deleteDirectory(File dir) throws IOException {
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    deleteDirectory(f);
                }
            }
        }
        Files.deleteIfExists(dir.toPath());
    }

    /**
     * Extracts the testerina report zip from resources to a given destination.
     *
     * @param source zip stream
     * @param target target directory
     * @throws IOException if extraction failed
     */
    public static void unzipReportResources(InputStream source, File target) throws IOException {
        final ZipInputStream zipStream = new ZipInputStream(source);
        ZipEntry nextEntry;
        while ((nextEntry = zipStream.getNextEntry()) != null) {
            final String name = nextEntry.getName();
            // only extract files
            if (!name.endsWith("/")) {
                final File nextFile = new File(target, name);

                // create directories
                final File parent = nextFile.getParentFile();
                if (parent != null) {
                    Files.createDirectories(parent.toPath());
                }

                // write file
                try (OutputStream targetStream = new FileOutputStream(nextFile)) {
                    final int bufferSize = 4 * 1024;
                    final byte[] buffer = new byte[bufferSize];

                    int nextCount;
                    while ((nextCount = zipStream.read(buffer)) >= 0) {
                        targetStream.write(buffer, 0, nextCount);
                    }
                }
            }
        }
    }
}
