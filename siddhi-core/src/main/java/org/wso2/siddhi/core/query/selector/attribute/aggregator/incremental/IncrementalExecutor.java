package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.mvel2.util.Varargs;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class IncrementalExecutor {
    // has the property called Duration it can be SECOND, MINUTE and etc
    // list of functions operators
    // Also need to have a link to the parent for instance a link from SECOND to MINUTE
    // Next, based on basic calculators, we initialize a set of basic calculators
    //
    private TimePeriod.Duration duration;
    private IncrementalExecutor child;
    private ConcurrentMap<String, IncrementalAggregator> aggregatorsMap;
    private List<ExpressionExecutorDTO> expressionExecutors;
    private List<String> executorNames;
    private ExpressionExecutor groupByExecutor;

    // For each unique value of the the "group by" attribute
    // we initialize this array and keep function values.
    private ConcurrentMap<String, ConcurrentMap<String, Object>> storeAggregatorFunctions;


    private IncrementalExecutor(TimePeriod.Duration duration, IncrementalExecutor child,
                                List<AttributeFunction> functionAttributes, MetaComplexEvent metaEvent,
                                int currentState, Map<String, EventTable> eventTableMap,
                                List<VariableExpressionExecutor> executorList,
                                ExecutionPlanContext executionPlanContext, boolean groupBy,
                                int defaultStreamEventIndex, String queryName, Variable groupByVariable) {
        this.duration = duration;
        this.child = child;
        this.aggregatorsMap = createIncrementalAggregators(functionAttributes);
        this.expressionExecutors = generateFunctionExecutors(metaEvent, currentState, eventTableMap, executorList,
                executionPlanContext, groupBy, defaultStreamEventIndex, queryName);
        this.groupByExecutor = generateGroupByExecutor(groupByVariable, metaEvent, currentState, eventTableMap,
                executorList, executionPlanContext, defaultStreamEventIndex, queryName);
        storeAggregatorFunctions = new ConcurrentHashMap<>();
    }

    private ConcurrentMap<String, IncrementalAggregator> createIncrementalAggregators(List<AttributeFunction> functionAttributes) {
        ConcurrentMap<String, IncrementalAggregator> aggregatorsMap = new ConcurrentHashMap<>();
        for (AttributeFunction function : functionAttributes) {
            if (function.getName().equals("avg")) {
                AvgIncrementalAttributeAggregator average = new AvgIncrementalAttributeAggregator(function);
                aggregatorsMap.putIfAbsent(average.getAttributeName(), average);
            } else {
                // TODO: 3/10/17 add other Exceptions
            }
        }
        return aggregatorsMap;
    }

    private ExpressionExecutor generateGroupByExecutor(Variable groupBy, MetaComplexEvent metaEvent,
                                                       int currentState, Map<String, EventTable> eventTableMap,
                                                       List<VariableExpressionExecutor> executorList,
                                                       ExecutionPlanContext executionPlanContext,
                                                       int defaultStreamEventIndex, String queryName) {

        ExpressionExecutor variableExpressionExecutor = ExpressionParser.parseExpression(groupBy, metaEvent, currentState, eventTableMap, executorList,
                executionPlanContext, true, defaultStreamEventIndex, queryName);
        return variableExpressionExecutor;
    }

    private List<ExpressionExecutorDTO> generateFunctionExecutors(MetaComplexEvent metaEvent,
                                                                  int currentState, Map<String, EventTable> eventTableMap,
                                                                  List<VariableExpressionExecutor> executorList,
                                                                  ExecutionPlanContext executionPlanContext, boolean groupBy,
                                                                  int defaultStreamEventIndex, String queryName) {
        Set<Expression> baseAggregators = new HashSet<>();
        for (String aggregator : this.aggregatorsMap.keySet()) {
            Expression[] bases = this.aggregatorsMap.get(aggregator).getBaseAggregators();
            baseAggregators.addAll(Arrays.asList(bases));
        }

        List<ExpressionExecutorDTO> baseFunctionExecutors = new ArrayList<>();
        for (Expression baseAggregator : baseAggregators) {

            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(baseAggregator, metaEvent,
                    currentState, eventTableMap, executorList, executionPlanContext, groupBy,
                    defaultStreamEventIndex, queryName);
            String executorName = ((AttributeFunction) baseAggregator).getName(); // TODO: 3/28/17 error checking ....?
            ExpressionExecutorDTO executorDTO = new ExpressionExecutorDTO(expressionExecutor, executorName);
            baseFunctionExecutors.add(executorDTO);
        }
        return baseFunctionExecutors;
    }


    public void execute(ComplexEvent event) {

        // TODO: 3/27/17 Based on the output type correctly pass this
        String groupByOutput = (String) this.groupByExecutor.execute(event);
        for (ExpressionExecutorDTO executorDTO : expressionExecutors) {
            ExpressionExecutor expressionExecutor = executorDTO.getExecutor();
            Object value = expressionExecutor.execute(event);
            String functionName = executorDTO.getExecutorName();
            if (storeAggregatorFunctions.containsKey(groupByOutput)) {
                ConcurrentMap<String, Object> individualMap = storeAggregatorFunctions.get(groupByOutput);
                if (individualMap.containsKey(functionName)) {
                    double newValue = (Double) individualMap.get(functionName) + (Double) value; // TODO: 3/28/17 correct this
                    individualMap.put(functionName, newValue);
                } else {
                    individualMap.put(functionName, value);
                }
            } else {
                ConcurrentMap<String, Object> individualMap = new ConcurrentHashMap<>();
                individualMap.put(functionName, value);
                storeAggregatorFunctions.put(groupByOutput, individualMap);
            }
        }
    }


    public static IncrementalExecutor second(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                             MetaComplexEvent metaEvent,
                                             int currentState, Map<String, EventTable> eventTableMap,
                                             List<VariableExpressionExecutor> executorList,
                                             ExecutionPlanContext executionPlanContext, boolean groupBy,
                                             int defaultStreamEventIndex, String queryName,
                                             Variable groupByVariable) {
        IncrementalExecutor second = new IncrementalExecutor(TimePeriod.Duration.SECONDS, child, functionAttributes,
                metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex, queryName, groupByVariable);
        return second;
    }

    public static IncrementalExecutor minute(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                             MetaComplexEvent metaEvent,
                                             int currentState, Map<String, EventTable> eventTableMap,
                                             List<VariableExpressionExecutor> executorList,
                                             ExecutionPlanContext executionPlanContext, boolean groupBy,
                                             int defaultStreamEventIndex, String queryName,
                                             Variable groupByVariable) {
        IncrementalExecutor minute = new IncrementalExecutor(TimePeriod.Duration.MINUTES, child, functionAttributes,
                metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable);
        return minute;
    }

    public static IncrementalExecutor hour(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                           MetaComplexEvent metaEvent,
                                           int currentState, Map<String, EventTable> eventTableMap,
                                           List<VariableExpressionExecutor> executorList,
                                           ExecutionPlanContext executionPlanContext, boolean groupBy,
                                           int defaultStreamEventIndex, String queryName,
                                           Variable groupByVariable) {
        IncrementalExecutor hour = new IncrementalExecutor(TimePeriod.Duration.HOURS, child, functionAttributes,
                metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable);
        return hour;
    }

    public static IncrementalExecutor day(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                          MetaComplexEvent metaEvent,
                                          int currentState, Map<String, EventTable> eventTableMap,
                                          List<VariableExpressionExecutor> executorList,
                                          ExecutionPlanContext executionPlanContext, boolean groupBy,
                                          int defaultStreamEventIndex, String queryName,
                                          Variable groupByVariable) {
        IncrementalExecutor day = new IncrementalExecutor(TimePeriod.Duration.DAYS, child, functionAttributes,
                metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable);
        return day;
    }

    public static IncrementalExecutor week(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                           MetaComplexEvent metaEvent,
                                           int currentState, Map<String, EventTable> eventTableMap,
                                           List<VariableExpressionExecutor> executorList,
                                           ExecutionPlanContext executionPlanContext, boolean groupBy,
                                           int defaultStreamEventIndex, String queryName, Variable groupByVariable) {
        IncrementalExecutor week = new IncrementalExecutor(TimePeriod.Duration.WEEKS, child, functionAttributes,
                metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable);
        return week;
    }

    public static IncrementalExecutor month(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                            MetaComplexEvent metaEvent,
                                            int currentState, Map<String, EventTable> eventTableMap,
                                            List<VariableExpressionExecutor> executorList,
                                            ExecutionPlanContext executionPlanContext, boolean groupBy,
                                            int defaultStreamEventIndex, String queryName, Variable groupByVariable) {
        IncrementalExecutor month = new IncrementalExecutor(TimePeriod.Duration.MONTHS, child, functionAttributes,
                metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable);
        return month;
    }

    public static IncrementalExecutor year(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                           MetaComplexEvent metaEvent,
                                           int currentState, Map<String, EventTable> eventTableMap,
                                           List<VariableExpressionExecutor> executorList,
                                           ExecutionPlanContext executionPlanContext, boolean groupBy,
                                           int defaultStreamEventIndex, String queryName, Variable groupByVariable) {
        IncrementalExecutor year = new IncrementalExecutor(TimePeriod.Duration.YEARS, child, functionAttributes,
                metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable);
        return year;
    }

    private class ExpressionExecutorDTO {
        private ExpressionExecutor executor;
        private String executorName;

        public ExpressionExecutorDTO(ExpressionExecutor executor, String executorName) {
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
}