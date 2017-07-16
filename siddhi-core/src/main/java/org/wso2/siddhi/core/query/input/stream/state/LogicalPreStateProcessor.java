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
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.query.api.execution.query.input.state.LogicalStateElement;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created on 12/26/14.
 */
public class LogicalPreStateProcessor extends StreamPreStateProcessor {

    private LogicalStateElement.Type logicalType;
    private LogicalPreStateProcessor partnerStatePreProcessor;

    public LogicalPreStateProcessor(LogicalStateElement.Type type, StateInputStream.Type stateType, List<Map
            .Entry<Long, Set<Integer>>> withinStates) {
        super(stateType, withinStates);
        this.logicalType = type;
    }

    /**
     * Clone a copy of processor
     *
     * @param key partition key
     * @return clone of LogicalPreStateProcessor
     */
    @Override
    public PreStateProcessor cloneProcessor(String key) {
        LogicalPreStateProcessor logicalPreStateProcessor = new LogicalPreStateProcessor(logicalType, stateType,
                withinStates);
        cloneProperties(logicalPreStateProcessor, key);
        logicalPreStateProcessor.init(siddhiAppContext, queryName);
        return logicalPreStateProcessor;
    }

    @Override
    public void addState(StateEvent stateEvent) {
        if (isStartState || stateType == StateInputStream.Type.SEQUENCE) {
            if (newAndEveryStateEventList.isEmpty()) {
                newAndEveryStateEventList.add(stateEvent);
            }
            if (partnerStatePreProcessor != null && partnerStatePreProcessor.newAndEveryStateEventList.isEmpty()) {
                partnerStatePreProcessor.newAndEveryStateEventList.add(stateEvent);
            }
        } else {
            newAndEveryStateEventList.add(stateEvent);
            if (partnerStatePreProcessor != null) {
                partnerStatePreProcessor.newAndEveryStateEventList.add(stateEvent);
            }
        }

    }

    @Override
    public void addEveryState(StateEvent stateEvent) {
        StateEvent clonedEvent = stateEventCloner.copyStateEvent(stateEvent);
        clonedEvent.setEvent(stateId, null);
        newAndEveryStateEventList.add(clonedEvent);
        if (partnerStatePreProcessor != null) {
            clonedEvent.setEvent(partnerStatePreProcessor.stateId, null);
            partnerStatePreProcessor.newAndEveryStateEventList.add(clonedEvent);
        }
    }

    public void setStartState(boolean isStartState) {
        this.isStartState = isStartState;
        if (partnerStatePreProcessor.isStartState != this.isStartState) {
            partnerStatePreProcessor.isStartState = isStartState;
        }
    }

    @Override
    public void resetState() {
        if (logicalType == LogicalStateElement.Type.OR || pendingStateEventList.size() ==
                partnerStatePreProcessor.pendingStateEventList.size()) {
            pendingStateEventList.clear();
            partnerStatePreProcessor.pendingStateEventList.clear();

            if (isStartState && newAndEveryStateEventList.isEmpty()) {
                //        if (isStartState && stateType == StateInputStream.Type.SEQUENCE && newAndEveryStateEventList
                // .isEmpty()) {
                if (stateType == StateInputStream.Type.SEQUENCE && thisStatePostProcessor.nextEveryStatePerProcessor ==
                        null && !((StreamPreStateProcessor) thisStatePostProcessor.nextStatePerProcessor)
                        .pendingStateEventList.isEmpty()) {
                    return;
                }
                init();
            }
        }
    }

    @Override
    public void updateState() {
        pendingStateEventList.addAll(newAndEveryStateEventList);
        newAndEveryStateEventList.clear();

        partnerStatePreProcessor.pendingStateEventList.addAll(partnerStatePreProcessor.newAndEveryStateEventList);
        partnerStatePreProcessor.newAndEveryStateEventList.clear();
    }

    @Override
    public ComplexEventChunk<StateEvent> processAndReturn(ComplexEventChunk complexEventChunk) {
        ComplexEventChunk<StateEvent> returnEventChunk = new ComplexEventChunk<StateEvent>(false);
        complexEventChunk.reset();
        StreamEvent streamEvent = (StreamEvent) complexEventChunk.next(); //Sure only one will be sent
        for (Iterator<StateEvent> iterator = pendingStateEventList.iterator(); iterator.hasNext(); ) {
            StateEvent stateEvent = iterator.next();
            if (withinStates.size() > 0) {
                if (isExpired(stateEvent, streamEvent)) {
                    iterator.remove();
                    continue;
                }
            }
            if (logicalType == LogicalStateElement.Type.OR && stateEvent.getStreamEvent(partnerStatePreProcessor
                    .getStateId()) != null) {
                iterator.remove();
                continue;
            }
            stateEvent.setEvent(stateId, streamEventCloner.copyStreamEvent(streamEvent));
            process(stateEvent);
            if (this.thisLastProcessor.isEventReturned()) {
                this.thisLastProcessor.clearProcessedEvent();
                returnEventChunk.add(stateEvent);
            }
            if (stateChanged) {
                iterator.remove();
            } else {
                switch (stateType) {
                    case PATTERN:
                        stateEvent.setEvent(stateId, null);
                        break;
                    case SEQUENCE:
                        stateEvent.setEvent(stateId, null);
                        iterator.remove();
                        break;
                }
            }
        }
        return returnEventChunk;
    }

    public void setPartnerStatePreProcessor(LogicalPreStateProcessor partnerStatePreProcessor) {
        this.partnerStatePreProcessor = partnerStatePreProcessor;
    }
}
