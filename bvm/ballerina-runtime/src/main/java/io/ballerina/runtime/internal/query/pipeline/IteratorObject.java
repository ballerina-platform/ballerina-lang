package io.ballerina.runtime.internal.query.pipeline;

import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.query.utils.QueryErrorValue;
import io.ballerina.runtime.internal.query.utils.QueryException;

import java.util.Iterator;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;

public class IteratorObject {
    private static final BString VALUE_FIELD = StringUtils.fromString("$value$");

    public static Object next(Object itr) {
        Iterator<Frame> iterator = (Iterator<Frame>) itr;

        try {
            if (iterator.hasNext()) {
                switch (iterator.next()) {
                    case ErrorFrame errorFrame:
                        return errorFrame.getError();
                    case Frame frame:
                        BMap<BString, Object> recordMap = frame.getRecord();
                        Object value = recordMap.get(VALUE_FIELD);
                        if (value instanceof BError error) {
                            return error;
                        }
                        BMap<BString, Object> record = ValueCreator.createRecordValue(BALLERINA_QUERY_PKG_ID, "nextRecord");
                        record.put(StringUtils.fromString("value"), value);
                        return record;
                }
            }
        } catch (QueryException e) {
            return e.getError();
        }

        return null;
    }
}
