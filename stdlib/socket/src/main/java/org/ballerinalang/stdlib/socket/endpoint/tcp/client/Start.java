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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ErrorValue;
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
import java.net.InetSocketAddress;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnsupportedAddressTypeException;

import static java.nio.channels.SelectionKey.OP_READ;
import static org.ballerinalang.stdlib.socket.SocketConstants.CLIENT;
import static org.ballerinalang.stdlib.socket.SocketConstants.CLIENT_CONFIG;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_SERVICE;

/**
 * Connect to the remote server.
 *
 * @since 0.985.0
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "start",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CLIENT, structPackage = SOCKET_PACKAGE),
        isPublic = true
)
public class Start {
    private static final Logger log = LoggerFactory.getLogger(Start.class);

    public static Object start(Strand strand, ObjectValue client) {
        final NonBlockingCallback callback = new NonBlockingCallback(strand);
        SelectorManager selectorManager = null;
        ErrorValue error = null;
        SocketChannel channel = null;
        try {
            channel = (SocketChannel) client.getNativeData(SOCKET_KEY);
            MapValue<String, Object> config = (MapValue<String, Object>) client.getNativeData(CLIENT_CONFIG);
            int port = Math.toIntExact(config.getIntValue(SocketConstants.CONFIG_FIELD_PORT));
            String host = config.getStringValue(SocketConstants.CONFIG_FIELD_HOST);
            channel.connect(new InetSocketAddress(host, port));
            channel.finishConnect();
            channel.configureBlocking(false);
            selectorManager = SelectorManager.getInstance();
            selectorManager.start();
        } catch (SelectorInitializeException e) {
            log.error(e.getMessage(), e);
            error = SocketUtils.createSocketError("Unable to initialize the selector");
        } catch (CancelledKeyException e) {
            error = SocketUtils.createSocketError("Unable to start the client socket");
        } catch (AlreadyBoundException e) {
            error = SocketUtils.createSocketError("Client socket is already bound to a port");
        } catch (UnsupportedAddressTypeException e) {
            log.error("Address not supported", e);
            error = SocketUtils.createSocketError("Provided address is not supported");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            error = SocketUtils.createSocketError("Unable to start the client socket: " + e.getMessage());
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            error = SocketUtils.createSocketError("Unable to start the socket client.");
        }
        if (error != null) {
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException e) {
                log.error("Unable to close the channel during the error report", e);
            }
            callback.notifyFailure(error);
            return null;
        }
        SocketService socketService = (SocketService) client.getNativeData(SOCKET_SERVICE);
        selectorManager.registerChannel(new ChannelRegisterCallback(socketService, callback, OP_READ));
        return null;
    }
}
