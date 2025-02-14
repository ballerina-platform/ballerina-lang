package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.*;
import io.ballerina.runtime.internal.query.pipeline.Frame;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class CollectionUtil {
    private static final BString $VALUE$_FIELD = StringUtils.fromString("$value$");
    private static final BString $ERROR$_FIELD = StringUtils.fromString("$error$");

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

    public static Object createTableForOnConflict(Stream<Frame> strm, BTable table) {
        Optional<BError> error = strm
                .map(frame -> {
                    BMap<BString, Object> record = (BMap<BString, Object>) frame.getRecord().get($VALUE$_FIELD);
                    try {
                        table.add(record);
                    } catch (Exception e) {
                        if (frame.getRecord().get($ERROR$_FIELD) instanceof BError) {
                            return (BError) frame.getRecord().get($ERROR$_FIELD);
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst();

        return error.isPresent() ? error.get() : table;
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

    public static Object createMapForOnConflict(Stream<Frame> strm, BMap<BString, Object> map) {
        Optional<BError> error = strm
                .map(frame -> {
                    BMap<BString, Object> record = frame.getRecord();
                    BArray recordArray = (BArray) record.get($VALUE$_FIELD);

                    BString key = (BString) recordArray.get(0);
                    if(map.containsKey(key) && record.get($ERROR$_FIELD) instanceof BError) {
                        return (BError) record.get($ERROR$_FIELD);
                    }
                    Object value = recordArray.get(1);

                    map.put(key, value);
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst();

        return error.isPresent() ? error.get() : map;
    }


    public static BXml createXML(Stream<Frame> strm) {
        String xmlStr = strm
                .map(frame -> frame.getRecord().get($VALUE$_FIELD).toString())
                .reduce("", String::concat);
        return ValueCreator.createXmlValue(xmlStr);
    }
}
