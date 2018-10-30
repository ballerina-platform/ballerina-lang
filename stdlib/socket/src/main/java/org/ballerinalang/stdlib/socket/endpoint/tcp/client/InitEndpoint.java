/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.socket.endpoint.tcp.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.socket.tcp.SocketService;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.stdlib.socket.SocketConstants.CLIENT_CONFIG;
import static org.ballerinalang.stdlib.socket.SocketConstants.CLIENT_SERVICE_CONFIG;
import static org.ballerinalang.stdlib.socket.SocketConstants.IS_CLIENT;
import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_CLOSE;
import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_CONNECT;
import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_ERROR;
import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_READ_READY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_SERVICE;

/**
 * Initialize the client socket endpoint.
 *
 * @since 0.983.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "initEndpoint",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Client", structPackage = SOCKET_PACKAGE),
        args = {@Argument(name = "config", type = TypeKind.RECORD, structType = "ClientEndpointConfiguration",
                          structPackage = SOCKET_PACKAGE)
        },
        isPublic = true
)
public class InitEndpoint extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(InitEndpoint.class);

    @Override
    public void execute(Context context) {
        try {
            Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            Struct clientEndpointConfig = clientEndpoint.getStructField(CLIENT_CONFIG);
            Value clientServiceType = clientEndpointConfig.getTypeField(CLIENT_SERVICE_CONFIG);
            Service service = null;
            if (clientServiceType != null) {
                service = BLangConnectorSPIUtil.getServiceFromType(context.getProgramFile(), clientServiceType);
                if (!"ballerina/socket:Client".equals(service.getEndpointName())) {
                    throw new BallerinaConnectorException("The callback service should be of type ");
                }
            }
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.socket().setReuseAddress(true);
            clientEndpoint.addNativeData(SOCKET_KEY, socketChannel);
            clientEndpoint.addNativeData(IS_CLIENT, true);
            BMap<String, BValue> endpointConfig = (BMap<String, BValue>) context.getRefArgument(1);
            Map<String, Resource> resourceMap = null;
            if (service != null) {
                resourceMap = getResourceMap(service);
            }
            clientEndpoint.addNativeData(SOCKET_SERVICE, new SocketService(socketChannel, resourceMap));
            clientEndpoint.addNativeData(CLIENT_CONFIG, endpointConfig);
        } catch (SocketException e) {
            throw new BallerinaException("Unable to bind the local socket port");
        } catch (IOException e) {
            log.error("Unable to initiate the client socket", e);
            throw new BallerinaException("Unable to initiate the socket endpoint");
        }
        context.setReturnValues();
    }

    private Map<String, Resource> getResourceMap(Service service) {
        return getResourceRegistry(service);
    }

    private Map<String, Resource> getResourceRegistry(Service service) {
        Map<String, Resource> registry = new HashMap<>(4);
        byte resourceCount = 0;
        for (Resource resource : service.getResources()) {
            switch (resource.getName()) {
                case RESOURCE_ON_CONNECT:
                    registry.put(RESOURCE_ON_CONNECT, resource);
                    resourceCount++;
                    break;
                case RESOURCE_ON_READ_READY:
                    registry.put(RESOURCE_ON_READ_READY, resource);
                    resourceCount++;
                    break;
                case RESOURCE_ON_ERROR:
                    registry.put(RESOURCE_ON_ERROR, resource);
                    resourceCount++;
                    break;
                case RESOURCE_ON_CLOSE:
                    registry.put(RESOURCE_ON_CLOSE, resource);
                    resourceCount++;
                    break;
                default:
                    // Do nothing.
            }
            if (resourceCount == 4) {
                break;
            }
        }
        return registry;
    }
}
