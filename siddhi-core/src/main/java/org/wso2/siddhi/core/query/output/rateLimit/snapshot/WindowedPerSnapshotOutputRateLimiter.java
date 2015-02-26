/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.output.rateLimit.snapshot;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WindowedPerSnapshotOutputRateLimiter extends SnapshotOutputRateLimiter {
    private String id;
    private final Long value;
    private final ScheduledExecutorService scheduledExecutorService;
    private LinkedList<ComplexEvent> eventList;
    private Comparator comparator;

    public WindowedPerSnapshotOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        super(wrappedSnapshotOutputRateLimiter);
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
        this.eventList = new LinkedList<ComplexEvent>();
        this.comparator = new Comparator<ComplexEvent>() {

            @Override
            public int compare(ComplexEvent event1, ComplexEvent event2) {
                if (Arrays.equals(event1.getOutputData(), event2.getOutputData())) {
                    return 0;
                } else {
                    return 1;

                }
            }
        };
    }

    @Override
    public void send(ComplexEventChunk complexEventChunk) {

    }

    @Override
    public void add(ComplexEvent complexEvent) {
        if (complexEvent.getType() == ComplexEvent.Type.CURRENT) {
            eventList.add(complexEvent);
        } else if (complexEvent.getType() == ComplexEvent.Type.EXPIRED) {
            for (Iterator<ComplexEvent> iterator = eventList.iterator(); iterator.hasNext(); ) {
                ComplexEvent event = iterator.next();
                if (comparator.compare(event, complexEvent) == 0) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    @Override
    public SnapshotOutputRateLimiter clone(String key, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        return new WindowedPerSnapshotOutputRateLimiter(id + key, value, scheduledExecutorService, wrappedSnapshotOutputRateLimiter);
    }

    @Override
    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(new EventSender(), 0, value, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    private synchronized void sendEvents() {
        ComplexEvent firstEvent = null;
        ComplexEvent lastEvent = null;

        for (ComplexEvent complexEvent : eventList) {
            if (firstEvent == null) {
                firstEvent = complexEvent;
            } else {
                lastEvent.setNext(complexEvent);
            }
            lastEvent = complexEvent;
        }

        ComplexEventChunk<ComplexEvent> complexEventChunk = new ComplexEventChunk<ComplexEvent>();
        if (firstEvent != null) {
            complexEventChunk.add(firstEvent);
        }

        sendToCallBacks(complexEventChunk);
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
