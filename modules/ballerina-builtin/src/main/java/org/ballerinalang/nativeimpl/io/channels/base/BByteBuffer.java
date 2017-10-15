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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * <p>
 * Specialized ByteBuffer which will dynamically resize, maintain state and call the underlying ByteChannel as
 * necessary to return the required amount of bytes
 * </p>
 * <p>
 * BByteBuffer also focuses on offering the capability to read bytes through multiple channels and place into one
 * </p>
 */
public class BByteBuffer {

    /**
     * Holds the bytes read from the byte channel
     */
    private ByteBuffer byteBuffer;

    private static final Logger log = LoggerFactory.getLogger(BByteChannel.class);

    /**
     * <p>
     * Returns a ByteBuffer which will contain any remaining bytes left
     * </p>
     * <p>
     * This operation will not guarantee a return of all the bytes required. This will return bytes if any is
     * available in the buffer.
     * </p>
     * <p>
     * The operation will return null if none of the bytes are remaining in the buffer
     * </p>
     *
     * @param requiredNumberOfBytes number of bytes required
     * @return new ByteBuffer which will contain bytes which will be remaining
     */
    private ByteBuffer getRemainingBytesFromBuffer(int requiredNumberOfBytes) {

        ByteBuffer bufferedContent = null;

        if (null != byteBuffer) {
            int position = byteBuffer.position();
            int limit = byteBuffer.limit();
            int remainingBytesToBeRead = limit - position;

            if (remainingBytesToBeRead == 0) {
                //Given there's nothing to read from the buffer
                return null;
            }

            if (remainingBytesToBeRead > requiredNumberOfBytes) {
                //This means there's excess amount of bytes than requested, hence we narrow down the limit
                limit = requiredNumberOfBytes;
            }

            byte[] content = Arrays.copyOfRange(byteBuffer.array(), position, limit);

            bufferedContent = ByteBuffer.wrap(content);
            //We need to mark the bytes wrapped as read
            bufferedContent.position(content.length);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("ByteBuffer has not being initialized, buffer will be initialized while reading the " +
                        "requested amount of " + requiredNumberOfBytes + " of bytes");
            }
        }

        return bufferedContent;
    }

    /**
     * <p>
     * Reads the specified number of bytes through ByteChannel
     * </p>
     * <p>
     * The operation will not guarantee all the bytes could be read. It will ensure to read the maximum number of
     * bytes from what's requested. If an offset is specified the amount of bytes will be reduced
     * (requiredNumberOfBytes - offset)
     * </p>
     * <p>
     * If the channel doesn't return any bytes it will return null
     * </p>
     *
     * @param requiredNumberOfBytes the number of bytes required to be read from the channel
     * @param offset                the number of bytes already present
     * @return ByteBuffer which wraps the bytes read, null if the channel doesn't return any bytes
     * @throws IOException if an error is encountered when reading bytes from the channel
     */
    private ByteBuffer getRemainingBytesFromChannel(int requiredNumberOfBytes, int offset, BByteChannel channel) throws
            IOException {
        int numberOfBytesRequiredFromChannel = requiredNumberOfBytes - offset;
        return channel.getReadBuffer(numberOfBytesRequiredFromChannel);
    }

    /**
     * <p>
     * Consolidates a given set of ByteBuffers into a single buffer instance
     * </p>
     * <p>
     * <b>Note : </b>It should be ensured that all the buffers provided are in write-able mode (not flipped)
     * </p>
     *
     * @param size   the number of bytes which should be allocated for the buffer
     * @param values the list of ByteBuffer instances which should be merged
     * @return the consolidated ByteBuffer instance
     */
    private ByteBuffer consolidate(int size, ByteBuffer... values) {
        ByteBuffer consolidatedByteBuffer = ByteBuffer.allocate(size);
        for (ByteBuffer value : values) {
            if (null != value) {
                value.flip();
                consolidatedByteBuffer.put(value);
            }
        }
        //Make the buffer readable
        consolidatedByteBuffer.flip();
        return consolidatedByteBuffer;
    }

    /**
     * <p>
     * Will reverse the read position of the buffer
     * </p>
     * <p>
     * This could be called if the amount of bytes which were read have not being consumed properly
     * </p>
     *
     * @param count the number of bytes which should be reversed
     */
    void reverse(int count) {
        if (null != byteBuffer) {
            int reversedByteBufferPosition = byteBuffer.position() - count;
            byteBuffer.position(reversedByteBufferPosition);
        } else {
            log.error("An unread ByteBuffer is attempted for reverse, please read from buffer first");
        }
    }

    /**
     * <p>
     * Reads the bytes from the buffer
     * </p>
     * <p>
     * If the bytes are not available in the buffer, an attempt would be made to read from the channel, best effort
     * will be taken to return the required amount of bytes. If bytes are not available in both the buffer and the
     * the maximum available bytes will be returned.
     * </p>
     * <p>
     * If bytes have being read previously through the buffer and the requested amount of bytes exceed the capacity
     * of the current buffer. The buffer will be re-sized.
     * </p>
     * <p>
     * If no bytes are available an empty buffer with the required size will be returned
     * </p>
     *
     * @param numberOfBytesRequested number of bytes requested from the buffer
     * @param channel                bytes channel which will perform I/O ops necessary for reading
     * @return Buffer which contains the requested amount of bytes
     */
    public ByteBuffer read(int numberOfBytesRequested, BByteChannel channel) throws IOException {
        ByteBuffer remainingBytesInBuffer = getRemainingBytesFromBuffer(numberOfBytesRequested);
        int numberOfBytesReadFromBuffer = remainingBytesInBuffer != null ? remainingBytesInBuffer.capacity() : 0;
        int numberOfBytesReadFromChannel;
        int consolidatedBufferSize;

        if (numberOfBytesReadFromBuffer == numberOfBytesRequested) {
            return remainingBytesInBuffer;
        }

        ByteBuffer bytesReadFromChannel = getRemainingBytesFromChannel(numberOfBytesRequested,
                numberOfBytesReadFromBuffer, channel);

        numberOfBytesReadFromChannel = bytesReadFromChannel != null ? bytesReadFromChannel.capacity() : 0;
        consolidatedBufferSize = numberOfBytesReadFromChannel + numberOfBytesReadFromBuffer;
        byteBuffer = consolidate(consolidatedBufferSize, remainingBytesInBuffer, bytesReadFromChannel);

        return byteBuffer;
    }

}
