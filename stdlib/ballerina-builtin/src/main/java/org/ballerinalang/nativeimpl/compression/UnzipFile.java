/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.nativeimpl.compression;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Native function ballerina.compression:unzipFile
 *
 * @since 0.962.0
 */
@BallerinaFunction(
        packageName = "ballerina.compression",
        functionName = "unzipFile",
        args = {@Argument(name = "dirPath", type = TypeKind.STRING),
                @Argument(name = "destDir", type = TypeKind.STRING),
                @Argument(name = "folderToUnzip", type = TypeKind.STRING)},
        isPublic = true
)
public class UnzipFile extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(UnzipFile.class);

    /**
     * File path defined in ballerina.compression
     */
    private static final int SRC_PATH_FIELD_INDEX = 0;

    /**
     * File path of the destination directory defined in ballerina.compression
     */
    private static final int DEST_PATH_FIELD_INDEX = 1;

    /**
     * Folder to unzip from the compressed bytes.
     */
    private static final int FOLDER_TO_UNZIP_INDEX = 2;
    /**
     * Decompress/unzip compressed file.
     *  @param dirPath      compressed file path
     * @param outputFolder destination folder
     * @param folderToUnzip
     */
    private static void decompress(String dirPath, String outputFolder, String folderToUnzip) {
        try {
            byte[] fileContentAsByteArray = Files.readAllBytes(Paths.get(dirPath));
            UnzipBytes.decompress(fileContentAsByteArray, outputFolder, folderToUnzip);
        } catch (IOException e) {
            log.debug("I/O exception occured when processing the file " + dirPath, e);
            log.error("I/O exception occured when processing the file " + dirPath);
        }
    }

    @Override
    public BValue[] execute(Context context) {
        String dirPath = getStringArgument(context, SRC_PATH_FIELD_INDEX);
        String destDir = getStringArgument(context, DEST_PATH_FIELD_INDEX);
        String folderToUnzip = getStringArgument(context, FOLDER_TO_UNZIP_INDEX);
        decompress(dirPath, destDir, folderToUnzip);
        return VOID_RETURN;
    }
}
