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
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecutor;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IncrementalAggregateCompileCondition implements CompiledCondition {
    protected Map<TimePeriod.Duration, CompiledCondition> tableCompiledConditions;
    protected Map<TimePeriod.Duration, CompiledCondition> inMemoryStoreCompileConditions;
    private List<ExpressionExecutor> baseExecutors;
    private Map<TimePeriod.Duration, ComplexEventChunk<StreamEvent>> runningAggregationEventMap;
    private StreamEventPool streamEventPool;
    private Map<TimePeriod.Duration, Table> aggregationTables;
    private Map<TimePeriod.Duration, Object> inMemoryStoreMap;
    private final StreamEvent resetEvent;
    private final StreamEventCloner storeEventCloner;

    public IncrementalAggregateCompileCondition(Map<TimePeriod.Duration, CompiledCondition> tableCompiledConditions,
            Map<TimePeriod.Duration, CompiledCondition> inMemoryStoreCompileConditions,
            List<ExpressionExecutor> baseExecutors,
            Map<TimePeriod.Duration, ComplexEventChunk<StreamEvent>> runningAggregationEventMap,
            Map<TimePeriod.Duration, Table> aggregationTables, Map<TimePeriod.Duration, Object> inMemoryStoreMap,
            MetaStreamEvent metaStreamEvent) {
        this.tableCompiledConditions = tableCompiledConditions;
        this.inMemoryStoreCompileConditions = inMemoryStoreCompileConditions;
        this.baseExecutors = baseExecutors;
        this.runningAggregationEventMap = runningAggregationEventMap;
        this.streamEventPool = new StreamEventPool(metaStreamEvent, 10);
        this.aggregationTables = aggregationTables;
        this.inMemoryStoreMap = inMemoryStoreMap;
        this.resetEvent = streamEventPool.borrowEvent();
        this.resetEvent.setType(ComplexEvent.Type.RESET);
        this.storeEventCloner = new StreamEventCloner(metaStreamEvent, streamEventPool);
    }

    @Override
    public CompiledCondition cloneCompiledCondition(String key) {
        return null;
    }

    public StreamEvent find(StateEvent matchingEvent, TimePeriod.Duration perValue,
            boolean perEqualsRootExecutorDuration) {
        ComplexEventChunk<StreamEvent> complexEventChunk = new ComplexEventChunk<>(true);
        StreamEvent matchFromPersistedEvents = aggregationTables.get(perValue).find(matchingEvent,
                tableCompiledConditions.get(perValue));
        complexEventChunk.add(matchFromPersistedEvents);
        // Write the inMemory (running aggregate and buffer) data to inMemory tables before processing
        // Initially write inMemory values for 'perValue' to table
        writeInMemoryAggregatesToTables(perValue, inMemoryStoreMap);
        // Call find method of that table
        StreamEvent matchFromInMemory = ((Operator) inMemoryStoreCompileConditions.get(perValue)).find(matchingEvent,
                runningAggregationEventMap.get(perValue), storeEventCloner);
        runningAggregationEventMap.get(perValue).clear();

        if (!perEqualsRootExecutorDuration && matchFromInMemory != null) {
            ComplexEventChunk<StreamEvent> tempEventChunk = new ComplexEventChunk<>(true);
            tempEventChunk.add(matchFromInMemory);
            // TODO: 7/25/17 add the and condition when the compile condition is for within only
            // If the 'per' is for root executor, there would be no executors prior to that
            Object[] outputDataOfStreamEvent = new Object[matchFromInMemory.getOutputData().length];
            outputDataOfStreamEvent[0] = matchFromInMemory.getTimestamp();
            for (int i = perValue.ordinal() - 1; i >= 0; i--) {
                TimePeriod.Duration duration = TimePeriod.Duration.values()[i];
                writeInMemoryAggregatesToTables(duration, inMemoryStoreMap);
                tempEventChunk.add(((Operator) inMemoryStoreCompileConditions.get(duration)).find(matchingEvent,
                        runningAggregationEventMap.get(duration), storeEventCloner));
                runningAggregationEventMap.get(duration).clear();
            }
            // After retrieving inMemory values for all durations, the base executors must be executed on all of them
            // i.e. For a running hour, some of the values would be residing in min and sec executors. All those values
            // must be aggregated to get final result.
            for (int i = 1; i < baseExecutors.size(); i++) {
                outputDataOfStreamEvent[i] = baseExecutors.get(i).execute(tempEventChunk.getFirst());
                baseExecutors.get(i).execute(resetEvent);
            }
            matchFromInMemory.setOutputData(outputDataOfStreamEvent);
        }
        complexEventChunk.add(matchFromInMemory);
        return complexEventChunk.getFirst();
    }

    private void writeInMemoryAggregatesToTables(TimePeriod.Duration duration,
            Map<TimePeriod.Duration, Object> inMemoryStoreMap) {
        Object inMemoryStoreObject = inMemoryStoreMap.get(duration);
        if (inMemoryStoreObject instanceof ArrayList) {
            for (int i = 0; i < ((ArrayList) inMemoryStoreObject).size(); i++) {
                if (((ArrayList) inMemoryStoreObject).get(i) instanceof IncrementalExecutor.BaseIncrementalValueStore) {
                    // inMemory store is baseIncrementalValueStoreList
                    StreamEvent streamEvent = createStreamEvent(
                            (IncrementalExecutor.BaseIncrementalValueStore) ((ArrayList) inMemoryStoreObject).get(i),
                            streamEventPool);
                    runningAggregationEventMap.get(duration).add(streamEvent);
                } else { // inMemory store is baseIncrementalValueGroupByStoreList
                    assert ((ArrayList) inMemoryStoreObject).get(i) instanceof Map;
                    Map inMemoryMap = (Map) ((ArrayList) inMemoryStoreObject).get(i);
                    for (Object aBaseIncrementalValueStore : inMemoryMap.values()) {
                        StreamEvent streamEvent = createStreamEvent(
                                (IncrementalExecutor.BaseIncrementalValueStore) aBaseIncrementalValueStore,
                                streamEventPool);
                        runningAggregationEventMap.get(duration).add(streamEvent);
                    }
                }
            }
        } else if (inMemoryStoreObject instanceof Map) {
            // inMemory store is baseIncrementalValueStoreMap
            for (Object aBaseIncrementalValueStore : ((Map) inMemoryStoreObject).values()) {
                StreamEvent streamEvent = createStreamEvent(
                        (IncrementalExecutor.BaseIncrementalValueStore) aBaseIncrementalValueStore, streamEventPool);
                runningAggregationEventMap.get(duration).add(streamEvent);
            }
        } else {
            // inMemory store is baseIncrementalValueStore
            StreamEvent streamEvent = createStreamEvent(
                    (IncrementalExecutor.BaseIncrementalValueStore) inMemoryStoreObject, streamEventPool);
            runningAggregationEventMap.get(duration).add(streamEvent);
        }
    }

    private StreamEvent createStreamEvent(IncrementalExecutor.BaseIncrementalValueStore aBaseIncrementalValueStore,
            StreamEventPool streamEventPool) {
        StreamEvent streamEvent = streamEventPool.borrowEvent();
        streamEvent.setTimestamp(aBaseIncrementalValueStore.getTimestamp());
        aBaseIncrementalValueStore.setValue(aBaseIncrementalValueStore.getTimestamp(), 0);
        streamEvent.setOutputData(aBaseIncrementalValueStore.getValues());
        return streamEvent;
    }
}
