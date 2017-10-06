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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;

/**
 * <p>
 * Represents the channel for character I/O
 * </p>
 * <p>
 *  This is a stateful channel
 * </p>
 */
public class BCharacterChannel {

    private static final Logger log = LoggerFactory.getLogger(BCharacterChannel.class);

    /**
     * Holds the byte channel the characters should be read
     */
    private BByteChannel channel;

    /**
     * Decodes the specified bytes when reading, currently decoder supports only unicode charset
     */
    private CharsetDecoder bytesDecoder;

    /**
     * Encodes a given list of characters before writing to channel
     */
    private CharsetEncoder byteEncoder;

    /**
     * Will hold the decoded characters, char-buffer holds the character length in it's remaining
     */
    private CharBuffer charBuffer;

    /**
     * Holds the bytes read from the byte channel
     */
    private ByteBuffer byteBuffer;

    /**
     * <p>
     * The maximum number of bytes held by a character
     * </p>
     * <p>
     * Based on https://tools.ietf.org/html/rfc3629, the max number of bytes which could be allocated for a
     * character would be '6'
     * </p>
     */
    private static final int MAX_BYTES_PER_CHAR = 2;


    public BCharacterChannel(BByteChannel channel, String encoding) {
        this.channel = channel;
        bytesDecoder = Charset.forName(encoding).newDecoder();
        byteEncoder = Charset.forName(encoding).newEncoder();
        //Given there would be a possibility for a byte to be marked as malformed due to the counts
        //TODO explain this more
        bytesDecoder.onMalformedInput(CodingErrorAction.IGNORE);
    }

    /**
     * <p>
     * Returns the bytes remaining in the current buffer
     * </p>
     * <p>
     * This will return any of the bytes which have not being read by the decoder
     * i.e say 10 bytes are read. Each character is represented with 3 bytes, hence there will be 1 byte remaining
     * which is not decoded
     * </p>
     *
     * @return the number of bytes remaining in the buffer, 0 if the buffer is not initialized
     */
    private int getRemainingBytes() {
        int remainingBytes = 0;
        if (null != byteBuffer) {
            int byteContentPosition = byteBuffer.position();
            int limit = byteBuffer.limit();
            //This could be obtained from the ByteBuffer position and limit
            if (byteContentPosition < limit) {
                //this means there're remaining bytes
                remainingBytes = limit - byteContentPosition;
            }
        }
        return remainingBytes;
    }


    /**
     * Returns the buffer which has remaining bytes
     *
     * @return the buffer which contains the bytes
     */
    private ByteBuffer getRemainingByteBuffer() {
        ByteBuffer remaining = null;
        if (null != byteBuffer) {
            //byteBuffer.compact();
            //remaining = byteBuffer.slice();
            byte[] content = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
            remaining = ByteBuffer.wrap(content);
        }
        return remaining;
    }

    /**
     * <p>
     * Appends a given character sequence to a string
     * </p>
     * <p>
     * if the character buffer has any remaining this operation will copy the remaining characters into the string
     * <p>
     * The character buffer may or may not have the required amount of characters to fill the entire builder
     * <p>
     * if the character buffer has > characters the required amount will be copied, if the character buffer has
     * less characters what ever that is remaining will be copied
     * </p>
     *
     * @param content the container which will hold the content
     */
    private void appendRemainingCharacters(StringBuilder content) {
        if (null != charBuffer) {
            //Get the remaining content left in the buffer
            int limit = charBuffer.limit();
            int position = charBuffer.position();
            int numberOfCharactersRemaining = limit - position;
            //The length of the string builder
            int numberOfChars = content.capacity();

            if (numberOfChars < numberOfCharactersRemaining) {
                //If the remaining character count is <= we need to reduce the required number of chars
                numberOfCharactersRemaining = numberOfChars;
            }

            if (numberOfCharactersRemaining > 0) {
                char[] remainingChars = new char[numberOfCharactersRemaining];
                charBuffer.get(remainingChars, 0, numberOfCharactersRemaining);
                content.append(remainingChars);
            }
        }
    }


