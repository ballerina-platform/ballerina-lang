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
import org.wso2.siddhi.core.util.IncrementalTimeConverterUtil;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncrementalAggregateCompileCondition implements CompiledCondition {
    private Map<TimePeriod.Duration, CompiledCondition> tableCompiledConditions;
    private CompiledCondition inMemoryStoreCompileCondition;
    private CompiledCondition finalCompiledCondition;
    private List<ExpressionExecutor> baseExecutors;
    private List<ExpressionExecutor> outputExpressionExecutors;
    private StreamEventPool streamEventPoolForInternalMeta;
    private StreamEventPool streamEventPoolForFinalMeta;
    private Map<TimePeriod.Duration, Table> aggregationTables;
    private Map<TimePeriod.Duration, Object> inMemoryStoreMap;
    private List<TimePeriod.Duration> incrementalDurations;
    private Long startTime;
    private Long endTime;
    private List<List<ExpressionExecutor>> executorListForSingleWithin;
    private final StreamEvent resetEvent;
    private final StreamEventCloner storeEventCloner;
    private final StreamEventCloner outputEventCloner;
    private final Map<Long, Map<String, Object[]>> inMemoryAggregateMap = new HashMap<>();

    public IncrementalAggregateCompileCondition(Map<TimePeriod.Duration, CompiledCondition> tableCompiledConditions,
                                                CompiledCondition inMemoryStoreCompileCondition, CompiledCondition finalCompiledCondition,
                                                List<ExpressionExecutor> baseExecutors, Map<TimePeriod.Duration, Table> aggregationTables,
                                                Map<TimePeriod.Duration, Object> inMemoryStoreMap, MetaStreamEvent metaStreamEvent,
                                                List<TimePeriod.Duration> incrementalDurations, List<ExpressionExecutor> outputExpressionExecutors,
                                                MetaStreamEvent finalOutputMeta, Long startTime, Long endTime,
                                                List<List<ExpressionExecutor>> executorListForSingleWithin) {
        this.tableCompiledConditions = tableCompiledConditions;
        this.inMemoryStoreCompileCondition = inMemoryStoreCompileCondition;
        this.finalCompiledCondition = finalCompiledCondition;
        this.baseExecutors = baseExecutors;
        this.outputExpressionExecutors = outputExpressionExecutors;
        this.streamEventPoolForInternalMeta = new StreamEventPool(metaStreamEvent, 10);
        this.streamEventPoolForFinalMeta = new StreamEventPool(finalOutputMeta, 10);
        this.aggregationTables = aggregationTables;
        this.inMemoryStoreMap = inMemoryStoreMap;
        this.incrementalDurations = incrementalDurations;
        this.resetEvent = streamEventPoolForInternalMeta.borrowEvent();
        this.resetEvent.setType(ComplexEvent.Type.RESET);
        this.storeEventCloner = new StreamEventCloner(metaStreamEvent, streamEventPoolForInternalMeta);
        this.outputEventCloner = new StreamEventCloner(finalOutputMeta, streamEventPoolForFinalMeta);
        this.startTime = startTime;
        this.endTime = endTime;
        this.executorListForSingleWithin = executorListForSingleWithin;
    }

    @Override
    public CompiledCondition cloneCompiledCondition(String key) {
        return null;
    }

    public StreamEvent find(StateEvent matchingEvent, TimePeriod.Duration perValue,
            boolean perEqualsRootExecutorDuration) {
        ComplexEventChunk<StreamEvent> complexEventChunk = new ComplexEventChunk<>(true);
        if (!executorListForSingleWithin.isEmpty()) {
            deriveStartTimeEndTime(matchingEvent);
        }
        StreamEvent matchFromPersistedEvents = aggregationTables.get(perValue).find(matchingEvent,
                tableCompiledConditions.get(perValue));
        complexEventChunk.add(matchFromPersistedEvents);

        findAndProcessInMemoryData(inMemoryStoreMap.get(perValue), matchingEvent, perValue);

        if (!perEqualsRootExecutorDuration) {
            // If the 'per' is for root executor, there would be no executors prior to that
            for (int i = perValue.ordinal() - 1; i >= 0
                    && incrementalDurations.contains(TimePeriod.Duration.values()[i]); i--) {
                // if (matchFromInMemoryFound) {
                TimePeriod.Duration duration = TimePeriod.Duration.values()[i];
                findAndProcessInMemoryData(inMemoryStoreMap.get(duration), matchingEvent, perValue);
            }
        }
        for (Map<String, Object[]> executorsAndOutputDataMap : inMemoryAggregateMap.values()) {
            for (Object[] executorsAndOutputData : executorsAndOutputDataMap.values()) {
                List<ExpressionExecutor> executors = (List<ExpressionExecutor>) executorsAndOutputData[0];
                Object[] outputData = (Object[]) executorsAndOutputData[1];
                StreamEvent streamEvent = streamEventPoolForInternalMeta.borrowEvent();
                streamEvent.setTimestamp((long) outputData[0]);
                streamEvent.setOutputData(outputData);
                complexEventChunk.add(streamEvent);
                for (ExpressionExecutor expressionExecutor : executors) {
                    expressionExecutor.execute(resetEvent);
                }
            }
        }
        return executeFinalCompileConditionOnWithinData(matchingEvent, complexEventChunk);
    }

    private void findAndProcessInMemoryData(Object inMemoryStore, StateEvent matchingEvent,
            TimePeriod.Duration perValue) {
        if (inMemoryStore instanceof ArrayList) {
            for (int i = 0; i < ((ArrayList) inMemoryStore).size(); i++) {
                if (((ArrayList) inMemoryStore).get(i) instanceof IncrementalExecutor.BaseIncrementalValueStore) {
                    // inMemory store is baseIncrementalValueStoreList
                    ComplexEventChunk<StreamEvent> complexEventChunk = createComplexEventChunk(
                            (IncrementalExecutor.BaseIncrementalValueStore) ((ArrayList) inMemoryStore).get(i),
                            streamEventPoolForInternalMeta);
                    StreamEvent matchFromInMemory = ((Operator) inMemoryStoreCompileCondition).find(matchingEvent,
                            complexEventChunk, storeEventCloner);
                    if (matchFromInMemory != null) {
                        updateInMemoryAggregateMap(perValue, matchFromInMemory, "ALL");
                    }
                } else { // inMemory store is baseIncrementalValueGroupByStoreList
                    assert ((ArrayList) inMemoryStore).get(i) instanceof Map;
                    Map inMemoryMap = (Map) ((ArrayList) inMemoryStore).get(i);
                    for (Object groupByValue : inMemoryMap.keySet()) {
                        ComplexEventChunk<StreamEvent> complexEventChunk = createComplexEventChunk(
                                (IncrementalExecutor.BaseIncrementalValueStore) inMemoryMap.get(groupByValue),
                                streamEventPoolForInternalMeta);
                        StreamEvent matchFromInMemory = ((Operator) inMemoryStoreCompileCondition).find(matchingEvent,
                                complexEventChunk, storeEventCloner);
                        if (matchFromInMemory != null) {
                            updateInMemoryAggregateMap(perValue, matchFromInMemory, (String) groupByValue);
                        }
                    }
                }
            }
        } else if (inMemoryStore instanceof Map) {
            // inMemory store is baseIncrementalValueStoreMap
            Map inMemoryMap = (Map) inMemoryStore;
            for (Object groupByValue : inMemoryMap.keySet()) {
                ComplexEventChunk<StreamEvent> complexEventChunk = createComplexEventChunk(
                        (IncrementalExecutor.BaseIncrementalValueStore) inMemoryMap.get(groupByValue),
                        streamEventPoolForInternalMeta);
                StreamEvent matchFromInMemory = ((Operator) inMemoryStoreCompileCondition).find(matchingEvent,
                        complexEventChunk, storeEventCloner);
                if (matchFromInMemory != null) {
                    updateInMemoryAggregateMap(perValue, matchFromInMemory, (String) groupByValue);
                }
            }
        } else {
            // inMemory store is baseIncrementalValueStore
            ComplexEventChunk<StreamEvent> complexEventChunk = createComplexEventChunk(
                    (IncrementalExecutor.BaseIncrementalValueStore) inMemoryStore, streamEventPoolForInternalMeta);
            StreamEvent matchFromInMemory = ((Operator) inMemoryStoreCompileCondition).find(matchingEvent,
                    complexEventChunk, storeEventCloner);
            if (matchFromInMemory != null) {
                updateInMemoryAggregateMap(perValue, matchFromInMemory, "ALL");
            }
        }
    }

    private ComplexEventChunk<StreamEvent> createComplexEventChunk(
            IncrementalExecutor.BaseIncrementalValueStore aBaseIncrementalValueStore, StreamEventPool streamEventPool) {
        ComplexEventChunk<StreamEvent> complexEventChunk = new ComplexEventChunk<>(true);
        StreamEvent streamEvent = streamEventPool.borrowEvent();
        streamEvent.setTimestamp(aBaseIncrementalValueStore.getTimestamp());
        aBaseIncrementalValueStore.setValue(aBaseIncrementalValueStore.getTimestamp(), 0);
        streamEvent.setOutputData(aBaseIncrementalValueStore.getValues());
        complexEventChunk.add(streamEvent);
        return complexEventChunk;
    }

    private static List<ExpressionExecutor> cloneExpressionExecutors(List<ExpressionExecutor> expressionExecutors) {
        List<ExpressionExecutor> arrayList = new ArrayList<>();
        for (ExpressionExecutor expressionExecutor : expressionExecutors) {
            arrayList.add(expressionExecutor.cloneExecutor(null));
        }
        return arrayList;
    }

    private void updateInMemoryAggregateMap(TimePeriod.Duration perValue, StreamEvent matchFromInMemory,
            String groupByKey) {
        long timeBucket = IncrementalTimeConverterUtil.getStartTimeOfAggregates(matchFromInMemory.getTimestamp(),
                perValue);
        List<ExpressionExecutor> baseExecutors;
        Object[] outputData;
        if (inMemoryAggregateMap.get(timeBucket) != null) {
            if (inMemoryAggregateMap.get(timeBucket).get(groupByKey) != null) {
                baseExecutors = (List<ExpressionExecutor>) inMemoryAggregateMap.get(timeBucket).get(groupByKey)[0];
                outputData = (Object[]) inMemoryAggregateMap.get(timeBucket).get(groupByKey)[1];
            } else {
                baseExecutors = cloneExpressionExecutors(this.baseExecutors);
                for (ExpressionExecutor baseExecutor : baseExecutors) {
                    baseExecutor.execute(resetEvent);
                }
                outputData = new Object[matchFromInMemory.getOutputData().length];
                Object[] executorsAndOutputData = new Object[] { baseExecutors, outputData };
                outputData[0] = timeBucket;
                inMemoryAggregateMap.get(timeBucket).put(groupByKey, executorsAndOutputData);
            }
        } else {
            baseExecutors = cloneExpressionExecutors(this.baseExecutors);
            for (ExpressionExecutor baseExecutor : baseExecutors) {
                baseExecutor.execute(resetEvent);
            }
            outputData = new Object[matchFromInMemory.getOutputData().length];
            Object[] executorsAndOutputData = new Object[] { baseExecutors, outputData };
            outputData[0] = timeBucket;
            Map<String, Object[]> executorsAndOutputDataMap = new HashMap<>();
            executorsAndOutputDataMap.put(groupByKey, executorsAndOutputData);
            inMemoryAggregateMap.put(timeBucket, executorsAndOutputDataMap);
        }
        for (int j = 1; j < baseExecutors.size(); j++) {
            outputData[j] = baseExecutors.get(j).execute(matchFromInMemory);
        }
    }

    private StreamEvent executeFinalCompileConditionOnWithinData(StateEvent matchingEvent,
            ComplexEventChunk<StreamEvent> complexEventChunk) {
        ComplexEventChunk<StreamEvent> finalComplexEventChunk = new ComplexEventChunk<>(true);
        while (complexEventChunk.hasNext()) {
            StreamEvent streamEvent = complexEventChunk.next();
            StreamEvent newStreamEvent = streamEventPoolForFinalMeta.borrowEvent();
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
        return ((Operator) finalCompiledCondition).find(matchingEvent, finalComplexEventChunk, outputEventCloner);
    }

    private void deriveStartTimeEndTime(StateEvent matchingEvent) {
        int i = 0;
        for (List<ExpressionExecutor> regexMatchAndTimeDeriver : executorListForSingleWithin) {
            if ((Boolean) regexMatchAndTimeDeriver.get(0).execute(matchingEvent)){
                startTime = (Long) regexMatchAndTimeDeriver.get(1).execute(matchingEvent);
                endTime = IncrementalTimeConverterUtil.getNextEmitTime(startTime, TimePeriod.Duration.values()[i]);
                break;
            }
            i++;
        }
    }
}
