/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function ballerina.lo#readBytes.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "readBytes",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ByteChannel", structPackage = "ballerina.io"),
        args = {@Argument(name = "numberOfBytes", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.BLOB),
                @ReturnType(type = TypeKind.INT)},
        isPublic = true
)
public class ReadBytes extends AbstractNativeFunction {

    /**
     * Specifies the index which holds the number of bytes in ballerina.lo#readBytes.
     */
    private static final int NUMBER_OF_BYTES_INDEX = 0;

    /**
     * Specifies the index which contains the byte channel in ballerina.lo#readBytes.
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
    public BValue[] execute(Context context) {
        BStruct channel;
        int numberOfBytes;
        BBlob readByteBlob;
        BInteger numberOfReadBytes;
        try {
            channel = (BStruct) getRefArgument(context, BYTE_CHANNEL_INDEX);
            numberOfBytes = (int) getIntArgument(context, NUMBER_OF_BYTES_INDEX);
            Channel byteChannel = (Channel) channel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            byte[] readBytes = byteChannel.read(numberOfBytes);
            readByteBlob = new BBlob(readBytes);
            numberOfReadBytes = new BInteger(readBytes.length);
        } catch (Throwable e) {
            String message = "Error occurred while reading bytes. ";
            throw new BallerinaException(message + e.getMessage(), context);
        }
        return getBValues(readByteBlob, numberOfReadBytes);
    }
}
