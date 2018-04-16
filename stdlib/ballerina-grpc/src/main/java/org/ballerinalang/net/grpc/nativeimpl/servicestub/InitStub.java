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

import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.Message;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.ServiceDefinition;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.stubs.GrpcBlockingStub;
import org.ballerinalang.net.grpc.stubs.GrpcNonBlockingStub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.net.grpc.EndpointConstants.CLIENT_END_POINT;
import static org.ballerinalang.net.grpc.MessageConstants.BLOCKING_TYPE;
import static org.ballerinalang.net.grpc.MessageConstants.CHANNEL_KEY;
import static org.ballerinalang.net.grpc.MessageConstants.CLIENT_ENDPOINT_REF_INDEX;
import static org.ballerinalang.net.grpc.MessageConstants.DESCRIPTOR_KEY_STRING_INDEX;
import static org.ballerinalang.net.grpc.MessageConstants.DESCRIPTOR_MAP_REF_INDEX;
import static org.ballerinalang.net.grpc.MessageConstants.METHOD_DESCRIPTORS;
import static org.ballerinalang.net.grpc.MessageConstants.NON_BLOCKING_TYPE;
import static org.ballerinalang.net.grpc.MessageConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageConstants.SERVICE_STUB;
import static org.ballerinalang.net.grpc.MessageConstants.SERVICE_STUB_REF_INDEX;
import static org.ballerinalang.net.grpc.MessageConstants.STUB_TYPE_STRING_INDEX;

/**
 * {@code InitStub} is the InitStub function implementation of the gRPC service stub.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "initStub",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = SERVICE_STUB,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {
                @Argument(name = "clientEndpoint", type = TypeKind.STRUCT, structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC,
                        structType = "Client"),
                @Argument(name = "stubType", type = TypeKind.STRING),
                @Argument(name = "descriptorKey", type = TypeKind.STRING),
                @Argument(name = "descriptorMap", type = TypeKind.MAP)
        },
        isPublic = true
)
public class InitStub extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BStruct serviceStub = (BStruct) context.getRefArgument(SERVICE_STUB_REF_INDEX);
        BStruct clientEndpoint = (BStruct) context.getRefArgument(CLIENT_ENDPOINT_REF_INDEX);
        Channel channel = (Channel) clientEndpoint.getNativeData(CHANNEL_KEY);
        String stubType = context.getStringArgument(STUB_TYPE_STRING_INDEX);
        String descriptorKey = context.getStringArgument(DESCRIPTOR_KEY_STRING_INDEX);
        BMap<String, BValue> descriptorMap = (BMap<String, BValue>) context.getRefArgument(DESCRIPTOR_MAP_REF_INDEX);

        if (stubType == null || descriptorKey == null || descriptorMap == null) {
            context.setError(MessageUtils.getConnectorError(context, new StatusRuntimeException(Status
                    .fromCode(Status.INTERNAL.getCode()).withDescription("Error while initializing connector. " +
                            "message descriptor keys not exist. Please check the generated sub file"))));
            return;
        }

        try {
            // If there are more than one descriptors exist, other descriptors are considered as dependent
            // descriptors.  client supported only one depth descriptor dependency.
            List<byte[]> dependentDescriptors = new ArrayList<>();
            byte[] fileDescriptor = null;
            for (String key : descriptorMap.keySet()) {
                if (descriptorMap.get(key) == null) {
                    continue;
                }
                if (descriptorKey.equals(key)) {
                    fileDescriptor = hexStringToByteArray(descriptorMap.get(key).stringValue());
                } else {
                    dependentDescriptors.add(hexStringToByteArray(descriptorMap.get(key).stringValue()));
                }
            }

            if (fileDescriptor == null) {
                context.setError(MessageUtils.getConnectorError(context, new StatusRuntimeException(Status
                        .fromCode(Status.INTERNAL.getCode()).withDescription("Error while establishing the connection" +
                                ". service descriptor is null."))));
                return;
            }
            ServiceDefinition serviceDefinition = new ServiceDefinition(fileDescriptor, dependentDescriptors);
            Map<String, MethodDescriptor<Message, Message>> methodDescriptorMap = serviceDefinition
                    .getMethodDescriptors();

            serviceStub.addNativeData(METHOD_DESCRIPTORS, methodDescriptorMap);
            if (BLOCKING_TYPE.equalsIgnoreCase(stubType)) {
                GrpcBlockingStub grpcBlockingStub = new GrpcBlockingStub(channel);
                serviceStub.addNativeData(SERVICE_STUB, grpcBlockingStub);
            } else if (NON_BLOCKING_TYPE.equalsIgnoreCase(stubType)) {
                GrpcNonBlockingStub nonBlockingStub = new GrpcNonBlockingStub(channel);
                serviceStub.addNativeData(SERVICE_STUB, nonBlockingStub);
            } else {
                context.setError(MessageUtils.getConnectorError(context, new StatusRuntimeException(Status
                        .fromCode(Status.INTERNAL.getCode()).withDescription("Error while initializing connector. " +
                                "invalid connector type"))));
                return;
            }
            serviceStub.addNativeData(CLIENT_END_POINT, clientEndpoint);
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
