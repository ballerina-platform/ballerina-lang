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
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

import static org.ballerinalang.stdlib.socket.SocketConstants.IS_CLIENT;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_SERVICE;
import static org.ballerinalang.stdlib.socket.SocketConstants.UDP_CLIENT;
import static java.nio.channels.SelectionKey.OP_READ;

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
public class InitEndpoint implements NativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(InitEndpoint.class);

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        SelectorManager selectorManager;
        SocketService socketService;
        try {
            Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            DatagramChannel socketChannel = DatagramChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.socket().setReuseAddress(true);
            clientEndpoint.addNativeData(SOCKET_KEY, socketChannel);
            clientEndpoint.addNativeData(IS_CLIENT, true);
            BMap<String, BValue> localAddress = (BMap<String, BValue>) context.getNullableRefArgument(1);
            if (localAddress != null) {
                BString host = (BString) localAddress.get(SocketConstants.CONFIG_FIELD_HOST);
                BInteger port = (BInteger) localAddress.get(SocketConstants.CONFIG_FIELD_PORT);
                if (host == null) {
                    socketChannel.bind(new InetSocketAddress((int) port.intValue()));
                } else {
                    socketChannel.bind(new InetSocketAddress(host.stringValue(), (int) port.intValue()));
                }
            }
            socketService = new SocketService(socketChannel, null);
            clientEndpoint.addNativeData(SOCKET_SERVICE, socketService);
            selectorManager = SelectorManager.getInstance();
            selectorManager.start();
        } catch (SelectorInitializeException e) {
            log.error(e.getMessage(), e);
            context.setReturnValues(SocketUtils.createSocketError(context, "Unable to initialize the selector"));
            callback.notifySuccess();
            return;
        } catch (SocketException e) {
            context.setReturnValues(SocketUtils.createSocketError(context, "Unable to bind the local socket port"));
            callback.notifySuccess();
            return;
        } catch (IOException e) {
            log.error("Unable to initiate the client socket", e);
            context.setReturnValues(SocketUtils.createSocketError(context, "Unable to initiate the socket"));
            callback.notifySuccess();
            return;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            context.setReturnValues(SocketUtils.createSocketError(context, "Unable to start the socket client."));
            callback.notifySuccess();
            return;
        }
        selectorManager.registerChannel(new ChannelRegisterCallback(socketService, callback, context, OP_READ));
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
