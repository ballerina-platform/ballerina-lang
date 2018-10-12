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

package org.ballerinalang.stdlib.socket.endpoint.tcp.calleraction;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
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

import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;

/**
 * 'Write' method implementation of the socket server listener's caller action.
 *
 * @since 0.983.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "write",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "CallerAction", structPackage = SOCKET_PACKAGE),
        args = {@Argument(name = "content", type = TypeKind.ARRAY, elementType = TypeKind.BYTE)},
        isPublic = true
)
public class Write extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(Write.class);

    @Override
    public void execute(Context context) {
        BMap<String, BValue> clientEndpoint = (BMap<String, BValue>) context.getRefArgument(0);
        final SocketChannel socketChannel = (SocketChannel) clientEndpoint.getNativeData(SocketConstants.SOCKET_KEY);
        BByteArray content = (BByteArray) context.getRefArgument(1);
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
            context.setReturnValues(new BInteger(write));
        } catch (ClosedChannelException e) {
            context.setReturnValues(SocketUtils.createError(context, "Client socket close already."));
        } catch (IOException e) {
            context.setReturnValues(SocketUtils.createError(context, "Write failed."));
            log.error("Unable to perform write[" + socketChannel.hashCode() + "]", e);
        } catch (NotYetConnectedException e) {
            context.setReturnValues(SocketUtils.createError(context, "Client socket not connected yet."));
        }
    }
}
