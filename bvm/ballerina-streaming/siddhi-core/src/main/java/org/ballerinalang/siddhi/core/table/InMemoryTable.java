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

package org.ballerinalang.siddhi.core.table;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.state.StateEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventCloner;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.exception.ConnectionUnavailableException;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.VariableExpressionExecutor;
import org.ballerinalang.siddhi.core.table.holder.EventHolder;
import org.ballerinalang.siddhi.core.table.record.RecordTableHandler;
import org.ballerinalang.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledCondition;
import org.ballerinalang.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.ballerinalang.siddhi.core.util.collection.operator.Operator;
import org.ballerinalang.siddhi.core.util.config.ConfigReader;
import org.ballerinalang.siddhi.core.util.parser.EventHolderPasser;
import org.ballerinalang.siddhi.core.util.parser.ExpressionParser;
import org.ballerinalang.siddhi.core.util.parser.OperatorParser;
import org.ballerinalang.siddhi.core.util.snapshot.Snapshotable;
import org.ballerinalang.siddhi.query.api.definition.TableDefinition;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.UpdateSet;
import org.ballerinalang.siddhi.query.api.expression.Expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In-memory event table implementation of SiddhiQL.
 */
public class InMemoryTable extends Table implements Snapshotable {

    private TableDefinition tableDefinition;
    private StreamEventCloner tableStreamEventCloner;
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private EventHolder eventHolder;
    private String elementId;


    @Override
    public void init(TableDefinition tableDefinition, StreamEventPool storeEventPool,
                     StreamEventCloner storeEventCloner, ConfigReader configReader, SiddhiAppContext siddhiAppContext,
                     RecordTableHandler recordTableHandler) {
        this.tableDefinition = tableDefinition;
        this.tableStreamEventCloner = storeEventCloner;

        eventHolder = EventHolderPasser.parse(tableDefinition, storeEventPool, siddhiAppContext);

        if (elementId == null) {
            elementId = "InMemoryTable-" + siddhiAppContext.getElementIdGenerator().createNewId();
        }
        siddhiAppContext.getSnapshotService().addSnapshotable(tableDefinition.getId(), this);
    }

    @Override
    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    @Override
    public void add(ComplexEventChunk<StreamEvent> addingEventChunk) {
        try {
            readWriteLock.writeLock().lock();
            eventHolder.add(addingEventChunk);
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    @Override
    public void delete(ComplexEventChunk<StateEvent> deletingEventChunk, CompiledCondition compiledCondition) {
        try {
            readWriteLock.writeLock().lock();
            ((Operator) compiledCondition).delete(deletingEventChunk, eventHolder);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, CompiledCondition compiledCondition,
                       CompiledUpdateSet compiledUpdateSet) {
        try {
            readWriteLock.writeLock().lock();
            ((Operator) compiledCondition).update(updatingEventChunk, eventHolder,
                    (InMemoryCompiledUpdateSet) compiledUpdateSet);
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    @Override
    public void updateOrAdd(ComplexEventChunk<StateEvent> updateOrAddingEventChunk,
                            CompiledCondition compiledCondition,
                            CompiledUpdateSet compiledUpdateSet,
                            AddingStreamEventExtractor addingStreamEventExtractor) {
        try {
            readWriteLock.writeLock().lock();
            ComplexEventChunk<StreamEvent> failedEvents = ((Operator) compiledCondition).tryUpdate(
                    updateOrAddingEventChunk,
                    eventHolder,
                    (InMemoryCompiledUpdateSet) compiledUpdateSet,
                    addingStreamEventExtractor);
            if (failedEvents != null) {
                eventHolder.add(failedEvents);
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    @Override
    public boolean contains(StateEvent matchingEvent, CompiledCondition compiledCondition) {
        try {
            readWriteLock.readLock().lock();
            return ((Operator) compiledCondition).contains(matchingEvent, eventHolder);
        } finally {
            readWriteLock.readLock().unlock();
        }

    }

    @Override
    protected void connect() throws ConnectionUnavailableException {

    }

    @Override
    protected void disconnect() {

    }

    @Override
    protected void destroy() {

    }

    @Override
    public StreamEvent find(CompiledCondition compiledCondition, StateEvent matchingEvent) {
        try {
            readWriteLock.readLock().lock();
            return ((Operator) compiledCondition).find(matchingEvent, eventHolder, tableStreamEventCloner);
        } finally {
            readWriteLock.readLock().unlock();
        }

    }

    @Override
    public CompiledCondition compileCondition(Expression condition, MatchingMetaInfoHolder matchingMetaInfoHolder,
                                              SiddhiAppContext siddhiAppContext,
                                              List<VariableExpressionExecutor> variableExpressionExecutors,
                                              Map<String, Table> tableMap, String queryName) {
        return OperatorParser.constructOperator(eventHolder, condition, matchingMetaInfoHolder,
                siddhiAppContext, variableExpressionExecutors, tableMap, tableDefinition.getId());
    }

    @Override
    public CompiledUpdateSet compileUpdateSet(UpdateSet updateSet, MatchingMetaInfoHolder matchingMetaInfoHolder,
                                              SiddhiAppContext siddhiAppContext,
                                              List<VariableExpressionExecutor> variableExpressionExecutors,
                                              Map<String, Table> tableMap, String queryName) {
        Map<Integer, ExpressionExecutor> expressionExecutorMap = new HashMap<>();
        for (UpdateSet.SetAttribute setAttribute : updateSet.getSetAttributeList()) {
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(
                    setAttribute.getAssignmentExpression(),
                    matchingMetaInfoHolder.getMetaStateEvent(), matchingMetaInfoHolder.getCurrentState(),
                    tableMap, variableExpressionExecutors, siddhiAppContext, false, 0, queryName);
            int attributePosition = tableDefinition.
                    getAttributePosition(setAttribute.getTableVariable().getAttributeName());
            expressionExecutorMap.put(attributePosition, expressionExecutor);
        }
        return new InMemoryCompiledUpdateSet(expressionExecutorMap);
    }


    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> state = new HashMap<>();
        state.put("EventHolder", eventHolder);
        return state;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        eventHolder = (EventHolder) state.get("EventHolder");
    }

    @Override
    public String getElementId() {
        return elementId;
    }
}
