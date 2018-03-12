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
package org.ballerinalang.net.grpc.nativeimpl.connection.client.clientendpoint;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.ClientConnectorFactory;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.stubs.GrpcBlockingStub;
import org.ballerinalang.net.grpc.stubs.GrpcNonBlockingStub;
import org.ballerinalang.net.grpc.stubs.ProtoFileDefinition;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.net.grpc.EndpointConstants.CHANNEL_KEY;
import static org.ballerinalang.net.grpc.EndpointConstants.CLIENT_CONNECTOR;
import static org.ballerinalang.net.grpc.EndpointConstants.CLIENT_ENDPOINT_INDEX;
import static org.ballerinalang.net.grpc.EndpointConstants.CLIENT_STUB;
import static org.ballerinalang.net.grpc.EndpointConstants.GRPC_PACKAGE_PATH;

/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */
@BallerinaFunction(
        packageName = "ballerina.net.grpc",
        functionName = "getStub",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Client",
                structPackage = "ballerina.net.grpc"),
        returnType = {@ReturnType(type = TypeKind.CONNECTOR)},
        isPublic = true
)
public class GetStub extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BStruct clientEndPoint = (BStruct) context.getRefArgument(CLIENT_ENDPOINT_INDEX);
        BConnector clientConnector = BLangConnectorSPIUtil.createBConnector(context.getProgramFile(), GRPC_PACKAGE_PATH,
                CLIENT_CONNECTOR);

        BStruct clientStub = (BStruct) clientEndPoint.getNativeData(CLIENT_STUB);
        ManagedChannel channel = (ManagedChannel) clientEndPoint.getNativeData(CHANNEL_KEY);
        String subtype = clientStub.getStringField(0);
        String descriptorKey = clientStub.getStringField(1);
        BMap<String, BValue> descriptorMap = (BMap<String, BValue>) clientStub.getRefField(0);

        if (subtype == null || descriptorKey == null || descriptorMap == null) {
            context.setError(MessageUtils.getConnectorError(context, new StatusRuntimeException(Status
                    .fromCode(Status.INTERNAL.getCode()).withDescription("Error while initializing connector. " +
                            "message descriptor keys not exist. Please check the generated sub file"))));
            return;
        }

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
                context.setError(MessageUtils.getConnectorError(context, new StatusRuntimeException(Status
                        .fromCode(Status.INTERNAL.getCode()).withDescription("Error while establishing the connection" +
                                ". service descriptor is null."))));
                return;
            }
            ProtoFileDefinition protoFileDefinition = new ProtoFileDefinition(depDescriptorData);
            protoFileDefinition.setRootDescriptorData(descriptorValue);

            ClientConnectorFactory clientConnectorFactory = new ClientConnectorFactory(protoFileDefinition);

            if ("blocking".equalsIgnoreCase(subtype)) {
                GrpcBlockingStub grpcBlockingStub = clientConnectorFactory.newBlockingStub(channel);
                clientConnector.setNativeData("stub", grpcBlockingStub);
            } else if ("non-blocking".equalsIgnoreCase(subtype)) {
                GrpcNonBlockingStub nonBlockingStub = clientConnectorFactory.newNonBlockingStub(channel);
                clientConnector.setNativeData("stub", nonBlockingStub);
            } else {
                context.setError(MessageUtils.getConnectorError(context, new StatusRuntimeException(Status
                        .fromCode(Status.INTERNAL.getCode()).withDescription("Error while initializing connector. " +
                                "invalid connector type"))));
                return;
            }
            clientStub.addNativeData(CLIENT_CONNECTOR, clientConnector);
        } catch (RuntimeException | GrpcClientException e) {
            context.setError(MessageUtils.getConnectorError(context, e));
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
}
