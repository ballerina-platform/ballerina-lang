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

package org.ballerinalang.nativeimpl.io.channels.base;

import org.ballerinalang.nativeimpl.io.BallerinaIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * <p>
 * Specialized ByteBuffer which will dynamically resize, maintain state and call the underlying ByteChannel as
 * necessary to return the required amount of bytes.
 * </p>
 * <p>
 * Buffer also focuses on offering the capability to get bytes through multiple channels and place into one.
 * </p>
 */
public class Buffer {

    /**
     * Contains the buffer instance which will be wrapped.
     */
    private ByteBuffer byteBuffer;

    /**
     * <p>
     * This is used for performance reasons, specified the minimum byte count which should be read from channel.
     * </p>
     * <p>
     * This will ensure to always maintain the minimum number of bytes in the buffer. Even if the required amount of
     * bytes < than the minimum so that that I/O overhead could be minimized.
     * </p>
     */
    private int minimumSizeOfBuffer;

    private static final Logger log = LoggerFactory.getLogger(Buffer.class);

    public Buffer(int minimumSizeOfBuffer) {
        this.minimumSizeOfBuffer = minimumSizeOfBuffer;
    }

    /**
     * <p>
     * Returns a ByteBuffer which will contain any remaining bytes left in the previous read.
     * </p>
     * <p>
     * This operation will not guarantee a return of all the bytes required. This will return bytes if any is
     * available in the buffer.
     * </p>
     * <p>
     * The operation will return null if none of the bytes are remaining in the buffer.
     * </p>
     *
     * @param totalNumberOfBytesRequired number of bytes required.
     * @return new ByteBuffer which will contain bytes which are remaining.
     */
    private ByteBuffer getBytesLeftInBuffer(int totalNumberOfBytesRequired) {
        ByteBuffer bufferedContent = null;
        if (null != byteBuffer) {
            int position = byteBuffer.position();
            int limit = byteBuffer.limit();
            int numberOfBytesRemainingInBuffer = limit - position;
            if (numberOfBytesRemainingInBuffer == 0) {
                //Given there's nothing to get from the buffer
                return null;
            }
            if (numberOfBytesRemainingInBuffer > totalNumberOfBytesRequired) {
                //This means there's excess amount of bytes than requested, hence we narrow down the limit
                limit = position + totalNumberOfBytesRequired;
            }
            byte[] content = Arrays.copyOfRange(byteBuffer.array(), position, limit);
            int readBufferPosition = position + content.length;
            bufferedContent = ByteBuffer.wrap(content);
            byteBuffer.position(readBufferPosition);
            //We need to keep the buffer in write mode
            bufferedContent.position(bufferedContent.limit());
        } else {
            if (log.isDebugEnabled()) {
                log.debug("ByteBuffer has not being initialized, buffer will be initialized while reading the " +
                        "requested amount of " + totalNumberOfBytesRequired + " of bytes");
            }
        }
        return bufferedContent;
    }

    /**
     * <p>
     * Will allocate a buffer for the specified size.
     * </p>
     * <p>
     * If the size < minimumSizeOfBuffer the size of the new buffer = minimumSizeOfBuffer.
     * If the size > minimumSizeOfBuffer the size of the new buffer = size.
     * </p>
     *
     * @param size the size of the buffer.
     * @return the newly allocated buffer for the specified size.
     */
    private ByteBuffer allocate(int size) {
        if (size < minimumSizeOfBuffer) {
            size = minimumSizeOfBuffer;
        }
        return ByteBuffer.allocate(size);
    }

    /**
     * <p>
     * Will return a copy of the buffer which contains the read content.
     * </p>
     *
     * @param srcBuffer the buffer which will be compressed.
     * @return the compressed buffer.
     */
    private ByteBuffer compactBytes(ByteBuffer srcBuffer) {
        int bufferSize = srcBuffer.capacity();
        int bufferReadPosition = srcBuffer.position();
        ByteBuffer response;
        if (bufferSize > bufferReadPosition) {
            byte[] resizedContent = Arrays.copyOfRange(srcBuffer.array(), 0, bufferReadPosition);
            response = ByteBuffer.wrap(resizedContent);
            //We need to keep the buffer in write mode
            response.position(response.limit());
        } else {
            response = srcBuffer;
        }
        return response;
    }

    /**
     * <p>
     * Reads the specified number of bytes through ByteChannel.
     * </p>
     * <p>
     * This operation will read bytes from the channel. The channel may or may not return all required bytes. If the
     * offset it specified the bytes will be read from the offset position.
     * </p>
     * <p>
     * If the channel doesn't return any bytes it will return an empty buffer.
     * </p>
     *
     * @param requiredNumberOfBytes the number of bytes required to be read from the channel.
     * @param offset                the number of bytes already present.
     * @return ByteBuffer which wraps the bytes, null if the channel doesn't return any bytes.
     * @throws BallerinaIOException if an error is encountered when reading bytes from the channel.
     */
    private ByteBuffer getBytesFromChannel(int requiredNumberOfBytes, int offset, AbstractChannel channel) throws
            BallerinaIOException {
        int numberOfBytesRequiredFromChannel = requiredNumberOfBytes - offset;
        ByteBuffer srcBuffer = allocate(numberOfBytesRequiredFromChannel);
        channel.readFromChannel(srcBuffer);
        return compactBytes(srcBuffer);
    }

