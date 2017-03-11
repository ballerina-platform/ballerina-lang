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

package org.wso2.siddhi.extension.eventtable.hazelcast;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.collection.OverwritingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.CollectionOperator;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;

import java.util.Iterator;

/**
 * Operator which is related to non-indexed Hazelcast table operations.
 */
public class HazelcastCollectionOperator extends CollectionOperator {

    public HazelcastCollectionOperator(ExpressionExecutor expressionExecutor, int candidateEventPosition) {
        super(expressionExecutor, candidateEventPosition);
    }

    @Override
    public CompiledCondition cloneCompiledCondition(String key) {
        return new HazelcastCollectionOperator(expressionExecutor, candidateEventPosition);
    }

    @Override
    public void delete(ComplexEventChunk<StateEvent> deletingEventChunk, Object candidateEvents) {
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            StateEvent deletingEvent = deletingEventChunk.next();
            try {
                for (Iterator<StreamEvent> iterator = ((HazelcastCollectionEventHolder) candidateEvents).iterator(); iterator.hasNext(); ) {
                    StreamEvent candidateEvent = iterator.next();
                    deletingEvent.setEvent(candidateEventPosition, candidateEvent);
                    if ((Boolean) expressionExecutor.execute(deletingEvent)) {
                        ((HazelcastCollectionEventHolder) candidateEvents).remove(candidateEvent);
                    }
                }
            } finally {
                deletingEvent.setEvent(candidateEventPosition, null);
            }
        }
    }

    /**
     * Called when updating the event table entries.
     *
     * @param updatingEventChunk     Event list that needs to be updated.
     * @param candidateEvents        Map of candidate events.
     * @param updateAttributeMappers Mapping positions array.
     */
    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, Object candidateEvents, UpdateAttributeMapper[] updateAttributeMappers) {
        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            StateEvent updatingEvent = updatingEventChunk.next();
            try {
                for (int i = 0; i < ((HazelcastCollectionEventHolder) candidateEvents).size(); i++) {
                    StreamEvent candidateEvent = ((HazelcastCollectionEventHolder) candidateEvents).get(i);
                    updatingEvent.setEvent(candidateEventPosition, candidateEvent);
                    if ((Boolean) expressionExecutor.execute(updatingEvent)) {
                        for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                            candidateEvent.setOutputData(updateAttributeMapper.getOutputData(updatingEvent),
                                    updateAttributeMapper.getCandidateAttributePosition());
                        }
                        ((HazelcastCollectionEventHolder) candidateEvents).set(i, candidateEvent);
                    }
                }
            } finally {
                updatingEvent.setEvent(candidateEventPosition, null);
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
                for (int i = 0; i < ((HazelcastCollectionEventHolder) candidateEvents).size(); i++) {
                    StreamEvent candidateEvent = ((HazelcastCollectionEventHolder) candidateEvents).get(i);
                    overwritingOrAddingEvent.setEvent(candidateEventPosition, candidateEvent);
                    if ((Boolean) expressionExecutor.execute(overwritingOrAddingEvent)) {
                        for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                            candidateEvent.setOutputData(updateAttributeMapper.getOutputData(overwritingOrAddingEvent),
                                    updateAttributeMapper.getCandidateAttributePosition());
                        }
                        ((HazelcastCollectionEventHolder) candidateEvents).set(i, candidateEvent);
                        updated = true;
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
