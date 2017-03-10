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

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.stream.window.FindableProcessor;
import org.wso2.siddhi.core.util.collection.OverwritingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaStateHolder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.List;
import java.util.Map;

public interface EventTable extends FindableProcessor {

    void init(TableDefinition tableDefinition, StreamEventPool tableStreamEventPool, StreamEventCloner tableStreamEventCloner, ExecutionPlanContext executionPlanContext);

    TableDefinition getTableDefinition();

    void add(ComplexEventChunk<StreamEvent> addingEventChunk);

    void delete(ComplexEventChunk<StateEvent> deletingEventChunk, Operator operator);

    void update(ComplexEventChunk<StateEvent> updatingEventChunk, Operator operator, UpdateAttributeMapper[] updateAttributeMappers);

    void overwriteOrAdd(ComplexEventChunk<StateEvent> overwritingOrAddingEventChunk, Operator operator, UpdateAttributeMapper[] updateAttributeMappers, OverwritingStreamEventExtractor overwritingStreamEventExtractor);

    boolean contains(StateEvent matchingEvent, Finder finder);

    Operator constructOperator(Expression expression, MatchingMetaStateHolder matchingMetaStateHolder, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap) ;

}
