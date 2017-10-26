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
import java.nio.channels.ByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * <p>
 * Represents the channel which allows reading/writing bytes to I/O devices.
 * </p>
 * <p>
 * Any channel implementation could inherit it's base methods from this.
 * </p>
 * <p>
 * <b>Note : </b> this channel does not support concurrent reading/writing, hence this should not be accessed
 * concurrently.
 * </p>
 *
 * @see Channel
 * @see CharacterChannel
 * @see TextRecordChannel
 */
public abstract class AbstractChannel {

    /**
     * Will be used to read/write bytes to/from channels.
     */
    private ByteChannel channel;

    /**
     * Specifies whether the channel has reached EoF.
     */
    private boolean hasReachedToEnd = false;

    private static final Logger log = LoggerFactory.getLogger(AbstractChannel.class);

    /**
     * Creates a ballerina channel which will source/sink from I/O resource.
     *
     * @param channel the channel to source/sink bytes.
     */
    public AbstractChannel(ByteChannel channel) throws BallerinaIOException {
        if (null != channel) {
            this.channel = channel;
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
     * Reads data from the channel until the given buffer is filled.
     * </p>
     *
     * @param inputBuffer the source to read bytes from.
     */
    void readFromChannel(ByteBuffer inputBuffer) throws BallerinaIOException {
        try {
            if (!hasReachedToEnd) {
                if (log.isDebugEnabled()) {
                    log.debug("Reading from channel " + channel.hashCode());
                }
                //We will need to allocate a buffer per each call, reusing this will not be an option
                // a - the size of the buffer would very based on the requested number of bytes
                // b - the bytes getByteArray should be re-used
                int numberOfReadBytes = 0;
                int channelEndOfStreamFlag = -1;
                //Fills the inputBuffer with the
                while (inputBuffer.hasRemaining() && numberOfReadBytes > channelEndOfStreamFlag) {
                    numberOfReadBytes = channel.read(inputBuffer);
                    if (log.isDebugEnabled()) {
                        log.debug("Read from channel " + numberOfReadBytes + " from " + channel.hashCode());
                    }
                }
                //If the EoF has reached we do not want to get anymore
                if (numberOfReadBytes == channelEndOfStreamFlag) {
                    if (log.isDebugEnabled()) {
                        log.debug("The channel " + channel.hashCode() + " reached EoF");
                    }
                    hasReachedToEnd = true;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("The channel " + channel.hashCode() + " reached EoF hence  will not be get from the " +
                            "channel");
                }
            }
        } catch (IOException e) {
            String message = "Error occurred while reading from channel ";
            throw new BallerinaIOException(message, e);
        }
    }

    /**
     * <p>
     * Writes provided buffer content to the channel.
     * </p>
     *
     * @param contentBuffer the buffer which holds the content.
     * @return the number of bytes written to the channel.
     * @throws BallerinaIOException during I/O error.
     */
    int write(ByteBuffer contentBuffer) throws BallerinaIOException {
        int numberOfBytesWritten;
        try {
            //There's no guarantee in a single write all the bytes will be written
            //Hence we need to check iteratively whether all the bytes in the buffer were written until the EoF is
            // reached
            while (contentBuffer.hasRemaining()) {
                int write = channel.write(contentBuffer);
                if (log.isTraceEnabled()) {
                    log.trace("Number of bytes " + write + " written to channel " + channel.hashCode());
                }
            }
            numberOfBytesWritten = contentBuffer.position();
            if (log.isDebugEnabled()) {
                log.debug("Number of bytes " + numberOfBytesWritten + " to the channel " + channel.hashCode());
            }
        } catch (IOException e) {
            String message = "Error occurred while writing to channel ";
            throw new BallerinaIOException(message, e);
        }
        return numberOfBytesWritten;
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
}
