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

package org.ballerinalang.nativeimpl.socket;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.base.AbstractChannel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.channels.SocketChannel;

/**
 * Native function to close a Client socket.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "closeSocket",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Socket", structPackage = "ballerina.io"),
        isPublic = true
)
public class CloseSocket extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BStruct socket;
        try {
            socket = (BStruct) getRefArgument(context, 0);
            SocketChannel socketChannel = (SocketChannel) socket.getNativeData(IOConstants.CLIENT_SOCKET_NAME);
            BStruct byteChannelStruct = (BStruct) socket.getRefField(0);
            AbstractChannel byteChannel = (AbstractChannel) byteChannelStruct
                    .getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            socketChannel.close();
            byteChannel.close();
        } catch (Throwable e) {
            String message = "Failed to close the socket:" + e.getMessage();
            throw new BallerinaException(message, e, context);
        }
        return VOID_RETURN;
    }
}
