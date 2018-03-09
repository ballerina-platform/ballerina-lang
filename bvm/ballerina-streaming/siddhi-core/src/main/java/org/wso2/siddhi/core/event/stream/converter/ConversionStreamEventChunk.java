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
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;

/**
 * A StreamEvent holder that can also convert other events into StreamEvents
 */
public class ConversionStreamEventChunk extends ComplexEventChunk<StreamEvent> {

    private static final long serialVersionUID = 2754352338846132676L;
    private StreamEventConverter streamEventConverter;
    private StreamEventPool streamEventPool;

    public ConversionStreamEventChunk(MetaStreamEvent metaStreamEvent, StreamEventPool streamEventPool) {
        super(false);
        this.streamEventPool = streamEventPool;
        streamEventConverter = StreamEventConverterFactory.constructEventConverter(metaStreamEvent);
    }

    public ConversionStreamEventChunk(StreamEventConverter streamEventConverter, StreamEventPool streamEventPool) {
        super(false);
        this.streamEventConverter = streamEventConverter;
        this.streamEventPool = streamEventPool;
    }

    public void convertAndAssign(Event event) {
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        streamEventConverter.convertEvent(event, borrowedEvent);
        first = borrowedEvent;
        last = first;
    }

    public void convertAndAssign(long timestamp, Object[] data) {
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        streamEventConverter.convertData(timestamp, data, borrowedEvent);
        first = borrowedEvent;
        last = first;
    }

    public void convertAndAssign(ComplexEvent complexEvent) {
        first = streamEventPool.borrowEvent();
        last = convertAllStreamEvents(complexEvent, first);
    }

//    @Override
//    public void convertAndAssignFirst(StreamEvent streamEvent) {
//        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
//        eventConverter.convertComplexEvent(streamEvent, borrowedEvent);
//        first = borrowedEvent;
//        last = first;
//    }

    public void convertAndAssign(Event[] events) {
        StreamEvent firstEvent = streamEventPool.borrowEvent();
        streamEventConverter.convertEvent(events[0], firstEvent);
        StreamEvent currentEvent = firstEvent;
        for (int i = 1, eventsLength = events.length; i < eventsLength; i++) {
            StreamEvent nextEvent = streamEventPool.borrowEvent();
            streamEventConverter.convertEvent(events[i], nextEvent);
            currentEvent.setNext(nextEvent);
            currentEvent = nextEvent;
        }
        first = firstEvent;
        last = currentEvent;
    }

    public void convertAndAdd(Event event) {
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        streamEventConverter.convertEvent(event, borrowedEvent);

        if (first == null) {
            first = borrowedEvent;
            last = first;
        } else {
            last.setNext(borrowedEvent);
            last = borrowedEvent;
        }

    }

    private StreamEvent convertAllStreamEvents(ComplexEvent complexEvents, StreamEvent firstEvent) {
        streamEventConverter.convertComplexEvent(complexEvents, firstEvent);
        StreamEvent currentEvent = firstEvent;
        complexEvents = complexEvents.getNext();
        while (complexEvents != null) {
            StreamEvent nextEvent = streamEventPool.borrowEvent();
            streamEventConverter.convertComplexEvent(complexEvents, nextEvent);
            currentEvent.setNext(nextEvent);
            currentEvent = nextEvent;
            complexEvents = complexEvents.getNext();
        }
        return currentEvent;
    }
}
