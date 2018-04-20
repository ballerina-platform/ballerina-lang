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

import io.netty.handler.ssl.SslContext;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.GrpcServicesBuilder;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.config.EndpointConfiguration;
import org.ballerinalang.net.grpc.nativeimpl.EndpointUtils;
import org.ballerinalang.net.grpc.ssl.SSLHandlerFactory;

import static org.ballerinalang.net.grpc.MessageConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.MessageConstants.SERVICE_BUILDER;
import static org.ballerinalang.net.grpc.MessageConstants.SERVICE_ENDPOINT_TYPE;

/**
 * Native function for initializing gRPC server endpoint.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "init",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = SERVICE_ENDPOINT_TYPE,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {@Argument(name = "config", type = TypeKind.STRUCT, structType = "ServiceEndpointConfiguration",
                        structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC)},
        isPublic = true
)
public class Init extends BlockingNativeCallableUnit {
    
    @Override
    public void execute(Context context) {
        try {
            Struct serviceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            BStruct endpointConfigStruct = (BStruct) context.getRefArgument(1);
            Struct serviceEndpointConfig = BLangConnectorSPIUtil.toStruct(endpointConfigStruct);
            EndpointConfiguration configuration = EndpointUtils.getEndpointConfiguration(serviceEndpointConfig);
            io.grpc.ServerBuilder serverBuilder;
            if (configuration.getSslConfig() != null) {
                SslContext sslCtx = new SSLHandlerFactory(configuration.getSslConfig())
                        .createHttp2TLSContextForServer();
                serverBuilder = GrpcServicesBuilder.initService(configuration, sslCtx);
            } else {
                serverBuilder = GrpcServicesBuilder.initService(configuration, null);
            }
            serviceEndpoint.addNativeData(SERVICE_BUILDER, serverBuilder);
            context.setReturnValues();
        } catch (Throwable throwable) {
            BStruct err = getConnectorError(context, throwable);
            context.setError(err);
        }
    }
    
    private static BStruct getConnectorError(Context context, Throwable throwable) {
        return MessageUtils.getConnectorError(context, throwable);
    }
}
