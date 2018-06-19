/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.nativeimpl.io.channels.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Represents a channel which will allow performing data i/o operations.
 */
public class DataChannel {
    /**
     * Source for reading bytes.
     */
    private Channel channel;
    /**
     * Represents network byte order.
     */
    private ByteOrder order;

    public DataChannel(Channel channel, ByteOrder order) {
        this.channel = channel;
        this.order = order;
    }

    /**
     * Recursively read bytes until the buffer is filled.
     *
     * @param buffer buffer which will hold the read bytes.
     * @throws IOException during i/o error.
     */
    private void readFull(ByteBuffer buffer) throws IOException {
        do {
            channel.read(buffer);
        } while (buffer.hasRemaining() && !channel.hasReachedEnd());
    }

    /**
     * Decodes the long from a provided input channel.
     *
     * @param representation specified size representation of the long value.
     * @return the decoded long value.
     * @throws IOException during i/o error.
     */
    private long decodeLong(Representation representation) throws IOException {
        ByteBuffer buffer;
        int requiredNumberOfBytes;
        if (Representation.VARIABLE.equals(representation)) {
            throw new UnsupportedOperationException();
        } else {
            requiredNumberOfBytes = representation.getNumberOfBytes();
            buffer = ByteBuffer.allocate(requiredNumberOfBytes);
            buffer.order(order);
        }
        readFull(buffer);
        buffer.flip();
        return deriveLong(representation, buffer);
    }

    /**
     * Merge bytes and encodes long.
     *
     * @param representation the capacity of the long value i.e whether it's 32bit, 64bit.
     * @param buffer         holds the bytes which represents the long.
     * @return the value of long.
     */
    private long deriveLong(Representation representation, ByteBuffer buffer) {
        long value = 0;
        int maxNumberOfBits = 0xFFFF;
        int totalNumberOfBits = (buffer.limit() - 1) * representation.getBase();
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
            }
            maxNumberOfBits = 0xFF;
            value = value + shiftedValue;
            totalNumberOfBits = totalNumberOfBits - representation.getBase();
        } while (buffer.hasRemaining());
        return value;
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
            nBytes = (int) Math.abs(Math.round((Math.log(Math.abs(value)) / Math.log(2)) / Byte.SIZE));
            content = new byte[nBytes];
        } else {
            nBytes = representation.getNumberOfBytes();
            content = new byte[representation.getNumberOfBytes()];
        }
        totalNumberOfBits = (nBytes * representation.getBase()) - representation.getBase();
        for (int count = 0; count < nBytes; count++) {
            content[count] = (byte) (value >> totalNumberOfBits);
            totalNumberOfBits = totalNumberOfBits - representation.getBase();
        }
        return content;
    }

    /**
     * Writes fixed size long value.
     *
     * @param value          the value of the long which should be written.
     * @param representation the size of the long in bits.
     * @throws IOException during i/o error.
     */
    public void writeFixedLong(long value, Representation representation) throws IOException {
        byte[] bytes = encodeLong(value, representation);
        channel.write(ByteBuffer.wrap(bytes));
    }

    /**
     * Reads fixed size long.
     *
     * @param representation size of the long in bits.
     * @return the long value which is read.
     * @throws IOException during i/o error.
     */
    public long readFixedLong(Representation representation) throws IOException {
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
        writeFixedLong(lValue, representation);
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
            int fValue = (int) readFixedLong(Representation.BIT_32);
            return Float.intBitsToFloat(fValue);
        } else {
            long lValue = readFixedLong(representation);
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
        channel.write(buffer);
    }

    /**
     * Reads boolean.
     *
     * @return boolean which should be read.
     * @throws IOException during i/o error.
     */
    public boolean readBoolean() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        readFull(buffer);
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
     * Close the channel.
     *
     * @throws IOException during i/o error.
     */
    public void close() throws IOException {
        this.channel.close();
    }
}
