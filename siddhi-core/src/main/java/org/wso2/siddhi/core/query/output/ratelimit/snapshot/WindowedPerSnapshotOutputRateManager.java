/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.query.output.ratelimit.snapshot;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WindowedPerSnapshotOutputRateManager extends SnapshotOutputRateManager {

    private final Long value;
    private final ScheduledExecutorService scheduledExecutorService;
    private long timeStamp;
    private LinkedList<Event> eventList;
    private Comparator comparator;

    public WindowedPerSnapshotOutputRateManager(Long value, ScheduledExecutorService scheduledExecutorService, WrappedSnapshotOutputRateManager wrappedSnapshotOutputRateManager) {
        super(wrappedSnapshotOutputRateManager);
        this.value = value;
        this.eventList = new LinkedList<Event>();
        this.scheduledExecutorService = scheduledExecutorService;
        this.comparator = new Comparator<Event>() {

            @Override
            public int compare(Event event1, Event event2) {
                if (Arrays.equals(event1.getData(), event2.getData())) {
                    return 0;
                } else {
                    return 1;

                }
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(new EventSender(), 0, value, TimeUnit.MILLISECONDS);
    }


    @Override
    public synchronized void send(long timeStamp, StreamEvent currentEvent, StreamEvent expiredEvent, String groupByKey) {
        this.timeStamp = timeStamp;
        if (currentEvent != null) {
            if (currentEvent instanceof ListEvent) {
                for (int i = 0, size = ((ListEvent) currentEvent).getActiveEvents(); i < size; i++) {
                    eventList.add(((ListEvent) currentEvent).getEvent(i));
                }
            } else {
                eventList.add((InEvent) currentEvent);
            }
        }
        if (expiredEvent != null) {
            if (expiredEvent instanceof ListEvent) {
                for (int i = 0, size = ((ListEvent) expiredEvent).getActiveEvents(); i < size; i++) {
                    for (Iterator<Event> iterator = eventList.iterator(); iterator.hasNext(); ) {
                        Event event = iterator.next();
                        if (comparator.compare(event, expiredEvent) == 0) {
                            iterator.remove();
                            break;
                        }
                    }
                }
            } else {
                for (Iterator<Event> iterator = eventList.iterator(); iterator.hasNext(); ) {
                    Event event = iterator.next();
                    if (comparator.compare(event, expiredEvent) == 0) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }

    private synchronized void sendEvents() {
        if (eventList.size() > 0) {
            StreamEvent currentEvent = null;

            if (eventList.size() == 1) {
                currentEvent = new InEvent(eventList.get(0));
            } else if (eventList.size() >= 1) {
                InEvent[] events = new InEvent[eventList.size()];
                eventList.toArray(events);
                currentEvent = new InListEvent(events);
            }
            sendToCallBacks(timeStamp, currentEvent, null, currentEvent);
        } else {
            sendToCallBacks(timeStamp, null, null, null);
        }
    }


    private class EventSender implements Runnable {

        @Override
        public void run() {
            try {
                sendEvents();
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        }
    }
}
