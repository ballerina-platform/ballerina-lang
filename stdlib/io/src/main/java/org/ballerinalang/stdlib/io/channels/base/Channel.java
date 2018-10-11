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

import org.ballerinalang.stdlib.io.channels.base.readers.Reader;
import org.ballerinalang.stdlib.io.channels.base.writers.Writer;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

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
    private ByteChannel channel;

    /**
     * Specifies how the content should be read from the channel.
     */
    private Reader reader;

    /**
     * Specifies how the content should be written to a channel.
     */
    private Writer writer;

    /**
     * Specifies whether the channel has reached EoF.
     */
    private boolean hasReachedToEnd = false;

    /**
     * Holds the content belonging to a particular channel.
     */
    private Buffer contentBuffer;

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
     * @param channel which will be used to read/write content.
     * @param reader  will be used for reading content.
     * @param writer  will be used for writing content.
     * @throws BallerinaIOException initialization error.
     */
    public Channel(ByteChannel channel, Reader reader, Writer writer) throws BallerinaIOException {
        if (null != channel) {
            this.channel = channel;
            this.reader = reader;
            this.writer = writer;
            if (log.isDebugEnabled()) {
                log.debug("Initializing ByteChannel with ref id " + channel.hashCode());
            }
        } else {
            String message = "Provided channel cannot be initialized";
            throw new BallerinaIOException(message);
        }
    }

    /**
     * <p>
     * Creates a channel which will contain a fixed sized buffer.
     * </p>
     * <p>
     * This operation will in turn request for excess bytes than required. And buffer the content. Also this will
     * mostly be used with blocking readers and writers.
     * </p>
     *
     * @param channel which will be used to read/write content.
     * @param reader  will be used for reading content.
     * @param writer  will be used for writing content.
     * @param size    the size of the fixed buffer.
     * @throws BallerinaIOException initialization error.
     */
    @Deprecated
    public Channel(ByteChannel channel, Reader reader, Writer writer, int size) throws BallerinaIOException {
        if (null != channel) {
            this.channel = channel;
            this.reader = reader;
            this.writer = writer;
            contentBuffer = new Buffer(size);
            if (log.isDebugEnabled()) {
                log.debug("Initializing ByteChannel with ref id " + channel.hashCode());
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
     * Specifies whether the channel is selectable.
     *
     * @return true if the channel is selectable.
     */
    public abstract boolean isSelectable();

    /**
     * Returns the hashcode of the channel as the id.
     *
     * @return id of the channel.
     */
    @Override
    public int id() {
        return channel.hashCode();
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
        return channel;
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
        int readBytes = reader.read(buffer, channel);
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
        return writer.write(content, channel);
    }

    /**
     * This will return {@link InputStream} from underlying {@link ByteChannel}.
     *
     * @return An {@link InputStream}
     * @throws BallerinaIOException error occur during obtaining input-stream.
     */
    public InputStream getInputStream() throws BallerinaIOException {
        if (!channel.isOpen()) {
            String message = "Channel is already closed.";
            throw new BallerinaIOException(message);
        }
        return Channels.newInputStream(channel);
    }

    /**
     * <p>
     * Reads specified amount of bytes from a given channel.
     * <p>
     * Each time this method is called the data will be retrieved from the channels last read position.
     * </P>
     * <p>
     * <b>Note : </b> This operation cannot be called in parallel invocations since the underlying ByteBuffer and the
     * channel are not synchronous.
     * </P>
     *
     * @param numberOfBytes the number of bytes required.
     * @return bytes which are retrieved from the channel.
     * @throws IOException during I/O error.
     */
    public byte[] readFull(int numberOfBytes) throws IOException {
        ByteBuffer readBuffer = contentBuffer.get(numberOfBytes, this);
        byte[] content = readBuffer.array();
        int contentLength = readBuffer.capacity();
        if (content.length > numberOfBytes || contentLength < numberOfBytes) {
            //We need to collect the bytes only belonging to the offset, since the number of bytes returned can be < the
            //required amount of bytes
            content = Arrays.copyOfRange(content, 0, contentLength);
        }
        return content;
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
        try {
            if (null != channel) {
                channel.close();
            } else {
                log.error("The channel has already being closed");
            }
        } catch (IOException e) {
            String message = "Error occurred while closing the connection";
            log.error(message, e);
            throw new IOException(message, e);
        }
    }

}
