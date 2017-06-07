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

package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

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
import org.wso2.siddhi.core.table.holder.ListEventHolder;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.lock.LockWrapper;
import org.wso2.siddhi.core.util.parser.AggregationParser;
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

public class IncrementalExecutor implements Executor {
    // compositeAggregators contains a map of CompositeAggregator
    public List<CompositeAggregator> compositeAggregators;
    // basicExecutorDetails contains basic executors such as sum, count, and etc
    // we need to build composite aggregates.
    public List<AggregationParser.ExpressionExecutorDetails> basicExecutorDetails;
    public ExecutionPlanContext executionPlanContext;
    // groupByExecutor is used to get the value of group by clause
    private ExpressionExecutor groupByExecutor;
    private ExpressionExecutor externalTimeStampExecutor;
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
    private final StreamEvent resetEvent;
    private int onAfterWindowLength;

    public IncrementalExecutor(TimePeriod.Duration duration, IncrementalExecutor child,
                               MetaComplexEvent metaEvent,
                               Map<String, Table> tableMap,
                               ExecutionPlanContext executionPlanContext, String aggregatorName,
                               List<CompositeAggregator> compositeAggregators,
                               List<AggregationParser.ExpressionExecutorDetails> basicExecutorDetails,
                               ExpressionExecutor groupByExecutor, ExpressionExecutor externalTimeStampExecutor,
                               GroupByKeyGeneratorForIncremental groupByKeyGenerator) {
        this.duration = duration;
        this.child = child; // TODO: 6/2/17 do all this at parser and parse it here (Mostly done)
        this.compositeAggregators = compositeAggregators;
        this.basicExecutorDetails = basicExecutorDetails;
        this.groupByExecutor = groupByExecutor;
        this.externalTimeStampExecutor = externalTimeStampExecutor;

        storeAggregatorFunctions = new ConcurrentHashMap<>();
        this.executionPlanContext = executionPlanContext;
        this.groupByKeyGenerator = groupByKeyGenerator;
        this.onAfterWindowLength = ((MetaStreamEvent)metaEvent).getOnAfterWindowData().size();

        //Create a dummy event to reset aggregates
        this.resetEvent = new StreamEvent(0,onAfterWindowLength,0);
        resetEvent.setType(ComplexEvent.Type.RESET);

        setNextExecutor();

        initDefaultTable(tableMap, aggregatorName);

        createStreamEventCloner((MetaStreamEvent) metaEvent);

        setEventPopulator(metaEvent);

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
//                scheduler.notifyAt(nextEmitTime); // TODO: 6/2/17 have scheduler at receiver
                }
                if (externalTimeStamp > nextEmitTime) {
                    long timeStampOfBaseAggregate = getStartTime(nextEmitTime);
                    nextEmitTime = getNextEmitTime(externalTimeStamp);
                    // TODO: 6/2/17 don't send events as soon as time elapses (to allow for out of order events)
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

    private long getNextEmitTime(long currentTime) {
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
                attribute("sumprice1", Attribute.Type.DOUBLE).
                attribute("countprice1", Attribute.Type.DOUBLE).
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
                for (AggregationParser.ExpressionExecutorDetails basicExecutor:basicExecutorDetails) {
                    // TODO: 5/31/17 wrong value for minute when count is executed
                    baseValuesPerGroupBy.put(basicExecutor.getExecutorName(), basicExecutor.getExecutor().execute(event));
                }
                storeAggregatorFunctions.put(groupByOutput, baseValuesPerGroupBy);
                // TODO: 6/2/17 we should be able to get time as well. Hence, may have to change this representation
                if (this.duration== TimePeriod.Duration.MINUTES){
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

    public void dispatchEvents(long timeStampOfBaseAggregate) {

        //Send RESET event to groupByExecutor
        // TODO: 6/2/17 call reset method (can't reset since GroupByAggregationAttributeExecutor is called)

        for (AggregationParser.ExpressionExecutorDetails basicExecutor:basicExecutorDetails) {
            basicExecutor.getExecutor().execute(resetEvent);
        }

        ComplexEventChunk<StreamEvent> newComplexEventChunk = new ComplexEventChunk<>();
        for (String groupByKey: storeAggregatorFunctions.keySet()) {
            //Create new on after window data to send aggregates to next iterator
            List<Object> newOnAfterWindowData = new ArrayList<>();
            newOnAfterWindowData.add(groupByKey);
            ConcurrentMap<String, Object> aggregatesPerGroupBy = storeAggregatorFunctions.remove(groupByKey);
            for (String aggregateKey : aggregatesPerGroupBy.keySet()) {
                newOnAfterWindowData.add(aggregatesPerGroupBy.get(aggregateKey));
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
