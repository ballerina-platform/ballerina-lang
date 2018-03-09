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

package org.ballerinalang.siddhi.core.util.collection.operator;

import org.ballerinalang.siddhi.core.aggregation.IncrementalDataAggregator;
import org.ballerinalang.siddhi.core.aggregation.IncrementalExecutor;
import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.ComplexEvent;
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.state.StateEvent;
import org.ballerinalang.siddhi.core.event.stream.MetaStreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventCloner;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.ballerinalang.siddhi.core.exception.SiddhiAppRuntimeException;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.table.Table;
import org.ballerinalang.siddhi.query.api.aggregation.TimePeriod;
import org.ballerinalang.siddhi.query.api.definition.AggregationDefinition;
import org.ballerinalang.siddhi.query.api.definition.Attribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines the logic to find a matching event from an incremental aggregator (retrieval from incremental aggregator),
 * based on the logical conditions defined herewith.
 */
public class IncrementalAggregateCompileCondition implements CompiledCondition {
    private final StreamEventPool streamEventPoolForTableMeta;
    private final StreamEventCloner tableEventCloner;
    private final StreamEventPool streamEventPoolForAggregateMeta;
    private final StreamEventCloner aggregateEventCloner;
    private final List<Attribute> additionalAttributes;
    private Map<TimePeriod.Duration, CompiledCondition> withinTableCompiledConditions;
    private CompiledCondition inMemoryStoreCompileCondition;
    private CompiledCondition onCompiledCondition;
    private MetaStreamEvent tableMetaStreamEvent;
    private MetaStreamEvent aggregateMetaStreamEvent;
    private ComplexEventPopulater complexEventPopulater;
    private MatchingMetaInfoHolder alteredMatchingMetaInfoHolder;
    private ExpressionExecutor perExpressionExecutor;
    private ExpressionExecutor startTimeEndTimeExpressionExecutor;

    public IncrementalAggregateCompileCondition(
            Map<TimePeriod.Duration, CompiledCondition> withinTableCompiledConditions,
            CompiledCondition inMemoryStoreCompileCondition, CompiledCondition onCompiledCondition,
            MetaStreamEvent tableMetaStreamEvent, MetaStreamEvent aggregateMetaSteamEvent,
            List<Attribute> additionalAttributes, MatchingMetaInfoHolder alteredMatchingMetaInfoHolder,
            ExpressionExecutor perExpressionExecutor, ExpressionExecutor startTimeEndTimeExpressionExecutor) {
        this.withinTableCompiledConditions = withinTableCompiledConditions;
        this.inMemoryStoreCompileCondition = inMemoryStoreCompileCondition;
        this.onCompiledCondition = onCompiledCondition;
        this.tableMetaStreamEvent = tableMetaStreamEvent;
        this.aggregateMetaStreamEvent = aggregateMetaSteamEvent;

        this.streamEventPoolForTableMeta = new StreamEventPool(tableMetaStreamEvent, 10);
        this.tableEventCloner = new StreamEventCloner(tableMetaStreamEvent, streamEventPoolForTableMeta);

        this.streamEventPoolForAggregateMeta = new StreamEventPool(aggregateMetaSteamEvent, 10);
        this.aggregateEventCloner = new StreamEventCloner(aggregateMetaSteamEvent, streamEventPoolForAggregateMeta);
        this.additionalAttributes = additionalAttributes;
        this.alteredMatchingMetaInfoHolder = alteredMatchingMetaInfoHolder;
        this.perExpressionExecutor = perExpressionExecutor;
        this.startTimeEndTimeExpressionExecutor = startTimeEndTimeExpressionExecutor;
    }

    @Override
    public CompiledCondition cloneCompilation(String key) {
        Map<TimePeriod.Duration, CompiledCondition> copyOfWithinTableCompiledConditions = new HashMap<>();
        for (Map.Entry<TimePeriod.Duration, CompiledCondition> entry : withinTableCompiledConditions.entrySet()) {
            copyOfWithinTableCompiledConditions.put(entry.getKey(), entry.getValue().cloneCompilation(key));
        }
        return new IncrementalAggregateCompileCondition(copyOfWithinTableCompiledConditions,
                inMemoryStoreCompileCondition.cloneCompilation(key),
                onCompiledCondition.cloneCompilation(key), tableMetaStreamEvent, aggregateMetaStreamEvent,
                additionalAttributes, alteredMatchingMetaInfoHolder, perExpressionExecutor,
                startTimeEndTimeExpressionExecutor);
    }

