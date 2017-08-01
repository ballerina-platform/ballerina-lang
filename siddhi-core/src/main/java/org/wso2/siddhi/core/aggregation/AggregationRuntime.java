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

package org.wso2.siddhi.core.aggregation;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.input.stream.single.EntryValveExecutor;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecutor;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.IncrementalAggregateCompileCondition;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.core.util.parser.MatcherParser;
import org.wso2.siddhi.core.util.parser.OperatorParser;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.aggregation.Within;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.Compare;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.siddhi.core.util.SiddhiConstants.UNKNOWN_STATE;

/**
 * Aggregation runtime managing aggregation operations for aggregation definition
 */
public class AggregationRuntime {
    private final AggregationDefinition aggregationDefinition;
    private final Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap;
    private final Map<TimePeriod.Duration, Table> aggregationTables;
    private final SiddhiAppContext siddhiAppContext;
    private final MetaStreamEvent metaStreamEvent;
    private List<TimePeriod.Duration> incrementalDurations;
    private SingleStreamRuntime singleStreamRuntime;
    private EntryValveExecutor entryValveExecutor;
    private ExpressionExecutor perExpressionExecutor;
    private Map<TimePeriod.Duration, Object> inMemoryStoreMap = new HashMap<>();
    private List<ExpressionExecutor> baseExecutors;
    private List<ExpressionExecutor> outputExpressionExecutors;

    public AggregationRuntime(AggregationDefinition aggregationDefinition,
                              Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap,
                              Map<TimePeriod.Duration, Table> aggregationTables, SingleStreamRuntime singleStreamRuntime,
                              EntryValveExecutor entryValveExecutor, List<TimePeriod.Duration> incrementalDurations,
                              SiddhiAppContext siddhiAppContext, List<ExpressionExecutor> baseExecutors,
                              MetaStreamEvent metaStreamEvent, List<ExpressionExecutor> outputExpressionExecutors) {
        this.aggregationDefinition = aggregationDefinition;
        this.incrementalExecutorMap = incrementalExecutorMap;
        this.aggregationTables = aggregationTables;
        this.incrementalDurations = incrementalDurations;
        this.siddhiAppContext = siddhiAppContext;
        this.singleStreamRuntime = singleStreamRuntime;
        this.entryValveExecutor = entryValveExecutor;
        this.baseExecutors = baseExecutors;
        this.metaStreamEvent = metaStreamEvent;
        this.outputExpressionExecutors = outputExpressionExecutors;
    }

    public Map<TimePeriod.Duration, IncrementalExecutor> getIncrementalExecutorMap() {
        return incrementalExecutorMap;
    }

    public Map<TimePeriod.Duration, Table> getAggregationTables() {
        return aggregationTables;
    }

    public AggregationDefinition getAggregationDefinition() {
        return aggregationDefinition;
    }

    public SiddhiAppContext getSiddhiAppContext() {
        return siddhiAppContext;
    }

    public SingleStreamRuntime getSingleStreamRuntime() {
        return singleStreamRuntime;
    }

    public EntryValveExecutor getEntryValveExecutor() {
        return entryValveExecutor;
    }

    public List<TimePeriod.Duration> getIncrementalDurations() {
        return incrementalDurations;
    }

    public StreamEvent find(StateEvent matchingEvent, CompiledCondition compiledCondition) {
        String perValueAsString = perExpressionExecutor.execute(matchingEvent).toString();
        TimePeriod.Duration perValue = TimePeriod.Duration.valueOf(perValueAsString.toUpperCase());
        if (!incrementalExecutorMap.keySet().contains(perValue)) {
            throw new SiddhiAppRuntimeException("The aggregate values for " + perValue.toString()
                    + " granularity cannot be provided since aggregation definition " + aggregationDefinition.getId()
                    + " does not contain " + perValue.toString() + " duration");
        }
        return ((IncrementalAggregateCompileCondition) compiledCondition).find(matchingEvent, perValue,
                incrementalExecutorMap.get(perValue).isRoot());
    }

