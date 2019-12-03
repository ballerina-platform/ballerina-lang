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

import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.ServicesRegistry;
import org.ballerinalang.net.grpc.nativeimpl.AbstractGrpcNativeFunction;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;

import static org.ballerinalang.net.grpc.GrpcConstants.SERVER_CONNECTOR;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_REGISTRY_BUILDER;
import static org.ballerinalang.net.grpc.GrpcUtil.getListenerConfig;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_PORT;

/**
 * Extern function for initializing gRPC server endpoint.
 *
 * @since 1.0.0
 */
public class InitEndpoint extends AbstractGrpcNativeFunction {
    private static final Logger LOG = LoggerFactory.getLogger(InitEndpoint.class);

    public static Object externInitEndpoint(ObjectValue listenerObject) {
        MapValue serviceEndpointConfig = listenerObject.getMapValue(HttpConstants.SERVICE_ENDPOINT_CONFIG);
        long port = listenerObject.getIntValue(ENDPOINT_CONFIG_PORT);
        try {
            ListenerConfiguration configuration = getListenerConfig(port, serviceEndpointConfig);
            ServerConnector httpServerConnector =
                    HttpConnectionManager.getInstance().createHttpServerConnector(configuration);
            ServicesRegistry.Builder servicesRegistryBuilder = new ServicesRegistry.Builder();
            listenerObject.addNativeData(SERVER_CONNECTOR, httpServerConnector);
            listenerObject.addNativeData(SERVICE_REGISTRY_BUILDER, servicesRegistryBuilder);
            return null;
        } catch (ErrorValue ex) {
            return ex;
        } catch (Exception e) {
            LOG.error("Error while initializing service listener.", e);
            return MessageUtils.getConnectorError(e);
        }
    }

}
