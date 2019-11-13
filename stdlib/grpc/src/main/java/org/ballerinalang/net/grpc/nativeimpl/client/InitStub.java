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
package org.ballerinalang.net.grpc.nativeimpl.client;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.ServiceDefinition;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.ballerinalang.net.grpc.stubs.BlockingStub;
import org.ballerinalang.net.grpc.stubs.NonBlockingStub;
import org.wso2.transport.http.netty.contract.HttpClientConnector;

import java.util.Map;

import static org.ballerinalang.net.grpc.GrpcConstants.BLOCKING_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_CONNECTOR;
import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_ENDPOINT_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.ENDPOINT_URL;
import static org.ballerinalang.net.grpc.GrpcConstants.METHOD_DESCRIPTORS;
import static org.ballerinalang.net.grpc.GrpcConstants.NON_BLOCKING_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_STUB;

/**
 * {@code InitStub} is the InitStub function implementation of the gRPC service stub.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "initStub",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CLIENT_ENDPOINT_TYPE,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        isPublic = true
)
public class InitStub {

    public static Object initStub(Strand strand, ObjectValue genericEndpoint, ObjectValue clientEndpoint,
                                  String stubType, String rootDescriptor, MapValue<String, Object> descriptorMap) {
        HttpClientConnector clientConnector = (HttpClientConnector) genericEndpoint.getNativeData(CLIENT_CONNECTOR);
        String urlString = (String) genericEndpoint.getNativeData(ENDPOINT_URL);

        if (stubType == null || rootDescriptor == null || descriptorMap == null) {
            return MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error while initializing " +
                            "connector. message descriptor keys not exist. Please check the generated sub file")));
        }

        try {
            ServiceDefinition serviceDefinition = new ServiceDefinition(rootDescriptor, descriptorMap);
            Map<String, MethodDescriptor> methodDescriptorMap =
                    serviceDefinition.getMethodDescriptors(clientEndpoint.getType());

            genericEndpoint.addNativeData(METHOD_DESCRIPTORS, methodDescriptorMap);
            if (BLOCKING_TYPE.equalsIgnoreCase(stubType)) {
                BlockingStub blockingStub = new BlockingStub(clientConnector, urlString);
                genericEndpoint.addNativeData(SERVICE_STUB, blockingStub);
            } else if (NON_BLOCKING_TYPE.equalsIgnoreCase(stubType)) {
                NonBlockingStub nonBlockingStub = new NonBlockingStub(clientConnector, urlString);
                genericEndpoint.addNativeData(SERVICE_STUB, nonBlockingStub);
            } else {
                return MessageUtils.getConnectorError(new StatusRuntimeException(Status
                        .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error while " +
                                "initializing connector. invalid connector type")));
            }
        } catch (RuntimeException | GrpcClientException e) {
            return MessageUtils.getConnectorError(e);
        }
        return null;
    }
}
