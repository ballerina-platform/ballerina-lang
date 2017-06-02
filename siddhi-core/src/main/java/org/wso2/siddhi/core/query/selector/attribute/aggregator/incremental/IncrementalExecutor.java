package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.populater.StateEventPopulator;
import org.wso2.siddhi.core.event.state.populater.StateEventPopulatorFactory;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.input.stream.single.EntryValveProcessor;
import org.wso2.siddhi.core.table.InMemoryTable;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.lock.LockWrapper;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.core.util.parser.SchedulerParser;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

public class IncrementalExecutor implements Executor, Job {
    // compositeAggregators contains a map of CompositeAggregator
    public List<CompositeAggregator> compositeAggregators;
    // basicExecutorDetails contains basic executors such as sum, count, and etc
    // we need to build composite aggregates.
    public List<ExpressionExecutorDetails> basicExecutorDetails;
    public ExecutionPlanContext executionPlanContext;
    // groupByExecutor is used to get the value of group by clause
    private ExpressionExecutor groupByExecutor;
    private ExpressionExecutor externalTimeStampExecutor = null;
    public long nextEmitTime = -1;
    public long startTime = 0;
    public TimePeriod.Duration duration;
    public IncrementalExecutor child;
    // For each unique value of the the "group by" attribute
    // we initialize this array and keep function values.
    public ConcurrentMap<String, ConcurrentMap<String, Object>> storeAggregatorFunctions;
    public IncrementalExecutor next;
    public List<Attribute> FilteredAttributes;
    //Table to write data to
    public InMemoryTable inMemoryTable;
    public TableDefinition tableDefinition;
    public StreamEventCloner streamEventCloner;
    private StateEventPopulator eventPopulator;
    private GroupByKeyGeneratorForIncremental groupByKeyGenerator;
    private static final ThreadLocal<String> keyThreadLocal = new ThreadLocal<String>();
    private Scheduler scheduler;
    private String jobName;
    private final String jobGroup = "IncrementalWindowGroup";
    private final StreamEvent resetEvent;
    private int onAfterWindowLength;
    private Date date;

