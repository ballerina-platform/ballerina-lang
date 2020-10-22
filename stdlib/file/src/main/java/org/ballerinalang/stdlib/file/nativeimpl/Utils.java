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

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import org.ballerinalang.stdlib.file.utils.FileConstants;
import org.ballerinalang.stdlib.file.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/**
 * Native function implementations of the file module.
 *
 * @since 1.1.0
 */
public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);
    private static final String CURRENT_DIR_PROPERTY_KEY = "user.dir";
    private static final String TEMP_DIR_PROPERTY_KEY = "java.io.tmpdir";
    private static Type fileInfoType;

    public static BString getCurrentDirectory() {
        return StringUtils.fromString(FileUtils.getSystemProperty(CURRENT_DIR_PROPERTY_KEY));
    }

    public static boolean exists(BString path) {
        return Files.exists(Paths.get(path.getValue()));
    }

    public static Object createDir(BString dir, boolean parentDirs) {
        try {
            Path dirPath;
            if (parentDirs) {
                dirPath = Files.createDirectories(Paths.get(dir.getValue()));
            } else {
                dirPath = Files.createDirectory(Paths.get(dir.getValue()));
            }
            return StringUtils.fromString(dirPath.toAbsolutePath().toString());
        } catch (FileAlreadyExistsException e) {
            String msg = "File already exists. Failed to create the file: " + dir;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR, StringUtils.fromString(msg));
        } catch (SecurityException e) {
            String msg = "Permission denied. Failed to create the file: " + dir;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.PERMISSION_ERROR, StringUtils.fromString(msg));
        } catch (IOException e) {
            String msg = "IO error while creating the file " + dir;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, StringUtils.fromString(msg));
        } catch (Exception e) {
            String msg = "Error while creating the file " + dir;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, StringUtils.fromString(msg));
        }
    }

    public static Object rename(BString oldPath, BString newPath) {
        Path oldFilePath = Paths.get(oldPath.getValue());
        Path newFilePath = Paths.get(newPath.getValue());

        if (Files.notExists(oldFilePath)) {
            return FileUtils.getBallerinaError(FileConstants.FILE_NOT_FOUND_ERROR,
                                               StringUtils
                                                       .fromString("File not found: " + oldFilePath.toAbsolutePath()));
        }

        try {
            Files.move(oldFilePath.toAbsolutePath(), newFilePath.toAbsolutePath());
            return null;
        } catch (FileAlreadyExistsException e) {
            return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR,
                                               StringUtils.fromString(
                                                       "File already exists in the new path " + newFilePath));
        } catch (IOException e) {
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, e);
        } catch (SecurityException e) {
            return FileUtils.getBallerinaError(FileConstants.PERMISSION_ERROR, e);
        }
    }

    public static BString tempDir() {
        return StringUtils.fromString(FileUtils.getSystemProperty(TEMP_DIR_PROPERTY_KEY));
    }

    public static Object createFile(BString path) {
        try {
            Path filepath = Files.createFile(Paths.get(path.getValue()));
            return StringUtils.fromString(filepath.toAbsolutePath().toString());
        } catch (FileAlreadyExistsException e) {
            String msg = "File already exists. Failed to create the file: " + path;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR,
                                               StringUtils.fromString(msg));
        } catch (SecurityException e) {
            String msg = "Permission denied. Failed to create the file: " + path;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.PERMISSION_ERROR, StringUtils.fromString(msg));
        } catch (NoSuchFileException e) {
            String msg = "The file does not exist in path " + path;
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, StringUtils.fromString(msg));
        } catch (IOException e) {
            String msg = "IO error occurred while creating the file " + path;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, StringUtils.fromString(msg));
        } catch (Exception e) {
            String msg = "Error occurred while creating the file " + path;
            log.error(msg, e);
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, StringUtils.fromString(msg));
        }
    }

    public static Object getFileInfo(BString path) {
        File inputFile = Paths.get(path.getValue()).toAbsolutePath().toFile();
        if (!inputFile.exists()) {
            return FileUtils.getBallerinaError(FileConstants.FILE_NOT_FOUND_ERROR,
                                               StringUtils.fromString("File not found: " + path));
        }
        try {
            return FileUtils.getFileInfo(inputFile);
        } catch (IOException e) {
            log.error("IO error while creating the file " + path, e);
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, e);
        }
    }

    public static Object remove(BString path, boolean recursive) {
        File removeFile = Paths.get(path.getValue()).toAbsolutePath().toFile();
        String wdBValue = FileUtils.getSystemProperty(CURRENT_DIR_PROPERTY_KEY);
        File wd = Paths.get(wdBValue).toAbsolutePath().toFile();

        try {
            if (wd.getCanonicalPath().equals(removeFile.getCanonicalPath())) {
                return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR,
                                                   StringUtils.fromString("Cannot delete the current working " +
                                                                                   "directory " +
                                                                                   wd.getCanonicalPath()));
            }

            if (!removeFile.exists()) {
                return FileUtils.getBallerinaError(FileConstants.FILE_NOT_FOUND_ERROR,
                                                   StringUtils.fromString("File not found: " +
                                                                                   removeFile.getCanonicalPath()));
            }

            if (recursive) {
                Path directory = Paths.get(removeFile.getCanonicalPath());
                Files.walkFileTree(directory, new RecursiveFileVisitor());
            } else {
                if (!removeFile.delete()) {
                    return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR,
                                                       StringUtils.fromString("Error while deleting " +
                                                                                       removeFile.getCanonicalPath()));
                }
            }
            return null;
        } catch (IOException ex) {
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, ex);
        } catch (SecurityException ex) {
            return FileUtils.getBallerinaError(FileConstants.PERMISSION_ERROR, ex);
        }
    }

    static class RecursiveFileVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }

    public static Object readDir(BString path, long maxDepth) {
        File inputFile = Paths.get(path.getValue()).toAbsolutePath().toFile();

        if (!inputFile.exists()) {
            return FileUtils.getBallerinaError(FileConstants.FILE_NOT_FOUND_ERROR,
                                               StringUtils.fromString("File not found: " + path));
        }

        if (!inputFile.isDirectory()) {
            return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR,
                                               StringUtils
                                                       .fromString("File in path " + path + " is not a directory"));
        }

        if (maxDepth == FileConstants.DEFAULT_MAX_DEPTH) {
            // If the user has not given a value, read all levels
            return readFileTree(inputFile, Integer.MAX_VALUE);
        } else if (maxDepth > FileConstants.DEFAULT_MAX_DEPTH && maxDepth < Integer.MAX_VALUE) {
            // If the user has given a valid depth level, read up-to that level
            return readFileTree(inputFile, Math.toIntExact(maxDepth));
        } else {
            return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR,
                                               StringUtils.fromString("Invalid maxDepth value " + maxDepth));
        }
    }

    private static Object readFileTree(File inputFile, int maxDepth) {
        BObject[] results;
        try (Stream<Path> walk = Files.walk(inputFile.toPath(), maxDepth)) {
            results = walk.map(x -> {
                try {
                    BObject bObject = FileUtils.getFileInfo(x.toFile());
                    fileInfoType = bObject.getType();
                    return bObject;
                } catch (IOException e) {
                    throw new BallerinaException("Error while accessing file info", e);
                }
            }).skip(1).toArray(BObject[]::new);
            return ValueCreator.createArrayValue(results, TypeCreator.createArrayType(fileInfoType));
        } catch (IOException | BallerinaException ex) {
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, ex);
        } catch (SecurityException ex) {
            return FileUtils.getBallerinaError(FileConstants.PERMISSION_ERROR, ex);
        }
    }

    public static Object copy(BString sourcePath, BString destinationPath, boolean replaceExisting) {
        Path srcPath = Paths.get(sourcePath.getValue());
        Path destPath = Paths.get(destinationPath.getValue());

        if (Files.notExists(srcPath)) {
            return FileUtils.getBallerinaError(FileConstants.FILE_NOT_FOUND_ERROR,
                                               StringUtils.fromString("File not found: " + sourcePath));
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
