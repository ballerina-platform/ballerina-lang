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

public class AllAggregationGroupByWindowedPerSnapshotOutputRateLimiter extends SnapshotOutputRateLimiter {
    private String id;
    private final Long value;
    private final ScheduledExecutorService scheduledExecutorService;
    private Map<String, LastEventHolder> groupByKeyEvents = new LinkedHashMap<String, LastEventHolder>();
    private Scheduler scheduler;
    private long scheduledTime;

    public AllAggregationGroupByWindowedPerSnapshotOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        super(wrappedSnapshotOutputRateLimiter);
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public SnapshotOutputRateLimiter clone(String key, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        return new AllAggregationGroupByWindowedPerSnapshotOutputRateLimiter(id + key, value, scheduledExecutorService, wrappedSnapshotOutputRateLimiter);
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        List<ComplexEventChunk<ComplexEvent>> outputEventChunks = new ArrayList<ComplexEventChunk<ComplexEvent>>();
        complexEventChunk.reset();
        synchronized (this) {
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next();
                if (event.getType() == ComplexEvent.Type.TIMER) {
                    if (event.getTimestamp() >= scheduledTime) {
                        ComplexEventChunk<ComplexEvent> outputEventChunk = new ComplexEventChunk<ComplexEvent>(false);
                        for (Iterator<Map.Entry<String, LastEventHolder>> iterator = groupByKeyEvents.entrySet().iterator(); iterator.hasNext(); ) {
                            Map.Entry<String, LastEventHolder> lastEventHolderEntry = iterator.next();

                            //clearing expired events after update
                            lastEventHolderEntry.getValue().checkAndClearLastInEvent();
                            if (lastEventHolderEntry.getValue().lastEvent == null) {
                                iterator.remove();
                            }else {
                                outputEventChunk.add(cloneComplexEvent(lastEventHolderEntry.getValue().lastEvent));
                            }
                        }
                        outputEventChunks.add(outputEventChunk);
                        scheduledTime += value;
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
                        lastEventHolder.removeLastInEvent(groupedComplexEvent.getComplexEvent());
                    }else if (groupedComplexEvent.getType() == ComplexEvent.Type.RESET) {
                        groupByKeyEvents.clear();
                    }
                }
            }
        }
        for (ComplexEventChunk eventChunk : outputEventChunks) {
            sendToCallBacks(eventChunk);
        }

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
        return new Object[]{groupByKeyEvents};
    }

    @Override
    public void restoreState(Object[] state) {
        groupByKeyEvents = (Map<String, LastEventHolder>) state[0];
    }

    private class LastEventHolder {
        long count = 0;
        ComplexEvent lastEvent = null;

        public void addLastInEvent(ComplexEvent lastEvent) {
            this.lastEvent = lastEvent;
            count++;
        }

        public void removeLastInEvent(ComplexEvent lastEvent) {
            this.lastEvent = lastEvent;
            count--;
        }

        public void checkAndClearLastInEvent() {
            if (count <= 0) {
                lastEvent = null;
            }
        }
    }
}
