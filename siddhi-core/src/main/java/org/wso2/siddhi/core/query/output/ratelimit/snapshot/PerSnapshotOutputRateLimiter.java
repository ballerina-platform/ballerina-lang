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
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.util.Scheduler;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PerSnapshotOutputRateLimiter extends SnapshotOutputRateLimiter {
    private String id;
    private final Long value;
    private ScheduledExecutorService scheduledExecutorService;
    private ComplexEventChunk<ComplexEvent> eventChunk = new ComplexEventChunk<ComplexEvent>();
    private ComplexEvent lastEvent;
    private Scheduler scheduler;
    private long scheduledTime;
    private Lock lock;

    public PerSnapshotOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
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
                lastEvent = complexEvent;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SnapshotOutputRateLimiter clone(String key, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        return new PerSnapshotOutputRateLimiter(id + key, value, scheduledExecutorService, wrappedSnapshotOutputRateLimiter);
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
        return new Object[]{eventChunk};
    }

    @Override
    public void restoreState(Object[] state) {
        eventChunk = (ComplexEventChunk<ComplexEvent>) state[0];
        //endOfChunk = (Boolean) state[1];
    }

    public synchronized void sendEvents() {
        if (lastEvent != null) {
            ComplexEventChunk<ComplexEvent> snapshotChunk = new ComplexEventChunk<ComplexEvent>();
            snapshotChunk.add(cloneComplexEvent(lastEvent));
            sendToCallBacks(snapshotChunk);
        }
    }

}
