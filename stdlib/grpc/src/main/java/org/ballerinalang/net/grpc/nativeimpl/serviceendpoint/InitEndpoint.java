/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc.nativeimpl.serviceendpoint;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.ServicesRegistry;
import org.ballerinalang.net.grpc.nativeimpl.AbstractGrpcNativeFunction;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpConstants;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;

import static org.ballerinalang.net.grpc.GrpcConstants.LISTENER;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVER_CONNECTOR;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_REGISTRY_BUILDER;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_PORT;
import static org.ballerinalang.net.http.HttpUtil.getListenerConfig;

/**
 * Extern function for initializing gRPC server endpoint.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "initEndpoint",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = LISTENER,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        isPublic = true
)
public class InitEndpoint extends AbstractGrpcNativeFunction {

    public static Object initEndpoint(Strand strand, ObjectValue listenerObject) {
        MapValue serviceEndpointConfig = listenerObject.getMapValue(HttpConstants.SERVICE_ENDPOINT_CONFIG);
        long port = listenerObject.getIntValue(ENDPOINT_CONFIG_PORT);
        ListenerConfiguration configuration = getListenerConfig(port, serviceEndpointConfig);
        try {
            ServerConnector httpServerConnector =
                    HttpConnectionManager.getInstance().createHttpServerConnector(configuration);
            ServicesRegistry.Builder servicesRegistryBuilder = new ServicesRegistry.Builder();
            listenerObject.addNativeData(SERVER_CONNECTOR, httpServerConnector);
            listenerObject.addNativeData(SERVICE_REGISTRY_BUILDER, servicesRegistryBuilder);
            return null;
        } catch (Exception e) {
            // TODO: Add proper error object.
            return BallerinaErrors.createError("{ballerina/grpc}INTERNAL_ERROR");
        }
    }

}
