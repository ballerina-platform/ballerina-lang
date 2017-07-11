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

package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.input.stream.single.EntryValveExecutor;
import org.wso2.siddhi.core.query.selector.GroupByKeyGenerator;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.CompositeAggregator;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecuteStreamReceiver;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecutor;
import org.wso2.siddhi.core.table.InMemoryTable;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.extension.holder.CompositeAggregatorExtensionHolder;
import org.wso2.siddhi.core.util.lock.LockSynchronizer;
import org.wso2.siddhi.core.util.lock.LockWrapper;
import org.wso2.siddhi.core.util.parser.helper.AggregationDefinitionParserHelper;
import org.wso2.siddhi.core.util.parser.helper.QueryParserHelper;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.core.window.Window;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

/*
 * This is the parer class of incremental aggregation definition.
 */
public class AggregationParser {

    public static AggregationRuntime parse(AggregationDefinition aggregationDefinition,
                                           SiddhiAppContext siddhiAppContext,
                                           Map<String, AbstractDefinition> streamDefinitionMap,
                                           Map<String, AbstractDefinition> tableDefinitionMap,
                                           Map<String, AbstractDefinition> windowDefinitionMap,
                                           Map<String, Table> tableMap,
                                           Map<String, Window> windowMap,
                                           LockSynchronizer lockSynchronizer) {

        if (aggregationDefinition == null) {
            throw new SiddhiAppCreationException(
                    "AggregationDefinition instance is null. " +
                            "Hence, can't create the siddhi app '" + siddhiAppContext.getName() + "'");
        }
        if (aggregationDefinition.getTimePeriod() == null) {
            throw new SiddhiAppCreationException(
                    "AggregationDefinition '" + aggregationDefinition.getId() + "'s timePeriod is null. " +
                            "Hence, can't create the siddhi app '" + siddhiAppContext.getName() + "'");
        }
        if (aggregationDefinition.getSelector() == null) {
            throw new SiddhiAppCreationException(
                    "AggregationDefinition '" + aggregationDefinition.getId() + "'s selection is not defined. " +
                            "Hence, can't create the siddhi app '" + siddhiAppContext.getName() + "'");
        }

        List<VariableExpressionExecutor> processVariableExpressionExecutors = new ArrayList<>();
        LatencyTracker latencyTracker = null;
        LockWrapper lockWrapper = null;
        boolean groupBy = false;
        AggregationRuntime aggregationRuntime;
        try {

            String aggregatorName = aggregationDefinition.getId();
            latencyTracker = QueryParserHelper.getLatencyTracker(siddhiAppContext, aggregatorName,
                    SiddhiConstants.METRIC_INFIX_AGGRIGATIONS);

            BasicSingleInputStream basicSingleInputStream = aggregationDefinition.getBasicSingleInputStream();
            // TODO: 7/7/17 check usage ???
            StreamRuntime streamRuntime = InputStreamParser.parse(basicSingleInputStream, siddhiAppContext,
                    streamDefinitionMap, tableDefinitionMap, windowDefinitionMap, tableMap, windowMap,
                    processVariableExpressionExecutors, latencyTracker, false, aggregatorName);

            List<TimePeriod.Duration> incrementalDurations = getSortedPeriods(aggregationDefinition.getTimePeriod());
            groupBy = aggregationDefinition.getSelector().getGroupByList().size() != 0;

            // Get original meta for later use.
            MetaStreamEvent incomingMetaStreamEvent = (MetaStreamEvent) streamRuntime.getMetaComplexEvent();
            AbstractDefinition incomingStreamDefinition = incomingMetaStreamEvent.getLastInputDefinition();

            // Create new meta stream event.
            // This must hold the timestamp, group by attributes (if given) and the incremental attributes, in
            // onAfterWindowData array
            // Example format: _TIMESTAMP, groupByAttribute1, groupByAttribute2, _incAttribute1, _incAttribute2
            // _incAttribute1, _incAttribute2 would have the same attribute names as in finalListOfIncrementalAttributes
            MetaStreamEvent processMetaStreamEvent = new MetaStreamEvent();
            processMetaStreamEvent.initializeAfterWindowData(); // To enter data as onAfterWindowData

            // To assign values to new meta, a list of expression executors using the original meta must be created
            // Array elements must appear in the order of new meta elements (since assignment is done based on position
            // at the receiver level).
            List<ExpressionExecutor> processExpressionExecutors = new ArrayList<>();

            // New stream definition corresponding to new meta.
            StreamDefinition processStreamDefinition = StreamDefinition.id("");

            processMetaStreamEvent.addData(new Attribute("_TIMESTAMP", Attribute.Type.LONG));
            processStreamDefinition.attribute("_TIMESTAMP", Attribute.Type.LONG);

            // Retrieve the external timestamp. If not given, this would return null.
            Expression timestampExpression = aggregationDefinition.getAggregateAttribute();
            if (timestampExpression == null) {
                timestampExpression = AttributeFunction.function("currentTimeMillis");
            }
            ExpressionExecutor timestampExecutor = ExpressionParser.parseExpression(timestampExpression,
                    incomingMetaStreamEvent, 0, tableMap, processVariableExpressionExecutors,
                    siddhiAppContext, groupBy, 0, aggregatorName);
            if (timestampExecutor.getReturnType() != Attribute.Type.LONG) {
                throw new SiddhiAppCreationException(
                        "AggregationDefinition '" + aggregationDefinition.getId() + "'s aggregateAttribute does not" +
                                " return long, but returns " + timestampExecutor.getReturnType() + ". " +
                                "Hence, can't create the siddhi app '" + siddhiAppContext.getName() + "'");
            }
            processExpressionExecutors.add(timestampExecutor);
            if (groupBy) {
                for (Variable groupByVariable : aggregationDefinition.getSelector().getGroupByList()) {
                    processMetaStreamEvent.addData(incomingStreamDefinition.getAttributeList()
                            .get(incomingStreamDefinition.getAttributePosition(groupByVariable.getAttributeName())));
                    processStreamDefinition.attribute(groupByVariable.getAttributeName(),
                            incomingStreamDefinition.getAttributeType(groupByVariable.getAttributeName()));
                    processExpressionExecutors.add(ExpressionParser.parseExpression(groupByVariable,
                            incomingMetaStreamEvent, 0, tableMap, processVariableExpressionExecutors,
                            siddhiAppContext, true, 0, aggregatorName));
                }
            }

            // Store generic expressions, which are not group by, timeStamp or incremental (for later use)
            List<Expression> genericExpressions = new ArrayList<>();

            // Retrieve attribute functions (e.g. avg, sum, etc.) and corresponding attributes.
            // Each aggregator must take only one attribute as input, at a time.
            List<OutputAttribute> outputAttributes = aggregationDefinition.getSelector().getSelectionList();
            // Store the composite aggregators
            List<CompositeAggregator> compositeAggregators = new ArrayList<>();
            AttributeFunction attributeFunction;
            String attributeName;
            for (OutputAttribute outputAttribute : outputAttributes) {
                Expression expression = outputAttribute.getExpression();
                if (expression instanceof AttributeFunction) {
                    attributeFunction = (AttributeFunction) expression;
                    if (attributeFunction.getParameters() == null || attributeFunction.getParameters()[0] == null) {
                        throw new SiddhiAppCreationException("Attribute function " + attributeFunction.getName()
                                + " cannot be executed when no parameters are given");
                    }
                    if (attributeFunction.getParameters().length != 1) {
                        throw new SiddhiAppCreationException("Aggregation requires only on one parameter. "
                                + "Found " + attributeFunction.getParameters().length);
                    }
                    if (!(attributeFunction.getParameters()[0] instanceof Variable)) {
                        throw new SiddhiAppCreationException("Expected a variable. However a parameter of type "
                                + attributeFunction.getParameters()[0].getClass().getTypeName() + " was found");
                    }
                    attributeName = ((Variable) attributeFunction.getParameters()[0]).getAttributeName();

                    CompositeAggregator compositeAggregator = (CompositeAggregator) SiddhiClassLoader
                            .loadExtensionImplementation(
                                    new AttributeFunction("incrementalAggregator", attributeFunction.getName(),
                                            attributeFunction.getParameters()),
                                    // TODO: 6/20/17 is it ok to create new AttributeFunction?
                                    CompositeAggregatorExtensionHolder.getInstance(siddhiAppContext));
                    compositeAggregator.init(attributeName, incomingStreamDefinition.getAttributeType(attributeName));
                    compositeAggregators.add(compositeAggregator);
                } else {
                    if (groupByVariables != null && expression instanceof Variable) {
                        if (!groupByVariables.contains(expression)) {
                            processedMetaStreamEvent.addData(incomingStreamDefinition.getAttributeList().get(incomingStreamDefinition
                                    .getAttributePosition(((Variable) expression).getAttributeName())));
                            newStreamDefinition.attribute(((Variable) expression).getAttributeName(),
                                    incomingStreamDefinition
                                            .getAttributeType(((Variable) expression).getAttributeName()));
                            metaValueRetrievers.add(ExpressionParser.parseExpression(expression, incomingMetaStreamEvent, 0,
                                    tableMap, processVariableExpressionExecutors, siddhiAppContext, true, 0,
                                    aggregatorName));
                            genericExpressions.add(expression);
                        }
                    } else {
                        ExpressionExecutor genericExpressionExecutor = ExpressionParser.parseExpression(expression,
                                incomingMetaStreamEvent, 0, tableMap, processVariableExpressionExecutors, siddhiAppContext,
                                true, 0, aggregatorName);
                        // For generic expression executors, the rename value is used (since if
                        // the expression is constant expression, etc. it would not have a corresponding
                        // attribute in the input stream)
                        processedMetaStreamEvent.addData(
                                new Attribute(outputAttribute.getRename(), genericExpressionExecutor.getReturnType()));
                        newStreamDefinition.attribute(outputAttribute.getRename(),
                                genericExpressionExecutor.getReturnType());
                        metaValueRetrievers.add(genericExpressionExecutor);
                        genericExpressions.add(new Variable(outputAttribute.getRename()));
                    }
                }
            }

            // Map each incremental attribute to initial value for later use.
            List<Expression> attributeInitialValues = new ArrayList<>();
            Map<Attribute, Expression> attributeInitialValueCheckerMap = new HashMap<>();
            // Map each incremental attribute name to a base incremental aggregator.
            List<Expression> baseIncrementalAggregators = new ArrayList<>();
            Map<String, Expression> baseIncrementalAggregatorsCheckerMap = new HashMap<>();
            // Final list of incremental attributes (without duplicates) which are used for creating new meta.
            List<Attribute> finalListOfIncrementalAttributes = new ArrayList<>();
            Attribute[] incrementalAttributes;
            Expression[] initialValues;
            Expression[] baseAggregators;
            for (CompositeAggregator compositeAggregator : compositeAggregators) {
                incrementalAttributes = compositeAggregator.getIncrementalAttributes();
                initialValues = compositeAggregator.getIncrementalAttributeInitialValues();
                baseAggregators = compositeAggregator.getIncrementalAggregators();
                for (int i = 0; i < incrementalAttributes.length; i++) {
                    if (attributeInitialValueCheckerMap.containsKey(incrementalAttributes[i])
                            && baseIncrementalAggregatorsCheckerMap.containsKey(incrementalAttributes[i].getName())) {
                        if (!attributeInitialValueCheckerMap.get(incrementalAttributes[i]).equals(initialValues[i])) {
                            // TODO: 6/10/17 This is an error in implementation logic. What needs to be done?
                            throw new SiddhiAppCreationException("For a given incrementalAttribute, "
                                    + "same initial value should have been defined across all "
                                    + "composite aggregators.");
                        }
                        if (!baseIncrementalAggregatorsCheckerMap.get(incrementalAttributes[i].getName())
                                .equals(baseAggregators[i])) {
                            // TODO: 6/10/17 This is an error in implementation logic. What needs to be done?
                            throw new SiddhiAppCreationException("For a given incrementalAttribute, "
                                    + "same base incremental aggregator should have been"
                                    + " defined across all composite aggregators.");
                        }
                    } else {
                        attributeInitialValues.add(initialValues[i]);
                        baseIncrementalAggregators.add(baseAggregators[i]);
                        finalListOfIncrementalAttributes.add(incrementalAttributes[i]);

                        attributeInitialValueCheckerMap.put(incrementalAttributes[i], initialValues[i]);
                        baseIncrementalAggregatorsCheckerMap.put(incrementalAttributes[i].getName(),
                                baseAggregators[i]);
                    }
                }
            }

            for (Attribute incrementalAttribute : finalListOfIncrementalAttributes) {
                processedMetaStreamEvent.addData(incrementalAttribute);
                newStreamDefinition.attribute(incrementalAttribute.getName(), incrementalAttribute.getType());
            }
            for (Expression initialValueExpression : attributeInitialValues) {
                metaValueRetrievers.add(ExpressionParser.parseExpression(initialValueExpression, incomingMetaStreamEvent, 0,
                        tableMap, processVariableExpressionExecutors, siddhiAppContext, groupBy, 0, aggregatorName));
            }

            processedMetaStreamEvent.addInputDefinition(newStreamDefinition);

            // Executors of new meta
            List<VariableExpressionExecutor> executorsOfNewMeta = new ArrayList<>();

            // Timestamp executor created for newMeta. The timeStamp would be retrieved using this
            // executor in runtime.
            VariableExpressionExecutor timeStampExecutor = (VariableExpressionExecutor) ExpressionParser
                    .parseExpression(new Variable("_TIMESTAMP"), processedMetaStreamEvent, 0, tableMap, executorsOfNewMeta,
                            siddhiAppContext, groupBy, 0, aggregatorName);

            // Create group by key generator
            GroupByKeyGenerator groupByKeyGenerator = null;
            if (groupBy) {
                groupByKeyGenerator = new GroupByKeyGenerator(groupByVariables, processedMetaStreamEvent, tableMap, executorsOfNewMeta,
                        siddhiAppContext, aggregatorName);
            }

            // Create expression executors for generic expressions, which are not group by, timeStamp or
            // incremental
            List<ExpressionExecutor> genericExpressionExecutors = new ArrayList<>();
            for (Expression genericExpression : genericExpressions) {
                genericExpressionExecutors.add(ExpressionParser.parseExpression(genericExpression, processedMetaStreamEvent, 0, tableMap,
                        executorsOfNewMeta, siddhiAppContext, groupBy, 0, aggregatorName));
            }

            // Create stream event pool
            StreamEventPool streamEventPool = new StreamEventPool(processedMetaStreamEvent, 10);

            // Default buffer size // TODO: 6/11/17 this must later be defined by user?
            int bufferSize = 3;

            // Create in-memory default table definitions and add to tableMap // TODO: 6/11/17 must later be taken from
            // @store. Optional?
            initDefaultTables(tableMap, aggregatorName, incrementalDurations, processedMetaStreamEvent, siddhiAppContext);

            // Minimum scheduling time needs to be identified (duration of root incremental executor)
            TimePeriod.Duration minSchedulingTime = null;

            // Create incremental executors
            IncrementalExecutor child = null;
            IncrementalExecutor root;
            for (int i = incrementalDurations.size() - 1; i >= 0; i--) {
                // Base incremental expression executors created using new meta
                List<ExpressionExecutorDetails> baseExpressionExecutors = new ArrayList<>();
                ExpressionExecutor expressionExecutor;
                for (int j = 0; j < baseIncrementalAggregators.size(); j++) {
                    expressionExecutor = ExpressionParser.parseExpression(baseIncrementalAggregators.get(j), processedMetaStreamEvent, 0,
                            tableMap, executorsOfNewMeta, siddhiAppContext, groupBy, 0, aggregatorName);
                    baseExpressionExecutors.add(new ExpressionExecutorDetails(expressionExecutor,
                            finalListOfIncrementalAttributes.get(j).getName()));
                }
                root = new IncrementalExecutor(incrementalDurations.get(i), child, processedMetaStreamEvent, tableMap,
                        siddhiAppContext, aggregatorName, compositeAggregators, baseExpressionExecutors,
                        groupByVariables, timeStampExecutor, groupByKeyGenerator, genericExpressionExecutors,
                        bufferSize, streamEventPool);
                child = root;
                minSchedulingTime = incrementalDurations.get(i);

            }

            // Create new scheduler
            EntryValveExecutor entryValveExecutor = new EntryValveExecutor(siddhiAppContext);
            Scheduler scheduler = SchedulerParser.parse(siddhiAppContext.getScheduledExecutorService(),
                    entryValveExecutor, siddhiAppContext);
            lockWrapper = new LockWrapper(aggregatorName);
            lockWrapper.setLock(new ReentrantLock());
            scheduler.init(lockWrapper, aggregatorName);
            scheduler.setStreamEventPool(streamEventPool);

            // Connect entry valve to root incremental executor
            entryValveExecutor.setNextExecutor(child);

            // Create new execute stream receiver
            SingleInputStream singleInputStream = (SingleInputStream) inputStream;
            IncrementalExecuteStreamReceiver incrementalExecuteStreamReceiver = new IncrementalExecuteStreamReceiver(
                    singleInputStream.getStreamId(), latencyTracker, aggregatorName);

            aggregationRuntime = new AggregationRuntime(aggregationDefinition, siddhiAppContext, streamRuntime, processedMetaStreamEvent,
                    incrementalExecuteStreamReceiver);
            assert child != null; // Child won't be null if incremental durations are given
            child.setRoot(); // Set root incremental executor
            if (timestampVariable != null) {
                child.setIsExternalTimeStampBased();
            }
            child.setScheduler(scheduler); // Set scheduler in root incremental executor
            aggregationRuntime.setExecutor(entryValveExecutor);

            // This is required when processing out of order data in the root incremental executor
            Queue<List<ExpressionExecutorDetails>> poolOfExtraBaseExpressionExecutors = new ArrayDeque<>();
            List<ExpressionExecutorDetails> extraBaseExpressionExecutors = new ArrayList<>();
            ExpressionExecutor expressionExecutor;
            for (int i = 0; i < bufferSize - 1; i++) {
                for (int j = 0; j < baseIncrementalAggregators.size(); j++) {
                    expressionExecutor = ExpressionParser.parseExpression(baseIncrementalAggregators.get(j), processedMetaStreamEvent, 0,
                            tableMap, executorsOfNewMeta, siddhiAppContext, groupBy, 0, aggregatorName);
                    extraBaseExpressionExecutors.add(new ExpressionExecutorDetails(expressionExecutor,
                            finalListOfIncrementalAttributes.get(j).getName()));
                }
                poolOfExtraBaseExpressionExecutors.add(extraBaseExpressionExecutors);
            }
            child.setPoolOfExecutors(poolOfExtraBaseExpressionExecutors);

            AggregationDefinitionParserHelper.updateVariablePosition(processedMetaStreamEvent, executorsOfNewMeta);
            AggregationDefinitionParserHelper.updateVariablePosition(incomingMetaStreamEvent, processVariableExpressionExecutors);
            AggregationDefinitionParserHelper.initStreamRuntime(streamRuntime, processedMetaStreamEvent, lockWrapper, aggregatorName,
                    aggregationRuntime, metaValueRetrievers, incomingMetaStreamEvent, scheduler, minSchedulingTime);
        } catch (RuntimeException ex) {
            throw ex; // TODO: 5/12/17 should we log?
        }

        return aggregationRuntime;
    }

