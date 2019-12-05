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
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
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
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnsupportedAddressTypeException;

import static java.nio.channels.SelectionKey.OP_READ;

/**
 * Native function implementations of the TCP Client.
 *
 * @since 1.1.0
 */
public class ClientActions {
    private static final Logger log = LoggerFactory.getLogger(ClientActions.class);

    public static Object initEndpoint(ObjectValue client, MapValue<String, Object> config) {
        Object returnValue = null;
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.socket().setReuseAddress(true);
            client.addNativeData(SocketConstants.SOCKET_KEY, socketChannel);
            client.addNativeData(SocketConstants.IS_CLIENT, true);
            ObjectValue callbackService = (ObjectValue) config.get(SocketConstants.CLIENT_SERVICE_CONFIG);
            client.addNativeData(SocketConstants.CLIENT_CONFIG, config);
            final long readTimeout = config.getIntValue(SocketConstants.READ_TIMEOUT);
            client.addNativeData(SocketConstants.SOCKET_SERVICE,
                    new SocketService(socketChannel, Scheduler.getStrand().scheduler, callbackService, readTimeout));
        } catch (SocketException e) {
            returnValue = SocketUtils.createSocketError("unable to bind the local socket port");
        } catch (IOException e) {
            log.error("Unable to initiate the client socket", e);
            returnValue = SocketUtils.createSocketError("unable to initiate the socket");
        }
        return returnValue;
    }

    public static Object close(ObjectValue client) {
        final SocketChannel socketChannel = (SocketChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
        try {
            // SocketChannel can be null if something happen during the onConnect. Hence the null check.
            if (socketChannel != null) {
                socketChannel.close();
                SelectorManager.getInstance().unRegisterChannel(socketChannel);
            }
            // This need to handle to support multiple client close.
            Object isClient = client.getNativeData(SocketConstants.IS_CLIENT);
            if (isClient != null && Boolean.parseBoolean(isClient.toString())) {
                SelectorManager.getInstance().stop(true);
            }
        } catch (IOException e) {
            log.error("Unable to close the connection", e);
            return SocketUtils.createSocketError("unable to close the client socket connection");
        }
        return null;
    }

    public static Object read(ObjectValue client, long length) {
        final NonBlockingCallback callback = new NonBlockingCallback(Scheduler.getStrand());
        if (length != SocketConstants.DEFAULT_EXPECTED_READ_LENGTH && length < 1) {
            String msg = "requested byte length need to be 1 or more";
            callback.setReturnValues(SocketUtils.createSocketError(SocketConstants.ErrorCode.ReadTimedOutError, msg));
            callback.notifySuccess();
            return null;
        }
        SocketService socketService = (SocketService) client.getNativeData(SocketConstants.SOCKET_SERVICE);
        SocketChannel socketChannel = (SocketChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
        int socketHash = socketChannel.hashCode();
        ReadPendingCallback readPendingCallback = new ReadPendingCallback(callback, (int) length, socketHash,
                socketService.getReadTimeout());
        ReadPendingSocketMap.getInstance().add(socketChannel.hashCode(), readPendingCallback);
        log.debug("Notify to invokeRead");
        SelectorManager.getInstance().invokeRead(socketHash, socketService.getService() != null);
        return null;
    }

    public static Object shutdownRead(ObjectValue client) {
        final SocketChannel socketChannel = (SocketChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
        try {
            // SocketChannel can be null if something happen during the onAccept. Hence the null check.
            if (socketChannel != null) {
                socketChannel.shutdownInput();
            }
        } catch (ClosedChannelException e) {
            return SocketUtils.createSocketError("socket is already closed");
        } catch (IOException e) {
            log.error("Unable to shutdown the read", e);
            return SocketUtils.createSocketError("unable to shutdown the write");
        } catch (NotYetConnectedException e) {
            return SocketUtils.createSocketError("socket is not yet connected");
        }
        return null;
    }

    public static Object shutdownWrite(ObjectValue client) {
        final SocketChannel socketChannel = (SocketChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
        try {
            // SocketChannel can be null if something happen during the onAccept. Hence the null check.
            if (socketChannel != null) {
                socketChannel.shutdownOutput();
            }
        } catch (ClosedChannelException e) {
            return SocketUtils.createSocketError("socket is already closed");
        } catch (IOException e) {
            log.error("Unable to shutdown the write", e);
            return SocketUtils.createSocketError("unable to shutdown the write");
        } catch (NotYetConnectedException e) {
            return SocketUtils.createSocketError("socket is not yet connected");
        }
        return null;
    }

    public static Object start(ObjectValue client) {
        final NonBlockingCallback callback = new NonBlockingCallback(Scheduler.getStrand());
        SelectorManager selectorManager = null;
        ErrorValue error = null;
        SocketChannel channel = null;
        try {
            channel = (SocketChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
            MapValue<String, Object> config =
                    (MapValue<String, Object>) client.getNativeData(SocketConstants.CLIENT_CONFIG);
            int port = Math.toIntExact(config.getIntValue(SocketConstants.CONFIG_FIELD_PORT));
            String host = config.getStringValue(SocketConstants.CONFIG_FIELD_HOST);
            channel.connect(new InetSocketAddress(host, port));
            channel.finishConnect();
            channel.configureBlocking(false);
            selectorManager = SelectorManager.getInstance();
            selectorManager.start();
        } catch (SelectorInitializeException e) {
            log.error(e.getMessage(), e);
            error = SocketUtils.createSocketError("unable to initialize the selector");
        } catch (CancelledKeyException e) {
            error = SocketUtils.createSocketError("unable to start the client socket");
        } catch (AlreadyBoundException e) {
            error = SocketUtils.createSocketError("client socket is already bound to a port");
        } catch (UnsupportedAddressTypeException e) {
            log.error("Address not supported", e);
            error = SocketUtils.createSocketError("provided address is not supported");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            error = SocketUtils.createSocketError("unable to start the client socket: " + e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            error = SocketUtils.createSocketError("unable to start the socket client.");
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
        SocketService socketService = (SocketService) client.getNativeData(SocketConstants.SOCKET_SERVICE);
        selectorManager.registerChannel(new ChannelRegisterCallback(socketService, callback, OP_READ));
        return null;
    }

    public static Object write(ObjectValue client, ArrayValue content) {
        final SocketChannel socketChannel = (SocketChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
        byte[] byteContent = content.getBytes();
        if (log.isDebugEnabled()) {
            log.debug(String.format("No of byte going to write[%d]: %d", socketChannel.hashCode(), byteContent.length));
        }
        ByteBuffer buffer = ByteBuffer.wrap(byteContent);
        int write;
        try {
            write = socketChannel.write(buffer);
            if (log.isDebugEnabled()) {
                log.debug(String.format("No of byte written for the client[%d]: %d", socketChannel.hashCode(), write));
            }
            return (long) write;
        } catch (ClosedChannelException e) {
            return SocketUtils.createSocketError("client socket close already.");
        } catch (IOException e) {
            log.error("Unable to perform write[" + socketChannel.hashCode() + "]", e);
            return SocketUtils.createSocketError("write failed. " + e.getMessage());
        } catch (NotYetConnectedException e) {
            return SocketUtils.createSocketError("client socket not connected yet.");
        }
    }
}
