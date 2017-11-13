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
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
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
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.Compare;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.siddhi.core.util.SiddhiConstants.UNKNOWN_STATE;

/**
 * Aggregation runtime managing aggregation operations for aggregation definition.
 */
public class AggregationRuntime {
    private final AggregationDefinition aggregationDefinition;
    private final Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap;
    private final Map<TimePeriod.Duration, Table> aggregationTables;
    private final SiddhiAppContext siddhiAppContext;
    private final MetaStreamEvent tableMetaStreamEvent;
    private final MetaStreamEvent aggregateMetaSteamEvent;
    private List<TimePeriod.Duration> incrementalDurations;
    private SingleStreamRuntime singleStreamRuntime;
    private EntryValveExecutor entryValveExecutor;
    private ExpressionExecutor perExpressionExecutor;
    private ExpressionExecutor startTimeEndTimeExpressionExecutor;
    private List<ExpressionExecutor> baseExecutors;
    private ExpressionExecutor timestampExecutor;
    private List<ExpressionExecutor> outputExpressionExecutors;
    private static int[] metaStreamArraySizes = {0, 0, 2};

    public AggregationRuntime(AggregationDefinition aggregationDefinition,
                              Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap,
                              Map<TimePeriod.Duration, Table> aggregationTables,
                              SingleStreamRuntime singleStreamRuntime,
                              EntryValveExecutor entryValveExecutor, List<TimePeriod.Duration> incrementalDurations,
                              SiddhiAppContext siddhiAppContext, List<ExpressionExecutor> baseExecutors,
                              ExpressionExecutor timestampExecutor, MetaStreamEvent tableMetaStreamEvent,
                              List<ExpressionExecutor> outputExpressionExecutors) {
        this.aggregationDefinition = aggregationDefinition;
        this.incrementalExecutorMap = incrementalExecutorMap;
        this.aggregationTables = aggregationTables;
        this.incrementalDurations = incrementalDurations;
        this.siddhiAppContext = siddhiAppContext;
        this.singleStreamRuntime = singleStreamRuntime;
        this.entryValveExecutor = entryValveExecutor;
        this.baseExecutors = baseExecutors;
        this.timestampExecutor = timestampExecutor;
        this.tableMetaStreamEvent = tableMetaStreamEvent;
        this.outputExpressionExecutors = outputExpressionExecutors;


        aggregateMetaSteamEvent = new MetaStreamEvent();
        aggregationDefinition.getAttributeList().forEach(aggregateMetaSteamEvent::addOutputData);
    }

    private static MatchingMetaInfoHolder aggregationTableMetaInfoHolder(MatchingMetaInfoHolder matchingMetaInfoHolder,
                                                                         AbstractDefinition tableDefinition) {
        // TODO: 11/10/17 change method name (line alterMetaInfoHolder?)
        MetaStreamEvent metaStreamEventForTable = new MetaStreamEvent();
        metaStreamEventForTable.setEventType(MetaStreamEvent.EventType.TABLE);
        metaStreamEventForTable.addInputDefinition(tableDefinition);
        MetaStateEvent metaStateEvent;
        if (matchingMetaInfoHolder.getMetaStateEvent().getMetaStreamEvents().length == 1) {
            // Only store meta is passed via StoreQuery.
            // Hence the Meta state event would contain only one meta stream event.
            metaStateEvent = new MetaStateEvent(1);
            metaStateEvent.addEvent(metaStreamEventForTable);
            return new MatchingMetaInfoHolder(metaStateEvent, 0, 0, tableDefinition, tableDefinition, 0);
        } else {
            // Both stream and store events appear for a join
            metaStateEvent = new MetaStateEvent(2);
            MetaStreamEvent matchingStreamEvent = matchingMetaInfoHolder.getMetaStateEvent().getMetaStreamEvent(
                    matchingMetaInfoHolder.getMatchingStreamEventIndex());
            MetaStreamEvent newMatchingStreamEvent = new MetaStreamEvent();
            newMatchingStreamEvent.setEventType(MetaStreamEvent.EventType.TABLE);
            // TODO: 11/13/17 hack to create variable exp exec
            StreamDefinition newMatchingStreamDefinition = new StreamDefinition();
            for (Attribute attribute : matchingStreamEvent.getBeforeWindowData()) {
                newMatchingStreamEvent.addData(attribute);
                newMatchingStreamDefinition.attribute(attribute.getName(), attribute.getType());
                metaStreamArraySizes[0] = ++metaStreamArraySizes[0];
            }
            newMatchingStreamEvent.initializeAfterWindowData();
            for (Attribute attribute : matchingStreamEvent.getOnAfterWindowData()) {
                newMatchingStreamEvent.addData(attribute);
                newMatchingStreamDefinition.attribute(attribute.getName(), attribute.getType());
                metaStreamArraySizes[1] = ++metaStreamArraySizes[1];
            }
            for (Attribute attribute : matchingStreamEvent.getOutputData()) {
                newMatchingStreamEvent.addOutputData(attribute);
                newMatchingStreamDefinition.attribute(attribute.getName(), attribute.getType());
                metaStreamArraySizes[2] = ++metaStreamArraySizes[2];
            }

            newMatchingStreamEvent.addOutputData(new Attribute("_START", Attribute.Type.LONG));
            newMatchingStreamEvent.addOutputData(new Attribute("_END", Attribute.Type.LONG));
            newMatchingStreamDefinition.attribute("_START", Attribute.Type.LONG);
            newMatchingStreamDefinition.attribute("_END", Attribute.Type.LONG);
            newMatchingStreamEvent.addInputDefinition(newMatchingStreamDefinition);
            metaStateEvent.addEvent(newMatchingStreamEvent);
            metaStateEvent.addEvent(metaStreamEventForTable);
            return MatcherParser.constructMatchingMetaStateHolder(metaStateEvent, 0, tableDefinition, UNKNOWN_STATE);
        }
    }

