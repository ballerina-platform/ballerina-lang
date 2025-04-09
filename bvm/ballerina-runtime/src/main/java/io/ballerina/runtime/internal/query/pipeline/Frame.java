/*
 *  Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org)
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for
 *  the specific language governing permissions and limitations
 *  under the License.
 *
 */

package io.ballerina.runtime.internal.query.pipeline;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;

/**
 * Represents a frame that wraps elements as Ballerina records (BMap).
 *
 * @since 2201.13.0
 */
public class Frame {

    private BMap<BString, Object> frame;

    public Frame() {
        this.frame = ValueCreator.createRecordValue(BALLERINA_QUERY_PKG_ID, "_Frame");
    }

    /**
     * Constructor to create a frame from a Ballerina record (BMap).
     *
     * @param record The Ballerina record to wrap.
     */
    public Frame(BMap<BString, Object> record) {
        this.frame = record;
    }

    /**
     * Static method to create a `_Frame` from an element.
     *
     * @param key   The key for the record.
     * @param value The value for the record.
     * @return A `_Frame` wrapping the BMap.
     */
    public static Frame create(BString key, Object value) {
        BMap<BString, Object> record = ValueCreator.createRecordValue(BALLERINA_QUERY_PKG_ID, "_Frame");
        record.put(key, value);
        return new Frame(record);
    }

    /**
     * Static method to create a `_Frame` from a Ballerina record.
     *
     * @param record The Ballerina record to wrap.
     * @return A `_Frame` wrapping the BMap.
     */
    public static Frame create(BMap<BString, Object> record) {
        return new Frame(record);
    }

    /**
     * Updates the underlying record with a new record.
     * @param newRecord The new record to update.
     */
    public void updateRecord(BMap<BString, Object> newRecord) {
        this.frame = newRecord;
    }

    /**
     * Updates the underlying record with a new key-value pair.
     *
     * @param key   The key to update.
     * @param value The value to set.
     */
    public Frame updateRecord(BString key, Object value) {
        this.frame.put(key, value);
        return this;
    }

    /**
     * Gets the Ballerina record (BMap) wrapped by the frame.
     *
     * @return The Ballerina record.
     */
    public BMap<BString, Object> getRecord() {
        return frame;
    }
}
