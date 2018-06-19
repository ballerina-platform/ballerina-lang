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

package org.ballerinalang.nativeimpl.socket.server;

import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.SocketIOChannel;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class ServerSocketUtils {

    private static final String SOCKET_STRUCT_TYPE = "Socket";
    private static final String BYTE_CHANNEL_STRUCT_TYPE = "ByteChannel";

    public static BStruct getSocketStruct(SocketChannel socketChannel, PackageInfo ioPackageInfo) throws IOException {
        StructureTypeInfo socketStructInfo = ioPackageInfo.getStructInfo(SOCKET_STRUCT_TYPE);
        BStruct socketStruct = BLangVMStructs.createBStruct(socketStructInfo);
        Socket socket = socketChannel.socket();
        socketStruct.setRefField(0, getByteChannelStruct(socketChannel, ioPackageInfo));
        socketStruct.setIntField(0, socket.getPort());
        socketStruct.setIntField(1, socket.getLocalPort());
        socketStruct.setStringField(0, socket.getInetAddress().getHostAddress());
        socketStruct.setStringField(1, socket.getLocalAddress().getHostAddress());
        socketStruct.addNativeData(IOConstants.CLIENT_SOCKET_NAME, socketChannel);
        return socketStruct;
    }

    private static BStruct getByteChannelStruct(SocketChannel socketChannel, PackageInfo ioPackageInfo)
            throws IOException {
        StructureTypeInfo channelStructInfo = ioPackageInfo.getStructInfo(BYTE_CHANNEL_STRUCT_TYPE);
        Channel ballerinaSocketChannel = new SocketIOChannel(socketChannel, 0);
        BStruct channelStruct = BLangVMStructs.createBStruct(channelStructInfo, ballerinaSocketChannel);
        channelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, ballerinaSocketChannel);
        return channelStruct;
    }
}
