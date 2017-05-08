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

package org.wso2.siddhi.core.query.output.callback;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.state.StateEventPool;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;

/**
 * Abstract class to represent parent callback implementation which allows users to get processed events from Siddhi
 * queries. There are multiple implementation of this which will receive events and perform various tasks.
 */
public abstract class OutputCallback {

    public abstract void send(ComplexEventChunk complexEventChunk);

    protected ComplexEventChunk<StateEvent> constructMatchingStateEventChunk(ComplexEventChunk
                                                                                     matchingComplexEventChunk,
                                                                             boolean convertToStreamEvent,
                                                                             StateEventPool stateEventPool,
                                                                             int matchingStreamIndex, StreamEventPool
                                                                                     streamEventPool,
                                                                             StreamEventConverter
                                                                                     streamEventConvertor) {
        ComplexEventChunk<StateEvent> stateEventChunk = new ComplexEventChunk<StateEvent>(matchingComplexEventChunk
                                                                                                  .isBatch());
        while (matchingComplexEventChunk.hasNext()) {
            ComplexEvent matchingComplexEvent = matchingComplexEventChunk.next();
            matchingComplexEventChunk.remove();
            StateEvent stateEvent = stateEventPool.borrowEvent();
            if (convertToStreamEvent) {
                StreamEvent borrowEvent = streamEventPool.borrowEvent();
                streamEventConvertor.convertData(
                        matchingComplexEvent.getTimestamp(),
                        matchingComplexEvent.getOutputData(),
                        matchingComplexEvent.getType() == ComplexEvent.Type.EXPIRED ? ComplexEvent.Type.CURRENT :
                                matchingComplexEvent.getType(),
                        borrowEvent);
                stateEvent.addEvent(matchingStreamIndex, borrowEvent);
            } else {
                stateEvent.addEvent(matchingStreamIndex, (StreamEvent) matchingComplexEvent);
            }
            stateEventChunk.add(stateEvent);
        }
        return stateEventChunk;
    }

}
