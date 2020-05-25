/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.grpc.nativeimpl.serviceendpoint;

import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.ServerConnectorListener;
import org.ballerinalang.net.grpc.ServerConnectorPortBindingListener;
import org.ballerinalang.net.grpc.ServicesBuilderUtils;
import org.ballerinalang.net.grpc.ServicesRegistry;
import org.ballerinalang.net.grpc.Status;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.ballerinalang.net.grpc.nativeimpl.AbstractGrpcNativeFunction;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;

import static org.ballerinalang.net.grpc.GrpcConstants.ANN_SERVICE_DESCRIPTOR_FQN;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVER_CONNECTOR;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_REGISTRY_BUILDER;
import static org.ballerinalang.net.grpc.GrpcUtil.getListenerConfig;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_PORT;

/**
 * Utility methods represents lifecycle functions of the service listener.
 *
 * @since 1.0.0
 */

public class FunctionUtils  extends AbstractGrpcNativeFunction  {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionUtils.class);

    /**
     * Extern function to initialize gRPC service listener.
     *
     * @param listenerObject service listener instance.
     * @return Error if there is an error while initializing the service listener, else returns nil.
     */
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

    /**
     * Extern function to register service to service listener.
     *
     * @param listenerObject service listener instance.
     * @param service service instance.
     * @param annotationData service annotation data.
     * @return Error if there is an error while registering the service, else returns nil.
     */
    public static Object externRegister(ObjectValue listenerObject, ObjectValue service,
                                        Object annotationData) {
        ServicesRegistry.Builder servicesRegistryBuilder = getServiceRegistryBuilder(listenerObject);
        try {
            if (servicesRegistryBuilder == null) {
                return MessageUtils.getConnectorError(new StatusRuntimeException(Status
                        .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error when " +
                                "initializing service register builder.")));
            } else {
                servicesRegistryBuilder.addService(ServicesBuilderUtils.getServiceDefinition(
                        BRuntime.getCurrentRuntime(), service,
                        service.getType().getAnnotation(StringUtils.fromString(ANN_SERVICE_DESCRIPTOR_FQN))));
                return null;
            }
        } catch (GrpcServerException e) {
            return MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Error when " +
                            "initializing service register builder. " + e.getLocalizedMessage())));
        }
    }

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

    /**
     * Extern function to start gRPC server instance.
     *
     * @param listener service listener instance.
     * @return Error if there is an error while starting the server, else returns nil.
     */
    public static Object externStart(ObjectValue listener) {
        ServicesRegistry.Builder servicesRegistryBuilder = getServiceRegistryBuilder(listener);

        if (!isConnectorStarted(listener)) {
            return startServerConnector(listener, servicesRegistryBuilder.build());
        }
        return null;
    }

    /**
     * Extern function to stop gRPC server instance.
     *
     * @param serverEndpoint service listener instance.
     * @return Error if there is an error while starting the server, else returns nil.
     */
    public static Object externStop(ObjectValue serverEndpoint) {
        getServerConnector(serverEndpoint).stop();
        serverEndpoint.addNativeData(HttpConstants.CONNECTOR_STARTED, false);
        return null;
    }
}
