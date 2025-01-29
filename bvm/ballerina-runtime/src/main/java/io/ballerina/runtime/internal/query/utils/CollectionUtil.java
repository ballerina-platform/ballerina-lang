package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.List;
import java.util.stream.Stream;

public class CollectionUtil {
    private static final BString VALUE_FIELD = StringUtils.fromString("$value$");

    public static BArray createArray(Stream<Frame> strm, BArray arr) {
        Object[] tmpArr = strm
                .map(frame -> frame.getRecord().get(VALUE_FIELD))
                .toArray();

        arr = ValueCreator.createArrayValue(tmpArr, TypeCreator.createArrayType(PredefinedTypes.TYPE_ANY));
        return arr;
    }

    public static BValue collectQuery(Stream<Frame> strm) {
        try {
            List<Object> collectedResult = strm
                    .map(frame -> frame.getRecord().get(VALUE_FIELD))
                    .toList();

            // Convert the List to a Ballerina Array
            return ValueCreator.createArrayValue(
                    collectedResult.toArray(),
                    TypeCreator.createArrayType(PredefinedTypes.TYPE_ANY)
            );
        } catch (Exception e) {
            return (BValue) e; // Consider wrapping this in an error type
        }
    }
}
