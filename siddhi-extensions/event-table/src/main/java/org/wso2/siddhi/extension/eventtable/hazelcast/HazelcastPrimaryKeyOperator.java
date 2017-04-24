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
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.Operator;


/**
 * Operator which is related to Indexed Hazelcast table operations.
 */
public class HazelcastPrimaryKeyOperator implements Operator {

    protected final ExpressionExecutor expressionExecutor;
    protected final int storeEventPosition;
    protected final int indexPosition;

    public HazelcastPrimaryKeyOperator(ExpressionExecutor expressionExecutor, int storeEventPosition, int indexPosition) {
        this.expressionExecutor = expressionExecutor;
        this.storeEventPosition = storeEventPosition;
        this.indexPosition = indexPosition;
    }

    @Override
    public CompiledCondition cloneCompiledCondition(String key) {
        return new HazelcastPrimaryKeyOperator(expressionExecutor.cloneExecutor(key), storeEventPosition, indexPosition);
    }

    @Override
    public StreamEvent find(StateEvent matchingEvent, Object storeEvents, StreamEventCloner storeEventCloner) {
        Object matchingKey = expressionExecutor.execute(matchingEvent);
        StreamEvent streamEvent = ((HazelcastPrimaryKeyEventHolder) storeEvents).get(matchingKey);
        if (streamEvent == null) {
            return null;
        } else {
            return storeEventCloner.copyStreamEvent(streamEvent);
        }
    }

    @Override
    public boolean contains(StateEvent matchingEvent, Object storeEvents) {
        Object matchingKey = expressionExecutor.execute(matchingEvent);
        StreamEvent streamEvent = ((HazelcastPrimaryKeyEventHolder) storeEvents).get(matchingKey);
        return streamEvent != null;
    }

    @Override
    public void delete(ComplexEventChunk<StateEvent> deletingEventChunk, Object storeEvents) {
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            StateEvent deletingEvent = deletingEventChunk.next();
            Object matchingKey = expressionExecutor.execute(deletingEvent);
            ((HazelcastPrimaryKeyEventHolder) storeEvents).remove(matchingKey);
        }

    }

    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, Object storeEvents, UpdateAttributeMapper[] updateAttributeMappers) {
        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            StateEvent updatingEvent = updatingEventChunk.next();
            Object matchingKey = expressionExecutor.execute(updatingEvent);
            StreamEvent streamEvent = ((HazelcastPrimaryKeyEventHolder) storeEvents).get(matchingKey);
            if (streamEvent != null) {
                for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                    updateAttributeMapper.mapOutputData(updatingEvent, streamEvent);
                }
                ((HazelcastPrimaryKeyEventHolder) storeEvents).replace(matchingKey, streamEvent);

            }
        }
    }

    @Override
    public ComplexEventChunk<StreamEvent> tryUpdate(ComplexEventChunk<StateEvent> updatingOrAddingEventChunk,
                                                    Object storeEvents,
                                                    UpdateAttributeMapper[] updateAttributeMappers,
                                                    AddingStreamEventExtractor addingStreamEventExtractor) {
        updatingOrAddingEventChunk.reset();
        ComplexEventChunk<StreamEvent> failedEventChunk = new ComplexEventChunk<StreamEvent>(updatingOrAddingEventChunk.isBatch());
        while (updatingOrAddingEventChunk.hasNext()) {
            StateEvent overwritingOrAddingEvent = updatingOrAddingEventChunk.next();
            Object matchingKey = expressionExecutor.execute(overwritingOrAddingEvent);
            StreamEvent streamEvent = ((HazelcastPrimaryKeyEventHolder) storeEvents).get(matchingKey);
            if (streamEvent != null) {
                for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                    updateAttributeMapper.mapOutputData(overwritingOrAddingEvent, streamEvent);
                }
                ((HazelcastPrimaryKeyEventHolder) storeEvents).replace(matchingKey, streamEvent);
            } else {
                failedEventChunk.add(addingStreamEventExtractor.getAddingStreamEvent(overwritingOrAddingEvent));
            }
        }
        return failedEventChunk;
    }


}
