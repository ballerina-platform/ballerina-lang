/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.input.stream.state;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.query.processor.Processor;

/**
 * Created on 12/17/14.
 */
public class StreamPostStateProcessor implements PostStateProcessor {
    protected PreStateProcessor nextStatePerProcessor;
    protected PreStateProcessor nextEveryStatePerProcessor;
    protected StreamPreStateProcessor thisStatePreProcessor;
    protected Processor nextProcessor;

    /**
     * Process the handed StreamEvent
     *
     * @param complexEventChunk event chunk to be processed
     */
    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        complexEventChunk.reset();
        if (complexEventChunk.hasNext()) {     //one one event will be coming
            StateEvent stateEvent = (StateEvent) complexEventChunk.next();
            thisStatePreProcessor.stateChanged();
            if (nextProcessor != null) {
                complexEventChunk.reset();
                nextProcessor.process(complexEventChunk);
            }
            if (nextStatePerProcessor != null) {
                nextStatePerProcessor.addState(stateEvent);
            }
            if (nextEveryStatePerProcessor != null) {
                nextEveryStatePerProcessor.addEveryState(stateEvent);
            }
        }
        complexEventChunk.clear();
    }

    /**
     * Get next processor element in the processor chain. Processed event should be sent to next processor
     *
     * @return
     */
    @Override
    public Processor getNextProcessor() {
        return nextProcessor;
    }

    /**
     * Set next processor element in processor chain
     *
     * @param nextProcessor Processor to be set as next element of processor chain
     */
    @Override
    public void setNextProcessor(Processor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    /**
     * Set as the last element of the processor chain
     *
     * @param processor Last processor in the chain
     */
    @Override
    public void setToLast(Processor processor) {
        if (nextProcessor == null) {
            this.nextProcessor = processor;
        } else {
            this.nextProcessor.setToLast(processor);
        }
    }

    /**
     * Clone a copy of processor
     *
     * @return
     */
    @Override
    public Processor cloneProcessor() {
        return null;
    }

    public void setNextStatePreProcessor(PreStateProcessor preStateProcessor) {
        this.nextStatePerProcessor = preStateProcessor;
    }

    public void setThisStatePreProcessor(StreamPreStateProcessor preStateProcessor) {
        thisStatePreProcessor = preStateProcessor;
    }

    public PreStateProcessor getNextStatePerProcessor() {
        return nextStatePerProcessor;
    }


    public PreStateProcessor getNextEveryStatePerProcessor() {
        return nextEveryStatePerProcessor;
    }

    public void setNextEveryStatePerProcessor(PreStateProcessor nextEveryStatePerProcessor) {
        this.nextEveryStatePerProcessor = nextEveryStatePerProcessor;
    }

    public PreStateProcessor getThisStatePreProcessor() {
        return thisStatePreProcessor;
    }
}
