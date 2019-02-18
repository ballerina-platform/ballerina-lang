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
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
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

import static org.ballerinalang.stdlib.socket.SocketConstants.CLIENT_CONFIG;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_SERVICE;
import static java.nio.channels.SelectionKey.OP_READ;

/**
 * Connect to the remote server.
 *
 * @since 0.985.0
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "start",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Client", structPackage = SOCKET_PACKAGE),
        isPublic = true
)
public class Start implements NativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(Start.class);

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        try {
            Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            SocketChannel channel = (SocketChannel) clientEndpoint.getNativeData(SOCKET_KEY);
            BMap<String, BValue> config = (BMap<String, BValue>) clientEndpoint.getNativeData(CLIENT_CONFIG);
            BInteger port = (BInteger) config.get(SocketConstants.CONFIG_FIELD_PORT);
            BString host = (BString) config.get(SocketConstants.CONFIG_FIELD_HOST);
            SocketService socketService = (SocketService) clientEndpoint.getNativeData(SOCKET_SERVICE);
            channel.connect(new InetSocketAddress(host.stringValue(), (int) port.intValue()));
            channel.finishConnect();
            channel.configureBlocking(false);
            SelectorManager selectorManager = SelectorManager.getInstance();
            selectorManager.start();
            selectorManager.registerChannel(new ChannelRegisterCallback(socketService, callback, context, OP_READ));
            context.setReturnValues();
            callback.notifySuccess();
        } catch (SelectorInitializeException e) {
            log.error(e.getMessage(), e);
            context.setReturnValues(SocketUtils.createSocketError(context, "Unable to initialize the selector"));
            callback.notifySuccess();
        } catch (CancelledKeyException e) {
            context.setReturnValues(SocketUtils.createSocketError(context, "Unable to start the client socket"));
            callback.notifySuccess();
        } catch (AlreadyBoundException e) {
            context.setReturnValues(SocketUtils.createSocketError(context, "Client socket is already bound to a port"));
            callback.notifySuccess();
        } catch (UnsupportedAddressTypeException e) {
            log.error("Address not supported", e);
            context.setReturnValues(SocketUtils.createSocketError(context, "Provided address not supported"));
            callback.notifySuccess();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            context.setReturnValues(
                    SocketUtils.createSocketError(context, "Unable to start the client socket: " + e.getMessage()));
            callback.notifySuccess();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            context.setReturnValues(SocketUtils.createSocketError(context, "Unable to start the socket client."));
            callback.notifySuccess();
        }
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
