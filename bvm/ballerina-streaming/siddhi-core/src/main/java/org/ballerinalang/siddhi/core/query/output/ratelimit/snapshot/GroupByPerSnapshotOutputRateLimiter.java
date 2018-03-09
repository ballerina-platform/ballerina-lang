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

package org.ballerinalang.siddhi.core.query.output.ratelimit.snapshot;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.ComplexEvent;
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.GroupedComplexEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.util.Scheduler;
import org.ballerinalang.siddhi.core.util.parser.SchedulerParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Implementation of {@link PerSnapshotOutputRateLimiter} for queries with GroupBy.
 */
public class GroupByPerSnapshotOutputRateLimiter extends SnapshotOutputRateLimiter {
    private final Long value;
    private String id;
    private ScheduledExecutorService scheduledExecutorService;
    private Map<String, ComplexEvent> groupByKeyEvents = new LinkedHashMap<String, ComplexEvent>();
    private Scheduler scheduler;
    private long scheduledTime;
    private String queryName;

    public GroupByPerSnapshotOutputRateLimiter(String id, Long value, ScheduledExecutorService
            scheduledExecutorService, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter,
                                               SiddhiAppContext siddhiAppContext, String queryName) {
        super(wrappedSnapshotOutputRateLimiter, siddhiAppContext);
        this.queryName = queryName;
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    /**
     * Sends the collected unique outputs per group by key upon arrival of timer event from scheduler.
     *
     * @param complexEventChunk Incoming {@link org.ballerinalang.siddhi.core.event.ComplexEventChunk}
     */
    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        List<ComplexEventChunk<ComplexEvent>> outputEventChunks = new ArrayList<ComplexEventChunk<ComplexEvent>>();
        complexEventChunk.reset();
        synchronized (this) {
            complexEventChunk.reset();
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next();
                if (event.getType() == ComplexEvent.Type.TIMER) {
                    tryFlushEvents(outputEventChunks, event);
                } else if (event.getType() == ComplexEvent.Type.CURRENT) {
                    complexEventChunk.remove();
                    tryFlushEvents(outputEventChunks, event);
                    GroupedComplexEvent groupedComplexEvent = ((GroupedComplexEvent) event);
                    groupByKeyEvents.put(groupedComplexEvent.getGroupKey(), groupedComplexEvent.getComplexEvent());
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
            for (ComplexEvent complexEvent : groupByKeyEvents.values()) {
                outputEventChunk.add(cloneComplexEvent(complexEvent));
            }
            outputEventChunks.add(outputEventChunk);
            scheduledTime += value;
            scheduler.notifyAt(scheduledTime);
        }
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
            state.put("GroupByKeyEvents", groupByKeyEvents);
        }
        return state;
    }

    @Override
    public synchronized void restoreState(Map<String, Object> state) {
        groupByKeyEvents = (Map<String, ComplexEvent>) state.get("groupByKeyEvents");
    }

    @Override
    public SnapshotOutputRateLimiter clone(String key, WrappedSnapshotOutputRateLimiter
            wrappedSnapshotOutputRateLimiter) {
        return new GroupByPerSnapshotOutputRateLimiter(id + key, value, scheduledExecutorService,
                wrappedSnapshotOutputRateLimiter, siddhiAppContext, queryName);
    }

}
