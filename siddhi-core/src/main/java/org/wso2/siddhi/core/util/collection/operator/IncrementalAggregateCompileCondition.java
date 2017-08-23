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
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalDataAggregator;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.IncrementalExecutor;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;

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

    private final StreamEventPool streamEventPoolForTableMeta;
    private final StreamEventCloner tableEventCloner;
    private final StreamEventPool streamEventPoolForAggregateMeta;
    private final StreamEventCloner aggregateEventCloner;

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
            List<ExpressionExecutor> baseExecutors, ExpressionExecutor timestampExecutor,
            List<ExpressionExecutor> outputExpressionExecutors) {

        ComplexEventChunk<StreamEvent> complexEventChunkToHoldWithinMatches = new ComplexEventChunk<>(true);

        // Get all the aggregates within the given duration, from table corresponding to "per" duration
        StreamEvent withinMatchFromPersistedEvents = tableForPerDuration.find(matchingEvent,
                withinTableCompiledConditions.get(perValue));
        complexEventChunkToHoldWithinMatches.add(withinMatchFromPersistedEvents);

        // Optimization step.
        // Get the newest and oldest events from in-memory (running) aggregates, and
        // check whether at least one of those events is within the given time range. If it's not the case,
        // there's no need to iterate through in-memory data.
        BaseIncrementalValueStore newestInMemoryEvent = getNewestInMemoryEvent(incrementalExecutorMap,
                incrementalDurations, perValue);
        BaseIncrementalValueStore oldestInMemoryEvent = getOldestInMemoryEvent(incrementalExecutorMap,
                incrementalDurations, perValue);

        if (requiresAggregatingInMemoryData(newestInMemoryEvent, oldestInMemoryEvent, matchingEvent)) {
            IncrementalDataAggregator incrementalDataAggregator = new IncrementalDataAggregator(incrementalDurations,
                    perValue, baseExecutors, timestampExecutor, tableMetaStreamEvent);

            // Aggregate in-memory data and create an event chunk out of it
            ComplexEventChunk<StreamEvent> aggregatedInMemoryEventChunk = incrementalDataAggregator
                    .aggregateInMemoryData(incrementalExecutorMap);

            // Get the in-memory aggregate data, which is within given duration
            StreamEvent withinMatchFromInMemory = ((Operator) inMemoryStoreCompileCondition).find(matchingEvent,
                    aggregatedInMemoryEventChunk, tableEventCloner);
            complexEventChunkToHoldWithinMatches.add(withinMatchFromInMemory);
        }

        // Get the final event chunk from the data which is within given duration. This event chunk contains the values
        // in the select clause of an aggregate definition.
        ComplexEventChunk<StreamEvent> aggregateSelectionComplexEventChunk = createAggregateSelectionEventChunk(
                complexEventChunkToHoldWithinMatches, outputExpressionExecutors);

        // Execute the on compile condition
        return ((Operator) onCompiledCondition).find(matchingEvent, aggregateSelectionComplexEventChunk,
                aggregateEventCloner);
    }

    private ComplexEventChunk<StreamEvent> createAggregateSelectionEventChunk(
            ComplexEventChunk<StreamEvent> complexEventChunkToHoldMatches,
            List<ExpressionExecutor> outputExpressionExecutors) {
        ComplexEventChunk<StreamEvent> aggregateSelectionComplexEventChunk = new ComplexEventChunk<>(true);
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
            aggregateSelectionComplexEventChunk.add(newStreamEvent);
        }

        for (ExpressionExecutor expressionExecutor : outputExpressionExecutors) {
            expressionExecutor.execute(resetEvent);
        }

        return aggregateSelectionComplexEventChunk;
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
            List<TimePeriod.Duration> incrementalDurations, TimePeriod.Duration perValue) {
        BaseIncrementalValueStore newestEvent;
        for (TimePeriod.Duration incrementalDuration : incrementalDurations) {
            newestEvent = incrementalExecutorMap.get(incrementalDuration).getNewestEvent();
            if (newestEvent != null) {
                return newestEvent;
            }
            if (incrementalDuration == perValue) {
                break;
            }
        }
        return null;
    }

    private BaseIncrementalValueStore getOldestInMemoryEvent(
            Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap,
            List<TimePeriod.Duration> incrementalDurations, TimePeriod.Duration perValue) {
        BaseIncrementalValueStore oldestEvent;
        TimePeriod.Duration incrementalDuration;
        for (int i = perValue.ordinal(); i >= 0; i--) {
            incrementalDuration = TimePeriod.Duration.values()[i];
            oldestEvent = incrementalExecutorMap.get(incrementalDuration).getOldestEvent();
            if (oldestEvent != null) {
                return oldestEvent;
            }
        }
        return null;
    }
}
