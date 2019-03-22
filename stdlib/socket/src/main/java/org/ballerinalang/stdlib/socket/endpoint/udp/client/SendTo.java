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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.socket.SocketConstants;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;

import static org.ballerinalang.stdlib.socket.SocketConstants.SOCKET_PACKAGE;
import static org.ballerinalang.stdlib.socket.SocketConstants.UDP_CLIENT;

/**
 * 'SendTo' method implementation of the UDP socket client action.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "socket",
        functionName = "sendTo",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = UDP_CLIENT, structPackage = SOCKET_PACKAGE),
        isPublic = true
)
public class SendTo extends BlockingNativeCallableUnit {
    private static final Logger log = LoggerFactory.getLogger(SendTo.class);

    @Override
    public void execute(Context context) {
        BMap<String, BValue> clientEndpoint = (BMap<String, BValue>) context.getRefArgument(0);
        DatagramChannel socket = (DatagramChannel) clientEndpoint.getNativeData(SocketConstants.SOCKET_KEY);
        BValueArray content = (BValueArray) context.getRefArgument(1);
        BMap<String, BValue> remoteAddress = (BMap<String, BValue>) context.getRefArgument(2);
        BString host = (BString) remoteAddress.get(SocketConstants.CONFIG_FIELD_HOST);
        BInteger port = (BInteger) remoteAddress.get(SocketConstants.CONFIG_FIELD_PORT);
        byte[] byteContent = content.getBytes();
        if (log.isDebugEnabled()) {
            log.debug("No of byte going to write[" + socket.hashCode() + "]: " + byteContent.length);
        }
        try {
            final InetSocketAddress remote = new InetSocketAddress(host.stringValue(), (int) port.intValue());
            int write = socket.send(ByteBuffer.wrap(byteContent), remote);
            if (log.isDebugEnabled()) {
                log.debug("No of byte written for the client[" + socket.hashCode() + "]: " + write);
            }
            context.setReturnValues(new BInteger(write));
        } catch (ClosedChannelException e) {
            context.setReturnValues(SocketUtils.createSocketError(context, "Client socket close already."));
        } catch (IOException e) {
            context.setReturnValues(SocketUtils.createSocketError(context, "Write failed."));
            log.error("Unable to perform write[" + socket.hashCode() + "]", e);
        }
    }
}
