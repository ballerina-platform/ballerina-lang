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
import org.ballerinalang.nativeimpl.io.events.EventManager;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.bytes.ReadBytesEvent;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
     * Will be the I/O event handler.
     */
    private EventManager eventManager = EventManager.getInstance();


    /**
     * Wraps byte [] to a buffer.
     *
     * @return the buffer which as the wrapped byte []
     */
    private ByteBuffer wrapByteArray(byte[] content, int offset) {
        ByteBuffer bufferedContent = ByteBuffer.wrap(content);
        bufferedContent.position(offset);
        return bufferedContent;
    }

    /**
     * Asynchronously reads bytes from the channel.
     *
     * @param content               the initialized array which should be filled with the content.
     * @param channel               the channel the content should be read into.
     * @param expectedNumberOfBytes the number of bytes which should be read
     * @throws InterruptedException during interrupt error.
     * @throws ExecutionException   errors which occurs while execution.
     */
    private BInteger readAsync(byte[] content, Channel channel, int expectedNumberOfBytes)
            throws InterruptedException, ExecutionException {
        int numberOfBytesRead = 0;
        do {
            //TODO we need to validate value provided for numberOfBytesRead
            ByteBuffer contentBuffer = wrapByteArray(content, numberOfBytesRead);
            ReadBytesEvent event = new ReadBytesEvent(contentBuffer, channel);
            Future<EventResult> future = eventManager.publish(event);
            EventResult eventResponse = future.get();
            numberOfBytesRead = numberOfBytesRead + (Integer) eventResponse.getResponse();
        } while (numberOfBytesRead < expectedNumberOfBytes && !channel.hasReachedEnd());
        return new BInteger(numberOfBytesRead);
    }

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
            byte[] content = new byte[numberOfBytes];
            numberOfReadBytes = readAsync(content, byteChannel, numberOfBytes);
            readByteBlob = new BBlob(content);
/*            byte[] readBytes = byteChannel.readFull(numberOfBytes);
            readByteBlob = new BBlob(readBytes);
            numberOfReadBytes = new BInteger(readBytes.length);*/
        } catch (Throwable e) {
            String message = "Error occurred while reading bytes:" + e.getMessage();
            throw new BallerinaException(message, context);
        }
        return getBValues(readByteBlob, numberOfReadBytes);
    }
}
