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

import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.io.channels.SocketIOChannel;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.socket.SocketConstants;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * Util class for ServerSocket related operation.
 *
 * @since 0.975.1
 */
public class ServerSocketUtils {

    private static final String SOCKET_STRUCT_TYPE = "Socket";
    private static final String BYTE_CHANNEL_STRUCT_TYPE = "ByteChannel";

    /**
     * Create new socket struct.
     *
     * @param socketChannel Java ServerSocket channel.
     * @param ioPackageInfo {@link PackageInfo} instance that contains the IO package.
     * @return Ballerina socket struct.
     * @throws IOException Throws if unable to create a Java socket from SocketChannel.
     */
    public static BMap<String, BValue> getSocketStruct(SocketChannel socketChannel, PackageInfo ioPackageInfo)
            throws IOException {
        StructureTypeInfo socketStructInfo = ioPackageInfo.getStructInfo(SOCKET_STRUCT_TYPE);
        Socket socket = socketChannel.socket();
        BMap<String, BValue> socketStruct = BLangVMStructs.createBStruct(socketStructInfo);
        socketStruct.put(IOConstants.BYTE_CHANNEL_NAME, getByteChannelStruct(socketChannel, ioPackageInfo));
        socketStruct.put(SocketConstants.REMOTE_PORT_FIELD, new BInteger(socket.getPort()));
        socketStruct.put(SocketConstants.LOCAL_PORT_OPTION_FIELD, new BInteger(socket.getLocalPort()));
        socketStruct.put(SocketConstants.REMOTE_ADDRESS_FIELD, new BString(socket.getInetAddress().getHostAddress()));
        socketStruct.put(SocketConstants.LOCAL_ADDRESS_FIELD, new BString(socket.getLocalAddress().getHostAddress()));
        socketStruct.addNativeData(IOConstants.CLIENT_SOCKET_NAME, socketChannel);
        return socketStruct;
    }

    private static BMap<String, BValue> getByteChannelStruct(SocketChannel socketChannel, PackageInfo ioPackageInfo)
            throws IOException {
        StructureTypeInfo channelStructInfo = ioPackageInfo.getStructInfo(BYTE_CHANNEL_STRUCT_TYPE);
        Channel ballerinaSocketChannel = new SocketIOChannel(socketChannel, 0);
        BMap<String, BValue> channelStruct = BLangVMStructs.createBStruct(channelStructInfo, ballerinaSocketChannel);
        channelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, ballerinaSocketChannel);
        return channelStruct;
    }
}
