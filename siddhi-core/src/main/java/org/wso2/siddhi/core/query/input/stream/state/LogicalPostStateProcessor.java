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

package org.wso2.siddhi.core.query.input.stream.state;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.query.api.execution.query.input.state.LogicalStateElement;

/**
 * Created on 12/28/14.
 */
public class LogicalPostStateProcessor extends StreamPostStateProcessor {

    private LogicalStateElement.Type type;
    private LogicalPreStateProcessor partnerPreStateProcessor;
    private LogicalPostStateProcessor partnerPostStateProcessor;

    public LogicalPostStateProcessor(LogicalStateElement.Type type) {

        this.type = type;
    }

    public LogicalStateElement.Type getType() {
        return type;
    }

//    /**
//     * Process the handed StreamEvent
//     *
//     * @param complexEventChunk event chunk to be processed
//     */
//    @Override
//    public void process(ComplexEventChunk complexEventChunk) {
//        complexEventChunk.reset();
//        if (complexEventChunk.hasNext()) {     //one one event will be coming
//            StateEvent stateEvent = (StateEvent) complexEventChunk.next();
//
//        }
//        complexEventChunk.clear();
//    }

    protected void process(StateEvent stateEvent, ComplexEventChunk complexEventChunk) {
        switch (type) {
            case AND:
                if (stateEvent.getStreamEvent(partnerPreStateProcessor.getStateId()) != null) {
                    super.process(stateEvent, complexEventChunk);
                } else {
                    thisStatePreProcessor.stateChanged();
                }
                break;
            case OR:
                super.process(stateEvent, complexEventChunk);
                if (partnerPostStateProcessor.nextProcessor != null && thisStatePreProcessor.thisLastProcessor ==
                        partnerPostStateProcessor) {
                    // 'from A or B select' scenario require this
                    partnerPostStateProcessor.isEventReturned = true;
                }
                break;
            case NOT:
                break;
        }
    }

    public void setPartnerPreStateProcessor(LogicalPreStateProcessor partnerPreStateProcessor) {
        this.partnerPreStateProcessor = partnerPreStateProcessor;
    }

    public void setPartnerPostStateProcessor(LogicalPostStateProcessor partnerPostStateProcessor) {
        this.partnerPostStateProcessor = partnerPostStateProcessor;
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
     * @param key partition key
     * @return clone of PostStateProcessor
     */
    @Override
    public PostStateProcessor cloneProcessor(String key) {
        LogicalPostStateProcessor logicalPostStateProcessor = new LogicalPostStateProcessor(type);
        cloneProperties(logicalPostStateProcessor);
        return logicalPostStateProcessor;
    }

    public void setNextStatePreProcessor(PreStateProcessor preStateProcessor) {
        this.nextStatePerProcessor = preStateProcessor;
        partnerPostStateProcessor.nextStatePerProcessor = preStateProcessor;
    }

    public void setNextEveryStatePerProcessor(PreStateProcessor nextEveryStatePerProcessor) {
        this.nextEveryStatePerProcessor = nextEveryStatePerProcessor;
        partnerPostStateProcessor.nextEveryStatePerProcessor = nextEveryStatePerProcessor;
    }
}
