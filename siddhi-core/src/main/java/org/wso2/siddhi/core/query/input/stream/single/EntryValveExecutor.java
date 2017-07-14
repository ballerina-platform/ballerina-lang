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

package org.wso2.siddhi.core.query.input.stream.single;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental.Executor;
import org.wso2.siddhi.core.util.Schedulable;
import org.wso2.siddhi.core.util.ThreadBarrier;

/**
 * Entry point to incremental executors
 */
public class EntryValveExecutor implements Executor, Schedulable {
    private Executor next;
    private ThreadBarrier threadBarrier;

    public EntryValveExecutor(SiddhiAppContext siddhiAppContext) {
        threadBarrier = siddhiAppContext.getThreadBarrier();
    }

    /**
     * Execute the handed StreamEvent
     *
     * @param complexEventChunk event chunk to be executed
     */
    @Override
    public void execute(ComplexEventChunk complexEventChunk) {
        threadBarrier.pass();
        next.execute(complexEventChunk);
    }

    /**
     * Get next executor element in the executor chain. Executed event should be sent to next executor
     *
     * @return Next Executor
     */
    @Override
    public Executor getNextExecutor() {
        return next;
    }

    /**
     * Set next executor element in executor chain
     *
     * @param executor Executor to be set as next element of executor chain
     */
    @Override
    public void setNextExecutor(Executor executor) {
        next = executor;
    }

    /**
     * Implements process method in Schedulable interface.
     * Same logic in execute method applies here
     *
     * @param complexEventChunk event chunk to be executed
     */
    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        execute(complexEventChunk);
    }
}
