/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.StreamType;
import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlSequence;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.query.pipeline.StreamPipeline;
import io.ballerina.runtime.internal.values.HandleValue;
import io.ballerina.runtime.internal.xml.XmlFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;
import static io.ballerina.runtime.internal.query.utils.QueryConstants.ERROR_FIELD;
import static io.ballerina.runtime.internal.query.utils.QueryConstants.VALUE_ACCESS_FIELD;
import static io.ballerina.runtime.internal.query.utils.QueryConstants.EMPTY_BSTRING;

/**
 * Contains methods that collect query streams.
 *
 * @since 2201.13.0
 */
public class CollectionUtil {

    public static Object createArray(StreamPipeline pipeline, BArray array) {
        Stream<Frame> strm = pipeline.getStream();
        try {
            Iterator<Frame> it = strm.iterator();
            while (it.hasNext()) {
                Frame frame = it.next();
                array.append(frame.getRecord().get(VALUE_ACCESS_FIELD));
            }
            return array;
        } catch (QueryException e) {
            return e.getError();
        }
    }

    public static Object collectQuery(Object pipeline) {
        if (pipeline instanceof StreamPipeline streamPipeline) {
            Stream<Frame> strm = streamPipeline.getStream();
            Optional<Object> result = strm
                    .map(frame -> frame.getRecord().get(VALUE_ACCESS_FIELD))
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
                    .map(frame -> frame.getRecord().get(VALUE_ACCESS_FIELD))
                    .map(string -> (BString) string)
                    .reduce(EMPTY_BSTRING, BString::concat);
        } catch (QueryException e) {
            return e.getError();
        }
    }

    public static Object createTable(StreamPipeline pipeline, BTable table) {
        Stream<Frame> strm = pipeline.getStream();

        try {
            strm.forEach(frame -> {
                BMap<BString, Object> record = (BMap<BString, Object>) frame.getRecord().get(VALUE_ACCESS_FIELD);
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
                        BMap<BString, Object> record = (BMap<BString, Object>) frame
                                .getRecord()
                                .get(VALUE_ACCESS_FIELD);
                        try {
                            table.add(record);
                        } catch (Exception e) {
                            if (frame.getRecord().get(ERROR_FIELD) instanceof BError errorValue) {
                                return errorValue;
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
                BArray record = (BArray) frame.getRecord().get(VALUE_ACCESS_FIELD);
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
                        BArray recordArray = (BArray) record.get(VALUE_ACCESS_FIELD);

                        BString key = (BString) recordArray.get(0);
                        if (map.containsKey(key) && record.get(ERROR_FIELD) instanceof BError errorValue) {
                            return errorValue;
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
        StreamType streamType = TypeCreator.createStreamType(
                pipeline.getConstraintType().getDescribingType(),
                pipeline.getCompletionType().getDescribingType());
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
                    .map(frame -> frame.getRecord().get(VALUE_ACCESS_FIELD))
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
            switch (refValue) {
                case BString bString -> {
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
                }
                case BXmlSequence bXmlSequence -> {
                    backingArray.addAll(bXmlSequence.getChildrenList());
                    lastItem = (BXml) refValue;
                }
                case null, default -> {
                    backingArray.add((BXml) refValue);
                    lastItem = (BXml) refValue;
                }
            }
        }
        return ValueCreator.createXmlSequence(backingArray);
    }

    public static Object consumeStream(StreamPipeline pipeline) {
        Stream<Frame> strm = pipeline.getStream();
        try {
            Iterator<Frame> it = strm.iterator();
            while (it.hasNext()) {
                Frame frame = it.next();
                if (frame.getRecord().containsKey(VALUE_ACCESS_FIELD)
                        && frame.getRecord().get(VALUE_ACCESS_FIELD) != null) {
                    return frame.getRecord().get(VALUE_ACCESS_FIELD);
                }
            }
        } catch (QueryException e) {
            return e.getError();
        }
        return null;
    }
}
