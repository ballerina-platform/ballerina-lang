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
package org.ballerinalang.stdlib.io.socket.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.io.socket.SelectorManager;
import org.ballerinalang.stdlib.io.socket.SocketConstants;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;

/**
 * Extern function to open a Client socket connection.
 *
 * @since 0.971.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "connect",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Socket",
                structPackage = SocketConstants.SOCKET_PACKAGE),
        args = {@Argument(name = "host", type = TypeKind.STRING),
                @Argument(name = "port", type = TypeKind.INT)
        },
        isPublic = true
)
public class Connect implements NativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(Connect.class);

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        final String host = context.getStringArgument(0);
        final int port = (int) context.getIntArgument(0);
        if (log.isDebugEnabled()) {
            log.debug("Remote host: " + host);
            log.debug("Remote port: " + port);
        }
        try {
            // Open a client connection
            BMap<String, BValue> socketStruct = (BMap<String, BValue>) context.getRefArgument(0);
            SocketChannel socketChannel = (SocketChannel) socketStruct.getNativeData(SocketConstants.SOCKET_KEY);
            socketChannel.configureBlocking(false);
            socketChannel.register(SelectorManager.getInstance(), SelectionKey.OP_CONNECT, socketChannel);
            SocketConnectCallbackRegistry callbackQueue = SocketConnectCallbackRegistry.getInstance();
            callbackQueue.registerSocketConnectCallback(socketChannel.hashCode(),
                    new SocketConnectCallback(context, callback));
            SelectorManager.start();
            socketChannel.connect(new InetSocketAddress(host, port));
        } catch (AlreadyConnectedException e) {
            String msg = "Socket is already connected.";
            sendError(msg, context, callback, e, false);
        } catch (ConnectionPendingException e) {
            String msg = "Socket connect attempt already in progress.";
            sendError(msg, context, callback, e, false);
        } catch (ClosedByInterruptException e) {
            String msg = "Socket connect attempt interrupted.";
            sendError(msg, context, callback, e, false);
        } catch (ClosedChannelException e) {
            String msg = "Socket connection already closed.";
            sendError(msg, context, callback, e, false);
        } catch (UnresolvedAddressException e) {
            String msg = "unresolved socket address: " + host;
            sendError(msg, context, callback, e, false);
        } catch (UnsupportedAddressTypeException e) {
            String msg = "Socket address doesn't support for a TCP connection.";
            sendError(msg, context, callback, e, false);
        } catch (SecurityException e) {
            String msg = "Unknown error occurred.";
            sendError(msg, context, callback, e, true);
        } catch (IOException e) {
            String msg = "Failed to open a connection to [" + host + ":" + port + "]";
            sendError(msg, context, callback, e, true);
        } catch (Throwable e) {
            String msg = "An error occurred";
            sendError(msg, context, callback, e, true);
        }
    }

    private void sendError(String msg, Context context, CallableUnitCallback callback, Throwable e, boolean isLogging) {
        if (isLogging) {
            log.error(msg, e);
        }
        BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, e.getMessage());
        context.setReturnValues(errorStruct);
        callback.notifySuccess();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
