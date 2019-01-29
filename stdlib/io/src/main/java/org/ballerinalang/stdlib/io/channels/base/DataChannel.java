/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io.channels.base;

import org.ballerinalang.stdlib.io.channels.base.data.LongResult;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Represents a channel which will allow performing data i/o operations.
 */
public class DataChannel implements IOChannel {
    /**
     * Source for reading bytes.
     */
    private Channel channel;
    /**
     * Represents network byte order.
     */
    private ByteOrder order;

    /**
     * Represents 64 bit long value which will be used to convert from var long to fixed long.
     */
    private static final long BIT_64_LONG_MAX = 0xFFFFFFFFFFFFFFFFL;

    public DataChannel(Channel channel, ByteOrder order) {
        this.channel = channel;
        this.order = order;
    }

    @Override
    public boolean hasReachedEnd() {
        return channel.hasReachedEnd();
    }

    public Channel getChannel() {
        return channel;
    }

    /**
     * Reverse the underlying array of the given ByteBuffer.
     *
     * @param buffer byte content in it's native order.
     */
    private byte[] reverse(ByteBuffer buffer) {
        byte[] contentArr = buffer.array();
        int length = buffer.limit();
        byte[] reverseContent = new byte[length];
        for (int count = 0; count < length; count++) {
            reverseContent[count] = contentArr[(length - 1) - count];
        }
        return reverseContent;
    }

    /**
     * Recursively read bytes until the buffer is filled.
     *
     * @param buffer         buffer which holds the byte[].
     * @param representation specifies the representation.
     * @throws IOException during i/o error.
     */
    private void readFull(ByteBuffer buffer, Representation representation) throws IOException {
        do {
            channel.read(buffer);
        } while (buffer.hasRemaining() && !channel.hasReachedEnd());
        if (order.equals(ByteOrder.LITTLE_ENDIAN) && !representation.equals(Representation.VARIABLE)) {
            int bufferPosition = buffer.position();
            int limit = buffer.limit();
            byte[] reverseContent = reverse(buffer);
            buffer.rewind();
            buffer.put(reverseContent, 0, limit);
            buffer.position(bufferPosition);
            buffer.limit(limit);
        }
    }

    /**
     * Reads varint from the given channel.
     *
     * @return the bytes read through the buffer.
     * @throws IOException during i/o error.
     */
    private ByteBuffer readVarInt() throws IOException {
        int bufferLimit = 0;
        boolean hasRemainingBytes = true;
        //Will create an array with maximum number of bytes allocated
        byte[] content = new byte[Long.BYTES];
        ByteBuffer buf = ByteBuffer.wrap(content);
        do {
            buf.limit(++bufferLimit);
            readFull(buf, Representation.VARIABLE);
            buf.flip();
            byte b = buf.get(bufferLimit - 1);
            //We '&' with 10000000 and shift it by 7 to identify the msb
            if ((b & 0x80) >> 7 == 0) {
                //We identify whether there're bytes remaining to be read
                hasRemainingBytes = false;
            }
            //This means we could read more bytes
            //We convert the read value back by omitting the msb
            buf.put(bufferLimit - 1, (byte) (b & 0x7F));
            //The inserted byte will be made ready to be read
            buf.position(buf.limit());
        } while (hasRemainingBytes);
        return buf;
    }

    /**
     * Decodes the long from a provided input channel.
     *
     * @param representation specified size representation of the long value.
     * @return the decoded long value.
     * @throws IOException during i/o error.
     */
    private LongResult decodeLong(Representation representation) throws IOException {
        ByteBuffer buffer;
        int requiredNumberOfBytes;
        if (Representation.VARIABLE.equals(representation)) {
            buffer = readVarInt();
            if (order.equals(ByteOrder.LITTLE_ENDIAN)) {
                byte[] reverse = reverse(buffer);
                buffer.rewind();
                buffer.put(reverse);
            }
        } else {
            requiredNumberOfBytes = representation.getNumberOfBytes();
            buffer = ByteBuffer.allocate(requiredNumberOfBytes);
            buffer.order(order);
            readFull(buffer, representation);
        }
        buffer.flip();
        return deriveLong(representation, buffer);
    }

