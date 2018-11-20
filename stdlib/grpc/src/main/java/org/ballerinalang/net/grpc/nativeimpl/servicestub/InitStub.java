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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
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

import static org.ballerinalang.bre.bvm.BLangVMErrors.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.net.grpc.GrpcConstants.BLOCKING_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.CALLER_ACTIONS;
import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_ENDPOINT_CONFIG;
import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_ENDPOINT_REF_INDEX;
import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_END_POINT;
import static org.ballerinalang.net.grpc.GrpcConstants.DESCRIPTOR_KEY_STRING_INDEX;
import static org.ballerinalang.net.grpc.GrpcConstants.DESCRIPTOR_MAP_REF_INDEX;
import static org.ballerinalang.net.grpc.GrpcConstants.METHOD_DESCRIPTORS;
import static org.ballerinalang.net.grpc.GrpcConstants.NON_BLOCKING_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_STUB;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_STUB_REF_INDEX;
import static org.ballerinalang.net.grpc.GrpcConstants.STUB_TYPE_STRING_INDEX;
import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG;

/**
 * {@code InitStub} is the InitStub function implementation of the gRPC service stub.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "initStub",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = SERVICE_STUB,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {
                @Argument(name = "clientEndpoint", type = TypeKind.OBJECT, structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC,
                        structType = "Client"),
                @Argument(name = "stubType", type = TypeKind.STRING),
                @Argument(name = "descriptorKey", type = TypeKind.STRING),
                @Argument(name = "descriptorMap", type = TypeKind.MAP)
        },
        returnType = {
                @ReturnType(type = TypeKind.RECORD, structType = STRUCT_GENERIC_ERROR,
                        structPackage = BALLERINA_BUILTIN_PKG)
        },
        isPublic = true
)
public class InitStub extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BMap<String, BValue> serviceStub = (BMap<String, BValue>) context.getRefArgument(SERVICE_STUB_REF_INDEX);
        BMap<String, BValue> clientEndpoint = (BMap<String, BValue>) context.getRefArgument(CLIENT_ENDPOINT_REF_INDEX);
        HttpClientConnector clientConnector = (HttpClientConnector) clientEndpoint.getNativeData(CALLER_ACTIONS);
        Struct endpointConfig = (Struct) clientEndpoint.getNativeData(CLIENT_ENDPOINT_CONFIG);
        String stubType = context.getStringArgument(STUB_TYPE_STRING_INDEX);
        String descriptorKey = context.getStringArgument(DESCRIPTOR_KEY_STRING_INDEX);
        BMap<String, BValue> descriptorMap = (BMap<String, BValue>) context.getRefArgument(DESCRIPTOR_MAP_REF_INDEX);
        
        if (stubType == null || descriptorKey == null || descriptorMap == null) {
            context.setReturnValues(MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error while initializing " +
                            "connector. message descriptor keys not exist. Please check the generated sub file"))));
            return;
        }
        
        try {
            if (!descriptorMap.hasKey(descriptorKey)) {
                context.setReturnValues(MessageUtils.getConnectorError(new StatusRuntimeException(Status
                        .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error while " +
                                "establishing the connection. service descriptor is null."))));
                return;
            }
            ServiceDefinition serviceDefinition = new ServiceDefinition(descriptorMap.get(descriptorKey).stringValue(),
                    descriptorMap);
            Map<String, MethodDescriptor> methodDescriptorMap = serviceDefinition.getMethodDescriptors(context);
            
            serviceStub.addNativeData(METHOD_DESCRIPTORS, methodDescriptorMap);
            if (BLOCKING_TYPE.equalsIgnoreCase(stubType)) {
                BlockingStub blockingStub = new BlockingStub(clientConnector, endpointConfig);
                serviceStub.addNativeData(SERVICE_STUB, blockingStub);
            } else if (NON_BLOCKING_TYPE.equalsIgnoreCase(stubType)) {
                NonBlockingStub nonBlockingStub = new NonBlockingStub(clientConnector, endpointConfig);
                serviceStub.addNativeData(SERVICE_STUB, nonBlockingStub);
            } else {
                context.setReturnValues(MessageUtils.getConnectorError(new StatusRuntimeException(Status
                        .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error while " +
                                "initializing connector. invalid connector type"))));
                return;
            }
            serviceStub.addNativeData(CLIENT_END_POINT, clientEndpoint);
        } catch (RuntimeException | GrpcClientException e) {
            context.setReturnValues(MessageUtils.getConnectorError(e));
        }
    }
}
