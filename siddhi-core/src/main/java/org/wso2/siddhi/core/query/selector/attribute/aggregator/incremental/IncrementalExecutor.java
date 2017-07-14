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

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.selector.GroupByKeyGenerator;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Incremental executor class which is responsible for performing incremental aggregation
 */
public class IncrementalExecutor implements Executor {
    static final Logger LOG = Logger.getLogger(IncrementalExecutor.class);

    private static final ThreadLocal<String> keyThreadLocal = new ThreadLocal<>();
    private final StreamEvent resetEvent;
    private final ExpressionExecutor timestampExpressionExecutor;
    private TimePeriod.Duration duration;
    private SiddhiAppContext siddhiAppContext;
    private String aggregatorName;
    private GroupByKeyGenerator groupByKeyGenerator;
    private int bufferSize;
    private StreamEventPool streamEventPool;
    private long nextEmitTime = -1;
    private boolean isProcessingOnExternalTime;
    private int currentBufferIndex = 0;
    private boolean timerStarted = false;
    private boolean isGroupBy;
    private Executor next;
    private Scheduler scheduler;
    private boolean isRoot;

    private BaseIncrementalValueStore baseIncrementalValueStore = null;
    private Map<String, BaseIncrementalValueStore> baseIncrementalValueStoreMap = null;
    private ArrayList<BaseIncrementalValueStore> baseIncrementalValueStoreList = null;
    private ArrayList<HashMap<String, BaseIncrementalValueStore>> baseIncrementalValueGroupByStoreList = null;


    public IncrementalExecutor(TimePeriod.Duration duration, List<ExpressionExecutor> processExpressionExecutors,
                               GroupByKeyGenerator groupByKeyGenerator, MetaStreamEvent metaStreamEvent,
                               int bufferSize, String aggregatorName, IncrementalExecutor child,
                               boolean isRoot, SiddhiAppContext siddhiAppContext, boolean isProcessingOnExternalTime) {
        this.duration = duration;
        this.next = child;
        this.isRoot = isRoot;
        this.siddhiAppContext = siddhiAppContext;
        this.aggregatorName = aggregatorName;
        this.bufferSize = bufferSize;
        this.streamEventPool = new StreamEventPool(metaStreamEvent, 10);
        this.isProcessingOnExternalTime = isProcessingOnExternalTime;
        this.timestampExpressionExecutor = processExpressionExecutors.remove(0);
        this.baseIncrementalValueStore = new BaseIncrementalValueStore(processExpressionExecutors);

        if (groupByKeyGenerator != null) {
            this.groupByKeyGenerator = groupByKeyGenerator;
            isGroupBy = true;
            if (bufferSize > 0) {
                baseIncrementalValueGroupByStoreList = new ArrayList<>(bufferSize + 1);
                for (int i = 0; i < bufferSize + 1; i++) {
                    baseIncrementalValueGroupByStoreList.add(new HashMap<>());
                }
            } else {
                baseIncrementalValueStoreMap = new HashMap<>();
            }
        } else {
            isGroupBy = false;
            if (bufferSize > 0) {
                baseIncrementalValueStoreList = new ArrayList<BaseIncrementalValueStore>(bufferSize + 1);
                for (int i = 0; i < bufferSize + 1; i++) {
                    baseIncrementalValueStoreList.add(baseIncrementalValueStore.cloneStore(null));
                }
            }
        }

        this.resetEvent = streamEventPool.borrowEvent();
        this.resetEvent.setType(ComplexEvent.Type.RESET);
        setNextExecutor(child);
    }

    public static String getThreadLocalGroupByKey() {
        return keyThreadLocal.get();
    }

//    public void setRoot() {
//        this.isRoot = true;
//    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
//
//    public void setPoolOfExecutors(Queue<List<AggregationParser.ExpressionExecutorDetails>>
//                                           poolOfExtraBaseExpressionExecutors) {
//        this.poolOfExtraBaseExpressionExecutors = poolOfExtraBaseExpressionExecutors;
//    }
//
//    public void resetAggregatorStore() {
//        this.runningBaseAggregatorCollection = new HashMap<>();
//    }

