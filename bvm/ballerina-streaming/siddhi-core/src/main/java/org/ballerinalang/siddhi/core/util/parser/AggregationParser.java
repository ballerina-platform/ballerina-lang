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

package org.ballerinalang.siddhi.core.util.parser;

import org.ballerinalang.siddhi.core.aggregation.AggregationRuntime;
import org.ballerinalang.siddhi.core.aggregation.IncrementalAggregationProcessor;
import org.ballerinalang.siddhi.core.aggregation.IncrementalExecutor;
import org.ballerinalang.siddhi.core.aggregation.RecreateInMemoryData;
import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.stream.MetaStreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.VariableExpressionExecutor;
import org.ballerinalang.siddhi.core.query.input.stream.StreamRuntime;
import org.ballerinalang.siddhi.core.query.input.stream.single.EntryValveExecutor;
import org.ballerinalang.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.ballerinalang.siddhi.core.query.selector.GroupByKeyGenerator;
import org.ballerinalang.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalAttributeAggregator;
import org.ballerinalang.siddhi.core.table.Table;
import org.ballerinalang.siddhi.core.util.ExceptionUtil;
import org.ballerinalang.siddhi.core.util.Scheduler;
import org.ballerinalang.siddhi.core.util.SiddhiAppRuntimeBuilder;
import org.ballerinalang.siddhi.core.util.SiddhiClassLoader;
import org.ballerinalang.siddhi.core.util.SiddhiConstants;
import org.ballerinalang.siddhi.core.util.extension.holder.FunctionExecutorExtensionHolder;
import org.ballerinalang.siddhi.core.util.extension.holder.IncrementalAttributeAggregatorExtensionHolder;
import org.ballerinalang.siddhi.core.util.lock.LockWrapper;
import org.ballerinalang.siddhi.core.util.parser.helper.QueryParserHelper;
import org.ballerinalang.siddhi.core.util.statistics.LatencyTracker;
import org.ballerinalang.siddhi.core.util.statistics.ThroughputTracker;
import org.ballerinalang.siddhi.core.window.Window;
import org.ballerinalang.siddhi.query.api.aggregation.TimePeriod;
import org.ballerinalang.siddhi.query.api.annotation.Annotation;
import org.ballerinalang.siddhi.query.api.annotation.Element;
import org.ballerinalang.siddhi.query.api.definition.AbstractDefinition;
import org.ballerinalang.siddhi.query.api.definition.AggregationDefinition;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;
import org.ballerinalang.siddhi.query.api.definition.TableDefinition;
import org.ballerinalang.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.ballerinalang.siddhi.query.api.expression.AttributeFunction;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.Variable;
import org.ballerinalang.siddhi.query.api.util.AnnotationHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * This is the parser class of incremental aggregation definition.
 */
public class AggregationParser {

