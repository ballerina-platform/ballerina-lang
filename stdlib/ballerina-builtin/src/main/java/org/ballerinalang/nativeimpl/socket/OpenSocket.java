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
package org.ballerinalang.nativeimpl.socket;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.SocketIOChannel;
import org.ballerinalang.nativeimpl.io.channels.base.AbstractChannel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * Native function to open a Client socket.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "openSocket",
        args = {@Argument(name = "host", type = TypeKind.STRING),
                @Argument(name = "port", type = TypeKind.INT),
                @Argument(name = "option", type = TypeKind.STRUCT, structType = "SocketProperties",
                        structPackage = "ballerina.io")},
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "Socket", structPackage = "ballerina.io")},
        isPublic = true
)
public class OpenSocket extends AbstractNativeFunction {

    private static final String SOCKET_PACKAGE = "ballerina.io";
    private static final String SOCKET_STRUCT_TYPE = "Socket";
    private static final String BYTE_CHANNEL_STRUCT_TYPE = "ByteChannel";

    @Override
    public BValue[] execute(Context context) {
        final String host = getStringArgument(context, 0);
        final int port = (int) getIntArgument(context, 0);
        final BStruct options = (BStruct) getRefArgument(context, 0);

        Socket socket;
        SocketChannel channel;
        try {
            // Open a client connection
            SocketChannel socketChannel = SocketChannel.open();
            if (options.getIntField(0) > 0) {
                socketChannel.bind(new InetSocketAddress((int) options.getIntField(0)));
            }
            socketChannel.connect(new InetSocketAddress(host, port));
            socket = socketChannel.socket();
            channel = socket.getChannel();
        } catch (Throwable e) {
            String msg = "Failed to open a connection to [" + host + ":" + port + "] : " + e.getMessage();
            throw new BallerinaException(msg, e, context);
        }

        PackageInfo ioPackageInfo = context.getProgramFile().getPackageInfo(SOCKET_PACKAGE);
        // Create ByteChannel Struct
        StructInfo channelStructInfo = ioPackageInfo.getStructInfo(BYTE_CHANNEL_STRUCT_TYPE);
        AbstractChannel ballerinaSocketChannel = new SocketIOChannel(channel, 0);
        BStruct channelStruct = BLangVMStructs.createBStruct(channelStructInfo, ballerinaSocketChannel);
        channelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, ballerinaSocketChannel);

        // Create Socket Struct
        StructInfo socketStructInfo = ioPackageInfo.getStructInfo(SOCKET_STRUCT_TYPE);
        BStruct socketStruct = BLangVMStructs.createBStruct(socketStructInfo);
        socketStruct.setRefField(0, channelStruct);
        socketStruct.setIntField(0, socket.getPort());
        socketStruct.setIntField(1, socket.getLocalPort());
        socketStruct.setStringField(0, socket.getInetAddress().getHostAddress());
        socketStruct.setStringField(1, socket.getLocalAddress().getHostAddress());
        socketStruct.addNativeData(IOConstants.CLIENT_SOCKET_NAME, channel);
        return getBValues(socketStruct);
    }
}
