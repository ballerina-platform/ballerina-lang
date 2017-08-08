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
    private final MetaStreamEvent internalMetaStreamEvent;
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
            MetaStreamEvent internalMetaStreamEvent, List<ExpressionExecutor> outputExpressionExecutors) {
        this.aggregationDefinition = aggregationDefinition;
        this.incrementalExecutorMap = incrementalExecutorMap;
        this.aggregationTables = aggregationTables;
        this.incrementalDurations = incrementalDurations;
        this.siddhiAppContext = siddhiAppContext;
        this.singleStreamRuntime = singleStreamRuntime;
        this.entryValveExecutor = entryValveExecutor;
        this.baseExecutors = baseExecutors;
        this.internalMetaStreamEvent = internalMetaStreamEvent;
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
        Expression completeWithinExpression;
        ExpressionExecutor acceptedTimeFormatExecutor = null;
        perExpressionExecutor = ExpressionParser.parseExpression(per, matchingMetaInfoHolder.getMetaStateEvent(),
                matchingMetaInfoHolder.getCurrentState(), tableMap, variableExpressionExecutors, siddhiAppContext,
                false, 0, queryName);
        if (perExpressionExecutor.getReturnType() != Attribute.Type.STRING) {
            throw new SiddhiAppCreationException("Query " + queryName + "'s per value expected a string but found "
                    + perExpressionExecutor.getReturnType());
        }
        if (within.getTimeRange().size() == 2) {
            Expression greaterThanEqualExpression;
            Expression lessThanExpression;
            ExpressionExecutor startTime = ExpressionParser.parseExpression(within.getTimeRange().get(0),
                    matchingMetaInfoHolder.getMetaStateEvent(), matchingMetaInfoHolder.getCurrentState(), tableMap,
                    variableExpressionExecutors, siddhiAppContext, false, 0, queryName);
            ExpressionExecutor endTime = ExpressionParser.parseExpression(within.getTimeRange().get(1),
                    matchingMetaInfoHolder.getMetaStateEvent(), matchingMetaInfoHolder.getCurrentState(), tableMap,
                    variableExpressionExecutors, siddhiAppContext, false, 0, queryName);
            if (startTime.getReturnType() == Attribute.Type.STRING) {
                greaterThanEqualExpression = Expression.compare(Expression.variable("_TIMESTAMP"),
                        Compare.Operator.GREATER_THAN_EQUAL, Expression.function("time", "timestampInMilliseconds",
                                within.getTimeRange().get(0), Expression.value("yyyy-MM-dd HH:mm:ss ZZ")));
            } else if (startTime.getReturnType() == Attribute.Type.LONG) {
                greaterThanEqualExpression = Expression.compare(Expression.variable("_TIMESTAMP"),
                        Compare.Operator.GREATER_THAN_EQUAL, within.getTimeRange().get(0));
            } else {
                throw new SiddhiAppCreationException("The first value of within clause must return "
                        + "string or long, but found " + startTime.getReturnType());
            }
            if (endTime.getReturnType() == Attribute.Type.STRING) {
                lessThanExpression = Expression.compare(Expression.variable("_TIMESTAMP"), Compare.Operator.LESS_THAN,
                        Expression.function("time", "timestampInMilliseconds", within.getTimeRange().get(1),
                                Expression.value("yyyy-MM-dd HH:mm:ss ZZ")));
            } else if (endTime.getReturnType() == Attribute.Type.LONG) {
                lessThanExpression = Expression.compare(Expression.variable("_TIMESTAMP"), Compare.Operator.LESS_THAN,
                        within.getTimeRange().get(1));
            } else {
                throw new SiddhiAppCreationException("The second value of within clause must return "
                        + "string or long, but found " + endTime.getReturnType());
            }
            completeWithinExpression = Expression.and(greaterThanEqualExpression, lessThanExpression);
        } else if (within.getTimeRange().size() == 1) {
            Expression[] regexArray = new Expression[TimePeriod.Duration.values().length];
            Expression[] completeWithinExpressionArray = new Expression[TimePeriod.Duration.values().length];
            ExpressionExecutor withinValue = ExpressionParser.parseExpression(within.getTimeRange().get(0),
                    matchingMetaInfoHolder.getMetaStateEvent(), matchingMetaInfoHolder.getCurrentState(), tableMap,
                    variableExpressionExecutors, siddhiAppContext, false, 0, queryName);
            if (withinValue.getReturnType() != Attribute.Type.STRING) {
                throw new SiddhiAppCreationException(
                        "The within clause must return " + "string, but found " + withinValue.getReturnType());
            }
            populateSingleWithinExpressionArray(regexArray, completeWithinExpressionArray, within);
            // Since there are 6 durations (sec, min, hour, day, month, year) each expression array has 6 elements.
            completeWithinExpression = Expression.function("ifThenElse", regexArray[0],
                    completeWithinExpressionArray[0],
                    Expression.function("ifThenElse", regexArray[1], completeWithinExpressionArray[1],
                            Expression.function("ifThenElse", regexArray[2], completeWithinExpressionArray[2],
                                    Expression.function("ifThenElse", regexArray[3], completeWithinExpressionArray[3],
                                            Expression.function("ifThenElse", regexArray[4],
                                                    completeWithinExpressionArray[4],
                                                    completeWithinExpressionArray[5])))));
            Expression acceptedTimeFormatExpression = Expression.function("ifThenElse", regexArray[0],
                    Expression.value(true),
                    Expression.function("ifThenElse", regexArray[1], Expression.value(true),
                            Expression.function("ifThenElse", regexArray[2], Expression.value(true),
                                    Expression.function("ifThenElse", regexArray[3], Expression.value(true),
                                            Expression.function("ifThenElse", regexArray[4], Expression.value(true),
                                                    Expression.function("ifThenElse", regexArray[5],
                                                            Expression.value(true), Expression.value(false)))))));
            acceptedTimeFormatExecutor = ExpressionParser.parseExpression(acceptedTimeFormatExpression,
                    matchingMetaInfoHolder.getMetaStateEvent(), matchingMetaInfoHolder.getCurrentState(), tableMap,
                    variableExpressionExecutors, siddhiAppContext, false, 0, queryName);
        } else {
            throw new SiddhiAppRuntimeException("Only one or two values allowed for within condition");
        }

        for (Map.Entry<TimePeriod.Duration, Table> entry : aggregationTables.entrySet()) {
            CompiledCondition tableCompileCondition = entry.getValue().compileCondition(completeWithinExpression,
                    newMatchingMetaInfoHolderForTables(matchingMetaInfoHolder, entry.getValue().getTableDefinition()),
                    siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
            tableCompiledConditions.put(entry.getKey(), tableCompileCondition);

            inMemoryStoreMap.put(entry.getKey(), incrementalExecutorMap.get(entry.getKey()).getInMemoryStore());
        }
        CompiledCondition inMemoryStoreCompileCondition = OperatorParser
                .constructOperator(new ComplexEventChunk<>(true), completeWithinExpression,
                        newMatchingMetaInfoHolderForComplexEventChunk(matchingMetaInfoHolder,
                                ((Table) aggregationTables.values().toArray()[0]).getTableDefinition()
                                        .getAttributeList()),
                        siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
        CompiledCondition finalCompiledCondition = OperatorParser.constructOperator(new ComplexEventChunk<>(true),
                expression, matchingMetaInfoHolder, siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
        MetaStreamEvent finalOutputMetaStreamEvent = null;
        for (MetaStreamEvent metaStreamEvent : matchingMetaInfoHolder.getMetaStateEvent().getMetaStreamEvents()) {
            if (metaStreamEvent.getLastInputDefinition().getId()
                    .equals(matchingMetaInfoHolder.getStoreDefinition().getId())) {
                if (metaStreamEvent.getOutputData() == null || metaStreamEvent.getOutputData().isEmpty()) {
                    metaStreamEvent.getLastInputDefinition().getAttributeList().forEach(metaStreamEvent::addOutputData);
                }
                finalOutputMetaStreamEvent = metaStreamEvent;
            }
        }
        return new IncrementalAggregateCompileCondition(tableCompiledConditions, inMemoryStoreCompileCondition,
                finalCompiledCondition, baseExecutors, aggregationTables, inMemoryStoreMap, internalMetaStreamEvent,
                incrementalDurations, outputExpressionExecutors, finalOutputMetaStreamEvent,
                acceptedTimeFormatExecutor);
    }

    private static MatchingMetaInfoHolder newMatchingMetaInfoHolderForTables(
            MatchingMetaInfoHolder matchingMetaInfoHolder, AbstractDefinition tableDefinition) {
        MetaStreamEvent rightMetaStreamEventForTable = new MetaStreamEvent();
        rightMetaStreamEventForTable.setEventType(MetaStreamEvent.EventType.TABLE);
        rightMetaStreamEventForTable.addInputDefinition(tableDefinition);
        MetaStateEvent metaStateEvent = new MetaStateEvent(2);
        metaStateEvent.addEvent(matchingMetaInfoHolder.getMetaStateEvent()
                .getMetaStreamEvent(matchingMetaInfoHolder.getMatchingStreamEventIndex()));
        metaStateEvent.addEvent(rightMetaStreamEventForTable);
        return MatcherParser.constructMatchingMetaStateHolder(metaStateEvent, 0, tableDefinition, UNKNOWN_STATE);
    }

    private static MatchingMetaInfoHolder newMatchingMetaInfoHolderForComplexEventChunk(
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
        metaStateEvent.addEvent(matchingMetaInfoHolder.getMetaStateEvent()
                .getMetaStreamEvent(matchingMetaInfoHolder.getMatchingStreamEventIndex()));
        metaStateEvent.addEvent(rightMetaStreamEventForComplexEventChunk);
        return MatcherParser.constructMatchingMetaStateHolder(metaStateEvent, 0, streamDefinition, UNKNOWN_STATE);
    }

    private static void populateSingleWithinExpressionArray(Expression[] regexArray,
            Expression[] completeWithinExpressionArray, Within within) {
        Expression[] startTimeAsStringArray = new Expression[TimePeriod.Duration.values().length];
        Expression[] startTimeAsUnixArray = new Expression[TimePeriod.Duration.values().length];
        Expression[] endTimeAsUnixArray = new Expression[TimePeriod.Duration.values().length];
        Expression timeZone;
        Expression thisYear;
        Expression getNextYearBeginning;
        for (TimePeriod.Duration duration : TimePeriod.Duration.values()) {
            switch (duration) {
            case SECONDS:
                regexArray[0] = Expression.function("str", "regexp", within.getTimeRange().get(0), Expression.value(
                        "[0-9]{4}-[0-9]{2}-[0-9]{2}\\s[0-9]{2}[:][0-9]{2}[:][0-9]{2}\\s[+-]{1}[0-9]{2}[:][0-9]{2}"));
                startTimeAsStringArray[0] = within.getTimeRange().get(0);
                startTimeAsUnixArray[0] = Expression.function("time", "timestampInMilliseconds",
                        startTimeAsStringArray[0], Expression.value("yyyy-MM-dd HH:mm:ss ZZ"));
                endTimeAsUnixArray[0] = Expression.add(startTimeAsUnixArray[0], Expression.value(1000L));
                completeWithinExpressionArray[0] = Expression.and(
                        Expression.compare(Expression.variable("_TIMESTAMP"), Compare.Operator.GREATER_THAN_EQUAL,
                                startTimeAsUnixArray[0]),
                        Expression.compare(Expression.variable("_TIMESTAMP"), Compare.Operator.LESS_THAN,
                                endTimeAsUnixArray[0]));
                break;
            case MINUTES:
                regexArray[1] = Expression.function("str", "regexp", within.getTimeRange().get(0), Expression.value(
                        "[0-9]{4}-[0-9]{2}-[0-9]{2}\\s[0-9]{2}[:][0-9]{2}[:]\\*\\*\\s[+-]{1}[0-9]{2}[:][0-9]{2}"));
                startTimeAsStringArray[1] = Expression.function("str", "replaceAll", within.getTimeRange().get(0),
                        Expression.value("\\*"), Expression.value("0"));
                startTimeAsUnixArray[1] = Expression.function("time", "timestampInMilliseconds",
                        startTimeAsStringArray[1], Expression.value("yyyy-MM-dd HH:mm:ss ZZ"));
                endTimeAsUnixArray[1] = Expression.add(startTimeAsUnixArray[1], Expression.value(60000L));
                completeWithinExpressionArray[1] = Expression.and(
                        Expression.compare(Expression.variable("_TIMESTAMP"), Compare.Operator.GREATER_THAN_EQUAL,
                                startTimeAsUnixArray[1]),
                        Expression.compare(Expression.variable("_TIMESTAMP"), Compare.Operator.LESS_THAN,
                                endTimeAsUnixArray[1]));
                break;
            case HOURS:
                regexArray[2] = Expression.function("str", "regexp", within.getTimeRange().get(0), Expression
                        .value("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s[0-9]{2}[:]\\*\\*[:]\\*\\*\\s[+-]{1}[0-9]{2}[:][0-9]{2}"));
                startTimeAsStringArray[2] = Expression.function("str", "replaceAll", within.getTimeRange().get(0),
                        Expression.value("\\*"), Expression.value("0"));
                startTimeAsUnixArray[2] = Expression.function("time", "timestampInMilliseconds",
                        startTimeAsStringArray[2], Expression.value("yyyy-MM-dd HH:mm:ss ZZ"));
                endTimeAsUnixArray[2] = Expression.add(startTimeAsUnixArray[2], Expression.value(3600000L));
                completeWithinExpressionArray[2] = Expression.and(
                        Expression.compare(Expression.variable("_TIMESTAMP"), Compare.Operator.GREATER_THAN_EQUAL,
                                startTimeAsUnixArray[2]),
                        Expression.compare(Expression.variable("_TIMESTAMP"), Compare.Operator.LESS_THAN,
                                endTimeAsUnixArray[2]));
                break;
            case DAYS:
                regexArray[3] = Expression.function("str", "regexp", within.getTimeRange().get(0), Expression
                        .value("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s\\*\\*[:]\\*\\*[:]\\*\\*\\s[+-]{1}[0-9]{2}[:][0-9]{2}"));
                startTimeAsStringArray[3] = Expression.function("str", "replaceAll", within.getTimeRange().get(0),
                        Expression.value("\\*"), Expression.value("0"));
                startTimeAsUnixArray[3] = Expression.function("time", "timestampInMilliseconds",
                        startTimeAsStringArray[3], Expression.value("yyyy-MM-dd HH:mm:ss ZZ"));
                endTimeAsUnixArray[3] = Expression.add(startTimeAsUnixArray[3], Expression.value(86400000L));
                completeWithinExpressionArray[3] = Expression.and(
                        Expression.compare(Expression.variable("_TIMESTAMP"), Compare.Operator.GREATER_THAN_EQUAL,
                                startTimeAsUnixArray[3]),
                        Expression.compare(Expression.variable("_TIMESTAMP"), Compare.Operator.LESS_THAN,
                                endTimeAsUnixArray[3]));
                break;
            case MONTHS:
                regexArray[4] = Expression.function("str", "regexp", within.getTimeRange().get(0), Expression
                        .value("[0-9]{4}-[0-9]{2}-\\*\\*\\s\\*\\*[:]\\*\\*[:]\\*\\*\\s[+-]{1}[0-9]{2}[:][0-9]{2}"));
                startTimeAsStringArray[4] = Expression.function("str", "replaceFirst", within.getTimeRange().get(0),
                        Expression.value("\\*\\*\\s\\*\\*[:]\\*\\*[:]\\*\\*"), Expression.value("01 00:00:00"));
                startTimeAsUnixArray[4] = Expression.function("time", "timestampInMilliseconds",
                        startTimeAsStringArray[4], Expression.value("yyyy-MM-dd HH:mm:ss ZZ"));

                timeZone = Expression.function("str", "split", startTimeAsStringArray[4], Expression.value(" "),
                        Expression.value(2));
                thisYear = Expression.function("str", "split", startTimeAsStringArray[4], Expression.value("-"),
                        Expression.value(0));
                Expression thisMonth = Expression.function("str", "split", startTimeAsStringArray[4],
                        Expression.value("-"), Expression.value(1));
                getNextYearBeginning = Expression.function("str", "concat",
                        Expression.function("convert",
                                Expression.add(Expression.function("convert", thisYear, Expression.value("int")),
                                        Expression.value(1)),
                                Expression.value("string")),
                        Expression.value("-01-01 00:00:00 "), timeZone);
                Expression getThisYearNextMonth = Expression.function("convert", Expression
                        .add(Expression.function("convert", thisMonth, Expression.value("int")), Expression.value(1)),
                        Expression.value("string"));
                Expression getThisYearNextMonthBeginning = Expression.function("str", "concat", thisYear,
                        Expression.value("-"),
                        Expression.function("ifThenElse",
                                Expression.compare(Expression.function("str", "length", getThisYearNextMonth),
                                        Compare.Operator.EQUAL, Expression.value(1)),
                                Expression.function("str", "concat", Expression.value("0"), getThisYearNextMonth),
                                getThisYearNextMonth),
                        Expression.value("-01 00:00:00 "), timeZone);
                endTimeAsUnixArray[4] = Expression.function("time", "timestampInMilliseconds",
                        Expression.function("ifThenElse",
                                Expression.compare(thisMonth, Compare.Operator.EQUAL, Expression.value("12")),
                                getNextYearBeginning, getThisYearNextMonthBeginning),
                        Expression.value("yyyy-MM-dd HH:mm:ss ZZ"));
                completeWithinExpressionArray[4] = Expression.and(
                        Expression.compare(Expression.variable("_TIMESTAMP"), Compare.Operator.GREATER_THAN_EQUAL,
                                startTimeAsUnixArray[4]),
                        Expression.compare(Expression.variable("_TIMESTAMP"), Compare.Operator.LESS_THAN,
                                endTimeAsUnixArray[4]));
                break;
            case YEARS:
                regexArray[5] = Expression.function("str", "regexp", within.getTimeRange().get(0), Expression
                        .value("[0-9]{4}-\\*\\*-\\*\\*\\s\\*\\*[:]\\*\\*[:]\\*\\*\\s[+-]{1}[0-9]{2}[:][0-9]{2}"));
                startTimeAsStringArray[5] = Expression.function("str", "replaceFirst", within.getTimeRange().get(0),
                        Expression.value("\\*\\*-\\*\\*\\s\\*\\*[:]\\*\\*[:]\\*\\*"),
                        Expression.value("01-01 00:00:00"));
                startTimeAsUnixArray[5] = Expression.function("time", "timestampInMilliseconds",
                        startTimeAsStringArray[5], Expression.value("yyyy-MM-dd HH:mm:ss ZZ"));

                timeZone = Expression.function("str", "split", startTimeAsStringArray[5], Expression.value(" "),
                        Expression.value(2));
                thisYear = Expression.function("str", "split", startTimeAsStringArray[5], Expression.value("-"),
                        Expression.value(0));
                getNextYearBeginning = Expression.function("str", "concat",
                        Expression.function("convert",
                                Expression.add(Expression.function("convert", thisYear, Expression.value("int")),
                                        Expression.value(1)),
                                Expression.value("string")),
                        Expression.value("-01-01 00:00:00 "), timeZone);
                endTimeAsUnixArray[5] = Expression.function("time", "timestampInMilliseconds", getNextYearBeginning,
                        Expression.value("yyyy-MM-dd HH:mm:ss ZZ"));
                completeWithinExpressionArray[5] = Expression.and(
                        Expression.compare(Expression.variable("_TIMESTAMP"), Compare.Operator.GREATER_THAN_EQUAL,
                                startTimeAsUnixArray[5]),
                        Expression.compare(Expression.variable("_TIMESTAMP"), Compare.Operator.LESS_THAN,
                                endTimeAsUnixArray[5]));
                break;
            }
        }
    }
}
