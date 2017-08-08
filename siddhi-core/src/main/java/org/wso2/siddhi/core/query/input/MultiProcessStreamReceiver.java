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
package org.wso2.siddhi.core.query.input;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverterFactory;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link org.wso2.siddhi.core.stream.StreamJunction.Receiver} implementation to receive events to be fed into multi
 * stream processors which consume multiple streams.
 */
public class MultiProcessStreamReceiver extends ProcessStreamReceiver {

    protected Processor[] nextProcessors;
    protected int processCount;
    protected int[] eventSequence;
    protected String queryName;
    private MetaStreamEvent[] metaStreamEvents;
    private StreamEventPool[] streamEventPools;
    private StreamEventConverter[] streamEventConverters;
    private List<Event> eventBuffer = new ArrayList<Event>(0);

    public MultiProcessStreamReceiver(String streamId, int processCount, LatencyTracker latencyTracker, String
            queryName) {
        super(streamId, latencyTracker, queryName);
        this.processCount = processCount;
        this.queryName = queryName;
        nextProcessors = new Processor[processCount];
        metaStreamEvents = new MetaStreamEvent[processCount];
        streamEventPools = new StreamEventPool[processCount];
        streamEventConverters = new StreamEventConverter[processCount];
        eventSequence = new int[processCount];
        for (int i = 0; i < eventSequence.length; i++) {
            eventSequence[i] = i;
        }

    }

    public MultiProcessStreamReceiver clone(String key) {
        return new MultiProcessStreamReceiver(streamId + key, processCount, latencyTracker, queryName);
    }

    private void process(int eventSequence, StreamEvent borrowedEvent) {
        if (lockWrapper != null) {
            lockWrapper.lock();
        }
        try {
            if (latencyTracker != null) {
                try {
                    latencyTracker.markIn();
                    processAndClear(eventSequence, borrowedEvent);
                } finally {
                    latencyTracker.markOut();
                }
            } else {
                processAndClear(eventSequence, borrowedEvent);
            }
        } finally {
            if (lockWrapper != null) {
                lockWrapper.unlock();
            }
        }
    }

    @Override
    public void receive(ComplexEvent complexEvent) {
        ComplexEvent aComplexEvent = complexEvent;
        while (aComplexEvent != null) {
            synchronized (this) {
                stabilizeStates();
                for (int anEventSequence : eventSequence) {
                    StreamEventConverter aStreamEventConverter = streamEventConverters[anEventSequence];
                    StreamEventPool aStreamEventPool = streamEventPools[anEventSequence];
                    StreamEvent borrowedEvent = aStreamEventPool.borrowEvent();
                    aStreamEventConverter.convertComplexEvent(aComplexEvent, borrowedEvent);
                    process(anEventSequence, borrowedEvent);

                }
            }
            aComplexEvent = aComplexEvent.getNext();
        }
    }

    @Override
    public void receive(Event event) {
        synchronized (this) {
            stabilizeStates();
            for (int anEventSequence : eventSequence) {
                StreamEventConverter aStreamEventConverter = streamEventConverters[anEventSequence];
                StreamEventPool aStreamEventPool = streamEventPools[anEventSequence];
                StreamEvent borrowedEvent = aStreamEventPool.borrowEvent();
                aStreamEventConverter.convertEvent(event, borrowedEvent);
                process(anEventSequence, borrowedEvent);
            }
        }
    }

    @Override
    public void receive(Event[] events) {
        for (Event event : events) {
            synchronized (this) {
                stabilizeStates();
                for (int anEventSequence : eventSequence) {
                    StreamEventConverter aStreamEventConverter = streamEventConverters[anEventSequence];
                    StreamEventPool aStreamEventPool = streamEventPools[anEventSequence];
                    StreamEvent borrowedEvent = aStreamEventPool.borrowEvent();
                    aStreamEventConverter.convertEvent(event, borrowedEvent);
                    process(anEventSequence, borrowedEvent);
                }
            }
        }
    }

    @Override
    public void receive(Event event, boolean endOfBatch) {
        eventBuffer.add(event);
        if (endOfBatch) {
            for (Event aEvent : eventBuffer) {
                synchronized (this) {
                    stabilizeStates();
                    for (int anEventSequence : eventSequence) {
                        StreamEventConverter aStreamEventConverter = streamEventConverters[anEventSequence];
                        StreamEventPool aStreamEventPool = streamEventPools[anEventSequence];
                        StreamEvent borrowedEvent = aStreamEventPool.borrowEvent();
                        aStreamEventConverter.convertEvent(aEvent, borrowedEvent);
                        process(anEventSequence, borrowedEvent);
                    }
                }
            }
            eventBuffer.clear();
        }
    }

    @Override
    public void receive(long timestamp, Object[] data) {
        synchronized (this) {
            stabilizeStates();
            for (int anEventSequence : eventSequence) {
                StreamEventConverter aStreamEventConverter = streamEventConverters[anEventSequence];
                StreamEventPool aStreamEventPool = streamEventPools[anEventSequence];
                StreamEvent borrowedEvent = aStreamEventPool.borrowEvent();
                aStreamEventConverter.convertData(timestamp, data, borrowedEvent);
                process(anEventSequence, borrowedEvent);
            }
        }
    }

    protected void processAndClear(int processIndex, StreamEvent streamEvent) {
        ComplexEventChunk<StreamEvent> currentStreamEventChunk = new ComplexEventChunk<StreamEvent>(
                streamEvent, streamEvent, batchProcessingAllowed);
        nextProcessors[processIndex].process(currentStreamEventChunk);
    }

    protected void stabilizeStates() {

    }


    public void setNext(Processor nextProcessor) {
        for (int i = 0, nextLength = nextProcessors.length; i < nextLength; i++) {
            Processor processor = nextProcessors[i];
            if (processor == null) {
                nextProcessors[i] = nextProcessor;
                break;
            }
        }
    }

    public void setMetaStreamEvent(MetaStreamEvent metaStreamEvent) {
        for (int i = 0, nextLength = metaStreamEvents.length; i < nextLength; i++) {
            MetaStreamEvent streamEvent = metaStreamEvents[i];
            if (streamEvent == null) {
                metaStreamEvents[i] = metaStreamEvent;
                break;
            }
        }
    }

    @Override
    public boolean toStream() {
        return metaStreamEvents[0].getEventType() == MetaStreamEvent.EventType.DEFAULT ||
                metaStreamEvents[0].getEventType() == MetaStreamEvent.EventType.WINDOW;
    }

    public void setStreamEventPool(StreamEventPool streamEventPool) {
        for (int i = 0, nextLength = streamEventPools.length; i < nextLength; i++) {
            StreamEventPool eventPool = streamEventPools[i];
            if (eventPool == null) {
                streamEventPools[i] = streamEventPool;
                break;
            }
        }
    }

    public void init() {

        for (int i = 0, nextLength = streamEventConverters.length; i < nextLength; i++) {
            StreamEventConverter streamEventConverter = streamEventConverters[i];
            if (streamEventConverter == null) {
                streamEventConverters[i] = StreamEventConverterFactory.constructEventConverter(metaStreamEvents[i]);
                break;
            }
        }
    }
}
