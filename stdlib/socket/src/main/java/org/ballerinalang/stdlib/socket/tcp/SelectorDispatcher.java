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

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_ACCEPT;
import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_CLOSE;
import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_ERROR;
import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_READ_READY;

/**
 * This will handle the dispatching for TCP listener and client.
 *
 * @since 0.985.0
 */
class SelectorDispatcher {

    private static final Logger log = LoggerFactory.getLogger(SelectorDispatcher.class);

    /**
     * Invoke the 'onError' resource.
     *
     * @param socketService {@link SocketService} instance that contains SocketChannel and resource map
     * @param errorMsg      Reason for cause this
     */
    static void invokeOnError(SocketService socketService, String errorMsg) {
        try {
            Resource error = socketService.getResources().get(RESOURCE_ON_ERROR);
            ProgramFile programFile = error.getResourceInfo().getPackageInfo().getProgramFile();
            // An error can be thrown during the onAccept function. So there is a possibility of client not
            // available at that time. Hence the below null check.
            SocketChannel client = null;
            if (socketService.getSocketChannel() != null) {
                client = (SocketChannel) socketService.getSocketChannel();
            }
            BValue[] params = getOnErrorResourceSignature(client, programFile, errorMsg);
            Executor.submit(error, new TCPSocketCallback(socketService), null, null, params);
        } catch (Throwable e) {
            log.error("Error while executing onError resource", e);
        }
    }

    /**
     * Invoke the 'onError' resource with provided callback and error.
     *
     * @param socketService {@link SocketService} instance that contains SocketChannel and resource map
     * @param callback      {@link TCPSocketCallback} instance
     * @param error         {@link BError} instance which contains the error details
     */
    static void invokeOnError(SocketService socketService, TCPSocketCallback callback, BError error) {
        try {
            Resource errorResource = socketService.getResources().get(RESOURCE_ON_ERROR);
            ProgramFile programFile = errorResource.getResourceInfo().getPackageInfo().getProgramFile();
            // An error can be thrown during the onAccept function. So there is a possibility of client not
            // available at that time. Hence the below null check.
            SocketChannel client = null;
            if (socketService.getSocketChannel() != null) {
                client = (SocketChannel) socketService.getSocketChannel();
            }
            final BMap<String, BValue> caller = SocketUtils.createClient(programFile, client);
            BValue[] params = new BValue[] { caller, error };
            Executor.submit(errorResource, callback, null, null, params);
        } catch (Throwable e) {
            log.error("Error while executing onError resource", e);
        }
    }

    /**
     * Invoke the 'onReadReady' resource.
     *
     * @param socketService {@link SocketService} instance that contains SocketChannel and resource map
     * @param buffer        content that receive from client
     */
    static void invokeReadReady(SocketService socketService, ByteBuffer buffer) {
        try {
            final Resource readReady = socketService.getResources().get(RESOURCE_ON_READ_READY);
            BMap<String, BValue> caller = createCaller(socketService, readReady);
            BValue[] params = { caller, new BValueArray(SocketUtils.getByteArrayFromByteBuffer(buffer)) };
            Executor.submit(readReady, new TCPSocketCallback(socketService), null, null, params);
        } catch (BallerinaConnectorException e) {
            invokeOnError(socketService, e.getMessage());
        }
    }

    private static BMap<String, BValue> createCaller(SocketService socketService, Resource resource) {
        ProgramFile programFile = resource.getResourceInfo().getPackageInfo().getProgramFile();
        return SocketUtils.createClient(programFile, (SocketChannel) socketService.getSocketChannel());
    }

    /**
     * Invoke the 'onClose' resource.
     *
     * @param socketService {@link SocketService} instance that contains SocketChannel and resource map
     */
    static void invokeOnClose(SocketService socketService) {
        try {
            socketService.getSocketChannel().close();
            final Resource close = socketService.getResources().get(RESOURCE_ON_CLOSE);
            BMap<String, BValue> caller = createCaller(socketService, close);
            Executor.submit(close, new TCPSocketCallback(socketService), null, null, caller);
        } catch (IOException e) {
            String msg = "Unable to close the client connection properly";
            log.error(msg, e);
            invokeOnError(socketService, msg);
        } catch (BallerinaConnectorException e) {
            invokeOnError(socketService, e.getMessage());
        }
    }

    /**
     * Invoke the 'onAccept' resource.
     *
     * @param socketService {@link SocketService} instance that contains ServerSocketChannel and resource map
     * @param client        Newly accept socketChannel
     */
    static void invokeOnAccept(SocketService socketService, SocketChannel client) {
        try {
            Resource accept = socketService.getResources().get(RESOURCE_ON_ACCEPT);
            ProgramFile programFile = accept.getResourceInfo().getPackageInfo().getProgramFile();
            BValue[] params = getAcceptResourceSignature(client, programFile);
            Executor.submit(accept, new TCPSocketCallback(socketService), null, null, params);
        } catch (BallerinaConnectorException e) {
            // SocketService's socketChannel has the ServerSocketChannel instance only in this time.
            invokeOnError(socketService, e.getMessage());
        }
    }

    private static BValue[] getAcceptResourceSignature(SocketChannel client, ProgramFile programFile) {
        BMap<String, BValue> caller = SocketUtils.createClient(programFile, client);
        return new BValue[] { caller };
    }

    private static BValue[] getOnErrorResourceSignature(SocketChannel client, ProgramFile programFile, String msg) {
        BMap<String, BValue> caller = SocketUtils.createClient(programFile, client);
        BError error = SocketUtils.createSocketError(programFile, msg);
        return new BValue[] { caller, error };
    }
}
