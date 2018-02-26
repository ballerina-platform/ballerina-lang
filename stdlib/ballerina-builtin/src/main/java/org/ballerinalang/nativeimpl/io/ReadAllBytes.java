/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function ballerina.lo#readAllBytes.
 *
 * @since 0.95
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "readAllBytes",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ByteChannel", structPackage = "ballerina.io"),
        returnType = {@ReturnType(type = TypeKind.BLOB),
                @ReturnType(type = TypeKind.INT)},
        isPublic = true
)
public class ReadAllBytes extends BlockingNativeCallableUnit {
    /**
     * Specifies the index which contains the byte channel in ballerina.lo#readAllBytes.
     */
    private static final int BYTE_CHANNEL_INDEX = 0;

    /**
     * <p>
     * Reads bytes from a given channel.
     * </p>
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
        BStruct channel;
        BBlob readByteBlob;
        BInteger numberOfReadBytes;
        try {
            channel = (BStruct) context.getRefArgument(BYTE_CHANNEL_INDEX);
            Channel byteChannel = (Channel) channel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            byte[] readBytes = byteChannel.readAll();
            readByteBlob = new BBlob(readBytes);
            numberOfReadBytes = new BInteger(readBytes.length);
        } catch (Throwable e) {
            String message = "Error occurred while reading bytes:" + e.getMessage();
            throw new BallerinaException(message, context);
        }
        context.setReturnValues(readByteBlob, numberOfReadBytes);
    }
}
