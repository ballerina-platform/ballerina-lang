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
import org.wso2.siddhi.core.util.collection.OverwritingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;

import java.util.Collection;
import java.util.Iterator;

/**
 * Operator which is related to non-indexed In-memory table operations.
 */
public class CollectionOperator implements Operator {
    protected ExpressionExecutor expressionExecutor;
    protected int candidateEventPosition;

    public CollectionOperator(ExpressionExecutor expressionExecutor, int candidateEventPosition) {
        this.expressionExecutor = expressionExecutor;
        this.candidateEventPosition = candidateEventPosition;
    }

    @Override
    public CompiledCondition cloneCompiledCondition(String key) {
        return new CollectionOperator(expressionExecutor.cloneExecutor(key), candidateEventPosition);
    }

    @Override
    public StreamEvent find(StateEvent matchingEvent, Object candidateEvents, StreamEventCloner candidateEventCloner) {

        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
        for (StreamEvent candidateEvent : (Collection<StreamEvent>) candidateEvents) {
            matchingEvent.setEvent(candidateEventPosition, candidateEvent);
            if ((Boolean) expressionExecutor.execute(matchingEvent)) {
                returnEventChunk.add(candidateEventCloner.copyStreamEvent(candidateEvent));
            }
            matchingEvent.setEvent(candidateEventPosition, null);
        }
        return returnEventChunk.getFirst();

    }

    @Override
    public boolean contains(StateEvent matchingEvent, Object candidateEvents) {
        try {
            for (StreamEvent candidateEvent : (Collection<StreamEvent>) candidateEvents) {
                matchingEvent.setEvent(candidateEventPosition, candidateEvent);
                if ((Boolean) expressionExecutor.execute(matchingEvent)) {
                    return true;
                }
            }
            return false;
        } finally {
            matchingEvent.setEvent(candidateEventPosition, null);
        }
    }

    @Override
    public void delete(ComplexEventChunk<StateEvent> deletingEventChunk, Object candidateEvents) {
        if (((Collection<StreamEvent>) candidateEvents).size() > 0) {
            deletingEventChunk.reset();
            while (deletingEventChunk.hasNext()) {
                StateEvent deletingEvent = deletingEventChunk.next();
                try {
                    for (Iterator<StreamEvent> iterator = ((Collection<StreamEvent>) candidateEvents).iterator(); iterator.hasNext(); ) {
                        StreamEvent candidateEvent = iterator.next();
                        deletingEvent.setEvent(candidateEventPosition, candidateEvent);
                        if ((Boolean) expressionExecutor.execute(deletingEvent)) {
                            iterator.remove();
                        }
                    }
                } finally {
                    deletingEvent.setEvent(candidateEventPosition, null);
                }
            }
        }
    }


    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, Object candidateEvents, UpdateAttributeMapper[] updateAttributeMappers) {
        if (((Collection<StreamEvent>) candidateEvents).size() > 0) {
            updatingEventChunk.reset();
            while (updatingEventChunk.hasNext()) {
                StateEvent updatingEvent = updatingEventChunk.next();
                try {
                    for (StreamEvent candidateEvent : ((Collection<StreamEvent>) candidateEvents)) {
                        updatingEvent.setEvent(candidateEventPosition, candidateEvent);
                        if ((Boolean) expressionExecutor.execute(updatingEvent)) {
                            for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                                candidateEvent.setOutputData(updateAttributeMapper.getOutputData(updatingEvent),
                                        updateAttributeMapper.getCandidateAttributePosition());
                            }
                        }
                    }
                } finally {
                    updatingEvent.setEvent(candidateEventPosition, null);
                }
            }
        }
    }

    @Override
    public ComplexEventChunk<StreamEvent> overwrite(ComplexEventChunk<StateEvent> overwritingOrAddingEventChunk, Object candidateEvents,
                                                    UpdateAttributeMapper[] updateAttributeMappers, OverwritingStreamEventExtractor overwritingStreamEventExtractor) {

        overwritingOrAddingEventChunk.reset();
        ComplexEventChunk<StreamEvent> failedEventChunk = new ComplexEventChunk<StreamEvent>(overwritingOrAddingEventChunk.isBatch());
        while (overwritingOrAddingEventChunk.hasNext()) {
            StateEvent overwritingOrAddingEvent = overwritingOrAddingEventChunk.next();
            try {
                boolean updated = false;
                if (((Collection<StreamEvent>) candidateEvents).size() > 0) {
                    for (StreamEvent candidateEvent : ((Collection<StreamEvent>) candidateEvents)) {
                        overwritingOrAddingEvent.setEvent(candidateEventPosition, candidateEvent);
                        if ((Boolean) expressionExecutor.execute(overwritingOrAddingEvent)) {
                            for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                                candidateEvent.setOutputData(updateAttributeMapper.getOutputData(overwritingOrAddingEvent),
                                        updateAttributeMapper.getCandidateAttributePosition());
                            }
                            updated = true;
                        }
                    }
                }
                if (!updated) {
                    failedEventChunk.add(overwritingStreamEventExtractor.getOverwritingStreamEvent(overwritingOrAddingEvent));
                }
            } finally {
                overwritingOrAddingEvent.setEvent(candidateEventPosition, null);
            }
        }
        return failedEventChunk;
    }
}
