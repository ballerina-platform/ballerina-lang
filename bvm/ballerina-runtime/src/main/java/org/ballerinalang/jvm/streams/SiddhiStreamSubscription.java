/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.jvm.streams;

import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;

import java.util.Map;

/**
 * The {@link SiddhiStreamSubscription} represents a siddhi based stream subscription in Ballerina.
 *
 * @since 0.995.0
 */
public class SiddhiStreamSubscription extends StreamSubscription {
    private StreamValue stream;
    private final InputHandler inputHandler;

    SiddhiStreamSubscription(StreamValue stream, InputHandler inputHandler,
                             StreamSubscriptionManager streamSubscriptionManager) {
        super(streamSubscriptionManager);
        this.stream = stream;
        this.inputHandler = inputHandler;
    }

    private Object[] createEvent(RefValue data) {
        if (data.getType().getTag() == TypeTags.RECORD_TYPE_TAG) {
            MapValue<String, RefValue> dataMap = (MapValue<String, RefValue>) data;
            BStructureType streamType = (BStructureType) data.getType();
            Object[] event = new Object[streamType.getFields().size()];
            int index = 0;
            for (Map.Entry<String, BField> fieldEntry : streamType.getFields().entrySet()) {
                BField field = fieldEntry.getValue();
                switch (field.getFieldType().getTag()) {
                    case TypeTags.INT_TAG:
                    case TypeTags.FLOAT_TAG:
                    case TypeTags.BOOLEAN_TAG:
                    case TypeTags.STRING_TAG:
                        event[index++] = dataMap.get(field.getFieldName());
                        break;
                    default:
                        throw new BallerinaException("Fields in streams do not support data types other than int, " +
                                                     "float, boolean and string");
                }
            }
            return event;
        } else {
            throw new BallerinaException("Received data is not of type record, found: " + data.getType().toString());
        }
    }

    public void execute(RefValue data) {
        Object[] event = createEvent(data);
        try {
            inputHandler.send(event);
        } catch (InterruptedException e) {
            throw new BallerinaException("Error while sending events to stream: " + stream.getStreamId() + ": " +
                    e.getMessage(), e);
        }
    }

    public StreamValue getStream() {
        return stream;
    }
}
