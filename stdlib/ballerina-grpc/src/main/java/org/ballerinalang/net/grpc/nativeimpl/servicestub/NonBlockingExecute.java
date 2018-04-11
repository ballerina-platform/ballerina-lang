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

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.stub.MetadataUtils;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.connector.impl.ValueImpl;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageHeaders;
import org.ballerinalang.net.grpc.MessageRegistry;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.stubs.DefaultStreamObserver;
import org.ballerinalang.net.grpc.stubs.GrpcNonBlockingStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.ballerinalang.net.grpc.MessageConstants.CONNECTOR_ERROR;
import static org.ballerinalang.net.grpc.MessageConstants.METHOD_DESCRIPTORS;
import static org.ballerinalang.net.grpc.MessageConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageConstants.SERVICE_STUB;
import static org.ballerinalang.net.grpc.MessageConstants.SERVICE_STUB_REF_INDEX;
import static org.ballerinalang.net.grpc.MessageUtils.getMessageHeaders;

/**
 * {@code NonBlockingExecute} is the NonBlockingExecute action implementation of the gRPC Connector.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "nonBlockingExecute",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = SERVICE_STUB,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {
                @Argument(name = "methodID", type = TypeKind.STRING),
                @Argument(name = "payload", type = TypeKind.ANY),
                @Argument(name = "listenerService", type = TypeKind.TYPEDESC),
                @Argument(name = "headers", type = TypeKind.ARRAY)
        },
        returnType = {
                @ReturnType(type = TypeKind.ANY),
                @ReturnType(type = TypeKind.STRUCT, structType = CONNECTOR_ERROR,
                        structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        },
        isPublic = true
)
public class NonBlockingExecute extends AbstractExecute {
    private static final Logger LOG = LoggerFactory.getLogger(NonBlockingExecute.class);
    private static final int MESSAGE_HEADER_REF_INDEX = 3;

    @Override
    public void execute(Context context) {
        BStruct serviceStub = (BStruct) context.getRefArgument(SERVICE_STUB_REF_INDEX);
        if (serviceStub == null) {
            notifyErrorReply(context, "Error while getting connector. gRPC Client connector is " +
                    "not initialized properly");
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

        Map<String, MethodDescriptor<Message, Message>> methodDescriptors = (Map<String, MethodDescriptor<Message,
                Message>>) serviceStub.getNativeData(METHOD_DESCRIPTORS);
        if (methodDescriptors == null) {
            notifyErrorReply(context, "Error while processing the request. method descriptors " +
                    "doesn't set properly");
            return;
        }

        com.google.protobuf.Descriptors.MethodDescriptor methodDescriptor = MessageRegistry.getInstance()
                .getMethodDescriptor(methodName);
        if (methodDescriptor == null) {
            notifyErrorReply(context, "No registered method descriptor for '" + methodName + "'");
            return;
        }


        // Update request headers when request headers exists in the context.
        BRefValueArray headerValues = (BRefValueArray) context.getRefArgument(MESSAGE_HEADER_REF_INDEX);
        MessageHeaders headers = getMessageHeaders(headerValues);

        if (connectionStub instanceof GrpcNonBlockingStub) {
            BValue payloadBValue = context.getRefArgument(1);
            Message requestMsg = MessageUtils.generateProtoMessage(payloadBValue, methodDescriptor.getInputType());
            GrpcNonBlockingStub grpcNonBlockingStub = (GrpcNonBlockingStub) connectionStub;

            // Attach header read/write listener to the service stub.
            AtomicReference<Metadata> headerCapture = new AtomicReference<>();
            AtomicReference<Metadata> trailerCapture = new AtomicReference<>();
            if (headers != null) {
                grpcNonBlockingStub = MetadataUtils.attachHeaders(grpcNonBlockingStub, headers.getMessageMetadata());
            }
            grpcNonBlockingStub = MetadataUtils.captureMetadata(grpcNonBlockingStub, headerCapture, trailerCapture);

            BTypeDescValue serviceType = (BTypeDescValue) context.getRefArgument(2);
            Service callbackService = BLangConnectorSPIUtil.getServiceFromType(context.getProgramFile(), getTypeField
                    (serviceType));
            try {
                MethodDescriptor.MethodType methodType = getMethodType(methodDescriptor);
                if (methodType.equals(MethodDescriptor.MethodType.UNARY)) {
                    grpcNonBlockingStub.executeUnary(requestMsg, new DefaultStreamObserver(callbackService,
                            headerCapture), methodDescriptors.get(methodName));
                } else if (methodType.equals(MethodDescriptor.MethodType.SERVER_STREAMING)) {
                    grpcNonBlockingStub.executeServerStreaming(requestMsg, new DefaultStreamObserver(callbackService,
                            headerCapture), methodDescriptors.get(methodName));
                } else {
                    notifyErrorReply(context, "Error while executing the client call. Method type " +
                            methodType.name() + " not supported");
                    return;
                }
                context.setReturnValues();
                return;
            } catch (RuntimeException | GrpcClientException e) {
                notifyErrorReply(context, "gRPC Client Connector Error :" + e.getMessage());
                return;
            }
        }
        notifyErrorReply(context, "Error while processing the request message. Connection Sub " +
                "type not supported");
    }

    private Value getTypeField(BTypeDescValue refField) {
        if (refField == null) {
            return null;
        }
        return ValueImpl.createValue(refField);
    }
}
