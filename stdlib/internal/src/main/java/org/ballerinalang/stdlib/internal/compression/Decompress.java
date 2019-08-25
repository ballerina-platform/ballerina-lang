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

package org.ballerinalang.stdlib.internal.compression;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BLangRuntimeException;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.internal.Constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Extern function ballerina.compression:decompress.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = Constants.ORG_NAME,
        packageName = Constants.PACKAGE_NAME,
        functionName = "decompress",
        args = {
                @Argument(name = "dirPath", type = TypeKind.OBJECT, structType = Constants.PATH_STRUCT,
                          structPackage = Constants.PACKAGE_PATH),
                @Argument(name = "destDir", type = TypeKind.OBJECT, structType = Constants.PATH_STRUCT,
                          structPackage = Constants.PACKAGE_PATH)
        },
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class Decompress {

    /**
     * File path defined in ballerina.compression.
     */
    private static final int SRC_PATH_FIELD_INDEX = 0;

    /**
     * File path of the destination directory defined in ballerina.compression.
     */
    private static final int DEST_PATH_FIELD_INDEX = 1;

    /**
     * Decompresses a compressed file.
     *
     * @param dirPath      compressed file path
     * @param outputFolder destination folder
     * @return ErrorValue when an error occurs with decompressing
     */
    private static ErrorValue decompress(Path dirPath, Path outputFolder) {
        try {
            InputStream inputStream = new FileInputStream(dirPath.toFile());
            return DecompressFromByteArray.decompress(inputStream, outputFolder);
        } catch (IOException e) {
            throw new BLangRuntimeException("Error occurred when decompressing");
        }
    }

    public static Object decompress(Strand strand, String dirPath, String destDir) {
        Path srcPath = Paths.get(dirPath);
        Path destPath = Paths.get(destDir);

        if (!srcPath.toFile().exists()) {
            return CompressionUtils.createCompressionError("Path of the folder to be " +
                    "decompressed is not available: " + srcPath);
        } else if (!destPath.toFile().exists()) {
            return CompressionUtils.createCompressionError("Path to place the decompressed file is not available: " +
                    destPath);
        } else {
            return decompress(srcPath, destPath);
        }

    }
}
