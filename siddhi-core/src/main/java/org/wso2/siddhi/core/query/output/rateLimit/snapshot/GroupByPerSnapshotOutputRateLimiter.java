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
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.util.Scheduler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GroupByPerSnapshotOutputRateLimiter extends SnapshotOutputRateLimiter {
    private String id;
    private final Long value;
    private ScheduledExecutorService scheduledExecutorService;
    private Map<String, List<ComplexEvent>> tempGroupByKeyEvents = new LinkedHashMap<String, List<ComplexEvent>>();
    private Map<String, List<ComplexEvent>> groupByKeyEvents = new LinkedHashMap<String, List<ComplexEvent>>();
    private Scheduler scheduler;
    private long scheduledTime;
    private Lock lock;

    public GroupByPerSnapshotOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        super(wrappedSnapshotOutputRateLimiter);
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
        lock = new ReentrantLock();
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        ComplexEvent firstEvent = complexEventChunk.getFirst();
        try {
            lock.lock();
            if(firstEvent != null && firstEvent.getType() == ComplexEvent.Type.TIMER) {
                if (firstEvent.getTimestamp() >= scheduledTime) {
                    sendEvents();
                    scheduledTime = scheduledTime + value;
                    scheduler.notifyAt(scheduledTime);
                }
            } else {
                for (String key : tempGroupByKeyEvents.keySet()) {
                    groupByKeyEvents.put(key, tempGroupByKeyEvents.get(key));
                }
                tempGroupByKeyEvents.clear();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void add(ComplexEvent complexEvent) {
        try {
            lock.lock();
            if (complexEvent.getType() == ComplexEvent.Type.CURRENT) {
                String groupByKey = QuerySelector.getThreadLocalGroupByKey();
                if (tempGroupByKeyEvents.containsKey(groupByKey)) {
                    tempGroupByKeyEvents.get(groupByKey).add(complexEvent);
                } else {
                    List<ComplexEvent> newEventChunk = new ArrayList<ComplexEvent>();
                    newEventChunk.add(complexEvent);
                    tempGroupByKeyEvents.put(groupByKey, newEventChunk);
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
        return new Object[]{tempGroupByKeyEvents, groupByKeyEvents};
    }

    @Override
    public void restoreState(Object[] state) {
        tempGroupByKeyEvents = (Map<String, List<ComplexEvent>>) state[0];
        groupByKeyEvents = (Map<String, List<ComplexEvent>>) state[1];
    }

    public synchronized void sendEvents() {
        ComplexEvent firstEvent = null;
        ComplexEvent lastEvent = null;

        for (List<ComplexEvent> complexEventList : groupByKeyEvents.values()) {
            for (ComplexEvent complexEvent : complexEventList) {
                if (firstEvent == null) {
                    firstEvent = complexEvent;
                } else {
                    lastEvent.setNext(complexEvent);
                }
                lastEvent = complexEvent;
            }
        }
        ComplexEventChunk<ComplexEvent> complexEventChunk = new ComplexEventChunk<ComplexEvent>();
        if (firstEvent != null) {
            complexEventChunk.add(firstEvent);
        }
        sendToCallBacks(complexEventChunk);

    }

    @Override
    public SnapshotOutputRateLimiter clone(String key, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        return new GroupByPerSnapshotOutputRateLimiter(id + key, value, scheduledExecutorService, wrappedSnapshotOutputRateLimiter);
    }

}
