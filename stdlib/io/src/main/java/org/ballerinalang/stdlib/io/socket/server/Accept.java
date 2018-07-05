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

package org.ballerinalang.stdlib.io.socket.server;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.socket.SocketConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Native function to accept new Client socket.
 *
 * @since 0.971.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io", functionName = "accept",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "ServerSocket",
                             structPackage = SocketConstants.SOCKET_PACKAGE),
        returnType = {
                @ReturnType(type = TypeKind.OBJECT, structType = "Socket",
                            structPackage = SocketConstants.SOCKET_PACKAGE)
        },
        isPublic = true
)
public class Accept implements NativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(Accept.class);

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> serverSocketStruct = (BMap<String, BValue>) context.getRefArgument(0);
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) serverSocketStruct
                .getNativeData(SocketConstants.SERVER_SOCKET_KEY);
        int serverSocketHash = serverSocketChannel.hashCode();
        SocketChannel socketChannel = SocketQueue.getInstance().getSocket(serverSocketHash);
        if (socketChannel != null) {
            ServerSocketUtils.createSocket(context, callback, socketChannel);
        } else {
            SocketAcceptCallbackQueue queue = SocketAcceptCallbackQueue.getInstance();
            queue.registerSocketAcceptCallback(serverSocketHash, new SocketAcceptCallback(context, callback));
        }
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
