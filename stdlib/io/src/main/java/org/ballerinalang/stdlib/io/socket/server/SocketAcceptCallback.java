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
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.io.socket.SocketConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.ballerinalang.util.codegen.PackageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * This represent Socket accept callback where once the socket is accept and task that need to perform.
 *
 * @since 0.975.1
 */
public class SocketAcceptCallback {

    private static final Logger log = LoggerFactory.getLogger(SocketAcceptCallback.class);

    private Context context;
    private CallableUnitCallback callback;

    public SocketAcceptCallback(Context context, CallableUnitCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void notifyAccept() {
        BMap<String, BValue> serverSocketStruct = (BMap<String, BValue>) context.getRefArgument(0);
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) serverSocketStruct
                .getNativeData(SocketConstants.SERVER_SOCKET_KEY);
        SocketChannel socketChannel = SocketQueue.getSocket(serverSocketChannel.hashCode());
        if (socketChannel != null) {
            try {
                PackageInfo ioPackageInfo = context.getProgramFile().getPackageInfo(SocketConstants.SOCKET_PACKAGE);
                BMap<String, BValue> socketStruct = ServerSocketUtils.getSocketStruct(socketChannel, ioPackageInfo);
                context.setReturnValues(socketStruct);
                callback.notifySuccess();
            } catch (IOException e) {
                String msg = "Failed to open a client connection: " + e.getMessage();
                log.error(msg, e);
                context.setReturnValues(IOUtils.createError(context, msg));
            }
        }
    }
}
