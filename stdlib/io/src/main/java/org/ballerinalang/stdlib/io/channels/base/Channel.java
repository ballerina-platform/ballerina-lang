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

package org.ballerinalang.stdlib.io.channels.base;

import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * <p>
 * Represents Ballerina Byte Channel.
 * </p>
 * <p>
 * Allows reading/writing bytes to perform I/O operations.
 * </p>
 * <p>
 * The bytes are read through the channel into the buffer. This will manage the buffer and the channel to deliver
 * bytes I/O reading/writing APIs.
 * </p>
 */
public abstract class Channel implements IOChannel {
    /**
     * Will be used to read/write bytes to/from channels.
     */
    private ByteChannel byteChannel;

    /**
     * Specifies whether the channel has reached EoF.
     */
    private boolean hasReachedToEnd = false;

    /**
     * Specifies whether the channel is readable.
     */
    private boolean readable;

    private static final Logger log = LoggerFactory.getLogger(Channel.class);

    /**
     * <p>
     * Will read/write bytes from the provided channel
     * </p>
     * <p>
     * This operation will asynchronously read data from the channel.
     * </p>
     *
     * @param byteChannel which will be used to read/write content.
     * @throws BallerinaIOException initialization error.
     */
    public Channel(ByteChannel byteChannel) throws BallerinaIOException {
        if (null != byteChannel) {
            this.byteChannel = byteChannel;
            if (log.isDebugEnabled()) {
                log.debug(String.format("Initializing ByteChannel with ref id %d", byteChannel.hashCode()));
            }
        } else {
            String message = "Provided channel cannot be initialized";
            throw new BallerinaIOException(message);
        }
    }

    /**
     * Will be used when performing direct transfer operations from OS cache.
     *
     * @param position   starting position of the bytes to be transferred.
     * @param count      number of bytes to be transferred.
     * @param dstChannel destination channel to transfer.
     * @throws IOException during I/O error.
     */
    public abstract void transfer(int position, int count, WritableByteChannel dstChannel) throws IOException;

    /**
     * Returns the hashcode of the channel as the id.
     *
     * @return id of the channel.
     */
    @Override
    public int id() {
        return byteChannel.hashCode();
    }

    /**
     * Specifies whether the channel has reached to it's end.
     *
     * @return true if the channel has reached to it's end
     */
    @Override
    public boolean hasReachedEnd() {
        return hasReachedToEnd;
    }

    /**
     * This will return {@link ByteChannel} instance that use underneath.
     *
     * @return {@link ByteChannel} instance.
     */
    public ByteChannel getByteChannel() {
        return byteChannel;
    }

    /**
     * <p>
     * Async read bytes from the channel.
     * </p>
     *
     * @param buffer the buffer which will hold the content.
     * @return the number of bytes read.
     * @throws IOException errors occur during reading from channel.
     */
    public int read(ByteBuffer buffer) throws IOException {
        int readBytes = byteChannel.read(buffer);
        if (readBytes < 0) {
            //Since we're counting the bytes if a value < 0 is returned, this will be re-set
            readBytes = 0;
            hasReachedToEnd = true;
        }
        return readBytes;
    }

    /**
     * <p>
     * Writes provided buffer content to the channel.
     * </p>
     *
     * @param content the buffer which holds the content.
     * @return the number of bytes written to the channel.
     * @throws IOException errors occur during writing data to channel.
     */
    public int write(ByteBuffer content) throws IOException {
        return byteChannel.write(content);
    }

    /**
     * This will return {@link InputStream} from underlying {@link ByteChannel}.
     *
     * @return An {@link InputStream}
     * @throws BallerinaIOException error occur during obtaining input-stream.
     */
    public InputStream getInputStream() throws BallerinaIOException {
        if (!byteChannel.isOpen()) {
            String message = "Channel is already closed.";
            throw new BallerinaIOException(message);
        }
        return Channels.newInputStream(byteChannel);
    }

    public void setReadable(boolean readable) {
        this.readable = readable;
    }

    public boolean isReadable() {
        return readable;
    }

    /**
     * Closes the given channel.
     *
     * @throws IOException errors occur while closing the connection.
     */
    @Override
    public void close() throws IOException {
        if (null != byteChannel) {
            byteChannel.close();
        } else {
            log.error("The channel has already being closed");
        }
    }

}
