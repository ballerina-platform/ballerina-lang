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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.socket.tcp.SelectorManager;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

import static org.ballerinalang.stdlib.socket.SocketConstants.SERVER_SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;

/**
 * Stop server socket listener.
 *
 * @since 0.990.1
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "stop",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener", structPackage = SOCKET_PACKAGE),
        isPublic = true
)
public class Stop {
    private static final Logger log = LoggerFactory.getLogger(Stop.class);

    public static Object stop(Strand strand, ObjectValue listener, boolean graceful) {
        try {
            ServerSocketChannel channel = (ServerSocketChannel) listener.getNativeData(SERVER_SOCKET_KEY);
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
