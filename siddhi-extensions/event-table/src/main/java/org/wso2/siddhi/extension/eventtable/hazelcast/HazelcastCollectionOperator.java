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
import org.wso2.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.CollectionOperator;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;

import java.util.Iterator;

/**
 * Operator which is related to non-indexed Hazelcast table operations.
 */
public class HazelcastCollectionOperator extends CollectionOperator {

    public HazelcastCollectionOperator(ExpressionExecutor expressionExecutor, int storeEventPosition) {
        super(expressionExecutor, storeEventPosition);
    }

    @Override
    public CompiledCondition cloneCompiledCondition(String key) {
        return new HazelcastCollectionOperator(expressionExecutor, storeEventPosition);
    }

    @Override
    public void delete(ComplexEventChunk<StateEvent> deletingEventChunk, Object storeEvents) {
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            StateEvent deletingEvent = deletingEventChunk.next();
            try {
                for (Iterator<StreamEvent> iterator = ((HazelcastCollectionEventHolder) storeEvents).iterator(); iterator.hasNext(); ) {
                    StreamEvent storeEvent = iterator.next();
                    deletingEvent.setEvent(storeEventPosition, storeEvent);
                    if ((Boolean) expressionExecutor.execute(deletingEvent)) {
                        ((HazelcastCollectionEventHolder) storeEvents).remove(storeEvent);
                    }
                }
            } finally {
                deletingEvent.setEvent(storeEventPosition, null);
            }
        }
    }

    /**
     * Called when updating the event table entries.
     *
     * @param updatingEventChunk     Event list that needs to be updated.
     * @param storeEvents            Map of store events.
     * @param updateAttributeMappers Mapping positions array.
     */
    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, Object storeEvents, UpdateAttributeMapper[] updateAttributeMappers) {
        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            StateEvent updatingEvent = updatingEventChunk.next();
            try {
                for (int i = 0; i < ((HazelcastCollectionEventHolder) storeEvents).size(); i++) {
                    StreamEvent storeEvent = ((HazelcastCollectionEventHolder) storeEvents).get(i);
                    updatingEvent.setEvent(storeEventPosition, storeEvent);
                    if ((Boolean) expressionExecutor.execute(updatingEvent)) {
                        for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                            updateAttributeMapper.mapOutputData(updatingEvent, storeEvent);
                        }
                        ((HazelcastCollectionEventHolder) storeEvents).set(i, storeEvent);
                    }
                }
            } finally {
                updatingEvent.setEvent(storeEventPosition, null);
            }
        }
    }

    @Override
    public ComplexEventChunk<StreamEvent> tryUpdate(ComplexEventChunk<StateEvent> updatingOrAddingEventChunk, Object storeEvents,
                                                    UpdateAttributeMapper[] updateAttributeMappers, AddingStreamEventExtractor addingStreamEventExtractor) {
        updatingOrAddingEventChunk.reset();
        ComplexEventChunk<StreamEvent> failedEventChunk = new ComplexEventChunk<StreamEvent>(updatingOrAddingEventChunk.isBatch());
        while (updatingOrAddingEventChunk.hasNext()) {
            StateEvent overwritingOrAddingEvent = updatingOrAddingEventChunk.next();
            try {
                boolean updated = false;
                for (int i = 0; i < ((HazelcastCollectionEventHolder) storeEvents).size(); i++) {
                    StreamEvent storeEvent = ((HazelcastCollectionEventHolder) storeEvents).get(i);
                    overwritingOrAddingEvent.setEvent(storeEventPosition, storeEvent);
                    if ((Boolean) expressionExecutor.execute(overwritingOrAddingEvent)) {
                        for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                            updateAttributeMapper.mapOutputData(overwritingOrAddingEvent, storeEvent);
                        }
                        ((HazelcastCollectionEventHolder) storeEvents).set(i, storeEvent);
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