    private static boolean isRange(TimePeriod timePeriod) {
        if (timePeriod.getOperator() == TimePeriod.Operator.RANGE) {
            return true;
        }
        return false;
    }

    private static List<TimePeriod.Duration> getSortedPeriods(TimePeriod timePeriod) {
        List<TimePeriod.Duration> durations = timePeriod.getDurations();
        if (isRange(timePeriod)) {
            durations = fillGap(durations.get(0), durations.get(1));
        }
        return sortedDurations(durations);
    }

    private static List<TimePeriod.Duration> sortedDurations(List<TimePeriod.Duration> durations) {
        List<TimePeriod.Duration> copyDurations = new ArrayList<TimePeriod.Duration>(durations);

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
        Collections.sort(copyDurations, periodComparator);
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

    private static void initDefaultTables(Map<String, Table> tableMap, String aggregatorName,
                                          List<TimePeriod.Duration> durations, MetaStreamEvent newMeta, SiddhiAppContext siddhiAppContext) {
        for (TimePeriod.Duration duration : durations) {
            TableDefinition tableDefinition = TableDefinition.id(aggregatorName + "_" + duration.toString());
            MetaStreamEvent tableMetaStreamEvent = new MetaStreamEvent();
            for (Attribute attribute : newMeta.getOnAfterWindowData()) {
                tableDefinition.attribute(attribute.getName(), attribute.getType());
                tableMetaStreamEvent.addOutputData(attribute); // A new meta needs to be created since
                // value mapping is done based on output data in in-memory tables. newMeta has no output data.
            }

            tableMetaStreamEvent.addInputDefinition(tableDefinition);

            StreamEventPool tableStreamEventPool = new StreamEventPool(tableMetaStreamEvent, 10);
            StreamEventCloner tableStreamEventCloner = new StreamEventCloner(tableMetaStreamEvent,
                    tableStreamEventPool);
            ConfigReader configReader = null;
            InMemoryTable inMemoryTable = new InMemoryTable();
            inMemoryTable.init(tableDefinition, tableStreamEventPool, tableStreamEventCloner, configReader,
                    siddhiAppContext);
            tableMap.putIfAbsent(tableDefinition.getId(), inMemoryTable);
        }
    }

    public static class ExpressionExecutorDetails {
        private ExpressionExecutor executor;
        private String executorName;

        public ExpressionExecutorDetails(ExpressionExecutor executor, String executorName) {
            this.executor = executor;
            this.executorName = executorName;
        }

        public ExpressionExecutor getExecutor() {
            return this.executor;
        }

        public String getExecutorName() {
            return this.executorName;
        }

        public ExpressionExecutorDetails copy() {
            return new ExpressionExecutorDetails(executor, executorName);
        }
    }
}
