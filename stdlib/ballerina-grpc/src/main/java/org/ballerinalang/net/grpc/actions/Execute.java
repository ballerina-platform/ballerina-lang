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
import org.ballerinalang.net.grpc.MessageRegistry;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.stubs.DefaultStreamObserver;
import org.ballerinalang.net.grpc.stubs.GrpcBlockingStub;
import org.ballerinalang.net.grpc.stubs.GrpcNonBlockingStub;
import org.ballerinalang.net.grpc.utils.MessageUtil;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code Execute} is the Execute action implementation of the gRPC Connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.grpc",
        actionName = "execute",
        connectorName = "GRPCConnector",
        args = {
                @Argument(name = "conn", type = TypeKind.STRUCT, structType = "Connection", structPackage =
                        "ballerina.net.grpc"),
                @Argument(name = "payload", type = TypeKind.ANY),
                @Argument(name = "methodID", type = TypeKind.INT)
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
            payloadBValue = getRefArgument(context, 2);
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
            com.google.protobuf.Message message = MessageUtil.generateProtoMessage
                    (payloadBValue, methodDescriptor.getInputType());
            Message messageRes = null;
            if (connectionStub.getNativeData("stub") instanceof GrpcBlockingStub) {
                GrpcBlockingStub grpcBlockingStub = (GrpcBlockingStub)
                        connectionStub.getNativeData("stub");
                if (getMethodType(methodDescriptor).equals(MethodDescriptor.MethodType.UNARY)) {
                    messageRes = (Message) grpcBlockingStub.executeUnary(message, methodName);
                } else if ((getMethodType(methodDescriptor).equals(MethodDescriptor.MethodType.SERVER_STREAMING))) {
                    messageRes = (Message) grpcBlockingStub.executeServerStreaming(message, methodName);
                }
            } else if (connectionStub.getNativeData("stub") instanceof GrpcNonBlockingStub) {
                GrpcNonBlockingStub grpcNonBlockingStub = (GrpcNonBlockingStub)
                        connectionStub.getNativeData("stub");
                if (getMethodType(methodDescriptor).equals(MethodDescriptor.MethodType.UNARY)) {
                    grpcNonBlockingStub.executeUnary(message, new DefaultStreamObserver(context), methodName);
                } else if ((getMethodType(methodDescriptor).equals(MethodDescriptor.MethodType.SERVER_STREAMING))) {
                    grpcNonBlockingStub.executeServerStreaming(message, new DefaultStreamObserver(context),
                            methodName);
                } else if ((getMethodType(methodDescriptor).equals(MethodDescriptor.MethodType.CLIENT_STREAMING))) {
                    grpcNonBlockingStub.executeServerStreaming(message, new DefaultStreamObserver(context),
                            methodName);
                }
            } else {
                throw new RuntimeException("Unsupported stub type.");
            }

            Descriptors.Descriptor outputDescriptor = methodDescriptor.getOutputType();

            BValue bValue = MessageUtil.generateRequestStruct(messageRes, outputDescriptor.getName(),
                    getBalType(outputDescriptor.getName(), context), context);
            ballerinaFuture.notifyReply(bValue, null);
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
                .getPackageInfo("ballerina.net.grpc");
        StructInfo structInfo = httpPackageInfo.getStructInfo("ConnectorError");
        BStructType structType = structInfo.getType();
        return new BStruct(structType);
    }
    
}