    @Override
    public void execute(ComplexEventChunk streamEventChunk) {
        while (streamEventChunk.hasNext()) {
            StreamEvent streamEvent = (StreamEvent) streamEventChunk.next();
            streamEventChunk.remove();

            LOG.info(duration + "..." + streamEvent.getType());

            // Create new chunk to hold one stream event only
            // ComplexEventChunk<StreamEvent> newEventChunk = new ComplexEventChunk<>(streamEvent, streamEvent,
            //       streamEventChunk.isBatch());
            long timestamp = getTimestamp(streamEvent);

            if (timestamp >= nextEmitTime) {
                nextEmitTime = IncrementalTimeConverterUtil.getNextEmitTime(timestamp, duration);
                long startTimeOfAggregates = IncrementalTimeConverterUtil.getStartTimeOfAggregates(timestamp, duration);
                dispatchAggregateEvents(startTimeOfAggregates, timestamp);
                sendTimerEvent(streamEvent, timestamp);
            }

            if (streamEvent.getType() == ComplexEvent.Type.CURRENT) {
                processAggregates(streamEvent);

//                for (Map.Entry<String, BaseIncrementalValueStore> x : runningBaseAggregatorCollection.entrySet()) {
//                    String a = "___";
//                    for (Object z : x.getValue().getBaseIncrementalValues()) {
//                        a = a.concat(z.toString() + "___");
//                    }
//                    LOG.info(this.duration + "...." + isRoot + "..." + x.getValue().getKey() + "...." + a);
//
//                }

            }
        }
    }

    private void sendTimerEvent(StreamEvent streamEvent, long timestamp) {
        if (streamEvent.getType() == ComplexEvent.Type.TIMER && getNextExecutor() != null) {
//                    if (bufferSize > 0) {
//                        if (timerStarted) {
//                            // Send TIMER event to next executor.
//                            // Timer events must be sent from root only after sending atleast 1
//                            // event from buffer, to next executor
//                            StreamEvent timerEvent = streamEventPool.borrowEvent();
//                            timerEvent.setType(ComplexEvent.Type.TIMER);
//                            timerEvent.setTimestamp(IncrementalTimeConverterUtil
//                                    .getEmitTimeOfLastEventToRemove(timestamp, this.duration, this.bufferSize));
//                            timerStreamEventChunk.add(timerEvent);
//                            getNextExecutor().execute(timerStreamEventChunk);
//                            timerStreamEventChunk.clear();
//                        }
//                    } else {
            // Send TIMER event to next executor.
            StreamEvent timerEvent = streamEventPool.borrowEvent();
            timerEvent.setType(ComplexEvent.Type.TIMER);
            timerEvent.setTimestamp(IncrementalTimeConverterUtil.getEmitTimeOfLastEventToRemove(timestamp,
                    this.duration, this.bufferSize));
            ComplexEventChunk<StreamEvent> timerStreamEventChunk = new ComplexEventChunk<>(true);
            timerStreamEventChunk.add(timerEvent);
            next.execute(timerStreamEventChunk);
//                    }
        }
    }

    private long getTimestamp(StreamEvent streamEvent) {
        long timestamp;
        if (streamEvent.getType() == ComplexEvent.Type.CURRENT) {
            timestamp = (long) timestampExpressionExecutor.execute(streamEvent);
            if (isRoot && !isProcessingOnExternalTime && !timerStarted) {
                scheduler.notifyAt(IncrementalTimeConverterUtil.getNextEmitTime(timestamp, duration));
                timerStarted = true;
            }
        } else {
            // TIMER event has arrived. A timer event never arrives for external timeStamp based execution
            timestamp = streamEvent.getTimestamp();
            if (isRoot) {
                // Scheduling is done by root incremental executor only
                scheduler.notifyAt(IncrementalTimeConverterUtil.getNextEmitTime(timestamp, duration));
            }
        }
        return timestamp;
    }

