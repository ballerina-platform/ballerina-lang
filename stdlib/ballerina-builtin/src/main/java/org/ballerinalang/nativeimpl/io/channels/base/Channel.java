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
import org.ballerinalang.nativeimpl.io.channels.base.readers.Reader;
import org.ballerinalang.nativeimpl.io.channels.base.writers.Writer;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public abstract class Channel {
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
     * <p>
     * Specifies the maximum number of bytes which will be read in each chunk.
     * </p>
     * <p>
     * This value will be used when reading all the bytes from the channel.
     * </p>
     *
     * @see Channel#readAll()
     */
    private static final int MAX_BUFFER_CHUNK_SIZE = 1024;


    private static final Logger log = LoggerFactory.getLogger(Channel.class);

    /**
     * Creates a ballerina channel which will source/sink from I/O resource.
     *
     * @param channel the channel to source/sink bytes.
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
            String message = "The provided information is incorrect, the specified channel ";
            throw new BallerinaIOException(message);
        }
    }

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
            String message = "The provided information is incorrect, the specified channel ";
            throw new BallerinaIOException(message);
        }
    }

    /**
     * Will be used when performing direct transfer operations from OS cache.
     *
     * @param position   starting position of the bytes to be transferred.
     * @param count      number of bytes to be transferred.
     * @param dstChannel destination channel to transfer.
     * @throws BallerinaIOException during I/O error.
     */
    public abstract void transfer(int position, int count, WritableByteChannel dstChannel) throws BallerinaIOException;

    /**
     * <p>
     * Async read bytes from the channel.
     * </p>
     *
     * @param buffer the buffer which will hold the content.
     * @return the number of bytes read.
     */
     public int read(ByteBuffer buffer) throws BallerinaIOException {
        int readBytes = reader.read(buffer, channel);
        if (readBytes <= 0) {
            hasReachedToEnd = true;
        }
        return readBytes;
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
     * @throws BallerinaIOException during I/O error.
     */
    @Deprecated
    public byte[] readFull(int numberOfBytes) throws BallerinaIOException {
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

    /**
     * Specifies whether the channel has reached to it's end.
     *
     * @return true if the channel has reached to it's end
     */
    boolean hasReachedEnd() {
        return hasReachedToEnd;
    }

    /**
     * <p>
     * Writes provided buffer content to the channel.
     * </p>
     *
     * @param content the buffer which holds the content.
     * @return the number of bytes written to the channel.
     * @throws BallerinaIOException during I/O error.
     */
    public int write(ByteBuffer content) throws BallerinaIOException {
        return writer.write(content, channel);
    }

    /**
     * Writes bytes to channel.
     *
     * @param content     the data which will be written.
     * @param startOffset if the data should be written from an offset (starting byte position).
     * @return the number of bytes written.
     */
    @Deprecated
    public int write(byte[] content, int startOffset) throws BallerinaException {
        ByteBuffer outputBuffer = ByteBuffer.wrap(content);
        //If a larger position is set, the position would be disregarded
        outputBuffer.position(startOffset);
        if (log.isDebugEnabled()) {
            log.debug("Writing " + content.length + " for the buffer with offset " + startOffset);
        }
        return write(outputBuffer);
    }

    /**
     * Closes the given channel.
     */
    public void close() {
        try {
            if (null != channel) {
                channel.close();
            } else {
                log.error("The channel has already being closed.");
            }
        } catch (IOException e) {
            String message = "Error occurred while closing the connection. ";
            throw new BallerinaIOException(message, e);
        }
    }

    /**
     * This will return {@link InputStream} from underlying {@link ByteChannel}.
     *
     * @return An {@link InputStream}
     */
    public InputStream getInputStream() {
        if (!channel.isOpen()) {
            String message = "Channel is already closed.";
            throw new BallerinaIOException(message);
        }
        return Channels.newInputStream(channel);
    }

    /**
     * <p>
     * Merge the list of buffers together into one.
     * </p>
     * <p>
     * <b>Note : </b> This will be used to consolidate buffers read in multiple iterations into one.
     * </p>
     *
     * @param bufferList the list which contains all bytes read through the buffer.
     * @param size       the size of all the bytes read.
     * @return the response buffer which contains all consolidated bytes.
     */
    private byte[] merge(List<ByteBuffer> bufferList, int size) {
        ByteBuffer consolidatedBuffer = ByteBuffer.allocate(size);
        for (ByteBuffer buffer : bufferList) {
            consolidatedBuffer.put(buffer);
        }
        return consolidatedBuffer.array();
    }

    /**
     * Reads all bytes from the I/O source.
     *
     * @return all the bytes read.
     * @throws BallerinaException during I/O error.
     */
    @Deprecated
    public byte[] readAll() throws BallerinaException {
        List<ByteBuffer> readBufferList = new ArrayList<>();
        int totalNumberOfBytes = 0;
        boolean hasRemaining = false;
        do {
            ByteBuffer readBuffer = contentBuffer.get(MAX_BUFFER_CHUNK_SIZE, this);
            int numberOfBytesRead = readBuffer.limit();
            if (numberOfBytesRead > 0) {
                readBufferList.add(readBuffer);
                totalNumberOfBytes = totalNumberOfBytes + numberOfBytesRead;
            } else {
                hasRemaining = true;
            }
        } while (!hasRemaining);
        return merge(readBufferList, totalNumberOfBytes);
    }

}
