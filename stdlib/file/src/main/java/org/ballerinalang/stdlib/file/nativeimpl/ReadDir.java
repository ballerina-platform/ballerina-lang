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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.file.utils.FileConstants;
import org.ballerinalang.stdlib.file.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Extern function ballerina.file:readDir.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = FileConstants.ORG_NAME,
        packageName = FileConstants.PACKAGE_NAME,
        functionName = "readDir",
        isPublic = true
)
public class ReadDir {

    private static BType fileInfoType;

    public static Object readDir(Strand strand, String path, long maxDepth) {
        File inputFile = Paths.get(path).toAbsolutePath().toFile();

        if (!inputFile.exists()) {
            return FileUtils.getBallerinaError(FileConstants.FILE_NOT_FOUND_ERROR,
                    "File not found: " + path);
        }

        if (!inputFile.isDirectory()) {
            return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR,
                    "File in path " + path + " is not a directory");
        }

        if (maxDepth == FileConstants.DEFAULT_MAX_DEPTH) {
            // If the user has not given a value, read all levels
            return readFileTree(inputFile, Integer.MAX_VALUE);
        } else if (maxDepth > FileConstants.DEFAULT_MAX_DEPTH && maxDepth < Integer.MAX_VALUE) {
            // If the user has given a valid depth level, read up-to that level
            return readFileTree(inputFile, Math.toIntExact(maxDepth));
        } else {
            return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR,
                    "Invalid maxDepth value " + maxDepth);
        }
    }

    private static Object readFileTree(File inputFile, int maxDepth) {
        ObjectValue[] results;
        try (Stream<Path> walk = Files.walk(inputFile.toPath(), maxDepth)) {
            results = walk.map(x -> {
                try {
                    ObjectValue objectValue = FileUtils.getFileInfo(x.toFile());
                    fileInfoType = objectValue.getType();
                    return objectValue;
                } catch (IOException e) {
                    throw new BallerinaException("Error while accessing file info", e);
                }
            }).skip(1).toArray(ObjectValue[]::new);
            return new ArrayValueImpl(results, new BArrayType(fileInfoType));
        } catch (IOException | BallerinaException ex) {
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, ex);
        } catch (SecurityException ex) {
            return FileUtils.getBallerinaError(FileConstants.PERMISSION_ERROR, ex);
        }
    }
}
