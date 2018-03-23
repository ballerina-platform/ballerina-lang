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
public class CharacterChannel {

    private static final Logger log = LoggerFactory.getLogger(CharacterChannel.class);

    /**
     * Channel implementation to read/write characters.
     */
    private Channel channel;

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
    private Buffer contentBuffer;

    /**
     * Defines character which will be included when malformed input is detected from the decoder.
     */
    private static final char UN_MAPPABLE_CHARACTER = '�';

    /**
     * <p>
     * The maximum number of bytes which should be used to represent a character.
     * </p>
     * <p>
     * Based on https://tools.ietf.org/html/rfc3629, the max number of bytes which could be allocated for a
     * character would be '6' the maximum bytes allocated for character in java is '2'.
     * </p>
     */
    private static final int MAX_BYTES_PER_CHAR = 3;

    /**
     * Specifies the minimum buffer size which should be held in content buffer.
     */
    private static final int MINIMUM_BYTE_BUFFER_SIZE = 0;

    /**
     * Maximum number of characters which should be read per single read.
     */
    private static final int MAX_CHAR_COUNT_PER_READ = 1024;


    public CharacterChannel(Channel channel, String encoding) {
        this.channel = channel;
        bytesDecoder = Charset.forName(encoding).newDecoder();
        byteEncoder = Charset.forName(encoding).newEncoder();
        contentBuffer = new Buffer(MINIMUM_BYTE_BUFFER_SIZE);
        //We would be reading a finite number of bytes based on the number of chars * max.byte per char
        //characters in the given sequence may/may-not contain the max.byt required, hence additional bytes which are
        //decoded could contain a fraction of a character which will result in a malformed-input Exception. The bytes
        //which are on the edge should not be replaced with unknown character.
        bytesDecoder.onMalformedInput(CodingErrorAction.REPLACE);
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
     * Whether the specified character resembles malformed input.
     *
     * @param character the character which should be validated.
     * @return true if the character is malformed.
     */
    private boolean isMalformedCharacter(char character) {
        return character == UN_MAPPABLE_CHARACTER;
    }

    /**
     * Will query for the content length read by the character buffer.
     *
     * @param length length character count length.
     * @return number of bytes in the buffer for the specified character count.
     */
    private int getNumberOfBytesInContent(int length) {
        char[] availableContent = new char[length];
        this.charBuffer.get(availableContent, 0, length);
        byte[] bytes = new String(availableContent).getBytes(bytesDecoder.charset());
        this.charBuffer = CharBuffer.wrap(availableContent);
        return bytes.length;
    }

    /**
     * Reads bytes asynchronously from the channel.
     *
     * @param numberOfBytesRequired number of bytes required from the channel.
     * @param numberOfCharsRequired number of characters required.
     * @throws IOException errors occur while reading and encoding characters.
     */
    private void asyncReadBytesFromChannel(int numberOfBytesRequired, int numberOfCharsRequired)
            throws IOException {
        ByteBuffer buffer;
        int numberOfCharsProcessed = 0;
        CharBuffer intermediateCharacterBuffer;
        //Provided at this point any remaining character left in the buffer is copied
        charBuffer = CharBuffer.allocate(numberOfBytesRequired);
        do {
            buffer = contentBuffer.get(numberOfBytesRequired, channel);
            intermediateCharacterBuffer = bytesDecoder.decode(buffer);
            numberOfCharsProcessed = numberOfCharsProcessed + intermediateCharacterBuffer.limit();
            charBuffer.put(intermediateCharacterBuffer);
        } while (!channel.hasReachedEnd() && numberOfCharsProcessed < numberOfCharsRequired);
        //We make the char buffer ready to read
        charBuffer.flip();
        processChars(numberOfCharsRequired, buffer, numberOfCharsProcessed);
    }

    /**
     * <p>
     * When processing characters, there will be instances where due to unavailability of bytes the characters gets
     * marked as malformed.
     * <p>
     * There will also be instances where the actual characters in the original content is malformed.
     * <p>
     * This function will distinguished between these two, if the last character processed is malformed this will
     * return the character as malformed. Since there will be no more bytes left to be read from the channel.
     * <p>
     * If the channel does not return EoL there will be a possibility where conjunction between the remaining bytes
     * will produce the content required.
     * </p>
     *
     * @param numberOfCharsRequired  total number of characters required.
     * @param buffer                 the buffer which will hold the content.
     * @param numberOfCharsProcessed number of characters processed.
     */
    private void processChars(int numberOfCharsRequired, ByteBuffer buffer, int numberOfCharsProcessed) {
        final int minimumNumberOfCharsRequired = 0;
        if (numberOfCharsProcessed > minimumNumberOfCharsRequired) {
            int lastCharacterIndex = numberOfCharsProcessed - 1;
            char lastCharacterProcessed = charBuffer.get(lastCharacterIndex);
            if (numberOfCharsRequired < numberOfCharsProcessed && isMalformedCharacter(lastCharacterProcessed)) {
                int numberOfBytesWithoutTheLastChar = getNumberOfBytesInContent(lastCharacterIndex);
                int numberOfBytesAllocatedForLastChar = buffer.capacity() - numberOfBytesWithoutTheLastChar;
                contentBuffer.reverse(numberOfBytesAllocatedForLastChar);
            }
        }
    }

    /**
     * Read asynchronously from channel.
     *
     * @param numberOfCharacters number of characters which needs to be read.
     * @return characters which were read.
     * @throws IOException during I/O error.
     */
    public String read(int numberOfCharacters) throws IOException {
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
            asyncReadBytesFromChannel(numberOfBytesRequired, numberOfCharacters);
            //We need to ensure that the required amount of characters are available in the buffer
            if (charBuffer.limit() < charsRequiredToBeReadFromChannel) {
                //This means the amount of chars required are not available
                charsRequiredToBeReadFromChannel = charBuffer.limit();
            }
            appendCharsToString(content, charsRequiredToBeReadFromChannel);
        } catch (IOException e) {
            throw new IOException("Error occurred while reading characters from buffer", e);
        }
        return content.toString();
    }

    /**
     * Reads all content from the I/O source.
     *
     * @return all content which is read.
     * @throws IOException during I/O error.
     */
    @Deprecated
    public String readAll() throws IOException {
        StringBuilder response = new StringBuilder();
        String value;
        do {
            value = read(MAX_CHAR_COUNT_PER_READ);
            response.append(value);
        } while (!value.isEmpty());
        return response.toString();
    }

    /**
     * Writes a given string input into the channel.
     *
     * @param content the string content to be written.
     * @param offset  the offset which should be used for writing.
     * @return the number of bytes/characters written.
     * @throws IOException during I/O error.
     */
    public int write(String content, int offset) throws IOException {
        try {
            int numberOfBytesWritten = 0;
            if (channel != null) {
                char[] characters = content.toCharArray();
                CharBuffer characterBuffer = CharBuffer.wrap(characters);
                characterBuffer.position(offset);
                ByteBuffer encodedBuffer = byteEncoder.encode(characterBuffer);
                do {
                    numberOfBytesWritten = numberOfBytesWritten + channel.write(encodedBuffer);
                } while (encodedBuffer.hasRemaining());
            } else {
                log.warn("The channel has already being closed");
            }
            return numberOfBytesWritten;
        } catch (CharacterCodingException e) {
            String message = "Error occurred while writing bytes to the channel " + channel.hashCode();
            throw new IOException(message, e);
        }
    }

    /**
     * Closes the given channel.
     *
     * @throws IOException errors occur while trying to close the connection.
     */
    public void close() throws IOException {
        channel.close();
    }
}
