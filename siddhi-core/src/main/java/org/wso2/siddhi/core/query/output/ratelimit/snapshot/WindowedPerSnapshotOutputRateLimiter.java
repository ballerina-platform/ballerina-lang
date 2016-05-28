/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.query.output.ratelimit.snapshot;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.GroupedComplexEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.util.Scheduler;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WindowedPerSnapshotOutputRateLimiter extends SnapshotOutputRateLimiter {
    private String id;
    private final Long value;
    private final ScheduledExecutorService scheduledExecutorService;
    private LinkedList<ComplexEvent> eventList;
    private Comparator comparator;
    private Scheduler scheduler;
    private long scheduledTime;
    private Lock lock;

    public WindowedPerSnapshotOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        super(wrappedSnapshotOutputRateLimiter);
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
        this.eventList = new LinkedList<ComplexEvent>();
        lock = new ReentrantLock();
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
    public void process(ComplexEventChunk complexEventChunk) {
        List<ComplexEventChunk<ComplexEvent>> outputEventChunks = new ArrayList<ComplexEventChunk<ComplexEvent>>();
        complexEventChunk.reset();
        synchronized (this) {
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next();
                if (event instanceof GroupedComplexEvent) {
                    event = ((GroupedComplexEvent) event).getComplexEvent();
                }
                if (event.getType() == ComplexEvent.Type.TIMER) {
                    if (event.getTimestamp() >= scheduledTime) {
                        ComplexEventChunk<ComplexEvent> outputEventChunk = new ComplexEventChunk<ComplexEvent>(false);
                        for (ComplexEvent complexEvent : eventList) {
                            outputEventChunk.add(cloneComplexEvent(complexEvent));
                        }
                        outputEventChunks.add(outputEventChunk);
                        scheduledTime = scheduledTime + value;
                        scheduler.notifyAt(scheduledTime);
                    }
                } else if (event.getType() == ComplexEvent.Type.CURRENT) {
                    complexEventChunk.remove();
                    eventList.add(event);
                } else if (event.getType() == ComplexEvent.Type.EXPIRED) {
                    for (Iterator<ComplexEvent> iterator = eventList.iterator(); iterator.hasNext(); ) {
                        ComplexEvent currentEvent = iterator.next();
                        if (comparator.compare(currentEvent, event) == 0) {
                            iterator.remove();
                            break;
                        }
                    }
                } else if (event.getType() == ComplexEvent.Type.RESET) {
                    eventList.clear();
                }
            }
        }
        for (ComplexEventChunk eventChunk : outputEventChunks) {
            sendToCallBacks(eventChunk);
        }
    }

    @Override
    public SnapshotOutputRateLimiter clone(String key, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        return new WindowedPerSnapshotOutputRateLimiter(id + key, value, scheduledExecutorService, wrappedSnapshotOutputRateLimiter);
    }

    @Override
    public void start() {
        scheduler = new Scheduler(scheduledExecutorService, this);
        scheduler.setStreamEventPool(new StreamEventPool(0, 0, 0, 5));
        long currentTime = System.currentTimeMillis();
        scheduledTime = currentTime + value;
        scheduler.notifyAt(scheduledTime);
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public Object[] currentState() {
        return new Object[]{eventList};
    }

    @Override
    public void restoreState(Object[] state) {
        eventList = (LinkedList<ComplexEvent>) state[0];
    }

}
