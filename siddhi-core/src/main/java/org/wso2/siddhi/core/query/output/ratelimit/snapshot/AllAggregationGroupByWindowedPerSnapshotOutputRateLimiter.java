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
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.util.Scheduler;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AllAggregationGroupByWindowedPerSnapshotOutputRateLimiter extends SnapshotOutputRateLimiter {
    private String id;
    private final Long value;
    private final ScheduledExecutorService scheduledExecutorService;
    private Map<String, LastEventHolder> groupByKeyEvents = new LinkedHashMap<String, LastEventHolder>();
    private Scheduler scheduler;
    private long scheduledTime;
    private Lock lock;

    public AllAggregationGroupByWindowedPerSnapshotOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        super(wrappedSnapshotOutputRateLimiter);
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
        lock = new ReentrantLock();
    }

    @Override
    public SnapshotOutputRateLimiter clone(String key, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        return new AllAggregationGroupByWindowedPerSnapshotOutputRateLimiter(id + key, value, scheduledExecutorService, wrappedSnapshotOutputRateLimiter);
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        try {
            lock.lock();
            complexEventChunk.reset();
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next();
                if (event.getType() == ComplexEvent.Type.TIMER) {
                    if (event.getTimestamp() >= scheduledTime) {
                        sendEvents();
                        scheduledTime = scheduledTime + value;
                        scheduler.notifyAt(scheduledTime);
                    }
                } else {
                    complexEventChunk.remove();
                    GroupedComplexEvent groupedComplexEvent = ((GroupedComplexEvent) event);

                    LastEventHolder lastEventHolder = groupByKeyEvents.get(groupedComplexEvent.getGroupKey());
                    if (lastEventHolder == null) {
                        lastEventHolder = new LastEventHolder();
                        groupByKeyEvents.put(groupedComplexEvent.getGroupKey(), lastEventHolder);
                    }
                    if (groupedComplexEvent.getType() == ComplexEvent.Type.CURRENT) {
                        lastEventHolder.addLastInEvent(groupedComplexEvent.getComplexEvent());
                    } else if (groupedComplexEvent.getType() == ComplexEvent.Type.EXPIRED) {
                        lastEventHolder.removeLastInEvent();
                        if (lastEventHolder.lastEvent == null) {
                            groupByKeyEvents.remove(groupedComplexEvent.getGroupKey());
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void start() {
        scheduler = new Scheduler(scheduledExecutorService, this);
        scheduler.setStreamEventPool(new StreamEventPool(0,0,0, 5));
        long currentTime = System.currentTimeMillis();
        scheduler.notifyAt(currentTime);
        scheduledTime = currentTime;
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public Object[] currentState() {
        return new Object[]{groupByKeyEvents};
    }

    @Override
    public void restoreState(Object[] state) {
        groupByKeyEvents = (Map<String, LastEventHolder>) state[0];
    }

    public synchronized void sendEvents() {
        ComplexEventChunk<ComplexEvent> eventChunk = new ComplexEventChunk<ComplexEvent>();
        if (groupByKeyEvents.size() > 0) {
            for (LastEventHolder lastEventHolder : groupByKeyEvents.values()) {
                eventChunk.add(cloneComplexEvent(lastEventHolder.lastEvent));
            }
        }
        sendToCallBacks(eventChunk);
    }

    private class LastEventHolder {
        long count = 0;
        ComplexEvent lastEvent = null;

        public void addLastInEvent(ComplexEvent lastEvent) {
            this.lastEvent = lastEvent;
            count++;
        }

        public void removeLastInEvent() {
            count--;
            if (count <= 0) {
                lastEvent = null;
            }
        }
    }
}
