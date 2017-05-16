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

package org.wso2.siddhi.core.util.collection.operator;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;

import java.util.Collection;
import java.util.Iterator;

/**
 * Operator which is related to non-indexed In-memory table operations.
 */
public class CollectionOperator implements Operator {
    protected ExpressionExecutor expressionExecutor;
    protected int storeEventPosition;

    public CollectionOperator(ExpressionExecutor expressionExecutor, int storeEventPosition) {
        this.expressionExecutor = expressionExecutor;
        this.storeEventPosition = storeEventPosition;
    }

    @Override
    public CompiledCondition cloneCompiledCondition(String key) {
        return new CollectionOperator(expressionExecutor.cloneExecutor(key), storeEventPosition);
    }

    @Override
    public StreamEvent find(StateEvent matchingEvent, Object storeEvents, StreamEventCloner storeEventCloner) {

        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
        for (StreamEvent storeEvent : (Collection<StreamEvent>) storeEvents) {
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
        try {
            for (StreamEvent storeEvent : (Collection<StreamEvent>) storeEvents) {
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
        if (((Collection<StreamEvent>) storeEvents).size() > 0) {
            deletingEventChunk.reset();
            while (deletingEventChunk.hasNext()) {
                StateEvent deletingEvent = deletingEventChunk.next();
                try {
                    for (Iterator<StreamEvent> iterator = ((Collection<StreamEvent>) storeEvents).iterator();
                         iterator.hasNext(); ) {
                        StreamEvent storeEvent = iterator.next();
                        deletingEvent.setEvent(storeEventPosition, storeEvent);
                        if ((Boolean) expressionExecutor.execute(deletingEvent)) {
                            iterator.remove();
                        }
                    }
                } finally {
                    deletingEvent.setEvent(storeEventPosition, null);
                }
            }
        }
    }


    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, Object storeEvents, UpdateAttributeMapper[]
            updateAttributeMappers) {
        if (((Collection<StreamEvent>) storeEvents).size() > 0) {
            updatingEventChunk.reset();
            while (updatingEventChunk.hasNext()) {
                StateEvent updatingEvent = updatingEventChunk.next();
                try {
                    for (StreamEvent storeEvent : ((Collection<StreamEvent>) storeEvents)) {
                        updatingEvent.setEvent(storeEventPosition, storeEvent);
                        if ((Boolean) expressionExecutor.execute(updatingEvent)) {
                            for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                                updateAttributeMapper.mapOutputData(updatingEvent, storeEvent);
                            }
                        }
                    }
                } finally {
                    updatingEvent.setEvent(storeEventPosition, null);
                }
            }
        }
    }

    @Override
    public ComplexEventChunk<StreamEvent> tryUpdate(ComplexEventChunk<StateEvent> updatingOrAddingEventChunk, Object
            storeEvents,
                                                    UpdateAttributeMapper[] updateAttributeMappers,
                                                    AddingStreamEventExtractor addingStreamEventExtractor) {

        updatingOrAddingEventChunk.reset();
        ComplexEventChunk<StreamEvent> failedEventChunk = new ComplexEventChunk<StreamEvent>
                (updatingOrAddingEventChunk.isBatch());
        while (updatingOrAddingEventChunk.hasNext()) {
            StateEvent updateOrAddingEvent = updatingOrAddingEventChunk.next();
            try {
                boolean updated = false;
                if (((Collection<StreamEvent>) storeEvents).size() > 0) {
                    for (StreamEvent storeEvent : ((Collection<StreamEvent>) storeEvents)) {
                        updateOrAddingEvent.setEvent(storeEventPosition, storeEvent);
                        if ((Boolean) expressionExecutor.execute(updateOrAddingEvent)) {
                            for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                                updateAttributeMapper.mapOutputData(updateOrAddingEvent, storeEvent);
                            }
                            updated = true;
                        }
                    }
                }
                if (!updated) {
                    failedEventChunk.add(addingStreamEventExtractor.getAddingStreamEvent(updateOrAddingEvent));
                }
            } finally {
                updateOrAddingEvent.setEvent(storeEventPosition, null);
            }
        }
        return failedEventChunk;
    }
}
