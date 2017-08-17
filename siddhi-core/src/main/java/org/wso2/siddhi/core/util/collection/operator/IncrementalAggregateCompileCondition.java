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

package org.wso2.siddhi.core.util.collection.operator;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.BaseIncrementalValueStore;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecutor;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.IncrementalTimeConverterUtil;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines the logic to find a matching event from an incremental aggregator (retrieval from incremental aggregator),
 * based on the logical conditions defined herewith.
 */
public class IncrementalAggregateCompileCondition implements CompiledCondition {
    private Map<TimePeriod.Duration, CompiledCondition> withinTableCompiledConditions;
    private CompiledCondition inMemoryStoreCompileCondition;
    private CompiledCondition onCompiledCondition;
    private MetaStreamEvent tableMetaStreamEvent;
    private MetaStreamEvent aggregateMetaStreamEvent;

    private Map<Long, Map<String, BaseIncrementalValueStore>> inMemoryAggregateStoreMap = new HashMap<>();
    private final StreamEventPool streamEventPoolForTableMeta;
    private final StreamEventCloner tableEventCloner;
    private final StreamEventPool streamEventPoolForAggregateMeta;
    private final StreamEventCloner aggregateEventCloner;
    private BaseIncrementalValueStore baseIncrementalValueStore;

    public IncrementalAggregateCompileCondition(
            Map<TimePeriod.Duration, CompiledCondition> withinTableCompiledConditions,
            CompiledCondition inMemoryStoreCompileCondition, CompiledCondition onCompiledCondition,
            MetaStreamEvent tableMetaStreamEvent, MetaStreamEvent aggregateMetaSteamEvent) {
        this.withinTableCompiledConditions = withinTableCompiledConditions;
        this.inMemoryStoreCompileCondition = inMemoryStoreCompileCondition;
        this.onCompiledCondition = onCompiledCondition;
        this.tableMetaStreamEvent = tableMetaStreamEvent;
        this.aggregateMetaStreamEvent = aggregateMetaSteamEvent;

        this.streamEventPoolForTableMeta = new StreamEventPool(tableMetaStreamEvent, 10);
        this.tableEventCloner = new StreamEventCloner(tableMetaStreamEvent, streamEventPoolForTableMeta);

        this.streamEventPoolForAggregateMeta = new StreamEventPool(aggregateMetaSteamEvent, 10);
        this.aggregateEventCloner = new StreamEventCloner(aggregateMetaSteamEvent, streamEventPoolForAggregateMeta);
    }

    @Override
    public CompiledCondition cloneCompiledCondition(String key) {
        Map<TimePeriod.Duration, CompiledCondition> copyOfWithinTableCompiledConditions = new HashMap<>();
        for (Map.Entry<TimePeriod.Duration, CompiledCondition> entry : withinTableCompiledConditions.entrySet()) {
            copyOfWithinTableCompiledConditions.put(entry.getKey(), entry.getValue().cloneCompiledCondition(key));
        }
        return new IncrementalAggregateCompileCondition(copyOfWithinTableCompiledConditions,
                inMemoryStoreCompileCondition.cloneCompiledCondition(key),
                onCompiledCondition.cloneCompiledCondition(key), tableMetaStreamEvent, aggregateMetaStreamEvent);
    }

