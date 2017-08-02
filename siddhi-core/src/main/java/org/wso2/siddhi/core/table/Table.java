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
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.stream.window.FindableProcessor;
import org.wso2.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.operator.CompiledExpression;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.execution.query.output.stream.UpdateSet;

import java.util.List;
import java.util.Map;

/**
 * Interface class to represent Tables in Siddhi. There are multiple implementations. Ex: {@link InMemoryTable}. Table
 * will support basic operations of add, delete, update, update or add and contains. *
 */
public interface Table extends FindableProcessor {

    void init(TableDefinition tableDefinition, StreamEventPool storeEventPool, StreamEventCloner storeEventCloner,
              ConfigReader configReader, SiddhiAppContext siddhiAppContext);

    TableDefinition getTableDefinition();

    void add(ComplexEventChunk<StreamEvent> addingEventChunk);

    void delete(ComplexEventChunk<StateEvent> deletingEventChunk, CompiledExpression compiledExpression);

    void update(ComplexEventChunk<StateEvent> updatingEventChunk, CompiledExpression compiledExpression,
                CompiledUpdateSet compiledUpdateSet);

    void updateOrAdd(ComplexEventChunk<StateEvent> updateOrAddingEventChunk,
                     CompiledExpression compiledExpression,
                     CompiledUpdateSet compiledUpdateSet,
                     AddingStreamEventExtractor addingStreamEventExtractor);

    boolean contains(StateEvent matchingEvent, CompiledExpression compiledExpression);

    /**
     * Builds the "compiled" set clause of an update query.
     * Here, all the pre-processing that can be done prior to receiving the update event is done,
     * so that such pre-processing work will not be done at each update-event-arrival.
     *
     * @param updateSet                   the set of assignment expressions, each containing the table column to be
     *                                    updated and the expression to be assigned.
     * @param matchingMetaInfoHolder      the meta structure of the incoming matchingEvent
     * @param siddhiAppContext            current siddhi app context
     * @param variableExpressionExecutors the list of variable ExpressionExecutors already created
     * @param tableMap                    map of event tables
     * @param queryName                   query name to which the update statement belongs.
     * @return
     */
    CompiledUpdateSet compileUpdateSet(UpdateSet updateSet,
                                       MatchingMetaInfoHolder matchingMetaInfoHolder,
                                       SiddhiAppContext siddhiAppContext,
                                       List<VariableExpressionExecutor> variableExpressionExecutors,
                                       Map<String, Table> tableMap, String queryName);
}
