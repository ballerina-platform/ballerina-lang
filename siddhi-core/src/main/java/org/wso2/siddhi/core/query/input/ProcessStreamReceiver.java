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
import java.util.concurrent.locks.ReentrantLock;

public class ProcessStreamReceiver implements StreamJunction.Receiver {

    protected String streamId;
    protected Processor next;
    private StreamEventConverter streamEventConverter;
    private MetaStreamEvent metaStreamEvent;
    private StreamEventPool streamEventPool;
    protected List<PreStateProcessor> stateProcessors = new ArrayList<PreStateProcessor>();
    protected int stateProcessorsSize;
    protected LatencyTracker latencyTracker;
    protected ReentrantLock queryLock;
    protected ComplexEventChunk<StreamEvent> batchingStreamEventChunk = new ComplexEventChunk<StreamEvent>(false);
    protected boolean batchProcessingAllowed;

    public ProcessStreamReceiver(String streamId, LatencyTracker latencyTracker) {
        this.streamId = streamId;
        this.latencyTracker = latencyTracker;
    }

    @Override
    public String getStreamId() {
        return streamId;
    }

    public ProcessStreamReceiver clone(String key) {
        ProcessStreamReceiver processStreamReceiver = new ProcessStreamReceiver(streamId + key, latencyTracker);
        processStreamReceiver.batchProcessingAllowed = this.batchProcessingAllowed;
        return processStreamReceiver;
    }

    private void process(ComplexEventChunk<StreamEvent> streamEventChunk) {
        if(queryLock!=null) {
            queryLock.lock();
        }
        try {
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
        } finally {
            if (queryLock!=null && queryLock.isHeldByCurrentThread()) {
                queryLock.unlock();
            }
        }
    }

    @Override
    public void receive(ComplexEvent complexEvents) {
        StreamEvent firstEvent = streamEventPool.borrowEvent();
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
        process(new ComplexEventChunk<StreamEvent>(firstEvent, currentEvent, this.batchProcessingAllowed));
    }

    @Override
    public void receive(Event event) {
        if (event != null) {
            StreamEvent borrowedEvent = streamEventPool.borrowEvent();
            streamEventConverter.convertEvent(event, borrowedEvent);
            process(new ComplexEventChunk<StreamEvent>(borrowedEvent, borrowedEvent, this.batchProcessingAllowed));
        }
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
        process(new ComplexEventChunk<StreamEvent>(firstEvent, currentEvent, this.batchProcessingAllowed));
    }


    @Override
    public void receive(Event event, boolean endOfBatch) {
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        streamEventConverter.convertEvent(event, borrowedEvent);
        ComplexEventChunk<StreamEvent> streamEventChunk = null;
        synchronized (this) {
            batchingStreamEventChunk.add(borrowedEvent);
            if (endOfBatch) {
                streamEventChunk = batchingStreamEventChunk;
                batchingStreamEventChunk = new ComplexEventChunk<StreamEvent>(this.batchProcessingAllowed);
            }
        }
        if (streamEventChunk != null) {
            process(streamEventChunk);
        }
    }

    @Override
    public void receive(long timeStamp, Object[] data) {
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        streamEventConverter.convertData(timeStamp, data, borrowedEvent);
        process(new ComplexEventChunk<StreamEvent>(borrowedEvent, borrowedEvent, this.batchProcessingAllowed));
    }

    protected void processAndClear(ComplexEventChunk<StreamEvent> streamEventChunk) {
        next.process(streamEventChunk);
        streamEventChunk.clear();
    }

    public void setMetaStreamEvent(MetaStreamEvent metaStreamEvent) {
        this.metaStreamEvent = metaStreamEvent;
    }

    public boolean toTable() {
        return metaStreamEvent.isTableEvent();
    }

    public void setBatchProcessingAllowed(boolean batchProcessingAllowed) {
        this.batchProcessingAllowed = batchProcessingAllowed;
    }

    public void setNext(Processor next) {
        this.next = next;
    }

    public void setStreamEventPool(StreamEventPool streamEventPool) {
        this.streamEventPool = streamEventPool;
    }

    public void setQueryLock(ReentrantLock queryLock) {
        this.queryLock = queryLock;
    }

    public void init() {
        streamEventConverter = StreamEventConverterFactory.constructEventConverter(metaStreamEvent);
    }

    public void addStatefulProcessor(PreStateProcessor stateProcessor) {
        stateProcessors.add(stateProcessor);
        stateProcessorsSize = stateProcessors.size();
    }
}