    public StreamEvent find(StateEvent matchingEvent, TimePeriod.Duration perValue,
            Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap,
            List<TimePeriod.Duration> incrementalDurations, Table tableForPerDuration,
            List<ExpressionExecutor> baseExecutors, List<ExpressionExecutor> outputExpressionExecutors) {

        ComplexEventChunk<StreamEvent> complexEventChunkToHoldMatches = new ComplexEventChunk<>(true);

        // Get all the aggregates within the given duration, from table corresponding to "per" duration
        StreamEvent withinMatchFromPersistedEvents = tableForPerDuration.find(matchingEvent,
                withinTableCompiledConditions.get(perValue));
        complexEventChunkToHoldMatches.add(withinMatchFromPersistedEvents);

        // Optimization step.
        // Get the newest and oldest events from in-memory (running) aggregates, and
        // check whether at least one of those events is within the given time range. If it's not the case,
        // there's no need to iterate through in-memory data.
        BaseIncrementalValueStore newestInMemoryEvent = getNewestInMemoryEvent(incrementalExecutorMap,
                incrementalDurations);
        BaseIncrementalValueStore oldestInMemoryEvent = getOldestInMemoryEvent(incrementalExecutorMap,
                incrementalDurations);

        if (requiresAggregatingInMemoryData(newestInMemoryEvent, oldestInMemoryEvent, matchingEvent)) {
            ExpressionExecutor timeZoneExecutor = baseExecutors.get(0);
            this.baseIncrementalValueStore = new BaseIncrementalValueStore(-1, baseExecutors,
                    streamEventPoolForTableMeta);

            // Aggregate in-memory data corresponding to "per" duration
            aggregateInMemoryData(incrementalExecutorMap.get(perValue).getInMemoryStore(), perValue, timeZoneExecutor);

            if (perValue != incrementalDurations.get(0)) {
                // If the 'per' is for root executor (first value in incrementalDurations list),
                // there would be no executors prior to that
                for (TimePeriod.Duration duration : incrementalDurations) {
                    if (duration == perValue) {
                        break;
                    }
                    aggregateInMemoryData(incrementalExecutorMap.get(duration).getInMemoryStore(), perValue,
                            timeZoneExecutor);
                }
            } // TODO: 8/11/17 add thrown error messages to my error message (e.getMessage)

            // Create event chunk out of aggregated in-memory data (which is stored in inMemoryAggregateStoreMap)
            ComplexEventChunk<StreamEvent> aggregatedInMemoryEventChunk = createEventChunkFromAggregatedInMemoryData();

            StreamEvent withinMatchFromInMemory = ((Operator) inMemoryStoreCompileCondition).find(matchingEvent,
                    aggregatedInMemoryEventChunk, tableEventCloner);
            complexEventChunkToHoldMatches.add(withinMatchFromInMemory);
        }
        return executeOnCompileCondition(matchingEvent, complexEventChunkToHoldMatches, outputExpressionExecutors);
    }

