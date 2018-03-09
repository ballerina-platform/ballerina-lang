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
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created on 1/6/15.
 */
public class CountPreStateProcessor extends StreamPreStateProcessor {
    private final int minCount;
    private final int maxCount;
    protected volatile boolean successCondition = false;
    private CountPostStateProcessor countPostStateProcessor;
    private volatile boolean startStateReset = false;

    public CountPreStateProcessor(int minCount, int maxCount, StateInputStream.Type stateType, List<Map.Entry<Long,
            Set<Integer>>> withinStates) {
        super(stateType, withinStates);
        this.minCount = minCount;
        this.maxCount = maxCount;
    }


    public PreStateProcessor cloneProcessor(String key) {
        CountPreStateProcessor countPreStateProcessor = new CountPreStateProcessor(minCount, maxCount, stateType,
                withinStates);
        cloneProperties(countPreStateProcessor, key);
        countPreStateProcessor.init(siddhiAppContext, queryName);
        return countPreStateProcessor;
    }

    @Override
    public ComplexEventChunk<StateEvent> processAndReturn(ComplexEventChunk complexEventChunk) {
        ComplexEventChunk<StateEvent> returnEventChunk = new ComplexEventChunk<StateEvent>(false);
        complexEventChunk.reset();
        StreamEvent streamEvent = (StreamEvent) complexEventChunk.next(); //Sure only one will be sent
        for (Iterator<StateEvent> iterator = pendingStateEventList.iterator(); iterator.hasNext(); ) {
            StateEvent stateEvent = iterator.next();
            if (removeIfNextStateProcessed(stateEvent, iterator, stateId + 1)) {
                continue;
            }
            if (removeIfNextStateProcessed(stateEvent, iterator, stateId + 2)) {
                continue;
            }
            stateEvent.addEvent(stateId, streamEventCloner.copyStreamEvent(streamEvent));
            successCondition = false;
            process(stateEvent);
            if (this.thisLastProcessor.isEventReturned()) {
                this.thisLastProcessor.clearProcessedEvent();
                returnEventChunk.add(stateEvent);
            }
            if (stateChanged) {
                iterator.remove();
            }
            if (!successCondition) {
                switch (stateType) {
                    case PATTERN:
                        stateEvent.removeLastEvent(stateId);
                        break;
                    case SEQUENCE:
                        stateEvent.removeLastEvent(stateId);
                        iterator.remove();
                        break;
                }
            }
        }
        return returnEventChunk;
    }

    private boolean removeIfNextStateProcessed(StateEvent stateEvent, Iterator<StateEvent> iterator, int position) {
        if (stateEvent.getStreamEvents().length > position && stateEvent.getStreamEvent(position) != null) {
            iterator.remove();
            return true;
        }
        return false;
    }

    public void successCondition() {
        this.successCondition = true;
    }


    @Override
    public void addState(StateEvent stateEvent) {
        //        if (stateType == StateInputStream.Type.SEQUENCE) {
        //            newAndEveryStateEventList.clear();
        //            pendingStateEventList.clear();
        //        }
        if (stateType == StateInputStream.Type.SEQUENCE) {
            if (newAndEveryStateEventList.isEmpty()) {
                newAndEveryStateEventList.add(stateEvent);
            }
        } else {
            newAndEveryStateEventList.add(stateEvent);
        }
        if (minCount == 0 && stateEvent.getStreamEvent(stateId) == null) {
            currentStateEventChunk.clear();
            currentStateEventChunk.add(stateEvent);
            countPostStateProcessor.processMinCountReached(stateEvent, currentStateEventChunk);
            currentStateEventChunk.clear();
        }
    }

    public CountPostStateProcessor getCountPostStateProcessor() {
        return countPostStateProcessor;
    }

    public void setCountPostStateProcessor(CountPostStateProcessor countPostStateProcessor) {
        this.countPostStateProcessor = countPostStateProcessor;
    }

    public void startStateReset() {
        startStateReset = true;
        if (thisStatePostProcessor.callbackPreStateProcessor != null) {
            ((CountPreStateProcessor) countPostStateProcessor.thisStatePreProcessor).startStateReset();
        }
    }

    @Override
    public void updateState() {
        if (startStateReset) {
            startStateReset = false;
            init();
        }
        super.updateState();
    }
}
