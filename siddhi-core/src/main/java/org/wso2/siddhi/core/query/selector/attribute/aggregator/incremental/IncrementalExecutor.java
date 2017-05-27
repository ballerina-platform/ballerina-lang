package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;

public class IncrementalExecutor implements Executor{
    // compositeAggregators contains a map of CompositeAggregator
    public List<CompositeAggregator> compositeAggregators;
    // basicExecutorDetails contains basic executors such as sum, count, and etc
    // we need to build composite aggregates.
    public List<ExpressionExecutorDetails> basicExecutorDetails;
    public ExecutionPlanContext executionPlanContext;
    // groupByExecutor is used to get the value of group by clause
    public ExpressionExecutor groupByExecutor;
    public long nextEmitTime = -1;
    public long startTime = 0;
    public TimePeriod.Duration duration;
    public IncrementalExecutor child;
    // For each unique value of the the "group by" attribute
    // we initialize this array and keep function values.
    public ConcurrentMap<String, ConcurrentMap<String, Object>> storeAggregatorFunctions;
    public IncrementalExecutor next;
    public Variable groupByVariable;
    public List<Attribute> FilteredAttributes;


    private IncrementalExecutor(TimePeriod.Duration duration, IncrementalExecutor child,
                                List<AttributeFunction> functionAttributes, MetaComplexEvent metaEvent,
                                int currentState, Map<String, Table> tableMap,
                                List<VariableExpressionExecutor> executorList,
                                ExecutionPlanContext executionPlanContext, boolean groupBy,
                                int defaultStreamEventIndex, String queryName, Variable groupByVariable) {
        this.duration = duration;
        this.child = child;
        this.compositeAggregators = createIncrementalAggregators(functionAttributes);
        this.basicExecutorDetails = basicFunctionExecutors(metaEvent, currentState, tableMap, executorList,
                executionPlanContext, groupBy, defaultStreamEventIndex, queryName);
        this.groupByExecutor = generateGroupByExecutor(groupByVariable, metaEvent, currentState, tableMap,
                executorList, executionPlanContext, defaultStreamEventIndex, queryName);
        storeAggregatorFunctions = new ConcurrentHashMap<>();
        this.executionPlanContext = executionPlanContext;
        this.groupByVariable = groupByVariable;
        setNextExecutor();
    }

