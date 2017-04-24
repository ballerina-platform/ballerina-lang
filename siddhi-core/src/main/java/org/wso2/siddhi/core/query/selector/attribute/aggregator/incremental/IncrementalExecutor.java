package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
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
    // compositeAggregators contains a map of CompositeAggregator
    private List<CompositeAggregator> compositeAggregators;
    // basicExecutorDetails contains basic executors such as sum, count, and etc
    // we need to build composite aggregates.
    private List<ExpressionExecutorDetails> basicExecutorDetails;
    private ExecutionPlanContext executionPlanContext;
    // groupByExecutor is used to get the value of group by clause
    private ExpressionExecutor groupByExecutor;
    private long nextEmitTime = -1;
    private long startTime = 0;
    private TimePeriod.Duration duration;
    private IncrementalExecutor child;
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
        this.compositeAggregators = createIncrementalAggregators(functionAttributes);
        this.basicExecutorDetails = basicFunctionExecutors(metaEvent, currentState, eventTableMap, executorList,
                executionPlanContext, groupBy, defaultStreamEventIndex, queryName);
        this.groupByExecutor = generateGroupByExecutor(groupByVariable, metaEvent, currentState, eventTableMap,
                executorList, executionPlanContext, defaultStreamEventIndex, queryName);
        storeAggregatorFunctions = new ConcurrentHashMap<>();
        this.executionPlanContext = executionPlanContext;
    }

    private List<CompositeAggregator> createIncrementalAggregators(List<AttributeFunction> functionAttributes) {
        List<CompositeAggregator> compositeAggregators = new ArrayList<>();
        for (AttributeFunction function : functionAttributes) {
            if (function.getName().equals("avg")) {
                AvgIncrementalAttributeAggregator average = new AvgIncrementalAttributeAggregator(function);
                compositeAggregators.add(average);
            } else {
                // TODO: 3/10/17 add other Exceptions
            }
        }
        return compositeAggregators;
    }

    private List<Object> calculateAggregators(String groupBy) {
        List<Object> aggregatorValues = new ArrayList<>();
        for (CompositeAggregator compositeAggregator : this.compositeAggregators) {
            // key will be attribute name + function name, examples price+ave, age+count etc
            ConcurrentMap<String, Object> baseAggregatorValues = this.storeAggregatorFunctions.get(groupBy);
            Expression[] baseAggregators = compositeAggregator.getBaseAggregators();
            Object[] expressionValues = new Object[baseAggregators.length];
            for (int i = 0; i < baseAggregators.length; i++) {
                Expression aggregator = baseAggregators[i];
                String functionName = ((AttributeFunction) aggregator).getName();
                String attributeName = compositeAggregator.getAttributeName();
                expressionValues[i] = baseAggregatorValues.get(functionName + attributeName);
            }
            aggregatorValues.add(compositeAggregator.aggregate(expressionValues));
        }
        return aggregatorValues;
    }

    private void resetAggregatorStore(){
        this.storeAggregatorFunctions = new ConcurrentHashMap<>();
    }

    /**
     * @param groupByClause
     * @param metaEvent
     * @param currentState
     * @param eventTableMap
     * @param executorList
     * @param executionPlanContext
     * @param defaultStreamEventIndex
     * @param queryName
     * @return
     */
    private ExpressionExecutor generateGroupByExecutor(Variable groupByClause, MetaComplexEvent metaEvent,
                                                       int currentState, Map<String, EventTable> eventTableMap,
                                                       List<VariableExpressionExecutor> executorList,
                                                       ExecutionPlanContext executionPlanContext,
                                                       int defaultStreamEventIndex, String queryName) {

        ExpressionExecutor variableExpressionExecutor = ExpressionParser.parseExpression(groupByClause, metaEvent,
                currentState, eventTableMap, executorList, executionPlanContext, true,
                defaultStreamEventIndex, queryName);
        return variableExpressionExecutor;
    }


    /***
     *
     * @param metaEvent
     * @param currentState
     * @param eventTableMap
     * @param executorList
     * @param executionPlanContext
     * @param groupBy
     * @param defaultStreamEventIndex
     * @param queryName
     * @return
     */
    private List<ExpressionExecutorDetails> basicFunctionExecutors(MetaComplexEvent metaEvent,
                                                                   int currentState, Map<String, EventTable> eventTableMap,
                                                                   List<VariableExpressionExecutor> executorList,
                                                                   ExecutionPlanContext executionPlanContext, boolean groupBy,
                                                                   int defaultStreamEventIndex, String queryName) {
        Set<BaseExpressionDetails> baseAggregators = new HashSet<>();
        for(CompositeAggregator compositeAggregator : this.compositeAggregators){
            Expression[] bases = compositeAggregator.getBaseAggregators();
            for(Expression expression : bases){
                BaseExpressionDetails baseExpressionDetails = new BaseExpressionDetails(expression, compositeAggregator.getAttributeName());
                baseAggregators.add(baseExpressionDetails);
            }
        }

        List<ExpressionExecutorDetails> baseFunctionExecutors = new ArrayList<>();
        for (BaseExpressionDetails baseAggregator : baseAggregators) {

            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(baseAggregator.getBaseExpression(), metaEvent,
                    currentState, eventTableMap, executorList, executionPlanContext, groupBy,
                    defaultStreamEventIndex, queryName);
            String executorUniqueKey = ((AttributeFunction) baseAggregator.getBaseExpression()).getName() + baseAggregator.getAttribute();
            ExpressionExecutorDetails executorDetails = new ExpressionExecutorDetails(expressionExecutor, executorUniqueKey);
            baseFunctionExecutors.add(executorDetails);
        }
        return baseFunctionExecutors;
    }


    public void execute(ComplexEvent event) {
        if (nextEmitTime == -1) {
            long currentTime = this.executionPlanContext.getTimestampGenerator().currentTime();
            nextEmitTime = getNextEmitTime(currentTime);
        }
        boolean sendEvents;
        long currentTime = executionPlanContext.getTimestampGenerator().currentTime();
        if (currentTime >= nextEmitTime) {
            nextEmitTime += 1000; // TODO: 3/29/17 :
            sendEvents = true;
        } else {
            sendEvents = false;
        }

        // TODO: 3/27/17 Based on the output type correctly pass this
        // TODO: 3/29/17 Handle multiple group by clauses
        String groupByOutput = (String) this.groupByExecutor.execute(event);
        for (ExpressionExecutorDetails executorDTO : basicExecutorDetails) {
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

        if (sendEvents) {
            // 1. Extract relevant data from HashMap. Create an event and send it
                  // calculateAggregators(groupBy)
            // 2. Update the child
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

    private class ExpressionExecutorDetails {
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

    private class BaseExpressionDetails {
        private Expression baseExpression;
        private String attribute; // baseExpression will be evaluated against this attribure

        public BaseExpressionDetails(Expression baseExpression, String attribute) {
            this.baseExpression = baseExpression;
            this.attribute = attribute;
        }

        public Expression getBaseExpression() {
            return this.baseExpression;
        }

        public String getAttribute() {
            return this.attribute;
        }

        @Override
        public int hashCode(){
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

    private long getNextEmitTime(long currentTime) {
        // returns the next emission time based on system clock round time values.
        long elapsedTimeSinceLastEmit = (currentTime - startTime) % 1000;
        long emitTime = currentTime + (1000 - elapsedTimeSinceLastEmit);
        return emitTime;
    }
}