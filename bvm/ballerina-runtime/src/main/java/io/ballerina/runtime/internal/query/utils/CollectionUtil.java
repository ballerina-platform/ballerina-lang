package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.*;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.*;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.query.pipeline.StreamPipeline;
import io.ballerina.runtime.internal.values.HandleValue;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;

public class CollectionUtil {
    private static final BString $VALUE$_FIELD = StringUtils.fromString("$value$");
    private static final BString $ERROR$_FIELD = StringUtils.fromString("$error$");

    public static void consumeStream(StreamPipeline pipeline) {
        pipeline.getStream().forEach(frame -> {
            BMap<BString, Object> result = frame.getRecord();
        });
    }

    public static BArray createArray(StreamPipeline pipeline, BArray array) {
        Stream<Frame> strm = pipeline.getStream();
        Object[] tmpArr = strm
                .map(frame -> frame.getRecord().get($VALUE$_FIELD))
                .toArray();

        if (tmpArr.length > 0) {
            array.unshift(tmpArr);
        }
        return array;
    }

    public static Object collectQuery(Object pipeline) {
        if(pipeline instanceof StreamPipeline streamPipeline) {
            Stream<Frame> strm = streamPipeline.getStream();
            Optional<Object> result = strm
                    .map(frame -> frame.getRecord().get($VALUE$_FIELD))
                    .filter(Objects::nonNull)
                    .findFirst();

            return result.orElse(null);
        }

        return pipeline;
    }

    public static Object toString(StreamPipeline pipeline) {
        Stream<Frame> strm = pipeline.getStream();

        try {
            return strm
                    .map(frame -> frame.getRecord().get($VALUE$_FIELD))
                    .map(string -> (BString) string)
                    .reduce(StringUtils.fromString(""), BString::concat);
        } catch (BError e) {
            return e;
        }
    }

    public static BTable createTable(StreamPipeline pipeline, BTable table) {
        Stream<Frame> strm = pipeline.getStream();
//        TableType tableType = (TableType) pipeline.getConstraintType().getDescribingType();
//        BTable table = ValueCreator.createTableValue(tableType);

        strm.forEach(frame -> {
            BMap<BString, Object> record = (BMap<BString, Object>) frame.getRecord().get($VALUE$_FIELD);
            table.add(record);
        });

        return table;
    }

    public static Object createTableForOnConflict(StreamPipeline pipeline, BTable table) {
        Stream<Frame> strm = pipeline.getStream();
//        TableType tableType = (TableType) pipeline.getConstraintType().getDescribingType();
//        BTable table = ValueCreator.createTableValue(tableType);

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

        return error.orElse(null) != null ? error.get() : table;
    }

    public static BMap<BString, Object> createMap(StreamPipeline pipeline, BMap<BString, Object> map) {
        Stream<Frame> strm = pipeline.getStream();
//        MapType mapType = (MapType) pipeline.getConstraintType().getDescribingType();
//        BMap<BString, Object> map = ValueCreator.createMapValue(mapType);

        strm.forEach(frame -> {
            BArray record = (BArray) frame.getRecord().get($VALUE$_FIELD);
            BString key = (BString) record.get(0);
            Object value = record.get(1);
            map.put(key, value);
        });

        return map;
    }

    public static Object createMapForOnConflict(StreamPipeline pipeline, BMap<BString, Object> map) {
        Stream<Frame> strm = pipeline.getStream();
//        MapType mapType = (MapType) pipeline.getConstraintType().getDescribingType();
//        BMap<BString, Object> map = ValueCreator.createMapValue(mapType);

        Optional<BError> error = strm
                .map(frame -> {
                    BMap<BString, Object> record = frame.getRecord();
                    BArray recordArray = (BArray) record.get($VALUE$_FIELD);

                    BString key = (BString) recordArray.get(0);
                    if (map.containsKey(key) && record.get($ERROR$_FIELD) instanceof BError) {
                        return (BError) record.get($ERROR$_FIELD);
                    }
                    Object value = recordArray.get(1);

                    map.put(key, value);
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst();

        return error.orElse(null) != null ? error.get() : map;
    }

    public static BXml createXML(StreamPipeline pipeline) {
        Stream<Frame> strm = pipeline.getStream();

        String xmlStr = strm
                .map(frame -> frame.getRecord().get($VALUE$_FIELD).toString())
                .reduce("", String::concat);

        return ValueCreator.createXmlValue(xmlStr);
    }

    public static BStream toStream(StreamPipeline pipeline) {
        StreamType streamType = TypeCreator.createStreamType(pipeline.getConstraintType().getDescribingType(), pipeline.getCompletionType().getDescribingType());
        Object pipelineObj = pipeline.getStream().iterator();
        HandleValue handleValue = new HandleValue(pipelineObj);
        BObject iteratorObj = ValueCreator.createObjectValue(BALLERINA_QUERY_PKG_ID, "_IteratorObject", handleValue);

        return ValueCreator.createStreamValue(streamType, iteratorObj);
    }
}
