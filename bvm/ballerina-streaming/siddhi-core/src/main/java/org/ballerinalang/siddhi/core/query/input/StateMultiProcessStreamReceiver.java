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
package org.ballerinalang.siddhi.core.query.input;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.state.StateEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.query.input.stream.state.StreamPreStateProcessor;
import org.ballerinalang.siddhi.core.query.processor.Processor;
import org.ballerinalang.siddhi.core.query.selector.QuerySelector;
import org.ballerinalang.siddhi.core.util.statistics.LatencyTracker;

/**
 * Implementation of {@link org.ballerinalang.siddhi.core.stream.StreamJunction.Receiver}
 * to receive events to be fed into
 * multi stream stateful queries.
 */
public class StateMultiProcessStreamReceiver extends MultiProcessStreamReceiver {

    private QuerySelector querySelector;

    public StateMultiProcessStreamReceiver(String streamId, int processCount, LatencyTracker latencyTracker,
                                           String queryName, SiddhiAppContext siddhiAppContext) {
        super(streamId, processCount, latencyTracker, queryName, siddhiAppContext);
    }

    public void setNext(Processor next) {
        super.setNext(next);
        this.querySelector = (QuerySelector) ((StreamPreStateProcessor) next).getThisStatePostProcessor()
                .getNextProcessor();
    }

    public StateMultiProcessStreamReceiver clone(String key) {
        return new StateMultiProcessStreamReceiver(streamId + key, processCount, latencyTracker, queryName,
                siddhiAppContext);
    }

    protected void processAndClear(int processIndex, StreamEvent streamEvent) {
        ComplexEventChunk<StateEvent> retEventChunk = new ComplexEventChunk<StateEvent>(batchProcessingAllowed);
        ComplexEventChunk<StreamEvent> currentStreamEventChunk = new ComplexEventChunk<StreamEvent>(streamEvent,
                streamEvent, batchProcessingAllowed);

        ComplexEventChunk<StateEvent> eventChunk = ((StreamPreStateProcessor) nextProcessors[processIndex])
                .processAndReturn(currentStreamEventChunk);
        if (eventChunk.getFirst() != null) {
            retEventChunk.add(eventChunk.getFirst());
        }
        eventChunk.clear();

        if (querySelector != null) {
            while (retEventChunk.hasNext()) {
                StateEvent stateEvent = retEventChunk.next();
                retEventChunk.remove();
                querySelector.process(new ComplexEventChunk<StateEvent>(stateEvent, stateEvent,
                        batchProcessingAllowed));
            }
        }

    }

}
