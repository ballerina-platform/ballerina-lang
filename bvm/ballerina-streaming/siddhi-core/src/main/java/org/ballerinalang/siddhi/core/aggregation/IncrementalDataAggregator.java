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
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.stream.MetaStreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.util.IncrementalTimeConverterUtil;
import org.ballerinalang.siddhi.query.api.aggregation.TimePeriod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements the logic to aggregate data that is in-memory or in tables, in incremental data processing.
 * <p>
 * In-memory data is required to be aggregated when retrieving values from an aggregate.
 * Table data (tables used to persist incremental aggregates) needs to be aggregated when querying aggregate data from
 * a different server (apart from the server, which was used to define the aggregation).
 */
public class IncrementalDataAggregator {
    private final List<TimePeriod.Duration> incrementalDurations;
    private final TimePeriod.Duration aggregateForDuration;
    private final ExpressionExecutor timestampExecutor;
    private final ExpressionExecutor timeZoneExecutor;
    private final BaseIncrementalValueStore baseIncrementalValueStore;

    private final Map<Long, BaseIncrementalValueStore> baseIncrementalValueStoreMap;
    private final Map<Long, Map<String, BaseIncrementalValueStore>> baseIncrementalValueGroupByStoreMap;

    public IncrementalDataAggregator(List<TimePeriod.Duration> incrementalDurations,
                                     TimePeriod.Duration aggregateForDuration, List<ExpressionExecutor> baseExecutors,
                                     ExpressionExecutor timestampExecutor, MetaStreamEvent metaStreamEvent,
                                     SiddhiAppContext siddhiAppContext) {
        this.incrementalDurations = incrementalDurations;
        this.aggregateForDuration = aggregateForDuration;
        this.timestampExecutor = timestampExecutor;
        this.timeZoneExecutor = baseExecutors.get(0);
        StreamEventPool streamEventPool = new StreamEventPool(metaStreamEvent, 10);
        this.baseIncrementalValueStore = new BaseIncrementalValueStore(-1, baseExecutors, streamEventPool,
                siddhiAppContext, null);
        this.baseIncrementalValueStoreMap = new HashMap<>();
        this.baseIncrementalValueGroupByStoreMap = new HashMap<>();
    }

    public ComplexEventChunk<StreamEvent> aggregateInMemoryData(
            Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap) {
        for (TimePeriod.Duration duration : incrementalDurations) {
            IncrementalExecutor incrementalExecutor = incrementalExecutorMap.get(duration);

            ArrayList<HashMap<String, BaseIncrementalValueStore>> baseIncrementalValueGroupByStoreList =
                    incrementalExecutor.getBaseIncrementalValueGroupByStoreList();
            Map<String, BaseIncrementalValueStore> baseIncrementalValueStoreMap = incrementalExecutor
                    .getBaseIncrementalValueStoreMap();
            ArrayList<BaseIncrementalValueStore> baseIncrementalValueStoreList = incrementalExecutor
                    .getBaseIncrementalValueStoreList();
            BaseIncrementalValueStore baseIncrementalValueStore = incrementalExecutor.getBaseIncrementalValueStore();

            if (baseIncrementalValueGroupByStoreList != null) {
                for (HashMap<String, BaseIncrementalValueStore> aBaseIncrementalValueGroupByStoreList :
                        baseIncrementalValueGroupByStoreList) {
                    for (Map.Entry<String, BaseIncrementalValueStore> entry : aBaseIncrementalValueGroupByStoreList
                            .entrySet()) {
                        BaseIncrementalValueStore aBaseIncrementalValueStore = entry.getValue();
                        if (aBaseIncrementalValueStore.isProcessed()) {
                            processInMemoryAggregates(aBaseIncrementalValueStore.createStreamEvent(),
                                    aBaseIncrementalValueStore.getTimestamp(), entry.getKey());
                        }
                    }
                }
            } else if (baseIncrementalValueStoreMap != null) {
                for (Map.Entry<String, BaseIncrementalValueStore> entry : baseIncrementalValueStoreMap.entrySet()) {
                    BaseIncrementalValueStore aBaseIncrementalValueStore = entry.getValue();
                    if (aBaseIncrementalValueStore.isProcessed()) {
                        processInMemoryAggregates(aBaseIncrementalValueStore.createStreamEvent(),
                                aBaseIncrementalValueStore.getTimestamp(), entry.getKey());
                    }
                }
            } else if (baseIncrementalValueStoreList != null) {
                for (BaseIncrementalValueStore aBaseIncrementalValueStore : baseIncrementalValueStoreList) {
                    if (aBaseIncrementalValueStore.isProcessed()) {
                        processInMemoryAggregates(aBaseIncrementalValueStore.createStreamEvent(),
                                aBaseIncrementalValueStore.getTimestamp(), null);
                    }
                }
            } else if (baseIncrementalValueStore.isProcessed()) {
                processInMemoryAggregates(baseIncrementalValueStore.createStreamEvent(),
                        baseIncrementalValueStore.getTimestamp(), null);
            }

            if (duration == aggregateForDuration) {
                break;
            }
        }

        return createEventChunkFromAggregatedData();
    }

