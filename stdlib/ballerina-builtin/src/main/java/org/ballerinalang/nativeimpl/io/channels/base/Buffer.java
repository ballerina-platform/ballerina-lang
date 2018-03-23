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

import java.io.IOException;
import java.nio.ByteBuffer;

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
    private ByteBuffer remainingContent(int totalNumberOfBytesRequired) {
        ByteBuffer remainingContent = null;
        if (null != byteBuffer) {
            remainingContent = byteBuffer.slice();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("ByteBuffer has not being initialized, buffer will be initialized while reading the " +
                        "requested amount of " + totalNumberOfBytesRequired + " of bytes");
            }
        }
        return remainingContent;
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
     * Resize the buffer for the specified size.
     * </p>
     * <p>
     * If a larger buffer was selected
     * </p>
     *
     * @param srcBuffer             buffer which requires resizing.
     * @param requiredNumberOfBytes the number of bytes which should be in the buffer.
     * @return the buffer which is resized.
     */
    private ByteBuffer resize(ByteBuffer srcBuffer, int requiredNumberOfBytes) {
        srcBuffer.flip();
        int totalBytesRead = srcBuffer.limit();
        if (totalBytesRead > requiredNumberOfBytes) {
            //If the size exceeds the required amount we need to narrow it down
            srcBuffer.limit(requiredNumberOfBytes);
        }
        ByteBuffer resizedBuffer = srcBuffer.slice();
        //After slicing we rest the limit
        srcBuffer.limit(totalBytesRead);
        //We need to move the cursor forward of the main buffer and mark it as read
        srcBuffer.position(resizedBuffer.capacity());
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
     * Deep copies a byte buffer with it's underlying array.
     * </p>
     *
     * @param srcBuffer the source buffer which should be copied.
     * @return the buffer which is duplicated.
     */
    private ByteBuffer deepCopy(ByteBuffer srcBuffer) {
        ByteBuffer dstBuffer = ByteBuffer.allocate(srcBuffer.capacity());
        srcBuffer.rewind();
        dstBuffer.put(srcBuffer);
        srcBuffer.rewind();
        dstBuffer.flip();
        return dstBuffer;
    }

    /**
     * <p>
     * Retrieves the required content from the existing buffer.
     * </p>
     * <p>
     * In this case it will not be required to do another channel call.
     * </p>
     *
     * @param numberOfBytesRequested number of bytes required.
     * @param content                buffer which holds the content.
     * @return buffer which contains the required amount of bytes.
     */
    private ByteBuffer copyRemainingContent(int numberOfBytesRequested, ByteBuffer content) {
        //If there is excess bytes we need only a sub-set of them
        int capacity;
        content.limit(numberOfBytesRequested);
        ByteBuffer slicedBuffer = content.slice();
        capacity = slicedBuffer.capacity();
        int offset = slicedBuffer.arrayOffset();
        byteBuffer.position(offset + capacity);
        return deepCopy(slicedBuffer);
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
     * The operation will return slice of the bytes requested. The size of the buffer returned would be =
     * numberOfBytesRequested. If numberOfBytesRequested &lt; minimumSizeOfBuffer the size of the buffer will be
     * minimumSizeOfBuffer
     * </p>
     *
     * @param numberOfBytesRequested number of bytes requested from the buffer.
     * @param channel                byte channel which will perform I/O ops necessary for reading.
     * @return buffer which will contain bytes &gt;= numberOfBytesRequested
     * @throws IOException errors which occur while reading from the channel.
     */
    public ByteBuffer get(int numberOfBytesRequested, Channel channel) throws IOException {
        ByteBuffer remainingContent = remainingContent(numberOfBytesRequested);
        if (null != remainingContent && remainingContent.capacity() >= numberOfBytesRequested) {
            return copyRemainingContent(numberOfBytesRequested, remainingContent);
        } else {
            if (null != remainingContent && remainingContent.hasRemaining()) {
                remainingContent = deepCopy(remainingContent);
            }
            if (byteBuffer != null && byteBuffer.capacity() >= numberOfBytesRequested) {
                //If the required amount of bytes > than the current buffer size we enlarge the buffer
                byteBuffer.clear();
            } else {
                //In this case we re-allocate
                byteBuffer = allocate(numberOfBytesRequested);
            }
            if (null != remainingContent && remainingContent.hasRemaining()) {
                byteBuffer.put(remainingContent);
            }
            channel.read(byteBuffer);
            return resize(byteBuffer, numberOfBytesRequested);
        }
    }

}
