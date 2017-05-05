package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecutor;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.lock.LockSynchronizer;
import org.wso2.siddhi.core.util.lock.LockWrapper;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.core.window.Window;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.*;

public class AggregationParser {

    public static AggregationRuntime parse(AggregationDefinition definition,
                                           ExecutionPlanContext executionPlanContext,
                                           Map<String, AbstractDefinition> streamDefinitionMap,
                                           Map<String, AbstractDefinition> tableDefinitionMap,
                                           Map<String, AbstractDefinition> windowDefinitionMap,
                                           Map<String, Table> tableMap,
                                           Map<String, Window> windowMap,
                                           Map<String, List<Source>> eventSourceMap,
                                           Map<String, List<Sink>> eventSinkMap,
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
        if (definition == null) {
            throw new ExecutionPlanCreationException("AggregationDefinition instance is null. " +
                    "Hence, can't create the execution plan");
        }
        if (definition.getTimePeriod() == null) {
            throw new ExecutionPlanCreationException("AggregationDefinition's timePeriod is null. " +
                    "Hence, can't create the execution plan");
        }

        List<VariableExpressionExecutor> executors = new ArrayList<VariableExpressionExecutor>();
        Element nameElement = null;
        LatencyTracker latencyTracker = null;
        LockWrapper lockWrapper = null;
        AggregationRuntime aggregationRuntime = null;
        try {
            nameElement = AnnotationHelper.getAnnotationElement("info", "name", definition.getAnnotations());
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

            InputStream inputStream = definition.getInputStream();
            StreamRuntime streamRuntime = InputStreamParser.parse(inputStream, executionPlanContext,
                    streamDefinitionMap, tableDefinitionMap, windowDefinitionMap, tableMap, windowMap,
                    executors, latencyTracker, false, queryName);
            MetaComplexEvent metaComplexEvent = streamRuntime.getMetaComplexEvent();

            List<OutputAttribute> outputAttributes = definition.getSelector().getSelectionList(); // TODO: 3/15/17 null checking ...
            List<AttributeFunction> functionsAttributes = new ArrayList<>();
            for (int i = 1; i < outputAttributes.size(); i++) { // TODO: 3/15/17 this logic is wrong will be corrected later
                OutputAttribute tmp = outputAttributes.get(i);

                functionsAttributes.add((AttributeFunction) tmp.getExpression());
            }

            List<TimePeriod.Duration> incrementalDurations = getSortedPeriods(definition.getTimePeriod());
            Variable groupByVar = getGroupByAttribute(definition.getSelector());

            IncrementalExecutor child = build(functionsAttributes, incrementalDurations.get(incrementalDurations.size() - 1), null,
                    metaComplexEvent, 0, tableMap, executors, executionPlanContext,
                    true, 0, queryName, groupByVar);
            IncrementalExecutor root = child;
            for (int i = incrementalDurations.size() - 2; i >= 0; i--) {
                root = build(functionsAttributes, incrementalDurations.get(i), child, metaComplexEvent, 0,
                        tableMap, executors, executionPlanContext, true, 0,
                        queryName, groupByVar);
                child = root;
            }

            aggregationRuntime = new AggregationRuntime(definition, executionPlanContext, streamRuntime, metaComplexEvent);


        } catch (RuntimeException ex) {

        }
        return aggregationRuntime;
    }

    private static IncrementalExecutor build(List<AttributeFunction> functionsAttributes,
                                             TimePeriod.Duration duration, IncrementalExecutor child, MetaComplexEvent metaEvent,
                                             int currentState, Map<String, Table> tableMap,
                                             List<VariableExpressionExecutor> executorList,
                                             ExecutionPlanContext executionPlanContext, boolean groupBy,
                                             int defaultStreamEventIndex, String queryName, Variable groupByVariable) {
        switch (duration) {
            case SECONDS:
                return IncrementalExecutor.second(functionsAttributes, child, metaEvent, currentState, tableMap,
                        executorList, executionPlanContext, groupBy, defaultStreamEventIndex, queryName, groupByVariable);
            case MINUTES:
                return IncrementalExecutor.minute(functionsAttributes, child, metaEvent, currentState, tableMap,
                        executorList, executionPlanContext, groupBy, defaultStreamEventIndex, queryName, groupByVariable);
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
