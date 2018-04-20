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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ByteChannel;

/**
 * Native function to close a Client socket.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "close",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Socket", structPackage = "ballerina.io"),
        returnType = { @ReturnType(type = TypeKind.STRUCT, structType = "error")},
        isPublic = true
)
public class Close extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(Close.class);

    @Override
    public void execute(Context context) {
        BStruct socket;
        try {
            socket = (BStruct) context.getRefArgument(0);
            ByteChannel byteChannel = (ByteChannel) socket.getNativeData(IOConstants.CLIENT_SOCKET_NAME);
            BStruct byteChannelStruct = (BStruct) socket.getRefField(0);
            Channel channel = (Channel) byteChannelStruct.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            byteChannel.close();
            channel.close();
        } catch (Throwable e) {
            String message = "Failed to close the socket:" + e.getMessage();
            log.error(message, e);
            context.setReturnValues(IOUtils.createError(context, message));
        }
        context.setReturnValues();
    }
}
