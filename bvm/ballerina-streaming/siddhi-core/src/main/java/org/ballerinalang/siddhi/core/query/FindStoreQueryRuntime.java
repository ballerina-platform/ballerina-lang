/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.siddhi.core.query;

import org.ballerinalang.siddhi.core.aggregation.AggregationRuntime;
import org.ballerinalang.siddhi.core.event.ComplexEvent;
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.event.state.StateEvent;
import org.ballerinalang.siddhi.core.event.state.StateEventPool;
import org.ballerinalang.siddhi.core.event.stream.MetaStreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.exception.StoreQueryRuntimeException;
import org.ballerinalang.siddhi.core.query.selector.QuerySelector;
import org.ballerinalang.siddhi.core.table.Table;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledCondition;
import org.ballerinalang.siddhi.core.window.Window;
import org.ballerinalang.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Store Query Runtime holds the runtime information needed for executing the store query.
 */
public class FindStoreQueryRuntime implements StoreQueryRuntime {

    private CompiledCondition compiledCondition;
    private Table table;
    private Window window;
    private String queryName;
    private MetaStreamEvent.EventType eventType;
    private AggregationRuntime aggregation;
    private QuerySelector selector;
    private StateEventPool stateEventPool;
    private MetaStreamEvent metaStreamEvent;
    private Attribute[] outputAttributes;

    public FindStoreQueryRuntime(Table table, CompiledCondition compiledCondition, String queryName,
                                 MetaStreamEvent metaStreamEvent) {
        this.table = table;
        this.compiledCondition = compiledCondition;
        this.queryName = queryName;
        this.eventType = metaStreamEvent.getEventType();
        this.metaStreamEvent = metaStreamEvent;
        this.setOutputAttributes(metaStreamEvent.getLastInputDefinition().getAttributeList());
    }

    public FindStoreQueryRuntime(Window window, CompiledCondition compiledCondition, String queryName,
                                 MetaStreamEvent metaStreamEvent) {
        this.window = window;
        this.compiledCondition = compiledCondition;
        this.queryName = queryName;
        this.eventType = metaStreamEvent.getEventType();
        this.metaStreamEvent = metaStreamEvent;
        this.setOutputAttributes(metaStreamEvent.getLastInputDefinition().getAttributeList());
    }

    public FindStoreQueryRuntime(AggregationRuntime aggregation, CompiledCondition compiledCondition, String queryName,
                                 MetaStreamEvent metaStreamEvent) {
        this.aggregation = aggregation;
        this.compiledCondition = compiledCondition;
        this.queryName = queryName;
        this.eventType = metaStreamEvent.getEventType();
        this.metaStreamEvent = metaStreamEvent;
        this.setOutputAttributes(metaStreamEvent.getLastInputDefinition().getAttributeList());
    }

    @Override
    public Event[] execute() {
        try {
            StateEvent stateEvent = new StateEvent(1, 0);
            StreamEvent streamEvents = null;
            switch (eventType) {
                case TABLE:
                    streamEvents = table.find(stateEvent, compiledCondition);
                    break;
                case WINDOW:
                    streamEvents = window.find(stateEvent, compiledCondition);
                    break;
                case AGGREGATE:
                    stateEvent = new StateEvent(2, 0);
                    StreamEvent streamEvent = new StreamEvent(0, 2, 0);
                    stateEvent.addEvent(0, streamEvent);
                    streamEvents = aggregation.find(stateEvent, compiledCondition);
                    break;
                case DEFAULT:
                    break;
            }
            if (streamEvents == null) {
                return null;
            } else {
                if (selector != null) {
                    return executeSelector(streamEvents, eventType);
                } else {
                    List<Event> events = new ArrayList<Event>();
                    while (streamEvents != null) {
                        events.add(new Event(streamEvents.getTimestamp(), streamEvents.getOutputData()));
                        streamEvents = streamEvents.getNext();
                    }
                    return events.toArray(new Event[0]);
                }
            }
        } catch (Throwable t) {
            throw new StoreQueryRuntimeException("Error executing '" + queryName + "', " + t.getMessage(), t);
        }
    }

    @Override
    public void reset() {
        if (selector != null) {
            selector.process(generateResetComplexEventChunk(metaStreamEvent));
        }
    }

    private ComplexEventChunk<ComplexEvent> generateResetComplexEventChunk(MetaStreamEvent metaStreamEvent) {
        StreamEvent streamEvent = new StreamEvent(metaStreamEvent.getBeforeWindowData().size(),
                metaStreamEvent.getOnAfterWindowData().size(), metaStreamEvent.getOutputData().size());
        streamEvent.setType(ComplexEvent.Type.RESET);

        StateEvent stateEvent = stateEventPool.borrowEvent();
        if (eventType == MetaStreamEvent.EventType.AGGREGATE) {
            stateEvent.addEvent(1, streamEvent);
        } else {
            stateEvent.addEvent(0, streamEvent);
        }
        stateEvent.setType(ComplexEvent.Type.RESET);

        ComplexEventChunk<ComplexEvent> complexEventChunk = new ComplexEventChunk<>(true);
        complexEventChunk.add(stateEvent);
        return complexEventChunk;
    }

    public void setStateEventPool(StateEventPool stateEventPool) {
        this.stateEventPool = stateEventPool;
    }

    public void setSelector(QuerySelector selector) {
        this.selector = selector;
    }

    /**
     * This method sets the output attribute list of the given store query.
     *
     * @param outputAttributeList
     */
    public void setOutputAttributes(List<Attribute> outputAttributeList) {
        this.outputAttributes = outputAttributeList.toArray(new Attribute[outputAttributeList.size()]);
    }

    @Override
    public Attribute[] getStoreQueryOutputAttributes() {
        return Arrays.copyOf(outputAttributes, outputAttributes.length);
    }

    private Event[] executeSelector(StreamEvent streamEvents, MetaStreamEvent.EventType eventType) {
        ComplexEventChunk<StateEvent> complexEventChunk = new ComplexEventChunk<>(true);
        while (streamEvents != null) {

            StreamEvent streamEvent = streamEvents;
            streamEvents = streamEvents.getNext();
            streamEvent.setNext(null);

            StateEvent stateEvent = stateEventPool.borrowEvent();
            if (eventType == MetaStreamEvent.EventType.AGGREGATE) {
                stateEvent.addEvent(1, streamEvent);
            } else {
                stateEvent.addEvent(0, streamEvent);
            }
            complexEventChunk.add(stateEvent);
        }
        ComplexEventChunk outputComplexEventChunk = selector.execute(complexEventChunk);
        if (outputComplexEventChunk != null) {
            List<Event> events = new ArrayList<>();
            outputComplexEventChunk.reset();
            while (outputComplexEventChunk.hasNext()) {
                ComplexEvent complexEvent = outputComplexEventChunk.next();
                events.add(new Event(complexEvent.getTimestamp(), complexEvent.getOutputData()));
            }
            return events.toArray(new Event[0]);
        } else {
            return null;
        }

    }
}
