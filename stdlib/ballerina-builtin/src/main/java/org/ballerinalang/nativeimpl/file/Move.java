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
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.ballerinalang.nativeimpl.file.utils.FileUtils.createAccessDeniedError;
import static org.ballerinalang.nativeimpl.file.utils.FileUtils.createFileNotFoundError;
import static org.ballerinalang.nativeimpl.file.utils.FileUtils.createIOError;

/**
 * Moves a file from a given location to another.
 */
@BallerinaFunction(
        packageName = "ballerina.file",
        functionName = "move",
        args = {@Argument(name = "source", type = TypeKind.STRUCT, structType = "File",
                structPackage = "ballerina.file"),
                @Argument(name = "destination", type = TypeKind.STRUCT, structType = "File",
                        structPackage = "ballerina.file")},
        returnType = {
                @ReturnType(type = TypeKind.STRUCT, structType = "FileNotFoundError", structPackage = "ballerina.file"),
                @ReturnType(type = TypeKind.STRUCT, structType = "AccessDeniedError", structPackage = "ballerina.file"),
                @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.file")},
        isPublic = true
)
public class Move extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(Move.class);

    @Override 
    public BValue[] execute(Context context) {
        BStruct source = (BStruct) getRefArgument(context, 0);
        BStruct destination = (BStruct) getRefArgument(context, 1);
        boolean replaceExisting = getBooleanArgument(context, 0);

        Path sourceFile = Paths.get(source.getStringField(0)).toAbsolutePath();
        if (!Files.exists(sourceFile)) {
            return getBValues(createFileNotFoundError(context, "Failed to move file: '"
                                                + sourceFile.toString() + "'. File not found."), null, null);
        }

        Path targetFile = Paths.get(destination.getStringField(0)).toAbsolutePath();
        CopyOption[] copyOptions =
                replaceExisting ? new CopyOption[]{REPLACE_EXISTING, ATOMIC_MOVE} : new CopyOption[]{ATOMIC_MOVE};

        try {
            if (Files.isDirectory(targetFile)) {
                // If the target file is a directory, move the source file directly to the target directory.
                Files.move(sourceFile, targetFile.resolve(sourceFile.getFileName()), copyOptions);
            } else {
                Path targetDir = targetFile.getParent();

                // If the target directory tree doesn't exist, create it. Creates a directory tree for the
                // parent of the file specified by 'targetFile'.
                createDirectories(targetDir);

                Files.move(sourceFile, targetFile, copyOptions);
            }
        } catch (SecurityException | AccessDeniedException e) {
            String errMsg = "Failed to move file: '" + sourceFile.toString()
                    + "' to '" + targetFile.toString() + "'. Permission denied.";
            log.error(errMsg, e);
            return getBValues(null, createAccessDeniedError(context, errMsg), null);
        } catch (BallerinaException e) {
            return getBValues(null, createIOError(context, e.getMessage()));
        } catch (FileAlreadyExistsException e) {
            String errMsg =
                    "File already exists: '" + targetFile.toString() +
                            "'. Enable file replacing to replace existing file.";
            log.error(errMsg, e);
            return getBValues(null, null, createIOError(context, errMsg));
        } catch (DirectoryNotEmptyException e) {
            String errMsg = "Failed to move file: '" + sourceFile.toString()
                    + "' to '" + targetFile.toString() + "'. Target directory is not empty.";
            log.error(errMsg, e);
            return getBValues(null, null, createIOError(context, errMsg));
        } catch (IOException e) {
            String errMsg = "Failed to move file: '" + sourceFile.toString()
                    + "' to '" + targetFile.toString() + "'. I/O error occurred.";
            log.error(errMsg, e);
            return getBValues(null, null, createIOError(context, errMsg));
        }

        return getBValues(null, null, null);
    }

    private void createDirectories(Path dirPath) {
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (FileAlreadyExistsException e) {
                String errMsg = "Failed to create directory: '" + dirPath + "'. File already exists.";
                log.error(errMsg, e);
                throw new BallerinaException(errMsg);
            } catch (SecurityException e) {
                String errMsg = "Failed to create directory: '" + dirPath + "'. Permission denied.";
                log.error(errMsg, e);
                throw new BallerinaException(errMsg);
            } catch (IOException e) {
                String errMsg = "Failed to create directory: '" + dirPath + "'. I/O error occurred.";
                log.error(errMsg, e);
                throw new BallerinaException(errMsg);
            }
        }
    }
}
