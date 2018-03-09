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

package org.wso2.siddhi.core.query.input.stream.state;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;

/**
 * PostStateProcessor to handle not pattern state processors.
 */
public class AbsentStreamPostStateProcessor extends StreamPostStateProcessor {

    /**
     * This method just mark the state changed but does not send the stateEvent to the next processors.
     *
     * @param stateEvent        the state event
     * @param complexEventChunk the ComplexEventChunk
     */
    protected void process(StateEvent stateEvent, ComplexEventChunk complexEventChunk) {

        // Mark the state changed
        thisStatePreProcessor.stateChanged();

        // Update the timestamp
        StreamEvent streamEvent = stateEvent.getStreamEvent(stateId);
        stateEvent.setTimestamp(streamEvent.getTimestamp());

        // This is the notification to AbsentStreamPreStateProcessor that this event has been processed
        this.isEventReturned = true;

        if (thisStatePreProcessor.isStartState) {
            if (nextEveryStatePerProcessor != null && nextEveryStatePerProcessor == thisStatePreProcessor) {
                // nextEveryStatePerProcessor refers the AbsentStreamPreStateProcessor
                nextEveryStatePerProcessor.addEveryState(stateEvent);
            }
        }

        ((AbsentPreStateProcessor) thisStatePreProcessor).updateLastArrivalTime(streamEvent.getTimestamp());
    }

    @Override
    public PostStateProcessor cloneProcessor(String key) {
        AbsentStreamPostStateProcessor streamPostStateProcessor = new AbsentStreamPostStateProcessor();
        cloneProperties(streamPostStateProcessor);
        return streamPostStateProcessor;
    }
}
