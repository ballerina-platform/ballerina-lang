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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.SocketIOChannel;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;

/**
 * Extern function to ShutdownOutput in a socket.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "shutdownOutput",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Socket", structPackage = "ballerina/io"),
        returnType = { @ReturnType(type = TypeKind.RECORD, structType = "error")},
        isPublic = true
)
public class ShutdownOutput extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(ShutdownOutput.class);
    private static final String READABLE_CHANNEL = "readableChannel";
    private static final String WRITABLE_CHANNEL = "writableChannel";

    @Override
    public void execute(Context context) {
        BMap<String, BValue> socket;
        try {
            socket = (BMap<String, BValue>) context.getRefArgument(0);
            shutdownChannel((BMap<String, BValue>) socket.get(READABLE_CHANNEL));
            shutdownChannel((BMap<String, BValue>) socket.get(WRITABLE_CHANNEL));
        } catch (NotYetConnectedException e) {
            String message = "Socket is not connected.";
            context.setReturnValues(IOUtils.createError(context, message));
        } catch (ClosedChannelException e) {
            String message = "Socket connection already closed.";
            context.setReturnValues(IOUtils.createError(context, message));
        } catch (IOException e) {
            String message = "Failed to shutdown input in socket:" + e.getMessage();
            log.error(message, e);
            context.setReturnValues(IOUtils.createError(context, message));
        }
        context.setReturnValues();
    }

    private void shutdownChannel(BMap<String, BValue> byteChannelStruct) throws IOException {
        Channel channel = (Channel) byteChannelStruct.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        if (channel instanceof SocketIOChannel) {
            SocketIOChannel socketIOChannel = (SocketIOChannel) channel;
            socketIOChannel.shutdownOutput();
        }
    }
}
