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

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.GroupedComplexEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.util.Scheduler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class GroupByPerSnapshotOutputRateLimiter extends SnapshotOutputRateLimiter {
    private String id;
    private final Long value;
    private ScheduledExecutorService scheduledExecutorService;
    private Map<String, ComplexEvent> groupByKeyEvents = new LinkedHashMap<String, ComplexEvent>();
    private Scheduler scheduler;
    private long scheduledTime;

    public GroupByPerSnapshotOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter, ExecutionPlanContext executionPlanContext) {
        super(wrappedSnapshotOutputRateLimiter, executionPlanContext);
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    /**
     * Sends the collected unique outputs per group by key upon arrival of timer event from scheduler.
     *
     * @param complexEventChunk Incoming {@link org.wso2.siddhi.core.event.ComplexEventChunk}
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
        scheduler = new Scheduler(scheduledExecutorService, this, executionPlanContext);
        scheduler.setStreamEventPool(new StreamEventPool(0, 0, 0, 5));
        scheduler.init(queryLock);
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
        return new Object[]{groupByKeyEvents};
    }

    @Override
    public void restoreState(Object[] state) {
        groupByKeyEvents = (Map<String, ComplexEvent>) state[0];
    }

    @Override
    public SnapshotOutputRateLimiter clone(String key, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        return new GroupByPerSnapshotOutputRateLimiter(id + key, value, scheduledExecutorService, wrappedSnapshotOutputRateLimiter, executionPlanContext);
    }

}
