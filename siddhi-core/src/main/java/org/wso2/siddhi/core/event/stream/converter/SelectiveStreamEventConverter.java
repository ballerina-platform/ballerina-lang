/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.event.stream.converter;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.StreamEvent;

import java.util.List;

/**
 * The converter class that converts the events into StreamEvent
 */
public class SelectiveStreamEventConverter implements StreamEventConverter {

    private List<ConversionMapping> conversionMappings;       //List to hold information needed for conversion

    public SelectiveStreamEventConverter(List<ConversionMapping> conversionMappings) {
        this.conversionMappings = conversionMappings;
    }

    private void convertToInnerStreamEvent(Object[] data, StreamEvent.Type type, long timestamp, StreamEvent borrowedEvent) {
        for (ConversionMapping conversionMapping : conversionMappings) {
            int[] position = conversionMapping.getToPosition();
            int fromPosition = conversionMapping.getFromPosition();
            switch (position[0]) {
                case 0:
                    borrowedEvent.setBeforeWindowData(data[fromPosition], position[1]);
                    break;
                case 1:
                    borrowedEvent.setOnAfterWindowData(data[fromPosition], position[1]);
                    break;
                case 2:
                    borrowedEvent.setOutputData(data[fromPosition], position[1]);
                    break;
                default:
                    //can not happen
            }
        }

        borrowedEvent.setType(type);
        borrowedEvent.setTimestamp(timestamp);
    }


    public void convertEvent(Event event, StreamEvent borrowedEvent) {
        convertToInnerStreamEvent(event.getData(), event.isExpired() ? StreamEvent.Type.EXPIRED : StreamEvent.Type.CURRENT,
                event.getTimestamp(), borrowedEvent);
    }

    public void convertStreamEvent(ComplexEvent complexEvent, StreamEvent borrowedEvent) {
        convertToInnerStreamEvent(complexEvent.getOutputData(), complexEvent.getType(), complexEvent.getTimestamp(),
                borrowedEvent);
    }

    @Override
    public void convertData(long timeStamp, Object[] data, StreamEvent borrowedEvent) {
        convertToInnerStreamEvent(data, StreamEvent.Type.CURRENT, timeStamp, borrowedEvent);
    }

}
