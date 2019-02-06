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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.stdlib.socket.SocketConstants.CLIENT;
import static org.ballerinalang.stdlib.socket.SocketConstants.ID;
import static org.ballerinalang.stdlib.socket.SocketConstants.LOCAL_ADDRESS;
import static org.ballerinalang.stdlib.socket.SocketConstants.LOCAL_PORT;
import static org.ballerinalang.stdlib.socket.SocketConstants.REMOTE_ADDRESS;
import static org.ballerinalang.stdlib.socket.SocketConstants.REMOTE_PORT;
import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_CONNECT;
import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_ERROR;
import static org.ballerinalang.stdlib.socket.SocketConstants.RESOURCE_ON_READ_READY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_SERVICE;

/**
 * Represents the util functions of Socket operations.
 *
 * @since 0.985.0
 */
public class SocketUtils {

    private static final String SOCKET_ERROR_CODE = "{ballerina/socket}SocketError";
    private static final String SOCKET_ERROR = "SocketError";

    /**
     * Creates an error message.
     *
     * @param context context which is invoked
     * @param errMsg  the cause for the error
     * @return an error which will be propagated to ballerina user
     */
    public static BError createSocketError(Context context, String errMsg) {
        BMap<String, BValue> errorRecord = BLangConnectorSPIUtil.createBStruct(context, SOCKET_PACKAGE, SOCKET_ERROR);
        errorRecord.put("message", new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, SOCKET_ERROR_CODE, errorRecord);
    }

    /**
     * Creates an error message.
     *
     * @param programFile ProgramFile which is used
     * @param errMsg      the cause for the error
     * @return an error which will be propagated to ballerina user
     */
    public static BError createSocketError(ProgramFile programFile, String errMsg) {
        BMap<String, BValue> errorRecord = BLangConnectorSPIUtil
                .createBStruct(programFile, SOCKET_PACKAGE, SOCKET_ERROR);
        errorRecord.put("message", new BString(errMsg));
        return BLangVMErrors.createError(SOCKET_ERROR_CODE, errorRecord);
    }

    /**
     * Create a `Caller` object that associated with the given SocketChannel.
     *
     * @param programFile A program file
     * @param socketService {@link SocketService} instance that contains SocketChannel and resource map
     * @return 'Caller' object
     */
    public static BMap<String, BValue> createClient(ProgramFile programFile, SocketService socketService) {
        BValue[] args = new BValue[] { null };
        // Passing parameters as null to prevent object init in the socket client.
        BMap<String, BValue> caller = BLangConnectorSPIUtil.createObject(programFile, SOCKET_PACKAGE, CLIENT, args);
        caller.addNativeData(SOCKET_SERVICE, socketService);
        SocketChannel client = null;
        // An error can be thrown during the onAccept function. So there is a possibility of client not
        // available at that time. Hence the below null check.
        if (socketService.getSocketChannel() != null) {
            client = (SocketChannel) socketService.getSocketChannel();
        }
        if (client != null) {
            caller.addNativeData(SOCKET_KEY, client);
            Socket socket = client.socket();
            caller.put(REMOTE_PORT, new BInteger(socket.getPort()));
            caller.put(LOCAL_PORT, new BInteger(socket.getLocalPort()));
            caller.put(REMOTE_ADDRESS, new BString(socket.getInetAddress().getHostAddress()));
            caller.put(LOCAL_ADDRESS, new BString(socket.getLocalAddress().getHostAddress()));
            caller.put(ID, new BInteger(client.hashCode()));
        }
        return caller;
    }

    /**
     * This will return a byte array that only contains the data from ByteBuffer.
     * This will not copy any unused byte from ByteBuffer.
     *
     * @param content {@link ByteBuffer} with content
     * @return a byte array
     */
    public static byte[] getByteArrayFromByteBuffer(ByteBuffer content) {
        int contentLength = content.position();
        byte[] bytesArray = new byte[contentLength];
        content.flip();
        content.get(bytesArray, 0, contentLength);
        return bytesArray;
    }

    /**
     * This will filter out resource information from a given {@link Service} instance.
     *
     * @param service Service instance which contains the resource information
     * @return {@link Map} that contains the {@link Resource} instances
     */
    public static Map<String, Resource> getResourceRegistry(Service service) {
        Map<String, Resource> registry = new HashMap<>(5);
        byte resourceCount = 0;
        for (Resource resource : service.getResources()) {
            switch (resource.getName()) {
                case RESOURCE_ON_CONNECT:
                    registry.put(RESOURCE_ON_CONNECT, resource);
                    resourceCount++;
                    break;
                case RESOURCE_ON_READ_READY:
                    registry.put(RESOURCE_ON_READ_READY, resource);
                    resourceCount++;
                    break;
                case RESOURCE_ON_ERROR:
                    registry.put(RESOURCE_ON_ERROR, resource);
                    resourceCount++;
                    break;
                default:
                    // Do nothing.
            }
            if (resourceCount == 3) {
                break;
            }
        }
        return registry;
    }
}
