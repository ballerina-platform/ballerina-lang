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
import org.wso2.siddhi.core.query.selector.attribute.processor.executor.GroupByAggregationAttributeExecutor;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.IncrementalTimeConverterUtil;
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

    private final StreamEvent resetEvent;
    private final ExpressionExecutor timestampExpressionExecutor;
    private TimePeriod.Duration duration;
    private Table table;
    private SiddhiAppContext siddhiAppContext;
    private String aggregatorName;
    private GroupByKeyGenerator groupByKeyGenerator;
    private int bufferSize;
    private StreamEventPool streamEventPool;
    private long nextEmitTime = -1;
    private boolean isProcessingOnExternalTime;
    private int currentBufferIndex = 0;
    private long startTimeOfAggregates = -1;
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
                               boolean isRoot, Table table, SiddhiAppContext siddhiAppContext,
                               boolean isProcessingOnExternalTime) {
        this.duration = duration;
        this.next = child;
        this.isRoot = isRoot;
        this.table = table;
        this.siddhiAppContext = siddhiAppContext;
        this.aggregatorName = aggregatorName;
        this.bufferSize = bufferSize;
        this.streamEventPool = new StreamEventPool(metaStreamEvent, 10);
        this.isProcessingOnExternalTime = isProcessingOnExternalTime;
        this.timestampExpressionExecutor = processExpressionExecutors.remove(0);
        this.baseIncrementalValueStore = new BaseIncrementalValueStore(-1, processExpressionExecutors);

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
                    baseIncrementalValueStoreList.add(baseIncrementalValueStore.cloneStore(null, -1));
                }
            }
        }

        this.resetEvent = streamEventPool.borrowEvent();
        this.resetEvent.setType(ComplexEvent.Type.RESET);
        setNextExecutor(child);
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void execute(ComplexEventChunk streamEventChunk) {
        streamEventChunk.reset();
        while (streamEventChunk.hasNext()) {
            StreamEvent streamEvent = (StreamEvent) streamEventChunk.next();
            streamEventChunk.remove();

//            LOG.info(duration + "..." + streamEvent.getType());
            long timestamp = getTimestamp(streamEvent);
            if (timestamp >= nextEmitTime) {
                nextEmitTime = IncrementalTimeConverterUtil.getNextEmitTime(timestamp, duration);
                startTimeOfAggregates = IncrementalTimeConverterUtil.getStartTimeOfAggregates(timestamp, duration);
                dispatchAggregateEvents(startTimeOfAggregates, timestamp);
                sendTimerEvent(streamEvent, timestamp);
            }

            if (streamEvent.getType() == ComplexEvent.Type.CURRENT) {
                processAggregates(streamEvent);
            }
        }
    }

    private void sendTimerEvent(StreamEvent streamEvent, long timestamp) {
        if (streamEvent.getType() == ComplexEvent.Type.TIMER && getNextExecutor() != null) {
            StreamEvent timerEvent = streamEventPool.borrowEvent();
            timerEvent.setType(ComplexEvent.Type.TIMER);
            timerEvent.setTimestamp(IncrementalTimeConverterUtil.getEmitTimeOfLastEventToRemove(timestamp,
                    this.duration, this.bufferSize));
            ComplexEventChunk<StreamEvent> timerStreamEventChunk = new ComplexEventChunk<>(true);
            timerStreamEventChunk.add(timerEvent);
            next.execute(timerStreamEventChunk);
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
                    GroupByAggregationAttributeExecutor.getKeyThreadLocal().set(groupedByKey);
                    if (baseIncrementalValueGroupByStoreList != null) {
                        Map<String, BaseIncrementalValueStore> baseIncrementalValueGroupByStore =
                                baseIncrementalValueGroupByStoreList.get(currentBufferIndex);
                        BaseIncrementalValueStore aBaseIncrementalValueStore =
                                baseIncrementalValueGroupByStore.computeIfAbsent(groupedByKey,
                                        k -> baseIncrementalValueStore.cloneStore(k, startTimeOfAggregates));
                        process(streamEvent, aBaseIncrementalValueStore);
                    } else {
                        BaseIncrementalValueStore aBaseIncrementalValueStore =
                                baseIncrementalValueStoreMap.computeIfAbsent(groupedByKey,
                                        k -> baseIncrementalValueStore.cloneStore(k, startTimeOfAggregates));
                        process(streamEvent, aBaseIncrementalValueStore);
                    }
                } finally {
                    GroupByAggregationAttributeExecutor.getKeyThreadLocal().remove();
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
    }

    private void dispatchEvent(long startTimeOfNewAggregates, BaseIncrementalValueStore aBaseIncrementalValueStore) {
        if (aBaseIncrementalValueStore.isProcessed) {
            StreamEvent streamEvent = createStreamEvent(aBaseIncrementalValueStore);
            ComplexEventChunk<StreamEvent> eventChunk = new ComplexEventChunk<>(true);
            eventChunk.add(streamEvent);
            table.addEvents(eventChunk);
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
            table.addEvents(eventChunk);
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

    private class BaseIncrementalValueStore {
        private long timestamp; // This is the starting timeStamp of aggregates
        private Object[] values;
        private List<ExpressionExecutor> expressionExecutors;
        private boolean isProcessed = false;

        public BaseIncrementalValueStore(long timeStamp, List<ExpressionExecutor> expressionExecutors) {
            this.timestamp = timeStamp;
            this.values = new Object[expressionExecutors.size() + 1];
            this.expressionExecutors = expressionExecutors;
        }

        public void clearValues() {
            this.values = new Object[expressionExecutors.size() + 1];
        }

        public void setValue(Object value, int position) {
            values[position] = value;
        }

        public BaseIncrementalValueStore cloneStore(String key, long timestamp) {
            List<ExpressionExecutor> newExpressionExecutors = new ArrayList<>(expressionExecutors.size());
            expressionExecutors.forEach(expressionExecutor ->
                    newExpressionExecutors.add(expressionExecutor.cloneExecutor(key)));
            return new BaseIncrementalValueStore(timestamp, newExpressionExecutors);
        }

    }
}
