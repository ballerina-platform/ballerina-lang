/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
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

import java.util.ArrayList;
import java.util.List;

public class MultiProcessStreamReceiver extends ProcessStreamReceiver {

    private Processor[] nextProcessors;
    private MetaStreamEvent[] metaStreamEvents;
    private StreamEventPool[] streamEventPools;
    private StreamEventConverter[] streamEventConverters;
    private ComplexEventChunk<StreamEvent> currentStreamEventChunk;
    protected int processCount;
    private List<Event> eventBuffer = new ArrayList<Event>(0);
    protected int[] eventSequence;

    public MultiProcessStreamReceiver(String streamId, int processCount) {
        super(streamId);
        this.processCount = processCount;
        nextProcessors = new Processor[processCount];
        metaStreamEvents = new MetaStreamEvent[processCount];
        streamEventPools = new StreamEventPool[processCount];
        streamEventConverters = new StreamEventConverter[processCount];
        currentStreamEventChunk = new ComplexEventChunk<StreamEvent>();
        eventSequence= new int[processCount];
        for (int i=0;i<eventSequence.length;i++){
            eventSequence[i]=i;
        }

    }

    public MultiProcessStreamReceiver clone(String key) {
        return new MultiProcessStreamReceiver(streamId + key, processCount);
    }

    @Override
    public void receive(ComplexEvent complexEvent) {
        ComplexEvent aComplexEvent = complexEvent;
        while (aComplexEvent != null) {
            stabilizeStates();
            for (int anEventSequence : eventSequence) {
                StreamEventConverter aStreamEventConverter = streamEventConverters[anEventSequence];
                StreamEventPool aStreamEventPool = streamEventPools[anEventSequence];
                StreamEvent borrowedEvent = aStreamEventPool.borrowEvent();
                aStreamEventConverter.convertStreamEvent(aComplexEvent, borrowedEvent);
                processAndClear(anEventSequence, borrowedEvent);
            }
            aComplexEvent = aComplexEvent.getNext();
        }
    }

    @Override
    public void receive(Event event) {
        stabilizeStates();
        for (int anEventSequence : eventSequence) {
            StreamEventConverter aStreamEventConverter = streamEventConverters[anEventSequence];
            StreamEventPool aStreamEventPool = streamEventPools[anEventSequence];
            StreamEvent borrowedEvent = aStreamEventPool.borrowEvent();
            aStreamEventConverter.convertEvent(event, borrowedEvent);
            processAndClear(anEventSequence, borrowedEvent);
        }
    }

    @Override
    public void receive(Event[] events) {
        for (Event event : events) {
            stabilizeStates();
            for (int anEventSequence : eventSequence) {
                StreamEventConverter aStreamEventConverter = streamEventConverters[anEventSequence];
                StreamEventPool aStreamEventPool = streamEventPools[anEventSequence];
                StreamEvent borrowedEvent = aStreamEventPool.borrowEvent();
                aStreamEventConverter.convertEvent(event, borrowedEvent);
                processAndClear(anEventSequence, borrowedEvent);
            }
        }
    }

    @Override
    public void receive(Event event, boolean endOfBatch) {
        eventBuffer.add(event);
        if (endOfBatch) {
            for (Event aEvent : eventBuffer) {
                stabilizeStates();
                for (int anEventSequence : eventSequence) {
                    StreamEventConverter aStreamEventConverter = streamEventConverters[anEventSequence];
                    StreamEventPool aStreamEventPool = streamEventPools[anEventSequence];
                    StreamEvent borrowedEvent = aStreamEventPool.borrowEvent();
                    aStreamEventConverter.convertEvent(aEvent, borrowedEvent);
                    processAndClear(anEventSequence, borrowedEvent);
                }
            }
            eventBuffer.clear();
        }
    }

    @Override
    public void receive(long timeStamp, Object[] data) {
        stabilizeStates();
        for (int anEventSequence : eventSequence) {
            StreamEventConverter aStreamEventConverter = streamEventConverters[anEventSequence];
            StreamEventPool aStreamEventPool = streamEventPools[anEventSequence];
            StreamEvent borrowedEvent = aStreamEventPool.borrowEvent();
            aStreamEventConverter.convertData(timeStamp, data, borrowedEvent);
            processAndClear(anEventSequence, borrowedEvent);
        }
    }

    private void processAndClear(int processIndex, StreamEvent streamEvent) {
        currentStreamEventChunk.add(streamEvent);
        nextProcessors[processIndex].process(currentStreamEventChunk);
        currentStreamEventChunk.clear();
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
    public boolean toTable() {
        return metaStreamEvents[0].isTableEvent();
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
