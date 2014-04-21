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

import org.wso2.siddhi.core.event.in.InEvent;

import java.util.Arrays;

public abstract class ListEvent implements StreamEvent, BundleEvent {

    protected Event[] events;
    protected int activeEvents = 0;

    protected ListEvent(Event[] events, int activeEvents) {
        this.events = events;
        this.activeEvents = activeEvents;
    }

    public ListEvent(Event[] events) {
        this.events = events;
        this.activeEvents = events.length;
    }

    protected ListEvent() {
    }

    public Event[] getEvents() {
        return events;
    }

    public Event getEvent(int i) {
        return events[i];
    }

    @Override
    public String toString() {
        return "SingleEventList{" +
                "events=" + (events == null ? null : Arrays.asList(events)) +
                '}';
    }

    @Override
    public long getTimeStamp() {
        if (activeEvents > 0) {
            return events[activeEvents - 1].getTimeStamp();
        } else {
            return -1;
        }
    }

    public boolean addEvent(Event event) {
        if (events.length == activeEvents) {
            Event[] inEvents = new Event[activeEvents + 10];
            System.arraycopy(events, 0, inEvents, 0, events.length);
            events = inEvents;
        }
        this.events[activeEvents] = event;
        activeEvents++;
        return true;
    }

    public int getActiveEvents() {
        return activeEvents;
    }


    public void removeLast() {
        activeEvents--;
        events[activeEvents] = null;
    }

    public ListEvent cloneEvent() {
        Event[] inEvents = new Event[events.length];
        System.arraycopy(inEvents, 0, inEvents, 0, activeEvents);
        return createEventClone(inEvents, activeEvents);
    }

    protected abstract ListEvent createEventClone(Event[] inEvents, int activeEvents);

    public void setEvents(Event[] events) {
        this.events = events;
        this.activeEvents = events.length;
    }

    @Override
    public Event[] toArray() {
        return events;
    }

    public Event getEvent0() {
        return events[0];
    }

    public Event getEvent1() {
        return events[1];
    }

    public Event getEvent2() {
        return events[2];
    }

    public Event getEvent3() {
        return events[3];
    }

    public Event getEvent4() {
        return events[4];
    }

    public Event getEvent5() {
        return events[5];
    }

    public Event getEvent6() {
        return events[6];
    }

    public Event getEvent7() {
        return events[7];
    }

    public Event getEvent8() {
        return events[8];
    }

    public Event getEvent9() {
        return events[9];
    }
}