    @Override
    public Executor getNextExecutor() {
        return next;
    }

    @Override
    public void setNextExecutor(Executor nextExecutor) {
        next = nextExecutor;
    }

    private void processAggregates(StreamEvent streamEvent) {
        synchronized (this) {
            if (isGroupBy) {
                try {
                    String groupedByKey = groupByKeyGenerator.constructEventKey(streamEvent);
                    keyThreadLocal.set(groupedByKey);
                    if (baseIncrementalValueGroupByStoreList != null) {
                        Map<String, BaseIncrementalValueStore> baseIncrementalValueGroupByStore =
                                baseIncrementalValueGroupByStoreList.get(currentBufferIndex);
                        BaseIncrementalValueStore aBaseIncrementalValueStore =
                                baseIncrementalValueGroupByStore.computeIfAbsent(groupedByKey,
                                        k -> baseIncrementalValueStore.cloneStore(k));
                        process(streamEvent, aBaseIncrementalValueStore);
                    } else {
                        BaseIncrementalValueStore aBaseIncrementalValueStore =
                                baseIncrementalValueStoreMap.computeIfAbsent(groupedByKey,
                                        k -> baseIncrementalValueStore.cloneStore(k));
                        process(streamEvent, aBaseIncrementalValueStore);
                    }
                } finally {
                    keyThreadLocal.remove();
                }
            } else {
                if (baseIncrementalValueStoreList != null) {
                    BaseIncrementalValueStore aBaseIncrementalValueStore =
                            baseIncrementalValueStoreList.get(currentBufferIndex);
                    process(streamEvent, aBaseIncrementalValueStore);
                } else {
                    process(streamEvent, baseIncrementalValueStore);
                }
            }

//
//            String groupedByKey = null;
//            if (isGroupBy) {
//                groupedByKey = groupByKeyGenerator.constructEventKey(streamEvent);
//                keyThreadLocal.set(groupedByKey);
//            }
//            try {
//                if (isRoot && bufferSize > 0) {
//                    // get time at which the incoming event should have expired
//                    // get relevant base map corresponding to that expiry time
//                    // update aggregates of that map
//                    if (emitTimeOfEvent == nextEmitTime) {
//                        updateRunningBaseAggregatorCollection(groupedByKey, streamEvent, emitTimeOfEvent);
//                    } else {
//                        Map<String, BaseIncrementalValueStore> bufferedBaseAggregatorCollection
//                                = bufferedBaseAggregatorMap.get(emitTimeOfEvent);
//                        if (bufferedBaseAggregatorCollection != null) {
//                            updateBufferedBaseAggregatorCollection(groupedByKey, event,
//                                    bufferedBaseAggregatorCollection, timeStamp,
//                                    basicExecutorsOfBufferedEvents.get(emitTimeOfEvent));
//
//                        } else if (((TreeMap<Long, Map<String, BaseIncrementalValueStore>>)
//                                bufferedBaseAggregatorMap).firstKey() < emitTimeOfEvent) {
//                            // An event which is newer than oldest buffered event, but older than current.
//                            // Event should be buffered. But no events belonging to this time period have
//                            // previously arrived. Hence, bufferedBaseAggregatorCollection gives null.
//                            Map<String, BaseIncrementalValueStore> newBaseAggregatorCollection = new HashMap<>();
//                            List<AggregationParser.ExpressionExecutorDetails> borrowedBaseExecutors
//                                    = poolOfExtraBaseExpressionExecutors.poll();
//                            basicExecutorsOfBufferedEvents.put(emitTimeOfEvent, borrowedBaseExecutors);
//                            bufferedBaseAggregatorMap.put(emitTimeOfEvent, newBaseAggregatorCollection);
//                            updateBufferedBaseAggregatorCollection(groupedByKey, event, newBaseAggregatorCollection,
//                                    timeStamp, borrowedBaseExecutors);
//
//                        } else {
//                            // The incoming event is older than buffered data
//                            // TODO: 6/26/17 in this case process event with current event or oldest buffered event?
//                            updateRunningBaseAggregatorCollection(groupedByKey, event, emitTimeOfEvent);
//                        }
//                    }
//                } else {
//                    updateRunningBaseAggregatorCollection(groupedByKey, streamEvent, null);
//                }
//
//            } finally {
//                if (isGroupBy) {
//                    keyThreadLocal.remove();
//                }
//            }
        }
    }

