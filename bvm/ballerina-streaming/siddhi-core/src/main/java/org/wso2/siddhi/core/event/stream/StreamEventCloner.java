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

package org.wso2.siddhi.core.event.stream;

/**
 * Cloner class for {@link StreamEvent} to be used when creating {@link org.wso2.siddhi.core.partition.PartitionRuntime}
 */
public class StreamEventCloner {

    private final int beforeWindowDataSize;
    private final int onAfterWindowDataSize;
    private final int outputDataSize;
    private final StreamEventPool streamEventPool;

    public StreamEventCloner(MetaStreamEvent metaStreamEvent, StreamEventPool streamEventPool) {

        this.streamEventPool = streamEventPool;
        this.beforeWindowDataSize = metaStreamEvent.getBeforeWindowData().size();
        this.onAfterWindowDataSize = metaStreamEvent.getOnAfterWindowData().size();
        this.outputDataSize = metaStreamEvent.getOutputData().size();

    }

    /**
     * Method to copy new StreamEvent from StreamEvent
     *
     * @param streamEvent StreamEvent to be copied
     * @return StreamEvent
     */
    public StreamEvent copyStreamEvent(StreamEvent streamEvent) {
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        if (beforeWindowDataSize > 0) {
            System.arraycopy(streamEvent.getBeforeWindowData(), 0, borrowedEvent.getBeforeWindowData(), 0,
                             beforeWindowDataSize);
        }
        if (onAfterWindowDataSize > 0) {
            System.arraycopy(streamEvent.getOnAfterWindowData(), 0, borrowedEvent.getOnAfterWindowData(), 0,
                             onAfterWindowDataSize);
        }
        if (outputDataSize > 0) {
            System.arraycopy(streamEvent.getOutputData(), 0, borrowedEvent.getOutputData(), 0, outputDataSize);
        }
        borrowedEvent.setType(streamEvent.getType());
        borrowedEvent.setTimestamp(streamEvent.getTimestamp());
        return borrowedEvent;
    }
}
