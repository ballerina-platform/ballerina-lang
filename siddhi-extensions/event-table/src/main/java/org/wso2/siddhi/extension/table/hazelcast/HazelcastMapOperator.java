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

package org.wso2.siddhi.extension.table.hazelcast;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.MapOperator;

import java.util.Map;

/**
 * Operator which is related to non-indexed In-memory table operations.
 */
public class HazelcastMapOperator extends MapOperator {

    public HazelcastMapOperator(ExpressionExecutor expressionExecutor, int storeEventPosition) {
        super(expressionExecutor, storeEventPosition);
    }

    @Override
    public CompiledCondition cloneCompiledCondition(String key) {
        return new HazelcastMapOperator(expressionExecutor.cloneExecutor(key), storeEventPosition);
    }

    @Override
    public void delete(ComplexEventChunk<StateEvent> deletingEventChunk, Object storeEvents) {
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            StateEvent deletingEvent = deletingEventChunk.next();
            try {
                for (Map.Entry<Object, StreamEvent> storeEventEntry : ((HazelcastPrimaryKeyEventHolder) storeEvents).entrySet()) {
                    deletingEvent.setEvent(storeEventPosition, storeEventEntry.getValue());
                    if ((Boolean) expressionExecutor.execute(deletingEvent)) {
                        ((HazelcastPrimaryKeyEventHolder) storeEvents).remove(storeEventEntry.getKey());
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
                for (Map.Entry<Object, StreamEvent> storeEventEntry : ((HazelcastPrimaryKeyEventHolder) storeEvents).entrySet()) {
                    updatingEvent.setEvent(storeEventPosition, storeEventEntry.getValue());
                    if ((Boolean) expressionExecutor.execute(updatingEvent)) {
                        for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                            updateAttributeMapper.mapOutputData(updatingEvent, storeEventEntry.getValue());
                        }
                        ((HazelcastPrimaryKeyEventHolder) storeEvents).replace(storeEventEntry.getKey(), storeEventEntry.getValue());
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
                for (Map.Entry<Object, StreamEvent> storeEventEntry : ((HazelcastPrimaryKeyEventHolder) storeEvents).entrySet()) {
                    overwritingOrAddingEvent.setEvent(storeEventPosition, storeEventEntry.getValue());
                    if ((Boolean) expressionExecutor.execute(overwritingOrAddingEvent)) {
                        for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                            updateAttributeMapper.mapOutputData(overwritingOrAddingEvent, storeEventEntry.getValue());
                        }
                        ((HazelcastPrimaryKeyEventHolder) storeEvents).replace(storeEventEntry.getKey(), storeEventEntry.getValue());
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
