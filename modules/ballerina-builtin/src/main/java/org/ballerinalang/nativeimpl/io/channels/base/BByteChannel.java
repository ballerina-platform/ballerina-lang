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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

/**
 * <p>
 * Represents the bytes channel, Note this class is not thread safe so there cannot be multiple reads or writes
 * </p>
 * <p>
 * This is a stateful channel
 * </p>
 */
public abstract class BByteChannel {

    /**
     * Represents the channel for reading bytes
     */
    private ByteChannel channel;
    /**
     * Whether the channel has reached to it's end
     */
    private boolean hasReachedToEnd = false;
    /**
     * Specifies a fixed buffer size, if multiple reads are done frequently this is recommended
     */
    private int fixedBufferSize = -1;

    /**
     * Maximum buffer size in bytes (64kb) this will be the maximum size allowed when defining a fixed buffer
     */
    private static final int MAX_BUFFER_SIZE = 78905344;

    /**
     * Minimum buffer size in bytes
     */
    private static final int MINIMUM_BUFFER_SIZE = 0;

    /**
     * If a fixed buffer is used for reading this will manage the state
     */
    private ByteBuffer fixedBuffer;


    private static final Logger log = LoggerFactory.getLogger(BByteChannel.class);

    /**
     * Creates a ballerina channel which will source/sink from I/O resource
     *
     * @param channel the channel which will source/sink
     */
    public BByteChannel(ByteChannel channel) throws BallerinaIOException {
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
     * Creates a channel with a fixed size
     *
     * @param channel         the channel to perform I/O operations
     * @param fixedBufferSize the size of the fixed buffer
     * @throws BallerinaIOException during I/O error
     */
    public BByteChannel(ByteChannel channel, int fixedBufferSize) throws BallerinaIOException {
        if (null != channel && (fixedBufferSize > MINIMUM_BUFFER_SIZE && fixedBufferSize < MAX_BUFFER_SIZE)) {
            this.channel = channel;
            this.fixedBufferSize = fixedBufferSize;
            this.fixedBuffer = ByteBuffer.allocate(fixedBufferSize);
            if (log.isDebugEnabled()) {
                log.debug("Fixed byte buffer is allocated for channel " + channel.hashCode() + " with a size of " +
                        fixedBufferSize);
            }
        } else {
            String message = "The specified channel " + channel + " is incorrect or the size " + fixedBufferSize;
            throw new BallerinaIOException(message);
        }
    }


    /**
     * Will be used when performing direct transfer operations from OS cache
     *
     * @param position   starting position of the bytes to be transferred
     * @param count      number of bytes to be transferred
     * @param dstChannel destination channel to transfer
     * @throws BallerinaIOException during I/O error
     */
    public abstract void transfer(int position, int count, WritableByteChannel dstChannel) throws BallerinaIOException;

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
     * Will get the bytes from buffer
     * </p>
     * <p>
     * This operation would ensure only the read number of bytes are returned from the buffer
     * </p>
     *
     * @param origin the buffer which contains the bytes read
     * @return the underlying byte getByteArray which contains the read bytes
     */
    private byte[] getByteArray(ByteBuffer origin) {
        byte[] content;
        int readBytePosition = origin.position();
        int totalBufferSpace = origin.capacity();
        byte[] originData = origin.array();
        //This means the required amount of bytes are not available
        //Hence we will need to shrink the buffer
        if (log.isTraceEnabled()) {
            log.trace("Origin data in buffer [" + Arrays.toString(originData) + "] for channel " + channel.hashCode());
        }
        if (readBytePosition < totalBufferSpace) {
            content = shrink(originData, readBytePosition);
        } else {
            content = originData;
        }
        if (log.isTraceEnabled()) {
            log.trace("Content after sanitation [" + Arrays.toString(content) + "] for channel " + channel.hashCode());
        }
        return content;
    }

    /**
     * Reads the content from a byte buffer which has fixed length
     *
     * @param numberOfBytes number of bytes required to be read from the channel
     * @return the number of bytes read
     * @throws BallerinaIOException during I/O error
     */
    private ByteBuffer readFromFixedBuffer(int numberOfBytes) throws BallerinaIOException {
        //We need to allocate a new buffer to produce as the response
        ByteBuffer responseBuffer = ByteBuffer.allocate(numberOfBytes);
        int remainingBytes = fixedBuffer.remaining();
        //We dump the remaining bytes to the response
        if (remainingBytes >= numberOfBytes) {
            if (log.isDebugEnabled()) {
                log.debug("The required number of bytes " + numberOfBytes + " is available in fixed buffer");
            }
            //This means the requested amount of bytes are available in the fixed buffer
            byte[] remainingContent = getBytes(numberOfBytes);
            responseBuffer.put(remainingContent, 0, remainingContent.length);
        } else {
            //We need a refill the buffer with the existing content
            byte[] remainingContent = getBytes(remainingBytes);
            if (log.isDebugEnabled()) {
                log.debug("Adding the remaining byte count of " + remainingBytes + " to response buffer");
            }
            responseBuffer.put(remainingContent, 0, remainingContent.length);
            //Let's request for a re-fill of the remaining
            int numberOfRemainingBytesToBeRead = numberOfBytes - remainingBytes;

            if (log.isDebugEnabled()) {
                log.debug("Reading " + numberOfRemainingBytesToBeRead + " from channel " + channel.hashCode());
            }
            int blockCount = (int) Math.ceil((float) numberOfRemainingBytesToBeRead / (float) fixedBufferSize);
            readBlocksFromChannel(responseBuffer, numberOfRemainingBytesToBeRead, blockCount);
        }
        return responseBuffer;
    }

    /**
     * Reads content from the channel as blocks
     *
     * @param responseBuffer                 buffer which will holds the final content which will be the response
     * @param numberOfRemainingBytesToBeRead number of bytes which should be read
     * @param blockCount                     the number of iterations to read all the bytes required
     * @throws BallerinaIOException I/O error when reading the content
     */
    private void readBlocksFromChannel(ByteBuffer responseBuffer, int numberOfRemainingBytesToBeRead, int blockCount)
            throws BallerinaIOException {
        final int readBlockIndex = 0;
        final int finalBlockIndex = blockCount - 1;
        for (int block = readBlockIndex; block < blockCount; block++) {
            //We clear the existing buffer
            fixedBuffer.clear();
            fixedBuffer = readFromChannel(fixedBufferSize);
            if (log.isDebugEnabled()) {
                log.debug("Reading block " + block + "/" + blockCount + " from channel " + channel.hashCode());
            }
            if (null == fixedBuffer) {
                //This means the buffer has reached it's end
                break;
            }
            if (block == finalBlockIndex) {
                //This means it will be the final lap and only the relevant bytes should be put to the response
                int finalByteCount = numberOfRemainingBytesToBeRead - (fixedBufferSize * block);
                //We need to identify the content ready to be read
                fixedBuffer.flip();
                byte[] finalContent = copyFinalContent(finalByteCount);
                fixedBuffer.position(finalContent.length);
                //Will take the un-read bytes forward
                fixedBuffer.compact();
                responseBuffer.put(finalContent, 0, finalContent.length);
            } else {
                //Making the fixed buffer readable
                fixedBuffer.flip();
                responseBuffer.put(fixedBuffer);
            }
        }
    }

    /**
     * Returns the last byte block read
     *
     * @param finalByteCount the number of bytes read in the final block
     * @return the number of bytes read from the channel
     */
    private byte[] copyFinalContent(int finalByteCount) {
        byte[] finalContent;
        int remaining = fixedBuffer.remaining();
        if (remaining < finalByteCount) {
            //If the end of file has reached before acquiring the required bytes
            finalContent = getBytes(remaining);
        } else {
            finalContent = getBytes(finalByteCount);
        }
        return finalContent;
    }

    /**
     * <p>
     * Returns a copy of the array for the specified content length index
     * </p>
     *
     * @param toByteIndex final index of the bytes which should be copied from it's position
     * @return the array which is copied from the buffer
     */
    private byte[] getBytes(int toByteIndex) {
        byte[] finalContent;
        finalContent = Arrays.copyOfRange(fixedBuffer.array(), fixedBuffer.position(), toByteIndex);
        return finalContent;
    }

    /**
     * <p>
     * Reads data from the channel until the buffer is full
     * </p>
     * <p>
     * This operation would allocate the buffer to the required amount of bytes
     * </p>
     *
     * @param numberOfBytes the number of bytes to read
     * @return the buffer which contains the bytes read
     */
    private ByteBuffer readFromChannel(int numberOfBytes) throws BallerinaIOException {
        ByteBuffer inputBuffer = null;
        try {
            if (!hasReachedToEnd) {
                if (log.isDebugEnabled()) {
                    log.debug("Reading " + numberOfBytes + " from channel " + channel.hashCode());
                }
                //We will need to allocate a buffer per each call, reusing this will not be an option
                // a - the size of the buffer would very based on the requested number of bytes
                // b - the bytes getByteArray should be re-used
                inputBuffer = ByteBuffer.allocate(numberOfBytes);
                int numberOfReadBytes = 0;
                int channelEndOfStreamFlag = -1;
                //Fills the inputBuffer with the
                while (inputBuffer.hasRemaining() && numberOfReadBytes > channelEndOfStreamFlag) {
                    numberOfReadBytes = channel.read(inputBuffer);
                    if (log.isDebugEnabled()) {
                        log.debug("Read " + numberOfBytes + " from channel " + channel.hashCode());
                    }
                }
                //If the EoF has reached we do not want to read anymore
                if (numberOfReadBytes == channelEndOfStreamFlag) {
                    if (log.isDebugEnabled()) {
                        log.debug("The channel " + channel.hashCode() + " reached EoF while reading " + numberOfBytes);
                    }
                    hasReachedToEnd = true;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("The channel " + channel.hashCode() + " reached EoF hence " + numberOfBytes + " will " +
                            "not be read from the channel");
                }
            }
        } catch (IOException e) {
            String message = "Error occurred while reading from channel ";
            throw new BallerinaIOException(message, e);
        }
        return inputBuffer;
    }

    /**
     * <p>
     * Reads a specified number of bytes from a given channel
     * <p>
     * Each time this method is called the data will be read from the point where it was read before.
     * </P>
     * <p>
     * <b>Note : </b> This method is not thread safe, if attempted to read the same file concurrently there will be
     * inconsistencies
     * </P>
     *
     * @param numberOfBytes the number of bytes to read
     * @return the read bytes
     * @throws BallerinaIOException during I/O error
     */
    public byte[] read(int numberOfBytes) throws BallerinaIOException {
        byte[] bytesRead = new byte[0];
        if (shouldNotContinue(numberOfBytes)) {
            return bytesRead;
        }
        ByteBuffer readBuffer = getBuffer(numberOfBytes);
        bytesRead = getByteArray(readBuffer);
        return bytesRead;
    }

    /**
     * Reads the specified amount of bytes to the buffer
     *
     * @param numberOfBytes the number of bytes read
     * @return the buffer which contains the bytes read
     * @throws BallerinaIOException during I/O error
     */
    ByteBuffer getReadBuffer(int numberOfBytes) throws BallerinaIOException {
        ByteBuffer readBuffer;
        if (shouldNotContinue(numberOfBytes)) {
            return null;
        }
        readBuffer = getBuffer(numberOfBytes);
        return readBuffer;
    }

    /**
     * Returns whether the operations should be continues
     *
     * @param numberOfBytes number of bytes requested to be read
     * @return true if the operations should not be continued
     * @throws BallerinaIOException during I/O error
     */
    private boolean shouldNotContinue(int numberOfBytes) throws BallerinaIOException {
        if (numberOfBytes < 0) {
            String message = "Illegal number of bytes specified " + numberOfBytes + ", expected a positive integer";
            throw new BallerinaIOException(message);
        }
        if (null != fixedBuffer) {
            //We flip the buffer to read-mode
            fixedBuffer.flip();
        }
        return hasReachedToEnd && (fixedBuffer == null || !fixedBuffer.hasRemaining());
    }

    /**
     * <p>
     * Based on the type of the buffer the bytes will be read and returned
     * </p>
     * <p>>
     * There will be two types of buffers
     * 1. Fixed size buffer, where the size of the buffer is fixed and the channel read will fill the buffer
     * 2. The allocation of the buffer will be dynamic and the state will not be maintained
     * </p>
     *
     * @param numberOfBytes number of to be read from the channel
     * @return the Buffer which will hold the read content
     * @throws BallerinaIOException during I/O error when reading the buffer
     */
    private ByteBuffer getBuffer(int numberOfBytes) throws BallerinaIOException {
        ByteBuffer readBuffer;
        if (fixedBufferSize > 0) {
            readBuffer = readFromFixedBuffer(numberOfBytes);
        } else {
            readBuffer = readFromChannel(numberOfBytes);
        }
        if (log.isTraceEnabled()) {
            log.trace("Content read into the buffer [" + Arrays.toString(readBuffer.array()) + "] from channel " +
                    channel.hashCode());
        }
        return readBuffer;
    }

    /**
     * Writes bytes to a channel, we do not worry about locking since channel is synchronous
     *
     * @param content     the data which will be written
     * @param startOffset the offset the bytes should be written
     * @return the number of bytes written
     */
    public int write(byte[] content, long startOffset) throws BallerinaException {
        ByteBuffer outputBuffer = ByteBuffer.wrap(content);
        //If a larger position is set, the position would be disregarded
        outputBuffer.position((int) startOffset);
        if (log.isDebugEnabled()) {
            log.debug("Writing " + content.length + " for the buffer with offset " + startOffset + " to channel " +
                    channel.hashCode());
        }
        return write(outputBuffer);
    }

    /**
     * Writes content in the provided buffer to the channel
     *
     * @param contentBuffer the buffer which holds the content
     * @return the number of bytes written to the channel
     * @throws BallerinaException during I/O error
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
     * Close the given channel
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
