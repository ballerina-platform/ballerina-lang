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
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.connector.impl.ValueImpl;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageHeaders;
import org.ballerinalang.net.grpc.MessageRegistry;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.stubs.DefaultStreamObserver;
import org.ballerinalang.net.grpc.stubs.GrpcNonBlockingStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.ballerinalang.net.grpc.EndpointConstants.CLIENT_END_POINT;
import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT;
import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_CONNECTION;
import static org.ballerinalang.net.grpc.GrpcConstants.CONNECTOR_ERROR;
import static org.ballerinalang.net.grpc.GrpcConstants.METHOD_DESCRIPTORS;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.REQUEST_MESSAGE_DEFINITION;
import static org.ballerinalang.net.grpc.GrpcConstants.REQUEST_SENDER;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_STUB;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_STUB_REF_INDEX;
import static org.ballerinalang.net.grpc.MessageUtils.getMessageHeaders;

/**
 * {@code StreamingExecute} is the StreamingExecute action implementation of the gRPC Connector.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "streamingExecute",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = SERVICE_STUB,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {
                @Argument(name = "methodID", type = TypeKind.STRING),
                @Argument(name = "listenerService", type = TypeKind.TYPEDESC),
                @Argument(name = "headers", type = TypeKind.ARRAY)
        },
        returnType = {
                @ReturnType(type = TypeKind.STRUCT, structType = CLIENT,
                        structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
                @ReturnType(type = TypeKind.STRUCT, structType = CONNECTOR_ERROR,
                        structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        },
        isPublic = true
)
public class StreamingExecute extends AbstractExecute {
    private static final Logger LOG = LoggerFactory.getLogger(StreamingExecute.class);
    private static final int MESSAGE_HEADER_REF_INDEX = 2;
    
    @Override
    public void execute(Context context) {
        BStruct serviceStub = (BStruct) context.getRefArgument(SERVICE_STUB_REF_INDEX);
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
            GrpcNonBlockingStub grpcNonBlockingStub = (GrpcNonBlockingStub) connectionStub;
            
            // Attach header read/write listener to the service stub.
            AtomicReference<Metadata> headerCapture = new AtomicReference<>();
            AtomicReference<Metadata> trailerCapture = new AtomicReference<>();
            if (headers != null) {
                grpcNonBlockingStub = MetadataUtils.attachHeaders(grpcNonBlockingStub, headers.getMessageMetadata());
            }
            grpcNonBlockingStub = MetadataUtils.captureMetadata(grpcNonBlockingStub, headerCapture, trailerCapture);
            
            BTypeDescValue serviceType = (BTypeDescValue) context.getRefArgument(1);
            Service callbackService = BLangConnectorSPIUtil.getServiceFromType(context.getProgramFile(), getTypeField
                    (serviceType));
            try {
                MethodDescriptor.MethodType methodType = getMethodType(methodDescriptor);
                DefaultStreamObserver responseObserver = new DefaultStreamObserver(callbackService, headerCapture);
                StreamObserver<Message> requestSender;
                if (methodType.equals(MethodDescriptor.MethodType.CLIENT_STREAMING)) {
                    requestSender = grpcNonBlockingStub.executeClientStreaming
                            (responseObserver, methodDescriptors.get(methodName));
                    
                } else if (methodType.equals(MethodDescriptor.MethodType.BIDI_STREAMING)) {
                    requestSender = grpcNonBlockingStub.executeBidiStreaming
                            (responseObserver, methodDescriptors.get(methodName));
                } else {
                    notifyErrorReply(context, "Error while executing the client call. Method type " +
                            methodType.name() + " not supported");
                    return;
                }
                BStruct connStruct = createStruct(context, CLIENT_CONNECTION);
                connStruct.addNativeData(REQUEST_SENDER, requestSender);
                connStruct.addNativeData(REQUEST_MESSAGE_DEFINITION, methodDescriptor
                        .getInputType());
                BStruct clientEndpoint = (BStruct) serviceStub.getNativeData(CLIENT_END_POINT);
                clientEndpoint.addNativeData(CLIENT_CONNECTION, connStruct);
                context.setReturnValues(clientEndpoint);
            } catch (RuntimeException | GrpcClientException e) {
                notifyErrorReply(context, "gRPC Client Connector Error :" + e.getMessage());
            }
        }
    }
    
    private Value getTypeField(BTypeDescValue refField) {
        if (refField == null) {
            return null;
        }
        return ValueImpl.createValue(refField);
    }
}
