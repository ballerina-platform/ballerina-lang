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
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Native function ballerina.compression:ZipToBytes.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "compression",
        functionName = "zipToBytes",
        args = {@Argument(name = "dirPath", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.BLOB)},
        isPublic = true
)
public class ZipToBytes extends BlockingNativeCallableUnit {
    /**
     * File path defined in ballerina.compression
     */
    private static final int SRC_PATH_FIELD_INDEX = 0;

    /**
     * Zip a file content as bytes.
     * @param dirPath file content as a byte array.
     */
    private static byte[] zipToByte(String dirPath) {
        OutputStream os = ZipFile.zipFiles(dirPath, new ByteArrayOutputStream());
        ByteArrayOutputStream bos = (ByteArrayOutputStream) os;
        return bos.toByteArray();
    }

    @Override
    public void execute(Context context) {
        BBlob readByteBlob;
        String dirPath = context.getStringArgument(SRC_PATH_FIELD_INDEX);
        byte[] compressedBytes = zipToByte(dirPath);
        readByteBlob = new BBlob(compressedBytes);
        context.setReturnValues(readByteBlob);
    }
}
