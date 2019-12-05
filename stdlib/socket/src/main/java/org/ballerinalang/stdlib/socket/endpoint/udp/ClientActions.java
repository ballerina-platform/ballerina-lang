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
package org.ballerinalang.stdlib.socket.endpoint.udp;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.stdlib.socket.SocketConstants;
import org.ballerinalang.stdlib.socket.exceptions.SelectorInitializeException;
import org.ballerinalang.stdlib.socket.tcp.ChannelRegisterCallback;
import org.ballerinalang.stdlib.socket.tcp.ReadPendingCallback;
import org.ballerinalang.stdlib.socket.tcp.ReadPendingSocketMap;
import org.ballerinalang.stdlib.socket.tcp.SelectorManager;
import org.ballerinalang.stdlib.socket.tcp.SocketService;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;

import static java.nio.channels.SelectionKey.OP_READ;
import static org.ballerinalang.stdlib.socket.SocketConstants.DEFAULT_EXPECTED_READ_LENGTH;
import static org.ballerinalang.stdlib.socket.SocketConstants.IS_CLIENT;
import static org.ballerinalang.stdlib.socket.SocketConstants.READ_TIMEOUT;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_SERVICE;

/**
 * Native function implementations of the UDP Client.
 *
 * @since 1.1.0
 */
public class ClientActions {
    private static final Logger log = LoggerFactory.getLogger(ClientActions.class);

    public static Object close(ObjectValue client) {
        final DatagramChannel socketChannel = (DatagramChannel) client.getNativeData(SOCKET_KEY);
        try {
            // SocketChannel can be null if something happen during the onConnect. Hence the null check.
            if (socketChannel != null) {
                socketChannel.close();
                SelectorManager.getInstance().unRegisterChannel(socketChannel);
            }
            // This need to handle to support multiple client close.
            if (Boolean.parseBoolean(client.getNativeData(IS_CLIENT).toString())) {
                SelectorManager.getInstance().stop(true);
            }
        } catch (IOException e) {
            log.error("Unable to close the socket", e);
            return SocketUtils.createSocketError("unable to close the client socket. " + e.getMessage());
        }
        return null;
    }

    public static Object initEndpoint(ObjectValue client, Object address,
                                      MapValue<String, Object> config) {
        final NonBlockingCallback callback = new NonBlockingCallback(Scheduler.getStrand());
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
            socketService = new SocketService(socketChannel, Scheduler.getStrand().scheduler, null, timeout);
            client.addNativeData(SOCKET_SERVICE, socketService);
            selectorManager = SelectorManager.getInstance();
            selectorManager.start();
        } catch (SelectorInitializeException e) {
            log.error(e.getMessage(), e);
            callback.notifyFailure(SocketUtils.createSocketError("unable to initialize the selector"));
            return null;
        } catch (SocketException e) {
            callback.notifyFailure(SocketUtils.createSocketError("unable to bind the local socket port"));
            return null;
        } catch (IOException e) {
            log.error("Unable to initiate the client socket", e);
            callback.notifyFailure(SocketUtils.createSocketError("unable to initiate the socket: " + e.getMessage()));
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            callback.notifyFailure(SocketUtils.createSocketError("unable to start the socket client."));
            return null;
        }
        selectorManager.registerChannel(new ChannelRegisterCallback(socketService, callback, OP_READ));
        return null;
    }

    public static Object receiveFrom(ObjectValue client, long length) {
        final NonBlockingCallback callback = new NonBlockingCallback(Scheduler.getStrand());
        if (length != DEFAULT_EXPECTED_READ_LENGTH && length < 1) {
            String msg = "requested byte length need to be 1 or more";
            callback.notifyFailure(SocketUtils.createSocketError(msg));
            return null;
        }
        DatagramChannel socket = (DatagramChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
        int socketHash = socket.hashCode();
        SocketService socketService = (SocketService) client.getNativeData(SocketConstants.SOCKET_SERVICE);
        ReadPendingCallback readPendingCallback = new ReadPendingCallback(callback, (int) length, socketHash,
                socketService.getReadTimeout());
        ReadPendingSocketMap.getInstance().add(socket.hashCode(), readPendingCallback);
        log.debug("Notify to invokeRead");
        SelectorManager.getInstance().invokeRead(socketHash, false);
        return null;
    }

    public static Object sendTo(ObjectValue client, ArrayValue content, MapValue<String, Object> address) {
        DatagramChannel socket = (DatagramChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
        String host = address.getStringValue(SocketConstants.CONFIG_FIELD_HOST);
        int port = address.getIntValue(SocketConstants.CONFIG_FIELD_PORT).intValue();
        byte[] byteContent = content.getBytes();
        if (log.isDebugEnabled()) {
            log.debug(String.format("No of byte going to write[%d]: %d", socket.hashCode(), byteContent.length));
        }
        try {
            final InetSocketAddress remote = new InetSocketAddress(host, port);
            int write = socket.send(ByteBuffer.wrap(byteContent), remote);
            if (log.isDebugEnabled()) {
                log.debug(String.format("No of byte written for the client[%d]: %d", socket.hashCode(), write));
            }
            return write;
        } catch (ClosedChannelException e) {
            return SocketUtils.createSocketError("client socket close already.");
        } catch (IOException e) {
            log.error("Unable to perform write[" + socket.hashCode() + "]", e);
            return SocketUtils.createSocketError("write failed. " + e.getMessage());
        }
    }
}
