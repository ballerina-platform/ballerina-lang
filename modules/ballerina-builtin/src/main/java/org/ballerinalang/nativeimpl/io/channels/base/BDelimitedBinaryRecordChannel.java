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

import java.nio.ByteBuffer;

/**
 * Handles delimited binary records
 */
public class BDelimitedBinaryRecordChannel {

    /**
     * Channel for reading bytes
     */
    private AbstractChannel channel;

    /**
     * record delimited of the byte stream
     */
    private final byte[] delimiter;

    /**
     * A rough character could which will contain a record
     */
    private int recordByteCount = 10;


    /**
     * Holds the byte buffer content
     */
    private BByteBuffer contentBuffer = new BByteBuffer(0);

    public BDelimitedBinaryRecordChannel(AbstractChannel channel, byte[] delimiter) {
        this.channel = channel;
        this.delimiter = delimiter.clone();
    }

    /**
     * <p>
     * Finds matching record in the byte stream
     * </p>
     * <p>
     * When performing this operation the byte buffer position will be moved, when the function returns it will be at
     * it's last read position
     * </p>
     *
     * @param content buffer which holds the byte stream
     * @return true if matching record is found in the buffer
     */
    private boolean findRecord(ByteBuffer content) {
        boolean foundRecord = false;
        int matchingValueCount = 0;
       // content.flip();
        while (content.hasRemaining() && !foundRecord) {
            byte value = content.get();
            if (value == delimiter[matchingValueCount]) {
                //Each time when a matching values is found
                matchingValueCount++;
            }
            if (matchingValueCount >= delimiter.length) {
                foundRecord = true;
            }
        }
        return foundRecord;
    }

    /**
     * <p>
     * Will retrieve the bytes from the record
     * </p>
     *
     * @param recordBuffer buffer which contains the content read from the channel
     * @param recordLength the length of the record
     * @return the record extracted
     */
    private byte[] extractRecord(ByteBuffer recordBuffer, int recordLength) {
        byte[] record = new byte[recordLength];
        System.arraycopy(recordBuffer.array(), 0, record, 0, recordLength);
        return record;
    }


    /**
     * Reads a record from the channel
     *
     * @return record value
     */
    public byte[] read() {
        byte[] record = null;
        int readIterationCount = 1;
        do {
            ByteBuffer recordBuffer = contentBuffer.get(recordByteCount, channel);
            boolean recordStatus = findRecord(recordBuffer);
            int readLimit = recordBuffer.limit();
            readIterationCount++;
            if (recordStatus) {
                //If the record was found
                int recordLength = recordBuffer.position() - delimiter.length;
                record = extractRecord(recordBuffer, recordLength);
                int unprocessedByteCount = recordBuffer.limit() - recordBuffer.position();
                contentBuffer.reverse(unprocessedByteCount);
            } else if (readLimit < recordByteCount) {
                //This would mean there're no data left in the channel for more reads
                //The existing data does not contain a record either
                record = recordBuffer.array();
            } else {
                //We need to reverse what was read
                contentBuffer.reverse(readLimit);
                //We increment the buffer size
                recordByteCount = recordByteCount * readIterationCount;
            }
        } while (record == null);
        return record;
    }

    /**
     * Writes the record content to the channel
     *
     * @param content the content which should be written to the channel
     */
    public void write(byte[] content) {
        if (null != content) {
            int recordLength = content.length + delimiter.length;
            ByteBuffer record = ByteBuffer.allocate(recordLength);
            record.put(content);
            record.put(delimiter);
            record.flip();
            channel.write(record);
        } else {
            throw new BallerinaIOException("The content provided cannot be null");
        }
    }
}
