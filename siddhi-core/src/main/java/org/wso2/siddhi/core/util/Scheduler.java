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

package org.wso2.siddhi.core.util;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.ConversionStreamEventChunk;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.query.input.stream.single.EntryValveProcessor;
import org.wso2.siddhi.core.util.lock.LockWrapper;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Scheduler implementation to take periodic snapshots
 */
public abstract class Scheduler implements Snapshotable {

    private static final Logger log = Logger.getLogger(Scheduler.class);
    protected final BlockingQueue<Long> toNotifyQueue = new LinkedBlockingQueue<Long>();
    private final ThreadBarrier threadBarrier;
    private final Schedulable singleThreadEntryValve;
    protected SiddhiAppContext siddhiAppContext;
    protected String elementId;
    protected String queryName;
    private StreamEventPool streamEventPool;
    private ComplexEventChunk<StreamEvent> streamEventChunk;
    private LatencyTracker latencyTracker;
    private LockWrapper lockWrapper;


    public Scheduler(Schedulable singleThreadEntryValve, SiddhiAppContext siddhiAppContext) {
        this.threadBarrier = siddhiAppContext.getThreadBarrier();
        this.siddhiAppContext = siddhiAppContext;
        this.singleThreadEntryValve = singleThreadEntryValve;
    }

    public abstract void schedule(long time);

    public abstract Scheduler clone(String key, EntryValveProcessor entryValveProcessor);

    public void notifyAt(long time) {
        try {
            // Insert the time into the queue
            toNotifyQueue.put(time);
            schedule(time);     // Let the subclasses to schedule the scheduler
        } catch (InterruptedException e) {
            log.error("Error when adding time:" + time + " to toNotifyQueue at Scheduler", e);
        }
    }

    public void setStreamEventPool(StreamEventPool streamEventPool) {
        this.streamEventPool = streamEventPool;
        streamEventChunk = new ConversionStreamEventChunk((StreamEventConverter) null, streamEventPool);
    }

    public void init(LockWrapper lockWrapper, String queryName) {
        this.lockWrapper = lockWrapper;
        this.queryName = queryName;
        if (elementId == null) {
            elementId = "Scheduler-" + siddhiAppContext.getElementIdGenerator().createNewId();
        }
        siddhiAppContext.getSnapshotService().addSnapshotable(queryName, this);
    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> state = new HashMap<>();
        state.put("ToNotifyQueue", toNotifyQueue);
        return state;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        BlockingQueue<Long> restoreToNotifyQueue = (BlockingQueue<Long>) state.get("ToNotifyQueue");
        for (Long time : restoreToNotifyQueue) {
            notifyAt(time);
        }
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    public void setLatencyTracker(LatencyTracker latencyTracker) {
        this.latencyTracker = latencyTracker;
    }

    /**
     * Go through the timestamps stored in the {@link #toNotifyQueue} and send the TIMER events for the expired events.
     */
    protected void sendTimerEvents() {
        Long toNotifyTime = toNotifyQueue.peek();
        long currentTime = siddhiAppContext.getTimestampGenerator().currentTime();
        while (toNotifyTime != null && toNotifyTime - currentTime <= 0) {
            toNotifyQueue.poll();

            StreamEvent timerEvent = streamEventPool.borrowEvent();
            timerEvent.setType(StreamEvent.Type.TIMER);
            timerEvent.setTimestamp(toNotifyTime);
            streamEventChunk.add(timerEvent);
            if (lockWrapper != null) {
                lockWrapper.lock();
            }
            threadBarrier.pass();
            try {
                if (latencyTracker != null) {
                    try {
                        latencyTracker.markIn();
                        singleThreadEntryValve.process(streamEventChunk);
                    } finally {
                        latencyTracker.markOut();
                    }
                } else {
                    singleThreadEntryValve.process(streamEventChunk);
                }
            } finally {
                if (lockWrapper != null) {
                    lockWrapper.unlock();
                }
            }
            streamEventChunk.clear();

            toNotifyTime = toNotifyQueue.peek();
            currentTime = siddhiAppContext.getTimestampGenerator().currentTime();
        }
    }
}
