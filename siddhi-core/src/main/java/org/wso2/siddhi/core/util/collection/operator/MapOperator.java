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

import java.util.Map;

/**
 * Operator which is related to non-indexed In-memory table operations.
 */
public class MapOperator extends CollectionOperator {

    public MapOperator(ExpressionExecutor expressionExecutor, int storeEventPosition) {
        super(expressionExecutor, storeEventPosition);
    }

    @Override
    public CompiledCondition cloneCompiledCondition(String key) {
        return new MapOperator(expressionExecutor.cloneExecutor(key), storeEventPosition);
    }

    @Override
    public StreamEvent find(StateEvent matchingEvent, Object storeEvents, StreamEventCloner storeEventCloner) {
        return super.find(matchingEvent, ((Map<Object, StreamEvent>) storeEvents).values(), storeEventCloner);
    }

    @Override
    public boolean contains(StateEvent matchingEvent, Object storeEvents) {
        return super.contains(matchingEvent, ((Map<Object, StreamEvent>) storeEvents).values());
    }

    @Override
    public void delete(ComplexEventChunk<StateEvent> deletingEventChunk, Object storeEvents) {
        super.delete(deletingEventChunk, ((Map<Object, StreamEvent>) storeEvents).values());
    }

    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, Object storeEvents, UpdateAttributeMapper[]
            updateAttributeMappers) {
        super.update(updatingEventChunk, ((Map<Object, StreamEvent>) storeEvents).values(), updateAttributeMappers);
    }

    @Override
    public ComplexEventChunk<StreamEvent> tryUpdate(ComplexEventChunk<StateEvent> updatingOrAddingEventChunk,
                                                    Object storeEvents,
                                                    UpdateAttributeMapper[] updateAttributeMappers,
                                                    AddingStreamEventExtractor addingStreamEventExtractor) {
        return super.tryUpdate(updatingOrAddingEventChunk, ((Map<Object, StreamEvent>) storeEvents).values(),
                updateAttributeMappers, addingStreamEventExtractor);
    }

}
