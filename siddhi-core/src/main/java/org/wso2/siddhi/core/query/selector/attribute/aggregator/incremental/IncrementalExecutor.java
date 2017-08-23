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

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Incremental executor class which is responsible for performing incremental aggregation
 */
public class IncrementalExecutor implements Executor {
    private static final Logger LOG = Logger.getLogger(IncrementalExecutor.class);

    private final StreamEvent resetEvent;
    private final ExpressionExecutor timestampExpressionExecutor;
    private final ExpressionExecutor timeZoneExpressionExecutor;
    private TimePeriod.Duration duration;
    private Table table;
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
            GroupByKeyGenerator groupByKeyGenerator, MetaStreamEvent metaStreamEvent, int bufferSize,
            IncrementalExecutor child, boolean isRoot, Table table, boolean isProcessingOnExternalTime) {
        this.duration = duration;
        this.next = child;
        this.isRoot = isRoot;
        this.table = table;
        this.bufferSize = bufferSize;
        this.streamEventPool = new StreamEventPool(metaStreamEvent, 10);
        this.isProcessingOnExternalTime = isProcessingOnExternalTime;
        this.timestampExpressionExecutor = processExpressionExecutors.remove(0);
        this.timeZoneExpressionExecutor = processExpressionExecutors.get(0);
        this.baseIncrementalValueStore = new BaseIncrementalValueStore(-1, processExpressionExecutors, streamEventPool);

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
                baseIncrementalValueStoreList = new ArrayList<>(bufferSize + 1);
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
        LOG.debug("Event Chunk received by " + this.duration + " incremental executor: " + streamEventChunk.toString());
        streamEventChunk.reset();
        while (streamEventChunk.hasNext()) {
            StreamEvent streamEvent = (StreamEvent) streamEventChunk.next();
            streamEventChunk.remove();

            String timeZone = getTimeZone(streamEvent);
            long timestamp = getTimestamp(streamEvent, timeZone);
            if (timestamp >= nextEmitTime) {
                nextEmitTime = IncrementalTimeConverterUtil.getNextEmitTime(timestamp, duration, timeZone);
                startTimeOfAggregates = IncrementalTimeConverterUtil.getStartTimeOfAggregates(timestamp, duration,
                        timeZone);
                dispatchAggregateEvents(startTimeOfAggregates);
                sendTimerEvent(streamEvent, timestamp, timeZone);
            }

            if (streamEvent.getType() == ComplexEvent.Type.CURRENT) {
                processAggregates(streamEvent);
            }
        }
    }

    private void sendTimerEvent(StreamEvent streamEvent, long timestamp, String timeZone) {
        if (streamEvent.getType() == ComplexEvent.Type.TIMER && getNextExecutor() != null) {
            StreamEvent timerEvent = streamEventPool.borrowEvent();
            timerEvent.setType(ComplexEvent.Type.TIMER);
            timerEvent.setTimestamp(IncrementalTimeConverterUtil.getEmitTimeOfLastEventToRemove(timestamp,
                    this.duration, this.bufferSize, timeZone));
            ComplexEventChunk<StreamEvent> timerStreamEventChunk = new ComplexEventChunk<>(true);
            timerStreamEventChunk.add(timerEvent);
            next.execute(timerStreamEventChunk);
        }
    }

    private long getTimestamp(StreamEvent streamEvent, String timeZone) {
        long timestamp;
        if (streamEvent.getType() == ComplexEvent.Type.CURRENT) {
            timestamp = (long) timestampExpressionExecutor.execute(streamEvent);
            if (isRoot && !isProcessingOnExternalTime && !timerStarted) {
                scheduler.notifyAt(IncrementalTimeConverterUtil.getNextEmitTime(timestamp, duration, timeZone));
                timerStarted = true;
            }
        } else {
            // TIMER event has arrived. A timer event never arrives for external timeStamp based execution
            timestamp = streamEvent.getTimestamp();
            if (isRoot) {
                // Scheduling is done by root incremental executor only
                scheduler.notifyAt(IncrementalTimeConverterUtil.getNextEmitTime(timestamp, duration, timeZone));
            }
        }
        return timestamp;
    }

    private String getTimeZone(StreamEvent streamEvent) {
        String timeZone;
        if (streamEvent.getType() == ComplexEvent.Type.CURRENT) {
            timeZone = timeZoneExpressionExecutor.execute(streamEvent).toString();
        } else {
            // TIMER event has arrived.
            timeZone = ZoneOffset.systemDefault().getRules().getOffset(Instant.now()).getId();
        }
        return timeZone;
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
                        BaseIncrementalValueStore aBaseIncrementalValueStore = baseIncrementalValueGroupByStore
                                .computeIfAbsent(groupedByKey,
                                        k -> baseIncrementalValueStore.cloneStore(k, startTimeOfAggregates));
                        process(streamEvent, aBaseIncrementalValueStore);
                    } else {
                        BaseIncrementalValueStore aBaseIncrementalValueStore = baseIncrementalValueStoreMap
                                .computeIfAbsent(groupedByKey,
                                        k -> baseIncrementalValueStore.cloneStore(k, startTimeOfAggregates));
                        process(streamEvent, aBaseIncrementalValueStore);
                    }
                } finally {
                    GroupByAggregationAttributeExecutor.getKeyThreadLocal().remove();
                }
            } else {
                if (baseIncrementalValueStoreList != null) {
                    BaseIncrementalValueStore aBaseIncrementalValueStore = baseIncrementalValueStoreList
                            .get(currentBufferIndex);
                    process(streamEvent, aBaseIncrementalValueStore);
                } else {
                    process(streamEvent, baseIncrementalValueStore);
                }
            }
        }
    }

    private void process(StreamEvent streamEvent, BaseIncrementalValueStore baseIncrementalValueStore) {
        List<ExpressionExecutor> expressionExecutors = baseIncrementalValueStore.getExpressionExecutors();
        for (int i = 0; i < expressionExecutors.size(); i++) { // keeping timestamp value location as null
            ExpressionExecutor expressionExecutor = expressionExecutors.get(i);
            baseIncrementalValueStore.setValue(expressionExecutor.execute(streamEvent), i + 1);
        }
        baseIncrementalValueStore.setProcessed(true);
    }

    private void dispatchAggregateEvents(long startTimeOfNewAggregates) {

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
        if (aBaseIncrementalValueStore.isProcessed()) {
            StreamEvent streamEvent = aBaseIncrementalValueStore.createStreamEvent();
            ComplexEventChunk<StreamEvent> eventChunk = new ComplexEventChunk<>(true);
            eventChunk.add(streamEvent);
            LOG.debug("Event dispatched by " + this.duration + " incremental executor: " + eventChunk.toString());
            table.addEvents(eventChunk);
            next.execute(eventChunk);
        }
        cleanBaseIncrementalValueStore(startTimeOfNewAggregates, aBaseIncrementalValueStore);
    }

    private void dispatchEvents(Map<String, BaseIncrementalValueStore> baseIncrementalValueGroupByStore) {
        if (baseIncrementalValueGroupByStore.size() > 0) {
            ComplexEventChunk<StreamEvent> eventChunk = new ComplexEventChunk<>(true);
            for (BaseIncrementalValueStore aBaseIncrementalValueStore : baseIncrementalValueGroupByStore.values()) {
                StreamEvent streamEvent = aBaseIncrementalValueStore.createStreamEvent();
                eventChunk.add(streamEvent);
            }
            LOG.debug("Event dispatched by " + this.duration + " incremental executor: " + eventChunk.toString());
            table.addEvents(eventChunk);
            next.execute(eventChunk);
        }
        baseIncrementalValueGroupByStore.clear();
    }

    private void cleanBaseIncrementalValueStore(long startTimeOfNewAggregates,
            BaseIncrementalValueStore baseIncrementalValueStore) {
        baseIncrementalValueStore.clearValues();
        baseIncrementalValueStore.setTimestamp(startTimeOfNewAggregates);
        baseIncrementalValueStore.setProcessed(false);
        for (ExpressionExecutor expressionExecutor : baseIncrementalValueStore.getExpressionExecutors()) {
            expressionExecutor.execute(resetEvent);
        }
    }

    ArrayList<HashMap<String, BaseIncrementalValueStore>> getBaseIncrementalValueGroupByStoreList() {
        return baseIncrementalValueGroupByStoreList;
    }

    Map<String, BaseIncrementalValueStore> getBaseIncrementalValueStoreMap() {
        return baseIncrementalValueStoreMap;
    }

    ArrayList<BaseIncrementalValueStore> getBaseIncrementalValueStoreList() {
        return baseIncrementalValueStoreList;
    }

    BaseIncrementalValueStore getBaseIncrementalValueStore() {
        return baseIncrementalValueStore;
    }

    public BaseIncrementalValueStore getOldestEvent() {
        if (isGroupBy) {
            if (baseIncrementalValueGroupByStoreList != null) {
                int oldestEventIndex = currentBufferIndex + 1;
                if (oldestEventIndex > bufferSize) {
                    oldestEventIndex -= bufferSize + 1;
                }
                Map<String, BaseIncrementalValueStore> baseIncrementalValueGroupByStore =
                        baseIncrementalValueGroupByStoreList.get(oldestEventIndex);
                return baseIncrementalValueGroupByStore.size() != 0
                        ? (BaseIncrementalValueStore) baseIncrementalValueGroupByStore.values().toArray()[0] : null;
            } else {
                return baseIncrementalValueStoreMap.size() != 0
                        ? (BaseIncrementalValueStore) baseIncrementalValueStoreMap.values().toArray()[0] : null;
            }
        } else {
            if (baseIncrementalValueStoreList != null) {
                int oldestEventIndex = currentBufferIndex + 1;
                if (oldestEventIndex > bufferSize) {
                    oldestEventIndex -= bufferSize + 1;
                }
                BaseIncrementalValueStore aBaseIncrementalValueStore = baseIncrementalValueStoreList
                        .get(oldestEventIndex);
                return aBaseIncrementalValueStore.isProcessed() ? aBaseIncrementalValueStore : null;
            } else {
                return baseIncrementalValueStore.isProcessed() ? baseIncrementalValueStore : null;
            }
        }
    }

    public BaseIncrementalValueStore getNewestEvent() {
        if (isGroupBy) {
            if (baseIncrementalValueGroupByStoreList != null) {
                Map<String, BaseIncrementalValueStore> baseIncrementalValueGroupByStore =
                        baseIncrementalValueGroupByStoreList.get(currentBufferIndex);
                return baseIncrementalValueGroupByStore.size() != 0
                        ? (BaseIncrementalValueStore) baseIncrementalValueGroupByStore.values().toArray()[0] : null;
                // Sometimes, there could be no in-memory aggregates, for event time based execution.
                // Hence the null return.
            } else {
                return baseIncrementalValueStoreMap.size() != 0
                        ? (BaseIncrementalValueStore) baseIncrementalValueStoreMap.values().toArray()[0] : null;
            }
        } else {
            if (baseIncrementalValueStoreList != null) {
                BaseIncrementalValueStore aBaseIncrementalValueStore = baseIncrementalValueStoreList
                        .get(currentBufferIndex);
                return aBaseIncrementalValueStore.isProcessed() ? aBaseIncrementalValueStore : null;
            } else {
                return baseIncrementalValueStore.isProcessed() ? baseIncrementalValueStore : null;
            }
        }
    }
}
