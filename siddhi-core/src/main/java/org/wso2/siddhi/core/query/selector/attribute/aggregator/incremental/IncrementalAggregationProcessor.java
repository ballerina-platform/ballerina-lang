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

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;

import java.util.List;

/**
 * Incremental Aggregation Processor to consume events to Incremental Aggregators
 */
public class IncrementalAggregationProcessor implements Processor {
    private final List<ExpressionExecutor> incomingExpressionExecutors;
    private final MetaStreamEvent processedMetaStreamEvent;
    private final StreamEventPool streamEventPool;
    private IncrementalExecutor incrementalExecutor;

    public IncrementalAggregationProcessor(IncrementalExecutor incrementalExecutor,
                                           List<ExpressionExecutor> incomingExpressionExecutors,
                                           MetaStreamEvent processedMetaStreamEvent) {
        this.incrementalExecutor = incrementalExecutor;
        this.incomingExpressionExecutors = incomingExpressionExecutors;
        this.processedMetaStreamEvent = processedMetaStreamEvent;
        this.streamEventPool = new StreamEventPool(processedMetaStreamEvent, 5);
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        ComplexEventChunk<StreamEvent> streamEventChunk =
                new ComplexEventChunk<>(complexEventChunk.isBatch());
        while (complexEventChunk.hasNext()) {
            ComplexEvent complexEvent = complexEventChunk.next();
            StreamEvent borrowedEvent = streamEventPool.borrowEvent();
            for (int i = 0; i < incomingExpressionExecutors.size(); i++) {
                ExpressionExecutor expressionExecutor = incomingExpressionExecutors.get(i);
                borrowedEvent.setOutputData(expressionExecutor.execute(complexEvent), i);
            }
            streamEventChunk.add(borrowedEvent);
        }
        incrementalExecutor.execute(streamEventChunk);
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
