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

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.populater.StreamEventPopulaterFactory;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.selector.GroupByKeyGenerator;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.AvgIncrementalAttributeAggregator;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.CompositeAggregator;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.CountIncrementalAttributeAggregator;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.ExecuteStreamReceiver;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.GroupByKeyGeneratorForIncremental;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecutor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.SumIncrementalAttributeAggregator;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.lock.LockSynchronizer;
import org.wso2.siddhi.core.util.lock.LockWrapper;
import org.wso2.siddhi.core.util.parser.helper.AggregationDefinitionParserHelper;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.core.window.Window;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.Constant;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.*;

public class AggregationParser {

    public static AggregationRuntime parse(AggregationDefinition definition, ExecutionPlanContext executionPlanContext,
            Map<String, AbstractDefinition> streamDefinitionMap, Map<String, AbstractDefinition> tableDefinitionMap,
            Map<String, AbstractDefinition> windowDefinitionMap, Map<String, Table> tableMap,
            Map<String, Window> windowMap, Map<String, List<Source>> eventSourceMap,
            Map<String, List<Sink>> eventSinkMap, LockSynchronizer lockSynchronizer) {

        // Read group by attribute
        // Check whether this is an INTERVAL or RANGE operation
        // Based on it, initialize Parent, child, child .... calculators
        // For each calculator
        // Create IncrementalCalculators
        // Calculate a set of unique base calculators
        // Initialize IncrementalStores with GroupBy attribute
        // When a new event comes
        //
        if (definition == null) {
            throw new ExecutionPlanCreationException(
                    "AggregationDefinition instance is null. " + "Hence, can't create the execution plan");
        }
        if (definition.getTimePeriod() == null) {
            throw new ExecutionPlanCreationException(
                    "AggregationDefinition's timePeriod is null. " + "Hence, can't create the execution plan");
        }
        if (definition.getSelector() == null) {
            throw new ExecutionPlanCreationException("AggregationDefinition's output attributes are not defined. "
                    + "Hence, can't create the execution plan");
        }

        List<VariableExpressionExecutor> executors = new ArrayList<>();
        LatencyTracker latencyTracker = null;
        LockWrapper lockWrapper = null;
        AggregationRuntime aggregationRuntime;
        try {

            String aggregatorName = definition.getId();

            InputStream inputStream = definition.getInputStream();
            StreamRuntime streamRuntime = InputStreamParser.parse(inputStream, executionPlanContext,
                    streamDefinitionMap, tableDefinitionMap, windowDefinitionMap, tableMap, windowMap, executors,
                    latencyTracker, false, aggregatorName);


            List<OutputAttribute> outputAttributes = definition.getSelector().getSelectionList(); // TODO: 3/15/17 null
                                                                                                  // checking ...
            // TODO: 5/25/17 above has link to original attributes (eg: price1) need to update?
            List<AttributeFunction> functionsAttributes = new ArrayList<>();
            for (int i = 0; i < outputAttributes.size(); i++) {
                Expression expression = outputAttributes.get(i).getExpression();
                if (expression instanceof AttributeFunction) {
                    functionsAttributes.add((AttributeFunction) expression);
                }
            }

            List<TimePeriod.Duration> incrementalDurations = getSortedPeriods(definition.getTimePeriod());
            Variable groupByVariable = getGroupByAttribute(definition.getSelector());
            Variable timeStampVariable = definition.getAggregateAttribute();

            // New stream definition created to match new meta
            StreamDefinition streamDefinition = new StreamDefinition();
            streamDefinition
                    .setId(((MetaStreamEvent) streamRuntime.getMetaComplexEvent()).getLastInputDefinition().getId());


            //List to hold new function attributes which reflect attribute name changes (e.g. from price1 to sumprice1)
            List<AttributeFunction> newFunctionsAttributes = new ArrayList<>();

            // List of original attributes
            List<Attribute> currentAttributes = streamDefinitionMap.get(((SingleInputStream) inputStream).getStreamId())
                    .getAttributeList();
            // List to hold original attribute, new attribute and position/expression variable.
            List<Object[]> baseMappingHolder = new ArrayList<>(); // TODO: 6/7/17 don't send to createMetaAttributes
            // Make list of new attributes
            List<Attribute> newMetaAttributes = createMetaAttributes(functionsAttributes, currentAttributes,
                    groupByVariable, newFunctionsAttributes, definition.getAggregateAttribute(), baseMappingHolder);

            /*** Following is related to new MetaStreamEvent creation ***/

            List<Attribute> attributeList = new ArrayList<>();

            // Create new metaStreamEvent corresponding to groupBy attribute and base aggregators
            MetaStreamEvent metaStreamEvent = new MetaStreamEvent();

            metaStreamEvent.initializeAfterWindowData();
            for (Attribute newMetaAttribute : newMetaAttributes) {
                metaStreamEvent.addData(newMetaAttribute);
                attributeList.add(newMetaAttribute); // TODO: 5/24/17 Is this correct?
                streamDefinition.attribute(newMetaAttribute.getName(), newMetaAttribute.getType());
                executors.add(new VariableExpressionExecutor(newMetaAttribute, -1, 0)); // TODO: 5/24/17 this adds new
                                                                                        // variable expression executor
                                                                                        // for groupBy too. but it's
                                                                                        // already there. Is it ok?
            }

            metaStreamEvent.addInputDefinition(streamDefinition);

            // TODO: 5/23/17 populate here?
//            StreamEventPopulaterFactory.constructEventPopulator(metaStreamEvent, 0, attributeList);
            /*******************************************/

            List<CompositeAggregator> compositeAggregators = createIncrementalAggregators(newFunctionsAttributes);
            Set<BaseExpressionDetails> baseAggregators = getBaseAggregators(compositeAggregators);

            Map<Attribute, Integer> mappingPositions = setMappingPositionsForBaseValues(metaStreamEvent, currentAttributes, baseAggregators);
            ExpressionExecutor groupByExecutor = generateGroupByExecutor(groupByVariable, metaStreamEvent, 0, tableMap,
                    executors, executionPlanContext, 0, aggregatorName);
            ExpressionExecutor externalTimeStampExecutor = null;
            if (timeStampVariable!=null){
                externalTimeStampExecutor = generateTimeStampExecutor(timeStampVariable, metaStreamEvent, 0, tableMap,
                        executors, executionPlanContext, 0, aggregatorName);
            }
            List<Variable> groupByList = new ArrayList<>();
            groupByList.add(groupByVariable); // TODO: 5/30/17 we must later get a list from parser itself
            GroupByKeyGeneratorForIncremental groupByKeyGenerator =
                    new GroupByKeyGeneratorForIncremental(groupByList, metaStreamEvent, tableMap,
                            executors, executionPlanContext, aggregatorName);


            IncrementalExecutor child = build(incrementalDurations.get(incrementalDurations.size() - 1), null, metaStreamEvent, tableMap,
                    executionPlanContext, aggregatorName, compositeAggregators,
                    groupByExecutor, externalTimeStampExecutor, groupByKeyGenerator, executors, baseAggregators);
            IncrementalExecutor root;
            for (int i = incrementalDurations.size() - 2; i >= 0; i--) {
                root = build(incrementalDurations.get(i), child, metaStreamEvent, tableMap,
                        executionPlanContext, aggregatorName, compositeAggregators, groupByExecutor,
                        externalTimeStampExecutor, groupByKeyGenerator, executors, baseAggregators);
                child = root;
            }

            SingleInputStream singleInputStream = (SingleInputStream) inputStream;
            ExecuteStreamReceiver executeStreamReceiver = new ExecuteStreamReceiver(singleInputStream.getStreamId(),
                    latencyTracker, aggregatorName);
            executeStreamReceiver.setMappingPositions(mappingPositions);

            aggregationRuntime = new AggregationRuntime(definition, executionPlanContext, streamRuntime,
                    metaStreamEvent, executeStreamReceiver);
            aggregationRuntime.setIncrementalExecutor(child);

            AggregationDefinitionParserHelper.reduceMetaComplexEvent(metaStreamEvent);
            AggregationDefinitionParserHelper.updateVariablePosition(metaStreamEvent, executors); // TODO: 5/22/17
                                                                                                  // change this logic
            AggregationDefinitionParserHelper.initStreamRuntime(streamRuntime, metaStreamEvent, lockWrapper,
                    aggregatorName, aggregationRuntime);
        } catch (RuntimeException ex) {
            throw ex; // TODO: 5/12/17 should we log?
        }

        return aggregationRuntime;
    }