    private IncrementalExecutor(TimePeriod.Duration duration, IncrementalExecutor child,
                                List<AttributeFunction> functionAttributes, MetaComplexEvent metaEvent,
                                int currentState, Map<String, Table> tableMap,
                                List<VariableExpressionExecutor> executorList,
                                ExecutionPlanContext executionPlanContext, boolean groupBy,
                                int defaultStreamEventIndex, String aggregatorName, Variable groupByVariable,
                                GroupByKeyGeneratorForIncremental groupByKeyGenerator,
                                Variable timeStampVariable) {
        this.duration = duration;
        this.child = child;
        this.compositeAggregators = createIncrementalAggregators(functionAttributes);
        this.basicExecutorDetails = basicFunctionExecutors(metaEvent, currentState, tableMap, executorList,
                executionPlanContext, groupBy, defaultStreamEventIndex, aggregatorName);
        this.groupByExecutor = generateGroupByExecutor(groupByVariable, metaEvent, currentState, tableMap,
                executorList, executionPlanContext, defaultStreamEventIndex, aggregatorName);
        if (timeStampVariable!=null){
            this.externalTimeStampExecutor = generateTimeStampExecutor(timeStampVariable, metaEvent, currentState, tableMap,
                    executorList, executionPlanContext, defaultStreamEventIndex, aggregatorName);
        }
        storeAggregatorFunctions = new ConcurrentHashMap<>();
        this.executionPlanContext = executionPlanContext;
        this.groupByKeyGenerator = groupByKeyGenerator;
        this.onAfterWindowLength = ((MetaStreamEvent)metaEvent).getOnAfterWindowData().size();

        //Create a dummy event to reset aggregates
        this.resetEvent = new StreamEvent(0,onAfterWindowLength,0);
        resetEvent.setType(ComplexEvent.Type.RESET);

        this.date = new Date();


        List<Variable> groupByList = new ArrayList<>();
        groupByList.add(groupByVariable); // TODO: 5/30/17 we must later get a list from parser itself

        setNextExecutor();

        initDefaultTable(tableMap, aggregatorName);

        createStreamEventCloner((MetaStreamEvent) metaEvent);

        setEventPopulator(metaEvent);

//        scheduleCronJob("*/1 * * * * ?", "aaaaaa"); // TODO: 5/31/17 change to 0th second

        EntryValveProcessor entryValveProcessor = new EntryValveProcessor(this.executionPlanContext);
        Scheduler scheduler = SchedulerParser.parse(this.executionPlanContext.getScheduledExecutorService(),
                    entryValveProcessor, this.executionPlanContext);
        LockWrapper lockWrapper = new LockWrapper(aggregatorName);
        lockWrapper.setLock(new ReentrantLock());
        scheduler.init(lockWrapper, aggregatorName);
        scheduler.setStreamEventPool(new StreamEventPool((MetaStreamEvent) metaEvent,5));
        setScheduler(scheduler);
//            ((SchedulingProcessor) internalWindowProcessor).setScheduler(scheduler);

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
    private ExpressionExecutor generateGroupByExecutor(Variable groupByClause, MetaComplexEvent metaEvent,
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
    private ExpressionExecutor generateTimeStampExecutor(Variable timeStampVariable, MetaComplexEvent metaEvent,
                                                         int currentState, Map<String, Table> tableMap,
                                                         List<VariableExpressionExecutor> executorList,
                                                         ExecutionPlanContext executionPlanContext,
                                                         int defaultStreamEventIndex, String queryName) {
        ExpressionExecutor variableExpressionExecutor = ExpressionParser.parseExpression(timeStampVariable, metaEvent,
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
    // TODO: 5/31/17 following was written for cron job
    /*@Override
    public void execute(ComplexEventChunk streamEventChunk) {
        //Logic: When events come, process and store in storeAggregatorFunctions
        //       Have a cron job to read data in storeAggregatorFunctions and form an event chunk -> send to next
        //       executor and send reset event to groupByExecutor (reset should happen before current event is processed)


        if (streamEventChunk.getFirst() != null) {
            processGroupBy(streamEventChunk);
        }
    }*/

    @Override
    public void execute(ComplexEventChunk streamEventChunk) {
        if (externalTimeStampExecutor!=null) {
            //User specified timestamp must be used
            while (streamEventChunk.hasNext()) {
                StreamEvent event = (StreamEvent)streamEventChunk.next();
                //Create new chunk to hold one stream event only
                ComplexEventChunk<StreamEvent> newEventChunk = new ComplexEventChunk<>();
                newEventChunk.add(event);
                long externalTimeStamp = (long) externalTimeStampExecutor.execute(event);
                if (nextEmitTime == -1) {
                    nextEmitTime = getNextEmitTime(externalTimeStamp);
//                scheduler.notifyAt(nextEmitTime);
                }
                if (externalTimeStamp > nextEmitTime) {
                    long timeStampOfBaseAggregate = getStartTime(nextEmitTime);
                    nextEmitTime = getNextEmitTime(externalTimeStamp);
                    dispatchEvents(timeStampOfBaseAggregate);
                }
                processGroupBy(newEventChunk);
            }

        } else {

            long currentTime = this.executionPlanContext.getTimestampGenerator().currentTime();
            if (nextEmitTime == -1) {
                nextEmitTime = getNextEmitTime(currentTime);
//                scheduler.notifyAt(nextEmitTime);
            }
            if (currentTime > nextEmitTime) {
                long timeStampOfBaseAggregate = getStartTime(nextEmitTime);
                nextEmitTime = getNextEmitTime(currentTime);
                dispatchEvents(timeStampOfBaseAggregate);
            }
            processGroupBy(streamEventChunk);
        }
    }



    public static IncrementalExecutor second(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                             MetaComplexEvent metaEvent,
                                             int currentState, Map<String, Table> tableMap,
                                             List<VariableExpressionExecutor> executorList,
                                             ExecutionPlanContext executionPlanContext, boolean groupBy,
                                             int defaultStreamEventIndex, String queryName,
                                             Variable groupByVariable, GroupByKeyGeneratorForIncremental groupByKeyGenerator, Variable timeStampVariable) {
        return new IncrementalExecutor(TimePeriod.Duration.SECONDS, child, functionAttributes,
                metaEvent, currentState, tableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable, groupByKeyGenerator, timeStampVariable);
    }

    public static IncrementalExecutor minute(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                             MetaComplexEvent metaEvent,
                                             int currentState, Map<String, Table> tableMap,
                                             List<VariableExpressionExecutor> executorList,
                                             ExecutionPlanContext executionPlanContext, boolean groupBy,
                                             int defaultStreamEventIndex, String queryName,
                                             Variable groupByVariable, GroupByKeyGeneratorForIncremental groupByKeyGenerator, Variable timeStampVariable) {
        return new IncrementalExecutor(TimePeriod.Duration.MINUTES, child, functionAttributes,
                metaEvent, currentState, tableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable, groupByKeyGenerator, timeStampVariable);
    }

    public static IncrementalExecutor hour(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                           MetaComplexEvent metaEvent,
                                           int currentState, Map<String, Table> tableMap,
                                           List<VariableExpressionExecutor> executorList,
                                           ExecutionPlanContext executionPlanContext, boolean groupBy,
                                           int defaultStreamEventIndex, String queryName,
                                           Variable groupByVariable, GroupByKeyGeneratorForIncremental groupByKeyGenerator, Variable timeStampVariable) {
        return new IncrementalExecutor(TimePeriod.Duration.HOURS, child, functionAttributes,
                metaEvent, currentState, tableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable, groupByKeyGenerator, timeStampVariable);
    }

    public static IncrementalExecutor day(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                          MetaComplexEvent metaEvent,
                                          int currentState, Map<String, Table> tableMap,
                                          List<VariableExpressionExecutor> executorList,
                                          ExecutionPlanContext executionPlanContext, boolean groupBy,
                                          int defaultStreamEventIndex, String queryName,
                                          Variable groupByVariable, GroupByKeyGeneratorForIncremental groupByKeyGenerator, Variable timeStampVariable) {
        return new IncrementalExecutor(TimePeriod.Duration.DAYS, child, functionAttributes,
                metaEvent, currentState, tableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable, groupByKeyGenerator, timeStampVariable);
    }

    public static IncrementalExecutor week(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                           MetaComplexEvent metaEvent,
                                           int currentState, Map<String, Table> tableMap,
                                           List<VariableExpressionExecutor> executorList,
                                           ExecutionPlanContext executionPlanContext, boolean groupBy,
                                           int defaultStreamEventIndex, String queryName, Variable groupByVariable,
                                           GroupByKeyGeneratorForIncremental groupByKeyGenerator, Variable timeStampVariable) {
        return new IncrementalExecutor(TimePeriod.Duration.WEEKS, child, functionAttributes,
                metaEvent, currentState, tableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable, groupByKeyGenerator, timeStampVariable);
    }

    public static IncrementalExecutor month(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                            MetaComplexEvent metaEvent,
                                            int currentState, Map<String, Table> tableMap,
                                            List<VariableExpressionExecutor> executorList,
                                            ExecutionPlanContext executionPlanContext, boolean groupBy,
                                            int defaultStreamEventIndex, String queryName, Variable groupByVariable,
                                            GroupByKeyGeneratorForIncremental groupByKeyGenerator, Variable timeStampVariable) {
        return new IncrementalExecutor(TimePeriod.Duration.MONTHS, child, functionAttributes,
                metaEvent, currentState, tableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable, groupByKeyGenerator, timeStampVariable);
    }

    public static IncrementalExecutor year(List<AttributeFunction> functionAttributes, IncrementalExecutor child,
                                           MetaComplexEvent metaEvent,
                                           int currentState, Map<String, Table> tableMap,
                                           List<VariableExpressionExecutor> executorList,
                                           ExecutionPlanContext executionPlanContext, boolean groupBy,
                                           int defaultStreamEventIndex, String queryName, Variable groupByVariable,
                                           GroupByKeyGeneratorForIncremental groupByKeyGenerator, Variable timeStampVariable) {
        return new IncrementalExecutor(TimePeriod.Duration.YEARS, child, functionAttributes,
                metaEvent, currentState, tableMap, executorList, executionPlanContext, groupBy,
                defaultStreamEventIndex, queryName, groupByVariable, groupByKeyGenerator, timeStampVariable);
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

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        IncrementalExecutor executor = (IncrementalExecutor) dataMap.get("windowProcessor");
//        executor.dispatchEvents();
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
//        long time = date.getTime();
        switch (this.duration) {
            case SECONDS:
                return currentTime-currentTime%1000 + 1000;
            case MINUTES:
                return currentTime-currentTime%60000 + 60000;
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

    private void initDefaultTable(Map<String, Table> tableMap, String aggregatorName) {
        tableDefinition = TableDefinition.id(aggregatorName+"_"+duration.toString());
        tableDefinition.attribute("symbol", Attribute.Type.STRING).
                attribute("sumprice1", Attribute.Type.FLOAT).
                attribute("countprice1", Attribute.Type.FLOAT).
                attribute("timestamp", Attribute.Type.LONG);
        MetaStreamEvent tableMetaStreamEvent = new MetaStreamEvent();
        tableMetaStreamEvent.addInputDefinition(tableDefinition);
        for (Attribute attribute : tableDefinition.getAttributeList()) {
            tableMetaStreamEvent.addOutputData(attribute);
        }

        StreamEventPool tableStreamEventPool = new StreamEventPool(tableMetaStreamEvent, 10);
        StreamEventCloner tableStreamEventCloner = new StreamEventCloner(tableMetaStreamEvent,
                tableStreamEventPool);
        ConfigReader configReader = null;
        inMemoryTable = new InMemoryTable();
        inMemoryTable.init(tableDefinition, tableStreamEventPool, tableStreamEventCloner, configReader,
                executionPlanContext);
        tableMap.putIfAbsent(tableDefinition.getId(), inMemoryTable);
    }

    private void createStreamEventCloner(MetaStreamEvent metaStreamEvent) {
        streamEventCloner = new StreamEventCloner(metaStreamEvent, new StreamEventPool(metaStreamEvent, 5));
    }

    private void setEventPopulator(MetaComplexEvent metaComplexEvent) {
        this.eventPopulator = StateEventPopulatorFactory.constructEventPopulator(metaComplexEvent);
    }

    private void processGroupBy(ComplexEventChunk complexEventChunk) {

        synchronized (this) {
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next(); //Type of event coming here is always CURRENT.
                String groupedByKey = groupByKeyGenerator.constructEventKey(event); // TODO: 5/30/17 original GroupByKeyGenerator was not used since constructEventKey is protected
                keyThreadLocal.set(groupedByKey);

                String groupByOutput = groupByExecutor.execute(event).toString();
                ConcurrentHashMap<String, Object> baseValuesPerGroupBy = new ConcurrentHashMap<>();
                for (ExpressionExecutorDetails basicExecutor:basicExecutorDetails) {
                    // TODO: 5/31/17 wrong value for minute when count is executed
                    baseValuesPerGroupBy.put(basicExecutor.getExecutorName(), basicExecutor.getExecutor().execute(event));
                }
                storeAggregatorFunctions.put(groupByOutput, baseValuesPerGroupBy);

                if (this.duration== TimePeriod.Duration.SECONDS ){
                    System.out.println(storeAggregatorFunctions);
                }

                keyThreadLocal.remove();
            }
        }
    }

    public static String getThreadLocalGroupByKey() {
        return keyThreadLocal.get();
    }

    private void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /*private void scheduleCronJob(String cronString, String elementId) {
        try {
            SchedulerFactory schedFact = new StdSchedulerFactory();
            scheduler = schedFact.getScheduler();
            jobName = "EventRemoverJob_" + elementId;
            JobKey jobKey = new JobKey(jobName, jobGroup);

            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
            scheduler.start();
            JobDataMap dataMap = new JobDataMap();
            dataMap.put("windowProcessor", this);

            JobDetail job = org.quartz.JobBuilder.newJob(IncrementalExecutor.class)
                    .withIdentity(jobName, jobGroup)
                    .usingJobData(dataMap)
                    .build();

            Trigger trigger = org.quartz.TriggerBuilder.newTrigger()
                    .withIdentity("EventRemoverTrigger_" + elementId, jobGroup)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronString))
                    .build();

            scheduler.scheduleJob(job, trigger);

        } catch (SchedulerException e) {
            throw new ExecutionPlanValidationException("");
        }
    }*/

    public void dispatchEvents(long timeStampOfBaseAggregate) {

        //Send RESET event to groupByExecutor
        for (ExpressionExecutorDetails basicExecutor:basicExecutorDetails) {
            basicExecutor.getExecutor().execute(resetEvent);
        }

        ComplexEventChunk<StreamEvent> newComplexEventChunk = new ComplexEventChunk<>();
        for (String groupByKey: storeAggregatorFunctions.keySet()) {
            //Create new on after window data to send aggregates to next iterator
            List<Object> newOnAfterWindowData = new ArrayList<>();
            newOnAfterWindowData.add(groupByKey);
            ConcurrentMap<String, Object> aggregatesPerGroupBy = storeAggregatorFunctions.remove(groupByKey);
            for (String aggregateKey : aggregatesPerGroupBy.keySet()) {
                // TODO: 5/27/17 this if else is a hack!!! change
                if (aggregateKey.startsWith("sum")) {
                    newOnAfterWindowData.add(((Double) aggregatesPerGroupBy.remove(aggregateKey)).floatValue());
                } else if (aggregateKey.startsWith("count")) {
                    newOnAfterWindowData.add(((Long) aggregatesPerGroupBy.remove(aggregateKey)).floatValue());
                }
            }
            newOnAfterWindowData.add(timeStampOfBaseAggregate); // TODO: 6/1/17 this needs to change

            StreamEvent newStream = new StreamEvent(0, onAfterWindowLength, 0);
            newStream.setTimestamp(timeStampOfBaseAggregate); // TODO: 5/31/17 we need to set the timestamp
            newStream.setOnAfterWindowData(newOnAfterWindowData.toArray());
            newComplexEventChunk.add(newStream);

            inMemoryTable.add(timeStampOfBaseAggregate, newOnAfterWindowData.toArray()); //write to table before sending to next executor
//            System.out.println(inMemoryTable.getElementId()+"..."+inMemoryTable.currentState());
        }
        if (getNextExecutor()!=null){
            getNextExecutor().execute(newComplexEventChunk);
        }
    }
}
