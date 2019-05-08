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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;

import static org.ballerinalang.stdlib.socket.SocketConstants.CONFIG_FIELD_PORT;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_CONFIG;
import static org.ballerinalang.stdlib.socket.SocketConstants.READ_TIMEOUT;
import static org.ballerinalang.stdlib.socket.SocketConstants.SERVER_SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;

/**
 * Initialize the server socket endpoint.
 *
 * @since 0.985.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "initServer",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener", structPackage = SOCKET_PACKAGE),
        isPublic = true
)
public class InitServer extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(InitServer.class);

    @Override
    public void execute(Context context) {
        try {
            Struct serviceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.socket().setReuseAddress(true);
            serviceEndpoint.addNativeData(SERVER_SOCKET_KEY, serverSocket);
            BMap<String, BValue> endpointConfig = (BMap<String, BValue>) context.getRefArgument(1);
            serviceEndpoint.addNativeData(LISTENER_CONFIG, endpointConfig);
            int port = (int) context.getIntArgument(0);
            serviceEndpoint.addNativeData(CONFIG_FIELD_PORT, port);
            final BValue readTimeoutBValue = endpointConfig.get(READ_TIMEOUT);
            long timeout;
            if (readTimeoutBValue != null) {
                timeout = ((BInteger) readTimeoutBValue).intValue();
            } else {
                timeout = SocketUtils.getReadTimeout();
            }
            serviceEndpoint.addNativeData(READ_TIMEOUT, timeout);
        } catch (SocketException e) {
            context.setReturnValues(SocketUtils.createSocketError(context, "Unable to bind the socket port"));
            return;
        } catch (IOException e) {
            log.error("Unable to initiate the server socket", e);
            context.setReturnValues(
                    SocketUtils.createSocketError(context, "Unable to initiate the socket service"));
            return;
        }
        context.setReturnValues();
    }
}
