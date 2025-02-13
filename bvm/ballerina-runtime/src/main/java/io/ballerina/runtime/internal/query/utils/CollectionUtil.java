package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class CollectionUtil {
    private static final BString $VALUE$_FIELD = StringUtils.fromString("$value$");

    public static void consumeStream(Object frameStream) {
        Stream<Frame> strm = (Stream<Frame>) frameStream;
        strm.forEach(frame -> {
            BMap<BString, Object> result = frame.getRecord();
        });
    }

    public static BArray createArray(Stream<Frame> strm, BArray arr) {
        Object[] tmpArr = strm
                .map(frame -> frame.getRecord().get($VALUE$_FIELD))
                .toArray();

        arr = ValueCreator.createArrayValue(tmpArr, TypeCreator.createArrayType(PredefinedTypes.TYPE_ANY));
        return arr;
    }

    public static Object collectQuery(Stream<Frame> strm) {
        Optional<Object> result = strm
                .map(frame -> frame.getRecord().get($VALUE$_FIELD))
                .filter(Objects::nonNull)
                .findFirst();

        return result.orElse(null);
    }

    public static BString toString(Stream<Frame> strm) {
        return strm
                .map(frame -> frame.getRecord().get($VALUE$_FIELD))
                .map(string -> (BString) string)
                .reduce(StringUtils.fromString(""), BString::concat);
    }

    public static BTable createTable(Stream<Frame> strm, BTable table) {
        strm.forEach(frame -> {
            BMap<BString, Object> record = (BMap<BString, Object>) frame.getRecord().get($VALUE$_FIELD);
            table.add(record);
        });
        return table;
    }

    public static BMap createMap(Stream<Frame> strm, BMap map) {
        strm.forEach(frame -> {
            BArray record = (BArray) frame.getRecord().get($VALUE$_FIELD);
            BString key = (BString) record.get(0);
            Object value = record.get(1);
            map.put(key, value);
        });
        return map;
    }
}
