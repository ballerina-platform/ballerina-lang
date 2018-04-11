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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.MessageHeaders;

import static org.ballerinalang.net.grpc.MessageConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageHeaders.METADATA_KEY;

/**
 * Set custom Header to the Message.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "setEntry",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Headers",
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {@Argument(name = "headerName", type = TypeKind.STRING),
                @Argument(name = "headerValue", type = TypeKind.STRING)},
        isPublic = true
)
public class SetEntry extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BStruct headerValues = (BStruct) context.getRefArgument(0);
        MessageHeaders metadata = (MessageHeaders) headerValues.getNativeData(METADATA_KEY);
        String headerName = context.getStringArgument(0);
        String headerValue = context.getStringArgument(1);

        // Only initialize ctx if not yet initialized
        metadata = metadata != null ? metadata : new MessageHeaders();
        Metadata.Key<String> key = Metadata.Key.of(headerName, Metadata.ASCII_STRING_MARSHALLER);
        if (metadata.containsKey(key)) {
            metadata.removeAll(key);
        }
        metadata.put(key, headerValue);
        headerValues.addNativeData(METADATA_KEY, metadata);
        context.setReturnValues();
    }
}
