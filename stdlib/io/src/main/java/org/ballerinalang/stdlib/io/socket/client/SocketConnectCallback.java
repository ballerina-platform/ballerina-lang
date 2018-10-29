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
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.io.channels.SocketIOChannel;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.socket.SocketConstants;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * This represent Socket connect callback where once the socket is successfully connect to the remote server.
 *
 * @since 0.980.1
 */
public class SocketConnectCallback {

    private static final Logger log = LoggerFactory.getLogger(SocketConnectCallback.class);
    private static final String READABLE_BYTE_CHANNEL_STRUCT_TYPE = "ReadableByteChannel";
    private static final String READABLE_CHANNEL = "readableChannel";
    private static final String WRITABLE_BYTE_CHANNEL_STRUCT_TYPE = "WritableByteChannel";
    private static final String WRITABLE_CHANNEL = "writableChannel";

    private Context context;
    private CallableUnitCallback callback;

    SocketConnectCallback(Context context, CallableUnitCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void notifyConnect() {
        final String host = context.getStringArgument(0);
        final int port = (int) context.getIntArgument(0);
        BMap<String, BValue> socketStruct = (BMap<String, BValue>) context.getRefArgument(0);
        SocketChannel socketChannel = (SocketChannel) socketStruct.getNativeData(SocketConstants.SOCKET_KEY);
        if (log.isDebugEnabled()) {
            log.debug("Successfully connected to the remote server[" + host + ":" + port + "].");
        }
        try {
            Socket socket = socketChannel.socket();
            if (log.isDebugEnabled()) {
                log.debug("Bound local port: " + socket.getLocalPort());
                log.debug("Timeout on blocking Socket operations: " + socket.getSoTimeout());
                log.debug("ReceiveBufferSize: " + socket.getReceiveBufferSize());
                log.debug("KeepAlive: " + socket.getKeepAlive());
            }
            PackageInfo ioPackageInfo = context.getProgramFile().getPackageInfo(SocketConstants.SOCKET_PACKAGE);

            // Create Socket Struct
            socketStruct.put(READABLE_CHANNEL, getByteChannelStruct(socketChannel, ioPackageInfo,
                    READABLE_BYTE_CHANNEL_STRUCT_TYPE));
            socketStruct.put(WRITABLE_CHANNEL, getByteChannelStruct(socketChannel, ioPackageInfo,
                    WRITABLE_BYTE_CHANNEL_STRUCT_TYPE));
            socketStruct.put(SocketConstants.REMOTE_PORT_FIELD, new BInteger(socket.getPort()));
            socketStruct.put(SocketConstants.LOCAL_PORT_OPTION_FIELD, new BInteger(socket.getLocalPort()));
            socketStruct
                    .put(SocketConstants.REMOTE_ADDRESS_FIELD, new BString(socket.getInetAddress().getHostAddress()));
            socketStruct
                    .put(SocketConstants.LOCAL_ADDRESS_FIELD, new BString(socket.getLocalAddress().getHostAddress()));
            socketStruct.addNativeData(IOConstants.CLIENT_SOCKET_NAME, socketChannel);
            context.setReturnValues();
            callback.notifySuccess();
        } catch (Throwable e) {
            String msg = "Failed to open a connection to [" + host + ":" + port + "] : " + e.getMessage();
            log.error(msg, e);
            BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, e.getMessage());
            context.setReturnValues(errorStruct);
            callback.notifySuccess();
        }
    }

    private static BMap<String, BValue> getByteChannelStruct(SocketChannel socketChannel, PackageInfo ioPackageInfo,
                                                             String channelType) {
        StructureTypeInfo channelStructInfo = ioPackageInfo.getStructInfo(channelType);
        Channel ballerinaSocketChannel = new SocketIOChannel(socketChannel, true);
        BMap<String, BValue> channelStruct = BLangVMStructs.createBStruct(channelStructInfo);
        channelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, ballerinaSocketChannel);
        return channelStruct;
    }
}