    /**
     * Converts var long to a fixed size long.
     *
     * @param value  var long value.
     * @param nBytes number of bytes in VarLong.
     * @return corresponding long converted through VarLong.
     */
    private long convertVarLongToFixedLong(long value, int nBytes) {
        int nBits = nBytes * Representation.VARIABLE.getBase() - 1;
        if (value >> nBits == 1) {
            long intercept = BIT_64_LONG_MAX << nBits;
            //This means it would be a sign representation
            value = value | intercept;
        }
        return value;
    }

    /**
     * Merge bytes and encodes long.
     *
     * @param representation the capacity of the long value i.e whether it's 32bit, 64bit.
     * @param buffer         holds the bytes which represents the long.
     * @return the value of long and the corresponding number of bytes read.
     */
    private LongResult deriveLong(Representation representation, ByteBuffer buffer) {
        long value = 0;
        int maxNumberOfBits = 0xFFFF;
        int byteLimit = buffer.limit();
        int totalNumberOfBits = (byteLimit - 1) * representation.getBase();
        do {
            long shiftedValue = 0L;
            if (Representation.BIT_64.equals(representation)) {
                long flippedValue = (buffer.get() & maxNumberOfBits);
                shiftedValue = flippedValue << totalNumberOfBits;
            } else if (Representation.BIT_32.equals(representation)) {
                int flippedValue = (buffer.get() & maxNumberOfBits);
                shiftedValue = flippedValue << totalNumberOfBits;
            } else if (Representation.BIT_16.equals(representation)) {
                short flippedValue = (short) (buffer.get() & maxNumberOfBits);
                shiftedValue = flippedValue << totalNumberOfBits;
            } else if (Representation.VARIABLE.equals(representation)) {
                long flippedValue = (buffer.get() & maxNumberOfBits);
                shiftedValue = flippedValue << totalNumberOfBits;
            }
            maxNumberOfBits = 0xFF;
            value = value + shiftedValue;
            totalNumberOfBits = totalNumberOfBits - representation.getBase();
        } while (buffer.hasRemaining());
        if (Representation.VARIABLE.equals(representation)) {
            value = convertVarLongToFixedLong(value, byteLimit);
        }
        return new LongResult(value, byteLimit);
    }

    /**
     * Reverse the buffer content based on little-endian byte order.
     *
     * @param content original content which contains the byte[] information.
     * @return content which is reversed.
     */
    private byte[] reverseOrder(byte[] content) {
        byte[] reversedContent = new byte[content.length];
        int length = content.length;
        for (int count = 0; count < length; count++) {
            //When we cast byte to a base 7 we need to omit the last bit being modified
            reversedContent[count] = (byte) (content[(length - 1) - count] & 0x7F);
            if (count < (length - 1)) {
                //We indicated the most significant bit to be '1' since this is variable length
                reversedContent[count] = (byte) (content[(length - 1) - count] | 0x80);
            }
        }
        return reversedContent;
    }

    /**
     * Splits the long between several bytes.
     *
     * @param value          the value of the long which should be split.
     * @param representation the size of the long in bits.
     * @return the encoded long value.
     */
    private byte[] encodeLong(long value, Representation representation) {
        byte[] content;
        int nBytes;
        int totalNumberOfBits;
        if (Representation.VARIABLE.equals(representation)) {
            //We identify the log(2) of the value to identify how many bits are required to represent
            int nBits = (int) Math.abs(Math.round((Math.log(Math.abs(value)) / Math.log(2))));
            nBytes = nBits / representation.getBase() + 1;
            content = new byte[nBytes];
        } else {
            nBytes = representation.getNumberOfBytes();
            content = new byte[representation.getNumberOfBytes()];
        }
        totalNumberOfBits = (nBytes * representation.getBase()) - representation.getBase();
        for (int count = 0; count < nBytes; count++) {
            content[count] = (byte) (value >> totalNumberOfBits);
            if (Representation.VARIABLE.equals(representation) && order.equals(ByteOrder.BIG_ENDIAN)) {
                //When we cast byte to a base 7 we need to omit the last bit being modified
                content[count] = (byte) (content[count] & 0x7F);
                if (count < (nBytes - 1)) {
                    //We indicated the most significant bit to be '1' since this is variable length
                    content[count] = (byte) (content[count] | 0x80);
                }
            }
            totalNumberOfBits = totalNumberOfBits - representation.getBase();
        }
        return content;
    }

