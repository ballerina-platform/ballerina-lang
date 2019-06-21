/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.crypto.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Function for generating CRC32 hashes.
 *
 * @since 0.970.0-alpha1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "crypto",
        functionName = "crc32b", isPublic = true)
public class Crc32b extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
    }

    public static String crc32b(Strand strand, Object entityBody) {
        Checksum checksum = new CRC32();
        byte[] bytes;
        long checksumVal;

        BType argType = TypeChecker.getType(entityBody);
        if (argType == BTypes.typeJSON || argType == BTypes.typeXML || argType == BTypes.typeString) {
            // TODO: Look at the possibility of making the encoding configurable
            bytes = entityBody.toString().getBytes(StandardCharsets.UTF_8);
        } else if (argType.getTag() == TypeTags.ARRAY_TAG &&
                ((BArrayType) argType).getElementType().getTag() == TypeTags.BYTE_TAG) {
            bytes = ((ArrayValue) entityBody).getBytes();
        } else {
            throw new BallerinaException("Failed to generate hash: unsupported data type: " +
                    TypeChecker.getType(entityBody).getName());
        }

        checksum.update(bytes, 0, bytes.length);
        checksumVal = checksum.getValue();
        return Long.toHexString(checksumVal);
    }
}
