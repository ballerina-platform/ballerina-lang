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

package org.ballerinalang.streams;

import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStream;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.siddhi.core.stream.input.InputHandler;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.Map;

/**
 * The {@link SiddhiStreamSubscription} represents a siddhi based stream subscription in Ballerina.
 *
 * @since 0.995.0
 */
public class SiddhiStreamSubscription extends StreamSubscription {
    private BStream stream;
    private final InputHandler inputHandler;

    SiddhiStreamSubscription(BStream stream, InputHandler inputHandler,
                             StreamSubscriptionManager streamSubscriptionManager) {
        super(streamSubscriptionManager);
        this.stream = stream;
        this.inputHandler = inputHandler;
    }

    private Object[] createEvent(BMap<String, BValue> data) {
        BStructureType streamType = (BStructureType) data.getType();
        Object[] event = new Object[streamType.getFields().size()];
        int index = 0;
        for (Map.Entry<String, BField> fieldEntry : streamType.getFields().entrySet()) {
            BField field = fieldEntry.getValue();
            switch (field.getFieldType().getTag()) {
                case TypeTags.INT_TAG:
                    event[index++] = ((BInteger) data.get(field.fieldName)).intValue();
                    break;
                case TypeTags.FLOAT_TAG:
                    event[index++] = ((BFloat) data.get(field.fieldName)).floatValue();
                    break;
                case TypeTags.BOOLEAN_TAG:
                    event[index++] = ((BBoolean) data.get(field.fieldName)).booleanValue();
                    break;
                case TypeTags.STRING_TAG:
                    event[index++] = data.get(field.fieldName).stringValue();
                    break;
                default:
                    throw new BallerinaException("Fields in streams do not support data types other than int, " +
                                                 "float, boolean and string");
            }
        }
        return event;
    }

    public void execute(BValue data) {
        Object[] event = createEvent((BMap) data);
        try {
            inputHandler.send(event);
        } catch (InterruptedException e) {
            throw new BallerinaException("Error while sending events to stream: " + stream.getStreamId() + ": " +
                    e.getMessage(), e);
        }
    }

    public BStream getStream() {
        return stream;
    }
}
