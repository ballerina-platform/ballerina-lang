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
package org.ballerinalang.stdlib.io.socket.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.io.channels.SocketIOChannel;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.socket.SocketConstants;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * Native function to open a Client socket connection.
 *
 * @since 0.971.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "connect",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Socket",
                             structPackage = SocketConstants.SOCKET_PACKAGE),
        args = {@Argument(name = "host", type = TypeKind.STRING),
                @Argument(name = "port", type = TypeKind.INT)
        },
        isPublic = true
)
public class Connect extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(Connect.class);

    private static final String BYTE_CHANNEL_STRUCT_TYPE = "ByteChannel";

    @Override
    public void execute(Context context) {
        final String host = context.getStringArgument(0);
        final int port = (int) context.getIntArgument(0);
        if (log.isDebugEnabled()) {
            log.debug("Remote host: " + host);
            log.debug("Remote port: " + port);
        }
        Socket socket;
        try {
            // Open a client connection
            BMap<String, BValue> socketStruct = (BMap<String, BValue>) context.getRefArgument(0);
            SocketChannel socketChannel = (SocketChannel) socketStruct.getNativeData(SocketConstants.SOCKET_KEY);
            socketChannel.connect(new InetSocketAddress(host, port));
            log.debug("Successfully connect to remote server.");
            socket = socketChannel.socket();
            if (log.isDebugEnabled()) {
                log.debug("Bound local port: " + socket.getLocalPort());
                log.debug("Timeout on blocking Socket operations: " + socket.getSoTimeout());
                log.debug("ReceiveBufferSize: " + socket.getReceiveBufferSize());
                log.debug("KeepAlive: " + socket.getKeepAlive());
            }
            PackageInfo ioPackageInfo = context.getProgramFile().getPackageInfo(SocketConstants.SOCKET_PACKAGE);
            // Create ByteChannel Struct
            StructureTypeInfo channelStructInfo = ioPackageInfo.getStructInfo(BYTE_CHANNEL_STRUCT_TYPE);
            Channel ballerinaSocketChannel = new SocketIOChannel(socketChannel, false);
            BMap<String, BValue> channelStruct = BLangVMStructs.createBStruct(channelStructInfo);
            channelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, ballerinaSocketChannel);

            // Create Socket Struct
            socketStruct.put(IOConstants.BYTE_CHANNEL_NAME, channelStruct);
            socketStruct.put(SocketConstants.REMOTE_PORT_FIELD, new BInteger(socket.getPort()));
            socketStruct.put(SocketConstants.LOCAL_PORT_OPTION_FIELD, new BInteger(socket.getLocalPort()));
            socketStruct
                    .put(SocketConstants.REMOTE_ADDRESS_FIELD, new BString(socket.getInetAddress().getHostAddress()));
            socketStruct
                    .put(SocketConstants.LOCAL_ADDRESS_FIELD, new BString(socket.getLocalAddress().getHostAddress()));
            socketStruct.addNativeData(IOConstants.CLIENT_SOCKET_NAME, socketChannel);
            context.setReturnValues();
        } catch (Throwable e) {
            String msg = "Failed to open a connection to [" + host + ":" + port + "] : " + e.getMessage();
            log.error(msg, e);
            context.setReturnValues(IOUtils.createError(context, msg));
        }
    }
}
