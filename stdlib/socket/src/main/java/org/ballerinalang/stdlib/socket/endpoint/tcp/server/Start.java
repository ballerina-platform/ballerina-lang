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

package org.ballerinalang.stdlib.socket.endpoint.tcp.server;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.socket.SocketConstants;
import org.ballerinalang.stdlib.socket.exceptions.SelectorInitializeException;
import org.ballerinalang.stdlib.socket.tcp.ChannelRegisterCallback;
import org.ballerinalang.stdlib.socket.tcp.SelectorManager;
import org.ballerinalang.stdlib.socket.tcp.SocketService;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.concurrent.RejectedExecutionException;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static org.ballerinalang.stdlib.socket.SocketConstants.CONFIG_FIELD_PORT;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_CONFIG;
import static org.ballerinalang.stdlib.socket.SocketConstants.SERVER_SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_SERVICE;

/**
 * Start server socket listener.
 *
 * @since 0.985.0
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "start",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener", structPackage = SOCKET_PACKAGE),
        isPublic = true
)
public class Start {
    private static final Logger log = LoggerFactory.getLogger(Start.class);

    public static Object start(Strand strand, ObjectValue listener) {
        final NonBlockingCallback callback = new NonBlockingCallback(strand);
        try {
            ServerSocketChannel channel = (ServerSocketChannel) listener.getNativeData(SERVER_SOCKET_KEY);
            int port = (int) listener.getNativeData(CONFIG_FIELD_PORT);
            MapValue<String, Object> config = (MapValue<String, Object>) listener.getNativeData(LISTENER_CONFIG);
            String networkInterface = (String) config.getNativeData(SocketConstants.CONFIG_FIELD_INTERFACE);
            if (networkInterface == null) {
                channel.bind(new InetSocketAddress(port));
            } else {
                channel.bind(new InetSocketAddress(networkInterface, port));
            }
            // Start selector
            final SelectorManager selectorManager = SelectorManager.getInstance();
            selectorManager.start();
            SocketService socketService = (SocketService) listener.getNativeData(SOCKET_SERVICE);
            ChannelRegisterCallback registerCallback = new ChannelRegisterCallback(socketService, callback, OP_ACCEPT);
            selectorManager.registerChannel(registerCallback);
            String socketListenerStarted = "[ballerina/socket] started socket listener ";
            PrintStream console = System.out;
            console.println(socketListenerStarted + channel.socket().getLocalPort());
        } catch (SelectorInitializeException e) {
            log.error(e.getMessage(), e);
            callback.notifyFailure(SocketUtils.createSocketError("Unable to initialize the selector"));
        } catch (CancelledKeyException e) {
            callback.notifyFailure(SocketUtils.createSocketError("Server socket registration is failed"));
        } catch (AlreadyBoundException e) {
            callback.notifyFailure(SocketUtils.createSocketError("Server socket service is already bound to a port"));
        } catch (UnsupportedAddressTypeException e) {
            log.error("Address not supported", e);
            callback.notifyFailure(SocketUtils.createSocketError("Provided address is not supported"));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            callback.notifyFailure(
                    SocketUtils.createSocketError("Unable to start the socket service: " + e.getMessage()));
        } catch (RejectedExecutionException e) {
            log.error(e.getMessage(), e);
            callback.notifyFailure(SocketUtils.createSocketError("Unable to start the socket listener."));
        }
        return null;
    }
}
