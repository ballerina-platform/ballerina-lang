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

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.AbstractNativeAction;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.ClientConnectorFactory;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.stubs.GrpcBlockingStub;
import org.ballerinalang.net.grpc.stubs.GrpcNonBlockingStub;
import org.ballerinalang.net.grpc.stubs.ProtoFileDefinition;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code Connect} is the Connect action implementation of the gRPC Connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.grpc",
        actionName = "connect",
        connectorName = "GRPCConnector",
        args = {
                @Argument(name = "stubType", type = TypeKind.ANY),
                @Argument(name = "descriptorMap", type = TypeKind.MAP),
                @Argument(name = "descriptorKey", type = TypeKind.STRING)
        },
        returnType = {
                @ReturnType(type = TypeKind.STRUCT, structType = MessageConstants.CLIENT_CONNECTION, structPackage =
                        MessageConstants.PROTOCOL_PACKAGE_GRPC),
                @ReturnType(type = TypeKind.STRUCT, structType = "ConnectorError",
                        structPackage = MessageConstants.PROTOCOL_PACKAGE_GRPC),
        },
        connectorArgs = {
                @Argument(name = "host", type = TypeKind.STRING),
                @Argument(name = "port", type = TypeKind.INT)
        }
)
public class Connect extends AbstractNativeAction {

    @Override
    public ConnectorFuture execute(Context context) {
        BConnector bConnector = (BConnector) getRefArgument(context, 0);
        if (bConnector == null) {
            throw new RuntimeException("Error while establishing the connection. BConnector is null.");
        }
        int port = (int) bConnector.getIntField(0);
        String host = bConnector.getStringField(0);
        String stubType = getStringArgument(context, 0);
        String descriptorKey = getStringArgument(context, 1);
        BMap<String, BValue> descriptorMap = (BMap<String, BValue>) getRefArgument(context, 1);
        BStruct outboundError = createStruct(context, "ConnectorError");
        ClientConnectorFuture ballerinaFuture = new ClientConnectorFuture();
        try {
            // If there are more than one descriptors exist, other descriptors are considered as dependent
            // descriptors.  client supported only one depth descriptor dependency.
            List<byte[]> depDescriptorData = new ArrayList<>();
            byte[] descriptorValue = null;
            for (String key : descriptorMap.keySet()) {
                if (descriptorMap.get(key) == null) {
                    continue;
                }
                if (descriptorKey.equals(key)) {
                    descriptorValue = hexStringToByteArray(descriptorMap.get(key).stringValue());
                } else {
                    depDescriptorData.add(hexStringToByteArray(descriptorMap.get(key).stringValue()));
                }
            }

            if (descriptorValue == null) {
                throw new RuntimeException("Error while establishing the connection. service descriptor is null.");
            }
            ProtoFileDefinition protoFileDefinition = new ProtoFileDefinition(descriptorValue, depDescriptorData);

            ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext(true)
                    .build();
            ClientConnectorFactory clientConnectorFactory = new ClientConnectorFactory(protoFileDefinition);
            BStruct outboundResponse = createStruct(context, MessageConstants.CLIENT_CONNECTION);
            outboundResponse.setStringField(0, host);
            outboundResponse.setIntField(0, port);

            if ("blocking".equalsIgnoreCase(stubType)) {
                GrpcBlockingStub grpcBlockingStub = clientConnectorFactory.newBlockingStub(channel);
                outboundResponse.addNativeData("stub", grpcBlockingStub);
            } else if ("non-blocking".equalsIgnoreCase(stubType)) {
                GrpcNonBlockingStub nonBlockingStub = clientConnectorFactory.newNonBlockingStub(channel);
                outboundResponse.addNativeData("stub", nonBlockingStub);
            } else {
                outboundError.setStringField(0, "gRPC Connector Error : Invalid stub type");
                ballerinaFuture.notifyReply(null, outboundError);
                return ballerinaFuture;
            }
            ballerinaFuture.notifyReply(outboundResponse, null);
            return ballerinaFuture;
        } catch (RuntimeException | GrpcClientException e) {
            outboundError.setStringField(0, "gRPC Connector Error :" + e.getMessage());
            ballerinaFuture.notifyReply(null, outboundError);
            return ballerinaFuture;
        }

    }
    
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    
    private BStruct createStruct(Context context, String structName) {
        PackageInfo httpPackageInfo = context.getProgramFile()
                .getPackageInfo(MessageConstants.PROTOCOL_PACKAGE_GRPC);
        StructInfo structInfo = httpPackageInfo.getStructInfo(structName);
        BStructType structType = structInfo.getType();
        return new BStruct(structType);
    }
}
