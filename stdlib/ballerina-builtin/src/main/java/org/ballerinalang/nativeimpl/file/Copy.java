/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.file;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.EnumSet;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.ballerinalang.nativeimpl.file.utils.FileUtils.createAccessDeniedError;
import static org.ballerinalang.nativeimpl.file.utils.FileUtils.createFileNotFoundError;
import static org.ballerinalang.nativeimpl.file.utils.FileUtils.createIOError;

/**
 * Copies a file from a given location to another.
 */
@BallerinaFunction(
        packageName = "ballerina.file",
        functionName = "copy",
        args = {@Argument(name = "source", type = TypeKind.STRUCT, structType = "File",
                structPackage = "ballerina.file"),
                @Argument(name = "destination", type = TypeKind.STRUCT, structType = "File",
                          structPackage = "ballerina.file"),
                @Argument(name = "replaceExisting", type = TypeKind.BOOLEAN)},
        returnType = {@ReturnType(type = TypeKind.STRUCT), @ReturnType(type = TypeKind.STRUCT)},
        isPublic = true
)
public class Copy extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(Copy.class);
    
    @Override
    public BValue[] execute(Context context) {
        BStruct source = (BStruct) getRefArgument(context, 0);
        BStruct destination = (BStruct) getRefArgument(context, 1);
        boolean replaceExisting = getBooleanArgument(context, 0);

        Path sourceFile = Paths.get(source.getStringField(0));
        Path destinationFile = Paths.get(destination.getStringField(0));

        if (!Files.exists(sourceFile)) {
            return getBValues(
                    createFileNotFoundError(context, "Failed to copy file: file not found: " + sourceFile.toString()));
        }

        try {
            copyFile(sourceFile, destinationFile, replaceExisting);
        } catch (SecurityException e) {
            String errMsg = "Failed to copy file/dir: " + sourceFile.toString() + " to " + destinationFile.toString();
            log.error(errMsg, e);
            return getBValues(createAccessDeniedError(context, "Permission denied: " + errMsg), null);
        } catch (IOException e) {
            String errMsg = "Failed to copy file/dir: " + sourceFile.toString() + " to " + destinationFile.toString();
            log.error(errMsg, e);
            return getBValues(null, createIOError(context, "I/O error occurred: " + errMsg));
        }

        return new BValue[]{null};
    }

    private void copyFile(Path source, Path target, boolean replaceExisting) throws IOException {
        CopyOption[] options =
                replaceExisting ?
                        new CopyOption[]{COPY_ATTRIBUTES, REPLACE_EXISTING} : new CopyOption[]{COPY_ATTRIBUTES};

        if (Files.isDirectory(source)) {
            EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
            DirCopier dirCopier = new DirCopier(source, target, options);
            Files.walkFileTree(source, opts, Integer.MAX_VALUE, dirCopier);
        } else {
            try {
                Files.copy(source, target, options);
            } catch (IOException ex) {
                System.err.println("ballerina: unable to copy file: " + source + " to " + target);
                throw ex;
            }
        }
    }

    private static class DirCopier extends SimpleFileVisitor<Path> {

        private final Path source;
        private final Path target;
        private final CopyOption[] fileCopyOptions;

        public DirCopier(Path source, Path target, CopyOption[] fileCopyOptions) {
            this.source = source;
            this.target = target;
            this.fileCopyOptions = fileCopyOptions;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) {
            // Preserve the attributes of the file being copied
            CopyOption[] options = new CopyOption[]{COPY_ATTRIBUTES};

            Path relativeDir = target.resolve(source.relativize(dir));
            try {
                Files.copy(dir, relativeDir, options);
            } catch (IOException e) {
                System.err.println("ballerina: unable to create directory: " + relativeDir);
                return FileVisitResult.SKIP_SUBTREE;
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            Path destination = null;

            try {
                destination = target.resolve(source.relativize(file));
                Files.copy(file, destination, fileCopyOptions);

                if (log.isDebugEnabled()) {
                    log.debug("Copying file: " + file + " to " + destination);
                }
            } catch (AccessDeniedException ex) {
                System.err.println("ballerina: permission denied. Unable to create file: " + destination);
            } catch (IOException e) {
                System.err.println("ballerina: unable to create file: " + destination);
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException ex) {
            if (ex == null) {
                Path relDir = target.resolve(source.relativize(dir));
                try {
                    FileTime lastModifiedTime = Files.getLastModifiedTime(dir);
                    Files.setLastModifiedTime(relDir, lastModifiedTime);
                } catch (IOException e) {
                    System.err.println("ballerina: unable to copy attributes to: " + relDir);
                }
            }

            return FileVisitResult.CONTINUE;
        }
    }
}
