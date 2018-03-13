/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.siddhi.core.table.record;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.state.StateEvent;
import org.ballerinalang.siddhi.core.event.stream.MetaStreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.exception.ConnectionUnavailableException;
import org.ballerinalang.siddhi.core.executor.ConstantExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.VariableExpressionExecutor;
import org.ballerinalang.siddhi.core.query.processor.stream.window.QueryableProcessor;
import org.ballerinalang.siddhi.core.table.Table;
import org.ballerinalang.siddhi.core.util.SiddhiConstants;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledCondition;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledSelection;
import org.ballerinalang.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.ballerinalang.siddhi.core.util.parser.ExpressionParser;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.execution.query.selection.OrderByAttribute;
import org.ballerinalang.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.ballerinalang.siddhi.query.api.execution.query.selection.Selector;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An abstract implementation of table. Abstract implementation will handle {@link ComplexEventChunk} so that
 * developer can directly work with event data.
 */
public abstract class AbstractQueryableRecordTable extends AbstractRecordTable implements QueryableProcessor {

    private static final Logger log = LoggerFactory.getLogger(AbstractQueryableRecordTable.class);

    @Override
    public StreamEvent query(StateEvent matchingEvent, CompiledCondition compiledCondition,
                             CompiledSelection compiledSelection) throws ConnectionUnavailableException {

        RecordStoreCompiledSelection recordStoreCompiledSelection = ((RecordStoreCompiledSelection) compiledSelection);
        RecordStoreCompiledCondition recordStoreCompiledCondition = ((RecordStoreCompiledCondition) compiledCondition);

        Map<String, Object> parameterMap = new HashMap<>();
        for (Map.Entry<String, ExpressionExecutor> entry :
                recordStoreCompiledCondition.variableExpressionExecutorMap.entrySet()) {
            parameterMap.put(entry.getKey(), entry.getValue().execute(matchingEvent));
        }
        for (Map.Entry<String, ExpressionExecutor> entry :
                recordStoreCompiledSelection.variableExpressionExecutorMap.entrySet()) {
            parameterMap.put(entry.getKey(), entry.getValue().execute(matchingEvent));
        }

        Iterator<Object[]> records;
        if (recordTableHandler != null) {
            records = recordTableHandler.query(matchingEvent.getTimestamp(), parameterMap,
                    recordStoreCompiledCondition.compiledCondition,
                    recordStoreCompiledSelection.compiledSelection);
        } else {
            records = query(parameterMap, recordStoreCompiledCondition.compiledCondition,
                    recordStoreCompiledSelection.compiledSelection);
        }
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
     * Query records matching the compiled condition and selection.
     *
     * @param parameterMap      map of matching StreamVariable Ids and their values
     *                          corresponding to the compiled condition and selection
     * @param compiledCondition the compiledCondition against which records should be matched
     * @param compiledSelection the compiledSelection that maps records based to requested format
     * @return RecordIterator of matching records
     * @throws ConnectionUnavailableException
     */
    protected abstract RecordIterator<Object[]> query(Map<String, Object> parameterMap,
                                                      CompiledCondition compiledCondition,
                                                      CompiledSelection compiledSelection)
            throws ConnectionUnavailableException;

    public CompiledSelection compileSelection(Selector selector,
                                              List<Attribute> expectedOutputAttributes,
                                              MatchingMetaInfoHolder matchingMetaInfoHolder,
                                              SiddhiAppContext siddhiAppContext,
                                              List<VariableExpressionExecutor> variableExpressionExecutors,
                                              Map<String, Table> tableMap, String queryName) {
        List<OutputAttribute> outputAttributes = selector.getSelectionList();
        if (outputAttributes.size() == 0) {
            MetaStreamEvent metaStreamEvent = matchingMetaInfoHolder.getMetaStateEvent().getMetaStreamEvent(
                    matchingMetaInfoHolder.getStoreEventIndex());
            List<Attribute> attributeList = metaStreamEvent.getLastInputDefinition().getAttributeList();
            for (Attribute attribute : attributeList) {
                outputAttributes.add(new OutputAttribute(new Variable(attribute.getName())));
            }
        }
        List<SelectAttributeBuilder> selectAttributeBuilders = new ArrayList<>(outputAttributes.size());
        for (OutputAttribute outputAttribute : outputAttributes) {
            ExpressionBuilder expressionBuilder = new ExpressionBuilder(outputAttribute.getExpression(),
                    matchingMetaInfoHolder, siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
            selectAttributeBuilders.add(new SelectAttributeBuilder(expressionBuilder, outputAttribute.getRename()));
        }

        List<ExpressionBuilder> groupByExpressionBuilders = null;
        if (selector.getGroupByList().size() != 0) {
            groupByExpressionBuilders = new ArrayList<>(outputAttributes.size());
            for (Variable variable : selector.getGroupByList()) {
                groupByExpressionBuilders.add(new ExpressionBuilder(variable, matchingMetaInfoHolder, siddhiAppContext,
                        variableExpressionExecutors, tableMap, queryName));
            }
        }

        ExpressionBuilder havingExpressionBuilder = null;
        if (selector.getHavingExpression() != null) {
            havingExpressionBuilder = new ExpressionBuilder(selector.getHavingExpression(), matchingMetaInfoHolder,
                    siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
        }

        List<OrderByAttributeBuilder> orderByAttributeBuilders = null;
        if (selector.getOrderByList().size() != 0) {
            orderByAttributeBuilders = new ArrayList<>(selector.getOrderByList().size());
            for (OrderByAttribute orderByAttribute : selector.getOrderByList()) {
                ExpressionBuilder expressionBuilder = new ExpressionBuilder(orderByAttribute.getVariable(),
                        matchingMetaInfoHolder, siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
                orderByAttributeBuilders.add(new OrderByAttributeBuilder(expressionBuilder,
                        orderByAttribute.getOrder()));
            }
        }

        Long limit = null;
        if (selector.getLimit() != null) {
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression((Expression) selector.getLimit(),
                    matchingMetaInfoHolder.getMetaStateEvent(), SiddhiConstants.HAVING_STATE, tableMap,
                    variableExpressionExecutors, siddhiAppContext, false, 0, queryName);
            limit = ((Number) (((ConstantExpressionExecutor) expressionExecutor).getValue())).longValue();
        }
        CompiledSelection compiledSelection = compileSelection(selectAttributeBuilders, groupByExpressionBuilders,
                havingExpressionBuilder, orderByAttributeBuilders, limit);

        Map<String, ExpressionExecutor> expressionExecutorMap = new HashMap<>();
        if (selectAttributeBuilders.size() != 0) {
            for (SelectAttributeBuilder selectAttributeBuilder : selectAttributeBuilders) {
                expressionExecutorMap.putAll(
                        selectAttributeBuilder.getExpressionBuilder().getVariableExpressionExecutorMap());
            }
        }
        if (groupByExpressionBuilders != null && groupByExpressionBuilders.size() != 0) {
            for (ExpressionBuilder groupByExpressionBuilder : groupByExpressionBuilders) {
                expressionExecutorMap.putAll(groupByExpressionBuilder.getVariableExpressionExecutorMap());
            }
        }
        if (havingExpressionBuilder != null) {
            expressionExecutorMap.putAll(havingExpressionBuilder.getVariableExpressionExecutorMap());
        }
        if (orderByAttributeBuilders != null && orderByAttributeBuilders.size() != 0) {
            for (OrderByAttributeBuilder orderByAttributeBuilder : orderByAttributeBuilders) {
                expressionExecutorMap.putAll(
                        orderByAttributeBuilder.getExpressionBuilder().getVariableExpressionExecutorMap());
            }
        }
        return new RecordStoreCompiledSelection(expressionExecutorMap, compiledSelection);
    }

    /**
     * Compile the query selection.
     *
     * @param selectAttributeBuilders  helps visiting the select attributes in order
     * @param groupByExpressionBuilder helps visiting the group by attributes in order
     * @param havingExpressionBuilder  helps visiting the having condition
     * @param orderByAttributeBuilders helps visiting the order by attributes in order
     * @param limit                    defines the limit level
     * @return compiled selection that can be used for retrieving events on a defined format
     */
    protected abstract CompiledSelection compileSelection(List<SelectAttributeBuilder> selectAttributeBuilders,
                                                          List<ExpressionBuilder> groupByExpressionBuilder,
                                                          ExpressionBuilder havingExpressionBuilder,
                                                          List<OrderByAttributeBuilder> orderByAttributeBuilders,
                                                          Long limit);


    private class RecordStoreCompiledSelection implements CompiledSelection {


        private final Map<String, ExpressionExecutor> variableExpressionExecutorMap;
        private final CompiledSelection compiledSelection;

        RecordStoreCompiledSelection(Map<String, ExpressionExecutor> variableExpressionExecutorMap,
                                     CompiledSelection compiledSelection) {

            this.variableExpressionExecutorMap = variableExpressionExecutorMap;
            this.compiledSelection = compiledSelection;
        }

        @Override
        public CompiledSelection cloneCompilation(String key) {
            Map<String, ExpressionExecutor> newVariableExpressionExecutorMap = new HashMap<>();
            for (Map.Entry<String, ExpressionExecutor> entry : variableExpressionExecutorMap.entrySet()) {
                newVariableExpressionExecutorMap.put(entry.getKey(), entry.getValue().cloneExecutor(key));
            }
            return new RecordStoreCompiledSelection(newVariableExpressionExecutorMap, compiledSelection);
        }
    }

    /**
     * Holder of Selection attribute with renaming field.
     */
    public class SelectAttributeBuilder {
        private final ExpressionBuilder expressionBuilder;
        private final String rename;

        public SelectAttributeBuilder(ExpressionBuilder expressionBuilder, String rename) {
            this.expressionBuilder = expressionBuilder;
            this.rename = rename;
        }

        public ExpressionBuilder getExpressionBuilder() {
            return expressionBuilder;
        }

        public String getRename() {
            return rename;
        }
    }

    /**
     * Holder of order by attribute with order orientation.
     */
    public class OrderByAttributeBuilder {
        private final ExpressionBuilder expressionBuilder;
        private final OrderByAttribute.Order order;

        public OrderByAttributeBuilder(ExpressionBuilder expressionBuilder, OrderByAttribute.Order order) {
            this.expressionBuilder = expressionBuilder;
            this.order = order;
        }

        public ExpressionBuilder getExpressionBuilder() {
            return expressionBuilder;
        }

        public OrderByAttribute.Order getOrder() {
            return order;
        }
    }


}