    public static AggregationRuntime parse(AggregationDefinition aggregationDefinition,
                                           SiddhiAppContext siddhiAppContext,
                                           Map<String, AbstractDefinition> streamDefinitionMap,
                                           Map<String, AbstractDefinition> tableDefinitionMap,
                                           Map<String, AbstractDefinition> windowDefinitionMap,
                                           Map<String, AbstractDefinition> aggregationDefinitionMap,
                                           Map<String, Table> tableMap,
                                           Map<String, Window> windowMap,
                                           Map<String, AggregationRuntime> aggregationMap,
                                           SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder) {

        if (aggregationDefinition == null) {
            throw new SiddhiAppCreationException(
                    "AggregationDefinition instance is null. " +
                            "Hence, can't create the siddhi app '" + siddhiAppContext.getName() + "'");
        }
        if (aggregationDefinition.getTimePeriod() == null) {
            throw new SiddhiAppCreationException(
                    "AggregationDefinition '" + aggregationDefinition.getId() + "'s timePeriod is null. " +
                            "Hence, can't create the siddhi app '" + siddhiAppContext.getName() + "'",
                    aggregationDefinition.getQueryContextStartIndex(), aggregationDefinition.getQueryContextEndIndex());
        }
        if (aggregationDefinition.getSelector() == null) {
            throw new SiddhiAppCreationException(
                    "AggregationDefinition '" + aggregationDefinition.getId() + "'s selection is not defined. " +
                            "Hence, can't create the siddhi app '" + siddhiAppContext.getName() + "'",
                    aggregationDefinition.getQueryContextStartIndex(), aggregationDefinition.getQueryContextEndIndex());
        }
        if (streamDefinitionMap.get(aggregationDefinition.getBasicSingleInputStream().getStreamId()) == null) {
            throw new SiddhiAppCreationException("Stream " + aggregationDefinition.getBasicSingleInputStream().
                    getStreamId() + " has not been defined");
        }
        try {
            List<VariableExpressionExecutor> incomingVariableExpressionExecutors = new ArrayList<>();

            String aggregatorName = aggregationDefinition.getId();

            StreamRuntime streamRuntime = InputStreamParser.parse(aggregationDefinition.getBasicSingleInputStream(),
                    siddhiAppContext, streamDefinitionMap, tableDefinitionMap, windowDefinitionMap,
                    aggregationDefinitionMap, tableMap, windowMap, aggregationMap, incomingVariableExpressionExecutors,
                    null, false, aggregatorName);

            // Get original meta for later use.
            MetaStreamEvent incomingMetaStreamEvent = (MetaStreamEvent) streamRuntime.getMetaComplexEvent();
            // Create new meta stream event.
            // This must hold the timestamp, group by attributes (if given) and the incremental attributes, in
            // onAfterWindowData array
            // Example format: AGG_TIMESTAMP, groupByAttribute1, groupByAttribute2, AGG_incAttribute1, AGG_incAttribute2
            // AGG_incAttribute1, AGG_incAttribute2 would have the same attribute names as in
            // finalListOfIncrementalAttributes
            incomingMetaStreamEvent.initializeAfterWindowData(); // To enter data as onAfterWindowData

            List<ExpressionExecutor> incomingExpressionExecutors = new ArrayList<>();
            List<IncrementalAttributeAggregator> incrementalAttributeAggregators = new ArrayList<>();
            List<Variable> groupByVariableList = aggregationDefinition.getSelector().getGroupByList();
            boolean isProcessingOnExternalTime = aggregationDefinition.getAggregateAttribute() != null;
            List<Expression> outputExpressions = new ArrayList<>(); //Expressions to get
            // final aggregate outputs. e.g avg = sum/count
            List<ExpressionExecutor> outputExpressionExecutors = new ArrayList<>(); //Expression executors to get
            // final aggregate outputs. e.g avg = sum/count

            populateIncomingAggregatorsAndExecutors(aggregationDefinition, siddhiAppContext, tableMap,
                    incomingVariableExpressionExecutors, aggregatorName, incomingMetaStreamEvent,
                    incomingExpressionExecutors, incrementalAttributeAggregators, groupByVariableList,
                    outputExpressions);

            int baseAggregatorBeginIndex = incomingMetaStreamEvent.getOutputData().size();

            List<Expression> finalBaseAggregators = getFinalBaseAggregators(siddhiAppContext, tableMap,
                    incomingVariableExpressionExecutors, aggregatorName, incomingMetaStreamEvent,
                    incomingExpressionExecutors, incrementalAttributeAggregators);

            StreamDefinition incomingOutputStreamDefinition = StreamDefinition.id("");
            incomingOutputStreamDefinition.setQueryContextStartIndex(aggregationDefinition.getQueryContextStartIndex());
            incomingOutputStreamDefinition.setQueryContextEndIndex(aggregationDefinition.getQueryContextEndIndex());
            MetaStreamEvent processedMetaStreamEvent = new MetaStreamEvent();
            for (Attribute attribute : incomingMetaStreamEvent.getOutputData()) {
                incomingOutputStreamDefinition.attribute(attribute.getName(), attribute.getType());
                processedMetaStreamEvent.addOutputData(attribute);
            }
            incomingMetaStreamEvent.setOutputDefinition(incomingOutputStreamDefinition);
            processedMetaStreamEvent.addInputDefinition(incomingOutputStreamDefinition);
            processedMetaStreamEvent.setOutputDefinition(incomingOutputStreamDefinition);

            // Executors of processing meta
            List<VariableExpressionExecutor> processVariableExpressionExecutors = new ArrayList<>();
            boolean groupBy = aggregationDefinition.getSelector().getGroupByList().size() != 0;

            List<ExpressionExecutor> processExpressionExecutors = constructProcessExpressionExecutors(
                    siddhiAppContext, tableMap, aggregatorName, baseAggregatorBeginIndex,
                    finalBaseAggregators, incomingOutputStreamDefinition, processedMetaStreamEvent,
                    processVariableExpressionExecutors, groupBy);

            outputExpressionExecutors.addAll(outputExpressions.stream().map(expression -> ExpressionParser.
                    parseExpression(expression, processedMetaStreamEvent, 0, tableMap,
                            processVariableExpressionExecutors, siddhiAppContext,
                            groupBy, 0, aggregatorName)).collect(Collectors.toList()));

            // Create group by key generator
            GroupByKeyGenerator groupByKeyGenerator = null;
            if (groupBy) {
                groupByKeyGenerator = new GroupByKeyGenerator(groupByVariableList, processedMetaStreamEvent,
                        SiddhiConstants.UNKNOWN_STATE, tableMap, processVariableExpressionExecutors, siddhiAppContext,
                        aggregatorName);
            }

            // Create new scheduler
            EntryValveExecutor entryValveExecutor = new EntryValveExecutor(siddhiAppContext);
            LockWrapper lockWrapper = new LockWrapper(aggregatorName);
            lockWrapper.setLock(new ReentrantLock());

            Scheduler scheduler = SchedulerParser.parse(siddhiAppContext.getScheduledExecutorService(),
                    entryValveExecutor, siddhiAppContext);
            scheduler.init(lockWrapper, aggregatorName);
            scheduler.setStreamEventPool(new StreamEventPool(processedMetaStreamEvent, 10));

            QueryParserHelper.reduceMetaComplexEvent(incomingMetaStreamEvent);
            QueryParserHelper.reduceMetaComplexEvent(processedMetaStreamEvent);
            QueryParserHelper.updateVariablePosition(incomingMetaStreamEvent, incomingVariableExpressionExecutors);
            QueryParserHelper.updateVariablePosition(processedMetaStreamEvent, processVariableExpressionExecutors);


            List<TimePeriod.Duration> incrementalDurations = getSortedPeriods(aggregationDefinition.getTimePeriod());
            Map<TimePeriod.Duration, Table> aggregationTables = initDefaultTables(aggregatorName, incrementalDurations,
                    processedMetaStreamEvent.getOutputStreamDefinition(), siddhiAppRuntimeBuilder,
                    aggregationDefinition.getAnnotations(), groupByVariableList);

            int bufferSize = 0;
            Element element = AnnotationHelper.getAnnotationElement(SiddhiConstants.ANNOTATION_BUFFER_SIZE, null,
                    aggregationDefinition.getAnnotations());
            if (element != null) {
                try {
                    bufferSize = Integer.parseInt(element.getValue());
                } catch (NumberFormatException e) {
                    throw new SiddhiAppCreationException(e.getMessage() + ": BufferSize must be an integer");
                }
            }
            if (bufferSize > 0) {
                TimePeriod.Duration rootDuration = incrementalDurations.get(0);
                if (rootDuration == TimePeriod.Duration.MONTHS || rootDuration == TimePeriod.Duration.YEARS) {
                    throw new SiddhiAppCreationException("A buffer size greater than 0 can be provided, only when the "
                            + "first duration value is seconds, minutes, hours or days");
                }
                if (!isProcessingOnExternalTime) {
                    throw new SiddhiAppCreationException("Buffer size cannot be specified when events are aggregated " +
                            "based on event arrival time.");
                    //Buffer size is used to process out of order events. However, events would never be out of
                    // order if they are processed based on event arrival time.
                }
            } else if (bufferSize < 0) {
                throw new SiddhiAppCreationException("Expected a positive integer as the buffer size, but found "
                        + bufferSize + " as the provided value");
            }

            boolean ignoreEventsOlderThanBuffer = false;
            element = AnnotationHelper.getAnnotationElement(SiddhiConstants.ANNOTATION_IGNORE_EVENTS_OLDER_THAN_BUFFER,
                    null, aggregationDefinition.getAnnotations());
            if (element != null) {
                if (element.getValue().equalsIgnoreCase("true")) {
                    ignoreEventsOlderThanBuffer = true;
                } else if (!element.getValue().equalsIgnoreCase("false")) {
                    throw new SiddhiAppCreationException("IgnoreEventsOlderThanBuffer value must " +
                            "be true or false");
                }
            }


            Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap = buildIncrementalExecutors(
                    isProcessingOnExternalTime, processedMetaStreamEvent, processExpressionExecutors,
                    groupByKeyGenerator, bufferSize, ignoreEventsOlderThanBuffer, incrementalDurations,
                    aggregationTables, siddhiAppContext, aggregatorName);

            //Recreate in-memory data from tables
            RecreateInMemoryData recreateInMemoryData = new RecreateInMemoryData(incrementalDurations,
                    aggregationTables, incrementalExecutorMap, siddhiAppContext, processedMetaStreamEvent, tableMap,
                    windowMap, aggregationMap);

            IncrementalExecutor rootIncrementalExecutor = incrementalExecutorMap.get(incrementalDurations.get(0));
            rootIncrementalExecutor.setScheduler(scheduler);
            // Connect entry valve to root incremental executor
            entryValveExecutor.setNextExecutor(rootIncrementalExecutor);

            QueryParserHelper.initStreamRuntime(streamRuntime, incomingMetaStreamEvent, lockWrapper, aggregatorName);

            LatencyTracker latencyTrackerFind = null;
            LatencyTracker latencyTrackerInsert = null;

            ThroughputTracker throughputTrackerFind = null;
            ThroughputTracker throughputTrackerInsert = null;

            if (siddhiAppContext.getStatisticsManager() != null) {
                latencyTrackerFind = QueryParserHelper.createLatencyTracker(siddhiAppContext,
                        aggregationDefinition.getId(),
                        SiddhiConstants.METRIC_INFIX_WINDOWS, SiddhiConstants.METRIC_TYPE_FIND);
                latencyTrackerInsert = QueryParserHelper.createLatencyTracker(siddhiAppContext,
                        aggregationDefinition.getId(),
                        SiddhiConstants.METRIC_INFIX_WINDOWS, SiddhiConstants.METRIC_TYPE_INSERT);

                throughputTrackerFind = QueryParserHelper.createThroughputTracker(siddhiAppContext,
                        aggregationDefinition.getId(),
                        SiddhiConstants.METRIC_INFIX_WINDOWS, SiddhiConstants.METRIC_TYPE_FIND);
                throughputTrackerInsert = QueryParserHelper.createThroughputTracker(siddhiAppContext,
                        aggregationDefinition.getId(),
                        SiddhiConstants.METRIC_INFIX_WINDOWS, SiddhiConstants.METRIC_TYPE_INSERT);

            }

            streamRuntime.setCommonProcessor(new IncrementalAggregationProcessor(rootIncrementalExecutor,
                    incomingExpressionExecutors, processedMetaStreamEvent, latencyTrackerInsert,
                    throughputTrackerInsert, siddhiAppContext));

            List<ExpressionExecutor> baseExecutors = cloneExpressionExecutors(processExpressionExecutors);
            ExpressionExecutor timestampExecutor = baseExecutors.remove(0);
            return new AggregationRuntime(aggregationDefinition, incrementalExecutorMap,
                    aggregationTables, ((SingleStreamRuntime) streamRuntime), entryValveExecutor, incrementalDurations,
                    siddhiAppContext, baseExecutors, timestampExecutor, processedMetaStreamEvent,
                    outputExpressionExecutors, latencyTrackerFind, throughputTrackerFind, recreateInMemoryData);
        } catch (Throwable t) {
            ExceptionUtil.populateQueryContext(t, aggregationDefinition, siddhiAppContext);
            throw t;
        }
    }

