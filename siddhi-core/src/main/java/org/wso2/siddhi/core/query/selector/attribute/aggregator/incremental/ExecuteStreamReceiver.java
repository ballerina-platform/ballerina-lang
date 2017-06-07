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
import org.wso2.siddhi.core.event.stream.converter.AggregatorEventConverterFactory;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverterFactory;
import org.wso2.siddhi.core.query.input.stream.state.PreStateProcessor;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.util.lock.LockWrapper;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.In;
import org.wso2.siddhi.query.api.expression.constant.DoubleConstant;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecuteStreamReceiver implements StreamJunction.Receiver {

    // TODO: 5/16/17 Copy of ProcessStreamReceiver. Change where necessary

    protected String streamId;
    protected Executor next;
    private StreamEventConverter streamEventConverter;
    private MetaStreamEvent metaStreamEvent;
    private MetaStreamEvent originalMetaStreamEvent;
    private StreamEventPool streamEventPool;
    protected List<PreStateProcessor> stateProcessors = new ArrayList<PreStateProcessor>();
    protected int stateProcessorsSize;
    protected LatencyTracker latencyTracker;
    protected LockWrapper lockWrapper;
    protected ComplexEventChunk<StreamEvent> batchingStreamEventChunk = new ComplexEventChunk<StreamEvent>(false);
    protected boolean batchProcessingAllowed;
    private SiddhiDebugger siddhiDebugger;
    private String aggregatorName;
    private Map<Attribute, Integer> mappingPositions;

    public ExecuteStreamReceiver(String streamId, LatencyTracker latencyTracker, String queryName) {
        this.streamId = streamId;
        this.latencyTracker = latencyTracker;
        this.aggregatorName = queryName;
    }

    @Override
    public String getStreamId() {
        return streamId;
    }

    public ExecuteStreamReceiver clone(String key) {
        ExecuteStreamReceiver processStreamReceiver = new ExecuteStreamReceiver(streamId + key, latencyTracker, aggregatorName);
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
        execute(new ComplexEventChunk<StreamEvent>(firstEvent, currentEvent, this.batchProcessingAllowed));
    }

    @Override
    public void receive(Event event) {
        if (event != null) {
            StreamEvent borrowedEvent = streamEventPool.borrowEvent();
            streamEventConverter.convertEvent(event, borrowedEvent);
            if (siddhiDebugger != null) {
                siddhiDebugger.checkBreakPoint(aggregatorName, SiddhiDebugger.QueryTerminal.IN, borrowedEvent);
            }
            execute(new ComplexEventChunk<StreamEvent>(borrowedEvent, borrowedEvent, this.batchProcessingAllowed));
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
        if (siddhiDebugger != null) {
            siddhiDebugger.checkBreakPoint(aggregatorName, SiddhiDebugger.QueryTerminal.IN, firstEvent);
        }
        execute(new ComplexEventChunk<StreamEvent>(firstEvent, currentEvent, this.batchProcessingAllowed));
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
            if (siddhiDebugger != null) {
                siddhiDebugger.checkBreakPoint(aggregatorName, SiddhiDebugger.QueryTerminal.IN, streamEventChunk.getFirst());
            }
            execute(streamEventChunk);
        }
    }

    @Override
    public void receive(long timestamp, Object[] data) {
        // TODO: 6/2/17 change mapping mechanism
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        streamEventConverter.convertData(timestamp, data, borrowedEvent);
        //Only the groupBy attribute values and timestamp (if available) are mapped with converter.
        //The base aggregate values must be taken from compositeAggregator.

        List<Attribute> onAfterWindowData = metaStreamEvent.getOnAfterWindowData(); //get index of new attribute (from the map we pass here)

        for (Map.Entry<Attribute, Integer> mapping: mappingPositions.entrySet()) {
            int toPosition = onAfterWindowData.indexOf(mapping.getKey());
            int fromPosition = mapping.getValue();
            Object dataValue = data[fromPosition];
            switch (dataValue.getClass().getTypeName()) {
                case "java.lang.Integer":
                    switch (onAfterWindowData.get(toPosition).getType()) {
                        case INT:
                            borrowedEvent.setOnAfterWindowData(dataValue, toPosition);
                            break;
                        case LONG:
                            borrowedEvent.setOnAfterWindowData(((Integer)dataValue).longValue(), toPosition);
                            break;
                        case FLOAT:
                            borrowedEvent.setOnAfterWindowData(((Integer)dataValue).floatValue(), toPosition);
                            break;
                        case DOUBLE:
                            borrowedEvent.setOnAfterWindowData(((Integer)dataValue).doubleValue(), toPosition);
                            break;
                        default:
                            // TODO: 6/7/17 exception
                    }
                    break;
                case "java.lang.Long":
                    switch (onAfterWindowData.get(toPosition).getType()) {
                        case INT:
                            borrowedEvent.setOnAfterWindowData(((Long)dataValue).intValue(), toPosition);
                            break;
                        case LONG:
                            borrowedEvent.setOnAfterWindowData(dataValue, toPosition);
                            break;
                        case FLOAT:
                            borrowedEvent.setOnAfterWindowData(((Long)dataValue).floatValue(), toPosition);
                            break;
                        case DOUBLE:
                            borrowedEvent.setOnAfterWindowData(((Long)dataValue).doubleValue(), toPosition);
                            break;
                        default:
                            // TODO: 6/7/17 exception
                    }
                    break;
                case "java.lang.Float":
                    switch (onAfterWindowData.get(toPosition).getType()) {
                        case INT:
                            borrowedEvent.setOnAfterWindowData(((Float)dataValue).intValue(), toPosition);
                            break;
                        case LONG:
                            borrowedEvent.setOnAfterWindowData(((Float)dataValue).longValue(), toPosition);
                            break;
                        case FLOAT:
                            borrowedEvent.setOnAfterWindowData(dataValue, toPosition);
                            break;
                        case DOUBLE:
                            borrowedEvent.setOnAfterWindowData(((Float)dataValue).doubleValue(), toPosition);
                            break;
                        default:
                            // TODO: 6/7/17 exception
                    }
                    break;
                case "java.lang.Double":
                    switch (onAfterWindowData.get(toPosition).getType()) {
                        case INT:
                            borrowedEvent.setOnAfterWindowData(((Double)dataValue).intValue(), toPosition);
                            break;
                        case LONG:
                            borrowedEvent.setOnAfterWindowData(((Double)dataValue).longValue(), toPosition);
                            break;
                        case FLOAT:
                            borrowedEvent.setOnAfterWindowData(((Double)dataValue).floatValue(), toPosition);
                            break;
                        case DOUBLE:
                            borrowedEvent.setOnAfterWindowData(dataValue, toPosition);
                            break;
                        default:
                            // TODO: 6/7/17 exception
                    }
                    break;
                default:
                    // TODO: 6/7/17 exception

            }

        }

        //then put value to borrowedEvent using the map

        // Send to debugger
        if (siddhiDebugger != null) {
            siddhiDebugger.checkBreakPoint(aggregatorName, SiddhiDebugger.QueryTerminal.IN, borrowedEvent);
        }
        execute(new ComplexEventChunk<StreamEvent>(borrowedEvent, borrowedEvent, this.batchProcessingAllowed));
    }

    protected void processAndClear(ComplexEventChunk<StreamEvent> streamEventChunk) {
        next.execute(streamEventChunk);
        streamEventChunk.clear();
    }

    public void setMetaStreamEvent(MetaStreamEvent metaStreamEvent) {
        this.metaStreamEvent = metaStreamEvent;
    }

    public void setOriginalMetaStreamEvent(MetaStreamEvent metaStreamEvent) {
        this.originalMetaStreamEvent = metaStreamEvent;
    }

    public boolean toTable() {
        return metaStreamEvent.isTableEvent();
    }

    public void setBatchProcessingAllowed(boolean batchProcessingAllowed) {
        this.batchProcessingAllowed = batchProcessingAllowed;
    }

    public void setNext(Executor next) {
        this.next = next;
    }

    public void setStreamEventPool(StreamEventPool streamEventPool) {
        this.streamEventPool = streamEventPool;
    }

    public void setLockWrapper(LockWrapper lockWrapper) {
        this.lockWrapper = lockWrapper;
    }

    public void init() {
        streamEventConverter = AggregatorEventConverterFactory.constructEventConverter(metaStreamEvent, originalMetaStreamEvent);
    }

    public void addStatefulProcessor(PreStateProcessor stateProcessor) {
        stateProcessors.add(stateProcessor);
        stateProcessorsSize = stateProcessors.size();
    }

    /*public void setCompositeAggregator(List<CompositeAggregator> compositeAggregator) {
        this.compositeAggregator = compositeAggregator;
    }*/

    public void setMappingPositions(Map<Attribute, Integer> mappingPositions) {
        this.mappingPositions = mappingPositions;
    }
}
