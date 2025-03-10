package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.*;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.*;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.query.pipeline.StreamPipeline;
import io.ballerina.runtime.internal.values.HandleValue;
import io.ballerina.runtime.internal.xml.XmlFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;

public class CollectionUtil {
    private static final BString VALUE_FIELD = StringUtils.fromString("value");
    private static final BString $VALUE$_FIELD = StringUtils.fromString("$value$");
    private static final BString $ERROR$_FIELD = StringUtils.fromString("$error$");

//    public static Object consumeStream(StreamPipeline pipeline) {
//        Stream<Frame> stream = pipeline.getStream();
//        var iterator = stream.iterator();
//
//        while (iterator.hasNext()) {
//            BMap<BString, Object> record = iterator.next().getRecord();
//            if (record.containsKey(VALUE_FIELD) && record.get(VALUE_FIELD) != null) {
//                return record.get(VALUE_FIELD);
//            }
//        }
//
//        return null;
//    }

    public static Object createArray(StreamPipeline pipeline, BArray array) {
        try {
            Stream<Frame> strm = pipeline.getStream();
            Object[] tmpArr = strm
                    .map(frame -> frame.getRecord().get($VALUE$_FIELD))
                    .toArray();

            if (tmpArr.length > 0) {
                array.unshift(tmpArr);
            }
            return array;
        } catch (BError e) {
//            return ErrorCreator.createError(BALLERINA_QUERY_PKG_ID, "CompleteEarlyError", e.getErrorMessage(), e, (BMap<BString, Object>) e.getDetails());
//            return ErrorCreator.createDistinctError("Error", BALLERINA_QUERY_PKG_ID, e.getErrorMessage());
//            return e;
//            return ErrorCreator.createError(e.getErrorMessage(), e);
            return e;
        }
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

    public static Object createTable(StreamPipeline pipeline, BTable table) {
        Stream<Frame> strm = pipeline.getStream();

        try {
            strm.forEach(frame -> {
                BMap<BString, Object> record = (BMap<BString, Object>) frame.getRecord().get($VALUE$_FIELD);
                table.add(record);
            });

            return table;
        } catch (BError e) {
            return e;
        }
    }

    public static Object createTableForOnConflict(StreamPipeline pipeline, BTable table) {
        Stream<Frame> strm = pipeline.getStream();

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

    public static Object createMap(StreamPipeline pipeline, BMap<BString, Object> map) {
        Stream<Frame> strm = pipeline.getStream();

        try {
            strm.forEach(frame -> {
                BArray record = (BArray) frame.getRecord().get($VALUE$_FIELD);
                BString key = (BString) record.get(0);
                Object value = record.get(1);
                map.put(key, value);
            });

            return map;
        } catch (BError e) {
            return e;
        }
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


    public static BStream toStream(StreamPipeline pipeline) {
        StreamType streamType = TypeCreator.createStreamType(pipeline.getConstraintType().getDescribingType(), pipeline.getCompletionType().getDescribingType());
        Object pipelineObj = pipeline.getStream().iterator();
        HandleValue handleValue = new HandleValue(pipelineObj);
        BObject iteratorObj = ValueCreator.createObjectValue(BALLERINA_QUERY_PKG_ID, "_IteratorObject", handleValue);

        return ValueCreator.createStreamValue(streamType, iteratorObj);
    }

    public static Object createXML(StreamPipeline pipeline) {
        Stream<Frame> strm = pipeline.getStream();

        try {
            Object[] xmlArray = strm
                    .map(frame -> frame.getRecord().get($VALUE$_FIELD))
                    .filter(Objects::nonNull)
                    .toArray();

            return concatXML(xmlArray);
        } catch (BError e) {
            return e;
        }
    }

    public static BXml concatXML(Object... arrayValue) {
        List<BXml> backingArray = new ArrayList<>();
        BXml lastItem = null;
        for (Object refValue : arrayValue) {
            if (refValue instanceof BString bString) {
                if (lastItem != null && lastItem.getNodeType() == XmlNodeType.TEXT) {
                    // If last added item is a string, then concat prev values with this values and replace prev value.
                    BString concat = StringUtils.fromString(lastItem.getTextValue()).concat(bString);
                    BXml xmlText = XmlFactory.createXMLText(concat);
                    backingArray.set(backingArray.size() - 1, xmlText);
                    lastItem = xmlText;
                    continue;
                }
                BXml xmlText = XmlFactory.createXMLText(bString);
                backingArray.add(xmlText);
                lastItem = xmlText;
            } else if (refValue instanceof BXmlSequence bXmlSequence) {
                backingArray.addAll(bXmlSequence.getChildrenList());
                lastItem = (BXml) refValue;
            } else {
                backingArray.add((BXml) refValue);
                lastItem = (BXml) refValue;
            }
        }
        return ValueCreator.createXmlSequence(backingArray);
    }
}
