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
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.socket.SocketConstants;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;

import static org.ballerinalang.stdlib.socket.SocketConstants.CLIENT;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;

/**
 * 'shutdownWrite' method implementation of the socket caller action.
 *
 * @since 0.985.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "shutdownWrite",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CLIENT, structPackage = SOCKET_PACKAGE),
        isPublic = true
)
public class ShutdownWrite {
    private static final Logger log = LoggerFactory.getLogger(ShutdownWrite.class);

    public static Object shutdownWrite(Strand strand, ObjectValue client) {
        final SocketChannel socketChannel = (SocketChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
        try {
            // SocketChannel can be null if something happen during the onAccept. Hence the null check.
            if (socketChannel != null) {
                socketChannel.shutdownOutput();
            }
        } catch (ClosedChannelException e) {
            return SocketUtils.createSocketError("Socket is already closed");
        } catch (IOException e) {
            log.error("Unable to shutdown the write", e);
            return SocketUtils.createSocketError("Unable to shutdown the write");
        } catch (NotYetConnectedException e) {
            return SocketUtils.createSocketError("Socket is not yet connected");
        }
        return null;
    }
}
