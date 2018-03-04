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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
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
 * @since 0.965.0
 */
@BallerinaFunction(
        packageName = "ballerina.security.crypto",
        functionName = "getCRC32",
        args = {@Argument(name = "element", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true)
public class GetCRC32 extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BValue value = getRefArgument(context, 0);
        Checksum checksum = new CRC32();
        byte[] bytes;
        long checksumVal;

        if (value instanceof BString) {
            // TODO: Look at the possibility of making the encoding configurable
            bytes = value.stringValue().getBytes(StandardCharsets.UTF_8);
            checksum.update(bytes, 0, bytes.length);
            checksumVal = checksum.getValue();
        } else if (value instanceof BBlob) {
            bytes = ((BBlob) value).blobValue();
            checksum.update(bytes, 0, bytes.length);
            checksumVal = checksum.getValue();
        } else {
            throw new BallerinaException(
                    "failed to generate hash: unsupported data type: " + value.getType().getName());
        }

        return getBValues(new BString(Long.toHexString(checksumVal)));
    }
}
