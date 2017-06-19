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
package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.core.debugger.SiddhiDebugger;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverterFactory;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.input.stream.state.PreStateProcessor;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.lock.LockWrapper;
import org.wso2.siddhi.core.util.parser.helper.AggregationDefinitionParserHelper;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;

import java.util.ArrayList;
import java.util.List;

public class IncrementalExecuteStreamReceiver implements StreamJunction.Receiver {

    // TODO: 5/16/17 Copy of ProcessStreamReceiver. Change where necessary

    protected String streamId;
    protected Executor next;
    private StreamEventConverter streamEventConverter;
    private MetaStreamEvent newMetaStreamEvent;
    private MetaStreamEvent originalMetaStreamEvent;
    private StreamEventPool originalStreamEventPool;
    private StreamEventPool newStreamEventPool;
    protected List<PreStateProcessor> stateProcessors = new ArrayList<PreStateProcessor>();
    protected int stateProcessorsSize;
    protected LatencyTracker latencyTracker;
    protected LockWrapper lockWrapper;
    protected ComplexEventChunk<StreamEvent> batchingStreamEventChunk = new ComplexEventChunk<StreamEvent>(false);
    protected boolean batchProcessingAllowed;
    private SiddhiDebugger siddhiDebugger;
    private String aggregatorName;
    private List<ExpressionExecutor> metaValueRetrievers;
    private Scheduler scheduler;
    private TimePeriod.Duration minSchedulingTime;
    private boolean isFirstEvent = true;

    public IncrementalExecuteStreamReceiver(String streamId, LatencyTracker latencyTracker, String queryName) {
        this.streamId = streamId;
        this.latencyTracker = latencyTracker;
        this.aggregatorName = queryName;
    }

    @Override
    public String getStreamId() {
        return streamId;
    }

    public IncrementalExecuteStreamReceiver clone(String key) {
        IncrementalExecuteStreamReceiver processStreamReceiver = new IncrementalExecuteStreamReceiver(streamId + key, latencyTracker, aggregatorName);
        processStreamReceiver.batchProcessingAllowed = this.batchProcessingAllowed;
        return processStreamReceiver;
    }

    public void setSiddhiDebugger(SiddhiDebugger siddhiDebugger) {
        this.siddhiDebugger = siddhiDebugger;
    }

    private void execute(ComplexEventChunk<StreamEvent> streamEventChunk) {
        if (lockWrapper != null) {
            lockWrapper.lock();
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
            if (lockWrapper != null) {
                lockWrapper.unlock();
            }
        }
    }

    @Override
    public void receive(ComplexEvent complexEvents) {
        if (siddhiDebugger != null) {
            siddhiDebugger.checkBreakPoint(aggregatorName, SiddhiDebugger.QueryTerminal.IN, complexEvents);
        }
        StreamEvent firstEvent = originalStreamEventPool.borrowEvent();
        streamEventConverter.convertComplexEvent(complexEvents, firstEvent);
        StreamEvent currentEvent = firstEvent;
        complexEvents = complexEvents.getNext();
        while (complexEvents != null) {
            StreamEvent nextEvent = originalStreamEventPool.borrowEvent();
            streamEventConverter.convertComplexEvent(complexEvents, nextEvent);
            currentEvent.setNext(nextEvent);
            currentEvent = nextEvent;
            complexEvents = complexEvents.getNext();
        }
        execute(new ComplexEventChunk<>(firstEvent, currentEvent, this.batchProcessingAllowed));
    }

    @Override
    public void receive(Event event) {
        if (event != null) {
            StreamEvent borrowedEvent = originalStreamEventPool.borrowEvent();
            streamEventConverter.convertEvent(event, borrowedEvent);
            if (siddhiDebugger != null) {
                siddhiDebugger.checkBreakPoint(aggregatorName, SiddhiDebugger.QueryTerminal.IN, borrowedEvent);
            }
            execute(new ComplexEventChunk<>(borrowedEvent, borrowedEvent, this.batchProcessingAllowed));
        }
    }

    @Override
    public void receive(Event[] events) {
        StreamEvent firstEvent = originalStreamEventPool.borrowEvent();
        streamEventConverter.convertEvent(events[0], firstEvent);
        StreamEvent currentEvent = firstEvent;
        for (int i = 1, eventsLength = events.length; i < eventsLength; i++) {
            StreamEvent nextEvent = originalStreamEventPool.borrowEvent();
            streamEventConverter.convertEvent(events[i], nextEvent);
            currentEvent.setNext(nextEvent);
            currentEvent = nextEvent;
        }
        if (siddhiDebugger != null) {
            siddhiDebugger.checkBreakPoint(aggregatorName, SiddhiDebugger.QueryTerminal.IN, firstEvent);
        }
        execute(new ComplexEventChunk<>(firstEvent, currentEvent, this.batchProcessingAllowed));
    }