    /**
     * Writes the given content to the channel.
     *
     * @param buffer         buffer which holds the content.
     * @param representation whether this a var/fix int.
     * @throws IOException occurs during i/o error.
     */
    private void write(ByteBuffer buffer, Representation representation) throws IOException {
        if (order.equals(ByteOrder.LITTLE_ENDIAN) && !Representation.VARIABLE.equals(representation)) {
            byte[] reverse = reverse(buffer);
            channel.write(ByteBuffer.wrap(reverse));
        } else if (order.equals(ByteOrder.LITTLE_ENDIAN) && Representation.VARIABLE.equals(representation)) {
            byte[] reverse = reverseOrder(buffer.array());
            channel.write(ByteBuffer.wrap(reverse));
        } else {
            channel.write(buffer);
        }
    }

    /**
     * Writes fixed size long value.
     *
     * @param value          the value of the long which should be written.
     * @param representation the size of the long in bits.
     * @throws IOException during i/o error.
     */
    public void writeLong(long value, Representation representation) throws IOException {
        byte[] bytes = encodeLong(value, representation);
        write(ByteBuffer.wrap(bytes), representation);
    }

    /**
     * Reads fixed size long.
     *
     * @param representation size of the long in bits.
     * @return the long value which is read.
     * @throws IOException during i/o error.
     */
    public LongResult readLong(Representation representation) throws IOException {
        return decodeLong(representation);
    }

    /**
     * Writes double value.
     *
     * @param value          value which should be written.
     * @param representation the size of the double in bits.
     * @throws IOException during i/o error.
     */
    public void writeDouble(double value, Representation representation) throws IOException {
        long lValue;
        if (Representation.BIT_32.equals(representation)) {
            lValue = Float.floatToIntBits((float) value);
        } else {
            lValue = Double.doubleToRawLongBits(value);
        }
        writeLong(lValue, representation);
    }

    /**
     * Reads double value.
     *
     * @param representation the size of the double value which should be read.
     * @return the double value which is read.
     * @throws IOException during i/o error.
     */
    public double readDouble(Representation representation) throws IOException {
        if (Representation.BIT_32.equals(representation)) {
            int fValue = (int) readLong(Representation.BIT_32).getValue();
            return Float.intBitsToFloat(fValue);
        } else {
            long lValue = readLong(representation).getValue();
            return Double.longBitsToDouble(lValue);
        }
    }

    /**
     * Writes boolean.
     *
     * @param value the value of the boolean which should be written.
     * @throws IOException during i/o error.
     */
    public void writeBoolean(boolean value) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        byte booleanValue = (byte) (value ? 1 : 0);
        buffer.put(booleanValue);
        buffer.flip();
        write(buffer, Representation.NONE);
    }

    /**
     * Reads boolean.
     *
     * @return boolean which should be read.
     * @throws IOException during i/o error.
     */
    public boolean readBoolean() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        readFull(buffer, Representation.NONE);
        buffer.flip();
        return buffer.get() == 1;
    }

    /**
     * Writes a given string.
     *
     * @param content  content which should be written.
     * @param encoding the char-set encoding if the content.
     * @throws IOException during i/o error.
     */
    public void writeString(String content, String encoding) throws IOException {
        CharacterChannel ch = new CharacterChannel(this.channel, encoding);
        ch.write(content, 0);
    }

    /**
     * Reads a string for the specified number of bytes.
     *
     * @param nBytes   number of bytes representing the string.
     * @param encoding encoding which should be used to represent the string.
     * @return the encoded string value.
     * @throws IOException during i/o error.
     */
    public String readString(int nBytes, String encoding) throws IOException {
        CharacterChannel ch = new CharacterChannel(this.channel, encoding);
        return ch.readAllChars(nBytes);
    }

    /**
     * Specified whether the channel is selectable.
     *
     * @return true if the channel is selectable.
     */
    public boolean isSelectable() {
        return channel.isSelectable();
    }

    /**
     * Provides the id of the channel.
     *
     * @return the id of the channel.
     */
    public int id() {
        return channel.id();
    }

    /**
     * Close the channel.
     *
     * @throws IOException during i/o error.
     */
    public void close() throws IOException {
        this.channel.close();
    }

    @Override
    public boolean remaining() {
        return false;
    }
}
