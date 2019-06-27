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
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
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

/**
 * Initialize the server socket endpoint.
 *
 * @since 0.985.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "initServer",
        isPublic = true
)
public class InitServer extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(InitServer.class);

    @Override
    public void execute(Context context) {
    }

    public static Object initServer(Strand strand, ObjectValue listener, long port, MapValue<String, Object> config) {
        try {
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.socket().setReuseAddress(true);
            listener.addNativeData(SERVER_SOCKET_KEY, serverSocket);
            listener.addNativeData(LISTENER_CONFIG, config);
            listener.addNativeData(CONFIG_FIELD_PORT, port);
            final long timeout = config.getIntValue(READ_TIMEOUT);
            listener.addNativeData(READ_TIMEOUT, timeout);
        } catch (SocketException e) {
            return SocketUtils.createSocketError("Unable to bind the socket port");
        } catch (IOException e) {
            log.error("Unable to initiate the socket listener", e);
            return SocketUtils.createSocketError("Unable to initiate the socket listener");
        }
        return null;
    }
}
