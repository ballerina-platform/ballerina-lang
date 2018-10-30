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
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;

import java.net.Socket;
import java.nio.channels.SocketChannel;

import static org.ballerinalang.stdlib.socket.SocketConstants.CALLER_ACTION;
import static org.ballerinalang.stdlib.socket.SocketConstants.ID;
import static org.ballerinalang.stdlib.socket.SocketConstants.LOCAL_ADDRESS;
import static org.ballerinalang.stdlib.socket.SocketConstants.LOCAL_PORT;
import static org.ballerinalang.stdlib.socket.SocketConstants.REMOTE_ADDRESS;
import static org.ballerinalang.stdlib.socket.SocketConstants.REMOTE_PORT;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_KEY;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;

/**
 * Represents the util functions of Socket operations.
 */
public class SocketUtils {

    /**
     * Returns the error struct for the corresponding message.
     *
     * @param context context of the extern function
     * @param message error message
     * @return error
     */
    public static BError createError(Context context, String message) {
        return BLangVMErrors.createError(context, false, message);
    }

    /**
     * Create a `CallerAction` object that associated with the given SocketChannel.
     *
     * @param programFile A program file
     * @param client      SocketClient that associate with this caller action
     * @return 'CallerAction' object
     */
    public static BMap<String, BValue> createCallerAction(ProgramFile programFile, SocketChannel client) {
        BMap<String, BValue> callerEndpoint = BLangConnectorSPIUtil
                .createBStruct(programFile, SOCKET_PACKAGE, CALLER_ACTION);
        callerEndpoint.addNativeData(SOCKET_KEY, client);
        BMap<String, BValue> endpoint = BLangConnectorSPIUtil.createBStruct(programFile, SOCKET_PACKAGE, "Listener");
        if (client != null) {
            Socket socket = client.socket();
            endpoint.put(REMOTE_PORT, new BInteger(socket.getPort()));
            endpoint.put(LOCAL_PORT, new BInteger(socket.getLocalPort()));
            endpoint.put(REMOTE_ADDRESS, new BString(socket.getInetAddress().getHostAddress()));
            endpoint.put(LOCAL_ADDRESS, new BString(socket.getLocalAddress().getHostAddress()));
            endpoint.put(ID, new BInteger(client.hashCode()));
        }
        endpoint.put("callerAction", callerEndpoint);
        return endpoint;
    }
}
