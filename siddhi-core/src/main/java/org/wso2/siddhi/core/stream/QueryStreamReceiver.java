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

package org.wso2.siddhi.core.stream;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventBuffer;
import org.wso2.siddhi.core.event.stream.StreamEventIterator;
import org.wso2.siddhi.core.event.stream.converter.EventManager;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

public class QueryStreamReceiver implements StreamJunction.Receiver {

    private String streamId;
    private EventManager eventManager;
    private Processor processorChain;
    private Processor next;
    private StreamEventIterator streamEventIterator = new StreamEventIterator();
    private StreamEventBuffer streamEventBuffer = new StreamEventBuffer();


    public QueryStreamReceiver(StreamDefinition streamDefinition) {
        this.streamId = streamDefinition.getId();
    }

    private QueryStreamReceiver(String id) {
        streamId = id;
    }

    @Override
    public String getStreamId() {
        return streamId;
    }

    public QueryStreamReceiver clone(String key) {
        QueryStreamReceiver clonedQueryStreamReceiver = new QueryStreamReceiver(streamId + key);
        clonedQueryStreamReceiver.setEventManager(eventManager);
        return clonedQueryStreamReceiver;
    }

    @Override
    public void receive(StreamEvent streamEvent) {

        if (streamEvent.getNext() == null) {
            StreamEvent borrowedEvent = eventManager.borrowEvent();
            eventManager.convertStreamEvent(streamEvent, borrowedEvent);
            next.process(borrowedEvent);
            eventManager.returnEvent(borrowedEvent);
        } else {
            streamEventIterator.assignEvent(streamEvent);
            while (streamEventIterator.hasNext()) {
                StreamEvent aStreamEvent = streamEventIterator.next();
                StreamEvent borrowedEvent = eventManager.borrowEvent();
                eventManager.convertStreamEvent(aStreamEvent, borrowedEvent);
                streamEventBuffer.addEvent(borrowedEvent);
            }
            next.process(streamEventBuffer.getFirst());
            streamEventIterator.clear();
            eventManager.returnEvent(streamEventBuffer.getFirstAndClear());
        }
    }

    @Override
    public void receive(Event event) {
        StreamEvent borrowedEvent = eventManager.borrowEvent();
        eventManager.convertEvent(event, borrowedEvent);
        next.process(borrowedEvent);
        eventManager.returnEvent(borrowedEvent);
    }

    @Override
    public void receive(Event[] events) {
        StreamEvent firstEvent = eventManager.borrowEvent();
        eventManager.convertEvent(events[0], firstEvent);
        StreamEvent currentEvent = firstEvent;
        for (int i = 1, eventsLength = events.length; i < eventsLength; i++) {
            StreamEvent nextEvent = eventManager.borrowEvent();
            eventManager.convertEvent(events[i], nextEvent);
            currentEvent.setNext(nextEvent);
            currentEvent = nextEvent;
        }
        next.process(firstEvent);
        eventManager.returnEvent(firstEvent);
    }


    @Override
    public void receive(Event event, boolean endOfBatch) {
        StreamEvent borrowedEvent = eventManager.borrowEvent();
        eventManager.convertEvent(event, borrowedEvent);

        streamEventBuffer.addEvent(borrowedEvent);
        if (endOfBatch) {
            next.process(streamEventBuffer.getFirst());
            eventManager.returnEvent(streamEventBuffer.getFirstAndClear());
        }
    }

    @Override
    public void receive(long timeStamp, Object[] data) {
        StreamEvent borrowedEvent = eventManager.borrowEvent();
        eventManager.convertData(timeStamp, data, borrowedEvent);
        next.process(borrowedEvent);
        eventManager.returnEvent(borrowedEvent);
    }

    public Processor getProcessorChain() {
        return processorChain;
    }

    public void setProcessorChain(Processor processorChain) {
        this.processorChain = processorChain;
    }

    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void setNext(Processor next) {
        this.next = next;
    }
}
