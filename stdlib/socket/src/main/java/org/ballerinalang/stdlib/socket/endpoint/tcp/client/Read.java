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

package org.ballerinalang.stdlib.socket.endpoint.tcp.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.socket.SocketConstants;
import org.ballerinalang.stdlib.socket.tcp.ReadReadyQueue;
import org.ballerinalang.stdlib.socket.tcp.SelectorManager;
import org.ballerinalang.stdlib.socket.tcp.SocketReader;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import static org.ballerinalang.stdlib.socket.SocketConstants.CLIENT;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;

/**
 * 'Read' method implementation of the socket caller action.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "read",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CLIENT, structPackage = SOCKET_PACKAGE),
        isPublic = true
)
public class Read implements NativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(Read.class);
    private static final BTupleType readTupleType = new BTupleType(
            Arrays.asList(new BArrayType(BTypes.typeByte), BTypes.typeInt));

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> clientEndpoint = (BMap<String, BValue>) context.getRefArgument(0);
        SocketChannel socketChannel = (SocketChannel) clientEndpoint.getNativeData(SocketConstants.SOCKET_KEY);
        SocketReader socketReader = ReadReadyQueue.getInstance().get(socketChannel.hashCode());
        try {
            if (socketReader == null) {

            } else {
                BValueArray contentTuple = new BValueArray(readTupleType);
                ByteBuffer buffer = ByteBuffer.allocate(socketChannel.socket().getReceiveBufferSize());
                int read = socketChannel.read(buffer);
                if (read < 0) {
                    SelectorManager.getInstance().unRegisterChannel(socketChannel);
                } else {
                    // Re-register for read ready events.
                    socketReader.getSelectionKey().interestOps(SelectionKey.OP_READ);
                }
                contentTuple.add(0, new BValueArray(SocketUtils.getByteArrayFromByteBuffer(buffer)));
                contentTuple.add(1, new BInteger(read));
                context.setReturnValues(contentTuple);
            }
        } catch (IOException e) {
            context.setReturnValues(SocketUtils.createSocketError(context, "Read failed."));
            log.error("Unable to perform read[" + socketChannel.hashCode() + "]", e);
        }
        callback.notifySuccess();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
