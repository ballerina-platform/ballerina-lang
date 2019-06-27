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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.socket.SocketConstants;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;

/**
 * 'shutdownRead' method implementation of the socket caller action.
 *
 * @since 0.985.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "shutdownRead",
        isPublic = true
)
public class ShutdownRead extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(ShutdownRead.class);

    @Override
    public void execute(Context context) {
    }

    public static Object shutdownRead(Strand strand, ObjectValue client) {
        final SocketChannel socketChannel = (SocketChannel) client.getNativeData(SocketConstants.SOCKET_KEY);
        try {
            // SocketChannel can be null if something happen during the onAccept. Hence the null check.
            if (socketChannel != null) {
                socketChannel.shutdownInput();
            }
        } catch (ClosedChannelException e) {
            return SocketUtils.createSocketError("Socket already closed");
        } catch (IOException e) {
            log.error("Unable to shutdown the read", e);
            return SocketUtils.createSocketError("Unable to shutdown the write");
        } catch (NotYetConnectedException e) {
            return SocketUtils.createSocketError("Socket not yet connected");
        }
        return null;
    }
}
