/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.query.output.ratelimit.snapshot;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.state.StateEventCloner;
import org.wso2.siddhi.core.event.state.StateEventPool;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.selector.attribute.processor.AttributeProcessor;
import org.wso2.siddhi.core.query.selector.attribute.processor.executor.AbstractAggregationAttributeExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class WrappedSnapshotOutputRateLimiter extends OutputRateLimiter {
    SnapshotOutputRateLimiter outputRateLimiter;
    private String id;
    private final Long value;
    private final ScheduledExecutorService scheduledExecutorService;
    private final boolean groupBy;
    private final boolean windowed;
    private List<Integer> aggregateAttributePositionList = new ArrayList<Integer>();

    public WrappedSnapshotOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService, boolean isGroupBy, boolean isWindowed) {
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
        groupBy = isGroupBy;
        windowed = isWindowed;

    }

    @Override
    public OutputRateLimiter clone(String key) {
        WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter = new WrappedSnapshotOutputRateLimiter(id + key, value, scheduledExecutorService, groupBy, windowed);
        wrappedSnapshotOutputRateLimiter.outputRateLimiter = this.outputRateLimiter.clone(key, wrappedSnapshotOutputRateLimiter);
        wrappedSnapshotOutputRateLimiter.setLatencyTracker(latencyTracker);
        return wrappedSnapshotOutputRateLimiter;
    }

    public void init(int outPutAttributeSize, List<AttributeProcessor> attributeProcessorList, MetaComplexEvent metaComplexEvent) {
        for (AttributeProcessor attributeProcessor : attributeProcessorList) {
            if (attributeProcessor.getExpressionExecutor() instanceof AbstractAggregationAttributeExecutor) {
                aggregateAttributePositionList.add(attributeProcessor.getOutputPosition());
            }
        }

        if (windowed) {
            if (groupBy) {
                if (outPutAttributeSize == aggregateAttributePositionList.size()) {   //All Aggregation
                    outputRateLimiter = new AllAggregationGroupByWindowedPerSnapshotOutputRateLimiter(id, value, scheduledExecutorService, this);
                } else if (aggregateAttributePositionList.size() > 0) {   //Some Aggregation
                    outputRateLimiter = new AggregationGroupByWindowedPerSnapshotOutputRateLimiter(id, value, scheduledExecutorService, aggregateAttributePositionList, this);
                } else { // No aggregation
                    //GroupBy is same as Non GroupBy
                    outputRateLimiter = new WindowedPerSnapshotOutputRateLimiter(id, value, scheduledExecutorService, this);
                }
            } else {
                if (outPutAttributeSize == aggregateAttributePositionList.size()) {   //All Aggregation
                    outputRateLimiter = new AllAggregationPerSnapshotOutputRateLimiter(id, value, scheduledExecutorService, this);
                } else if (aggregateAttributePositionList.size() > 0) {   //Some Aggregation
                    outputRateLimiter = new AggregationWindowedPerSnapshotOutputRateLimiter(id, value, scheduledExecutorService, aggregateAttributePositionList, this);
                } else { // No aggregation
                    outputRateLimiter = new WindowedPerSnapshotOutputRateLimiter(id, value, scheduledExecutorService, this);
                }
            }

        } else {
            if (groupBy) {
                outputRateLimiter = new GroupByPerSnapshotOutputRateLimiter(id, value, scheduledExecutorService, this);
            } else {
                outputRateLimiter = new PerSnapshotOutputRateLimiter(id, value, scheduledExecutorService, this);
            }
        }


        if (metaComplexEvent instanceof MetaStateEvent) {
            StateEventPool stateEventPool = new StateEventPool((MetaStateEvent) metaComplexEvent, 5);
            outputRateLimiter.setStateEventCloner(new StateEventCloner((MetaStateEvent) metaComplexEvent, stateEventPool));
        } else {
            StreamEventPool streamEventPool = new StreamEventPool((MetaStreamEvent) metaComplexEvent, 5);
            outputRateLimiter.setStreamEventCloner(new StreamEventCloner((MetaStreamEvent) metaComplexEvent, streamEventPool));
        }

    }


    @Override
    public void start() {
        outputRateLimiter.start();
    }

    @Override
    public void stop() {
        outputRateLimiter.stop();
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        outputRateLimiter.process(complexEventChunk);
    }

    public void passToCallBacks(ComplexEventChunk complexEventChunk) {
        sendToCallBacks(complexEventChunk);
    }

    @Override
    public Object[] currentState() {
        return outputRateLimiter.currentState();
    }

    @Override
    public void restoreState(Object[] state) {
        outputRateLimiter.restoreState(state);
    }
}
