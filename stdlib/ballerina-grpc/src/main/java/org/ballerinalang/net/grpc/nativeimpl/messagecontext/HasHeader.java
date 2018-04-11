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
package org.ballerinalang.net.grpc.nativeimpl.messagecontext;

import io.grpc.Metadata;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.MessageContext;

import static org.ballerinalang.net.grpc.MessageConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageContext.MESSAGE_CONTEXT_KEY;

/**
 * Check whether the Header exists in the Message.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "hasHeader",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "MessageContext",
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {@Argument(name = "headerName", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.BOOLEAN)},
        isPublic = true
)
public class HasHeader extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        String headerName = context.getStringArgument(0);
        MessageContext messageContext = (MessageContext) context.getProperty(MESSAGE_CONTEXT_KEY);
        boolean isExist = false;
        if (messageContext != null) {
            if (headerName.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                Metadata.Key<byte[]> key = Metadata.Key.of(headerName, Metadata.BINARY_BYTE_MARSHALLER);
                isExist = messageContext.containsKey(key);
            } else {
                Metadata.Key<String> key = Metadata.Key.of(headerName, Metadata.ASCII_STRING_MARSHALLER);
                isExist = messageContext.containsKey(key);
            }
        }
        context.setReturnValues(new BBoolean(isExist));
    }
}
