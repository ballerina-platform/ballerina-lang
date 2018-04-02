/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.security.crypto;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Function for generating CRC32 hashes.
 *
 * @since 0.970.0-alpha1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "security.crypto",
        functionName = "getCRC32",
        args = {@Argument(name = "content", type = TypeKind.ANY)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true)
public class GetCRC32 extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BValue entityBody = context.getRefArgument(0);
        Checksum checksum = new CRC32();
        byte[] bytes;
        long checksumVal;

        BType argType = entityBody.getType();
        if (argType == BTypes.typeJSON || argType == BTypes.typeXML || argType == BTypes.typeString) {
            // TODO: Look at the possibility of making the encoding configurable
            bytes = entityBody.stringValue().getBytes(StandardCharsets.UTF_8);
        } else if (argType == BTypes.typeBlob) {
            bytes = ((BBlob) entityBody).blobValue();
        } else {
            throw new BallerinaException(
                    "failed to generate hash: unsupported data type: " + entityBody.getType().getName());
        }

        checksum.update(bytes, 0, bytes.length);
        checksumVal = checksum.getValue();
        context.setReturnValues(new BString(Long.toHexString(checksumVal)));
    }
}
