package io.ballerina.runtime.internal.query.old;

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.stream.Stream;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;

/**
 * Consumes a stream of frames and processes each frame to extract a value.
 */
public class StreamConsumer {
    /**
     * Consumes a stream of frames, processes them, and returns the first matching value or an error.
     *
     * @param frameStream The input stream of frames.
     * @return A record with the value or an error.
     */
    public static BMap<BString, Object> consumeStream(Object frameStream) {
        Stream<Frame> strm = (Stream<Frame>) frameStream;
        BMap<BString , Object> result = strm.findFirst().get().getRecord();
        RecordType recordType = TypeCreator.createRecordType("_Frame", BALLERINA_QUERY_PKG_ID, 1, true, 0);
        BMap<BString , Object> record = ValueCreator.createRecordValue(BALLERINA_QUERY_PKG_ID, "_Frame", result);
//        BMap<BString , Object> record = ValueCreator.createRecordValue(recordType);
        return record;
    }
}
