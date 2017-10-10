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

package org.ballerinalang.nativeimpl.io.channels;

import org.ballerinalang.nativeimpl.io.Close;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Arrays;

/**
 * <p>
 * Represents the bytes channel, Note this class is not thread safe so there cannot be multiple reads or writes
 * </p>
 * <p>
 * This is a stateful channel
 * </p>
 */
public class BByteChannel {

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
     * If a fixed buffer is used for reading this will manage the state
     */
    private ByteBuffer fixedBuffer;


    private static final Logger log = LoggerFactory.getLogger(Close.class);

    /**
     * Creates a ballerina channel which will source/sink from I/O resource
     *
     * @param channel the channel which will source/sink
     */
    public BByteChannel(ByteChannel channel) throws IOException {
        if (null != channel) {
            this.channel = channel;
        } else {
            throw new IOException("The provided channel cannot be null");
        }
    }

    /**
     * Creates a channel with a fixed size
     *
     * @param channel         the channel to perform I/O operations
     * @param fixedBufferSize the size of the fixed buffer
     */
    public BByteChannel(ByteChannel channel, int fixedBufferSize) {
        this.channel = channel;
        this.fixedBufferSize = fixedBufferSize;
        this.fixedBuffer = ByteBuffer.allocate(fixedBufferSize);
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

        System.arraycopy(content, srcPos, destinationArray, destPos, length);
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
        int readBytePosition = origin.position();
        int totalBufferSpace = origin.capacity();
        byte[] content;

        if (readBytePosition < totalBufferSpace) {
            //This means the required amount of bytes are not available
            //Hence we will need to shrink the buffer
            content = shrink(origin.array(), readBytePosition);
        } else {
            content = origin.array();
        }

        return content;
    }

