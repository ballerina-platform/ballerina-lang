/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.io.UnsupportedEncodingException;

/**
 * Convert ByteArray to String.
 *
 * @since 0.980.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "byteArrayToString",
        args = {@Argument(name = "b", type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                @Argument(name = "encoding", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ByteArrayToString {

    public static String byteArrayToString(Strand strand, ArrayValue arr, String encoding) {
        try {
            return new String(arr.getBytes(), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new org.ballerinalang.jvm.util.exceptions.BallerinaException("Unsupported Encoding of Blob", e);
        }
    }
}
