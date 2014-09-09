/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.core.event.stream;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.selector.attribute.ComplexAttribute;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * The converter class that converts event into StreamEvent
 */
public class StreamEventConverter {

    private List<ConverterElement> converterElements;       //List to hold information needed for conversion
    private StreamEventPool streamEventPool;


    /**
     * Creation and initialization of EventConverter. This will create
     * relevant infrastructure for event conversion.
     *
     * @param metaStreamEvent
     */
    public StreamEventConverter(MetaStreamEvent metaStreamEvent) {
        StreamDefinition defaultDefinition = (StreamDefinition) metaStreamEvent.getDefinition();
        int beforeWindowDataSize = metaStreamEvent.getBeforeWindowData().size();
        int onAfterWindowDataSize = metaStreamEvent.getAfterWindowData().size();
        int outputDataSize = metaStreamEvent.getOutputData().size();

        StreamEventFactory eventFactory = new StreamEventFactory(beforeWindowDataSize, onAfterWindowDataSize, outputDataSize);
        int defaultPoolSize = 5;
        streamEventPool = new StreamEventPool(eventFactory, defaultPoolSize);

        int size = metaStreamEvent.getBeforeWindowData().size() + metaStreamEvent.getAfterWindowData().size() + metaStreamEvent.getOutputData().size();
        converterElements = new ArrayList<ConverterElement>(size);

        for (int j = 0; j < 3; j++) {
            List<Attribute> currentDataList = null;
            if (j == 0) {
                currentDataList = metaStreamEvent.getBeforeWindowData();
            } else if (j == 1) {
                currentDataList = metaStreamEvent.getAfterWindowData();
            } else if (j == 2) {
                currentDataList = metaStreamEvent.getOutputData();
            }
            if (currentDataList != null) {
                int i = 0;
                for (Attribute attribute : currentDataList) {           //Only variable slots will be filled.
                    if (attribute == null) {
                        i++;
                    } else {
                        ConverterElement converterElement = new ConverterElement();
                        int[] position = new int[2];
                        converterElement.setFromPosition(defaultDefinition.getAttributePosition(attribute.getName()));
                        position[0] = j;
                        position[1] = i;
                        converterElement.setToPosition(position);
                        converterElements.add(converterElement);
                        i++;
                    }
                }
            }
        }
    }

    /**
     * Converts events to StreamEvent
     *
     * @param data
     * @param isExpected
     * @param timestamp
     * @return StreamEvent
     */
    public StreamEvent convertToInnerStreamEvent(Object[] data, boolean isExpected, long timestamp) {
        StreamEvent streamEvent = streamEventPool.borrowEvent();
        for (ConverterElement converterElement : converterElements) {
            if (converterElement.getToPosition()[0] == SiddhiConstants.BEFORE_WINDOW_DATA_INDEX) {
                streamEvent.getBeforeWindowData()[converterElement.getToPosition()[1]] = data[converterElement.getFromPosition()];
            } else if (converterElement.getToPosition()[0] == SiddhiConstants.AFTER_WINDOW_DATA_INDEX) {
                streamEvent.getOnAfterWindowData()[converterElement.getToPosition()[1]] = data[converterElement.getFromPosition()];
            } else if (converterElement.getToPosition()[0] == SiddhiConstants.OUTPUT_DATA_INDEX) {
                streamEvent.getOutputData()[converterElement.getToPosition()[1]] = data[converterElement.getFromPosition()];
            }
        }

        streamEvent.setExpired(isExpected);
        streamEvent.setTimestamp(timestamp);

        return streamEvent;
    }

    /**
     * Converts events to StreamEvent
     *
     * @param event will be converted
     * @return StreamEvent
     */
    public StreamEvent convertToStreamEvent(Event event) {
        return convertToInnerStreamEvent(event.getData(), event.isExpired(), event.getTimestamp());
    }

    public StreamEvent convertToStreamEvent(StreamEvent streamEvent) {
        return convertToInnerStreamEvent(streamEvent.getOutputData(), streamEvent.isExpired(), streamEvent.getTimestamp());
    }

    /**
     * Element to hold information about event conversion
     */
    public class ConverterElement {
        private int fromPosition;               //position in StreamEvent/data[]
        private int[] toPosition = new int[2];  //new position in StreamEvent

        public int[] getToPosition() {
            return toPosition;
        }

        public void setToPosition(int[] toPosition) {
            this.toPosition = toPosition.clone();
        }

        public int getFromPosition() {
            return fromPosition;
        }

        public void setFromPosition(int fromPosition) {
            this.fromPosition = fromPosition;
        }
    }
}
