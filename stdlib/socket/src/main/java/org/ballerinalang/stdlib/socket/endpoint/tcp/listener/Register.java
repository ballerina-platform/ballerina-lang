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

package org.ballerinalang.stdlib.socket.endpoint.tcp.listener;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.socket.exceptions.SelectorInitializeException;
import org.ballerinalang.stdlib.socket.tcp.SelectorManager;
import org.ballerinalang.stdlib.socket.tcp.SocketService;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_ACCEPT;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_CLOSE;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_ERROR;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_READ_READY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SERVER_SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;
import static java.nio.channels.SelectionKey.OP_ACCEPT;

/**
 * Register socket listener service.
 *
 * @since 0.983.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "register",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener", structPackage = SOCKET_PACKAGE),
        args = {@Argument(name = "serviceType", type = TypeKind.TYPEDESC)},
        isPublic = true
)
public class Register extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(Register.class);

    @Override
    public void execute(Context context) {
        try {
            final SocketService socketService = getSocketService(context);
            final SelectorManager selectorManager = SelectorManager.getInstance();
            selectorManager.registerChannel(socketService, OP_ACCEPT);
            context.setReturnValues();
        } catch (SelectorInitializeException e) {
            log.error(e.getMessage(), e);
            throw new BallerinaException("Unable to initialize the selector");
        } catch (ClosedChannelException e) {
            throw new BallerinaException("Server socket not in an open state");
        }  catch (CancelledKeyException e) {
            throw new BallerinaException("Server socket registration is failed");
        }
    }

    private SocketService getSocketService(Context context) {
        Map<String, Resource> resources = getResourceMap(context);
        ServerSocketChannel serverSocket = getServerSocketChannel(context);
        return new SocketService(serverSocket, resources);
    }

    private ServerSocketChannel getServerSocketChannel(Context context) {
        Struct listenerEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        return (ServerSocketChannel) listenerEndpoint.getNativeData(SERVER_SOCKET_KEY);
    }

    private Map<String, Resource> getResourceMap(Context context) {
        Service service = BLangConnectorSPIUtil.getServiceRegistered(context);
        return getResourceRegistry(service);
    }

    private Map<String, Resource> getResourceRegistry(Service service) {
        Map<String, Resource> registry = new HashMap<>(4);
        byte resourceCount = 0;
        for (Resource resource : service.getResources()) {
            switch (resource.getName()) {
                case LISTENER_RESOURCE_ON_ACCEPT:
                    registry.put(LISTENER_RESOURCE_ON_ACCEPT, resource);
                    resourceCount++;
                    break;
                case LISTENER_RESOURCE_ON_READ_READY:
                    registry.put(LISTENER_RESOURCE_ON_READ_READY, resource);
                    resourceCount++;
                    break;
                case LISTENER_RESOURCE_ON_ERROR:
                    registry.put(LISTENER_RESOURCE_ON_ERROR, resource);
                    resourceCount++;
                    break;
                case LISTENER_RESOURCE_ON_CLOSE:
                    registry.put(LISTENER_RESOURCE_ON_CLOSE, resource);
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
