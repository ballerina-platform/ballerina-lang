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
package org.wso2.siddhi.core.query.input;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverterFactory;
import org.wso2.siddhi.core.query.input.stream.state.PreStateProcessor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;

import java.util.ArrayList;
import java.util.List;

public class ProcessStreamReceiver implements StreamJunction.Receiver {

    protected String streamId;
    protected Processor next;
    private StreamEventConverter streamEventConverter;
    private MetaStreamEvent metaStreamEvent;
    private StreamEventPool streamEventPool;
    protected List<PreStateProcessor> stateProcessors = new ArrayList<PreStateProcessor>();
    protected int stateProcessorsSize;
    protected LatencyTracker latencyTracker;

    public ProcessStreamReceiver(String streamId, LatencyTracker latencyTracker) {
        this.streamId = streamId;
        this.latencyTracker = latencyTracker;
    }

    @Override
    public String getStreamId() {
        return streamId;
    }

    public ProcessStreamReceiver clone(String key) {
        return new ProcessStreamReceiver(streamId + key, latencyTracker);
    }

    private void process(ComplexEventChunk<StreamEvent> streamEventChunk) {
        if (latencyTracker != null) {
            try {
                latencyTracker.markIn();
                processAndClear(streamEventChunk);
            } finally {
                latencyTracker.markOut();
            }
        } else {
            processAndClear(streamEventChunk);
        }
    }

    @Override
    public void receive(ComplexEvent complexEvent) {
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        process(new ComplexEventChunk<StreamEvent>(borrowedEvent, convertAllStreamEvents(complexEvent,borrowedEvent)));
    }

    private StreamEvent convertAllStreamEvents(ComplexEvent complexEvents, StreamEvent firstEvent) {
        streamEventConverter.convertStreamEvent(complexEvents, firstEvent);
        StreamEvent currentEvent = firstEvent;
        complexEvents = complexEvents.getNext();
        while (complexEvents != null) {
            StreamEvent nextEvent = streamEventPool.borrowEvent();
            streamEventConverter.convertStreamEvent(complexEvents, nextEvent);
            currentEvent.setNext(nextEvent);
            currentEvent = nextEvent;
            complexEvents = complexEvents.getNext();
        }
        return currentEvent;
    }

    @Override
    public void receive(Event event) {
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        streamEventConverter.convertEvent(event, borrowedEvent);
        process(new ComplexEventChunk<StreamEvent>(borrowedEvent,borrowedEvent));
    }

    @Override
    public void receive(Event[] events) {
        StreamEvent firstEvent = streamEventPool.borrowEvent();
        streamEventConverter.convertEvent(events[0], firstEvent);
        StreamEvent currentEvent = firstEvent;
        for (int i = 1, eventsLength = events.length; i < eventsLength; i++) {
            StreamEvent nextEvent = streamEventPool.borrowEvent();
            streamEventConverter.convertEvent(events[i], nextEvent);
            currentEvent.setNext(nextEvent);
            currentEvent = nextEvent;
        }
        process(new ComplexEventChunk<StreamEvent>(firstEvent,currentEvent));
    }


    @Override
    public void receive(Event event, boolean endOfBatch) {
//        streamEventChunk.convertAndAdd(event);
//        if (endOfBatch) {
//            process(complexEventChunk);
//        }
    }

    @Override
    public void receive(long timeStamp, Object[] data) {
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        streamEventConverter.convertData(timeStamp, data, borrowedEvent);
        process(new ComplexEventChunk<StreamEvent>(borrowedEvent,borrowedEvent));
    }

    protected void processAndClear(ComplexEventChunk<StreamEvent> streamEventChunk) {
        if (stateProcessorsSize != 0) {
            stateProcessors.get(0).updateState();
        }
        next.process(streamEventChunk);
        streamEventChunk.clear();
    }

    public void setMetaStreamEvent(MetaStreamEvent metaStreamEvent) {
        this.metaStreamEvent = metaStreamEvent;
    }

    public boolean toTable() {
        return metaStreamEvent.isTableEvent();
    }

    public void setNext(Processor next) {
        this.next = next;
    }

    public void setStreamEventPool(StreamEventPool streamEventPool) {
        this.streamEventPool = streamEventPool;
    }

    public void init() {
        streamEventConverter = StreamEventConverterFactory.constructEventConverter(metaStreamEvent);
//        streamEventChunk = new ConversionStreamEventChunk(metaStreamEvent, streamEventPool);
    }

    public void addStatefulProcessor(PreStateProcessor stateProcessor) {
        stateProcessors.add(stateProcessor);
        stateProcessorsSize = stateProcessors.size();
    }
}
