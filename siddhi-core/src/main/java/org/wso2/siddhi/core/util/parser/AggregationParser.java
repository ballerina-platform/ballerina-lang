package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalCalculator;
import org.wso2.siddhi.core.stream.input.source.InputTransport;
import org.wso2.siddhi.core.stream.output.sink.OutputTransport;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.lock.LockSynchronizer;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.core.window.EventWindow;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.*;

public class AggregationParser {

    public static AggregationRuntime parse(AggregationDefinition definition,
                                           ExecutionPlanContext executionPlanContext,
                                           Map<String, AbstractDefinition> streamDefinitionMap,
                                           Map<String, AbstractDefinition> tableDefinitionMap,
                                           Map<String, AbstractDefinition> windowDefinitionMap,
                                           Map<String, EventTable> eventTableMap,
                                           Map<String, EventWindow> eventWindowMap,
                                           Map<String, List<InputTransport>> eventSourceMap,
                                           Map<String, List<OutputTransport>> eventSinkMap,
                                           LockSynchronizer lockSynchronizer) {

        // Read group by attribute
        // Check whether this is an INTERVAL or RANGE operation
        // Based on it, initialize Parent, child, child .... calculators
        // For each calculator
        // Create IncrementalCalculators
        // Calculate a set of unique base calculators
        // Initialize IncrementalStores with GroupBy attribute
        // When a new event comes
        //
        if(definition == null){
            // TODO: 3/10/17 Exception
        }
        if(definition.getTimePeriod() == null){
            // TODO: 3/10/17 Exception
        }

        List<OutputAttribute> outputAttributes = definition.getSelector().getSelectionList(); // TODO: 3/15/17 null checking ...
        List<AttributeFunction> functionsAttributes = new ArrayList<>();
        for(int i=1; i<outputAttributes.size(); i++){ // TODO: 3/15/17 this logic is wrong will be corrected later
            OutputAttribute tmp = outputAttributes.get(i);

            functionsAttributes.add((AttributeFunction)tmp.getExpression());
        }

        List<TimePeriod.Duration> incrementalDurations = getSortedPeriods(definition.getTimePeriod());
        // parent
        //TimePeriod.Duration parent = incrementalDurations.get(0);
        //IncrementalCalculator second = IncrementalCalculator.second(functionsAttributes);

        //for(int i=1; i<incrementalDurations.size(); i++){

        //}
        InputStream inputStream = definition.getInputStream();
        Element nameElement = null;
        LatencyTracker latencyTracker = null;
        String queryName = null;
        if (nameElement != null) {
            queryName = nameElement.getValue();
        } else {
            queryName = "query_" + UUID.randomUUID().toString();
        }

        if (executionPlanContext.isStatsEnabled() && executionPlanContext.getStatisticsManager() != null) {
            if (nameElement != null) {
                String metricName =
                        executionPlanContext.getSiddhiContext().getStatisticsConfiguration().getMatricPrefix() +
                                SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_EXECUTION_PLANS +
                                SiddhiConstants.METRIC_DELIMITER + executionPlanContext.getName() +
                                SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_SIDDHI +
                                SiddhiConstants.METRIC_DELIMITER + SiddhiConstants.METRIC_INFIX_QUERIES +
                                SiddhiConstants.METRIC_DELIMITER + queryName;
                latencyTracker = executionPlanContext.getSiddhiContext()
                        .getStatisticsConfiguration()
                        .getFactory()
                        .createLatencyTracker(metricName, executionPlanContext.getStatisticsManager());
            }
        }
        List<VariableExpressionExecutor> executors = new ArrayList<VariableExpressionExecutor>();
        MetaComplexEvent metaComplexEvent = InputStreamParser.parse(inputStream,executionPlanContext,
                streamDefinitionMap,tableDefinitionMap,windowDefinitionMap,eventTableMap,eventWindowMap,
                executors,latencyTracker, false, queryName).getMetaComplexEvent();
        //InputStreamParser.parse(definition.getInputStream())

        IncrementalCalculator child = build(functionsAttributes, incrementalDurations.get(incrementalDurations.size() - 1), null,
                metaComplexEvent, 0, eventTableMap, executors, executionPlanContext, true, 0, queryName);
        IncrementalCalculator root = child;
        for(int i= incrementalDurations.size()-2; i>=0; i--){
            root = build(functionsAttributes, incrementalDurations.get(i), child, metaComplexEvent, 0,
                    eventTableMap, executors, executionPlanContext, true, 0, queryName);
            child = root;
        }

        return null;
    }



//    private static MetaComplexEvent getMetaComplexEvent(InputStream inputStream, Exe){
//        InputStreamParser.parse(inputStream, )
//    }

    private static IncrementalCalculator build(List<AttributeFunction> functionsAttributes,
                                               TimePeriod.Duration duration, IncrementalCalculator child,MetaComplexEvent metaEvent,
                                               int currentState, Map<String, EventTable> eventTableMap,
                                               List<VariableExpressionExecutor> executorList,
                                               ExecutionPlanContext executionPlanContext, boolean groupBy,
                                               int defaultStreamEventIndex, String queryName){
        switch (duration){
            case SECONDS:
                return IncrementalCalculator.second(functionsAttributes, child, metaEvent, currentState, eventTableMap,
                        executorList, executionPlanContext, groupBy, defaultStreamEventIndex, queryName);
            case MINUTES:
                return IncrementalCalculator.minute(functionsAttributes, child, metaEvent, currentState, eventTableMap,
                        executorList, executionPlanContext, groupBy, defaultStreamEventIndex, queryName);
            default:
                // TODO: 3/15/17 Throws an exception
                return null;
        }
    }

    private static Variable getGroupByAttribute(Selector selector) {
        if (selector == null) {
            // TODO: 3/10/17 : Exception
        }
        List<Variable> groupByAttributes = selector.getGroupByList();
        // TODO: 3/10/17 : Can we groupBy two or more attributes?
        // TODO: 3/10/17 : if groupByAttributes is empty throw an exception
        return groupByAttributes.get(0);
    }

    private static boolean isRange(TimePeriod timePeriod) {
        if (timePeriod == null) {
            // TODO: 3/10/17 : Exception
        }
        if (timePeriod.getOperator() == TimePeriod.Operator.RANGE) {
            return true;
        }
        return false;
    }

    private static List<TimePeriod.Duration> getSortedPeriods(TimePeriod timePeriod) {
        if (timePeriod == null) {
            // TODO: 3/10/17 : Exception
        }
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
            // TODO: 3/10/17 Exception
        }

        if (startIndex == endIndex) {

            filledDurations.add(start);

        } else {
            for (int i = startIndex; i <= endIndex; i++) { // TODO: 3/10/17 : Array Copy ?
                filledDurations.add(durations[i]);
            }
        }
        return filledDurations;
    }
}