    @Override
    public void receive(Event event, boolean endOfBatch) {
        StreamEvent borrowedEvent = originalStreamEventPool.borrowEvent();
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
            if (siddhiDebugger != null) {
                siddhiDebugger.checkBreakPoint(aggregatorName, SiddhiDebugger.QueryTerminal.IN, streamEventChunk.getFirst());
            }
            execute(streamEventChunk);
        }
    }

    @Override
    public void receive(long timestamp, Object[] data) {
        // Convert data using the original meta
        StreamEvent borrowedOriginalEvent = originalStreamEventPool.borrowEvent();
        streamEventConverter.convertData(timestamp, data, borrowedOriginalEvent);

        StreamEvent borrowedNewEvent = newStreamEventPool.borrowEvent();
        if (newMetaStreamEvent.getOnAfterWindowData().size() == metaValueRetrievers.size()) {
            // User has defined the timeStamp
            int i = 0;
            for (ExpressionExecutor expressionExecutor : metaValueRetrievers) {
                borrowedNewEvent.getOnAfterWindowData()[i] = expressionExecutor.execute(borrowedOriginalEvent);
                i++;
            }
        } else if (newMetaStreamEvent.getOnAfterWindowData().size() == metaValueRetrievers.size() +1 ) {
            // User has not defined the timeStamp
            borrowedNewEvent.getOnAfterWindowData()[0] = System.currentTimeMillis();
            int i = 1;
            for (ExpressionExecutor expressionExecutor : metaValueRetrievers) {
                borrowedNewEvent.getOnAfterWindowData()[i] = expressionExecutor.execute(borrowedOriginalEvent);
                i++;
            }
            if (isFirstEvent) {
                // Scheduling must be done only for system time based events
                scheduler.notifyAt(IncrementalTimeConverterUtil.getNextEmitTime(
                        (Long) borrowedNewEvent.getOnAfterWindowData()[0], minSchedulingTime));
                isFirstEvent = false;
            }
        } else {
            // TODO: 6/11/17 error
            // Expression executors must be defined to retrieve each new meta value
        }

        // Send to debugger
        if (siddhiDebugger != null) {
            siddhiDebugger.checkBreakPoint(aggregatorName, SiddhiDebugger.QueryTerminal.IN, borrowedOriginalEvent);
        }
        execute(new ComplexEventChunk<>(borrowedNewEvent, borrowedNewEvent, this.batchProcessingAllowed));
    }

    protected void processAndClear(ComplexEventChunk<StreamEvent> streamEventChunk) {
        next.execute(streamEventChunk);
        streamEventChunk.clear();
    }

    public void setNewMetaStreamEvent(MetaStreamEvent metaStreamEvent) {
        this.newMetaStreamEvent = metaStreamEvent;
    }

    public void setOriginalMetaStreamEvent(MetaStreamEvent metaStreamEvent) {
        this.originalMetaStreamEvent = metaStreamEvent;
    }

    public boolean toTable() {
        return newMetaStreamEvent.isTableEvent();
    }

    public void setBatchProcessingAllowed(boolean batchProcessingAllowed) {
        this.batchProcessingAllowed = batchProcessingAllowed;
    }

    public void setNext(Executor next) {
        this.next = next;
    }

    public void setStreamEventPoolForOriginalMeta(StreamEventPool streamEventPool) {
        this.originalStreamEventPool = streamEventPool;
    }

    public void setStreamEventPoolForNewMeta(StreamEventPool streamEventPool) {
        this.newStreamEventPool = streamEventPool;
    }

    public void setLockWrapper(LockWrapper lockWrapper) {
        this.lockWrapper = lockWrapper;
    }

    public void init() {
        streamEventConverter = StreamEventConverterFactory.constructEventConverter(originalMetaStreamEvent);
    }

    public void addStatefulProcessor(PreStateProcessor stateProcessor) {
        stateProcessors.add(stateProcessor);
        stateProcessorsSize = stateProcessors.size();
    }

    public void setExpressionExecutors(List<ExpressionExecutor> metaValueRetrievers) {
        this.metaValueRetrievers = metaValueRetrievers;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setMinSchedulingTime(TimePeriod.Duration minSchedulingTime) {
        this.minSchedulingTime = minSchedulingTime;
    }

}
