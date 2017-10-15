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

import org.ballerinalang.model.values.BStringArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * <p>
 * Will allow reading a channel into records
 * </p>
 * <p>
 * A readRecord will have a readRecord separator and a field separator
 * This is a stateful channel
 * </p>
 */
public class BTextRecordChannel {

    /**
     * Distinguishes the entire readRecord
     */
    private String recordSeparator;

    /**
     * Field is a granular term of readRecord
     */
    private String fieldSeparator;

    /**
     * Once the record is being identified the remaining string would hold the remaining elements
     */
    private StringBuilder persistentCharSequence;

    /**
     * A rough character could which will contain a record
     */
    private int recordCharacterCount = 1024;

    /**
     * Reads the character stream
     */
    private BCharacterChannel channel;

    /**
     * Whether there're remaining records in the channel
     */
    private boolean remaining = true;

    /**
     * Keeps track of the number of records which is being read through the channel
     */
    private int numberOfRecordsReadThroughChannel = 0;

    /**
     * Keeps track on the number of records written to channel
     */
    private int numberOfRecordsWrittenToChannel = 0;

    private static final Logger log = LoggerFactory.getLogger(BTextRecordChannel.class);


    public BTextRecordChannel(BCharacterChannel channel, String recordSeparator, String fieldSeparator) {
        this.recordSeparator = recordSeparator;
        this.fieldSeparator = fieldSeparator;
        this.channel = channel;
        this.persistentCharSequence = new StringBuilder();
    }

    /**
     * <p>
     * Initially the number of characters which would be contained in a record will be speculated, if the record
     * size is not adequate the size needs to be increased
     * </p>
     *
     * @param numberOfRecordHops number of reads done per record with the current recordCharacterCount
     */
    private void increaseRecordCharacterCount(int numberOfRecordHops) {
        int numberOfCharactersForRecord = numberOfRecordHops * this.recordCharacterCount;

        if (log.isDebugEnabled()) {
            log.debug("Character count in the record is increased from " + this.recordCharacterCount + " to " +
                    numberOfCharactersForRecord);
        }

        this.recordCharacterCount = numberOfCharactersForRecord;
    }

    /**
     * <p>
     * Gets record from specified sequence of characters
     * </p>
     * <p>
     * The record will be delimited based on the specified delimiter
     * </p>
     *
     * @return the requested record
     * @throws IOException during I/O error
     */
    private String readRecord() throws IOException {

        String record = null;
        String readCharacters = "";
        int numberOfChannelReads = 0;
        final int minimumRecordCount = 1;
        final int numberOfSplits = 2;
        final int delimitedRecordIndex = 0;
        final int minimumRemainingLength = 0;
        final int delimitedRemainingIndex = 1;
        final int recordThresholdIncreaseCount = 1;

        do {

            if (log.isTraceEnabled()) {
                log.trace("char[] remaining in memory " + persistentCharSequence);
            }

            //We need to split the string into 2
            String[] delimitedRecord = persistentCharSequence.toString().split(recordSeparator, numberOfSplits);


            if (delimitedRecord.length > minimumRecordCount) {
                record = delimitedRecord[delimitedRecordIndex];
                persistentCharSequence.setLength(minimumRemainingLength);
                persistentCharSequence.append(delimitedRecord[delimitedRemainingIndex]);

                if (log.isTraceEnabled()) {
                    log.trace("Record identified from remaining char[] in memory " + record);
                    log.trace("The char[] left after split " + persistentCharSequence);
                }

            } else {

                readCharacters = channel.read(recordCharacterCount);
                numberOfChannelReads++;


                if (log.isTraceEnabled()) {
                    log.trace("char [] read from channel," + channel.hashCode() + "=" + readCharacters);
                }

                persistentCharSequence.append(readCharacters);

                if (log.isTraceEnabled()) {
                    log.trace("char [] appended to the memory " + persistentCharSequence);
                }
            }

        } while (record == null && !readCharacters.isEmpty());

        if (null == record && readCharacters.isEmpty()) {
            //This means there's no more to be read as records
            if (log.isDebugEnabled()) {
                log.debug("The content returned from the channel " + channel.hashCode() + " is <void>");
            }
            //This means this will be the last record which could be read
            this.remaining = false;
            //If there're any remaining characters left we provide it as the last record
            if (persistentCharSequence.length() > minimumRemainingLength) {
                record = persistentCharSequence.toString();
                if (log.isTraceEnabled()) {
                    log.trace("char [] remaining in memory, will be marked as the last record " + record);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Final record is read from channel " + channel.hashCode() + " number of records read " +
                        "from channel " + (numberOfRecordsReadThroughChannel + 1));
            }
        } else {

            if (numberOfChannelReads > recordThresholdIncreaseCount) {
                //This means a record exceeds the currently specified number of characters per record and we need to
                // increase it
                increaseRecordCharacterCount(numberOfChannelReads);
            }

        }

        return record;
    }

