package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.*;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.*;
import io.ballerina.runtime.internal.query.pipeline.ErrorFrame;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.query.pipeline.StreamPipeline;
import io.ballerina.runtime.internal.values.HandleValue;
import io.ballerina.runtime.internal.xml.XmlFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.Optional;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;

import static io.ballerina.runtime.internal.query.utils.QueryConstants.$ERROR$_FIELD;
import static io.ballerina.runtime.internal.query.utils.QueryConstants.$VALUE$_FIELD;

public class CollectionUtil {

    public static Object createArray(StreamPipeline pipeline, BArray array) {
        Stream<Frame> strm = pipeline.getStream();
        try {
            Iterator<Frame> it = strm.iterator();
            while (it.hasNext()) {
                switch (it.next()) {
                    case ErrorFrame errorFrame:
                        return errorFrame.getError();
                    case Frame frame:
                        array.append(frame.getRecord().get($VALUE$_FIELD));
                }
            }
            return array;
        } catch (QueryException e) {
            return e.getError();
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
        } catch (QueryException e) {
            return e.getError();
        }
    }

    public static Object createTable(StreamPipeline pipeline, BTable table) {
        Stream<Frame> strm = pipeline.getStream();

        try {
            strm.forEach(frame -> {
                BMap<BString, Object> record = (BMap<BString, Object>) frame.getRecord().get($VALUE$_FIELD);
                table.put(record);
            });

            return table;
        } catch (QueryException e) {
            return e.getError();
        }
    }

    public static Object createTableForOnConflict(StreamPipeline pipeline, BTable table) {
        Stream<Frame> strm = pipeline.getStream();

        try {
            Optional<BError> error = strm
                    .map(frame -> {
                        BMap<BString, Object> record = (BMap<BString, Object>) frame.getRecord().get($VALUE$_FIELD);
                        try {
                            table.add(record);
                        } catch (Exception e) {
                            if (frame.getRecord().get($ERROR$_FIELD) instanceof BError) {
                                return (BError) frame.getRecord().get($ERROR$_FIELD);
                            } else {
                                table.put(record);
                            }
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .findFirst();

            return error.orElse(null) != null ? error.get() : table;
        } catch (QueryException e) {
            return e.getError();
        }
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
        } catch (QueryException e) {
            return e.getError();
        }
    }

    public static Object createMapForOnConflict(StreamPipeline pipeline, BMap<BString, Object> map) {
        Stream<Frame> strm = pipeline.getStream();

        try {
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
        } catch (QueryException e) {
            return e.getError();
        }
    }


    public static Object toStream(StreamPipeline pipeline) {
        StreamType streamType = TypeCreator.createStreamType(pipeline.getConstraintType().getDescribingType(), pipeline.getCompletionType().getDescribingType());
        Object pipelineObj;
        try {
            pipelineObj = pipeline.getStream().iterator();
        } catch (QueryException e) {
            return e.getError();
        }
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
        } catch (QueryException e) {
            return e.getError();
        }
    }

    public static BXml concatXML(Object... arrayValue) {
        List<BXml> backingArray = new ArrayList<>();
        BXml lastItem = null;
        for (Object refValue : arrayValue) {
            if (refValue instanceof BString bString) {
                if (lastItem != null && lastItem.getNodeType() == XmlNodeType.TEXT) {
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
