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
package org.ballerinalang.net.grpc.nativeimpl.connection.server.serviceendpoint;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.ConnectorUtil;
import org.ballerinalang.net.grpc.GrpcServicesBuilder;
import org.ballerinalang.net.grpc.config.EndPointConfiguration;
import org.ballerinalang.net.grpc.nativeimpl.AbstractGrpcNativeFunction;
import org.ballerinalang.net.grpc.ssl.SSLHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ballerinalang.net.grpc.ConnectorUtil.generateServiceConfiguration;

/**
 * Native function to Init connector.
 *
 * @since 0.96.1
 */
@BallerinaFunction(
        packageName = "ballerina.net.grpc",
        functionName = "init",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ServiceEndpoint",
                structPackage = "ballerina.net.http"),
        args = {@Argument(name = "epName", type = TypeKind.STRING),
                @Argument(name = "config", type = TypeKind.STRUCT, structType = "ServiceEndpointConfiguration")},
        isPublic = true
)
public class Init extends AbstractGrpcNativeFunction {
    private static final Logger log = LoggerFactory.getLogger(Init.class);
    
    @Override
    public BValue[] execute(Context context) {
        try {
            Struct serviceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            Struct serviceEndpointConfig = serviceEndpoint.getStructField("config");
            EndPointConfiguration serviceConfiguration = generateServiceConfiguration(serviceEndpointConfig);
            Service service = BLangConnectorSPIUtil.getServiceRegisted(context);
            Annotation annotation = ConnectorUtil.getServiceConfigAnnotation(service,
                    "ballerina.net.grpc");
            SSLHandlerFactory sslHandlerFactory = ConnectorUtil.getSSLConfigs(annotation);
            io.grpc.ServerBuilder serverBuilder = GrpcServicesBuilder.initService(serviceConfiguration,
                    sslHandlerFactory.createHttp2TLSContext());
            serviceEndpoint.addNativeData("serviceBuilder", serverBuilder);
            return new BValue[] {null};
        } catch (Throwable throwable) {
            // TODO: 3/10/18 write util to generate error struct
            return new BValue[] {null};
            //BStruct errorStruct = null;
            return new BValue[] {null};
        }
    }
}
