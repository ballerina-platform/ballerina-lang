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

package org.ballerinalang.siddhi.core.query.output.callback;

import org.ballerinalang.siddhi.core.debugger.SiddhiDebugger;
import org.ballerinalang.siddhi.core.event.ComplexEvent;
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.state.StateEvent;
import org.ballerinalang.siddhi.core.event.state.StateEventPool;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.event.stream.converter.StreamEventConverter;

/**
 * Abstract class to represent parent callback implementation which allows users to get processed events from Siddhi
 * queries. There are multiple implementation of this which will receive events and perform various tasks.
 */
public abstract class OutputCallback {
    private String queryName;
    private SiddhiDebugger siddhiDebugger;

    OutputCallback(String queryName) {
        this.queryName = queryName;
    }

    public abstract void send(ComplexEventChunk complexEventChunk, int noOfEvents);

    SiddhiDebugger getSiddhiDebugger() {
        return siddhiDebugger;
    }

    public void setSiddhiDebugger(SiddhiDebugger siddhiDebugger) {
        this.siddhiDebugger = siddhiDebugger;
    }

    String getQueryName() {
        return queryName;
    }

    protected ComplexEventChunk<StateEvent> constructMatchingStateEventChunk(ComplexEventChunk
                                                                                     matchingComplexEventChunk,
                                                                             boolean convertToStreamEvent,
                                                                             StateEventPool stateEventPool,
                                                                             int matchingStreamIndex, StreamEventPool
                                                                                     streamEventPool,
                                                                             StreamEventConverter
                                                                                     streamEventConverter) {
        ComplexEventChunk<StateEvent> stateEventChunk = new ComplexEventChunk<StateEvent>(matchingComplexEventChunk
                .isBatch());
        while (matchingComplexEventChunk.hasNext()) {
            ComplexEvent matchingComplexEvent = matchingComplexEventChunk.next();
            matchingComplexEventChunk.remove();
            StateEvent stateEvent = stateEventPool.borrowEvent();
            if (convertToStreamEvent) {
                StreamEvent borrowEvent = streamEventPool.borrowEvent();
                streamEventConverter.convertData(
                        matchingComplexEvent.getTimestamp(),
                        matchingComplexEvent.getOutputData(),
                        matchingComplexEvent.getType() == ComplexEvent.Type.EXPIRED ? ComplexEvent.Type.CURRENT :
                                matchingComplexEvent.getType(),
                        borrowEvent);
                stateEvent.addEvent(matchingStreamIndex, borrowEvent);
            } else {
                stateEvent.addEvent(matchingStreamIndex, (StreamEvent) matchingComplexEvent);
            }
            stateEventChunk.add(stateEvent);
        }
        return stateEventChunk;
    }

}
