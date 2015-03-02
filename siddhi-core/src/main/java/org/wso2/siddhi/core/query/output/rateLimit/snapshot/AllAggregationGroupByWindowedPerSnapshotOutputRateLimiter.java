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
import org.wso2.siddhi.core.query.selector.QuerySelector;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AllAggregationGroupByWindowedPerSnapshotOutputRateLimiter extends SnapshotOutputRateLimiter {
    private String id;
    private final Long value;
    private final ScheduledExecutorService scheduledExecutorService;
    Map<String, LastEventHolder> groupByKeyEvents = new LinkedHashMap<String, LastEventHolder>();

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
    public void send(ComplexEventChunk complexEventChunk) {
    }

    @Override
    public void add(ComplexEvent complexEvent) {
        String groupByKey = QuerySelector.getThreadLocalGroupByKey();
        LastEventHolder lastEventHolder = groupByKeyEvents.get(groupByKey);
        if (lastEventHolder == null) {
            lastEventHolder = new LastEventHolder();
            groupByKeyEvents.put(groupByKey, lastEventHolder);
        }
        if (complexEvent.getType() == ComplexEvent.Type.CURRENT) {
            lastEventHolder.addLastInEvent(complexEvent);
        } else if (complexEvent.getType() == ComplexEvent.Type.EXPIRED) {
            lastEventHolder.removeLastInEvent();
            if (lastEventHolder.lastEvent == null) {
                groupByKeyEvents.remove(groupByKey);
            }
        }
    }

    @Override
    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(new EventSender(), 0, value, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    public synchronized void sendEvents() {
        ComplexEventChunk<ComplexEvent> eventChunk = new ComplexEventChunk<ComplexEvent>();
        if (groupByKeyEvents.size() > 1) {
            for (LastEventHolder lastEventHolder : groupByKeyEvents.values()) {
                eventChunk.add(lastEventHolder.lastEvent);
            }
        }

        sendToCallBacks(eventChunk);

        if (groupByKeyEvents.size() > 1) {
            for (LastEventHolder lastEventHolder : groupByKeyEvents.values()) {
                lastEventHolder.lastEvent.setNext(null);
            }
        }

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
