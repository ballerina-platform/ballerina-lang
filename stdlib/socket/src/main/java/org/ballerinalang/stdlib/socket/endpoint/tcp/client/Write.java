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
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.socket.SocketConstants;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;

import static org.ballerinalang.stdlib.socket.SocketConstants.CLIENT;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;

/**
 * 'Write' method implementation of the socket caller action.
 *
 * @since 0.985.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "write",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CLIENT, structPackage = SOCKET_PACKAGE),
        isPublic = true
)
public class Write {
    private static final Logger log = LoggerFactory.getLogger(Write.class);

    public static Object write(Strand strand, ObjectValue client, ArrayValue content) {
        final SocketChannel socketChannel = (SocketChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
        byte[] byteContent = content.getBytes();
        if (log.isDebugEnabled()) {
            log.debug("No of byte going to write[" + socketChannel.hashCode() + "]: " + byteContent.length);
        }
        ByteBuffer buffer = ByteBuffer.wrap(byteContent);
        int write;
        try {
            write = socketChannel.write(buffer);
            if (log.isDebugEnabled()) {
                log.debug("No of byte written for the client[" + socketChannel.hashCode() + "]: " + write);
            }
            return (long) write;
        } catch (ClosedChannelException e) {
            return SocketUtils.createSocketError("Client socket close already.");
        } catch (IOException e) {
            log.error("Unable to perform write[" + socketChannel.hashCode() + "]", e);
            return SocketUtils.createSocketError("Write failed.");
        } catch (NotYetConnectedException e) {
            return SocketUtils.createSocketError("Client socket not connected yet.");
        }
    }
}
