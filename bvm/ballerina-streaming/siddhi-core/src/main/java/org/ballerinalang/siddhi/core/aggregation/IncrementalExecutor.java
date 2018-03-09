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

package org.ballerinalang.siddhi.core.aggregation;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.ComplexEvent;
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.stream.MetaStreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.exception.SiddhiAppRuntimeException;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.query.selector.GroupByKeyGenerator;
import org.ballerinalang.siddhi.core.query.selector.attribute.processor.executor.GroupByAggregationAttributeExecutor;
import org.ballerinalang.siddhi.core.table.Table;
import org.ballerinalang.siddhi.core.util.IncrementalTimeConverterUtil;
import org.ballerinalang.siddhi.core.util.Scheduler;
import org.ballerinalang.siddhi.core.util.snapshot.Snapshotable;
import org.ballerinalang.siddhi.query.api.aggregation.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Incremental executor class which is responsible for performing incremental aggregation.
 */
public class IncrementalExecutor implements Executor, Snapshotable {
    private static final Logger LOG = LoggerFactory.getLogger(IncrementalExecutor.class);

    private final StreamEvent resetEvent;
    private final ExpressionExecutor timestampExpressionExecutor;
    private final ExpressionExecutor timeZoneExpressionExecutor;
    private TimePeriod.Duration duration;
    private Table table;
    private GroupByKeyGenerator groupByKeyGenerator;
    private int bufferSize;
    private boolean ignoreEventsOlderThanBuffer;
    private StreamEventPool streamEventPool;
    private long nextEmitTime = -1;
    private boolean isProcessingOnExternalTime;
    private int currentBufferIndex = -1;
    private long startTimeOfAggregates = -1;
    private boolean timerStarted = false;
    private boolean isGroupBy;
    private Executor next;
    private Scheduler scheduler;
    private boolean isRoot;
    private long millisecondsPerDuration;
    private boolean eventOlderThanBuffer;
    private int maxTimestampPosition;
    private long maxTimestampInBuffer;
    private long minTimestampInBuffer;
    private Semaphore mutex;
    private boolean isRootAndLoadedFromTable = false;
    private String elementId;

    private BaseIncrementalValueStore baseIncrementalValueStore = null;
    private Map<String, BaseIncrementalValueStore> baseIncrementalValueStoreMap = null;
    private ArrayList<BaseIncrementalValueStore> baseIncrementalValueStoreList = null;
    private ArrayList<HashMap<String, BaseIncrementalValueStore>> baseIncrementalValueGroupByStoreList = null;

