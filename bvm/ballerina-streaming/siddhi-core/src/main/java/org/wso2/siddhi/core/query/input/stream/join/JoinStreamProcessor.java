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

package org.wso2.siddhi.core.query.input.stream.join;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.query.processor.Processor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 12/18/14.
 */
public class JoinStreamProcessor implements Processor {
    List<Processor> joinProcessorList = new ArrayList<Processor>();

    /**
     * Process the handed StreamEvent
     *
     * @param complexEventChunk event chunk to be processed
     */
    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        for (Processor processor : joinProcessorList) {
            processor.process(complexEventChunk);
        }
    }

    /**
     * Get next processor element in the processor chain. Processed event should be sent to next processor
     *
     * @return Next processor
     */
    @Override
    public Processor getNextProcessor() {
        return null;
    }

    /**
     * Set next processor element in processor chain
     *
     * @param processor Processor to be set as next element of processor chain
     */
    @Override
    public void setNextProcessor(Processor processor) {

    }

    /**
     * Set as the last element of the processor chain
     *
     * @param processor Last processor in the chain
     */
    @Override
    public void setToLast(Processor processor) {

    }

    /**
     * Clone a copy of processor
     *
     * @param key partition key
     * @return cloned processor
     */
    @Override
    public Processor cloneProcessor(String key) {
        return null;
    }
}
