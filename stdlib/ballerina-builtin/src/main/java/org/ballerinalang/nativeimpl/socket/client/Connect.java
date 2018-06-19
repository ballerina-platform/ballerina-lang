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
package org.ballerinalang.nativeimpl.socket.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.SocketIOChannel;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.nativeimpl.socket.SocketConstants;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import static org.ballerinalang.nativeimpl.socket.SocketConstants.LOCAL_PORT_OPTION_FIELD_INDEX;

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
        final BStruct options = (BStruct) context.getRefArgument(0);
        if (log.isDebugEnabled()) {
            log.debug("Remote host: " + host);
            log.debug("Remote port: " + port);
        }
        Socket socket;
        try {
            // Open a client connection
            BStruct socketStruct = (BStruct) context.getRefArgument(0);
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
            Channel ballerinaSocketChannel = new SocketIOChannel(socketChannel, 0);
            BStruct channelStruct = BLangVMStructs.createBStruct(channelStructInfo, ballerinaSocketChannel);
            channelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, ballerinaSocketChannel);

            // Create Socket Struct
            socketStruct.setRefField(0, channelStruct);
            socketStruct.setIntField(0, socket.getPort());
            socketStruct.setIntField(1, socket.getLocalPort());
            socketStruct.setStringField(0, socket.getInetAddress().getHostAddress());
            socketStruct.setStringField(1, socket.getLocalAddress().getHostAddress());
            socketStruct.addNativeData(IOConstants.CLIENT_SOCKET_NAME, socketChannel);
        } catch (Throwable e) {
            String msg = "Failed to open a connection to [" + host + ":" + port + "] : " + e.getMessage();
            log.error(msg, e);
            context.setReturnValues(IOUtils.createError(context, msg));
        }
    }
}