    private void process(StreamEvent streamEvent, BaseIncrementalValueStore baseIncrementalValueStore) {
        List<ExpressionExecutor> expressionExecutors = baseIncrementalValueStore.expressionExecutors;
        for (int i = 0; i < expressionExecutors.size(); i++) { //keeping timestamp value location as null
            ExpressionExecutor expressionExecutor = expressionExecutors.get(i);
            baseIncrementalValueStore.setValue(expressionExecutor.execute(streamEvent), i + 1);
        }
        baseIncrementalValueStore.isProcessed = true;
    }


//    private void updateRunningBaseAggregatorCollection(String groupedByKey, ComplexEvent event,
// Long emitTimeOfEvent) {
//        BaseIncrementalValueStore baseIncrementalValueStore;
//        if (isGroupBy) {
//            baseIncrementalValueStore = this.baseIncrementalValueStore;
//        } else {
//            baseIncrementalValueStore = baseIncrementalValueStoreMap.get(groupedByKey);
//            if (baseIncrementalValueStore == null) {
//                baseIncrementalValueStore = this.baseIncrementalValueStore.cloneStore(groupedByKey);
//                if (baseIncrementalValueStoreMap.putIfAbsent(groupedByKey, baseIncrementalValueStore) != null) {
//                    baseIncrementalValueStore = baseIncrementalValueStoreMap.get(groupedByKey);
//                }
//            }
//        }
//        if (emitTimeOfEvent != null) {
//            baseIncrementalValueStore.setTimeStamp(emitTimeOfEvent);
//        }
//        List<ExpressionExecutor> expressionExecutors = baseIncrementalValueStore.getExpressionExecutors();
//        for (int i = 0; i < expressionExecutors.size(); i++) {//keeping timestamp value location as null
//            ExpressionExecutor expressionExecutor = expressionExecutors.get(i);
//            baseIncrementalValueStore.setValue(expressionExecutor.execute(event), i + 1);
//        }
//    }

//    private void updateBufferedBaseAggregatorCollection(String groupedByKey,
//                                                        ComplexEvent event,
//                                                        Map<String, BaseIncrementalValueStore>
//                                                                bufferedBaseAggregatorCollection,
//                                                        long timeStamp,
//                                                        List<AggregationParser.ExpressionExecutorDetails>
//                                                                basicExecutorsOfBufferedEvents) {
//        BaseIncrementalValueStore bufferedBaseAggregator = bufferedBaseAggregatorCollection.get(groupedByKey);
//        if (bufferedBaseAggregator == null) {
//            bufferedBaseAggregator = new BaseIncrementalValueStore(timeStamp, groupedByKey,
//                    genericExpressionExecutors.size(), basicExecutorsOfBufferedEvents.size());
//            bufferedBaseAggregatorCollection.put(groupedByKey, bufferedBaseAggregator);
//        }
//        for (int i = 0; i < genericExpressionExecutors.size(); i++) {
//            bufferedBaseAggregator.setValue(genericExpressionExecutors.get(i).execute(event), i);
//        }
//        for (int i = 0; i < basicExecutorsOfBufferedEvents.size(); i++) {
//            bufferedBaseAggregator
//                    .setBaseIncrementalValue(basicExecutorsOfBufferedEvents.get(i).getExecutor().execute(event), i);
//        }
//    }

