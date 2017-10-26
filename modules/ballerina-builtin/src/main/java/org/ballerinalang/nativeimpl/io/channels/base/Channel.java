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
public abstract class Channel extends AbstractChannel {

    /**
     * Holds the content belonging to a particular channel.
     */
    private Buffer contentBuffer;

    private static final Logger log = LoggerFactory.getLogger(Channel.class);

    public Channel(ByteChannel channel, int size) throws BallerinaIOException {
        super(channel);
        contentBuffer = new Buffer(size);
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
    public byte[] read(int numberOfBytes) throws BallerinaIOException {
        ByteBuffer readBuffer = contentBuffer.get(numberOfBytes, this);
        return readBuffer.array();
    }

    /**
     * Writes bytes to channel.
     *
     * @param content     the data which will be written.
     * @param startOffset if the data should be written from an offset (starting byte position).
     * @return the number of bytes written.
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