    /**
     * Reads the content from a byte buffer which has fixed length
     *
     * @param numberOfBytes number of bytes required to be read from the channel
     * @return the number of bytes read
     * @throws IOException during I/O error
     */
    private ByteBuffer readFromFixedBuffer(int numberOfBytes) throws IOException {
        //We need to allocate a new buffer to produce as the response
        ByteBuffer responseBuffer = ByteBuffer.allocate(numberOfBytes);
        int remainingBytes = fixedBuffer.remaining();

        //We dump the remaining bytes to the response
        if (remainingBytes >= numberOfBytes) {
            //This means the requested amount of bytes are available in the channel
            byte[] remainingContent = Arrays.copyOfRange(fixedBuffer.array(), fixedBuffer.position(), numberOfBytes);
            responseBuffer.put(remainingContent, 0, remainingContent.length);
        } else {
            //We need a refill the buffer with the existing content
            byte[] remainingContent = Arrays.copyOfRange(fixedBuffer.array(), fixedBuffer.position(), remainingBytes);
            responseBuffer.put(remainingContent, 0, remainingContent.length);
            //Let's request for a re-fill of the remaining
            int numberOfRemainingBytesToBeRead = numberOfBytes - remainingBytes;
            int blockCount = (int) Math.ceil((float) numberOfRemainingBytesToBeRead / (float) fixedBufferSize);

            //We need to retrieve the content into blocks
            for (int block = 0; block < blockCount; block++) {
                //We clear the existing buffer
                fixedBuffer.clear();
                fixedBuffer = readFromChannel(fixedBufferSize);

                if (null == fixedBuffer) {
                    //This means the buffer has reached it's end
                    break;
                }

                if (block == (blockCount - 1)) {
                    //This means it will be the final lap and only the relevant bytes should be put to the response
                    int finalByteCount = numberOfRemainingBytesToBeRead - (fixedBufferSize * block);
                    byte[] finalContent;
                    //We need to identify the content ready to be read
                    fixedBuffer.flip();

                    if (fixedBuffer.remaining() < finalByteCount) {
                        //If the end of file has reached before acquiring the required bytes
                        finalContent = Arrays.copyOfRange(fixedBuffer.array(), fixedBuffer.position(), fixedBuffer
                                .remaining());
                    } else {
                        finalContent = Arrays.copyOfRange(fixedBuffer.array(), fixedBuffer.position(),
                                finalByteCount);
                    }

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

        return responseBuffer;
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
    private ByteBuffer readFromChannel(int numberOfBytes) throws IOException {

        ByteBuffer inputBuffer = null;

        if (!hasReachedToEnd) {

            //We will need to allocate a buffer per each call, reusing this will not be an option
            // a - the size of the buffer would very based on the requested number of bytes
            // b - the bytes getByteArray should be re-used
            inputBuffer = ByteBuffer.allocate(numberOfBytes);
            int numberOfReadBytes = 0;
            int channelEndOfStreamFlag = -1;


            //Fills the inputBuffer with the
            while (inputBuffer.hasRemaining() && numberOfReadBytes > channelEndOfStreamFlag) {
                numberOfReadBytes = channel.read(inputBuffer);
            }
            //If the EoF has reached we do not want to read anymore
            if (numberOfReadBytes == channelEndOfStreamFlag) {
                hasReachedToEnd = true;
            }
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
     * @throws IOException during I/O error
     */
    public byte[] read(int numberOfBytes) throws IOException {
        byte[] bytesRead = new byte[0];

        if (numberOfBytes < 0) {
            String message = "Illegal number of bytes specified " + numberOfBytes + ", expected a positive integer";
            throw new IOException(message);
        }

        if (null != fixedBuffer) {
            //We flip the buffer to read-mode
            fixedBuffer.flip();
        }

        if (!hasReachedToEnd || (fixedBuffer != null && fixedBuffer.hasRemaining())) {
            ByteBuffer readBuffer;

            if (fixedBufferSize > 0) {
                readBuffer = readFromFixedBuffer(numberOfBytes);
            } else {
                readBuffer = readFromChannel(numberOfBytes);
            }

            bytesRead = getByteArray(readBuffer);
        }
        return bytesRead;
    }

    /**
     * Reads the specified amount of bytes to the buffer
     *
     * @param numberOfBytes the number of bytes read
     * @return the buffer which contains the bytes read
     * @throws IOException during I/O error
     */
    ByteBuffer getReadBuffer(int numberOfBytes) throws IOException {

        ByteBuffer readBuffer = null;

        if (numberOfBytes < 0) {
            String message = "Illegal number of bytes specified " + numberOfBytes + ", expected a positive integer";
            throw new IOException(message);
        }

        if (null != fixedBuffer) {
            //We flip the buffer to read-mode
            fixedBuffer.flip();
        }

        if (!hasReachedToEnd || (fixedBuffer != null && fixedBuffer.hasRemaining())) {

            if (fixedBufferSize > 0) {
                readBuffer = readFromFixedBuffer(numberOfBytes);
            } else {
                readBuffer = readFromChannel(numberOfBytes);
            }
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
    public int write(byte[] content, long startOffset) throws IOException {
        ByteBuffer outputBuffer = ByteBuffer.wrap(content);
        //If a larger position is set, the position would be disregarded
        outputBuffer.position((int) startOffset);
        return write(outputBuffer);
    }

    /**
     * Writes content in the provided buffer to the channel
     *
     * @param contentBuffer the buffer which holds the content
     * @return the number of bytes written to the channel
     * @throws IOException during I/O error
     */
    int write(ByteBuffer contentBuffer) throws IOException {
        int numberOfBytesWritten;

        //There's no guarantee in a single write all the bytes will be written
        //Hence we need to check iteratively whether all the bytes in the buffer were written until the EoF is reached
        while (contentBuffer.hasRemaining()) {
            channel.write(contentBuffer);
        }

        numberOfBytesWritten = contentBuffer.position();

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
            throw new BallerinaException(message + e.getMessage());
        }
    }
}