    private void dispatchAggregateEvents(long startTimeOfNewAggregates, long currentTimeStamp) {

        if (isGroupBy) {
            if (baseIncrementalValueGroupByStoreList != null) {
                int dispatchIndex = currentBufferIndex + 1;
                if (dispatchIndex > bufferSize) {
                    dispatchIndex -= bufferSize + 1;
                }
                Map<String, BaseIncrementalValueStore> baseIncrementalValueGroupByStore =
                        baseIncrementalValueGroupByStoreList.get(dispatchIndex);
                dispatchEvents(baseIncrementalValueGroupByStore);
                currentBufferIndex = dispatchIndex;
            } else {
                dispatchEvents(baseIncrementalValueStoreMap);
            }
        } else {
            if (baseIncrementalValueStoreList != null) {
                int dispatchIndex = currentBufferIndex + 1;
                if (dispatchIndex > bufferSize) {
                    dispatchIndex -= bufferSize + 1;
                }
                BaseIncrementalValueStore aBaseIncrementalValueStore = baseIncrementalValueStoreList.get(dispatchIndex);
                dispatchEvent(startTimeOfNewAggregates, aBaseIncrementalValueStore);
                currentBufferIndex = dispatchIndex;
            } else {
                dispatchEvent(startTimeOfNewAggregates, baseIncrementalValueStore);
            }
        }
//        if (bufferSize > 0) {
//            // Add current basicExecutorDetails to basicExecutorsOfBufferedEvents
//            basicExecutorsOfBufferedEvents.put(copyOfEmitTime, basicExecutorDetails);
//            // Add current base aggregator collection to buffer
//            bufferedBaseAggregatorMap.put(copyOfEmitTime, runningBaseAggregatorCollection);
//            // Reset running base aggregator collection
//            resetAggregatorStore();
//            if (isExternalTimeStampBased) {
//                // When external timestamp is used, there could be instances where events which
//                // were supposed to have expired earlier, still remain in the buffer, due to
//                // events not arriving at the end of each duration period (e.g. For sec window
//                // events may not arrive for several seconds. Therefore, there could be several
//                // events in the buffer, which should have expired earlier. All such events must
//                // be dispatched.
//
//                // Remove oldest base executors from basicExecutorsOfBufferedEvents.
//                // This would remove all values corresponding to key equal to or less than
//                // "EmitTimeOfLastEventToRemove"
//                // Those executors would be reset to be used later when out of order events arrive.
//
//                NavigableMap<Long, List<AggregationParser.ExpressionExecutorDetails>> removedExecutors
//                        = ((TreeMap<Long, List<AggregationParser.ExpressionExecutorDetails>>)
//                        basicExecutorsOfBufferedEvents).headMap(IncrementalTimeConverterUtil.
//                        getEmitTimeOfLastEventToRemove(currentTimeStamp,
//                                this.duration, this.bufferSize), true);
//                for (Map.Entry<Long, List<AggregationParser.ExpressionExecutorDetails>>
//                        removedExecutorList : removedExecutors.entrySet()) {
//                    if (poolOfExtraBaseExpressionExecutors.size() >= bufferSize) {
//                        break;
//                    }
//                    for (AggregationParser.ExpressionExecutorDetails removedExecutor :
// removedExecutorList.getValue()) {
//                        removedExecutor.getExecutor().execute(resetEvent);
//                    }
//                    poolOfExtraBaseExpressionExecutors.add(removedExecutorList.getValue());
//                }
//                removedExecutors.clear();
//                // Remove oldest base aggregator collections from bufferedBaseAggregatorMap
//                NavigableMap<Long, Map<String, BaseIncrementalValueStore>> mapOfBaseAggregatesToDispatch
//                        = ((TreeMap<Long, Map<String, BaseIncrementalValueStore>>) bufferedBaseAggregatorMap)
//                        .headMap(IncrementalTimeConverterUtil.getEmitTimeOfLastEventToRemove(currentTimeStamp,
//                                this.duration, this.bufferSize), true);
//
//                // Send oldest base aggregator collection to next executor
//                if (mapOfBaseAggregatesToDispatch != null) {
//                    // Null check is done, since if the buffer is not filled yet,
//                    // there's no requirement to send oldest event
//                    for (Map.Entry<Long, Map<String, BaseIncrementalValueStore>>
//                            baseAggregatesToDispatch : mapOfBaseAggregatesToDispatch.entrySet()) {
//                        sendToNextExecutor(baseAggregatesToDispatch.getValue());
//                    }
//                    mapOfBaseAggregatesToDispatch.clear();
//                }
//                // Set new basicExecutorDetails from pool
//                basicExecutorDetails = poolOfExtraBaseExpressionExecutors.poll();
// TODO: 7/10/17 why doesn't this work
//                // for event time?
//            } else {
//                // Remove oldest base executors from basicExecutorsOfBufferedEvents
//                basicExecutorsOfBufferedEvents.remove(IncrementalTimeConverterUtil
//                        .getEmitTimeOfLastEventToRemove(currentTimeStamp, this.duration, this.bufferSize));
//
//                // Remove oldest base aggregator collection from bufferedBaseAggregatorMap
//                Map<String, BaseIncrementalValueStore> baseAggregatesToDispatch = bufferedBaseAggregatorMap
//                        .remove(IncrementalTimeConverterUtil.getEmitTimeOfLastEventToRemove(currentTimeStamp,
//                                this.duration, this.bufferSize));
//
//                // Send oldest base aggregator collection to next executor
//                if (baseAggregatesToDispatch != null) {
//                    // Null check is done, since if the buffer is not filled yet, there's no requirement to
// send oldest
//                    // event
//                    sendToNextExecutor(baseAggregatesToDispatch);
//                    // A timer event must be sent from root executor, only after dispatching atleast one event
//                    // from the buffer. Following bool is used to check that.
//                    timerStarted = true;
//                }
//                // Clone basicExecutorDetails and set as new basic executors
//                List<AggregationParser.ExpressionExecutorDetails> newBasicExecutorDetails = new ArrayList<>();
//                for (AggregationParser.ExpressionExecutorDetails basicExecutor : this.basicExecutorDetails) {
//                    newBasicExecutorDetails.add(new AggregationParser.ExpressionExecutorDetails(
//                            basicExecutor.getExecutor().cloneExecutor("clone"), basicExecutor.getExecutorName()));
//                }
//                basicExecutorDetails = newBasicExecutorDetails;
//            }
//        } else {
//            sendToNextExecutor(runningBaseAggregatorCollection);
//            // Reset running base aggregator collection
//            runningBaseAggregatorCollection = new HashMap<>();
//            // Send RESET event to groupByExecutor
//            if (!isGroupBy) {
//                for (ExpressionExecutor expressionExecutor : processExpressionExecutors) {
//                    expressionExecutor.execute(resetEvent);
//                }
//            } else {
//                // TODO: 7/13/17
//            }
//        }
    }