    private void aggregateInMemoryData(Object inMemoryStore, TimePeriod.Duration perValue,
            ExpressionExecutor timeZoneExecutor) {
        BaseIncrementalValueStore baseIncrementalValueStore; // TODO: 8/11/17 convert method in
                                                             // baseIncrementalValueStore. Extend store to have one
                                                             // format .have interface
        // TODO: 8/11/17 or move to 3 methods
        long timeBucket;
        String groupByKey;
        StreamEvent streamEvent;
        if (inMemoryStore instanceof ArrayList) {
            ArrayList inMemoryStoreElements = (ArrayList) inMemoryStore;
            for (int i = 0; i < inMemoryStoreElements.size(); i++) {
                if (inMemoryStoreElements.get(i) instanceof BaseIncrementalValueStore) {
                    // inMemory store is baseIncrementalValueStoreList (no group by, buffer > 0)
                    baseIncrementalValueStore = (BaseIncrementalValueStore) inMemoryStoreElements.get(i);
                    streamEvent = baseIncrementalValueStore.createStreamEvent();
                    timeBucket = IncrementalTimeConverterUtil.getStartTimeOfAggregates(
                            baseIncrementalValueStore.getTimestamp(), perValue,
                            timeZoneExecutor.execute(streamEvent).toString());
                    groupByKey = null;
                    // TODO: 8/11/17 add logs wherever possible
                    updateInMemoryAggregateStoreMap(timeBucket, groupByKey, streamEvent);
                } else { // inMemory store is baseIncrementalValueGroupByStoreList (group by, buffer > 0)
                    assert inMemoryStoreElements.get(i) instanceof Map;
                    Map inMemoryMap = (Map) inMemoryStoreElements.get(i);
                    for (Object entry : inMemoryMap.entrySet()) {
                        baseIncrementalValueStore = (BaseIncrementalValueStore) ((Map.Entry) entry).getValue();
                        streamEvent = baseIncrementalValueStore.createStreamEvent();
                        timeBucket = IncrementalTimeConverterUtil.getStartTimeOfAggregates(
                                baseIncrementalValueStore.getTimestamp(), perValue,
                                timeZoneExecutor.execute(streamEvent).toString());
                        groupByKey = (String) ((Map.Entry) entry).getKey();
                        updateInMemoryAggregateStoreMap(timeBucket, groupByKey, streamEvent);
                    }
                }
            }
        } else if (inMemoryStore instanceof Map) {
            // inMemory store is baseIncrementalValueStoreMap (group by, buffer == 0)
            Map inMemoryMap = (Map) inMemoryStore;
            for (Object entry : inMemoryMap.entrySet()) {
                baseIncrementalValueStore = (BaseIncrementalValueStore) ((Map.Entry) entry).getValue();
                streamEvent = baseIncrementalValueStore.createStreamEvent();
                timeBucket = IncrementalTimeConverterUtil.getStartTimeOfAggregates(
                        baseIncrementalValueStore.getTimestamp(), perValue,
                        timeZoneExecutor.execute(streamEvent).toString());
                groupByKey = (String) ((Map.Entry) entry).getKey();
                updateInMemoryAggregateStoreMap(timeBucket, groupByKey, streamEvent);
            }
        } else {
            // inMemory store is baseIncrementalValueStore (no group by, buffer == 0)
            baseIncrementalValueStore = (BaseIncrementalValueStore) inMemoryStore;
            streamEvent = baseIncrementalValueStore.createStreamEvent();
            timeBucket = IncrementalTimeConverterUtil.getStartTimeOfAggregates(baseIncrementalValueStore.getTimestamp(),
                    perValue, timeZoneExecutor.execute(streamEvent).toString());
            groupByKey = null;
            updateInMemoryAggregateStoreMap(timeBucket, groupByKey, streamEvent);
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

    private void updateInMemoryAggregateStoreMap(long timeBucket, String groupByKey, StreamEvent streamEvent) {
        if (inMemoryAggregateStoreMap.get(timeBucket) != null) {
            if (inMemoryAggregateStoreMap.get(timeBucket).get(groupByKey) != null) {
                process(streamEvent, inMemoryAggregateStoreMap.get(timeBucket).get(groupByKey));
            } else {
                String key = (groupByKey != null) ? (((Long) timeBucket).toString()).concat(groupByKey)
                        : ((Long) timeBucket).toString();
                BaseIncrementalValueStore baseIncrementalValueStore = this.baseIncrementalValueStore.cloneStore(key,
                        timeBucket);
                inMemoryAggregateStoreMap.get(timeBucket).put(groupByKey, baseIncrementalValueStore);
                process(streamEvent, baseIncrementalValueStore);
            }
        } else {
            String key = (groupByKey != null) ? (((Long) timeBucket).toString()).concat(groupByKey)
                    : ((Long) timeBucket).toString();
            BaseIncrementalValueStore baseIncrementalValueStore = this.baseIncrementalValueStore.cloneStore(key,
                    timeBucket);
            Map<String, BaseIncrementalValueStore> baseIncrementalValueGroupByStore = new HashMap<>();
            baseIncrementalValueGroupByStore.put(groupByKey, baseIncrementalValueStore);
            inMemoryAggregateStoreMap.put(timeBucket, baseIncrementalValueGroupByStore);
            process(streamEvent, baseIncrementalValueStore);
        }
    }

    private StreamEvent executeOnCompileCondition(StateEvent matchingEvent,
            ComplexEventChunk<StreamEvent> complexEventChunkToHoldMatches,
            List<ExpressionExecutor> outputExpressionExecutors) {
        ComplexEventChunk<StreamEvent> finalComplexEventChunk = new ComplexEventChunk<>(true);
        StreamEvent resetEvent = streamEventPoolForTableMeta.borrowEvent();
        resetEvent.setType(ComplexEvent.Type.RESET);
        while (complexEventChunkToHoldMatches.hasNext()) {
            StreamEvent streamEvent = complexEventChunkToHoldMatches.next();
            StreamEvent newStreamEvent = streamEventPoolForAggregateMeta.borrowEvent();
            Object outputData[] = new Object[newStreamEvent.getOutputData().length];
            for (int i = 0; i < outputExpressionExecutors.size(); i++) {
                outputData[i] = outputExpressionExecutors.get(i).execute(streamEvent);
            }
            newStreamEvent.setTimestamp(streamEvent.getTimestamp());
            newStreamEvent.setOutputData(outputData);
            finalComplexEventChunk.add(newStreamEvent);
        }
        for (ExpressionExecutor expressionExecutor : outputExpressionExecutors) {
            expressionExecutor.execute(resetEvent);
        }
        return ((Operator) onCompiledCondition).find(matchingEvent, finalComplexEventChunk, aggregateEventCloner);
    }

    private boolean requiresAggregatingInMemoryData(BaseIncrementalValueStore newestInMemoryEvent,
            BaseIncrementalValueStore oldestInMemoryEvent, StateEvent matchingEvent) {
        ComplexEventChunk<StreamEvent> newestAndOldestEventChunk = new ComplexEventChunk<>(true);
        if (newestInMemoryEvent == null && oldestInMemoryEvent == null) {
            return false;
        }
        if (newestInMemoryEvent != null) {
            newestAndOldestEventChunk.add(newestInMemoryEvent.createStreamEvent());
        }
        if (oldestInMemoryEvent != null) {
            newestAndOldestEventChunk.add(oldestInMemoryEvent.createStreamEvent());
        }
        return ((Operator) inMemoryStoreCompileCondition).find(matchingEvent, newestAndOldestEventChunk,
                tableEventCloner) != null;

    }

    private BaseIncrementalValueStore getNewestInMemoryEvent(
            Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap,
            List<TimePeriod.Duration> incrementalDurations) {
        BaseIncrementalValueStore newestEvent;
        for (TimePeriod.Duration incrementalDuration : incrementalDurations) {
            newestEvent = incrementalExecutorMap.get(incrementalDuration).getNewestEvent();
            if (newestEvent != null) {
                return newestEvent;
            }
        }
        return null;
    }

    private BaseIncrementalValueStore getOldestInMemoryEvent(
            Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap,
            List<TimePeriod.Duration> incrementalDurations) {
        BaseIncrementalValueStore oldestEvent;
        TimePeriod.Duration incrementalDuration;
        for (int i = incrementalDurations.size() - 1; i >= 0; i--) {
            incrementalDuration = TimePeriod.Duration.values()[i];
            oldestEvent = incrementalExecutorMap.get(incrementalDuration).getOldestEvent();
            if (oldestEvent != null) {
                return oldestEvent;
            }
        }
        return null;
    }

    private ComplexEventChunk<StreamEvent> createEventChunkFromAggregatedInMemoryData() {
        ComplexEventChunk<StreamEvent> processedInMemoryEventChunk = new ComplexEventChunk<>(true);
        for (Map.Entry<Long, Map<String, BaseIncrementalValueStore>> timeBucketEntry : inMemoryAggregateStoreMap
                .entrySet()) {
            for (Map.Entry<String, BaseIncrementalValueStore> groupByEntry : timeBucketEntry.getValue().entrySet()) {
                processedInMemoryEventChunk.add(groupByEntry.getValue().createStreamEvent());
            }
        }
        inMemoryAggregateStoreMap.clear();
        return processedInMemoryEventChunk;
    }
}
