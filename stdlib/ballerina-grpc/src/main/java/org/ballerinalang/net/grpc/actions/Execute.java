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
package org.ballerinalang.net.grpc.actions;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.AbstractNativeAction;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.MessageRegistry;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.exception.UnsupportedFieldTypeException;
import org.ballerinalang.net.grpc.stubs.DefaultStreamObserver;
import org.ballerinalang.net.grpc.stubs.GrpcBlockingStub;
import org.ballerinalang.net.grpc.stubs.GrpcNonBlockingStub;
import org.ballerinalang.net.grpc.utils.MessageUtil;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * {@code Execute} is the Execute action implementation of the gRPC Connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.grpc",
        actionName = "execute",
        connectorName = "GRPCConnector",
        args = {
                @Argument(name = "conn", type = TypeKind.STRUCT, structType = MessageConstants.CLIENT_CONNECTION,
                        structPackage = MessageConstants.PROTOCOL_PACKAGE_GRPC),
                @Argument(name = "payload", type = TypeKind.ANY),
                @Argument(name = "methodID", type = TypeKind.STRING),
                @Argument(name = "listenerService", type = TypeKind.STRING)

        },
        returnType = {
                @ReturnType(type = TypeKind.ANY),
                @ReturnType(type = TypeKind.STRUCT, structType = "ConnectorError",
                        structPackage = "ballerina.net.grpc"),
        },
        connectorArgs = {
                @Argument(name = "host", type = TypeKind.STRING),
                @Argument(name = "port", type = TypeKind.INT)
        }
)
public class Execute extends AbstractNativeAction {
    
    private static final Logger logger = LoggerFactory.getLogger(Execute.class);
    
