package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.In;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class IncrementalCalculator {
    // has the property called Duration it can be SECOND, MINUTE and etc
    // list of functions operators
    // Also need to have a link to the parent for instance a link from SECOND to MINUTE
    // Next, based on basic calculators, we initialize a set of basic calculators
    //

    private TimePeriod.Duration duration;
    private IncrementalCalculator child;
    private List<AttributeFunction> functionAttributes;
    private ConcurrentMap<String, IncrementalAggregator> aggregatorsMap;

    private IncrementalCalculator(TimePeriod.Duration duration, IncrementalCalculator child,
                                  List<AttributeFunction> functionAttributes, MetaComplexEvent metaEvent,
                                  int currentState, Map<String, EventTable> eventTableMap,
                                  List<VariableExpressionExecutor> executorList,
                                  ExecutionPlanContext executionPlanContext, boolean groupBy,
                                  int defaultStreamEventIndex, String queryName) {
        this.duration = duration;
        this.child = child;
        this.functionAttributes = functionAttributes;
        this.aggregatorsMap = new ConcurrentHashMap<>();
        createIncrementalAggregators(this.functionAttributes);
    }

    private void createIncrementalAggregators(List<AttributeFunction> functionAttributes) {
        //List<IncrementalAggregator> incrementalAggregators = new ArrayList<>();
        for (AttributeFunction function : functionAttributes) {
            if (function.getName().equals("avg")) {
                AvgIncrementalAttributeAggregator average = new AvgIncrementalAttributeAggregator(function);
                aggregatorsMap.putIfAbsent(average.getAttributeName(), average);
                //incrementalAggregators.add(average);
            } else {
                // TODO: 3/10/17 Exception....
            }
        }
        //return incrementalAggregators;
    }

    public List<ExpressionExecutor> generateFunctionExecutors(MetaComplexEvent metaEvent,
                                                            int currentState, Map<String, EventTable> eventTableMap,
                                                            List<VariableExpressionExecutor> executorList,
                                                            ExecutionPlanContext executionPlanContext, boolean groupBy,
                                                            int defaultStreamEventIndex, String queryName){
        Set<Expression> baseAggregators = new HashSet<>();
        for (String aggregator : this.aggregatorsMap.keySet()) {
            Expression[] bases = this.aggregatorsMap.get(aggregator).getBaseAggregators();
            baseAggregators.addAll(Arrays.asList(bases));
        }

        List<ExpressionExecutor> baseFunctionExecutors = new ArrayList<>();
        for(Expression baseAggregator : baseAggregators){
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(baseAggregator,metaEvent,
                    currentState,eventTableMap,executorList, executionPlanContext, groupBy,
                    defaultStreamEventIndex, queryName);
            baseFunctionExecutors.add(expressionExecutor);
        }
        return baseFunctionExecutors;
    }

    public Object execute(ComplexEvent event) {
        // TODO: 3/22/17 : correctly identify array indices
        
    }

    public static IncrementalCalculator second(List<AttributeFunction> functionAttributes, IncrementalCalculator child,
                                               MetaComplexEvent metaEvent,
                                               int currentState, Map<String, EventTable> eventTableMap,
                                               List<VariableExpressionExecutor> executorList,
                                               ExecutionPlanContext executionPlanContext, boolean groupBy,
                                               int defaultStreamEventIndex, String queryName) {
        IncrementalCalculator second = new IncrementalCalculator(TimePeriod.Duration.SECONDS, child, functionAttributes,
                metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex, queryName);
        return second;
    }

    public static IncrementalCalculator minute(List<AttributeFunction> functionAttributes, IncrementalCalculator child,
                                               MetaComplexEvent metaEvent,
                                               int currentState, Map<String, EventTable> eventTableMap,
                                               List<VariableExpressionExecutor> executorList,
                                               ExecutionPlanContext executionPlanContext, boolean groupBy,
                                               int defaultStreamEventIndex, String queryName) {
        IncrementalCalculator minute = new IncrementalCalculator(TimePeriod.Duration.MINUTES, child, functionAttributes,
                metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex, queryName);
        return minute;
    }

    public static IncrementalCalculator hour(List<AttributeFunction> functionAttributes, IncrementalCalculator child,
                                             MetaComplexEvent metaEvent,
                                             int currentState, Map<String, EventTable> eventTableMap,
                                             List<VariableExpressionExecutor> executorList,
                                             ExecutionPlanContext executionPlanContext, boolean groupBy,
                                             int defaultStreamEventIndex, String queryName) {
        IncrementalCalculator hour = new IncrementalCalculator(TimePeriod.Duration.HOURS, child, functionAttributes,
                metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex, queryName);
        return hour;
    }

    public static IncrementalCalculator day(List<AttributeFunction> functionAttributes, IncrementalCalculator child,
                                            MetaComplexEvent metaEvent,
                                            int currentState, Map<String, EventTable> eventTableMap,
                                            List<VariableExpressionExecutor> executorList,
                                            ExecutionPlanContext executionPlanContext, boolean groupBy,
                                            int defaultStreamEventIndex, String queryName) {
        IncrementalCalculator day = new IncrementalCalculator(TimePeriod.Duration.DAYS, child, functionAttributes,
                metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex, queryName);
        return day;
    }

    public static IncrementalCalculator week(List<AttributeFunction> functionAttributes, IncrementalCalculator child,
                                             MetaComplexEvent metaEvent,
                                             int currentState, Map<String, EventTable> eventTableMap,
                                             List<VariableExpressionExecutor> executorList,
                                             ExecutionPlanContext executionPlanContext, boolean groupBy,
                                             int defaultStreamEventIndex, String queryName) {
        IncrementalCalculator week = new IncrementalCalculator(TimePeriod.Duration.WEEKS, child, functionAttributes,
                metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex, queryName);
        return week;
    }

    public static IncrementalCalculator month(List<AttributeFunction> functionAttributes, IncrementalCalculator child,
                                              MetaComplexEvent metaEvent,
                                              int currentState, Map<String, EventTable> eventTableMap,
                                              List<VariableExpressionExecutor> executorList,
                                              ExecutionPlanContext executionPlanContext, boolean groupBy,
                                              int defaultStreamEventIndex, String queryName) {
        IncrementalCalculator month = new IncrementalCalculator(TimePeriod.Duration.MONTHS, child, functionAttributes,
                metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex, queryName);
        return month;
    }

    public static IncrementalCalculator year(List<AttributeFunction> functionAttributes, IncrementalCalculator child,
                                             MetaComplexEvent metaEvent,
                                             int currentState, Map<String, EventTable> eventTableMap,
                                             List<VariableExpressionExecutor> executorList,
                                             ExecutionPlanContext executionPlanContext, boolean groupBy,
                                             int defaultStreamEventIndex, String queryName) {
        IncrementalCalculator year = new IncrementalCalculator(TimePeriod.Duration.YEARS, child, functionAttributes,
                metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex, queryName);
        return year;
    }
}