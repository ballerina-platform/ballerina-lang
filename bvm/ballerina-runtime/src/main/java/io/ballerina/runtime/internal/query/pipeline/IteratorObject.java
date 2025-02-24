package io.ballerina.runtime.internal.query.pipeline;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;

import java.util.Iterator;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;

public class IteratorObject {
    private static final BString VALUE_FIELD = StringUtils.fromString("$value$");

    public static Object next(Object pipeline) {
        Type constraintType = ((StreamPipeline) pipeline).getConstraintType().getDescribingType();
        Type completionType = ((StreamPipeline) pipeline).getCompletionType().getDescribingType();
        try {
            Iterator<Frame> iterator = ((StreamPipeline) pipeline).getStream().iterator();

            if (iterator.hasNext()) {
                Frame frame = iterator.next();
                BMap<BString, Object> recordMap = frame.getRecord();
                if (recordMap.isEmpty()) {
                    throw new RuntimeException("Error occurred while iterating over the stream: " +
                            "record fields cannot be empty");
                }
                Object value = recordMap.get(VALUE_FIELD);
                BMap<BString, Object> record = ValueCreator.createRecordValue(BALLERINA_QUERY_PKG_ID, "nextRecord");
                record.put(StringUtils.fromString("value"), value);
                return record;
            }
        } catch (Exception e) {
//            throw new RuntimeException("Error occurred while iterating over the stream: " + e.getMessage());
            return null;
        }

        return null;
    }
}
