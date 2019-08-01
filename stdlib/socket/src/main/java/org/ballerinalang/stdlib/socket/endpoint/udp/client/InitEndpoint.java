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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.socket.endpoint.udp.client;

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
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

import static java.nio.channels.SelectionKey.OP_READ;
import static org.ballerinalang.stdlib.socket.SocketConstants.IS_CLIENT;
import static org.ballerinalang.stdlib.socket.SocketConstants.READ_TIMEOUT;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_SERVICE;
import static org.ballerinalang.stdlib.socket.SocketConstants.UDP_CLIENT;

/**
 * Initialize the client socket endpoint.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "initEndpoint",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = UDP_CLIENT, structPackage = SOCKET_PACKAGE),
        isPublic = true
)
public class InitEndpoint {
    private static final Logger log = LoggerFactory.getLogger(InitEndpoint.class);

    public static Object initEndpoint(Strand strand, ObjectValue client, Object address,
            MapValue<String, Object> config) {
        final NonBlockingCallback callback = new NonBlockingCallback(strand);
        SelectorManager selectorManager;
        SocketService socketService;
        try {
            DatagramChannel socketChannel = DatagramChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.socket().setReuseAddress(true);
            client.addNativeData(SOCKET_KEY, socketChannel);
            client.addNativeData(IS_CLIENT, true);
            if (address != null) {
                MapValue<String, Object> addressRecord = (MapValue<String, Object>) address;
                String host = addressRecord.getStringValue(SocketConstants.CONFIG_FIELD_HOST);
                int port = addressRecord.getIntValue(SocketConstants.CONFIG_FIELD_PORT).intValue();
                if (host == null) {
                    socketChannel.bind(new InetSocketAddress(port));
                } else {
                    socketChannel.bind(new InetSocketAddress(host, port));
                }
            }
            long timeout = config.getIntValue(READ_TIMEOUT);
            socketService = new SocketService(socketChannel, strand.scheduler, null, timeout);
            client.addNativeData(SOCKET_SERVICE, socketService);
            selectorManager = SelectorManager.getInstance();
            selectorManager.start();
        } catch (SelectorInitializeException e) {
            log.error(e.getMessage(), e);
            callback.notifyFailure(SocketUtils.createSocketError("Unable to initialize the selector"));
            return null;
        } catch (SocketException e) {
            callback.notifyFailure(SocketUtils.createSocketError("Unable to bind the local socket port"));
            return null;
        } catch (IOException e) {
            log.error("Unable to initiate the client socket", e);
            callback.notifyFailure(SocketUtils.createSocketError("Unable to initiate the socket"));
            return null;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            callback.notifyFailure(SocketUtils.createSocketError("Unable to start the socket client."));
            return null;
        }
        selectorManager.registerChannel(new ChannelRegisterCallback(socketService, callback, OP_READ));
        return null;
    }
}
