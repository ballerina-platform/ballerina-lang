/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.file.nativeimpl;

import org.ballerinalang.stdlib.file.utils.FileConstants;
import org.ballerinalang.stdlib.file.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/**
 * Extern function ballerina.file:copy.
 *
 * @since 1.1.0
 */
public class Copy {

    private static final Logger log = LoggerFactory.getLogger(Copy.class);


    public static Object copy(String sourcePath, String destinationPath, boolean replaceExisting) {
        Path srcPath = Paths.get(sourcePath);
        Path destPath = Paths.get(destinationPath);

        if (Files.notExists(srcPath)) {
            return FileUtils.getBallerinaError(FileConstants.FILE_NOT_FOUND_ERROR,
                    "File not found: " + sourcePath);
        }
        try {
            Files.walkFileTree(srcPath, new RecursiveFileCopyVisitor(srcPath, destPath, replaceExisting));
        } catch (IOException ex) {
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, ex);
        }
        return null;
    }

    static class RecursiveFileCopyVisitor extends SimpleFileVisitor<Path> {

        final Path source;
        final Path target;
        final boolean replaceExisting;

        RecursiveFileCopyVisitor(Path source, Path target, boolean replaceExisting) {
            this.source = source;
            this.target = target;
            this.replaceExisting = replaceExisting;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path newDirectory = target.resolve(source.relativize(dir));
            if (replaceExisting) {
                Files.copy(dir, newDirectory, StandardCopyOption.REPLACE_EXISTING);
            } else {
                try {
                    Files.copy(dir, newDirectory);
                } catch (FileAlreadyExistsException ioException) {
                    log.debug("Directory already exists in the path " + dir.toString() + ", Hence skipping " +
                            "the subtree.");
                    return SKIP_SUBTREE; // skip processing
                }
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path newFile = target.resolve(source.relativize(file));
            if (replaceExisting) {
                Files.copy(file, newFile, StandardCopyOption.REPLACE_EXISTING);
            } else {
                try {
                    Files.copy(file, newFile);
                } catch (FileAlreadyExistsException ioException) {
                    log.debug("File already exists in the path " + file.toString() + ", Hence skipping " +
                            "the subtree.");
                    return SKIP_SUBTREE; // skip processing
                }
            }
            return CONTINUE;
        }
    }
}
