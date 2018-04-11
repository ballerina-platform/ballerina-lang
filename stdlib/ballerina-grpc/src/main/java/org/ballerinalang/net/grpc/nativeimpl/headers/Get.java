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
package org.ballerinalang.net.grpc.nativeimpl.headers;

import io.grpc.Metadata;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.MessageHeaders;

import java.util.Base64;

import static org.ballerinalang.net.grpc.MessageConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageHeaders.METADATA_KEY;

/**
 * Get the Headers of the Message.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "get",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Headers",
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {@Argument(name = "headerName", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class Get extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        String headerName = context.getStringArgument(0);
        BStruct headerValues = (BStruct) context.getRefArgument(0);
        MessageHeaders metadata = headerValues != null ? (MessageHeaders) headerValues.getNativeData(METADATA_KEY)
                : null;
        String headerValue = getHeaderValue(metadata, headerName);
        if (headerValue != null) {
            context.setReturnValues(new BString(headerValue));
        } else {
            context.setReturnValues();
        }
    }

    private String getHeaderValue(MessageHeaders metadata, String keyName) {
        String headerValue = null;
        if (metadata != null) {
            if (keyName.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                Metadata.Key<byte[]> key = Metadata.Key.of(keyName, Metadata.BINARY_BYTE_MARSHALLER);
                byte[] byteValues = metadata.get(key);
                // Referred : https://stackoverflow
                // .com/questions/1536054/how-to-convert-byte-array-to-string-and-vice-versa
                // https://stackoverflow.com/questions/2418485/how-do-i-convert-a-byte-array-to-base64-in-java
                headerValue = byteValues != null ? Base64.getEncoder().encodeToString(byteValues) : null;
            } else {
                Metadata.Key<String> key = Metadata.Key.of(keyName, Metadata.ASCII_STRING_MARSHALLER);
                headerValue = metadata.get(key);
            }
        }
        return headerValue;
    }
}
