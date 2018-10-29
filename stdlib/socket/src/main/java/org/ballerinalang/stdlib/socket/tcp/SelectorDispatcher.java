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

package org.ballerinalang.stdlib.socket.tcp;

import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_ACCEPT;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_CLOSE;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_ERROR;
import static org.ballerinalang.stdlib.socket.SocketConstants.LISTENER_RESOURCE_ON_READ_READY;
import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG;

/**
 * This will handle the dispatching for TCP listener and client.
 *
 * @since 0.985.0
 */
class SelectorDispatcher {

    private static final Logger log = LoggerFactory.getLogger(SelectorDispatcher.class);

    static void invokeOnError(SocketService socketService, String s) {
        Resource error = socketService.getResources().get(LISTENER_RESOURCE_ON_ERROR);
        ProgramFile programFile = error.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile();
        SocketChannel client = null;
        if (socketService.getSocketChannel() != null) {
            client = (SocketChannel) socketService.getSocketChannel();
        }
        BValue[] params = getOnErrorResourceSignature(client, programFile, s);
        try {
            Executor.submit(error, new TCPSocketCallableUnitCallback(), null, null, params);
        } catch (BallerinaConnectorException e) {
            log.error("Error while exectuing onError resource", e);
        }
    }

    static void invokeReadReady(SocketService socketService, ByteBuffer buffer) {
        final Resource readReady = socketService.getResources().get(LISTENER_RESOURCE_ON_READ_READY);
        ProgramFile programFile = readReady.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile();
        BMap<String, BValue> endpoint = SocketUtils
                .createCallerAction(programFile, (SocketChannel) socketService.getSocketChannel());
        BValue[] params = { endpoint, new BByteArray(getByteArrayFromByteBuffer(buffer)) };
        try {
            Executor.submit(readReady, new TCPSocketCallableUnitCallback(), null, null, params);
        } catch (BallerinaConnectorException e) {
            invokeOnError(socketService, e.getMessage());
        }
    }

    static void invokeOnClose(SocketService socketService) {
        try {
            socketService.getSocketChannel().close();

            final Resource close = socketService.getResources().get(LISTENER_RESOURCE_ON_CLOSE);
            ProgramFile programFile = close.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile();
            BMap<String, BValue> endpoint = SocketUtils
                    .createCallerAction(programFile, (SocketChannel) socketService.getSocketChannel());
            BValue[] params = { endpoint };
            Executor.submit(close, new TCPSocketCallableUnitCallback(), null, null, params);
        } catch (IOException e) {
            invokeOnError(socketService, "Unable to close the client connection properly");
        } catch (BallerinaConnectorException e) {
            invokeOnError(socketService, e.getMessage());
        }
    }

    static void invokeOnAccept(SocketService socketService, SocketChannel client) {
        Resource accept = socketService.getResources().get(LISTENER_RESOURCE_ON_ACCEPT);
        ProgramFile programFile = accept.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile();
        BValue[] params = getAcceptResourceSignature(client, programFile);
        try {
            Executor.submit(accept, new TCPSocketCallableUnitCallback(), null, null, params);
        } catch (BallerinaConnectorException e) {
            invokeOnError(socketService, e.getMessage());
        }
    }

    private static BValue[] getAcceptResourceSignature(SocketChannel client, ProgramFile programFile) {
        BMap<String, BValue> endpoint = SocketUtils.createCallerAction(programFile, client);
        return new BValue[] { endpoint };
    }

    private static BValue[] getOnErrorResourceSignature(SocketChannel client, ProgramFile programFile, String msg) {
        BMap<String, BValue> endpoint = SocketUtils.createCallerAction(programFile, client);
        BMap<String, BValue> error = createError(programFile, msg);
        return new BValue[] { endpoint, error };
    }

    private static BMap<String, BValue> createError(ProgramFile programFile, String msg) {
        PackageInfo builtInPkg = programFile.getPackageInfo(BALLERINA_BUILTIN_PKG);
        StructureTypeInfo error = builtInPkg.getStructInfo(BLangVMErrors.STRUCT_GENERIC_ERROR);
        return BLangVMStructs.createBStruct(error, msg);
    }

    private static byte[] getByteArrayFromByteBuffer(ByteBuffer content) {
        int contentLength = content.position();
        byte[] bytesArray = new byte[contentLength];
        content.flip();
        content.get(bytesArray, 0, contentLength);
        return bytesArray;
    }
}
