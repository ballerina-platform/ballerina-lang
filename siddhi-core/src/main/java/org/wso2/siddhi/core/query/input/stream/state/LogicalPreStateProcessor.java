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
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.query.api.execution.query.input.state.LogicalStateElement;

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
            newAndEveryStateEventChunk.add(stateEvent);
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
        System.out.println("PSP: add " + stateId + " " + stateEvent);
        newAndEveryStateEventChunk.add(stateEvent);
        if (partnerStatePreProcessor != null) {
            partnerStatePreProcessor.newAndEveryStateEventChunk.add(stateEvent);
        }
    }

    @Override
    public void addEveryState(StateEvent stateEvent) {
        System.out.println("PSP: addEvery " + stateId + " " + stateEvent);
        newAndEveryStateEventChunk.add(stateEventCloner.copyStateEvent(stateEvent));
    }

    public void setStartState(boolean isStartState) {
        this.isStartState = isStartState;
        if (partnerStatePreProcessor.isStartState != this.isStartState) {
            partnerStatePreProcessor.isStartState = isStartState;
        }
    }

    @Override
    public void updateState() {
        System.out.println("PSP: update " + stateId + " " + newAndEveryStateEventChunk);
        moveNewAndEveryEventsToPendingEventChunk(newAndEveryStateEventChunk, pendingStateEventChunk);
        moveNewAndEveryEventsToPendingEventChunk(partnerStatePreProcessor.newAndEveryStateEventChunk,
                partnerStatePreProcessor.pendingStateEventChunk);
    }

    private void moveNewAndEveryEventsToPendingEventChunk(ComplexEventChunk<StateEvent> newAndEveryStateEventChunk,
                                                          ComplexEventChunk<StateEvent> pendingStateEventChunk) {
        newAndEveryStateEventChunk.reset();
        while (newAndEveryStateEventChunk.hasNext()) {
            StateEvent stateEvent = newAndEveryStateEventChunk.next();
            newAndEveryStateEventChunk.remove();
            pendingStateEventChunk.add(stateEvent);
        }
        newAndEveryStateEventChunk.clear();
    }

    public void partnerStateChanged(StateEvent partnerEvent) {
        switch (logicalType) {
            case AND:
                pendingStateEventChunk.reset();
                while (pendingStateEventChunk.hasNext()) {
                    StateEvent stateEvent = pendingStateEventChunk.next();
                    if (stateEvent.getId() == partnerEvent.getId()) {
                        int partnerId = partnerStatePreProcessor.getStateId();
                        stateEvent.setEvent(partnerId, partnerEvent.getStreamEvent(partnerId));
                        pendingStateEventChunk.reset();
                        return;
                    }
                }
                return;
            case OR:
                pendingStateEventChunk.reset();
                while (pendingStateEventChunk.hasNext()) {
                    StateEvent stateEvent = pendingStateEventChunk.next();
                    if (stateEvent.getId() == partnerEvent.getId()) {
                        pendingStateEventChunk.remove();
                        pendingStateEventChunk.reset();
                        return;
                    }
                }
                throw new IllegalStateException("OR partner should contain matching event for " + partnerEvent);
            case NOT:
                throw new OperationNotSupportedException();
        }
        throw new OperationNotSupportedException();
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public void setPartnerStatePreProcessor(LogicalPreStateProcessor partnerStatePreProcessor) {
        this.partnerStatePreProcessor = partnerStatePreProcessor;
    }
}
