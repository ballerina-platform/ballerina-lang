/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.socket.endpoint.udp.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.socket.SocketConstants;
import org.ballerinalang.stdlib.socket.tcp.ReadPendingCallback;
import org.ballerinalang.stdlib.socket.tcp.ReadPendingSocketMap;
import org.ballerinalang.stdlib.socket.tcp.SelectorManager;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.DatagramChannel;

import static org.ballerinalang.stdlib.socket.SocketConstants.DEFAULT_EXPECTED_READ_LENGTH;
import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;
import static org.ballerinalang.stdlib.socket.SocketConstants.UDP_CLIENT;

/**
 * 'ReceiveFrom' method implementation of the UDP socket client action.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "receiveFrom",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = UDP_CLIENT, structPackage = SOCKET_PACKAGE),
        isPublic = true
)
public class ReceiveFrom implements NativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(ReceiveFrom.class);

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> clientEndpoint = (BMap<String, BValue>) context.getRefArgument(0);
        int expectedLength = (int) context.getIntArgument(0);
        if (expectedLength != DEFAULT_EXPECTED_READ_LENGTH && expectedLength < 1) {
            String msg = "Requested byte length need to be 1 or more";
            callback.notifyFailure(SocketUtils.createSocketError(context, msg));
            return;
        }
        DatagramChannel socket = (DatagramChannel) clientEndpoint.getNativeData(SocketConstants.SOCKET_KEY);
        final ReadPendingCallback readPendingCallback = new ReadPendingCallback(context, callback, expectedLength);
        ReadPendingSocketMap.getInstance().add(socket.hashCode(), readPendingCallback);
        log.debug("Notify to invokeRead");
        SelectorManager.getInstance().invokeRead(socket.hashCode(), false);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
