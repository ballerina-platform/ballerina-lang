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

import org.ballerinalang.siddhi.core.event.Event;
import org.ballerinalang.siddhi.core.event.state.StateEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.exception.StoreQueryRuntimeException;
import org.ballerinalang.siddhi.core.query.processor.stream.window.QueryableProcessor;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledCondition;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledSelection;
import org.ballerinalang.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Store Query Runtime holds the runtime information needed for executing the store query.
 */
public class SelectStoreQueryRuntime implements StoreQueryRuntime {

    private final CompiledSelection compiledSelection;
    private final CompiledCondition compiledCondition;
    private final String queryName;
    private QueryableProcessor queryableProcessor;
    private Attribute[] outputAttributes;

    public SelectStoreQueryRuntime(QueryableProcessor queryableProcessor, CompiledCondition compiledCondition,
                                   CompiledSelection compiledSelection, List<Attribute> expectedOutputAttributeList,
                                   String queryName) {
        this.queryableProcessor = queryableProcessor;

        this.compiledCondition = compiledCondition;
        this.compiledSelection = compiledSelection;
        this.queryName = queryName;
        this.outputAttributes = expectedOutputAttributeList.toArray(new Attribute[expectedOutputAttributeList.size()]);
    }

    @Override
    public Attribute[] getStoreQueryOutputAttributes() {
        return Arrays.copyOf(outputAttributes, outputAttributes.length);
    }

    public Event[] execute() {
        try {
            StateEvent stateEvent = new StateEvent(1, 0);
            StreamEvent streamEvents = queryableProcessor.query(stateEvent, compiledCondition, compiledSelection);
            if (streamEvents == null) {
                return null;
            } else {
                List<Event> events = new ArrayList<Event>();
                while (streamEvents != null) {
                    events.add(new Event(streamEvents.getTimestamp(), streamEvents.getOutputData()));
                    streamEvents = streamEvents.getNext();
                }
                return events.toArray(new Event[0]);
            }
        } catch (Throwable t) {
            throw new StoreQueryRuntimeException("Error executing '" + queryName + "', " + t.getMessage(), t);
        }
    }

    @Override
    public void reset() {

    }

}
