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
package org.ballerinalang.net.grpc.nativeimpl.connection.client;

import com.google.protobuf.Descriptors;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.utils.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native function to respond the server.
 *
 * @since 0.96.1
 */
@BallerinaFunction(
        packageName = MessageConstants.PROTOCOL_PACKAGE_GRPC,
        functionName = "send",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = MessageConstants.CLIENT_CONNECTION,
                structPackage = MessageConstants.PROTOCOL_PACKAGE_GRPC),
        args = {@Argument(name = "response", type = TypeKind.STRING)},
        returnType = @ReturnType(type = TypeKind.STRUCT, structType = "ConnectorError",
                structPackage = MessageConstants.PROTOCOL_PACKAGE_GRPC),
        isPublic = true
)
public class Send extends AbstractNativeFunction {
    private static final Logger log = LoggerFactory.getLogger(Send.class);

    @Override
    public BValue[] execute(Context context) {
        log.info("calling send...");
        BStruct connectionStruct = (BStruct) getRefArgument(context, 0);
        BValue responseValue = getRefArgument(context, 1);
        StreamObserver requestSender = MessageUtils.getStreamObserver(connectionStruct);
        if (requestSender == null) {
            return new BValue[0];
        }
        Descriptors.Descriptor inputType = (Descriptors.Descriptor) connectionStruct.getNativeData(MessageConstants
                .REQUEST_MESSAGE_DEFINITION);

        try {
            Message requestMessage = MessageUtil.generateProtoMessage(responseValue, inputType);
            requestSender.onNext(requestMessage);
            return new BValue[0];
        } catch (Throwable e) {
            log.error("Error while sending client response.", e);
            return getBValues(MessageUtils.getServerConnectorError(context, e));
        }
    }
}
