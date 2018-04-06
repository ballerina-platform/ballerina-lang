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
import org.ballerinalang.nativeimpl.io.BallerinaIOException;
import org.ballerinalang.nativeimpl.io.csv.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * <p>
 * Allows performing record I/O operations.
 * </p>
 * <p>
 * A readRecord will have a readRecord separator and a field separator.
 * </p>
 * <p>
 * <b>Note : </b> this channel does not support concurrent operations, since underlying CharacterChannel is not
 * synchronous.
 * </p>
 */
public class DelimitedRecordChannel {

    /**
     * Distinguishes the Record.
     */
    private String recordSeparator;

    /**
     * Record contains multiple fields, each field is separated through the field separator.
     */
    private String fieldSeparator;

    /**
     * Once the record is being identified the remaining string would hold the remaining elements.
     */
    private StringBuilder persistentCharSequence;

    /**
     * A rough character count which will contain a record. This will be resized dynamically if the length of the
     * record is long.
     */
    private int recordCharacterCount = 100;

    /**
     * Read/Writes characters.
     */
    private CharacterChannel channel;

    /**
     * <p>
     * Specified whether there're any remaining records left to be read from the channel.
     * </p>
     * <p>
     * This will be false if there're no characters remaining in the persistentCharSequence and the the channel has
     * reached EoF
     * </p>
     */
    private boolean remaining = true;

    /**
     * Keeps track of the number of records which is being read through the channel.
     */
    private int numberOfRecordsReadThroughChannel = 0;

    /**
     * Keeps track of the number of records written to channel.
     */
    private int numberOfRecordsWrittenToChannel = 0;

    /**
     * Specifies the format for the record. This will be optional
     */
    private Format format;

    private static final Logger log = LoggerFactory.getLogger(DelimitedRecordChannel.class);

    public DelimitedRecordChannel(CharacterChannel channel, Format format) {
        this.channel = channel;
        this.format = format;
        this.persistentCharSequence = new StringBuilder();
    }

    public DelimitedRecordChannel(CharacterChannel channel, String recordSeparator, String fieldSeparator) {
        this.recordSeparator = recordSeparator;
        this.fieldSeparator = fieldSeparator;
        this.channel = channel;
        this.persistentCharSequence = new StringBuilder();
    }

    /**
     * Retrieves the record separator for reading records.
     *
     * @return the record separator.
     */
    private String getRecordSeparatorForReading() {
        if (null == format) {
            return recordSeparator;
        }
        return format.getReadRecSeparator();
    }

    /**
     * Retrieves field separator for reading.
     *
     * @return the field separator.
     */
    private String getFieldSeparatorForReading() {
        if (null == format) {
            return fieldSeparator;
        }
        return format.getReadFieldSeparator();
    }

    /**
     * Retrieves record separator for writing.
     *
     * @return the record separator.
     */
    private String getRecordSeparatorForWriting() {
        if (null == format) {
            return recordSeparator;
        }
        return format.getWriteRecSeparator();
    }

    /**
     * Retrieves field separator for writing.
     *
     * @return the field separator.
     */
    private String getFieldSeparatorForWriting() {
        if (null == format) {
            return fieldSeparator;
        }
        return format.getWriteFieldSeparator();
    }

    /**
     * <p>
     * Gets record from specified sequence of characters.
     * </p>
     *
     * @return the requested record.
     * @throws BallerinaIOException during I/O error.
     */
    private String readRecord() throws BallerinaIOException, IOException {
        String record = null;
        String readCharacters = "";
        final int minimumRecordCount = 1;
        final int numberOfSplits = 2;
        do {
            if (log.isTraceEnabled()) {
                log.trace("char[] remaining in memory " + persistentCharSequence);
            }
            //We need to split the string into 2
            String[] delimitedRecord = persistentCharSequence.toString().
                    split(getRecordSeparatorForReading(), numberOfSplits);
            if (delimitedRecord.length > minimumRecordCount) {
                record = processIdentifiedRecord(delimitedRecord);
                int recordCharacterLength = record.length();
                if (recordCharacterLength > recordCharacterCount) {
                    recordCharacterCount = record.length();
                }
            } else {
                readCharacters = readRecordFromChannel();
            }
        } while (record == null && !readCharacters.isEmpty());

        if (null == record) {
            record = readFinalRecord();
        }
        return record;
    }

