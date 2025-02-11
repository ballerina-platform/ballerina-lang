package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class CollectionUtil {
    private static final BString VALUE_FIELD = StringUtils.fromString("$value$");

    public static void consumeStream(Object frameStream) {
        Stream<Frame> strm = (Stream<Frame>) frameStream;
        strm.forEach(frame -> {
            BMap<BString, Object> result = frame.getRecord();
        });
    }

    public static BArray createArray(Stream<Frame> strm, BArray arr) {
        Object[] tmpArr = strm
                .map(frame -> frame.getRecord().get(VALUE_FIELD))
                .toArray();

        arr = ValueCreator.createArrayValue(tmpArr, TypeCreator.createArrayType(PredefinedTypes.TYPE_ANY));
        return arr;
    }

    public static Object collectQuery(Stream<Frame> strm) {
        Optional<Object> result = strm
                .map(frame -> frame.getRecord().get(VALUE_FIELD))
                .filter(Objects::nonNull)
                .findFirst();

        return result.orElse(null);
    }
}