    public StreamEvent find(StateEvent matchingEvent, AggregationDefinition aggregationDefinition,
                            Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap,
                            Map<TimePeriod.Duration, Table> aggregationTables,
                            List<TimePeriod.Duration> incrementalDurations,
                            List<ExpressionExecutor> baseExecutors, ExpressionExecutor timestampExecutor,
                            List<ExpressionExecutor> outputExpressionExecutors,
                            SiddhiAppContext siddhiAppContext) {

        ComplexEventChunk<StreamEvent> complexEventChunkToHoldWithinMatches = new ComplexEventChunk<>(true);

        // Retrieve per value
        String perValueAsString = perExpressionExecutor.execute(matchingEvent).toString();
        TimePeriod.Duration perValue = TimePeriod.Duration.valueOf(perValueAsString.toUpperCase());
        if (!incrementalExecutorMap.keySet().contains(perValue)) {
            throw new SiddhiAppRuntimeException("The aggregate values for " + perValue.toString()
                    + " granularity cannot be provided since aggregation definition " +
                    aggregationDefinition.getId() + " does not contain " + perValue.toString() + " duration");
        }

        Table tableForPerDuration = aggregationTables.get(perValue);

        Long[] startTimeEndTime = (Long[]) startTimeEndTimeExpressionExecutor.execute(matchingEvent);
        if (startTimeEndTime == null) {
            throw new SiddhiAppRuntimeException("Start and end times for within duration cannot be retrieved");
        }

        complexEventPopulater.populateComplexEvent(matchingEvent.getStreamEvent(0), startTimeEndTime);

        // Get all the aggregates within the given duration, from table corresponding to "per" duration
        StreamEvent withinMatchFromPersistedEvents = tableForPerDuration.find(matchingEvent,
                withinTableCompiledConditions.get(perValue));
        complexEventChunkToHoldWithinMatches.add(withinMatchFromPersistedEvents);

        // Optimization step.
        // Get the newest and oldest event timestamps from in-memory, and
        // check whether at least one of those timestamps fall out of the given time range. If that's the case,
        // there's no need to iterate through in-memory data.
        long oldestInMemoryEventTimestamp = getOldestInMemoryEventTimestamp(incrementalExecutorMap,
                incrementalDurations, perValue);
        long newestInMemoryEventTimestamp = getNewestInMemoryEventTimestamp(incrementalExecutorMap,
                incrementalDurations, perValue);

        if (requiresAggregatingInMemoryData(newestInMemoryEventTimestamp, oldestInMemoryEventTimestamp,
                startTimeEndTime)) {
            IncrementalDataAggregator incrementalDataAggregator = new IncrementalDataAggregator(incrementalDurations,
                    perValue, baseExecutors, timestampExecutor, tableMetaStreamEvent, siddhiAppContext);

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

    private boolean requiresAggregatingInMemoryData(long newestInMemoryEventTimestamp,
                                                    long oldestInMemoryEventTimestamp, Long[] startTimeEndTime) {
        if (newestInMemoryEventTimestamp == -1 && oldestInMemoryEventTimestamp == -1) {
            return false;
        }
        if (oldestInMemoryEventTimestamp != -1) {
            long endTimeForWithin = startTimeEndTime[1];
            if (endTimeForWithin <= oldestInMemoryEventTimestamp) {
                return false;
            }
        }
        if (newestInMemoryEventTimestamp != -1) {
            long startTimeForWithin = startTimeEndTime[0];
            if (newestInMemoryEventTimestamp < startTimeForWithin) {
                return false;
            }
        }
        return true;
    }

    private long getNewestInMemoryEventTimestamp(Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap,
                                                 List<TimePeriod.Duration> incrementalDurations,
                                                 TimePeriod.Duration perValue) {
        long newestEvent;
        for (TimePeriod.Duration incrementalDuration : incrementalDurations) {
            newestEvent = incrementalExecutorMap.get(incrementalDuration).getNewestEventTimestamp();
            if (newestEvent != -1) {
                return newestEvent;
            }
            if (incrementalDuration == perValue) {
                break;
            }
        }
        return -1;
    }

    private long getOldestInMemoryEventTimestamp(Map<TimePeriod.Duration, IncrementalExecutor> incrementalExecutorMap,
                                                 List<TimePeriod.Duration> incrementalDurations,
                                                 TimePeriod.Duration perValue) {
        long oldestEvent;
        TimePeriod.Duration incrementalDuration;
        for (int i = perValue.ordinal(); i >= incrementalDurations.get(0).ordinal(); i--) {
            incrementalDuration = TimePeriod.Duration.values()[i];
            oldestEvent = incrementalExecutorMap.get(incrementalDuration).getOldestEventTimestamp();
            if (oldestEvent != -1) {
                return oldestEvent;
            }
        }
        return -1;
    }

    public void setComplexEventPopulater(ComplexEventPopulater complexEventPopulater) {
        this.complexEventPopulater = complexEventPopulater;
    }

    public List<Attribute> getAdditionalAttributes() {
        return this.additionalAttributes;
    }

    public MatchingMetaInfoHolder getAlteredMatchingMetaInfoHolder() {
        return this.alteredMatchingMetaInfoHolder;
    }
}
