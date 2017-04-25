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

package org.wso2.siddhi.core.table.record;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.*;

public abstract class AbstractRecordTable implements EventTable {


    private TableDefinition tableDefinition;
    private StreamEventPool storeEventPool;
    private StreamEventCloner storeEventCloner;
    private ExecutionPlanContext executionPlanContext;

    @Override
    public void init(TableDefinition tableDefinition, StreamEventPool storeEventPool,
                     StreamEventCloner storeEventCloner, ExecutionPlanContext executionPlanContext) {
        this.tableDefinition = tableDefinition;
        this.storeEventPool = storeEventPool;
        this.storeEventCloner = storeEventCloner;
        this.executionPlanContext = executionPlanContext;
        init(tableDefinition);
    }

    /**
     * Initializing the Record Table
     *
     * @param tableDefinition definintion of the table with annotations if any
     */
    protected abstract void init(TableDefinition tableDefinition);

    @Override
    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    @Override
    public void add(ComplexEventChunk<StreamEvent> addingEventChunk) {
        List<Object[]> records = new ArrayList<>();
        addingEventChunk.reset();
        while (addingEventChunk.hasNext()) {
            StreamEvent event = addingEventChunk.next();
            records.add(event.getOutputData());
        }
        add(records);

    }

    /**
     * Add records to the Table
     *
     * @param records records that need to be added to the table, each Object[] represent a record and it will match
     *                the attributes of the Table Definition.
     */
    protected abstract void add(List<Object[]> records);

    @Override
    public StreamEvent find(StateEvent matchingEvent, CompiledCondition compiledCondition) {
        RecordStoreCompiledCondition recordStoreCompiledCondition = ((RecordStoreCompiledCondition) compiledCondition);

        Map<String, Object> findConditionParameterMap = new HashMap<>();
        for (Map.Entry<String, ExpressionExecutor> entry : recordStoreCompiledCondition.variableExpressionExecutorMap.entrySet()) {
            findConditionParameterMap.put(entry.getKey(), entry.getValue().execute(matchingEvent));
        }

        Iterator<Object[]> records = find(findConditionParameterMap, recordStoreCompiledCondition.compiledCondition);
        ComplexEventChunk<StreamEvent> streamEventComplexEventChunk = new ComplexEventChunk<>(true);
        if (records != null && records.hasNext()) {
            while (records.hasNext()) {
                Object[] record = records.next();
                StreamEvent streamEvent = storeEventPool.borrowEvent();
                System.arraycopy(record, 0, streamEvent.getOutputData(), 0, record.length);
                streamEventComplexEventChunk.add(streamEvent);
            }
        }
        return streamEventComplexEventChunk.getFirst();
    }

    /**
     * Find records matching the compiled condition
     *
     * @param findConditionParameterMap map of matching StreamVariable Ids and their values corresponding to the compiled
     *                                  condition
     * @param compiledCondition         the compiledCondition against which records should be matched
     * @return RecordIterator of matching records
     */
    protected abstract RecordIterator<Object[]> find(Map<String, Object> findConditionParameterMap,
                                               CompiledCondition compiledCondition);

    @Override
    public boolean contains(StateEvent matchingEvent, CompiledCondition compiledCondition) {
        RecordStoreCompiledCondition recordStoreCompiledCondition = ((RecordStoreCompiledCondition) compiledCondition);

        Map<String, Object> containsConditionParameterMap = new HashMap<>();
        for (Map.Entry<String, ExpressionExecutor> entry :
                recordStoreCompiledCondition.variableExpressionExecutorMap.entrySet()) {
            containsConditionParameterMap.put(entry.getKey(), entry.getValue().execute(matchingEvent));
        }
        return contains(containsConditionParameterMap, recordStoreCompiledCondition.compiledCondition);
    }