    /*private static void addStartEndAttributesToMatchingStream(MatchingMetaInfoHolder matchingMetaInfoHolder) {
        if (matchingMetaInfoHolder.getMetaStateEvent().getMetaStreamEvents().length == 2) {
            MetaStreamEvent matchingStreamEvent = matchingMetaInfoHolder.getMetaStateEvent()
                    .getMetaStreamEvent(matchingMetaInfoHolder.getMatchingStreamEventIndex());
            matchingStreamEvent.addOutputData(new Attribute("_START", Attribute.Type.LONG));
            matchingStreamEvent.addOutputData(new Attribute("_END", Attribute.Type.LONG));
            StreamDefinition originalDefinition = (StreamDefinition) matchingStreamEvent.getLastInputDefinition();
            originalDefinition.attribute("_START", Attribute.Type.LONG);
            originalDefinition.attribute("_END", Attribute.Type.LONG);
        }
    }*/

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

        // Retrieve per value
        String perValueAsString = perExpressionExecutor.execute(matchingEvent).toString();
        TimePeriod.Duration perValue = TimePeriod.Duration.valueOf(perValueAsString.toUpperCase());
        if (!incrementalExecutorMap.keySet().contains(perValue)) {
            throw new SiddhiAppRuntimeException("The aggregate values for " + perValue.toString()
                    + " granularity cannot be provided since aggregation definition " + aggregationDefinition.getId()
                    + " does not contain " + perValue.toString() + " duration");
        }

        Table tableForPerDuration = aggregationTables.get(perValue);

        Long[] startTimeEndTime = (Long[]) startTimeEndTimeExpressionExecutor.execute(matchingEvent);
        // TODO: 11/10/17 create a new matchingEvent or add to this. try both
        /*matchingEvent.setOutputData(startTimeEndTime[0], matchingEvent.getOutputData().length);
        matchingEvent.setOutputData(startTimeEndTime[1], matchingEvent.getOutputData().length + 1);*/