    private static Map<TimePeriod.Duration, IncrementalExecutor> buildIncrementalExecutors(
            boolean isProcessingOnExternalTime, MetaStreamEvent processedMetaStreamEvent,
            List<ExpressionExecutor> processExpressionExecutors,
            GroupByKeyGenerator groupByKeyGenerator, int bufferSize, boolean ignoreEventsOlderThanBuffer,
            List<TimePeriod.Duration> incrementalDurations,
            Map<TimePeriod.Duration, Table> aggregationTables, SiddhiAppContext siddhiAppContext,
            String aggregatorName) {
        Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap = new HashMap<>();
        // Create incremental executors
        IncrementalExecutor child;
        IncrementalExecutor root = null;
        for (int i = incrementalDurations.size() - 1; i >= 0; i--) {
            // Base incremental expression executors created using new meta
            boolean isRoot = false;
            if (i == 0) {
                isRoot = true;
            }
            child = root;
            TimePeriod.Duration duration = incrementalDurations.get(i);
            IncrementalExecutor incrementalExecutor = new IncrementalExecutor(duration,
                    cloneExpressionExecutors(processExpressionExecutors),
                    groupByKeyGenerator, processedMetaStreamEvent, bufferSize, ignoreEventsOlderThanBuffer,
                    child, isRoot, aggregationTables.get(duration), isProcessingOnExternalTime, siddhiAppContext,
                    aggregatorName);
            incrementalExecutorMap.put(duration, incrementalExecutor);
            root = incrementalExecutor;


        }
        return incrementalExecutorMap;
    }

