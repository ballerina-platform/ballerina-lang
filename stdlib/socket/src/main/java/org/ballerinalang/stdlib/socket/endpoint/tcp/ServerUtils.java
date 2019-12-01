/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.socket.endpoint.tcp;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
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
import java.net.SocketException;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.concurrent.RejectedExecutionException;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static org.ballerinalang.stdlib.socket.SocketConstants.READ_TIMEOUT;

/**
 * Native function implementations of the TCP Listener.
 *
 * @since 1.1.0
 */
public class ServerUtils {
    private static final Logger log = LoggerFactory.getLogger(ServerUtils.class);

    public static Object initServer(ObjectValue listener, long port, MapValue<String, Object> config) {
        try {
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.socket().setReuseAddress(true);
            listener.addNativeData(SocketConstants.SERVER_SOCKET_KEY, serverSocket);
            listener.addNativeData(SocketConstants.LISTENER_CONFIG, config);
            listener.addNativeData(SocketConstants.CONFIG_FIELD_PORT, (int) port);
            final long timeout = config.getIntValue(READ_TIMEOUT);
            listener.addNativeData(READ_TIMEOUT, timeout);
        } catch (SocketException e) {
            return SocketUtils.createSocketError("unable to bind the socket port");
        } catch (IOException e) {
            log.error("Unable to initiate the socket listener", e);
            return SocketUtils.createSocketError("unable to initiate the socket listener");
        }
        return null;
    }

    public static Object register(ObjectValue listener, ObjectValue service) {
        final SocketService socketService =
                getSocketService(listener, Scheduler.getStrand().scheduler, service);
        listener.addNativeData(SocketConstants.SOCKET_SERVICE, socketService);
        return null;
    }

    private static SocketService getSocketService(ObjectValue listener, Scheduler scheduler, ObjectValue service) {
        ServerSocketChannel serverSocket =
                (ServerSocketChannel) listener.getNativeData(SocketConstants.SERVER_SOCKET_KEY);
        long timeout = (long) listener.getNativeData(READ_TIMEOUT);
        return new SocketService(serverSocket, scheduler, service, timeout);
    }

    public static Object start(ObjectValue listener) {
        final NonBlockingCallback callback = new NonBlockingCallback(Scheduler.getStrand());
        try {
            ServerSocketChannel channel =
                    (ServerSocketChannel) listener.getNativeData(SocketConstants.SERVER_SOCKET_KEY);
            int port = (int) listener.getNativeData(SocketConstants.CONFIG_FIELD_PORT);
            MapValue<String, Object> config =
                    (MapValue<String, Object>) listener.getNativeData(SocketConstants.LISTENER_CONFIG);
            String networkInterface = (String) config.getNativeData(SocketConstants.CONFIG_FIELD_INTERFACE);
            if (networkInterface == null) {
                channel.bind(new InetSocketAddress(port));
            } else {
                channel.bind(new InetSocketAddress(networkInterface, port));
            }
            // Start selector
            final SelectorManager selectorManager = SelectorManager.getInstance();
            selectorManager.start();
            SocketService socketService = (SocketService) listener.getNativeData(SocketConstants.SOCKET_SERVICE);
            ChannelRegisterCallback registerCallback = new ChannelRegisterCallback(socketService, callback, OP_ACCEPT);
            selectorManager.registerChannel(registerCallback);
            String socketListenerStarted = "[ballerina/socket] started socket listener ";
            PrintStream console = System.out;
            console.println(socketListenerStarted + channel.socket().getLocalPort());
        } catch (SelectorInitializeException e) {
            log.error(e.getMessage(), e);
            callback.notifyFailure(SocketUtils.createSocketError("unable to initialize the selector"));
        } catch (CancelledKeyException e) {
            callback.notifyFailure(SocketUtils.createSocketError("server socket registration is failed"));
        } catch (AlreadyBoundException e) {
            callback.notifyFailure(SocketUtils.createSocketError("server socket service is already bound to a port"));
        } catch (UnsupportedAddressTypeException e) {
            log.error("Address not supported", e);
            callback.notifyFailure(SocketUtils.createSocketError("provided address is not supported"));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            callback.notifyFailure(
                    SocketUtils.createSocketError("unable to start the socket service: " + e.getMessage()));
        } catch (RejectedExecutionException e) {
            log.error(e.getMessage(), e);
            callback.notifyFailure(SocketUtils.createSocketError("unable to start the socket listener."));
        }
        return null;
    }

    public static Object stop(ObjectValue listener, boolean graceful) {
        try {
            ServerSocketChannel channel =
                    (ServerSocketChannel) listener.getNativeData(SocketConstants.SERVER_SOCKET_KEY);
            final SelectorManager selectorManager = SelectorManager.getInstance();
            selectorManager.unRegisterChannel(channel);
            channel.close();
            selectorManager.stop(graceful);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return SocketUtils.createSocketError("unable to stop the socket listener: " + e.getMessage());
        }
        return null;
    }
}
