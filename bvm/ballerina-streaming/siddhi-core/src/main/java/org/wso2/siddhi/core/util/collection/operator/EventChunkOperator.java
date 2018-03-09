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

package org.ballerinalang.siddhi.core.util.collection.operator;

import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.state.StateEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventCloner;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.table.InMemoryCompiledUpdateSet;
import org.ballerinalang.siddhi.core.util.collection.AddingStreamEventExtractor;

import java.util.Map;

/**
 * Operator which is related to non-indexed In-memory table operations.
 */
public class EventChunkOperator implements Operator {
    protected ExpressionExecutor expressionExecutor;
    protected int storeEventPosition;

    public EventChunkOperator(ExpressionExecutor expressionExecutor, int storeEventPosition) {
        this.expressionExecutor = expressionExecutor;
        this.storeEventPosition = storeEventPosition;
    }

    @Override
    public CompiledCondition cloneCompilation(String key) {
        return new EventChunkOperator(expressionExecutor.cloneExecutor(key), storeEventPosition);
    }

    @Override
    public StreamEvent find(StateEvent matchingEvent, Object storeEvents, StreamEventCloner storeEventCloner) {
        ComplexEventChunk<StreamEvent> storeEventChunk = (ComplexEventChunk<StreamEvent>) storeEvents;
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);

        storeEventChunk.reset();
        while (storeEventChunk.hasNext()) {
            StreamEvent storeEvent = storeEventChunk.next();
            matchingEvent.setEvent(storeEventPosition, storeEvent);
            if ((Boolean) expressionExecutor.execute(matchingEvent)) {
                returnEventChunk.add(storeEventCloner.copyStreamEvent(storeEvent));
            }
            matchingEvent.setEvent(storeEventPosition, null);
        }
        return returnEventChunk.getFirst();

    }

    @Override
    public boolean contains(StateEvent matchingEvent, Object storeEvents) {
        ComplexEventChunk<StreamEvent> storeEventChunk = (ComplexEventChunk<StreamEvent>) storeEvents;
        try {
            storeEventChunk.reset();
            while (storeEventChunk.hasNext()) {
                StreamEvent storeEvent = storeEventChunk.next();
                matchingEvent.setEvent(storeEventPosition, storeEvent);
                if ((Boolean) expressionExecutor.execute(matchingEvent)) {
                    return true;
                }
            }
            return false;
        } finally {
            matchingEvent.setEvent(storeEventPosition, null);
        }
    }

    @Override
    public void delete(ComplexEventChunk<StateEvent> deletingEventChunk, Object storeEvents) {
        ComplexEventChunk<StreamEvent> storeEventChunk = (ComplexEventChunk<StreamEvent>) storeEvents;
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            StateEvent deletingEvent = deletingEventChunk.next();
            try {
                storeEventChunk.reset();
                while (storeEventChunk.hasNext()) {
                    StreamEvent storeEvent = storeEventChunk.next();
                    deletingEvent.setEvent(storeEventPosition, storeEvent);
                    if ((Boolean) expressionExecutor.execute(deletingEvent)) {
                        storeEventChunk.remove();
                    }
                }
            } finally {
                deletingEvent.setEvent(storeEventPosition, null);
            }
        }
    }


    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, Object storeEvents,
                       InMemoryCompiledUpdateSet compiledUpdateSet) {
        ComplexEventChunk<StreamEvent> storeEventChunk = (ComplexEventChunk<StreamEvent>) storeEvents;
        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            StateEvent updatingEvent = updatingEventChunk.next();
            try {
                storeEventChunk.reset();
                while (storeEventChunk.hasNext()) {
                    StreamEvent storeEvent = storeEventChunk.next();
                    updatingEvent.setEvent(storeEventPosition, storeEvent);
                    if ((Boolean) expressionExecutor.execute(updatingEvent)) {
                        for (Map.Entry<Integer, ExpressionExecutor> entry :
                                compiledUpdateSet.getExpressionExecutorMap().entrySet()) {
                            storeEvent.setOutputData(entry.getValue().execute(updatingEvent), entry.getKey());
                        }
                    }
                }
            } finally {
                updatingEvent.setEvent(storeEventPosition, null);
            }
        }
    }

    @Override
    public ComplexEventChunk<StreamEvent> tryUpdate(ComplexEventChunk<StateEvent> updatingOrAddingEventChunk, Object
            storeEvents,
                                                    InMemoryCompiledUpdateSet compiledUpdateSet,
                                                    AddingStreamEventExtractor addingStreamEventExtractor) {
        ComplexEventChunk<StreamEvent> storeEventChunk = (ComplexEventChunk<StreamEvent>) storeEvents;
        updatingOrAddingEventChunk.reset();
        ComplexEventChunk<StreamEvent> failedEventChunk = new ComplexEventChunk<StreamEvent>
                (updatingOrAddingEventChunk.isBatch());
        while (updatingOrAddingEventChunk.hasNext()) {
            StateEvent overwritingOrAddingEvent = updatingOrAddingEventChunk.next();
            try {
                boolean updated = false;
                storeEventChunk.reset();
                while (storeEventChunk.hasNext()) {
                    StreamEvent storeEvent = storeEventChunk.next();
                    overwritingOrAddingEvent.setEvent(storeEventPosition, storeEvent);
                    if ((Boolean) expressionExecutor.execute(overwritingOrAddingEvent)) {
                        for (Map.Entry<Integer, ExpressionExecutor> entry :
                                compiledUpdateSet.getExpressionExecutorMap().entrySet()) {
                            storeEvent.setOutputData(entry.getValue().
                                    execute(overwritingOrAddingEvent), entry.getKey());
                        }
                        updated = true;
                    }
                }
                if (!updated) {
                    failedEventChunk.add(addingStreamEventExtractor.getAddingStreamEvent(overwritingOrAddingEvent));
                }
            } finally {
                overwritingOrAddingEvent.setEvent(storeEventPosition, null);
            }
        }
        return failedEventChunk;
    }

}
