package io.ballerina.runtime.internal.query.pipeline;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.util.stream.Stream;

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
        try {
            // Iterate through the stream
//            Optional<Object> result = strm
//                    .map(frame -> frame.getRecord().get(StringUtils.fromString("value")))
//                    .filter(value -> value != null)
//                    .findFirst();
//
//            if (result.isPresent()) {
                // Create and return the record {| Type value; |} with the matching value
                BMap<BString, Object> record = ValueCreator.createRecordValue(
                        strm.
                                findFirst().get().getRecord());
                return record;
//            }
        } catch (Exception e) {
            // Handle any exceptions as an error
            return null;
        }
    }
}