    public IncrementalAggregateCompileCondition compileCondition(Expression expression, Within within, Expression per,
            MatchingMetaInfoHolder matchingMetaInfoHolder, List<VariableExpressionExecutor> variableExpressionExecutors,
            Map<String, Table> tableMap, String queryName, SiddhiAppContext siddhiAppContext) {
        Map<TimePeriod.Duration, CompiledCondition> tableCompiledConditions = new HashMap<>();
        CompiledCondition inMemoryStoreCompileCondition;
        CompiledCondition finalCompiledCondition;
        perExpressionExecutor = ExpressionParser.parseExpression(per, matchingMetaInfoHolder.getMetaStateEvent(),
                matchingMetaInfoHolder.getCurrentState(), tableMap, variableExpressionExecutors, siddhiAppContext,
                false, 0, queryName);
        if (within.getTimeRange().size() == 2) {
            // TODO: 7/21/17 We assume that within.getTimeRange() values are in "yyyy-MM-dd HH:mm:ss" format
            Expression greaterThanEqualExpression = Expression.compare(Expression.variable("_TIMESTAMP"),
                    Compare.Operator.GREATER_THAN_EQUAL, Expression.function("time", "timestampInMilliseconds",
                            within.getTimeRange().get(0), Expression.value("yyyy-MM-dd HH:mm:ss")));
            Expression lessThanExpression = Expression.compare(Expression.variable("_TIMESTAMP"),
                    Compare.Operator.LESS_THAN, Expression.function("time", "timestampInMilliseconds",
                            within.getTimeRange().get(1), Expression.value("yyyy-MM-dd HH:mm:ss")));
            Expression completeExpression = Expression.and(greaterThanEqualExpression, lessThanExpression);
            for (Map.Entry<TimePeriod.Duration, Table> entry : aggregationTables.entrySet()) {
                CompiledCondition tableCompileCondition = entry.getValue().compileCondition(completeExpression,
                        newMatchingMetaInfoHolderForTables(matchingMetaInfoHolder,
                                entry.getValue().getTableDefinition()),
                        siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
                tableCompiledConditions.put(entry.getKey(), tableCompileCondition);

                inMemoryStoreMap.put(entry.getKey(), incrementalExecutorMap.get(entry.getKey()).getInMemoryStore());
            }
            inMemoryStoreCompileCondition = OperatorParser
                    .constructOperator(new ComplexEventChunk<>(true), completeExpression,
                            newMatchingMetaInfoHolderForComplexEventChunk(matchingMetaInfoHolder,
                                    ((Table)aggregationTables.values().toArray()[0]).getTableDefinition()
                                            .getAttributeList()),
                            siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
            finalCompiledCondition = OperatorParser.constructOperator(new ComplexEventChunk<>(true), expression,
                    matchingMetaInfoHolder, siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
            MetaStreamEvent finalOutputMeta = null;
            for (MetaStreamEvent metaStreamEvent : matchingMetaInfoHolder.getMetaStateEvent().getMetaStreamEvents()) {
                if(metaStreamEvent.getLastInputDefinition().getId().
                        equals(matchingMetaInfoHolder.getStoreDefinition().getId())) {
                    if (metaStreamEvent.getOutputData() == null || metaStreamEvent.getOutputData().isEmpty()) {
                        metaStreamEvent.getLastInputDefinition().getAttributeList().forEach(metaStreamEvent::addOutputData);
                    }
                    finalOutputMeta = metaStreamEvent;
                }
            }
            return new IncrementalAggregateCompileCondition(tableCompiledConditions, inMemoryStoreCompileCondition,
                    finalCompiledCondition, baseExecutors, aggregationTables, inMemoryStoreMap, metaStreamEvent,
                    incrementalDurations, outputExpressionExecutors, finalOutputMeta);

        } else { // TODO: 7/24/17 implement for one within value
            throw new SiddhiAppRuntimeException("Only one or two values allowed for within condition");
        }
    }

    private MatchingMetaInfoHolder newMatchingMetaInfoHolderForTables(MatchingMetaInfoHolder matchingMetaInfoHolder,
            AbstractDefinition tableDefinition) {
        MetaStreamEvent rightMetaStreamEventForTable = new MetaStreamEvent();
        rightMetaStreamEventForTable.setEventType(MetaStreamEvent.EventType.TABLE);
        rightMetaStreamEventForTable.addInputDefinition(tableDefinition);
        MetaStateEvent metaStateEvent = new MetaStateEvent(2);
        metaStateEvent.addEvent(matchingMetaInfoHolder.getMetaStateEvent().getMetaStreamEvent(0));
        metaStateEvent.addEvent(rightMetaStreamEventForTable);

        return MatcherParser.constructMatchingMetaStateHolder(metaStateEvent, 0, tableDefinition, UNKNOWN_STATE);
    }

    private MatchingMetaInfoHolder newMatchingMetaInfoHolderForComplexEventChunk(
            MatchingMetaInfoHolder matchingMetaInfoHolder, List<Attribute> attributeList) {
        MetaStreamEvent rightMetaStreamEventForComplexEventChunk = new MetaStreamEvent();
        rightMetaStreamEventForComplexEventChunk.setEventType(MetaStreamEvent.EventType.WINDOW);
        // TODO: 7/26/17 is it ok to set type WINDOW? Otherwise, _TIMESTAMP variable not parsed
        StreamDefinition streamDefinition = StreamDefinition.id("_StreamDefinitionForRetrieval");
        for (Attribute attribute : attributeList) {
            streamDefinition.attribute(attribute.getName(), attribute.getType());
        }
        rightMetaStreamEventForComplexEventChunk.addInputDefinition(streamDefinition);
        rightMetaStreamEventForComplexEventChunk.setOutputDefinition(streamDefinition);
        MetaStateEvent metaStateEvent = new MetaStateEvent(2);
        metaStateEvent.addEvent(matchingMetaInfoHolder.getMetaStateEvent().getMetaStreamEvent(0));
        metaStateEvent.addEvent(rightMetaStreamEventForComplexEventChunk);

        return MatcherParser.constructMatchingMetaStateHolder(metaStateEvent, 0, streamDefinition, UNKNOWN_STATE);
    }

}