    public IncrementalExecutor(TimePeriod.Duration duration, List<ExpressionExecutor> processExpressionExecutors,
                               GroupByKeyGenerator groupByKeyGenerator, MetaStreamEvent metaStreamEvent, int bufferSize,
                               boolean ignoreEventsOlderThanBuffer, IncrementalExecutor child, boolean isRoot,
                               Table table, boolean isProcessingOnExternalTime, SiddhiAppContext siddhiAppContext,
                               String aggregatorName) {
        this.duration = duration;
        this.next = child;
        this.isRoot = isRoot;
        this.table = table;
        this.bufferSize = bufferSize;
        this.ignoreEventsOlderThanBuffer = ignoreEventsOlderThanBuffer;
        this.streamEventPool = new StreamEventPool(metaStreamEvent, 10);
        this.isProcessingOnExternalTime = isProcessingOnExternalTime;
        this.timestampExpressionExecutor = processExpressionExecutors.remove(0);
        this.timeZoneExpressionExecutor = processExpressionExecutors.get(0);
        this.baseIncrementalValueStore = new BaseIncrementalValueStore(-1, processExpressionExecutors,
                streamEventPool, siddhiAppContext, aggregatorName);

        if (groupByKeyGenerator != null) {
            this.groupByKeyGenerator = groupByKeyGenerator;
            isGroupBy = true;
            if (bufferSize > 0 && isRoot) {
                baseIncrementalValueGroupByStoreList = new ArrayList<>(bufferSize + 1);
                for (int i = 0; i < bufferSize + 1; i++) {
                    baseIncrementalValueGroupByStoreList.add(new HashMap<>());
                }
                this.millisecondsPerDuration = IncrementalTimeConverterUtil.getMillisecondsPerDuration(duration);
            } else {
                baseIncrementalValueStoreMap = new HashMap<>();
            }
        } else {
            isGroupBy = false;
            if (bufferSize > 0 && isRoot) {
                baseIncrementalValueStoreList = new ArrayList<>(bufferSize + 1);
                for (int i = 0; i < bufferSize + 1; i++) {
                    baseIncrementalValueStoreList.add(baseIncrementalValueStore.cloneStore(null, -1));
                }
                this.millisecondsPerDuration = IncrementalTimeConverterUtil.getMillisecondsPerDuration(duration);
            }
        }

        this.resetEvent = streamEventPool.borrowEvent();
        this.resetEvent.setType(ComplexEvent.Type.RESET);
        setNextExecutor(child);

        mutex = new Semaphore(1);

        if (elementId == null) {
            elementId = "IncrementalExecutor-" + siddhiAppContext.getElementIdGenerator().createNewId();
        }
        siddhiAppContext.getSnapshotService().addSnapshotable(aggregatorName, this);

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

            startTimeOfAggregates = IncrementalTimeConverterUtil.getStartTimeOfAggregates(timestamp, duration,
                    timeZone);

            if (isRootAndLoadedFromTable) {
                // If events are loaded to in-memory from tables, we would not process any data until the
                // first event that is greater than or equal to emitTimeOfLatestEventInTable arrives.
                // Note that nextEmitTime is set to emitTimeOfLatestEventInTable with
                // setValuesForInMemoryRecreateFromTable method.
                // Hence, even if ignoreEventsOlderThanBuffer is false (meaning that we want old events to be processed)
                // the first few old events would be dropped. This avoids certain issues which may
                // arise when replaying data.
                if (timestamp < nextEmitTime) {
                    continue;
                } else {
                    isRootAndLoadedFromTable = false;
                }
            }

            if (bufferSize > 0 && isRoot) {
                try {
                    mutex.acquire();
                    dispatchBufferedAggregateEvents(startTimeOfAggregates);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SiddhiAppRuntimeException("Error when dispatching events from buffer", e);
                } finally {
                    mutex.release();
                }
                if (streamEvent.getType() == ComplexEvent.Type.CURRENT) {
                    if (!eventOlderThanBuffer) {
                        processAggregates(streamEvent);
                    } else if (!ignoreEventsOlderThanBuffer) {
                        // Incoming event is older than buffer
                        startTimeOfAggregates = minTimestampInBuffer;
                        processAggregates(streamEvent);
                    }
                }
            } else {
                if (timestamp >= nextEmitTime) {
                    nextEmitTime = IncrementalTimeConverterUtil.getNextEmitTime(timestamp, duration, timeZone);
                    dispatchAggregateEvents(startTimeOfAggregates);
                    if (!isProcessingOnExternalTime) {
                        sendTimerEvent(timeZone);
                    }
                }
                if (streamEvent.getType() == ComplexEvent.Type.CURRENT) {
                    if (nextEmitTime == IncrementalTimeConverterUtil.getNextEmitTime(timestamp, duration, timeZone)) {
                        // This condition checks whether incoming event belongs to current processing event's time slot
                        processAggregates(streamEvent);
                    } else if (!ignoreEventsOlderThanBuffer) {
                        // Incoming event is older than current processing event.
                        startTimeOfAggregates = minTimestampInBuffer;
                        processAggregates(streamEvent);
                    }
                }
            }
        }
    }

    private void sendTimerEvent(String timeZone) {
        if (getNextExecutor() != null) {
            StreamEvent timerEvent = streamEventPool.borrowEvent();
            timerEvent.setType(ComplexEvent.Type.TIMER);
            timerEvent.setTimestamp(
                    IncrementalTimeConverterUtil.getPreviousStartTime(startTimeOfAggregates, this.duration, timeZone));
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
                    if (!aBaseIncrementalValueStore.isProcessed()) {
                        aBaseIncrementalValueStore.setTimestamp(startTimeOfAggregates);
                    }
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
            dispatchEvents(baseIncrementalValueStoreMap);
        } else {
            dispatchEvent(startTimeOfNewAggregates, baseIncrementalValueStore);
        }
    }

    private void dispatchBufferedAggregateEvents(long startTimeOfNewAggregates) {
        int lastDispatchIndex;
        if (currentBufferIndex == -1) {
            maxTimestampPosition = 0;
            maxTimestampInBuffer = startTimeOfNewAggregates;
            currentBufferIndex = 0;
            eventOlderThanBuffer = false;
            return;
        }
        if (startTimeOfNewAggregates > maxTimestampInBuffer) {
            if ((startTimeOfNewAggregates - maxTimestampInBuffer) / millisecondsPerDuration >= bufferSize + 1) {
                // Need to flush all events
                if (isGroupBy) {
                    for (int i = 0; i <= bufferSize; i++) {
                        dispatchEvents(baseIncrementalValueGroupByStoreList.get(i));
                    }
                } else {
                    for (int i = 0; i <= bufferSize; i++) {
                        dispatchEvent(startTimeOfNewAggregates, baseIncrementalValueStoreList.get(i));
                    }
                }
                currentBufferIndex = (int) (((startTimeOfNewAggregates - maxTimestampInBuffer)
                        / millisecondsPerDuration) % (bufferSize + 1));
                maxTimestampPosition = currentBufferIndex;
            } else {
                lastDispatchIndex = (maxTimestampPosition
                        + (int) ((startTimeOfNewAggregates - maxTimestampInBuffer) / millisecondsPerDuration))
                        % (bufferSize + 1);
                int minTimestampIndex = maxTimestampPosition - bufferSize;
                if (minTimestampIndex < 0) {
                    minTimestampIndex = (bufferSize + 1) + minTimestampIndex;
                }
                if (isGroupBy) {
                    while (true) {
                        Map<String, BaseIncrementalValueStore> baseIncrementalValueGroupByStore =
                                baseIncrementalValueGroupByStoreList.get(minTimestampIndex);
                        if (baseIncrementalValueGroupByStore.size() > 0) {
                            dispatchEvents(baseIncrementalValueGroupByStore);
                        }
                        ++minTimestampIndex;
                        if (minTimestampIndex > bufferSize) {
                            minTimestampIndex = 0;
                        }

                        if (lastDispatchIndex != bufferSize) {
                            if (minTimestampIndex == lastDispatchIndex + 1) {
                                break;
                            }
                        } else if (minTimestampIndex == 0) {
                            break;
                        }
                    }
                } else {
                    while (true) {
                        BaseIncrementalValueStore aBaseIncrementalValueStore = baseIncrementalValueStoreList
                                .get(minTimestampIndex);
                        if (aBaseIncrementalValueStore.isProcessed()) {
                            dispatchEvent(startTimeOfNewAggregates, aBaseIncrementalValueStore);
                        }
                        ++minTimestampIndex;
                        if (minTimestampIndex > bufferSize) {
                            minTimestampIndex = 0;
                        }

                        if (lastDispatchIndex != bufferSize) {
                            if (minTimestampIndex == lastDispatchIndex + 1) {
                                break;
                            }
                        } else if (minTimestampIndex == 0) {
                            break;
                        }
                    }
                }
                currentBufferIndex = lastDispatchIndex;
                maxTimestampPosition = lastDispatchIndex;
            }
            maxTimestampInBuffer = startTimeOfNewAggregates;
            eventOlderThanBuffer = false;
        } else if ((int) ((maxTimestampInBuffer - startTimeOfNewAggregates) / millisecondsPerDuration) <= bufferSize) {
            int tempIndex = maxTimestampPosition
                    - (int) ((maxTimestampInBuffer - startTimeOfNewAggregates) / millisecondsPerDuration);
            if (tempIndex >= 0) {
                currentBufferIndex = tempIndex;
            } else {
                currentBufferIndex = (bufferSize + 1) + tempIndex;
            }
            eventOlderThanBuffer = false;

        } else {
            // Incoming event is older than buffer
            int minTimestampPosition = maxTimestampPosition - bufferSize;
            if (minTimestampPosition < 0) {
                currentBufferIndex = (bufferSize + 1) + minTimestampPosition;
            } else {
                currentBufferIndex = minTimestampPosition; // This could actually only be 0 (since the largest value
                // maxTimestampPosition can take equals the buffer size)
            }
            minTimestampInBuffer = maxTimestampInBuffer - bufferSize * millisecondsPerDuration;
            eventOlderThanBuffer = true;
        }
    }

    private void dispatchEvent(long startTimeOfNewAggregates, BaseIncrementalValueStore aBaseIncrementalValueStore) {
        if (aBaseIncrementalValueStore.isProcessed()) {
            StreamEvent streamEvent = aBaseIncrementalValueStore.createStreamEvent();
            ComplexEventChunk<StreamEvent> eventChunk = new ComplexEventChunk<>(true);
            eventChunk.add(streamEvent);
            LOG.debug("Event dispatched by " + this.duration + " incremental executor: " + eventChunk.toString());
            table.addEvents(eventChunk, 1);
            if (getNextExecutor() != null) {
                next.execute(eventChunk);
            }
        }
        cleanBaseIncrementalValueStore(startTimeOfNewAggregates, aBaseIncrementalValueStore);
    }

    private void dispatchEvents(Map<String, BaseIncrementalValueStore> baseIncrementalValueGroupByStore) {
        int noOfEvents = baseIncrementalValueGroupByStore.size();
        if (noOfEvents > 0) {
            ComplexEventChunk<StreamEvent> eventChunk = new ComplexEventChunk<>(true);
            for (BaseIncrementalValueStore aBaseIncrementalValueStore : baseIncrementalValueGroupByStore.values()) {
                StreamEvent streamEvent = aBaseIncrementalValueStore.createStreamEvent();
                eventChunk.add(streamEvent);
            }
            LOG.debug("Event dispatched by " + this.duration + " incremental executor: " + eventChunk.toString());
            table.addEvents(eventChunk, noOfEvents);
            if (getNextExecutor() != null) {
                next.execute(eventChunk);
            }
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

    public long getOldestEventTimestamp() {
        if (bufferSize > 0 && isRoot) { // Events are buffered
            try {
                mutex.acquire();
                if (currentBufferIndex == -1) {
                    return -1;
                } else {
                    return maxTimestampInBuffer - (bufferSize * millisecondsPerDuration);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SiddhiAppRuntimeException("Error when getting the oldest in-memory event timestamp", e);
            } finally {
                mutex.release();
            }
        } else {
            if (isGroupBy) {
                return baseIncrementalValueStoreMap.size() != 0
                        ? ((BaseIncrementalValueStore) baseIncrementalValueStoreMap.values().toArray()[0])
                        .getTimestamp()
                        : -1;
            } else {
                return baseIncrementalValueStore.isProcessed() ? baseIncrementalValueStore.getTimestamp() : -1;
            }
        }
    }

    public long getNewestEventTimestamp() {
        if (bufferSize > 0 && isRoot) { // Events are buffered
            try {
                mutex.acquire();
                if (currentBufferIndex == -1) {
                    return -1;
                } else {
                    return maxTimestampInBuffer;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SiddhiAppRuntimeException("Error when getting the newest in-memory event timestamp", e);
            } finally {
                mutex.release();
            }
        } else {
            if (isGroupBy) {
                return baseIncrementalValueStoreMap.size() != 0
                        ? ((BaseIncrementalValueStore) baseIncrementalValueStoreMap.values().toArray()[0])
                        .getTimestamp()
                        : -1;
            } else {
                return baseIncrementalValueStore.isProcessed() ? baseIncrementalValueStore.getTimestamp() : -1;
            }
        }
    }

    public long getNextEmitTime() {
        return nextEmitTime;
    }

    public void setValuesForInMemoryRecreateFromTable(boolean isRootAndLoadedFromTable,
                                                      long emitTimeOfLatestEventInTable) {
        this.isRootAndLoadedFromTable = isRootAndLoadedFromTable;
        this.nextEmitTime = emitTimeOfLatestEventInTable;
    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> state = new HashMap<>();

        state.put("NextEmitTime", nextEmitTime);
        state.put("CurrentBufferIndex", currentBufferIndex);
        state.put("StartTimeOfAggregates", startTimeOfAggregates);
        state.put("TimerStarted", timerStarted);
        state.put("EventOlderThanBuffer", eventOlderThanBuffer);
        state.put("MaxTimestampPosition", maxTimestampPosition);
        state.put("MaxTimestampInBuffer", maxTimestampInBuffer);
        state.put("MinTimestampInBuffer", minTimestampInBuffer);
        state.put("IsRootAndLoadedFromTable", isRootAndLoadedFromTable);
        state.put("Mutex", mutex);
        return state;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        nextEmitTime = (long) state.get("NextEmitTime");
        currentBufferIndex = (int) state.get("CurrentBufferIndex");
        startTimeOfAggregates = (long) state.get("StartTimeOfAggregates");
        timerStarted = (boolean) state.get("TimerStarted");
        eventOlderThanBuffer = (boolean) state.get("EventOlderThanBuffer");
        maxTimestampPosition = (int) state.get("MaxTimestampPosition");
        maxTimestampInBuffer = (long) state.get("MaxTimestampInBuffer");
        minTimestampInBuffer = (long) state.get("MinTimestampInBuffer");
        isRootAndLoadedFromTable = (boolean) state.get("IsRootAndLoadedFromTable");
        mutex = (Semaphore) state.get("Mutex");
    }

    @Override
    public String getElementId() {
        return elementId;
    }
}