        return ((IncrementalAggregateCompileCondition) compiledCondition).find(matchingEvent, perValue,
                incrementalExecutorMap, incrementalDurations, tableForPerDuration, baseExecutors, timestampExecutor,
                outputExpressionExecutors, startTimeEndTime);
    }

    public CompiledCondition compileExpression(Expression expression, Within within, Expression per,
                                               MatchingMetaInfoHolder matchingMetaInfoHolder,
                                               List<VariableExpressionExecutor> variableExpressionExecutors,
                                               Map<String, Table> tableMap, String queryName,
                                               SiddhiAppContext siddhiAppContext) {

        Map<TimePeriod.Duration, CompiledCondition> withinTableCompiledConditions = new HashMap<>();
        CompiledCondition withinInMemoryCompileCondition;
        CompiledCondition onCompiledCondition;

        // Insert _START, _END attributes to matching stream
//        addStartEndAttributesToMatchingStream(matchingMetaInfoHolder);


        // Create per expression executor
        perExpressionExecutor = ExpressionParser.parseExpression(per, matchingMetaInfoHolder.getMetaStateEvent(),
                matchingMetaInfoHolder.getCurrentState(), tableMap, variableExpressionExecutors, siddhiAppContext,
                false, 0, queryName);
        if (perExpressionExecutor.getReturnType() != Attribute.Type.STRING) {
            throw new SiddhiAppCreationException("Query " + queryName + "'s per value expected a string but found "
                    + perExpressionExecutor.getReturnType(), per.getQueryContextStartIndex(),
                    per.getQueryContextEndIndex());
        }

        // TODO: 8/11/17 optimize on and to retrieve group by
        // On compile condition.
        // After finding all the aggregates belonging to within duration, the final on condition (given as
        // "on stream1.name == aggregator.nickName ..." in the join query) must be executed on that data.
        // This condition is used for that purpose.
        onCompiledCondition = OperatorParser.constructOperator(new ComplexEventChunk<>(true), expression,
                matchingMetaInfoHolder, siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
        // TODO: 11/13/17 this must be done before calling aggregationTableMetaInfoHolder since
        // todo otherwise the output attributes won't be known

        // Get table definition. Table definitions for all the tables used to persist aggregates are similar.
        // Therefore it's enough to get the definition from one table.
        AbstractDefinition tableDefinition = ((Table) aggregationTables.values().toArray()[0]).getTableDefinition();

        // Alter the meta to include _START, _END attributes to matching stream and add table meta
        MatchingMetaInfoHolder newMatchingMetaInfoHolder = aggregationTableMetaInfoHolder(matchingMetaInfoHolder,
                tableDefinition);

        // Create within expression
        Expression withinExpression;
        Expression start = Expression.variable("_START");
        Expression end = Expression.variable("_END");
        Expression compareWithStartTime = Compare.compare(start, Compare.Operator.LESS_THAN_EQUAL,
                Expression.variable("_TIMESTAMP"));
        Expression compareWithEndTime = Compare.compare(Expression.variable("_TIMESTAMP"), Compare.Operator.LESS_THAN,
                end);
        withinExpression = Expression.and(compareWithStartTime, compareWithEndTime);

        // Create variable expression executors for _START and _END
        /*ExpressionParser.parseExpression(start, newMatchingMetaInfoHolder.getMetaStateEvent(),
                matchingMetaInfoHolder.getCurrentState(), tableMap, variableExpressionExecutors, siddhiAppContext,
                false, 0, queryName);
        ExpressionParser.parseExpression(end, newMatchingMetaInfoHolder.getMetaStateEvent(),
                matchingMetaInfoHolder.getCurrentState(), tableMap, variableExpressionExecutors, siddhiAppContext,
                false, 0, queryName);*/


        // Create start and end time expression
        Expression startEndTimeExpression;
        if (within.getTimeRange().size() == 1) {
            startEndTimeExpression = new AttributeFunction("incrementalAggregator", "startTimeEndTime",
                    within.getTimeRange().get(0));
        } else { // within.getTimeRange().size() == 2
            startEndTimeExpression = new AttributeFunction("incrementalAggregator", "startTimeEndTime",
                    within.getTimeRange().get(0), within.getTimeRange().get(1));
        }

        startTimeEndTimeExpressionExecutor = ExpressionParser.parseExpression(startEndTimeExpression,
                matchingMetaInfoHolder.getMetaStateEvent(), matchingMetaInfoHolder.getCurrentState(), tableMap,
                variableExpressionExecutors, siddhiAppContext, false, 0, queryName);

        // Create compile condition per each table used to persist aggregates.
        // These compile conditions are used to check whether the aggregates in tables are within the given duration.
        for (Map.Entry<TimePeriod.Duration, Table> entry : aggregationTables.entrySet()) {
            CompiledCondition withinTableCompileCondition = entry.getValue().compileCondition(withinExpression,
                    newMatchingMetaInfoHolder, siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
            withinTableCompiledConditions.put(entry.getKey(), withinTableCompileCondition);
        }

        // Create compile condition for in-memory data.
        // This compile condition is used to check whether the running aggregates (in-memory data)
        // are within given duration
        withinInMemoryCompileCondition = OperatorParser.constructOperator(new ComplexEventChunk<>(true),
                withinExpression, newMatchingMetaInfoHolder, siddhiAppContext, variableExpressionExecutors, tableMap,
                queryName);

        

        return new IncrementalAggregateCompileCondition(withinTableCompiledConditions, withinInMemoryCompileCondition,
                onCompiledCondition, tableMetaStreamEvent, aggregateMetaSteamEvent, metaStreamArraySizes);
    }
}
