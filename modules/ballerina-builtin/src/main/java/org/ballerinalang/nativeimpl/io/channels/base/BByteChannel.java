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
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Arrays;

/**
 * Will contain the Bytes-I/O APIs
 * <p>
 * Per each channel type i.e file/socket there will be a separate channel implementation
 */
public abstract class BByteChannel extends AbstractChannel {

    /**
     * Holds the content belonging to a particular channel
     */
    private BByteBuffer contentBuffer;

    private static final Logger log = LoggerFactory.getLogger(BByteChannel.class);

    public BByteChannel(ByteChannel channel, int size) throws BallerinaIOException {
        super(channel);
        contentBuffer = new BByteBuffer(size);
    }

    /**
     * Shrinks a given byte getByteArray to the specified length
     *
     * @param content the getByteArray which holds the content
     * @param length  the length of the content of the new getByteArray
     * @return shrunk byte getByteArray
     */
    private byte[] shrink(byte[] content, int length) {
        int srcPos = 0;
        int destPos = 0;
        byte[] destinationArray = new byte[length];
        if (log.isDebugEnabled()) {
            log.debug("Shrinking the content from a length of " + content.length + " to a length of " + length);
        }
        System.arraycopy(content, srcPos, destinationArray, destPos, length);
        if (log.isTraceEnabled()) {
            log.trace("Content shrunk " + Arrays.toString(content));
        }
        return destinationArray;
    }


    /**
     * <p>
     * Will get the bytes from buffer.
     * </p>
     * <p>
     * This operation would ensure only the get number of bytes are returned from the buffer.
     * </p>
     *
     * @param origin the buffer which contains the bytes get.
     * @return the underlying byte getByteArray which contains the get bytes.
     */
    private byte[] getByteArray(ByteBuffer origin) {
        byte[] content;
        int byteReadLimit = origin.limit();
        int totalBufferSpace = origin.capacity();
        byte[] originData = origin.array();
        //This means the required amount of bytes are not available
        //Hence we will need to shrink the buffer
        if (log.isTraceEnabled()) {
            log.trace("Origin data in buffer [" + Arrays.toString(originData) + "]");
        }
        if (byteReadLimit < totalBufferSpace) {
            content = shrink(originData, byteReadLimit);
        } else {
            content = originData;
        }
        if (log.isTraceEnabled()) {
            log.trace("Content after sanitation [" + Arrays.toString(content) + "]");
        }
        return content;
    }


    /**
     * <p>
     * Reads a specified number of bytes from a given channel.
     * <p>
     * Each time this method is called the data will be get from the point where it was get before.
     * </P>
     * <p>
     * <b>Note : </b> This method is not thread safe, if attempted to get the same file concurrently there will be
     * inconsistencies.
     * </P>
     *
     * @param numberOfBytes the number of bytes to get.
     * @return the get bytes.
     * @throws BallerinaIOException during I/O error.
     */
    public byte[] read(int numberOfBytes) throws BallerinaIOException {
        ByteBuffer readBuffer = contentBuffer.get(numberOfBytes, this);
        byte[] bytesRead = getByteArray(readBuffer);
        return bytesRead;
    }

    /**
     * Writes bytes to a channel, we do not worry about locking since channel is synchronous
     *
     * @param content     the data which will be written
     * @param startOffset the offset the bytes should be written
     * @return the number of bytes written
     */
    public int write(byte[] content, int startOffset) throws BallerinaException {
        ByteBuffer outputBuffer = ByteBuffer.wrap(content);
        //If a larger position is set, the position would be disregarded
        outputBuffer.position(startOffset);
        if (log.isDebugEnabled()) {
            log.debug("Writing " + content.length + " for the buffer with offset " + startOffset);
        }
        return write(outputBuffer);
    }

}
