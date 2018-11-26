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
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.ServerConnectorListener;
import org.ballerinalang.net.grpc.ServerConnectorPortBindingListener;
import org.ballerinalang.net.grpc.ServicesRegistry;
import org.ballerinalang.net.grpc.nativeimpl.AbstractGrpcNativeFunction;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;

import static org.ballerinalang.net.grpc.GrpcConstants.LISTENER;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;

/**
 * Extern function to start gRPC server instance.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "start",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = LISTENER,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        isPublic = true
)
public class Start extends AbstractGrpcNativeFunction {

    @Override
    public void execute(Context context) {
        Struct serviceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        ServicesRegistry.Builder servicesRegistryBuilder = getServiceRegistryBuilder(serviceEndpoint);
        if (!isConnectorStarted(serviceEndpoint)) {
            startServerConnector(serviceEndpoint, servicesRegistryBuilder.build());
        }
        context.setReturnValues();
    }

    private void startServerConnector(Struct serviceEndpoint, ServicesRegistry servicesRegistry) {
        ServerConnector serverConnector = getServerConnector(serviceEndpoint);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(new ServerConnectorListener(servicesRegistry));

        serverConnectorFuture.setPortBindingEventListener(new ServerConnectorPortBindingListener());
        try {
            serverConnectorFuture.sync();
        } catch (Exception ex) {
            throw new BallerinaException("failed to start server connector '" + serverConnector.getConnectorID()
                    + "': " + ex.getMessage(), ex);
        }
        serviceEndpoint.addNativeData(HttpConstants.CONNECTOR_STARTED, true);
    }
}