    private static List<ExpressionExecutor> constructProcessExpressionExecutors(
            SiddhiAppContext siddhiAppContext, Map<String, Table> tableMap,
            String aggregatorName, int baseAggregatorBeginIndex,
            List<Expression> finalBaseAggregators,
            StreamDefinition incomingOutputStreamDefinition,
            MetaStreamEvent processedMetaStreamEvent,
            List<VariableExpressionExecutor> processVariableExpressionExecutors, boolean groupBy) {
        List<ExpressionExecutor> processExpressionExecutors = new ArrayList<>();
        List<Attribute> attributeList = incomingOutputStreamDefinition.getAttributeList();
        for (int i = 0; i < baseAggregatorBeginIndex; i++) {
            Attribute attribute = attributeList.get(i);
            VariableExpressionExecutor variableExpressionExecutor = (VariableExpressionExecutor) ExpressionParser
                    .parseExpression(new Variable(attribute.getName()), processedMetaStreamEvent, 0,
                            tableMap, processVariableExpressionExecutors, siddhiAppContext, groupBy,
                            0, aggregatorName);
            processExpressionExecutors.add(variableExpressionExecutor);
        }

        for (Expression expression : finalBaseAggregators) {
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                    processedMetaStreamEvent, 0, tableMap, processVariableExpressionExecutors,
                    siddhiAppContext, groupBy, 0, aggregatorName);
            processExpressionExecutors.add(expressionExecutor);
        }
        return processExpressionExecutors;
    }

    private static List<Expression> getFinalBaseAggregators(
            SiddhiAppContext siddhiAppContext, Map<String, Table> tableMap,
            List<VariableExpressionExecutor> incomingVariableExpressionExecutors, String aggregatorName,
            MetaStreamEvent incomingMetaStreamEvent, List<ExpressionExecutor> incomingExpressionExecutors,
            List<IncrementalAttributeAggregator> incrementalAttributeAggregators) {
        List<Attribute> finalBaseAttributes = new ArrayList<>();
        List<Expression> finalBaseAggregators = new ArrayList<>();

        for (IncrementalAttributeAggregator incrementalAttributeAggregator : incrementalAttributeAggregators) {
            Attribute[] baseAttributes = incrementalAttributeAggregator.getBaseAttributes();
            Expression[] baseAttributeInitialValues = incrementalAttributeAggregator.getBaseAttributeInitialValues();
            Expression[] baseAggregators = incrementalAttributeAggregator.getBaseAggregators();
            for (int i = 0; i < baseAttributes.length; i++) {
                validateBaseAggregators(incrementalAttributeAggregators,
                        incrementalAttributeAggregator, baseAttributes,
                        baseAttributeInitialValues, baseAggregators, i);
                if (!finalBaseAttributes.contains(baseAttributes[i])) {
                    finalBaseAttributes.add(baseAttributes[i]);
                    finalBaseAggregators.add(baseAggregators[i]);
                    incomingMetaStreamEvent.addOutputData(baseAttributes[i]);
                    incomingExpressionExecutors.add(ExpressionParser.parseExpression(baseAttributeInitialValues[i],
                            incomingMetaStreamEvent, 0, tableMap, incomingVariableExpressionExecutors,
                            siddhiAppContext, false, 0, aggregatorName));
                }
            }
        }
        return finalBaseAggregators;
    }

    private static void populateIncomingAggregatorsAndExecutors(
            AggregationDefinition aggregationDefinition, SiddhiAppContext siddhiAppContext,
            Map<String, Table> tableMap, List<VariableExpressionExecutor> incomingVariableExpressionExecutors,
            String aggregatorName, MetaStreamEvent incomingMetaStreamEvent,
            List<ExpressionExecutor> incomingExpressionExecutors,
            List<IncrementalAttributeAggregator> incrementalAttributeAggregators, List<Variable> groupByVariableList,
            List<Expression> outputExpressions) {
        ExpressionExecutor[] timeStampTimeZoneExecutors = setTimeStampTimeZoneExecutors(aggregationDefinition,
                siddhiAppContext, tableMap, incomingVariableExpressionExecutors, aggregatorName,
                incomingMetaStreamEvent);
        ExpressionExecutor timestampExecutor = timeStampTimeZoneExecutors[0];
        ExpressionExecutor timeZoneExecutor = timeStampTimeZoneExecutors[1];
        Attribute timestampAttribute = new Attribute("AGG_TIMESTAMP", Attribute.Type.LONG);
        incomingMetaStreamEvent.addOutputData(timestampAttribute);
        incomingExpressionExecutors.add(timestampExecutor);

        incomingMetaStreamEvent.addOutputData(new Attribute("AGG_TIMEZONE", Attribute.Type.STRING));
        incomingExpressionExecutors.add(timeZoneExecutor);

        AbstractDefinition incomingLastInputStreamDefinition = incomingMetaStreamEvent.getLastInputDefinition();
        for (Variable groupByVariable : groupByVariableList) {
            incomingMetaStreamEvent.addOutputData(incomingLastInputStreamDefinition.getAttributeList()
                    .get(incomingLastInputStreamDefinition.getAttributePosition(
                            groupByVariable.getAttributeName())));
            incomingExpressionExecutors.add(ExpressionParser.parseExpression(groupByVariable,
                    incomingMetaStreamEvent, 0, tableMap, incomingVariableExpressionExecutors,
                    siddhiAppContext, false, 0, aggregatorName));
        }

        // Add AGG_TIMESTAMP to output as well
        outputExpressions.add(Expression.variable("AGG_TIMESTAMP"));
        aggregationDefinition.getAttributeList().add(timestampAttribute);
        for (OutputAttribute outputAttribute : aggregationDefinition.getSelector().getSelectionList()) {
            Expression expression = outputAttribute.getExpression();
            if (expression instanceof AttributeFunction) {
                IncrementalAttributeAggregator incrementalAggregator = null;
                try {
                    incrementalAggregator = (IncrementalAttributeAggregator)
                            SiddhiClassLoader.loadExtensionImplementation(
                                    new AttributeFunction("incrementalAggregator",
                                            ((AttributeFunction) expression).getName(),
                                            ((AttributeFunction) expression).getParameters()),
                                    IncrementalAttributeAggregatorExtensionHolder.getInstance(siddhiAppContext));
                } catch (SiddhiAppCreationException ex) {
                    try {
                        SiddhiClassLoader.loadExtensionImplementation((AttributeFunction) expression,
                                FunctionExecutorExtensionHolder.getInstance(siddhiAppContext));
                        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                                incomingMetaStreamEvent, 0, tableMap, incomingVariableExpressionExecutors,
                                siddhiAppContext, false, 0, aggregatorName);
                        incomingExpressionExecutors.add(expressionExecutor);
                        incomingMetaStreamEvent.addOutputData(
                                new Attribute(outputAttribute.getRename(), expressionExecutor.getReturnType()));
                        aggregationDefinition.getAttributeList().add(
                                new Attribute(outputAttribute.getRename(), expressionExecutor.getReturnType()));
                        outputExpressions.add(Expression.variable(outputAttribute.getRename()));
                    } catch (SiddhiAppCreationException e) {
                        throw new SiddhiAppCreationException("'" + ((AttributeFunction) expression).getName() +
                                "' is neither a incremental attribute aggregator extension or a function" +
                                " extension", expression.getQueryContextStartIndex(),
                                expression.getQueryContextEndIndex());
                    }
                }
                if (incrementalAggregator != null) {
                    initIncrementalAttributeAggregator(incomingLastInputStreamDefinition,
                            (AttributeFunction) expression, incrementalAggregator);
                    incrementalAttributeAggregators.add(incrementalAggregator);
                    aggregationDefinition.getAttributeList().add(
                            new Attribute(outputAttribute.getRename(), incrementalAggregator.getReturnType()));
                    outputExpressions.add(incrementalAggregator.aggregate());
                }
            } else {
                if (expression instanceof Variable && groupByVariableList.contains(expression)) {
                    Attribute groupByAttribute = null;
                    for (Attribute attribute : incomingMetaStreamEvent.getOutputData()) {
                        if (attribute.getName().equals(((Variable) expression).getAttributeName())) {
                            groupByAttribute = attribute;
                            break;
                        }
                    }
                    if (groupByAttribute == null) {
                        throw new SiddhiAppCreationException("Expected GroupBy attribute '" +
                                ((Variable) expression).getAttributeName() + "' not used in aggregation '" +
                                aggregatorName + "' processing.", expression.getQueryContextStartIndex(),
                                expression.getQueryContextEndIndex());
                    }
                    aggregationDefinition.getAttributeList().add(
                            new Attribute(outputAttribute.getRename(), groupByAttribute.getType()));
                    outputExpressions.add(Expression.variable(groupByAttribute.getName()));

                } else {
                    ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                            incomingMetaStreamEvent, 0, tableMap, incomingVariableExpressionExecutors,
                            siddhiAppContext, false, 0, aggregatorName);
                    incomingExpressionExecutors.add(expressionExecutor);
                    incomingMetaStreamEvent.addOutputData(
                            new Attribute(outputAttribute.getRename(), expressionExecutor.getReturnType()));
                    aggregationDefinition.getAttributeList().add(
                            new Attribute(outputAttribute.getRename(), expressionExecutor.getReturnType()));
                    outputExpressions.add(Expression.variable(outputAttribute.getRename()));
                }
            }
        }
    }

    private static List<ExpressionExecutor> cloneExpressionExecutors(List<ExpressionExecutor> expressionExecutors) {
        List<ExpressionExecutor> arrayList = expressionExecutors.stream().map(expressionExecutor ->
                expressionExecutor.cloneExecutor(null)).collect(Collectors.toList());
        return arrayList;
    }

    private static void validateBaseAggregators(List<IncrementalAttributeAggregator> incrementalAttributeAggregators,
                                                IncrementalAttributeAggregator incrementalAttributeAggregator,
                                                Attribute[] baseAttributes, Expression[] baseAttributeInitialValues,
                                                Expression[] baseAggregators, int i) {
        for (int i1 = i; i1 < incrementalAttributeAggregators.size(); i1++) {
            IncrementalAttributeAggregator otherAttributeAggregator = incrementalAttributeAggregators.get(i1);
            if (otherAttributeAggregator != incrementalAttributeAggregator) {
                Attribute[] otherBaseAttributes = otherAttributeAggregator.getBaseAttributes();
                Expression[] otherBaseAttributeInitialValues = otherAttributeAggregator
                        .getBaseAttributeInitialValues();
                Expression[] otherBaseAggregators = otherAttributeAggregator.getBaseAggregators();
                for (int j = 0; j < otherBaseAttributes.length; j++) {
                    if (baseAttributes[i].equals(otherBaseAttributes[j])) {
                        if (!baseAttributeInitialValues[i].equals(otherBaseAttributeInitialValues[j])) {
                            throw new SiddhiAppCreationException("BaseAttributes having same name should " +
                                    "be defined with same initial values, but baseAttribute '" +
                                    baseAttributes[i] + "' is defined in '" +
                                    incrementalAttributeAggregator.getClass().getName() + "' and '" +
                                    otherAttributeAggregator.getClass().getName() +
                                    "' with different initial values.");
                        }
                        if (!baseAggregators[i].equals(otherBaseAggregators[j])) {
                            throw new SiddhiAppCreationException("BaseAttributes having same name should " +
                                    "be defined with same baseAggregators, but baseAttribute '" +
                                    baseAttributes[i] + "' is defined in '" +
                                    incrementalAttributeAggregator.getClass().getName() + "' and '" +
                                    otherAttributeAggregator.getClass().getName() +
                                    "' with different baseAggregators.");
                        }
                    }
                }
            }
        }
    }

    private static void initIncrementalAttributeAggregator(
            AbstractDefinition lastInputStreamDefinition, AttributeFunction attributeFunction,
            IncrementalAttributeAggregator incrementalAttributeAggregator) {
        String attributeName = null;
        Attribute.Type attributeType = null;
        if (attributeFunction.getParameters() != null && attributeFunction.getParameters()[0] != null) {
            if (attributeFunction.getParameters().length != 1) {
                throw new SiddhiAppCreationException("Incremental aggregator requires only on one parameter. "
                        + "Found " + attributeFunction.getParameters().length,
                        attributeFunction.getQueryContextStartIndex(), attributeFunction.getQueryContextEndIndex());
            }
            if (!(attributeFunction.getParameters()[0] instanceof Variable)) {
                throw new SiddhiAppCreationException("Incremental aggregator expected a variable. " +
                        "However a parameter of type " + attributeFunction.getParameters()[0].getClass().getTypeName()
                        + " was found",
                        attributeFunction.getParameters()[0].getQueryContextStartIndex(),
                        attributeFunction.getParameters()[0].getQueryContextEndIndex());
            }
            attributeName = ((Variable) attributeFunction.getParameters()[0]).getAttributeName();
            attributeType = lastInputStreamDefinition.getAttributeType(attributeName);
        }

        incrementalAttributeAggregator.init(attributeName, attributeType);

        Attribute[] baseAttributes = incrementalAttributeAggregator.getBaseAttributes();
        Expression[] baseAttributeInitialValues = incrementalAttributeAggregator
                .getBaseAttributeInitialValues();
        Expression[] baseAggregators = incrementalAttributeAggregator.getBaseAggregators();

        if (baseAttributes.length != baseAggregators.length) {
            throw new SiddhiAppCreationException("Number of baseAggregators '" +
                    baseAggregators.length + "' and baseAttributes '" +
                    baseAttributes.length + "' is not equal for '" + attributeFunction + "'",
                    attributeFunction.getQueryContextStartIndex(), attributeFunction.getQueryContextEndIndex());
        }
        if (baseAttributeInitialValues.length != baseAggregators.length) {
            throw new SiddhiAppCreationException("Number of baseAggregators '" +
                    baseAggregators.length + "' and baseAttributeInitialValues '" +
                    baseAttributeInitialValues.length + "' is not equal for '" +
                    attributeFunction + "'",
                    attributeFunction.getQueryContextStartIndex(), attributeFunction.getQueryContextEndIndex());
        }
    }

    private static ExpressionExecutor[] setTimeStampTimeZoneExecutors(
            AggregationDefinition aggregationDefinition,
            SiddhiAppContext siddhiAppContext,
            Map<String, Table> tableMap,
            List<VariableExpressionExecutor> variableExpressionExecutors,
            String aggregatorName, MetaStreamEvent metaStreamEvent) {
        Expression timestampExpression = aggregationDefinition.getAggregateAttribute();
        Expression timeZoneExpression;
        ExpressionExecutor timestampExecutor;
        ExpressionExecutor timeZoneExecutor;
        boolean isSystemTimeBased = false;

        // Retrieve the external timestamp. If not given, this would return null.
        // When execution is based on system time, the system's time zone would be used.
        if (timestampExpression == null) {
            isSystemTimeBased = true;
            timestampExpression = AttributeFunction.function("currentTimeMillis", null);
        }
        timestampExecutor = ExpressionParser.parseExpression(timestampExpression,
                metaStreamEvent, 0, tableMap, variableExpressionExecutors,
                siddhiAppContext, false, 0, aggregatorName);
        if (timestampExecutor.getReturnType() == Attribute.Type.STRING) {
            Expression expression = AttributeFunction.function("incrementalAggregator",
                    "timestampInMilliseconds", timestampExpression);
            timestampExecutor = ExpressionParser.parseExpression(expression, metaStreamEvent, 0, tableMap,
                    variableExpressionExecutors, siddhiAppContext, false, 0, aggregatorName);
            timeZoneExpression = AttributeFunction.function("incrementalAggregator",
                    "getTimeZone", timestampExpression);
            timeZoneExecutor = ExpressionParser.parseExpression(timeZoneExpression, metaStreamEvent, 0, tableMap,
                    variableExpressionExecutors, siddhiAppContext, false, 0, aggregatorName);
        } else if (timestampExecutor.getReturnType() == Attribute.Type.LONG) {
            if (isSystemTimeBased) {
                timeZoneExpression = AttributeFunction.function("incrementalAggregator",
                        "getTimeZone", null);
                timeZoneExecutor = ExpressionParser.parseExpression(timeZoneExpression, metaStreamEvent, 0, tableMap,
                        variableExpressionExecutors, siddhiAppContext, false, 0, aggregatorName);
            } else {
                timeZoneExpression = Expression.value("+00:00"); //If long value is given, it's assumed that the
                // time zone is GMT
                timeZoneExecutor = ExpressionParser.parseExpression(timeZoneExpression, metaStreamEvent, 0, tableMap,
                        variableExpressionExecutors, siddhiAppContext, false, 0, aggregatorName);
            }
        } else {
            throw new SiddhiAppCreationException(
                    "AggregationDefinition '" + aggregationDefinition.getId() + "'s aggregateAttribute expects " +
                            "long or string, but found " + timestampExecutor.getReturnType() + ". " +
                            "Hence, can't create the siddhi app '" + siddhiAppContext.getName() + "'",
                    timestampExpression.getQueryContextStartIndex(), timestampExpression.getQueryContextEndIndex());
        }
        return new ExpressionExecutor[]{timestampExecutor, timeZoneExecutor};
    }

    private static boolean isRange(TimePeriod timePeriod) {
        return timePeriod.getOperator() == TimePeriod.Operator.RANGE;
    }

    private static List<TimePeriod.Duration> getSortedPeriods(TimePeriod timePeriod) {
        try {
            List<TimePeriod.Duration> durations = timePeriod.getDurations();
            if (isRange(timePeriod)) {
                durations = fillGap(durations.get(0), durations.get(1));
            }
            return sortedDurations(durations);
        } catch (Throwable t) {
            ExceptionUtil.populateQueryContext(t, timePeriod, null);
            throw t;
        }
    }

    private static List<TimePeriod.Duration> sortedDurations(List<TimePeriod.Duration> durations) {
        List<TimePeriod.Duration> copyDurations = new ArrayList<>(durations);

        Comparator periodComparator = new Comparator<TimePeriod.Duration>() {
            public int compare(TimePeriod.Duration firstDuration, TimePeriod.Duration secondDuration) {
                int firstOrdinal = firstDuration.ordinal();
                int secondOrdinal = secondDuration.ordinal();
                if (firstOrdinal > secondOrdinal) {
                    return 1;
                } else if (firstOrdinal < secondOrdinal) {
                    return -1;
                }
                return 0;
            }
        };
        copyDurations.sort(periodComparator);
        return copyDurations;
    }

    private static List<TimePeriod.Duration> fillGap(TimePeriod.Duration start, TimePeriod.Duration end) {
        TimePeriod.Duration[] durations = TimePeriod.Duration.values();
        List<TimePeriod.Duration> filledDurations = new ArrayList<>();

        int startIndex = start.ordinal();
        int endIndex = end.ordinal();

        if (startIndex > endIndex) {
            throw new SiddhiAppCreationException(
                    "Start time period must be less than end time period for range aggregation calculation");
        }

        if (startIndex == endIndex) {
            filledDurations.add(start);
        } else {
            TimePeriod.Duration[] temp = new TimePeriod.Duration[endIndex - startIndex + 1];
            System.arraycopy(durations, startIndex, temp, 0, endIndex - startIndex + 1);
            filledDurations = Arrays.asList(temp);
        }
        return filledDurations;
    }

    private static HashMap<TimePeriod.Duration, Table> initDefaultTables(
            String aggregatorName, List<TimePeriod.Duration> durations,
            StreamDefinition streamDefinition, SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder,
            List<Annotation> annotations, List<Variable> groupByVariableList) {
        HashMap<TimePeriod.Duration, Table> aggregationTableMap = new HashMap<>();
        // Create annotations for primary key
        Annotation primaryKeyAnnotation = new Annotation(SiddhiConstants.ANNOTATION_PRIMARY_KEY);
        primaryKeyAnnotation.element(null, "AGG_TIMESTAMP");
        for (Variable groupByVariable : groupByVariableList) {
            primaryKeyAnnotation.element(null, groupByVariable.getAttributeName());
        }
        annotations.add(primaryKeyAnnotation);
        for (TimePeriod.Duration duration : durations) {
            String tableId = aggregatorName + "_" + duration.toString();
            TableDefinition tableDefinition = TableDefinition.id(tableId);
            for (Attribute attribute : streamDefinition.getAttributeList()) {
                tableDefinition.attribute(attribute.getName(), attribute.getType());
            }
            annotations.forEach(tableDefinition::annotation);
            siddhiAppRuntimeBuilder.defineTable(tableDefinition);
            aggregationTableMap.put(duration, siddhiAppRuntimeBuilder.getTableMap().get(tableId));
        }
        return aggregationTableMap;
    }
}
