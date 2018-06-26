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
package org.ballerinalang.stdlib.io.socket;

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
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.SocketIOChannel;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import static org.ballerinalang.stdlib.io.socket.SocketConstants.ADDRESS_FIELD;
import static org.ballerinalang.stdlib.io.socket.SocketConstants.LOCAL_ADDRESS_FIELD;
import static org.ballerinalang.stdlib.io.socket.SocketConstants.LOCAL_PORT_OPTION_FIELD;
import static org.ballerinalang.stdlib.io.socket.SocketConstants.PORT_FIELD;

/**
 * Native function to open a Client socket.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "openSocket",
        args = {@Argument(name = "host", type = TypeKind.STRING),
                @Argument(name = "port", type = TypeKind.INT),
                @Argument(name = "option", type = TypeKind.RECORD, structType = "SocketProperties",
                        structPackage = "ballerina/io")},
        returnType = {
                @ReturnType(type = TypeKind.OBJECT, structType = "Socket", structPackage = "ballerina/io"),
                @ReturnType(type = TypeKind.RECORD, structType = "IOError", structPackage = "ballerina/io")
        },
        isPublic = true
)
public class OpenSocket extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(OpenSocket.class);

    private static final String SOCKET_PACKAGE = "ballerina/io";
    private static final String SOCKET_STRUCT_TYPE = "Socket";
    private static final String BYTE_CHANNEL_STRUCT_TYPE = "ByteChannel";

    @Override
    public void execute(Context context) {
        final String host = context.getStringArgument(0);
        final int port = (int) context.getIntArgument(0);
        final BMap<String, BValue> options = (BMap<String, BValue>) context.getRefArgument(0);
        if (log.isDebugEnabled()) {
            log.debug("Remote host: " + host);
            log.debug("Remote port: " + port);
        }
        Socket socket;
        SocketChannel channel;
        try {
            // Open a client connection
            SocketChannel socketChannel = SocketChannel.open();
            int socketPort = (int) ((BInteger) options.get(LOCAL_PORT_OPTION_FIELD)).intValue();
            if (socketPort > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Bind client socket to local port: " + options.get(LOCAL_PORT_OPTION_FIELD));
                }
                socketChannel.bind(new InetSocketAddress(socketPort));
            }
            socketChannel.connect(new InetSocketAddress(host, port));
            log.debug("Successfully connect to remote server.");
            socket = socketChannel.socket();
            if (log.isDebugEnabled()) {
                log.debug("Bound local port: " + socket.getLocalPort());
                log.debug("Timeout on blocking Socket operations: " + socket.getSoTimeout());
                log.debug("ReceiveBufferSize: " + socket.getReceiveBufferSize());
                log.debug("KeepAlive: " + socket.getKeepAlive());
            }
            channel = socket.getChannel();
            PackageInfo ioPackageInfo = context.getProgramFile().getPackageInfo(SOCKET_PACKAGE);
            // Create ByteChannel Struct
            StructureTypeInfo channelStructInfo = ioPackageInfo.getStructInfo(BYTE_CHANNEL_STRUCT_TYPE);
            Channel ballerinaSocketChannel = new SocketIOChannel(channel, 0);
            BMap<String, BValue> channelStruct =
                    BLangVMStructs.createBStruct(channelStructInfo, ballerinaSocketChannel);
            channelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, ballerinaSocketChannel);

            // Create Socket Struct
            StructureTypeInfo socketStructInfo = ioPackageInfo.getStructInfo(SOCKET_STRUCT_TYPE);
            BMap<String, BValue> socketStruct = BLangVMStructs.createBStruct(socketStructInfo);
            socketStruct.put(IOConstants.BYTE_CHANNEL_NAME, channelStruct);
            socketStruct.put(PORT_FIELD, new BInteger(socket.getPort()));
            socketStruct.put(LOCAL_PORT_OPTION_FIELD, new BInteger(socket.getLocalPort()));
            socketStruct.put(ADDRESS_FIELD, new BString(socket.getInetAddress().getHostAddress()));
            socketStruct.put(LOCAL_ADDRESS_FIELD,
                    new BString(socket.getLocalAddress().getHostAddress()));
            socketStruct.addNativeData(IOConstants.CLIENT_SOCKET_NAME, channel);
            context.setReturnValues(socketStruct);
        } catch (Throwable e) {
            String msg = "Failed to open a connection to [" + host + ":" + port + "] : " + e.getMessage();
            log.error(msg, e);
            context.setReturnValues(IOUtils.createError(context, msg));
        }
    }
}