    /**
     * <p>
     * Creates a buffer to accommodate the specified amount of bytes
     * </p>
     * <p>
     * This operation would,
     * a) consider the remaining bytes left in the current buffer, if there's remaining that could fill the entire
     * size required, this operation would read the existing bytes from the the current buffer
     * b) if the required amount of bytes > remaining bytes the operation will allocate a new buffer place the
     * existing bytes into the the new buffer and read the rest of the bytes from the channel
     * </p>
     *
     * @param numberOfBytesRequired the capacity of the new buffer
     * @return the new buffer which will accommodate the size requested
     * @throws IOException during I/O error
     * @see BCharacterChannel#getRemainingByteBuffer()
     */
    private ByteBuffer createBufferForCharacterReading(int numberOfBytesRequired) throws IOException {
        //The buffer which will hold the read content
        ByteBuffer dstBuffer = ByteBuffer.allocate(numberOfBytesRequired);
        //If not we need to read more content from the buffer and fill the channel
        //Given that we already have identified the remaining characters we need to fill the buffer to ger new chars
        int remainingBytes = getRemainingBytes();
        int numberOfBytesToReadIntoBuffer = dstBuffer.capacity();
        ByteBuffer remainingBuffer = getRemainingByteBuffer();

        if (numberOfBytesToReadIntoBuffer > remainingBytes) {
            //This means we need to re-fill the buffer
            if (null != remainingBuffer) {
                dstBuffer.put(remainingBuffer);
            }
            int numberOfBytesRequiredFromChannel = numberOfBytesToReadIntoBuffer - remainingBytes;
            //Given the remaining bytes are already processed we need only the rest of them
            ByteBuffer channelBuffer = channel.getReadBuffer(numberOfBytesRequiredFromChannel);
            if (null != channelBuffer) {
                //As long as ByteBuffer has not reached end
                channelBuffer.flip();
                dstBuffer.put(channelBuffer);
            }
        } else {
            if (null != remainingBuffer) {
                //This means we could read all the content required from the existing buffer
                dstBuffer.put(remainingBuffer.array(), 0, numberOfBytesToReadIntoBuffer);
                //The we set the new position to the remaining buffer
                remainingBuffer.position(numberOfBytesToReadIntoBuffer);
            }
        }

        //Finally the destination buffer will be flipped for reading
        dstBuffer.flip();

        return dstBuffer;

    }

    /**
     * Reads specified number of characters from a given channel
     *
     * @param numberOfCharacters the number of characters which should be read
     * @return the sequence of characters as a string
     * @throws IOException I/O errors
     */
    public String read(int numberOfCharacters) throws IOException {

        StringBuilder content = new StringBuilder(numberOfCharacters);
        int numberOfBytesRequired = numberOfCharacters * MAX_BYTES_PER_CHAR;

        //Will identify the number of characters required
        int numberOfCharacterRequired;

        //First the remaining buffer would be read and the characters remaining in the buffer will be written
        appendRemainingCharacters(content);

        //Content capacity would give the total size of the string builder (number of chars)
        //Content length will give the number of characters appended to the builder through the function
        //call appendRemainingCharacters(..)
        numberOfCharacterRequired = content.capacity() - content.length();

        //Now we need to check whether there's any room left in the
        if (numberOfCharacterRequired == 0) {
            //This means the required amount of characters are being read
            return content.toString();
        }

        //Will initialize the new byte buffer for reading characters
        byteBuffer = createBufferForCharacterReading(numberOfBytesRequired);

        charBuffer = bytesDecoder.decode(byteBuffer);

        //If there's a discrepancy between the limit and the capacity this could probably mean the required amount of
        //bytes have not being read
        int unmappedByteCount = charBuffer.capacity() - charBuffer.limit();

        if (unmappedByteCount > 0) {
            //This means some of the bytes were not read from the buffer
            //possibly a character representation which has requires more bytes
            //Hence we reverse the ByteBuffer position
            int reversedByteBufferPosition = byteBuffer.position() - unmappedByteCount;
            byteBuffer.position(reversedByteBufferPosition);
        }

        //We need to ensure that the required amount of characters are available in the buffer
        if (charBuffer.limit() < numberOfCharacterRequired) {
            //This means the amount of chars required are not available
            numberOfCharacterRequired = charBuffer.limit();
        }

        char[] readChars = new char[numberOfCharacterRequired];

        charBuffer.get(readChars, 0, numberOfCharacterRequired);
        content.append(readChars);

        return content.toString();
    }

    /**
     * Writes a given string input into the channel
     *
     * @param content the string content to write
     * @param offset  the offset which should be set when writing
     * @return the number of bytes/characters written
     * @throws IOException during I/O error
     */
    public int write(String content, int offset) throws IOException {

        int numberOfBytesWritten = -1;

        if (channel != null) {

            char[] characters = content.toCharArray();
            CharBuffer characterBuffer = CharBuffer.wrap(characters);
            characterBuffer.position(offset);
            ByteBuffer encodedBuffer = byteEncoder.encode(characterBuffer);
            numberOfBytesWritten = channel.write(encodedBuffer);
        } else {
            log.warn("The channel has already being closed");
        }

        return numberOfBytesWritten;
    }

    /**
     * Closes the given channel
     */
    public void close() {
        channel.close();
    }
}
