/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.event;

import java.util.Arrays;

/**
 * Event with state
 */
public abstract class StateEvent implements ComplexEvent, AtomicEvent {

    private int eventState = -1;
    private String eventId=null;
    protected StreamEvent[] streamEvents;

    public StateEvent(StreamEvent[] streamEvents) {
        this.streamEvents = streamEvents;
    }

    public StateEvent(StreamEvent[] streamEvents, String eventId) {
        this.streamEvents = streamEvents;
        this.eventId=eventId;
    }

    protected StateEvent(int eventState, StreamEvent[] streamEvents,String eventId) {
        this.eventState = eventState;
        this.streamEvents = streamEvents;
        this.eventId=eventId;
    }

    public StreamEvent[] getStreamEvents() {
        return streamEvents;
    }

    public StreamEvent getStreamEvent(int i) {
        return streamEvents[i];
    }

    @Override
    public long getTimeStamp() {
        for (int i = streamEvents.length - 1; i >= 0; i--) {
            StreamEvent streamEvent = streamEvents[i];
            if (streamEvent != null) {
                return streamEvent.getTimeStamp();

            }
        }
        return 0;
    }

    public long getFirstEventTimeStamp() {
        for (int i = 0; i < streamEvents.length; i++) {
            StreamEvent streamEvent = streamEvents[i];
            if (streamEvent != null) {
                return streamEvent.getTimeStamp();
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return "StateEvent{" +
               "eventState=" + eventState +
               ", eventId=" + eventId +
               ", streamEvents=" + (streamEvents == null ? null : Arrays.asList(streamEvents)) +
               '}';
    }

    public void setStreamEvent(int i, StreamEvent streamEvent) {
        this.streamEvents[i] = streamEvent;
    }

    protected abstract StateEvent createCloneEvent(StreamEvent[] inEventstream,
                                          int eventState,
                                          String eventId);

    public StateEvent cloneEvent(int stateNumber,String newEventId) {
        int length = streamEvents.length;
        StreamEvent[] inEventstream = new StreamEvent[length];
        for (int i = 0; i < stateNumber; i++) {
            StreamEvent streamEvent = streamEvents[i];
            if (streamEvent != null) {
                if (streamEvent instanceof ListAtomicEvent) {
                    ((ListAtomicEvent) streamEvent).cloneEvent();
                } else {
                    inEventstream[i] = streamEvent;
                }
            }
        }
        System.arraycopy(streamEvents, 0, inEventstream, 0, stateNumber);
        return createCloneEvent(inEventstream, eventState, newEventId);
    }

    public int getEventState() {
        return eventState;
    }

    public void setEventState(int eventState) {
        this.eventState = eventState;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Object getEvent0() {
        return streamEvents[0];
    }

    public Object getEvent1() {
        return streamEvents[1];
    }

    public Object getEvent2() {
        return streamEvents[2];
    }

    public Object getEvent3() {
        return streamEvents[3];
    }

    public Object getEvent4() {
        return streamEvents[4];
    }

    public Object getEvent5() {
        return streamEvents[5];
    }

    public Object getEvent6() {
        return streamEvents[6];
    }

    public Object getEvent7() {
        return streamEvents[7];
    }

    public Object getEvent8() {
        return streamEvents[8];
    }

    public Object getEvent9() {
        return streamEvents[9];
    }
}
