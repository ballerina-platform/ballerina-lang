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

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.ServerConnectorListener;
import org.ballerinalang.net.grpc.ServerConnectorPortBindingListener;
import org.ballerinalang.net.grpc.ServicesRegistry;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.ballerinalang.net.grpc.nativeimpl.AbstractGrpcNativeFunction;
import org.ballerinalang.net.http.HttpConstants;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;

/**
 * Extern function to start gRPC server instance.
 *
 * @since 1.0.0
 */
public class Start extends AbstractGrpcNativeFunction {

    private static Object startServerConnector(ObjectValue listener, ServicesRegistry servicesRegistry) {
        ServerConnector serverConnector = getServerConnector(listener);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(new ServerConnectorListener(servicesRegistry));

        serverConnectorFuture.setPortBindingEventListener(new ServerConnectorPortBindingListener());
        try {
            serverConnectorFuture.sync();
        } catch (Exception ex) {
            return MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription(
                            "Failed to start server connector '" + serverConnector.getConnectorID()
                            + "'. " + ex.getMessage())));
        }
        listener.addNativeData(HttpConstants.CONNECTOR_STARTED, true);
        return null;
    }

    public static Object externStart(ObjectValue listener) {
        ServicesRegistry.Builder servicesRegistryBuilder = getServiceRegistryBuilder(listener);

        if (!isConnectorStarted(listener)) {
            return startServerConnector(listener, servicesRegistryBuilder.build());
        }
        return null;
    }
}