    private void dispatchEvent(long startTimeOfNewAggregates, BaseIncrementalValueStore aBaseIncrementalValueStore) {
        if (aBaseIncrementalValueStore.isProcessed) {
            StreamEvent streamEvent = createStreamEvent(aBaseIncrementalValueStore);
            ComplexEventChunk<StreamEvent> eventChunk = new ComplexEventChunk<>(true);
            eventChunk.add(streamEvent);
            next.execute(eventChunk);
        }
        cleanBaseIncrementalValueStore(startTimeOfNewAggregates, aBaseIncrementalValueStore);
    }

    private void dispatchEvents(Map<String, BaseIncrementalValueStore> baseIncrementalValueGroupByStore) {
        if (baseIncrementalValueGroupByStore.size() > 0) {
            ComplexEventChunk<StreamEvent> eventChunk = new ComplexEventChunk<>(true);
            for (BaseIncrementalValueStore aBaseIncrementalValueStore : baseIncrementalValueGroupByStore.values()) {
                StreamEvent streamEvent = createStreamEvent(aBaseIncrementalValueStore);
                eventChunk.add(streamEvent);
            }
            next.execute(eventChunk);
        }
        baseIncrementalValueGroupByStore.clear();
    }

    private StreamEvent createStreamEvent(BaseIncrementalValueStore aBaseIncrementalValueStore) {
        StreamEvent streamEvent = streamEventPool.borrowEvent();
        streamEvent.setTimestamp(aBaseIncrementalValueStore.timestamp);
        aBaseIncrementalValueStore.values[0] = aBaseIncrementalValueStore.timestamp;
        streamEvent.setOutputData(aBaseIncrementalValueStore.values);
        return streamEvent;
    }