    private static IncrementalExecutor build(TimePeriod.Duration duration,
            IncrementalExecutor child, MetaComplexEvent metaEvent, Map<String, Table> tableMap,
            ExecutionPlanContext executionPlanContext, String aggregatorName, List<CompositeAggregator>compositeAggregators,
            ExpressionExecutor groupByExecutor, ExpressionExecutor externalTimeStampExecutor,
            GroupByKeyGeneratorForIncremental groupByKeyGenerator, List<VariableExpressionExecutor> executors,
            Set<BaseExpressionDetails> baseAggregators) {

        //Each IncrementalExecutor needs its own basicExecutorDetails (since the aggregate must be independently
        // manipulated based on duration). Hence create basicExecutorDetails here.
        List<ExpressionExecutorDetails> basicExecutorDetails = basicFunctionExecutors(baseAggregators, metaEvent, 0, tableMap, executors,
                executionPlanContext, true, 0, aggregatorName);

        switch (duration) {
        case SECONDS:
            return new IncrementalExecutor(TimePeriod.Duration.SECONDS, child, metaEvent, tableMap,
                    executionPlanContext, aggregatorName, compositeAggregators, basicExecutorDetails,
                    groupByExecutor, externalTimeStampExecutor, groupByKeyGenerator);
        case MINUTES:
            return new IncrementalExecutor(TimePeriod.Duration.MINUTES, child, metaEvent, tableMap,
                    executionPlanContext, aggregatorName, compositeAggregators, basicExecutorDetails,
                    groupByExecutor, externalTimeStampExecutor, groupByKeyGenerator);
        case HOURS:
            return new IncrementalExecutor(TimePeriod.Duration.HOURS, child, metaEvent, tableMap,
                    executionPlanContext, aggregatorName, compositeAggregators, basicExecutorDetails,
                    groupByExecutor, externalTimeStampExecutor, groupByKeyGenerator);
        case DAYS:
            return new IncrementalExecutor(TimePeriod.Duration.DAYS, child, metaEvent, tableMap,
                    executionPlanContext, aggregatorName, compositeAggregators, basicExecutorDetails,
                    groupByExecutor, externalTimeStampExecutor, groupByKeyGenerator);
        case WEEKS:
            return new IncrementalExecutor(TimePeriod.Duration.WEEKS, child, metaEvent, tableMap,
                    executionPlanContext, aggregatorName, compositeAggregators, basicExecutorDetails,
                    groupByExecutor, externalTimeStampExecutor, groupByKeyGenerator);
        case MONTHS:
            return new IncrementalExecutor(TimePeriod.Duration.MONTHS, child, metaEvent, tableMap,
                    executionPlanContext, aggregatorName, compositeAggregators, basicExecutorDetails,
                    groupByExecutor, externalTimeStampExecutor, groupByKeyGenerator);
        case YEARS:
            return new IncrementalExecutor(TimePeriod.Duration.YEARS, child, metaEvent, tableMap,
                    executionPlanContext, aggregatorName, compositeAggregators, basicExecutorDetails,
                    groupByExecutor, externalTimeStampExecutor, groupByKeyGenerator);
        default:
            throw new EnumConstantNotPresentException(TimePeriod.Duration.class,
                    "Aggregation is not defined for time period " + duration);
        }
    }

