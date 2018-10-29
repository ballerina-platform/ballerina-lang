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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.socket.SelectorManager;
import org.ballerinalang.stdlib.io.socket.SocketConstants;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

/**
 * Extern function to close a server socket.
 *
 * @since 0.971.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io", functionName = "close",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "ServerSocket",
                structPackage = SocketConstants.SOCKET_PACKAGE),
        returnType = {@ReturnType(type = TypeKind.OBJECT, structType = "error")},
        isPublic = true
)
public class Close extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(Close.class);

    @Override
    public void execute(Context context) {
        BMap<String, BValue> serverSocketStruct;
        try {
            serverSocketStruct = (BMap<String, BValue>) context.getRefArgument(0);
            ServerSocketChannel serverSocket = (ServerSocketChannel) serverSocketStruct
                    .getNativeData(SocketConstants.SERVER_SOCKET_KEY);
            final SelectionKey selectionKey = serverSocket.keyFor(SelectorManager.getInstance());
            if (selectionKey != null) {
                selectionKey.cancel();
            }
            serverSocket.close();
        } catch (Throwable e) {
            String message = "Failed to close the ServerSocket.";
            BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, message);
            context.setReturnValues(errorStruct);
        }
        context.setReturnValues();
    }
}
