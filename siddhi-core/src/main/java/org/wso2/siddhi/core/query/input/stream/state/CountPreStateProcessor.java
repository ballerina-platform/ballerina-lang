/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.Iterator;

/**
 * Created on 1/6/15.
 */
public class CountPreStateProcessor extends StreamPreStateProcessor {
    private final int minCount;
    private final int maxCount;
    protected volatile boolean successCondition = false;
    private CountPostStateProcessor countPostStateProcessor;

    public CountPreStateProcessor(int minCount, int maxCount) {

        this.minCount = minCount;
        this.maxCount = maxCount;
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
            if (removeIfNextStateProcessed(stateEvent, iterator, stateId + 1)) {
                continue;
            }
            if (removeIfNextStateProcessed(stateEvent, iterator, stateId + 2)) {
                continue;
            }
            stateEvent.addEvent(stateId, streamEventCloner.copyStreamEvent(streamEvent));
            successCondition = false;
            process(stateEvent, streamEvent, iterator);
            if (stateChanged) {
                iterator.remove();
            }
            if (!successCondition) {
                stateEvent.removeLastEvent(stateId);
            }
        }
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

    public void init() {
        if (isStartState) {
            StateEvent stateEvent = stateEventPool.borrowEvent();
            addState(stateEvent);
        }
    }

    @Override
    public void addState(StateEvent stateEvent) {
        newAndEveryStateEventList.add(stateEvent);
        if (minCount == 0 && stateEvent.getStreamEvent(stateId) == null) {
            countPostStateProcessor.getNextStatePerProcessor().addState(stateEvent);
        }
    }

    public void setCountPostStateProcessor(CountPostStateProcessor countPostStateProcessor) {
        this.countPostStateProcessor = countPostStateProcessor;
    }

    public CountPostStateProcessor getCountPostStateProcessor() {
        return countPostStateProcessor;
    }
}