    private static Variable getGroupByAttribute(Selector selector) {
        // TODO: 5/12/17 null check done in line 62
        List<Variable> groupByAttributes = selector.getGroupByList();
        // TODO: 3/10/17 : Can we groupBy two or more attributes?

        // TODO: 3/10/17 : if groupByAttributes is empty throw an exception
        if (groupByAttributes.size() == 0) {
            throw new ExecutionPlanCreationException("Group by attribute must be provided to perform aggregation");
        }
        return groupByAttributes.get(0);
    }

    private static boolean isRange(TimePeriod timePeriod) {
        // TODO: 5/12/17 null check for timePeriod already done (line 58)
        if (timePeriod.getOperator() == TimePeriod.Operator.RANGE) {
            return true;
        }
        return false;
    }

    private static List<TimePeriod.Duration> getSortedPeriods(TimePeriod timePeriod) {
        // TODO: 5/12/17 null check for timePeriod already done (line 58)
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
            throw new ExecutionPlanRuntimeException(
                    "Start time period must be less than end time period for range aggregation calculation");
        }

        if (startIndex == endIndex) {

            filledDurations.add(start);

        } else {
            TimePeriod.Duration[] temp = new TimePeriod.Duration[endIndex - startIndex + 1];
            System.arraycopy(durations, startIndex, temp, 0, endIndex - startIndex + 1);
            filledDurations = Arrays.asList(temp);
            /*
             * for (int i = startIndex; i <= endIndex; i++) { // TODO: 3/10/17 : Array Copy ?
             * filledDurations.add(durations[i]);
             * }
             */
        }
        return filledDurations;
    }

    private static List<Attribute> createMetaAttributes(List<AttributeFunction> functionsAttributes,
            List<Attribute> currentAttributes, Variable groupByVariable,
            List<AttributeFunction> newFunctionsAttributes, Variable externalTimestamp,
            List<Object[]> baseMappingHolder) {

        List<Attribute> newMetaAttributes = new ArrayList<>();
        String attributeName;

        // Create map of current attribute names and types
        Map currentAttributeNameType = new HashMap();
        for (Attribute currentAttribute : currentAttributes) {
            currentAttributeNameType.put(currentAttribute.getName(), currentAttribute.getType());
        }

        // Add group by attribute to newMetaAttributes
        String groupByAttributeName = groupByVariable.getAttributeName();
        newMetaAttributes.add(new Attribute(groupByAttributeName,
                (Attribute.Type) currentAttributeNameType.get(groupByAttributeName)));


        // Create and add attributes related to base aggregate functions
        for (AttributeFunction attributeFunction : functionsAttributes) {
            if (attributeFunction.getParameters() == null) {
                throw new ExecutionPlanValidationException("Parameters cannot be null for an attribute function");
            }
            if (attributeFunction.getParameters().length != 1) {
                throw new ExecutionPlanValidationException("Only one parameter allowed for attribute function");
            }
            if (attributeFunction.getParameters()[0] == null
                    || !(attributeFunction.getParameters()[0] instanceof Variable)) {
                throw new ExecutionPlanValidationException("Attribute function can only be executed on a variable");
            }
            attributeName = ((Variable) attributeFunction.getParameters()[0]).getAttributeName();

            //In base mapper..
            //1. Position 1 holds original attribute
            //2. Position 2 holds new attribute
            //3. Position 3 is reserved to hold
            //      corresponding position from input stream if the value is a variable in composite aggregator, or
            //      Expression value as specified in composite aggregator
            Object[] baseMapper;
            Attribute newAttribute;
            switch (attributeFunction.getName()) {
            case "avg":
                //Update sum information
                newAttribute = new Attribute("__SUM__".concat(attributeName),
                        Attribute.Type.DOUBLE);
                if (!newMetaAttributes.contains(newAttribute)) {
                    newMetaAttributes.add(newAttribute);
                }
                /*baseMapper = new Object[3];
                baseMapper[0] = new Attribute(attributeName,
                        (Attribute.Type) currentAttributeNameType.get(attributeName));
                baseMapper[1] = newAttribute;
                if (AvgIncrementalAttributeAggregator.getInternalExpression("sum") instanceof Variable) {
                    baseMapper[2] = currentAttributes.indexOf(baseMapper[0]);
                } else { //instance is a constant. // TODO: 6/7/17 should be check with if condition?
                    baseMapper[2] = AvgIncrementalAttributeAggregator.getInternalExpression("sum");
                }
                if (!baseMappingHolder.contains(baseMapper)) {
                    baseMappingHolder.add(baseMapper);
                }*/
                //Update count information
                newAttribute = new Attribute("__COUNT__".concat(attributeName),
                        Attribute.Type.DOUBLE); //Count is also Double since internally we add up the count
                if (!newMetaAttributes.contains(newAttribute)) {
                    newMetaAttributes.add(newAttribute);
                }
                /*baseMapper = new Object[3];
                baseMapper[0] = new Attribute(attributeName,
                        (Attribute.Type) currentAttributeNameType.get(attributeName));
                baseMapper[1] = newAttribute;
                if (AvgIncrementalAttributeAggregator.getInternalExpression("count") instanceof Variable) {
                    baseMapper[2] = currentAttributes.indexOf(baseMapper[0]);
                } else { //instance is a constant.
                    baseMapper[2] = AvgIncrementalAttributeAggregator.getInternalExpression("count");
                }
                if (!baseMappingHolder.contains(baseMapper)) {
                    baseMappingHolder.add(baseMapper);
                }*/

                newFunctionsAttributes.add(new AttributeFunction(attributeFunction.getNamespace(),
                        attributeFunction.getName(), new Variable(attributeName))); //Since meta doesnt have price1, we need to get sumprice1, countprice1
                break;
            case "sum":
                newAttribute = new Attribute("__SUM__".concat(attributeName),
                        Attribute.Type.DOUBLE);
                if (!newMetaAttributes.contains(newAttribute)) {
                    newMetaAttributes.add(newAttribute);
                }
                /*baseMapper = new Object[3];
                baseMapper[0] = new Attribute(attributeName,
                        (Attribute.Type) currentAttributeNameType.get(attributeName));
                baseMapper[1] = newAttribute;
                if (SumIncrementalAttributeAggregator.getInternalExpression("sum") instanceof Variable) {
                    baseMapper[2] = currentAttributes.indexOf(baseMapper[0]);
                } else { //instance is a constant.
                    baseMapper[2] = SumIncrementalAttributeAggregator.getInternalExpression("sum");
                }
                if (!baseMappingHolder.contains(baseMapper)) {
                    baseMappingHolder.add(baseMapper);
                }*/

                newFunctionsAttributes.add(new AttributeFunction(attributeFunction.getNamespace(),
                        attributeFunction.getName(), new Variable("__SUM__".concat(attributeName))));
                break;
            case "count":
                newAttribute = new Attribute("__COUNT__".concat(attributeName),
                        Attribute.Type.DOUBLE);
                if (!newMetaAttributes.contains(newAttribute)) {
                    newMetaAttributes.add(newAttribute);
                }
                /*baseMapper = new Object[3];
                baseMapper[0] = new Attribute(attributeName,
                        (Attribute.Type) currentAttributeNameType.get(attributeName));
                baseMapper[1] = newAttribute;
                if (CountIncrementalAttributeAggregator.getInternalExpression("count") instanceof Variable) {
                    baseMapper[2] = currentAttributes.indexOf(baseMapper[0]);
                } else { //instance is a constant.
                    baseMapper[2] = CountIncrementalAttributeAggregator.getInternalExpression("count");
                }
                if (!baseMappingHolder.contains(baseMapper)) {
                    baseMappingHolder.add(baseMapper);
                }*/

                newFunctionsAttributes.add(new AttributeFunction(attributeFunction.getNamespace(),
                        attributeFunction.getName(), new Variable("__COUNT__".concat(attributeName))));
                break;
            // TODO: 5/24/17 add other aggregates
            default:
                throw new ExecutionPlanValidationException("Unknown attribute function");
            }
        }

        // If an external timestamp is given, add that to newMetaAttributes
        if (externalTimestamp!=null) {
            String externalTimestampName = externalTimestamp.getAttributeName();
            if (currentAttributeNameType.get(externalTimestampName)
                    != Attribute.Type.LONG) {
                throw new ExecutionPlanValidationException("External timestamp must be of type Long");
            }
            newMetaAttributes.add(new Attribute(externalTimestampName, Attribute.Type.LONG));
        }

        return newMetaAttributes;
    }

    private static List<CompositeAggregator> createIncrementalAggregators(List<AttributeFunction> functionAttributes) {
        List<CompositeAggregator> compositeAggregators = new ArrayList<>();
        for (AttributeFunction function : functionAttributes) {
            if (function.getName().equals("avg")) {
                AvgIncrementalAttributeAggregator average = new AvgIncrementalAttributeAggregator(function);
                compositeAggregators.add(average);
            }else if (function.getName().equals("sum")) {
                SumIncrementalAttributeAggregator sum = new SumIncrementalAttributeAggregator(function);
                compositeAggregators.add(sum);
            } else {
                // TODO: 3/10/17 add other Exceptions
            }
        }
        return compositeAggregators;
    }

    /**
     *
     * @param compositeAggregators
     * @return
     */
    private static Set<BaseExpressionDetails> getBaseAggregators(List<CompositeAggregator> compositeAggregators) {
        Set<BaseExpressionDetails> baseAggregators = new HashSet<>();// TODO: 6/2/17 will have to change logic due to composite change
        for(CompositeAggregator compositeAggregator : compositeAggregators){
            Expression[] bases = compositeAggregator.getBaseAggregators();
            for(Expression expression : bases){
                BaseExpressionDetails baseExpressionDetails = new BaseExpressionDetails(expression, compositeAggregator.getAttributeName());
                baseAggregators.add(baseExpressionDetails);
            }
        }
        return baseAggregators;
    }

    /***
     *
     * @param metaEvent
     * @param currentState
     * @param tableMap
     * @param executorList
     * @param executionPlanContext
     * @param groupBy
     * @param defaultStreamEventIndex
     * @param aggregatorName
     * @return
     */
    private static List<AggregationParser.ExpressionExecutorDetails> basicFunctionExecutors(Set<BaseExpressionDetails> baseAggregators, MetaComplexEvent metaEvent,
                                                                                      int currentState, Map<String, Table> tableMap,
                                                                                      List<VariableExpressionExecutor> executorList,
                                                                                      ExecutionPlanContext executionPlanContext, boolean groupBy,
                                                                                      int defaultStreamEventIndex, String aggregatorName) {


        List<ExpressionExecutorDetails> baseFunctionExecutors = new ArrayList<>();
        for (BaseExpressionDetails baseAggregator : baseAggregators) {

            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(baseAggregator.getBaseExpression(), metaEvent,
                    currentState, tableMap, executorList, executionPlanContext, groupBy,
                    defaultStreamEventIndex, aggregatorName);
            ExpressionExecutorDetails executorDetails = new ExpressionExecutorDetails(expressionExecutor, baseAggregator.getAttribute()); // TODO: 5/25/17 we don't have to concat name again
            baseFunctionExecutors.add(executorDetails);
        }
        return baseFunctionExecutors;
    }

    private static class BaseExpressionDetails {
        private Expression baseExpression;
        private String attribute;

        public BaseExpressionDetails(Expression baseExpression, String attribute) {
            this.baseExpression = baseExpression;
            Expression internalExpression = ((AttributeFunction) baseExpression).getParameters()[0];
            if (internalExpression instanceof Variable) {
                this.attribute = ((Variable) internalExpression).getAttributeName();
            } else {
                //A constant value has been specified as internal expression
                this.attribute = attribute.concat((internalExpression).toString());
            }
        }

        public Expression getBaseExpression() {
            return this.baseExpression;
        }

        public String getAttribute() {
            return this.attribute;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            if(this.baseExpression != null){
                hash += this.baseExpression.hashCode();
            }
            if(this.attribute != null){
                hash += this.attribute.hashCode();
            }
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BaseExpressionDetails that = (BaseExpressionDetails) o;
            if (this.baseExpression.equals(that.baseExpression) && this.attribute.equals(that.attribute)) {
                return true;
            } else {
                return false;
            }
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
    }

    /**
     * @param groupByClause
     * @param metaEvent
     * @param currentState
     * @param tableMap
     * @param executorList
     * @param executionPlanContext
     * @param defaultStreamEventIndex
     * @param queryName
     * @return
     */
    private static ExpressionExecutor generateGroupByExecutor(Variable groupByClause, MetaComplexEvent metaEvent,
                                                       int currentState, Map<String, Table> tableMap,
                                                       List<VariableExpressionExecutor> executorList,
                                                       ExecutionPlanContext executionPlanContext,
                                                       int defaultStreamEventIndex, String queryName) {

        ExpressionExecutor variableExpressionExecutor = ExpressionParser.parseExpression(groupByClause, metaEvent,
                currentState, tableMap, executorList, executionPlanContext, true,
                defaultStreamEventIndex, queryName);
        return variableExpressionExecutor;
    }

    /**
     *
     * @param timeStampVariable
     * @param metaEvent
     * @param currentState
     * @param tableMap
     * @param executorList
     * @param executionPlanContext
     * @param defaultStreamEventIndex
     * @param queryName
     * @return
     */
    private static ExpressionExecutor generateTimeStampExecutor(Variable timeStampVariable, MetaComplexEvent metaEvent,
                                                         int currentState, Map<String, Table> tableMap,
                                                         List<VariableExpressionExecutor> executorList,
                                                         ExecutionPlanContext executionPlanContext,
                                                         int defaultStreamEventIndex, String queryName) {
        ExpressionExecutor variableExpressionExecutor = ExpressionParser.parseExpression(timeStampVariable, metaEvent,
                currentState, tableMap, executorList, executionPlanContext, true,
                defaultStreamEventIndex, queryName);
        return variableExpressionExecutor;
    }

    private static Map<Attribute, Integer> setMappingPositionsForBaseValues(MetaStreamEvent metaStreamEvent, List<Attribute>currentAttributes, Set<BaseExpressionDetails>baseAggregators){
        Map<Attribute, Integer> mappingPositionOfMeta = new HashMap<>();
        String regex = "__(.*?)__";
        List<Attribute> attributeList = metaStreamEvent.getOnAfterWindowData();
        List<String> originalAttributeNames = new ArrayList<>();
        List<String> baseNames = new ArrayList<>();

        //Create list of current attributes names
        for (Attribute originalAttribute: currentAttributes) {
            originalAttributeNames.add(originalAttribute.getName());
        }

        //Store base aggregator names in a list
        for (BaseExpressionDetails baseAggregator:baseAggregators) {
            baseNames.add(baseAggregator.getAttribute());
        }

        for (Attribute attribute: attributeList) {
            //In new meta event, check for attributes which are not there in original input stream
            // (i.e. find attributes with name changes)
            if (!currentAttributes.contains(attribute)) {
                //Check whether a base aggregator with same name appears.
                //If it does, that means the value for that meta attribute must be taken from input stream
                //Otherwise, an Expression value is already defined for the base aggregation. Hence,
                // no need to map from input data.
                if (baseNames.contains(attribute.getName())) {
                    //Specify from which position in input stream, we must map data to meta
                    int position = originalAttributeNames.indexOf(attribute.getName().replaceFirst(regex, ""));
                    mappingPositionOfMeta.put(attribute, position);
                }
            }
        }
        return mappingPositionOfMeta;
    }
}