    /**
     * Check if matching record exist
     *
     * @param containsConditionParameterMap map of matching StreamVariable Ids and their values corresponding to the
     *                                      compiled condition
     * @param compiledCondition             the compiledCondition against which records should be matched
     * @return if matching record found or not
     */
    protected abstract boolean contains(Map<String, Object> containsConditionParameterMap,
                                        CompiledCondition compiledCondition);

    @Override
    public void delete(ComplexEventChunk<StateEvent> deletingEventChunk, CompiledCondition compiledCondition) {
        RecordStoreCompiledCondition recordStoreCompiledCondition = ((RecordStoreCompiledCondition) compiledCondition);
        List<Map<String, Object>> deleteConditionParameterMaps = new ArrayList<>();
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            StateEvent stateEvent = deletingEventChunk.next();

            Map<String, Object> variableMap = new HashMap<>();
            for (Map.Entry<String, ExpressionExecutor> entry :
                    recordStoreCompiledCondition.variableExpressionExecutorMap.entrySet()) {
                variableMap.put(entry.getKey(), entry.getValue().execute(stateEvent));
            }

            deleteConditionParameterMaps.add(variableMap);
        }
        delete(deleteConditionParameterMaps, recordStoreCompiledCondition.compiledCondition);
    }

    /**
     * Delete all matching records
     *
     * @param deleteConditionParameterMaps map of matching StreamVariable Ids and their values corresponding to the
     *                                     compiled condition
     * @param compiledCondition            the compiledCondition against which records should be matched for deletion
     */
    protected abstract void delete(List<Map<String, Object>> deleteConditionParameterMaps,
                                   CompiledCondition compiledCondition);

    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, CompiledCondition compiledCondition,
                       UpdateAttributeMapper[] updateAttributeMappers) {
        RecordStoreCompiledCondition recordStoreCompiledCondition = ((RecordStoreCompiledCondition) compiledCondition);
        List<Map<String, Object>> updateConditionParameterMaps = new ArrayList<>();
        List<Map<String, Object>> updateValues = new ArrayList<>();
        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            StateEvent stateEvent = updatingEventChunk.next();

            Map<String, Object> variableMap = new HashMap<>();
            for (Map.Entry<String, ExpressionExecutor> entry :
                    recordStoreCompiledCondition.variableExpressionExecutorMap.entrySet()) {
                variableMap.put(entry.getKey(), entry.getValue().execute(stateEvent));
            }
            updateConditionParameterMaps.add(variableMap);

            Map<String, Object> valueMap = new HashMap<>();
            for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                valueMap.put(updateAttributeMapper.getStoreEventAttributeName(),
                        updateAttributeMapper.getUpdateEventOutputData(stateEvent));
            }
            updateValues.add(valueMap);
        }
        update(updateConditionParameterMaps, recordStoreCompiledCondition.compiledCondition, updateValues);
    }

    /**
     * Update all matching records
     *
     * @param updateConditionParameterMaps map of matching StreamVariable Ids and their values corresponding to the
     *                                     compiled condition based on which the records will be updated
     * @param compiledCondition            the compiledCondition against which records should be matched for update
     * @param updateValues                 the attributes and values that should be updated for the matching records
     */
    protected abstract void update(List<Map<String, Object>> updateConditionParameterMaps,
                                   CompiledCondition compiledCondition,
                                   List<Map<String, Object>> updateValues);

    @Override
    public void updateOrAdd(ComplexEventChunk<StateEvent> updateOrAddingEventChunk,
                            CompiledCondition compiledCondition, UpdateAttributeMapper[] updateAttributeMappers,
                            AddingStreamEventExtractor addingStreamEventExtractor) {
        RecordStoreCompiledCondition recordStoreCompiledCondition = ((RecordStoreCompiledCondition) compiledCondition);
        List<Map<String, Object>> updateConditionParameterMaps = new ArrayList<>();
        List<Map<String, Object>> updateValues = new ArrayList<>();
        List<Object[]> addingRecords = new ArrayList<>();
        updateOrAddingEventChunk.reset();
        while (updateOrAddingEventChunk.hasNext()) {
            StateEvent stateEvent = updateOrAddingEventChunk.next();

            Map<String, Object> variableMap = new HashMap<>();
            for (Map.Entry<String, ExpressionExecutor> entry :
                    recordStoreCompiledCondition.variableExpressionExecutorMap.entrySet()) {
                variableMap.put(entry.getKey(), entry.getValue().execute(stateEvent));
            }
            updateConditionParameterMaps.add(variableMap);

            Map<String, Object> valueMap = new HashMap<>();
            for (UpdateAttributeMapper updateAttributeMapper : updateAttributeMappers) {
                valueMap.put(updateAttributeMapper.getStoreEventAttributeName(),
                        updateAttributeMapper.getUpdateEventOutputData(stateEvent));
            }
            updateValues.add(valueMap);
            addingRecords.add(stateEvent.getOutputData());
        }
        updateOrAdd(updateConditionParameterMaps, recordStoreCompiledCondition.compiledCondition, updateValues,
                addingRecords);

    }

    /**
     * Try updating the records if they exist else add the records
     *
     * @param updateConditionParameterMaps map of matching StreamVariable Ids and their values corresponding to the
     *                                     compiled condition based on which the records will be updated
     * @param compiledCondition            the compiledCondition against which records should be matched for update
     * @param updateValues                 the attributes and values that should be updated if the condition matches
     * @param addingRecords                the values for adding new records if the update condition did not match
     */
    protected abstract void updateOrAdd(List<Map<String, Object>> updateConditionParameterMaps,
                                        CompiledCondition compiledCondition,
                                        List<Map<String, Object>> updateValues,
                                        List<Object[]> addingRecords);

    @Override
    public CompiledCondition compileCondition(Expression expression,
                                              MatchingMetaInfoHolder matchingMetaInfoHolder,
                                              ExecutionPlanContext executionPlanContext,
                                              List<VariableExpressionExecutor> variableExpressionExecutors,
                                              Map<String, EventTable> eventTableMap, String queryName) {
        ConditionBuilder conditionBuilder = new ConditionBuilder(expression, matchingMetaInfoHolder,
                executionPlanContext, variableExpressionExecutors, eventTableMap, queryName);
        CompiledCondition compiledCondition = compileCondition(conditionBuilder);
        Map<String, ExpressionExecutor> expressionExecutorMap = conditionBuilder.getVariableExpressionExecutorMap();
        return new RecordStoreCompiledCondition(expressionExecutorMap, compiledCondition);
    }

    /**
     * Compile the matching condition
     *
     * @param conditionBuilder that helps visiting the conditions in order to compile the condition
     * @return compiled condition that can be used for matching events in find, contains, delete, update and
     * updateOrAdd
     */
    protected abstract CompiledCondition compileCondition(ConditionBuilder conditionBuilder);

    private class RecordStoreCompiledCondition implements CompiledCondition {
        private Map<String, ExpressionExecutor> variableExpressionExecutorMap;
        private CompiledCondition compiledCondition;

        RecordStoreCompiledCondition(Map<String, ExpressionExecutor> variableExpressionExecutorMap,
                                     CompiledCondition compiledCondition) {
            this.variableExpressionExecutorMap = variableExpressionExecutorMap;
            this.compiledCondition = compiledCondition;
        }

        @Override
        public CompiledCondition cloneCompiledCondition(String key) {
            Map<String, ExpressionExecutor> newVariableExpressionExecutorMap = new HashMap<>();
            for (Map.Entry<String, ExpressionExecutor> entry : variableExpressionExecutorMap.entrySet()) {
                newVariableExpressionExecutorMap.put(entry.getKey(), entry.getValue().cloneExecutor(key));
            }
            return new RecordStoreCompiledCondition(newVariableExpressionExecutorMap, compiledCondition);
        }
    }
}
