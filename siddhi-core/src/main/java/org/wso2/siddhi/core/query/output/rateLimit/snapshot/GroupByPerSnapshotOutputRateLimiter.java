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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GroupByPerSnapshotOutputRateLimiter extends SnapshotOutputRateLimiter {
    private String id;
    private final Long value;
    private ScheduledExecutorService scheduledExecutorService;
    private Map<String, List<ComplexEvent>> tempGroupByKeyEvents = new LinkedHashMap<String, List<ComplexEvent>>();
    private Map<String, List<ComplexEvent>> groupByKeyEvents = new LinkedHashMap<String, List<ComplexEvent>>();

    public GroupByPerSnapshotOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        super(wrappedSnapshotOutputRateLimiter);
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public void send(ComplexEventChunk complexEventChunk) {
        for (String key : tempGroupByKeyEvents.keySet()) {
            groupByKeyEvents.put(key, tempGroupByKeyEvents.get(key));
        }
        tempGroupByKeyEvents.clear();
    }

    @Override
    public void add(ComplexEvent complexEvent) {
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

    private class EventSender implements Runnable {
        @Override
        public void run() {
            sendEvents();
        }
    }
}
