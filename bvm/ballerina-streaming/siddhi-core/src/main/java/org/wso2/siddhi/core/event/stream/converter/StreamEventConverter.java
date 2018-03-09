/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.io.Serializable;

/**
 * The converter that converts data of the events into StreamEvents
 */
public interface StreamEventConverter {

    /**
     * Method to construct StreamEvent form Event
     *
     * @param event         Event to be converted
     * @param borrowedEvent Event that will be populated
     */
    void convertEvent(Event event, StreamEvent borrowedEvent);

    /**
     * Method to construct(change format) new StreamEvent from StreamEvent
     *
     * @param complexEvent  StreamEvent to be Converted
     * @param borrowedEvent Event that will be populated
     */
    void convertComplexEvent(ComplexEvent complexEvent, StreamEvent borrowedEvent);

    /**
     * Method to construct(change format) timeStamp and data from StreamEvent
     *
     * @param timeStamp     timeStamp of the event
     * @param data          output data of the event
     * @param borrowedEvent Event that will be populated
     */
    void convertData(long timeStamp, Object[] data, StreamEvent borrowedEvent);

    /**
     * Method to construct(change format) timeStamp and data from StreamEvent
     *
     * @param timeStamp     timeStamp of the event
     * @param data          output data of the event
     * @param type          output type of the event
     * @param borrowedEvent Event that will be populated
     */
    void convertData(long timeStamp, Object[] data, StreamEvent.Type type, StreamEvent borrowedEvent);

    /**
     * Element to hold information about event conversion
     */
    class ConversionMapping implements Serializable {
        private static final long serialVersionUID = 4986399180249934830L;
        private int fromPosition;               //position in StreamEvent/data[]
        private int[] toPosition = new int[2];  //new position in StreamEvent

        public int[] getToPosition() {
            return toPosition;
        }

        public void setToPosition(int[] toPosition) {
            this.toPosition = toPosition;
        }

        public int getFromPosition() {
            return fromPosition;
        }

        public void setFromPosition(int fromPosition) {
            this.fromPosition = fromPosition;
        }

    }
}