    /**
     * Specifies whether the buffer is empty.
     *
     * @param srcBuffer the buffer which needs to be validated.
     * @return true if the buffer is empty.
     */
    private boolean isBufferEmpty(ByteBuffer srcBuffer) {
        return srcBuffer.limit() == 0;
    }

    /**
     * <p>
     * Consolidates a given set of ByteBuffers into a single buffer instance.
     * </p>
     * <p>
     * <b>Note : </b>It should be ensured that all the buffers provided are in write-able mode (not flipped).
     * </p>
     *
     * @param size   the number of bytes which should be allocated for the buffer.
     * @param values the list of ByteBuffer instances which should be merged.
     * @return the consolidated ByteBuffer instance.
     */
    private ByteBuffer consolidate(int size, ByteBuffer... values) {
        ByteBuffer consolidatedByteBuffer = ByteBuffer.allocate(size);
        for (ByteBuffer value : values) {
            if (null != value && !isBufferEmpty(value)) {
                value.flip();
                consolidatedByteBuffer.put(value);
            }
        }
        return consolidatedByteBuffer;
    }

    /**
     * <p>
     * Resize the buffer for the specified size.
     * </p>
     *
     * @param srcBuffer buffer which requires resizing.
     * @param size      the number of bytes which should be in the buffer.
     * @return the buffer which is resized.
     */
    private ByteBuffer resizeIfRequired(ByteBuffer srcBuffer, int size) {
        ByteBuffer resizedBuffer;
        int capacity = srcBuffer.capacity();
        if (capacity < size) {
            size = capacity;
        }
        byte[] resizedContent = Arrays.copyOfRange(srcBuffer.array(), 0, size);
        resizedBuffer = ByteBuffer.wrap(resizedContent);
        byteBuffer.position(resizedContent.length);
        return resizedBuffer;
    }

    /**
     * <p>
     * Will reverse the position of the buffer.
     * </p>
     * <p>
     * This will reverse the un-processed bytes so that these bytes could be re-read.
     * </p>
     * <p>
     * Reverse will start from the current buffers read position and will go all the way to the position '0'.
     * </p>
     *
     * @param count the number of bytes which should be reversed.
     */
    public void reverse(int count) throws BallerinaIOException {
        if (null != byteBuffer) {
            int reversedByteBufferPosition = byteBuffer.position() - count;
            final int minimumBytePosition = 0;
            if (reversedByteBufferPosition >= minimumBytePosition) {
                byteBuffer.position(reversedByteBufferPosition);
            } else {
                String message = "The specified byte count " + count + " has not being read,hence cannot reverse";
                throw new BallerinaIOException(message);
            }
        } else {
            String message = "ByteBuffer not initialized, please initialize it before reversing";
            log.error(message);
            throw new BallerinaIOException(message);
        }
    }

    /**
     * <p>
     * Get readable byte buffer.
     * </p>
     * <p>
     * If the bytes are not available in the buffer, an attempt would be made to get from the channel, best effort
     * will be taken to return the required amount of bytes. If bytes are not available in both the buffer the
     * maximum available bytes will be returned.
     * </p>
     * <p>
     * If bytes have being read previously through the buffer and the requested amount of bytes exceed the capacity
     * of the current buffer. The buffer will be re-sized.
     * </p>
     * <p>
     * If no bytes are available an empty buffer with the required size will be returned.
     * </p>
     *
     * @param numberOfBytesRequested number of bytes requested from the buffer.
     * @param channel                byte channel which will perform I/O ops necessary for reading.
     * @return New byte buffer which contains the requested amount of bytes.
     */
    public ByteBuffer get(int numberOfBytesRequested, AbstractChannel channel) throws BallerinaIOException {
        ByteBuffer remainingBytesInBuffer = getBytesLeftInBuffer(numberOfBytesRequested);
        int numberOfBytesReadFromBuffer = remainingBytesInBuffer != null ? remainingBytesInBuffer.capacity() : 0;
        int numberOfBytesReadFromChannel;
        int consolidatedBufferSize;
        if (numberOfBytesReadFromBuffer == numberOfBytesRequested) {
            return remainingBytesInBuffer;
        }
        ByteBuffer bytesReadFromChannel = getBytesFromChannel(numberOfBytesRequested, numberOfBytesReadFromBuffer,
                channel);
        numberOfBytesReadFromChannel = bytesReadFromChannel.capacity();
        consolidatedBufferSize = numberOfBytesReadFromChannel + numberOfBytesReadFromBuffer;
        byteBuffer = consolidate(consolidatedBufferSize, remainingBytesInBuffer, bytesReadFromChannel);
        return resizeIfRequired(byteBuffer, numberOfBytesRequested);
    }

}
