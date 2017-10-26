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
 * Native function ballerina.lo#writeBytes.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "writeBytes",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ByteChannel", structPackage = "ballerina.io"),
        args = {@Argument(name = "content", type = TypeKind.BLOB),
                @Argument(name = "startOffset", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.INT)},
        isPublic = true
)
public class WriteBytes extends AbstractNativeFunction {

    /**
     * Index which holds the byte channel in ballerina.io#writeBytes.
     */
    private static final int BYTE_CHANNEL_INDEX = 0;

    /**
     * Index which holds the content in ballerina.io#writeBytes.
     */
    private static final int CONTENT_INDEX = 0;

    /**
     * Index which holds the start offset in ballerina.io#writeBytes.
     */
    private static final int START_OFFSET_INDEX = 0;

    /**
     * Writes bytes to a given channel.
     *
     * {@inheritDoc}
     */
    @Override
    public BValue[] execute(Context context) {
        BStruct channel;
        byte[] content;
        int startOffset;
        int numberOfBytesWritten;
        try {
            channel = (BStruct) getRefArgument(context, BYTE_CHANNEL_INDEX);
            content = getBlobArgument(context, CONTENT_INDEX);
            startOffset = (int) getIntArgument(context, START_OFFSET_INDEX);
            Channel byteChannel = (Channel) channel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            numberOfBytesWritten = byteChannel.write(content, startOffset);
        } catch (Throwable e) {
            String message = "Error occurred while writing bytes .";
            throw new BallerinaException(message + e.getMessage(), context);
        }
        return getBValues(new BInteger(numberOfBytesWritten));
    }
}
