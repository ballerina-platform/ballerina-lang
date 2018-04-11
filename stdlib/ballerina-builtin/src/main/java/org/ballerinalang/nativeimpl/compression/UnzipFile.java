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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BLangRuntimeException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Native function ballerina.compression:unzipFile
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "compression",
        functionName = "unzipFile",
        args = {@Argument(name = "dirPath", type = TypeKind.STRING),
                @Argument(name = "destDir", type = TypeKind.STRING),
                @Argument(name = "folderToUnzip", type = TypeKind.STRING)},
        isPublic = true
)
public class UnzipFile extends BlockingNativeCallableUnit {

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
     * @param folderToUnzip folder to unzip from the zipped file
     */
    private static void decompress(String dirPath, String outputFolder, String folderToUnzip) {
        try {
            InputStream inputStream = new FileInputStream(dirPath);
            UnzipBytes.decompress(inputStream, outputFolder, folderToUnzip);
        } catch (IOException e) {
            throw new BLangRuntimeException("Error occurred when decompressing");
        }
    }

    @Override
    public void execute(Context context) {
        String dirPath = context.getStringArgument(SRC_PATH_FIELD_INDEX);
        String destDir = context.getStringArgument(DEST_PATH_FIELD_INDEX);
        String folderToUnzip = context.getStringArgument(FOLDER_TO_UNZIP_INDEX);
        decompress(dirPath, destDir, folderToUnzip);
    }
}