    /**
     * <p>
     * Reads the remaining set of characters as the final record.
     * </p>
     * <p>
     * This operation is called when there're no more content to be retrieved from the the channel.
     * </p>
     */
    private String readFinalRecord() {
        final int minimumRemainingLength = 0;
        String record = "";
        //This means there's no more to be get as records
        if (log.isDebugEnabled()) {
            log.debug("The content returned from the channel " + channel.hashCode() + " is <void>");
        }
        //This means this will be the last record which could be get
        this.remaining = false;
        //If there're any remaining characters left we provide it as the last record
        if (persistentCharSequence.length() > minimumRemainingLength) {
            record = persistentCharSequence.toString();
            //Once the final record is processed there will be no chars left
            persistentCharSequence.setLength(minimumRemainingLength);
            if (log.isTraceEnabled()) {
                log.trace("char [] remaining in memory, will be marked as the last record " + record);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Final record is get from channel " + channel.hashCode() + " number of records get " +
                    "from channel " + (numberOfRecordsReadThroughChannel + 1));
        }
        return record;
    }

    /**
     * <p>
     * Reads a record from the channel.
     * </p>
     *
     * @return the record content.
     */
    private String readRecordFromChannel() throws IOException {
        String readCharacters;
        readCharacters = channel.read(recordCharacterCount);
        if (log.isTraceEnabled()) {
            log.trace("char [] get from channel," + channel.hashCode() + "=" + readCharacters);
        }
        persistentCharSequence.append(readCharacters);
        if (log.isTraceEnabled()) {
            log.trace("char [] appended to the memory " + persistentCharSequence);
        }
        return readCharacters;
    }

    /**
     * <p>
     * Identifies the record from the provided collection.
     * </p>
     * <p>
     * <b>Note :</b> This operation would append the remaining content to the string.
     * </p>
     *
     * @param delimitedRecords collection of records which required to be split.
     * @return the record content value.
     */
    private String processIdentifiedRecord(String[] delimitedRecords) {
        String record;
        final int minimumRemainingLength = 0;
        final int delimitedRecordIndex = 0;
        final int delimitedRemainingIndex = 1;
        String recordContent = delimitedRecords[delimitedRemainingIndex];
        record = delimitedRecords[delimitedRecordIndex];
        persistentCharSequence.setLength(minimumRemainingLength);
        persistentCharSequence.append(recordContent);
        if (log.isTraceEnabled()) {
            log.trace("Record identified from remaining char[] in memory " + record);
            log.trace("The char[] left after split " + persistentCharSequence);
        }
        return record;
    }

    /**
     * Get the fields identified through the record.
     *
     * @param record the record which contains all the fields.
     * @return fields which are separated as records.
     */
    private String[] getFields(String record) {
        return record.split(getFieldSeparatorForReading());
    }

    /**
     * <p>
     * Read the next readRecord.
     * </p>
     * <p>
     * An empty list will be returned if all the records have being processed, all records will be marked as
     * processed if all the content have being retrieved from the provided channel.
     * </p>
     *
     * @return the list of fields.
     * @throws IOException during I/O error.
     */
    public String[] read() throws IOException {
        final int emptyArrayIndex = 0;
        String[] fields = new String[emptyArrayIndex];
        if (remaining) {
            if (log.isDebugEnabled()) {
                log.debug("Reading record " + numberOfRecordsReadThroughChannel + " from " + channel.hashCode());
            }
            String record = readRecord();
            if (!record.isEmpty() || remaining) {
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
     * Will place the relevant fields together to/form a record.
     *
     * @param fields the list of fields in the record.
     * @return the record constructed through the fields.
     */
    private String composeRecord(BStringArray fields) {
        StringBuilder recordConsolidator = new StringBuilder();
        String finalizedRecord;
        long numberOfFields = fields.size();
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
                recordConsolidator.append(getFieldSeparatorForWriting());
            }
        }
        finalizedRecord = recordConsolidator.toString();
        return finalizedRecord;
    }

    /**
     * Writes a given record to a file.
     *
     * @param fields the list of fields composing the record.
     * @throws IOException during I/O error.
     */
    public void write(BStringArray fields) throws IOException {
        final int writeOffset = 0;
        String record = composeRecord(fields);
        record = record + getRecordSeparatorForWriting();
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
     * Closes the record channel.
     *
     * @throws IOException error occur while closing the connection.
     */
    public void close() throws IOException {
        channel.close();
    }

    /**
     * Check whether there are more records or not.
     *
     * @return true if more records in the channel else false.
     * @throws IOException if an error occurs while reading from channel.
     */
    public boolean hasNext() throws IOException {
        if (remaining && persistentCharSequence.length() == 0) {
            //If this is the case we need to further verify whether there will be more bytes left to be read
            //Remaining can become false in the next iteration
            String readChars = readRecordFromChannel();
            if (readChars.isEmpty()) {
                remaining = false;
            }
        }
        return remaining;
    }
}
