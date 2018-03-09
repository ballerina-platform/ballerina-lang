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

package org.wso2.siddhi.core.query.output.ratelimit.snapshot;


import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.parser.SchedulerParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Parent implementation for per event periodic snapshot rate limiting. Multiple implementations of this will be
 * there to represent different queries. Snapshot rate limiting will only emit current events representing the
 * snapshot period.
 */
public class PerSnapshotOutputRateLimiter extends SnapshotOutputRateLimiter {
    private final Long value;
    private String id;
    private ScheduledExecutorService scheduledExecutorService;
    private ComplexEventChunk<ComplexEvent> eventChunk = new ComplexEventChunk<ComplexEvent>(false);
    private ComplexEvent lastEvent;
    private Scheduler scheduler;
    private long scheduledTime;
    private String queryName;

    public PerSnapshotOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService,
                                        WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter,
                                        SiddhiAppContext siddhiAppContext, String queryName) {
        super(wrappedSnapshotOutputRateLimiter, siddhiAppContext);
        this.queryName = queryName;
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        List<ComplexEventChunk<ComplexEvent>> outputEventChunks = new ArrayList<ComplexEventChunk<ComplexEvent>>();
        complexEventChunk.reset();
        synchronized (this) {
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next();
                if (event.getType() == ComplexEvent.Type.TIMER) {
                    tryFlushEvents(outputEventChunks, event);
                } else if (event.getType() == ComplexEvent.Type.CURRENT) {
                    complexEventChunk.remove();
                    tryFlushEvents(outputEventChunks, event);
                    lastEvent = event;
                } else {
                    tryFlushEvents(outputEventChunks, event);
                }
            }

        }
        for (ComplexEventChunk eventChunk : outputEventChunks) {
            sendToCallBacks(eventChunk);
        }
    }

    private void tryFlushEvents(List<ComplexEventChunk<ComplexEvent>> outputEventChunks, ComplexEvent event) {
        if (event.getTimestamp() >= scheduledTime) {
            ComplexEventChunk<ComplexEvent> outputEventChunk = new ComplexEventChunk<ComplexEvent>(false);
            if (lastEvent != null) {
                outputEventChunk.add(cloneComplexEvent(lastEvent));
            }
            outputEventChunks.add(outputEventChunk);
            scheduledTime += value;
            scheduler.notifyAt(scheduledTime);
        }
    }

    @Override
    public SnapshotOutputRateLimiter clone(String key, WrappedSnapshotOutputRateLimiter
            wrappedSnapshotOutputRateLimiter) {
        return new PerSnapshotOutputRateLimiter(id + key, value, scheduledExecutorService,
                                                wrappedSnapshotOutputRateLimiter, siddhiAppContext, queryName);
    }

    @Override
    public void start() {
        scheduler = SchedulerParser.parse(scheduledExecutorService, this, siddhiAppContext);
        scheduler.setStreamEventPool(new StreamEventPool(0, 0, 0, 5));
        scheduler.init(lockWrapper, queryName);
        long currentTime = System.currentTimeMillis();
        scheduledTime = currentTime + value;
        scheduler.notifyAt(scheduledTime);
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> state = new HashMap<>();
        synchronized (this) {
            state.put("EventChunk", eventChunk.getFirst());
        }
        return state;
    }

    @Override
    public synchronized void restoreState(Map<String, Object> state) {
        eventChunk.clear();
        eventChunk.add((ComplexEvent) state.get("EventList"));
    }

}
