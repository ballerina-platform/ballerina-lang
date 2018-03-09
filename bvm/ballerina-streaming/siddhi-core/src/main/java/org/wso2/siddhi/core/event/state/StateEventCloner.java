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

package org.wso2.siddhi.core.event.state;

/**
 * Class to clone {@link StateEvent} used when creating {@link org.wso2.siddhi.core.partition.PartitionRuntime}.
 */
public class StateEventCloner {

    private final int streamEventSize;
    //    private final int preOutputDataSize;
    private final int outputDataSize;
    private final StateEventPool stateEventPool;

    public StateEventCloner(MetaStateEvent metaStateEvent, StateEventPool stateEventPool) {

        this.stateEventPool = stateEventPool;
        this.streamEventSize = metaStateEvent.getStreamEventCount();
//        this.preOutputDataSize = metaStateEvent.getPreOutputDataAttributes().size();
        this.outputDataSize = metaStateEvent.getOutputDataAttributes().size();

    }

    /**
     * Method to copy new StreamEvent from StreamEvent
     *
     * @param stateEvent StreamEvent to be copied
     * @return StateEvent
     */
    public StateEvent copyStateEvent(StateEvent stateEvent) {
        StateEvent borrowedEvent = stateEventPool.borrowEvent();
        if (outputDataSize > 0) {
            System.arraycopy(stateEvent.getOutputData(), 0, borrowedEvent.getOutputData(), 0, outputDataSize);
        }
        if (streamEventSize > 0) {
            System.arraycopy(stateEvent.getStreamEvents(), 0, borrowedEvent.getStreamEvents(), 0, streamEventSize);
        }
        borrowedEvent.setType(stateEvent.getType());
        borrowedEvent.setTimestamp(stateEvent.getTimestamp());
        borrowedEvent.setId(stateEvent.getId());
        return borrowedEvent;
    }
}
