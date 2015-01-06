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
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.query.api.execution.query.input.state.LogicalStateElement;

import java.util.Iterator;

/**
 * Created on 12/26/14.
 */
public class LogicalPreStateProcessor extends StreamPreStateProcessor {

    private LogicalStateElement.Type logicalType;
    private LogicalPreStateProcessor partnerStatePreProcessor;

    public LogicalPreStateProcessor(LogicalStateElement.Type type) {
        this.logicalType = type;
    }

    public void init() {
        if (isStartState) {
            StateEvent stateEvent = stateEventPool.borrowEvent();
            newAndEveryStateEventList.add(stateEvent);
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

    @Override
    public void addState(StateEvent stateEvent) {
        newAndEveryStateEventList.add(stateEvent);
        if (partnerStatePreProcessor != null) {
            partnerStatePreProcessor.newAndEveryStateEventList.add(stateEvent);
        }
    }

    @Override
    public void addEveryState(StateEvent stateEvent) {
        newAndEveryStateEventList.add(stateEventCloner.copyStateEvent(stateEvent));
    }

    public void setStartState(boolean isStartState) {
        this.isStartState = isStartState;
        if (partnerStatePreProcessor.isStartState != this.isStartState) {
            partnerStatePreProcessor.isStartState = isStartState;
        }
    }

    @Override
    public void updateState() {

        pendingStateEventList.addAll(newAndEveryStateEventList);
        newAndEveryStateEventList.clear();

        partnerStatePreProcessor.pendingStateEventList.addAll(partnerStatePreProcessor.newAndEveryStateEventList);
        partnerStatePreProcessor.newAndEveryStateEventList.clear();
    }

    /**
     * Process the handed StreamEvent
     *
     * @param complexEventChunk event chunk to be processed
     */
    @Override
    public void process(ComplexEventChunk complexEventChunk) {

        complexEventChunk.reset();
        StreamEvent streamEvent = (StreamEvent) complexEventChunk.next(); //Sure only one will be sent
        for (Iterator<StateEvent> iterator = pendingStateEventList.iterator(); iterator.hasNext(); ) {
            StateEvent stateEvent = iterator.next();
            if (logicalType == LogicalStateElement.Type.OR &&
                    stateEvent.getStreamEvent(partnerStatePreProcessor.getStateId()) != null) {
                iterator.remove();
                continue;
            }
            stateEvent.setEvent(stateId, streamEventCloner.copyStreamEvent(streamEvent));
            process(stateEvent, streamEvent, iterator);
            if (stateChanged) {
                iterator.remove();
            } else {
                stateEvent.setEvent(stateId, null);
            }
        }
    }

    public void setPartnerStatePreProcessor(LogicalPreStateProcessor partnerStatePreProcessor) {
        this.partnerStatePreProcessor = partnerStatePreProcessor;
    }
}