    public List<CompositeAggregator> createIncrementalAggregators(List<AttributeFunction> functionAttributes) {
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

    public List<Object> calculateAggregators(String groupBy) {
        // TODO: 5/11/17 This returns the actual aggregate by using base aggregates (eg. avg=sum/count)
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

    public void resetAggregatorStore(){
        this.storeAggregatorFunctions = new ConcurrentHashMap<>();
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
    public ExpressionExecutor generateGroupByExecutor(Variable groupByClause, MetaComplexEvent metaEvent,
                                                       int currentState, Map<String, Table> tableMap,
                                                       List<VariableExpressionExecutor> executorList,
                                                       ExecutionPlanContext executionPlanContext,
                                                       int defaultStreamEventIndex, String queryName) {

        ExpressionExecutor variableExpressionExecutor = ExpressionParser.parseExpression(groupByClause, metaEvent,
                currentState, tableMap, executorList, executionPlanContext, true,
                defaultStreamEventIndex, queryName);
        return variableExpressionExecutor;
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
     * @param queryName
     * @return
     */
    public List<ExpressionExecutorDetails> basicFunctionExecutors(MetaComplexEvent metaEvent,
                                                                   int currentState, Map<String, Table> tableMap,
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
                    currentState, tableMap, executorList, executionPlanContext, groupBy,
                    defaultStreamEventIndex, queryName);
//            String executorUniqueKey = ((AttributeFunction) baseAggregator.getBaseExpression()).getName() + baseAggregator.getAttribute();
            ExpressionExecutorDetails executorDetails = new ExpressionExecutorDetails(expressionExecutor, baseAggregator.getAttribute()); // TODO: 5/25/17 we don't have to concat name again
            baseFunctionExecutors.add(executorDetails);
        }
        return baseFunctionExecutors;
    }

    @Override
    public void execute(ComplexEventChunk complexEventChunk) {
        // TODO: 5/27/17 per group start time or first event arrival time?
//        long currentTime = this.executionPlanContext.getTimestampGenerator().currentTime();
        ComplexEvent complexEvent =complexEventChunk.getFirst();
        long currentTime = complexEvent.getTimestamp(); // TODO: 5/27/17 this or getTimestampGenerator?
        if (nextEmitTime==-1) {
            nextEmitTime = getNextEmitTime(currentTime);
        }
        if (currentTime > nextEmitTime) {
            long timeStampOfBaseAggregate = getStartTime(nextEmitTime);
            nextEmitTime = getNextEmitTime(nextEmitTime);

            //send complex event with aggregate values if child not null. This should be for each group by
            // TODO: 5/27/17 this executes in the wrong way. group by doesn't work. Second, minute not working independently
            if (getNextExecutor()!= null) {
                for (String groupByKey: storeAggregatorFunctions.keySet()) {
                    //Create new on after window data to send aggregates to next iterator
                    List<Object> newOnAfterWindowData = new ArrayList<>();
                    newOnAfterWindowData.add(groupByKey);
                    ConcurrentMap<String, Object> aggregatesPerGroupBy = storeAggregatorFunctions.remove(groupByKey);
                    for (String aggregateKey: aggregatesPerGroupBy.keySet()) {
                        // TODO: 5/27/17 this if else is a hack!!! change
                        if (aggregateKey.startsWith("sum")) {
                            newOnAfterWindowData.add(((Double)aggregatesPerGroupBy.remove(aggregateKey)).floatValue());
                        } else if (aggregateKey.startsWith("count")) {
                            newOnAfterWindowData.add(((Long)aggregatesPerGroupBy.remove(aggregateKey)).floatValue());
                        }
                    }


                    StreamEvent tempStream = (StreamEvent) complexEvent;
                    complexEventChunk.clear(); //first is set to null
                    tempStream.setTimestamp(timeStampOfBaseAggregate);
                    tempStream.setOnAfterWindowData(newOnAfterWindowData.toArray());
                    complexEventChunk.add(tempStream); //tempStream would be added as first
                    getNextExecutor().execute(complexEventChunk); //pass this new complex event to next incremental aggregator
                }
            }
        }

        String groupByOutput = groupByExecutor.execute(complexEvent).toString();
        ConcurrentHashMap<String, Object> baseValuesPerGroupBy = new ConcurrentHashMap<>();
        for (ExpressionExecutorDetails basicExecutor:basicExecutorDetails) {
            baseValuesPerGroupBy.put(basicExecutor.getExecutorName(), basicExecutor.getExecutor().execute(complexEvent));
        }
        storeAggregatorFunctions.put(groupByOutput, baseValuesPerGroupBy);
        if (this.duration== TimePeriod.Duration.MINUTES){
            System.out.println(complexEvent);
            System.out.println(storeAggregatorFunctions);

        }

    }


    /*@Override
    public void execute(ComplexEventChunk complexEventChunk) {
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

        // TODO: 5/25/17 why are we getting 60 as 3rd value in chunk???
        // TODO: 5/25/17 in GroupByAggregationAttributeExecutor String key = QuerySelector.getThreadLocalGroupByKey(); is null



            String groupByOutput = (String) this.groupByExecutor.execute(complexEventChunk.getFirst()); // TODO: 5/17/17 this is wrong. Change
            for (ExpressionExecutorDetails executorDTO : basicExecutorDetails) {
                ExpressionExecutor expressionExecutor = executorDTO.getExecutor();
                Object value = expressionExecutor.execute(complexEventChunk.getFirst()); // TODO: 5/17/17 Wrong. Change
                String functionName = executorDTO.getExecutorName();
                if (storeAggregatorFunctions.containsKey(groupByOutput)) {
                    ConcurrentMap<String, Object> individualMap = storeAggregatorFunctions.get(groupByOutput);
                    if (individualMap.containsKey(functionName)) {
                        if (value instanceof Double) {
                            double newValue = (double)individualMap.get(functionName) +  (double)value; // TODO: 3/28/17 correct this
                            individualMap.put(functionName, newValue);
                        } else if (value instanceof Integer) {
                            int newValue = (int)individualMap.get(functionName) +  (int)value; // TODO: 3/28/17 correct this
                            individualMap.put(functionName, newValue);
                        }

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


    }*/

    public static IncrementalExecutor second(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                             MetaComplexEvent metaEvent,
                                             int currentState, Map<String, Table> tableMap,
                                             List<VariableExpressionExecutor> executorList,
                                             ExecutionPlanContext executionPlanContext, boolean groupBy,
                                             int defaultStreamEventIndex, String queryName,
                                             Variable groupByVariable) {
        return new IncrementalExecutor(TimePeriod.Duration.SECONDS, child, functionAttributes,
                metaEvent, currentState, tableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable);
    }

    public static IncrementalExecutor minute(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                             MetaComplexEvent metaEvent,
                                             int currentState, Map<String, Table> tableMap,
                                             List<VariableExpressionExecutor> executorList,
                                             ExecutionPlanContext executionPlanContext, boolean groupBy,
                                             int defaultStreamEventIndex, String queryName,
                                             Variable groupByVariable) {
        return new IncrementalExecutor(TimePeriod.Duration.MINUTES, child, functionAttributes,
                metaEvent, currentState, tableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable);
    }

    public static IncrementalExecutor hour(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                           MetaComplexEvent metaEvent,
                                           int currentState, Map<String, Table> tableMap,
                                           List<VariableExpressionExecutor> executorList,
                                           ExecutionPlanContext executionPlanContext, boolean groupBy,
                                           int defaultStreamEventIndex, String queryName,
                                           Variable groupByVariable) {
        return new IncrementalExecutor(TimePeriod.Duration.HOURS, child, functionAttributes,
                metaEvent, currentState, tableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable);
    }

    public static IncrementalExecutor day(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                          MetaComplexEvent metaEvent,
                                          int currentState, Map<String, Table> tableMap,
                                          List<VariableExpressionExecutor> executorList,
                                          ExecutionPlanContext executionPlanContext, boolean groupBy,
                                          int defaultStreamEventIndex, String queryName,
                                          Variable groupByVariable) {
        return new IncrementalExecutor(TimePeriod.Duration.DAYS, child, functionAttributes,
                metaEvent, currentState, tableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable);
    }

    public static IncrementalExecutor week(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                           MetaComplexEvent metaEvent,
                                           int currentState, Map<String, Table> tableMap,
                                           List<VariableExpressionExecutor> executorList,
                                           ExecutionPlanContext executionPlanContext, boolean groupBy,
                                           int defaultStreamEventIndex, String queryName, Variable groupByVariable) {
        return new IncrementalExecutor(TimePeriod.Duration.WEEKS, child, functionAttributes,
                metaEvent, currentState, tableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable);
    }

    public static IncrementalExecutor month(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                            MetaComplexEvent metaEvent,
                                            int currentState, Map<String, Table> tableMap,
                                            List<VariableExpressionExecutor> executorList,
                                            ExecutionPlanContext executionPlanContext, boolean groupBy,
                                            int defaultStreamEventIndex, String queryName, Variable groupByVariable) {
        return new IncrementalExecutor(TimePeriod.Duration.MONTHS, child, functionAttributes,
                metaEvent, currentState, tableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable);
    }

    public static IncrementalExecutor year(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                           MetaComplexEvent metaEvent,
                                           int currentState, Map<String, Table> tableMap,
                                           List<VariableExpressionExecutor> executorList,
                                           ExecutionPlanContext executionPlanContext, boolean groupBy,
                                           int defaultStreamEventIndex, String queryName, Variable groupByVariable) {
        return new IncrementalExecutor(TimePeriod.Duration.YEARS, child, functionAttributes,
                metaEvent, currentState, tableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable);
    }



    @Override
    public Executor getNextExecutor() {
        return next;
    }

    @Override
    public void setNextExecutor() {
        if (this.child != null) {
            next = this.child;
        }
    }

    @Override
    public void setToLast(Executor executor) { // TODO: 5/18/17 do we need this?
        if (((IncrementalExecutor)executor).child == null) {
            setToLast(executor);
        }
    }

    @Override
    public Executor cloneExecutor(String key) {
        return null;
    }

    public class ExpressionExecutorDetails {
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
        private String attribute; // baseExpression will be evaluated against this attribute

        public BaseExpressionDetails(Expression baseExpression, String attribute) {
            this.baseExpression = baseExpression;
            //If we haven't already prefixed with base name, it's concatenated now (Since price1 is no longer in meta)
            //This could occur in avg since the attribute is sent as price1.
            if (attribute.startsWith(((AttributeFunction) baseExpression).getName())) {
                this.attribute = attribute;
            }
            else {
                this.attribute = (((AttributeFunction) baseExpression).getName()).concat(attribute);
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

    /*public long getNextEmitTime(long currentTime) {
        // returns the next emission time based on system clock round time values.
        long elapsedTimeSinceLastEmit = (currentTime - startTime) % 1000;
        long emitTime = currentTime + (1000 - elapsedTimeSinceLastEmit);
        return emitTime;
    }*/

    private long getNextEmitTime(long currentTime) {
        switch (this.duration) {
            case SECONDS:
                return currentTime + 1000;
            case MINUTES:
                return currentTime + 60000;
            // TODO: 5/26/17 add rest
            default:
                return -1; // TODO: 5/26/17 This must be corrected
        }
    }

    private long getStartTime(long nextEmitTime) {
        switch (this.duration) {
            case SECONDS:
                return nextEmitTime - 1000;
            case MINUTES:
                return nextEmitTime - 60000;
            // TODO: 5/26/17 add rest
            default:
                return -1; // TODO: 5/26/17 This must be corrected
        }
    }
}