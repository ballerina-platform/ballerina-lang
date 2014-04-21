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

public abstract class ListAtomicEvent implements BundleEvent {

    protected AtomicEvent[] atomicEvents;
    protected int activeEvents = 0;

    public ListAtomicEvent(int initialSize) {
        this.atomicEvents = new AtomicEvent[initialSize];

    }

    protected ListAtomicEvent(AtomicEvent[] atomicEvents, int activeEvents) {
        this.atomicEvents = atomicEvents;
        this.activeEvents = activeEvents;
    }

    public ListAtomicEvent(AtomicEvent[] events) {
        this.atomicEvents = events;
        this.activeEvents = events.length;
    }

    public AtomicEvent[] getEvents() {
        return atomicEvents;
    }

    public AtomicEvent getEvent(int i) {
        return atomicEvents[i];
    }

    @Override
    public long getTimeStamp() {
        if (activeEvents > 0) {
            return atomicEvents[activeEvents - 1].getTimeStamp();
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "SingleEventList{" +
                "events=" + (atomicEvents == null ? null : Arrays.asList(activeEvents)) +
                '}';
    }


    public boolean addEvent(AtomicEvent atomicEvent) {
        if (atomicEvents.length == activeEvents) {
            AtomicEvent[] inEvents = new AtomicEvent[activeEvents + 10];
            System.arraycopy(atomicEvents, 0, inEvents, 0, atomicEvents.length);
            atomicEvents = inEvents;

        }
        this.atomicEvents[activeEvents] = (AtomicEvent)atomicEvent;
        activeEvents++;
        return true;
    }

    public int getActiveEvents() {
        return activeEvents;
    }


    public void removeLast() {
        activeEvents--;
        atomicEvents[activeEvents] = null;
    }

    public ListAtomicEvent cloneEvent() {
        Event[] inEvents = new Event[atomicEvents.length];
        System.arraycopy(inEvents, 0, inEvents, 0, activeEvents);
        return createEventClone(inEvents, activeEvents);
    }

    protected abstract ListAtomicEvent createEventClone(Event[] inEvents, int activeEvents);

    public void setEvents(AtomicEvent[] atomicEvents) {
        this.atomicEvents = atomicEvents;
        this.activeEvents = atomicEvents.length;
    }

    public AtomicEvent getEvent0() {
        return atomicEvents[0];
    }

    public AtomicEvent getEvent1() {
        return atomicEvents[1];
    }

    public AtomicEvent getEvent2() {
        return atomicEvents[2];
    }

    public AtomicEvent getEvent3() {
        return atomicEvents[3];
    }

    public AtomicEvent getEvent4() {
        return atomicEvents[4];
    }

    public AtomicEvent getEvent5() {
        return atomicEvents[5];
    }

    public AtomicEvent getEvent6() {
        return atomicEvents[6];
    }

    public AtomicEvent getEvent7() {
        return atomicEvents[7];
    }

    public AtomicEvent getEvent8() {
        return atomicEvents[8];
    }

    public AtomicEvent getEvent9() {
        return atomicEvents[9];
    }
}