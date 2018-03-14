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
import org.ballerinalang.net.grpc.config.EndpointConfiguration;
import org.ballerinalang.net.grpc.nativeimpl.EndpointUtils;
import org.ballerinalang.net.grpc.ssl.SSLHandlerFactory;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Native function to InitEndpoint connector.
 *
 * @since 0.96.1
 */
@BallerinaFunction(
        packageName = "ballerina.net.grpc",
        functionName = "initEndpoint",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Service",
                structPackage = "ballerina.net.grpc"),
        args = {@Argument(name = "epName", type = TypeKind.STRING),
                @Argument(name = "config", type = TypeKind.STRUCT, structType = "ServiceEndpointConfiguration")},
        isPublic = true
)
public class InitEndpoint extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(InitEndpoint.class);
    
    @Override
    public void execute(Context context) {
        try {
            Struct serviceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            Struct serviceEndpointConfig = serviceEndpoint.getStructField("config");
            EndpointConfiguration configuration = EndpointUtils.getEndpointConfiguration(serviceEndpointConfig);
            io.grpc.ServerBuilder serverBuilder;
            if (configuration.getSslConfig() != null) {
                TrustManagerFactory tmf = SSLHandlerFactory.generateTrustManagerFactory(configuration.getSslConfig());
                KeyManagerFactory kmf = SSLHandlerFactory.generateKeyManagerFactory(configuration.getSslConfig());
                serverBuilder = GrpcServicesBuilder.initService(configuration,
                        SSLHandlerFactory.createSSLContext(configuration.getSslConfig(), tmf, kmf));
            } else {
                serverBuilder = GrpcServicesBuilder.initService(configuration, null);
            }
            serviceEndpoint.addNativeData("serviceBuilder", serverBuilder);
            context.setReturnValues();
        } catch (Throwable throwable) {
            BStruct err = getHttpConnectorError(context, throwable);
            context.setError(err);
        }
    }
    
    private static BStruct getHttpConnectorError(Context context, Throwable throwable) {
        PackageInfo httpPackageInfo = context.getProgramFile()
                .getPackageInfo("ballerina.net.grpc");
        StructInfo errorStructInfo = httpPackageInfo.getStructInfo("ConnectorError");
        BStruct httpConnectorError = new BStruct(errorStructInfo.getType());
        if (throwable.getMessage() == null) {
            httpConnectorError.setStringField(0, "Service Initialization error.");
        } else {
            httpConnectorError.setStringField(0, throwable.getMessage());
        }
        return httpConnectorError;
    }
}
