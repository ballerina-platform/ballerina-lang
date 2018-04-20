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
package org.ballerinalang.net.grpc.nativeimpl.client;

import com.google.protobuf.Descriptors;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ballerinalang.bre.bvm.BLangVMErrors.PACKAGE_BUILTIN;
import static org.ballerinalang.bre.bvm.BLangVMErrors.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.net.grpc.MessageConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.MessageConstants.REQUEST_SENDER;

/**
 * Native function to respond the server.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = MessageConstants.PROTOCOL_PACKAGE_GRPC,
        functionName = "send",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = MessageConstants.GRPC_CLIENT,
                structPackage = MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {@Argument(name = "response", type = TypeKind.STRING)},
        returnType = @ReturnType(type = TypeKind.STRUCT, structType = STRUCT_GENERIC_ERROR, structPackage =
                PACKAGE_BUILTIN),
        isPublic = true
)
public class Send extends BlockingNativeCallableUnit {
    private static final Logger LOG = LoggerFactory.getLogger(Send.class);
    
    @Override
    public void execute(Context context) {
        BStruct connectionStruct = (BStruct) context.getRefArgument(0);
        BValue responseValue = context.getRefArgument(1);
        StreamObserver<Message> requestSender = (StreamObserver<Message>) connectionStruct.getNativeData
                (REQUEST_SENDER);
        if (requestSender == null) {
            context.setError(MessageUtils.getConnectorError(context, new StatusRuntimeException(Status
                    .fromCode(Status.INTERNAL.getCode()).withDescription("Error while initializing connector. " +
                            "response sender does not exist"))));
        } else {
            Descriptors.Descriptor inputType = (Descriptors.Descriptor) connectionStruct.getNativeData(MessageConstants
                    .REQUEST_MESSAGE_DEFINITION);
            try {
                Message requestMessage = MessageUtils.generateProtoMessage(responseValue, inputType);
                requestSender.onNext(requestMessage);
            } catch (Throwable e) {
                LOG.error("Error while sending client response.", e);
                context.setError(MessageUtils.getConnectorError(context, e));
            }
        }
    }
}
