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
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;


/**
 * <p>
 * Represents the channel to perform character I/O operations.
 * </p>
 * <p>
 * This is a stateful channel.
 * </p>
 */
public class BCharacterChannel {

    private static final Logger log = LoggerFactory.getLogger(BCharacterChannel.class);

    /**
     * Channel implementation to read/write characters.
     */
    private AbstractChannel channel;

    /**
     * Decodes the specified bytes when reading.
     */
    private CharsetDecoder bytesDecoder;

    /**
     * Encodes a given list of characters before writing to channel.
     */
    private CharsetEncoder byteEncoder;

    /**
     * Will hold the decoded characters, char-buffer holds the character length and it's remaining.
     */
    private CharBuffer charBuffer;

    /**
     * Holds bytes retrieved from the channel.
     */
    private BByteBuffer contentBuffer;

    /**
     * <p>
     * The maximum number of bytes which should be used to represent a character.
     * </p>
     * <p>
     * Based on https://tools.ietf.org/html/rfc3629, the max number of bytes which could be allocated for a
     * character would be '6' the maximum bytes allocated for character in java is '2'.
     * </p>
     */
    private static final int MAX_BYTES_PER_CHAR = 2;

    /**
     * Specifies the minimum buffer size which should be held in content buffer.
     */
    private static final int MINIMUM_BYTE_BUFFER_SIZE = 0;


    public BCharacterChannel(AbstractChannel channel, String encoding) {
        this.channel = channel;
        bytesDecoder = Charset.forName(encoding).newDecoder();
        byteEncoder = Charset.forName(encoding).newEncoder();
        contentBuffer = new BByteBuffer(MINIMUM_BYTE_BUFFER_SIZE);
        //We would be reading a finite number of bytes based on the number of chars * max.byte per char
        //characters in the given sequence may/may-not contain the max.byt required, hence additional bytes which are
        //decoded could contain a fraction of a character which will result in a malformed-input Exception. The bytes
        //which are on the edge should not be processed, it should be processed when more bytes are get from the
        //channel, hence the malformed input will be ignored and will be continued.
        bytesDecoder.onMalformedInput(CodingErrorAction.IGNORE);
    }

    /**
     * Gets number of characters left in the character buffer.
     *
     * @return number of remaining characters.
     */
    private int getNumberOfCharactersRemaining() {
        int limit = charBuffer.limit();
        int position = charBuffer.position();
        return limit - position;
    }

    /**
     * Appends data from CharBuffer to string.
     *
     * @param content        the char sequence which will be appended to final string.
     * @param characterCount the number of characters in the CharBuffer.
     */
    private void appendCharsToString(StringBuilder content, int characterCount) {
        final int indexCharacterOffset = 0;
        char[] remainingChars = new char[characterCount];
        charBuffer.get(remainingChars, indexCharacterOffset, characterCount);
        content.append(remainingChars);
        if (log.isTraceEnabled()) {
            log.trace("Characters appended to the string," + content);
        }
    }

    /**
     * <p>
     * Appends a given character sequence to a string.
     * </p>
     * <p>
     * if the character buffer has any remaining this operation will copy the remaining characters into the string.
     * <p>
     * The character buffer may or may not have the required amount of characters to fill the entire string builder.
     * <p>
     * if the character buffer has > characters the required amount will be copied, if the character buffer has
     * less characters the remaining characters will be copied.
     * </p>
     *
     * @param content the container which will hold the content.
     */
    private void appendRemainingCharacters(StringBuilder content) {
        if (null != charBuffer) {
            //Get the remaining content left in the buffer
            int numberOfCharactersRemaining = getNumberOfCharactersRemaining();
            //The length of the string builder
            int numberOfCharsRequired = content.capacity();
            final int minimumCharacterCount = 0;
            if (log.isDebugEnabled()) {
                log.debug("Number of characters requested = " + numberOfCharsRequired + ",characters remaining in " +
                        "buffer= " + numberOfCharactersRemaining);
            }
            if (numberOfCharsRequired < numberOfCharactersRemaining) {
                //If the remaining character count is < we need to reduce the required number of chars
                numberOfCharactersRemaining = numberOfCharsRequired;
            }
            if (numberOfCharactersRemaining > minimumCharacterCount) {
                if (log.isDebugEnabled()) {
                    log.debug("Appending " + numberOfCharactersRemaining + " to the string.");
                }
                appendCharsToString(content, numberOfCharactersRemaining);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Character buffer has not being initialized yet for channel " + channel.hashCode());
            }
        }
    }

    /**
     * Reads specified number of characters from a given channel.
     *
     * @param numberOfCharacters the number of characters which should be retrieved.
     * @return the sequence of characters as a string.
     * @throws BallerinaIOException I/O errors.
     */
    public String read(int numberOfCharacters) throws BallerinaIOException {
        StringBuilder content;
        try {
            //Will identify the number of characters required
            int charsRequiredToBeReadFromChannel;
            content = new StringBuilder(numberOfCharacters);
            int numberOfBytesRequired = numberOfCharacters * MAX_BYTES_PER_CHAR;
            //First the remaining buffer would be get and the characters remaining in the buffer will be written
            appendRemainingCharacters(content);
            //Content capacity would give the total size of the string builder (number of chars)
            //Content length will give the number of characters appended to the builder through the function
            //call appendRemainingCharacters(..)
            charsRequiredToBeReadFromChannel = content.capacity() - content.length();
            if (charsRequiredToBeReadFromChannel == 0) {
                //This means there's no requirement to get the characters from channel
                return content.toString();
            }
            if (log.isDebugEnabled()) {
                log.debug("Number of chars required to be get from the channel " + charsRequiredToBeReadFromChannel);
            }
            ByteBuffer byteBuffer = contentBuffer.get(numberOfBytesRequired, channel);
            charBuffer = bytesDecoder.decode(byteBuffer);
            //If there's a discrepancy between the limit and the capacity this could probably mean the required
            // amount of bytes have not being get
            int unmappedByteCount = charBuffer.capacity() - charBuffer.limit();
            if (unmappedByteCount > 0) {
                //This means some of the bytes were not get from the buffer
                //possibly a character representation which has requires more bytes
                //Hence we reverse the ByteBuffer position
                contentBuffer.reverse(unmappedByteCount);
            }
            //We need to ensure that the required amount of characters are available in the buffer
            if (charBuffer.limit() < charsRequiredToBeReadFromChannel) {
                //This means the amount of chars required are not available
                charsRequiredToBeReadFromChannel = charBuffer.limit();
            }
            appendCharsToString(content, charsRequiredToBeReadFromChannel);
        } catch (IOException e) {
            throw new BallerinaIOException("Error occurred while reading characters from buffer", e);
        }
        return content.toString();
    }

    /**
     * Writes a given string input into the channel.
     *
     * @param content the string content to be written.
     * @param offset  the offset which should be used for writing.
     * @return the number of bytes/characters written.
     * @throws BallerinaIOException during I/O error.
     */
    public int write(String content, int offset) throws BallerinaIOException {
        try {
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
        } catch (CharacterCodingException e) {
            String message = "Error occurred while writing bytes to the channel " + channel.hashCode();
            throw new BallerinaIOException(message, e);
        }
    }

    /**
     * Closes the given channel.
     */
    public void close() {
        channel.close();
    }
}
