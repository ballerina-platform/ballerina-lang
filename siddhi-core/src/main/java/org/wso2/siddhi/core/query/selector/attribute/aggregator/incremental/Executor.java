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

import org.wso2.siddhi.core.event.ComplexEventChunk;

/**
 * This is the parent interface representing Event executor for incremental execution in Siddhi.
 * Each event executor ({@link IncrementalExecutor}) corresponds to a duration specified in
 * {@link org.wso2.siddhi.query.api.aggregation.TimePeriod.Duration}.
 * Apart from that, {@link org.wso2.siddhi.core.query.input.stream.single.EntryValveExecutor}
 * which is a special implementation of this interface, marks the entry point to the rest of the
 * incremental executors. All of these executors are chained in such a way, that
 * {@link org.wso2.siddhi.core.query.input.stream.single.EntryValveExecutor} links to the first
 * {@link IncrementalExecutor} corresponding to the minimum duration, and the rest of the {@link IncrementalExecutor}s
 * link in ascending order of durations. A {@link ComplexEventChunk} received by a certain executor
 * would thus be handed over to next executor in-line.
 */
public interface Executor {

    /**
     * Execute the handed StreamEvent
     *
     * @param complexEventChunk event chunk to be processed
     */
    void execute(ComplexEventChunk complexEventChunk);

    /**
     * Get next executor element in the execution chain. Executed event should be sent to next executor
     *
     * @return next executor
     */
    Executor getNextExecutor();

    /**
     * Set next executor element in execution chain
     *
     */
    void setNextExecutor(Executor executor);

}
