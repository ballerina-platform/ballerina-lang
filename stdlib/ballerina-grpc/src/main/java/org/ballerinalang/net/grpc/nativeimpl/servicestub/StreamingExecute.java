/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.nativeimpl.servicestub;

import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.connector.impl.ValueImpl;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTypeValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.MessageRegistry;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.stubs.DefaultStreamObserver;
import org.ballerinalang.net.grpc.stubs.GrpcNonBlockingStub;

import static org.ballerinalang.net.grpc.EndpointConstants.CLIENT_END_POINT;
import static org.ballerinalang.net.grpc.EndpointConstants.SERVICE_STUB;

/**
 * {@code StreamingExecute} is the StreamingExecute action implementation of the gRPC Connector.
 */
@BallerinaFunction(
        packageName = "ballerina.net.grpc",
        functionName = "streamingExecute",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ServiceStub",
                structPackage = "ballerina.net.grpc"),
        args = {
                @Argument(name = "methodID", type = TypeKind.STRING),
                @Argument(name = "listenerService", type = TypeKind.TYPE)
        },
        returnType = {
                @ReturnType(type = TypeKind.ANY),
                @ReturnType(type = TypeKind.STRUCT, structType = "ConnectorError",
                        structPackage = "ballerina.net.grpc"),
        },
        isPublic = true
)
public class StreamingExecute extends AbstractExecute {
    @Override
    public void execute(Context context) {
        BStruct serviceStub = (BStruct) context.getRefArgument(0);
        if (serviceStub == null) {
            notifyErrorReply(context, "Error while getting connector. gRPC Client connector " +
                    "is not initialized properly");
            return;
        }

        Object connectionStub = serviceStub.getNativeData(SERVICE_STUB);
        if (connectionStub == null) {
            notifyErrorReply(context, "Error while getting connection stub. gRPC Client connector " +
                    "is not initialized properly");
            return;
        }
        String methodName = context.getStringArgument(0);
        if (methodName == null) {
            notifyErrorReply(context, "Error while processing the request. RPC endpoint doesn't " +
                    "set properly");
            return;
        }
        com.google.protobuf.Descriptors.MethodDescriptor methodDescriptor = MessageRegistry.getInstance()
                .getMethodDescriptor(methodName);
        if (methodDescriptor == null) {
            notifyErrorReply(context, "No registered method descriptor for '" + methodName + "'");
            return;
        }
        if (connectionStub instanceof GrpcNonBlockingStub) {
            GrpcNonBlockingStub grpcNonBlockingStub = (GrpcNonBlockingStub) connectionStub;
            BTypeValue serviceType = (BTypeValue) context.getRefArgument(1);
            Service callbackService = BLangConnectorSPIUtil.getServiceFromType(context.getProgramFile(), getTypeField
                    (serviceType));
            try {
                MethodDescriptor.MethodType methodType = getMethodType(methodDescriptor);
                DefaultStreamObserver responseObserver = new DefaultStreamObserver(context, callbackService);
                StreamObserver<Message> requestSender;
                if (methodType.equals(MethodDescriptor.MethodType.CLIENT_STREAMING)) {
                    requestSender = grpcNonBlockingStub.executeClientStreaming
                            (responseObserver, methodName);

                } else if (methodType.equals(MethodDescriptor.MethodType.BIDI_STREAMING)) {
                    requestSender = grpcNonBlockingStub.executeBidiStreaming
                            (responseObserver, methodName);
                } else {
                    notifyErrorReply(context, "Error while executing the client call. Method type " +
                            methodType.name() + " not supported");
                    return;
                }
                responseObserver.registerRequestSender(requestSender, methodDescriptor.getInputType());
                BStruct connStruct = createStruct(context, "ClientConnection");
                connStruct.addNativeData(MessageConstants.RESPONDER, requestSender);
                connStruct.addNativeData(MessageConstants.REQUEST_MESSAGE_DEFINITION, methodDescriptor
                        .getInputType());
                BStruct clientEndpoint = (BStruct) serviceStub.getNativeData(CLIENT_END_POINT);
                clientEndpoint.addNativeData("client", connStruct);
                context.setReturnValues(clientEndpoint);
            } catch (RuntimeException | GrpcClientException e) {
                notifyErrorReply(context, "gRPC Client Connector Error :" + e.getMessage());
            }
        }
    }

    private Value getTypeField(BTypeValue refField) {
        if (refField == null) {
            return null;
        }
        return ValueImpl.createValue(refField);
    }
}