    private void cleanBaseIncrementalValueStore(long startTimeOfNewAggregates,
                                                BaseIncrementalValueStore baseIncrementalValueStore) {
        baseIncrementalValueStore.clearValues();
        baseIncrementalValueStore.timestamp = startTimeOfNewAggregates;
        baseIncrementalValueStore.isProcessed = false;
        for (ExpressionExecutor expressionExecutor : baseIncrementalValueStore.expressionExecutors) {
            expressionExecutor.execute(resetEvent);
        }
    }

//    private void sendToNextExecutor(Map<String, BaseIncrementalValueStore> baseAggregatesToDispatch) {
//        InMemoryTable inMemoryTable = ((InMemoryTable) tableMap.get(aggregatorName + "_" + this.duration.toString()));
//        ComplexEventChunk<StreamEvent> newComplexEventChunk;
//        for (Map.Entry<String, BaseIncrementalValueStore> baseAggregateToDispatch : baseAggregatesToDispatch
//                .entrySet()) {
//            StreamEvent streamEvent = streamEventPool.borrowEvent();
//            streamEvent.getOnAfterWindowData()[0] = baseAggregateToDispatch.getValue().getTimeStamp();
//            int i = 1;
//            if (isGroupBy) {
//                String[] groupByValues = baseAggregateToDispatch.getValue().getKey().split("::");
//                for (String groupByValue : groupByValues) {
//                    streamEvent.getOnAfterWindowData()[i] = groupByValue;
//                    i++;
//                }
//            }
//            for (Object genericValues : baseAggregateToDispatch.getValue().getValues()) {
//                streamEvent.getOnAfterWindowData()[i] = genericValues;
//                i++;
//            }
//            for (Object baseIncrementalValue : baseAggregateToDispatch.getValue().getBaseIncrementalValues()) {
//                streamEvent.getOnAfterWindowData()[i] = baseIncrementalValue;
//                i++;
//            }
//            newComplexEventChunk = new ComplexEventChunk<>(streamEvent, streamEvent, false);
//            inMemoryTable.add(baseAggregateToDispatch.getValue().getTimeStamp(), streamEvent.getOnAfterWindowData());
//            LOG.info(inMemoryTable.getElementId() + "........" + inMemoryTable.currentState());
//            // TODO: 6/13/17 table may not always be there?
//            if (getNextExecutor() != null) {
//                getNextExecutor().execute(newComplexEventChunk);
//            }
//        }
//    }

    private class BaseIncrementalValueStore {
        private long timestamp; // This is the starting timeStamp of aggregates
        private Object[] values;
        private List<ExpressionExecutor> expressionExecutors;
        private boolean isProcessed = false;

        public BaseIncrementalValueStore(List<ExpressionExecutor> expressionExecutors) {
            this(-1, expressionExecutors);
        }

        private BaseIncrementalValueStore(long timeStamp, List<ExpressionExecutor> expressionExecutors) {
            this.timestamp = timeStamp;
            this.values = new Object[expressionExecutors.size()];
            this.expressionExecutors = expressionExecutors;
        }

        public void clearValues() {
            this.values = new Object[expressionExecutors.size()];
        }

        public void setValue(Object value, int position) {
            values[position] = value;
        }

        public BaseIncrementalValueStore cloneStore(String key) {
            List<ExpressionExecutor> newExpressionExecutors = new ArrayList<>(expressionExecutors.size());
            expressionExecutors.forEach(expressionExecutor ->
                    newExpressionExecutors.add(expressionExecutor.cloneExecutor(key)));
            return new BaseIncrementalValueStore(newExpressionExecutors);
        }

    }
}
