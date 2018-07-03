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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.internal.utils.Constants;
import org.ballerinalang.util.exceptions.BLangRuntimeException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Native function ballerina.compression:decompress.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "internal",
        functionName = "decompress",
        args = {
                @Argument(name = "dirPath", type = TypeKind.RECORD, structType = "Path",
                        structPackage = "ballerina/file"),
                @Argument(name = "destDir", type = TypeKind.RECORD, structType = "Path",
                        structPackage = "ballerina/file")
        },
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class Decompress extends BlockingNativeCallableUnit {

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
     */
    private static void decompress(Path dirPath, Path outputFolder, Context context) {
        try {
            InputStream inputStream = new FileInputStream(dirPath.toFile());
            DecompressFromByteArray.decompress(inputStream, outputFolder, context);
        } catch (IOException e) {
            throw new BLangRuntimeException("Error occurred when decompressing");
        }
    }

    @Override
    public void execute(Context context) {
        BMap<String, BValue> srcPathStruct = (BMap) context.getRefArgument(SRC_PATH_FIELD_INDEX);
        Path srcPath = (Path) srcPathStruct.getNativeData(Constants.PATH_DEFINITION_NAME);

        BMap<String, BValue> destPathStruct = (BMap) context.getRefArgument(DEST_PATH_FIELD_INDEX);
        Path destPath = (Path) destPathStruct.getNativeData(Constants.PATH_DEFINITION_NAME);

        if (!srcPath.toFile().exists()) {
            context.setReturnValues(CompressionUtils.createCompressionError(context, "Path of the folder to be " +
                    "decompressed is not available"));
        } else if (!destPath.toFile().exists()) {
            context.setReturnValues(CompressionUtils.createCompressionError(context,
                    "Path to place the decompressed file " +
                            "is not available"));
        } else {
            decompress(srcPath, destPath, context);
            if (context.getReturnValues() == null) {
                context.setReturnValues();
            }
        }
    }
}
