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

package org.wso2.siddhi.core.table;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.query.processor.stream.window.FindableProcessor;
import org.wso2.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.TableDefinition;

/**
 * Interface class to represent Tables in Siddhi. There are multiple implementations. Ex: {@link InMemoryTable}. Table
 * will support basic operations of add, delete, update, update or add and contains. *
 */
public interface Table extends FindableProcessor {

    void init(TableDefinition tableDefinition, StreamEventPool storeEventPool, StreamEventCloner storeEventCloner,
              ConfigReader configReader, SiddhiAppContext siddhiAppContext);

    TableDefinition getTableDefinition();

    void add(ComplexEventChunk<StreamEvent> addingEventChunk);

    void delete(ComplexEventChunk<StateEvent> deletingEventChunk, CompiledCondition compiledCondition);

    void update(ComplexEventChunk<StateEvent> updatingEventChunk, CompiledCondition compiledCondition,
                UpdateAttributeMapper[] updateAttributeMappers);

    void updateOrAdd(ComplexEventChunk<StateEvent> updateOrAddingEventChunk,
                     CompiledCondition compiledCondition,
                     UpdateAttributeMapper[] updateAttributeMappers,
                     AddingStreamEventExtractor addingStreamEventExtractor);

    boolean contains(StateEvent matchingEvent, CompiledCondition compiledCondition);

}
