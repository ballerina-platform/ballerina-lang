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

package org.ballerinalang.siddhi.core.query.input.stream.state;

import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.state.StateEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.query.api.execution.query.input.state.LogicalStateElement;

/**
 * Post-state processor of not logical operator.
 */
public class AbsentLogicalPostStateProcessor extends LogicalPostStateProcessor {


    public AbsentLogicalPostStateProcessor(LogicalStateElement.Type type) {
        super(type);
    }


    protected void process(StateEvent stateEvent, ComplexEventChunk complexEventChunk) {

        // Mark the state changed
        thisStatePreProcessor.stateChanged();

        // Update the timestamp
        StreamEvent streamEvent = stateEvent.getStreamEvent(stateId);

        // This is the notification to AbsentStreamPreStateProcessor that this event has been processed
        this.isEventReturned = true;

        ((AbsentPreStateProcessor) thisStatePreProcessor).updateLastArrivalTime(streamEvent.getTimestamp());
    }

    @Override
    public PostStateProcessor cloneProcessor(String key) {
        AbsentLogicalPostStateProcessor logicalPostStateProcessor = new AbsentLogicalPostStateProcessor(type);
        cloneProperties(logicalPostStateProcessor);
        return logicalPostStateProcessor;
    }
}
