package io.ballerina.runtime.internal.query.pipeline;

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;

/**
 * Represents a frame that wraps elements as Ballerina records (BMap).
 */
public class Frame {

    private final BMap<BString, Object> record;

    /**
     * Constructor to create a frame from a Ballerina record (BMap).
     *
     * @param record The Ballerina record to wrap.
     */
    public Frame(BMap<BString, Object> record) {
        this.record = record;
    }

    /**
     * Static method to create a `_Frame` from an element.
     *
     * @param key   The key for the record.
     * @param value The value for the record.
     * @return A `_Frame` wrapping the BMap.
     */
    public static Frame add(BString key, Object value) {
        RecordType recordType = TypeCreator.createRecordType("_Frame", BALLERINA_QUERY_PKG_ID, 1, true, 0);
        BMap<BString, Object> record = ValueCreator.createRecordValue(recordType);
        record.put(key, value);
        return new Frame(record);
    }

    /**
     * Gets the Ballerina record (BMap) wrapped by the frame.
     *
     * @return The Ballerina record.
     */
    public BMap<BString, Object> getRecord() {
        return record;
    }

    @Override
    public String toString() {
        return "Frame{" +
                "record=" + record +
                '}';
    }
}
