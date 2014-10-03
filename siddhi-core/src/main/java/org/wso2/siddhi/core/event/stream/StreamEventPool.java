/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.core.event.stream;

/**
 * Event pool containing StreamEvent for reuse
 * This is not a thread safe implementation
 */
public class StreamEventPool {

    private StreamEventFactory eventFactory;
    private int size;
    private int index = 0;
    private StreamEvent streamEventList;

    public StreamEventPool(StreamEventFactory eventFactory, int size) {
        this.eventFactory = eventFactory;
        this.size = size;
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
    public void returnEvent(StreamEvent streamEvent) {
        if (index < size) {
            StreamEvent first = streamEvent;
            StreamEvent last = streamEvent;
            while (streamEvent != null) {
                last = streamEvent;
                index++;
                streamEvent = streamEvent.getNext();
            }
            if(last != null){
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
}