    @Override
    public ConnectorFuture execute(Context context) {
        ClientConnectorFuture ballerinaFuture = new ClientConnectorFuture();
        BStruct outboundError = createStruct(context);
        BValue payloadBValue;
        BStruct connectionStub;
        String methodName;
        try {
            connectionStub = (BStruct) getRefArgument(context, 1);
            methodName = getStringArgument(context, 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            outboundError.setStringField(0, "gRPC Connector Error :" + e.getMessage());
            ballerinaFuture.notifyReply(null, outboundError);
            return ballerinaFuture;
        }

        try {
            com.google.protobuf.Descriptors.MethodDescriptor methodDescriptor = MessageRegistry.getInstance()
                    .getMethodDescriptor(methodName);
            Message requestMsg;
            Message responseMsg = null;
            BValue responseBValue = null;
            if (connectionStub.getNativeData("stub") instanceof GrpcBlockingStub) {
                payloadBValue = getRefArgument(context, 2);
                requestMsg = MessageUtil.generateProtoMessage(payloadBValue, methodDescriptor.getInputType());
                GrpcBlockingStub grpcBlockingStub = (GrpcBlockingStub) connectionStub.getNativeData("stub");
                if (getMethodType(methodDescriptor).equals(MethodDescriptor.MethodType.UNARY)) {
                    responseMsg = grpcBlockingStub.executeUnary(requestMsg, methodName);
                } else if ((getMethodType(methodDescriptor).equals(MethodDescriptor.MethodType.SERVER_STREAMING))) {
                    java.util.Iterator<Message> messageIterator = grpcBlockingStub.executeServerStreaming
                            (requestMsg, methodName);
                } else {
                    throw new UnsupportedFieldTypeException("Error while executing the client call, called method " +
                            "type is unknown");
                }
                Descriptors.Descriptor outputDescriptor = methodDescriptor.getOutputType();
                responseBValue = MessageUtil.generateRequestStruct(responseMsg, outputDescriptor.getName(),
                        getBalType(outputDescriptor.getName(), context), context);
            } else if (connectionStub.getNativeData("stub") instanceof GrpcNonBlockingStub) {
                GrpcNonBlockingStub grpcNonBlockingStub = (GrpcNonBlockingStub) connectionStub.getNativeData("stub");
                if (getMethodType(methodDescriptor).equals(MethodDescriptor.MethodType.UNARY)) {
                    payloadBValue = getRefArgument(context, 2);
                    requestMsg = MessageUtil.generateProtoMessage(payloadBValue, methodDescriptor.getInputType());
                    String listenerService = getStringArgument(context, 1);
                    grpcNonBlockingStub.executeUnary(requestMsg, new DefaultStreamObserver(context, listenerService),
                            methodName);
                } else if ((getMethodType(methodDescriptor).equals(MethodDescriptor.MethodType.SERVER_STREAMING))) {
                    payloadBValue = getRefArgument(context, 2);
                    requestMsg = MessageUtil.generateProtoMessage(payloadBValue, methodDescriptor.getInputType());
                    String listenerService = getStringArgument(context, 1);
                    grpcNonBlockingStub.executeServerStreaming(requestMsg, new DefaultStreamObserver(context,
                            listenerService), methodName);
                } else if ((getMethodType(methodDescriptor).equals(MethodDescriptor.MethodType.CLIENT_STREAMING))) {
                    String listenerService = getStringArgument(context, 1);
                    DefaultStreamObserver responseObserver = new DefaultStreamObserver(context, listenerService);
                    StreamObserver<Message> requestSender = grpcNonBlockingStub.executeClientStreaming
                            (responseObserver, methodName);
                    responseObserver.registerRequestSender(requestSender, methodDescriptor.getInputType());
                } else if ((getMethodType(methodDescriptor).equals(MethodDescriptor.MethodType.BIDI_STREAMING))) {
                    String listenerService = getStringArgument(context, 1);
                    DefaultStreamObserver responseObserver = new DefaultStreamObserver(context, listenerService);
                    StreamObserver<Message> requestSender = grpcNonBlockingStub.executeBidiStreaming
                            (responseObserver, methodName);
                    responseObserver.registerRequestSender(requestSender, methodDescriptor.getInputType());
                }
            } else {
                throw new RuntimeException("Unsupported stub type.");
            }


            ballerinaFuture.notifyReply(responseBValue, null);
            return ballerinaFuture;
        } catch (RuntimeException | GrpcClientException e) {
            outboundError.setStringField(0, "gRPC Connector Error :" + e.getMessage());
            ballerinaFuture.notifyReply(null, outboundError);
            return ballerinaFuture;
        }
    }
    
    /**
     * Returns corresponding Ballerina type for the proto buffer type.
     *
     * @param protoType Protocol buffer type
     * @param context Ballerina Context
     * @return .
     */
    private BType getBalType(String protoType, Context context) {
        
        if (protoType.equalsIgnoreCase("DoubleValue") || protoType
                .equalsIgnoreCase("FloatValue")) {
            return BTypes.typeFloat;
        } else if (protoType.equalsIgnoreCase("Int32Value") || protoType
                .equalsIgnoreCase("Int64Value") || protoType
                .equalsIgnoreCase("UInt32Value") || protoType
                .equalsIgnoreCase("UInt64Value")) {
            return BTypes.typeInt;
        } else if (protoType.equalsIgnoreCase("BoolValue")) {
            return BTypes.typeBoolean;
        } else if (protoType.equalsIgnoreCase("StringValue")) {
            return BTypes.typeString;
        } else if (protoType.equalsIgnoreCase("BytesValue")) {
            return BTypes.typeBlob;
        } else {
            // TODO: 2/22/18 enum
            return context.getProgramFile().getEntryPackage().getStructInfo(protoType).getType();
        }
    }
    
    public static MethodDescriptor.MethodType getMethodType(Descriptors.MethodDescriptor
                                                                    methodDescriptor) throws GrpcClientException {
        if (methodDescriptor == null) {
            throw new GrpcClientException("Method descriptor cannot be null.");
        }
        DescriptorProtos.MethodDescriptorProto methodDescriptorProto = methodDescriptor.toProto();
        if (methodDescriptorProto.getClientStreaming() && methodDescriptorProto.getServerStreaming()) {
            return MethodDescriptor.MethodType.BIDI_STREAMING;
        } else if (!(methodDescriptorProto.getClientStreaming() || methodDescriptorProto.getServerStreaming())) {
            return MethodDescriptor.MethodType.UNARY;
        } else if (methodDescriptorProto.getServerStreaming()) {
            return MethodDescriptor.MethodType.SERVER_STREAMING;
        } else if (methodDescriptorProto.getClientStreaming()) {
            return MethodDescriptor.MethodType.CLIENT_STREAMING;
        } else {
            return MethodDescriptor.MethodType.UNKNOWN;
        }
    }
    
    private BStruct createStruct(Context context) {
        
        PackageInfo httpPackageInfo = context.getProgramFile()
                .getPackageInfo(MessageConstants.PROTOCOL_PACKAGE_GRPC);
        StructInfo structInfo = httpPackageInfo.getStructInfo("ConnectorError");
        BStructType structType = structInfo.getType();
        return new BStruct(structType);
    }
    
}
