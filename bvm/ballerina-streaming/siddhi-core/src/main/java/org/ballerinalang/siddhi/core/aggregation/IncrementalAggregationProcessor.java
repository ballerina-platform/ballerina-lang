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
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.core.exception.SiddhiAppRuntimeException;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.incremental.IncrementalUnixTimeFunctionExecutor;
import org.ballerinalang.siddhi.core.query.processor.Processor;
import org.ballerinalang.siddhi.core.util.statistics.LatencyTracker;
import org.ballerinalang.siddhi.core.util.statistics.ThroughputTracker;

import java.util.List;

/**
 * Incremental Aggregation Processor to consume events to Incremental Aggregators.
 */
public class IncrementalAggregationProcessor implements Processor {
    private final List<ExpressionExecutor> incomingExpressionExecutors;
    private final MetaStreamEvent processedMetaStreamEvent;
    private final StreamEventPool streamEventPool;
    private final LatencyTracker latencyTrackerInsert;
    private final ThroughputTracker throughputTrackerInsert;
    private SiddhiAppContext siddhiAppContext;
    private IncrementalExecutor incrementalExecutor;

    public IncrementalAggregationProcessor(IncrementalExecutor incrementalExecutor,
                                           List<ExpressionExecutor> incomingExpressionExecutors,
                                           MetaStreamEvent processedMetaStreamEvent,
                                           LatencyTracker latencyTrackerInsert,
                                           ThroughputTracker throughputTrackerInsert,
                                           SiddhiAppContext siddhiAppContext) {
        this.incrementalExecutor = incrementalExecutor;
        this.incomingExpressionExecutors = incomingExpressionExecutors;
        this.processedMetaStreamEvent = processedMetaStreamEvent;
        this.streamEventPool = new StreamEventPool(processedMetaStreamEvent, 5);
        this.latencyTrackerInsert = latencyTrackerInsert;
        this.throughputTrackerInsert = throughputTrackerInsert;
        this.siddhiAppContext = siddhiAppContext;
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        ComplexEventChunk<StreamEvent> streamEventChunk =
                new ComplexEventChunk<>(complexEventChunk.isBatch());
        try {
            int noOfEvents = 0;
            if (latencyTrackerInsert != null && siddhiAppContext.isStatsEnabled()) {
                latencyTrackerInsert.markIn();
            }
            while (complexEventChunk.hasNext()) {
                ComplexEvent complexEvent = complexEventChunk.next();
                StreamEvent borrowedEvent = streamEventPool.borrowEvent();
                for (int i = 0; i < incomingExpressionExecutors.size(); i++) {
                    ExpressionExecutor expressionExecutor = incomingExpressionExecutors.get(i);
                    Object outputData = expressionExecutor.execute(complexEvent);
                    if (expressionExecutor instanceof IncrementalUnixTimeFunctionExecutor && outputData == null) {
                        throw new SiddhiAppRuntimeException("Cannot retrieve the timestamp of event");
                    }
                    borrowedEvent.setOutputData(outputData, i);
                }
                streamEventChunk.add(borrowedEvent);
                noOfEvents++;
            }
            incrementalExecutor.execute(streamEventChunk);
            if (throughputTrackerInsert != null && siddhiAppContext.isStatsEnabled()) {
                throughputTrackerInsert.eventsIn(noOfEvents);
            }
        } finally {
            if (latencyTrackerInsert != null && siddhiAppContext.isStatsEnabled()) {
                latencyTrackerInsert.markOut();
            }
        }

    }

    @Override
    public Processor getNextProcessor() {
        return null;
    }

    @Override
    public void setNextProcessor(Processor processor) {
        throw new SiddhiAppCreationException("IncrementalAggregationProcessor does not support any next processor");
    }

    @Override
    public void setToLast(Processor processor) {
        throw new SiddhiAppCreationException("IncrementalAggregationProcessor does not support any " +
                "next/last processor");
    }

    @Override
    public Processor cloneProcessor(String key) {
        throw new SiddhiAppCreationException("IncrementalAggregationProcessor cannot be cloned");
    }
}
