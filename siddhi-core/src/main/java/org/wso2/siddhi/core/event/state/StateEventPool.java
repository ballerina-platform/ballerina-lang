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
 * Event pool containing StreamEvent for reuse
 * This is not a thread safe implementation
 */
public class StateEventPool {

    private StateEventFactory eventFactory;
    private int size;
    private int index = 0;
    private StateEvent stateEventList;
    private long id = 0;

    public StateEventPool(MetaStateEvent metaStateEvent, int size) {
        eventFactory = new StateEventFactory(metaStateEvent.getStreamEventCount(),
                metaStateEvent.getOutputDataAttributes().size());
        this.size = size;
    }

//    public StateEventPool(int beforeWindowDataSize, int onAfterWindowDataSize, int outputDataSize, int poolSize) {
//        eventFactory = new StreamEventFactory(beforeWindowDataSize, onAfterWindowDataSize, outputDataSize);
//        this.size = poolSize;
//    }

    /**
     * Borrowing an StateEvent
     *
     * @return if StateEvent exist in the pool an existing event if not a new StateEvent will be returned
     */
    public StateEvent borrowEvent() {
        if (index > 0) {
            StateEvent event = stateEventList;
            stateEventList = stateEventList.getNext();
            event.setNext(null);
            index--;
            event.setId(++id);
            return event;
        } else {
            StateEvent event = eventFactory.newInstance();
            event.setId(++id);
            return event;
        }
    }

    /**
     * Collects the used InnerStreamEvents
     * If the pool has space the returned event will be added to the pool else it will be dropped
     *
     * @param stateEvent used event
     */
    public void returnEvents(StateEvent stateEvent) {
        if (stateEvent != null) {
            if (index < size) {
                StateEvent first = stateEvent;
                StateEvent last = stateEvent;
                while (stateEvent != null) {
                    last = stateEvent;
                    index++;
                    stateEvent = stateEvent.getNext();
                }
                last.setNext(stateEventList);
                stateEventList = first;
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