    private void processInMemoryAggregates(StreamEvent streamEvent, long timestamp, String groupByKey) {
        String timeZone = timeZoneExecutor.execute(streamEvent).toString();
        long startTimeOfAggregates = IncrementalTimeConverterUtil.getStartTimeOfAggregates(timestamp,
                aggregateForDuration, timeZone);
        synchronized (this) {
            if (groupByKey != null) {
                Map<String, BaseIncrementalValueStore> aBaseIncrementalValueStoreGroupBy =
                        baseIncrementalValueGroupByStoreMap.get(startTimeOfAggregates);
                if (aBaseIncrementalValueStoreGroupBy == null) {
                    aBaseIncrementalValueStoreGroupBy = new HashMap<>();
                    baseIncrementalValueGroupByStoreMap.put(startTimeOfAggregates, aBaseIncrementalValueStoreGroupBy);
                }
                BaseIncrementalValueStore aBaseIncrementalValueStore = aBaseIncrementalValueStoreGroupBy
                        .computeIfAbsent(groupByKey,
                                k -> baseIncrementalValueStore.cloneStore(k, startTimeOfAggregates));
                process(streamEvent, aBaseIncrementalValueStore);
            } else {
                BaseIncrementalValueStore aBaseIncrementalValueStore = baseIncrementalValueStoreMap
                        .get(startTimeOfAggregates);
                if (aBaseIncrementalValueStore == null) {
                    aBaseIncrementalValueStore = baseIncrementalValueStore.cloneStore(null, startTimeOfAggregates);
                    baseIncrementalValueStoreMap.put(startTimeOfAggregates, aBaseIncrementalValueStore);
                }
                process(streamEvent, aBaseIncrementalValueStore);
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

    private ComplexEventChunk<StreamEvent> createEventChunkFromAggregatedData() {
        ComplexEventChunk<StreamEvent> processedInMemoryEventChunk = new ComplexEventChunk<>(true);
        if (baseIncrementalValueStoreMap.size() > 0) {
            for (Map.Entry<Long, BaseIncrementalValueStore> entryAgainstTime : baseIncrementalValueStoreMap
                    .entrySet()) {
                processedInMemoryEventChunk.add(entryAgainstTime.getValue().createStreamEvent());
            }
        } else {
            for (Map.Entry<Long, Map<String, BaseIncrementalValueStore>> entryAgainstTime :
                    baseIncrementalValueGroupByStoreMap.entrySet()) {
                for (Map.Entry<String, BaseIncrementalValueStore> entryAgainstKey : entryAgainstTime.getValue()
                        .entrySet()) {
                    processedInMemoryEventChunk.add(entryAgainstKey.getValue().createStreamEvent());
                }
            }
        }
        return processedInMemoryEventChunk;
    }
}
