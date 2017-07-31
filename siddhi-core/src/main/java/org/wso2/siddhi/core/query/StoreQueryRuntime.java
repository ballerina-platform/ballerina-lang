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
package org.wso2.siddhi.core.query;

import org.wso2.siddhi.core.aggregation.AggregationRuntime;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.state.StateEventPool;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.window.Window;

import java.util.ArrayList;
import java.util.List;

/**
 * Store Query Runtime holds the runtime information needed for executing the store query.
 */
public class StoreQueryRuntime {

    private final CompiledCondition compiledCondition;
    private AggregationRuntime aggregation;
    private Table table;
    private Window window;
    private String queryName;
    private MetaStreamEvent.EventType eventType;
    private QuerySelector selector;
    private StateEventPool stateEventPool;

    public StoreQueryRuntime(Table table,
                             CompiledCondition compiledCondition, String queryName,
                             MetaStreamEvent.EventType eventType) {
        this.table = table;
        this.compiledCondition = compiledCondition;
        this.queryName = queryName;
        this.eventType = eventType;
    }

    public StoreQueryRuntime(Window window, CompiledCondition compiledCondition, String queryName,
                             MetaStreamEvent.EventType eventType) {
        this.window = window;
        this.compiledCondition = compiledCondition;
        this.queryName = queryName;
        this.eventType = eventType;
    }

    public StoreQueryRuntime(AggregationRuntime aggregation, CompiledCondition compiledCondition, String queryName,
                             MetaStreamEvent.EventType eventType) {
        this.aggregation = aggregation;
        this.compiledCondition = compiledCondition;
        this.queryName = queryName;
        this.eventType = eventType;
    }

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
                    streamEvents = aggregation.find(stateEvent, compiledCondition);
                    break;
                case DEFAULT:
                    break;
            }
            if (streamEvents == null) {
                return null;
            } else {
                if (selector != null) {
                    return executeSelector(streamEvents);
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
            throw new SiddhiAppRuntimeException("Error executing '" + queryName + "', " + t.getMessage(), t);
        }
    }

    public void setStateEventPool(StateEventPool stateEventPool) {
        this.stateEventPool = stateEventPool;
    }

    public void setSelector(QuerySelector selector) {
        this.selector = selector;
    }

    private Event[] executeSelector(StreamEvent streamEvents) {
        ComplexEventChunk<StateEvent> complexEventChunk = new ComplexEventChunk<StateEvent>(true);
        while (streamEvents != null) {
            StateEvent stateEvent = stateEventPool.borrowEvent();
            stateEvent.addEvent(0, streamEvents);
            complexEventChunk.add(stateEvent);
            streamEvents = streamEvents.getNext();
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
