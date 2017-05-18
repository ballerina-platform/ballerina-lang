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

import java.io.Serializable;

/**
 * Event pool containing StreamEvent for reuse
 * This is not a thread safe implementation
 */
public class StreamEventPool implements Serializable {

    private static final long serialVersionUID = -1743558131917334571L;
    private StreamEventFactory eventFactory;
    private int size;
    private int index = 0;
    private StreamEvent streamEventList;

    public StreamEventPool(MetaStreamEvent metaStreamEvent, int size) {
        eventFactory = new StreamEventFactory(metaStreamEvent.getBeforeWindowData().size(),
                metaStreamEvent.getOnAfterWindowData().size(),
                metaStreamEvent.getOutputData().size());
        this.size = size;
    }

    public StreamEventPool(int beforeWindowDataSize, int onAfterWindowDataSize, int outputDataSize, int poolSize) {
        eventFactory = new StreamEventFactory(beforeWindowDataSize, onAfterWindowDataSize, outputDataSize);
        this.size = poolSize;
    }

    /**
     * Borrowing an StreamEvent
     *
     * @return if StreamEvent exist in the pool an existing event if not a new StreamEvent will be returned
     */
    public StreamEvent borrowEvent() {
        if (index > 0) {
            StreamEvent event = streamEventList;
            streamEventList = streamEventList.getNext();
            event.setNext(null);
            index--;
            return event;
        } else {
            return eventFactory.newInstance();
        }
    }

    /**
     * Collects the used InnerStreamEvents
     * If the pool has space the returned event will be added to the pool else it will be dropped
     *
     * @param streamEvent used event
     */
    public void returnEvents(StreamEvent streamEvent) {
        if (streamEvent != null) {
            if (index < size) {
                StreamEvent first = streamEvent;
                StreamEvent last = streamEvent;
                while (streamEvent != null) {
                    last = streamEvent;
                    index++;
                    streamEvent = streamEvent.getNext();
                }
                last.setNext(streamEventList);
                streamEventList = first;
            }
        }

    }

    /**
     * @return Occupied buffer size
     */
    public int getBufferedEventsSize() {
        return index;
    }

    public int getSize() {
        return size;
    }
}
