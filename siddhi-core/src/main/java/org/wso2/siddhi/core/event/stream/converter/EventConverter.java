package org.wso2.siddhi.core.event.stream.converter;
/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.StreamEvent;

public interface EventConverter {

    /**
     * Method to construct StreamEvent form Event
     *
     * @param event         Event to be converted
     * @param borrowedEvent Event that will be populated
     */
    public void convertEvent(Event event, StreamEvent borrowedEvent);

    /**
     * Method to construct(change format) new StreamEvent from StreamEvent
     *
     * @param streamEvent   StreamEvent to be Converted
     * @param borrowedEvent Event that will be populated
     */
    public void convertStreamEvent(StreamEvent streamEvent, StreamEvent borrowedEvent);

    /**
     * Method to construct(change format) timeStamp and data from StreamEvent
     *
     * @param timeStamp     timeStamp of the event
     * @param data          output data of the event
     * @param borrowedEvent Event that will be populated
     */
    public void convertData(long timeStamp, Object[] data, StreamEvent borrowedEvent);

    /**
     * Borrow a event from the pool
     */
    public StreamEvent borrowEvent();

    /**
     * Return the used event back to the pool
     *
     * @param streamEvent used stream event
     */
    public void returnEvent(StreamEvent streamEvent);


}
