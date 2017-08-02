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

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.CompiledUpdateSet;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.operator.CompiledExpression;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.execution.query.output.stream.UpdateSet;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An abstract implementation of table. Abstract implementation will handle {@link ComplexEventChunk} so that
 * developer can directly work with event data.
 */
public abstract class AbstractRecordTable implements Table {

    private TableDefinition tableDefinition;
    private StreamEventPool storeEventPool;

    @Override
    public void init(TableDefinition tableDefinition, StreamEventPool storeEventPool,
                     StreamEventCloner storeEventCloner, ConfigReader configReader, SiddhiAppContext
                             siddhiAppContext) {
        this.tableDefinition = tableDefinition;
        this.storeEventPool = storeEventPool;
        init(tableDefinition, configReader);
    }

    /**
     * Initializing the Record Table
     *
     * @param tableDefinition definintion of the table with annotations if any
     * @param configReader this hold the {@link AbstractRecordTable} configuration reader.
     */
    protected abstract void init(TableDefinition tableDefinition, ConfigReader configReader);

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
    public StreamEvent find(StateEvent matchingEvent, CompiledExpression compiledExpression) {
        RecordStoreCompiledExpression recordStoreCompiledExpression = ((RecordStoreCompiledExpression) compiledExpression);

        Map<String, Object> findConditionParameterMap = new HashMap<>();
        for (Map.Entry<String, ExpressionExecutor> entry : recordStoreCompiledExpression.variableExpressionExecutorMap
                .entrySet()) {
            findConditionParameterMap.put(entry.getKey(), entry.getValue().execute(matchingEvent));
        }

        Iterator<Object[]> records = find(findConditionParameterMap, recordStoreCompiledExpression.compiledExpression);
        ComplexEventChunk<StreamEvent> streamEventComplexEventChunk = new ComplexEventChunk<>(true);
        if (records != null) {
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
     * @param findConditionParameterMap map of matching StreamVariable Ids and their values
     *                                  corresponding to the compiled condition
     * @param compiledExpression        the compiledExpression against which records should be matched
     * @return RecordIterator of matching records
     */
    protected abstract RecordIterator<Object[]> find(Map<String, Object> findConditionParameterMap,
                                                     CompiledExpression compiledExpression);

    @Override
    public boolean contains(StateEvent matchingEvent, CompiledExpression compiledExpression) {
        RecordStoreCompiledExpression recordStoreCompiledExpression = ((RecordStoreCompiledExpression) compiledExpression);

        Map<String, Object> containsConditionParameterMap = new HashMap<>();
        for (Map.Entry<String, ExpressionExecutor> entry :
                recordStoreCompiledExpression.variableExpressionExecutorMap.entrySet()) {
            containsConditionParameterMap.put(entry.getKey(), entry.getValue().execute(matchingEvent));
        }
        return contains(containsConditionParameterMap, recordStoreCompiledExpression.compiledExpression);
    }

    /**
     * Check if matching record exist
     *
     * @param containsConditionParameterMap map of matching StreamVariable Ids and their values corresponding to the
     *                                      compiled condition
     * @param compiledExpression             the compiledExpression against which records should be matched
     * @return if matching record found or not
     */
    protected abstract boolean contains(Map<String, Object> containsConditionParameterMap,
                                        CompiledExpression compiledExpression);

    @Override
    public void delete(ComplexEventChunk<StateEvent> deletingEventChunk, CompiledExpression compiledExpression) {
        RecordStoreCompiledExpression recordStoreCompiledExpression = ((RecordStoreCompiledExpression) compiledExpression);
        List<Map<String, Object>> deleteConditionParameterMaps = new ArrayList<>();
        deletingEventChunk.reset();
        while (deletingEventChunk.hasNext()) {
            StateEvent stateEvent = deletingEventChunk.next();

            Map<String, Object> variableMap = new HashMap<>();
            for (Map.Entry<String, ExpressionExecutor> entry :
                    recordStoreCompiledExpression.variableExpressionExecutorMap.entrySet()) {
                variableMap.put(entry.getKey(), entry.getValue().execute(stateEvent));
            }

            deleteConditionParameterMaps.add(variableMap);
        }
        delete(deleteConditionParameterMaps, recordStoreCompiledExpression.compiledExpression);
    }

    /**
     * Delete all matching records
     *
     * @param deleteConditionParameterMaps map of matching StreamVariable Ids and their values corresponding to the
     *                                     compiled condition
     * @param compiledExpression            the compiledExpression against which records should be matched for deletion
     */
    protected abstract void delete(List<Map<String, Object>> deleteConditionParameterMaps,
                                   CompiledExpression compiledExpression);

    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, CompiledExpression compiledExpression,
                       CompiledUpdateSet compiledUpdateSet) {
        RecordStoreCompiledExpression recordStoreCompiledExpression = ((RecordStoreCompiledExpression) compiledExpression);
        RecordTableCompiledUpdateSet recordTableCompiledUpdateSet = (RecordTableCompiledUpdateSet) compiledUpdateSet;
        List<Map<String, Object>> updateConditionParameterMaps = new ArrayList<>();
        List<Map<String, Object>> updateSetParameterMaps = new ArrayList<>();
        updatingEventChunk.reset();
        while (updatingEventChunk.hasNext()) {
            StateEvent stateEvent = updatingEventChunk.next();

            Map<String, Object> variableMap = new HashMap<>();
            for (Map.Entry<String, ExpressionExecutor> entry :
                    recordStoreCompiledExpression.variableExpressionExecutorMap.entrySet()) {
                variableMap.put(entry.getKey(), entry.getValue().execute(stateEvent));
            }
            updateConditionParameterMaps.add(variableMap);

            Map<String, Object> variableMapForUpdateSet = new HashMap<>();
            for (Map.Entry<String, ExpressionExecutor> entry :
                    recordTableCompiledUpdateSet.getExpressionExecutorMap().entrySet()) {
                variableMapForUpdateSet.put(entry.getKey(), entry.getValue().execute(stateEvent));
            }
            updateSetParameterMaps.add(variableMapForUpdateSet);
        }
        update(updateConditionParameterMaps, recordStoreCompiledExpression.compiledExpression,
                recordTableCompiledUpdateSet, updateSetParameterMaps);
    }

    /**
     * Update all matching records
     *  @param updateConditionParameterMaps map of matching StreamVariable Ids and their values corresponding to the
     *                                     compiled condition based on which the records will be updated
     * @param compiledExpression            the compiledExpression against which records should be matched for update
     * @param recordTableCompiledUpdateSet
     * @param updateValues                 the attributes and values that should be updated for the matching records
     */
    protected abstract void update(List<Map<String, Object>> updateConditionParameterMaps,
                                   CompiledExpression compiledExpression,
                                   RecordTableCompiledUpdateSet recordTableCompiledUpdateSet, List<Map<String, Object>> updateValues);

    @Override
    public void updateOrAdd(ComplexEventChunk<StateEvent> updateOrAddingEventChunk,
                            CompiledExpression compiledExpression, CompiledUpdateSet compiledUpdateSet,
                            AddingStreamEventExtractor addingStreamEventExtractor) {
        RecordStoreCompiledExpression recordStoreCompiledExpression = ((RecordStoreCompiledExpression) compiledExpression);
        RecordTableCompiledUpdateSet recordTableCompiledUpdateSet = (RecordTableCompiledUpdateSet) compiledUpdateSet;
        List<Map<String, Object>> updateConditionParameterMaps = new ArrayList<>();
        List<Map<String, Object>> updateSetParameterMaps = new ArrayList<>();
        List<Object[]> addingRecords = new ArrayList<>();
        updateOrAddingEventChunk.reset();
        while (updateOrAddingEventChunk.hasNext()) {
            StateEvent stateEvent = updateOrAddingEventChunk.next();

            Map<String, Object> variableMap = new HashMap<>();
            for (Map.Entry<String, ExpressionExecutor> entry :
                    recordStoreCompiledExpression.variableExpressionExecutorMap.entrySet()) {
                variableMap.put(entry.getKey(), entry.getValue().execute(stateEvent));
            }
            updateConditionParameterMaps.add(variableMap);

            Map<String, Object> variableMapForUpdateSet = new HashMap<>();
            for (Map.Entry<String, ExpressionExecutor> entry :
                    recordTableCompiledUpdateSet.getExpressionExecutorMap().entrySet()) {
                variableMapForUpdateSet.put(entry.getKey(), entry.getValue().execute(stateEvent));
            }
            updateSetParameterMaps.add(variableMapForUpdateSet);
            addingRecords.add(stateEvent.getStreamEvent(0).getOutputData());
        }
        updateOrAdd(updateConditionParameterMaps, recordStoreCompiledExpression.compiledExpression,
                recordTableCompiledUpdateSet, updateSetParameterMaps, addingRecords);

    }

    /**
     * Try updating the records if they exist else add the records
     *  @param updateConditionParameterMaps map of matching StreamVariable Ids and their values corresponding to the
     *                                     compiled condition based on which the records will be updated
     * @param compiledExpression            the compiledExpression against which records should be matched for update
     * @param recordTableCompiledUpdateSet
     * @param updateValues                 the attributes and values that should be updated if the condition matches
     * @param addingRecords                the values for adding new records if the update condition did not match
     */
    protected abstract void updateOrAdd(List<Map<String, Object>> updateConditionParameterMaps,
                                        CompiledExpression compiledExpression,
                                        RecordTableCompiledUpdateSet recordTableCompiledUpdateSet,
                                        List<Map<String, Object>> updateValues,
                                        List<Object[]> addingRecords);

    @Override
    public CompiledExpression compileExpression(Expression expression,
                                                MatchingMetaInfoHolder matchingMetaInfoHolder,
                                                SiddhiAppContext siddhiAppContext,
                                                List<VariableExpressionExecutor> variableExpressionExecutors,
                                                Map<String, Table> tableMap, String queryName) {
        ExpressionBuilder expressionBuilder = new ExpressionBuilder(expression, matchingMetaInfoHolder,
                                                                 siddhiAppContext, variableExpressionExecutors,
                                                                 tableMap, queryName);
        CompiledExpression compiledExpression = compileExpression(expressionBuilder);
        Map<String, ExpressionExecutor> expressionExecutorMap = expressionBuilder.getVariableExpressionExecutorMap();
        return new RecordStoreCompiledExpression(expressionExecutorMap, compiledExpression);
    }

    public CompiledUpdateSet compileUpdateSet(UpdateSet updateSet,
                                              MatchingMetaInfoHolder matchingMetaInfoHolder,
                                              SiddhiAppContext siddhiAppContext,
                                              List<VariableExpressionExecutor> variableExpressionExecutors,
                                              Map<String, Table> tableMap, String queryName) {
        RecordTableCompiledUpdateSet recordTableCompiledUpdateSet = new RecordTableCompiledUpdateSet();
        Map<String, ExpressionExecutor> parentExecutorMap = new HashMap<>();
        if (updateSet == null) {
            updateSet = new UpdateSet();
            for (Attribute attribute: matchingMetaInfoHolder.getMatchingStreamDefinition().getAttributeList()) {
                updateSet.set(new Variable(attribute.getName()), new Variable(attribute.getName()));
            }
        }
        for (UpdateSet.SetAttribute setAttribute: updateSet.getSetAttributeList()) {
            ExpressionBuilder expressionBuilder = new ExpressionBuilder(setAttribute.getAssignmentExpression(),
                    matchingMetaInfoHolder, siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
            CompiledExpression compiledExpression = compileSetAttribute(expressionBuilder);
            recordTableCompiledUpdateSet.put(setAttribute.getTableVariable().getAttributeName(), compiledExpression);
            Map<String, ExpressionExecutor> expressionExecutorMap = expressionBuilder.getVariableExpressionExecutorMap();
            parentExecutorMap.putAll(expressionExecutorMap);
        }
        recordTableCompiledUpdateSet.setExpressionExecutorMap(parentExecutorMap);
        return recordTableCompiledUpdateSet;
    }

    /**
     * Compile the matching expression
     *
     * @param expressionBuilder helps visiting the conditions in order to compile the condition
     * @return compiled expression that can be used for matching events in find, contains, delete, update and
     * updateOrAdd
     */
    protected abstract CompiledExpression compileExpression(ExpressionBuilder expressionBuilder);

    /**
     * Compiles the expression in a set clause
     * @param expressionBuilder helps visiting the conditions in order to compile the condition
     * @return compiled expression that can be used for matching events in find, contains, delete, update and
     * updateOrAdd
     */
    protected abstract CompiledExpression compileSetAttribute(ExpressionBuilder expressionBuilder);

    private class RecordStoreCompiledExpression implements CompiledExpression {
        private Map<String, ExpressionExecutor> variableExpressionExecutorMap;
        private CompiledExpression compiledExpression;

        RecordStoreCompiledExpression(Map<String, ExpressionExecutor> variableExpressionExecutorMap,
                                      CompiledExpression compiledExpression) {
            this.variableExpressionExecutorMap = variableExpressionExecutorMap;
            this.compiledExpression = compiledExpression;
        }

        @Override
        public CompiledExpression cloneCompiledExpression(String key) {
            Map<String, ExpressionExecutor> newVariableExpressionExecutorMap = new HashMap<>();
            for (Map.Entry<String, ExpressionExecutor> entry : variableExpressionExecutorMap.entrySet()) {
                newVariableExpressionExecutorMap.put(entry.getKey(), entry.getValue().cloneExecutor(key));
            }
            return new RecordStoreCompiledExpression(newVariableExpressionExecutorMap, compiledExpression);
        }
    }
}