    /**
     * Get the fields identified through the record
     *
     * @param record the record which contains all the fields
     * @return fields which are separated as records
     */
    private String[] getFields(String record) {
        return record.split(fieldSeparator);
    }

    /**
     * <p>
     * Read the next readRecord
     * </p>
     * <p>
     * An empty list will be returned if all the records have being processed, all records will be marked as
     * processed if all the content have being read from the provided channel
     * </p>
     *
     * @return the list of fields
     */
    public String[] read() throws IOException {

        final int emptyArrayIndex = 0;

        String[] fields = new String[emptyArrayIndex];

        if (remaining) {

            if (log.isDebugEnabled()) {
                log.debug("Reading record " + numberOfRecordsReadThroughChannel + " from " + channel.hashCode());
            }

            String record = readRecord();


            if (null != record) {
                fields = getFields(record);
                numberOfRecordsReadThroughChannel++;

                if (log.isDebugEnabled()) {
                    log.debug("Record " + numberOfRecordsReadThroughChannel + " returned " + fields.length + " from " +
                            "channel " + channel.hashCode());
                }

                if (log.isTraceEnabled()) {
                    log.trace("The list of fields identified in record " + numberOfRecordsReadThroughChannel + "from " +
                            "channel " + channel.hashCode() + "," + Arrays.toString(fields));
                }

            }
        } else {

            //The channel could be null if it's being closed by a different source
            if (null != channel) {
                log.warn("The final record has already being processed through the channel " + channel.hashCode());
            } else {
                log.warn("The requested channel has already being closed");
            }
        }

        return fields;
    }

    /**
     * Will place the relevant fields together to form a record
     *
     * @param fields the list of fields in the record
     * @return the record constructed through the fields
     */
    private String composeRecord(BStringArray fields) {
        StringBuilder recordConsolidator = new StringBuilder();
        String finalizedRecord;
        long numberOfFields = fields.length();
        final int fieldStartIndex = 0;
        final long secondLastFieldIndex = numberOfFields - 1;

        if (log.isDebugEnabled()) {
            log.debug("Number of fields to be composed " + numberOfFields);
        }

        for (int fieldCount = fieldStartIndex; fieldCount < numberOfFields; fieldCount++) {

            String currentFieldString = fields.get(fieldCount);
            recordConsolidator.append(currentFieldString);

            if (fieldCount < secondLastFieldIndex) {
                //The idea here is to omit appending the field separator after the final field
                recordConsolidator.append(fieldSeparator);
            }
        }

        finalizedRecord = recordConsolidator.toString();

        return finalizedRecord;
    }

    /**
     * Writes a given record to a file
     *
     * @param fields the list of fields composing the record
     * @throws IOException during I/O error
     */
    public void write(BStringArray fields) throws IOException {
        final int writeOffset = 0;

        String record = composeRecord(fields);
        record = record + recordSeparator;

        if (log.isTraceEnabled()) {
            log.trace("The record " + numberOfRecordsWrittenToChannel + " composed for writing, " + record);
        }

        channel.write(record, writeOffset);

        if (log.isDebugEnabled()) {
            log.debug("Record " + numberOfRecordsReadThroughChannel + " written to the channel " + channel.hashCode());
        }

        numberOfRecordsWrittenToChannel++;

    }

    /**
     * Closes the record channel
     */
    public void close() {
        channel.close();
    }
}
