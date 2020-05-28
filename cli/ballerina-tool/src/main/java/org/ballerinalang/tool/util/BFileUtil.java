/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.tool.util;

import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Utility methods for doing file operations.
 *
 * @since 0.975.0
 */
public class BFileUtil {

    private static final String IGNORE = ".gitignore";

    /**
     * Copy a file or directory to a target location.
     *
     * @param sourcePath File or directory to be copied
     * @param targetPath Target location
     */
    public static void copy(Path sourcePath, Path targetPath) {
        try {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (Files.exists(dir)) {
                        Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!IGNORE.equals(file.getFileName().toString()) && Files.exists(file)) {
                        CopyOption[] option = {
                                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES
                        };
                        Files.copy(file, targetPath.resolve(sourcePath.relativize(file)), option);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new BLangRuntimeException(
                    "error occured while copying from '" + sourcePath + "' " + "to '" + targetPath + "'", e);
        }
    }

    /**
     * Delete a file or directory.
     *
     * @param path Path to the file or directory
     */
    public static void delete(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (Files.exists(file)) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (Files.exists(dir)) {
                        Files.list(dir).forEach(BFileUtil::delete);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new BLangRuntimeException("error occurred while deleting '" + path + "'", e);
        }
    }

    /**
     * Provides Qualified Class Name.
     *
     * @param orgName     Org name
     * @param packageName Package name
     * @param version Package version
     * @param className   Class name
     * @return Qualified class name
     */
    public static String getQualifiedClassName(String orgName, String packageName, String version,
                                               String className) {


        if (!Names.DEFAULT_PACKAGE.value.equals(packageName)) {
            className = packageName.replace('.', '_') + "." + version.replace('.', '_') + "." + className;
        }

        if (!Names.ANON_ORG.value.equals(orgName)) {
            className = orgName.replace('.', '_') + "." +  className;
        }

        return className;
    }
}
