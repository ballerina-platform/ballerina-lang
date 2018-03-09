/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.util.Schedulable;
import org.wso2.siddhi.core.util.ThreadBarrier;

/**
 * Entry Valve Siddhi processor chain.
 */
public class EntryValveProcessor implements Processor, Schedulable {

    private Processor next;
    private ThreadBarrier threadBarrier;
    private SiddhiAppContext siddhiAppContext;

    public EntryValveProcessor(SiddhiAppContext siddhiAppContext) {
        this.siddhiAppContext = siddhiAppContext;
        threadBarrier = siddhiAppContext.getThreadBarrier();
    }


    /**
     * Process the handed StreamEvent
     *
     * @param complexEventChunk event chunk to be processed
     */
    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        threadBarrier.pass();
        next.process(complexEventChunk);
    }

    /**
     * Get next processor element in the processor chain. Processed event should be sent to next processor
     *
     * @return Next Processor
     */

    @Override
    public Processor getNextProcessor() {
        return next;
    }

    /**
     * Set next processor element in processor chain
     *
     * @param processor Processor to be set as next element of processor chain
     */
    @Override
    public void setNextProcessor(Processor processor) {
        next = processor;
    }

    /**
     * Set as the last element of the processor chain
     *
     * @param processor Last processor in the chain
     */
    @Override
    public void setToLast(Processor processor) {
        if (next == null) {
            this.next = processor;
        } else {
            this.next.setToLast(processor);
        }
    }

    /**
     * Clone a copy of processor
     *
     * @param key partition key
     * @return cloned processor
     */
    @Override
    public Processor cloneProcessor(String key) {
        return new EntryValveProcessor(siddhiAppContext);
    }

}
