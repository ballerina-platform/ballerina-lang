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

package org.ballerinalang.stdlib.socket.tcp.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.socket.SocketConstants;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Extern function for Socket bind address.
 *
 * @since 0.971.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io", functionName = "bindAddress",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Socket",
                             structPackage = SocketConstants.SOCKET_PACKAGE),
        args = {@Argument(name = "port", type = TypeKind.INT),
                @Argument(name = "interface", type = TypeKind.STRING)
        },
        isPublic = true
)
public class BindAddress extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(BindAddress.class);

    @Override
    public void execute(Context context) {
        BMap<String, BValue> socketStruct;
        try {
            socketStruct = (BMap<String, BValue>) context.getRefArgument(0);
            int port = (int) context.getIntArgument(0);
            BValue networkInterface = context.getNullableRefArgument(1);
            SocketChannel socket = (SocketChannel) socketStruct.getNativeData(SocketConstants.SOCKET_KEY);
            if (networkInterface == null) {
                socket.bind(new InetSocketAddress(port));
            } else {
                socket.bind(new InetSocketAddress(networkInterface.stringValue(), port));
            }
        } catch (IOException e) {
            String message = "Error occurred while bind the socket address: " + e.getMessage();
            log.error(message, e);
            throw new BallerinaIOException(message, e);
        }
    }
}
