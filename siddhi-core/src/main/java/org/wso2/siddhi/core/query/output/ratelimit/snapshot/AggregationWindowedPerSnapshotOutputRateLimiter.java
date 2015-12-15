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
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.util.Scheduler;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AggregationWindowedPerSnapshotOutputRateLimiter extends SnapshotOutputRateLimiter {
    protected String id;
    protected final Long value;
    protected LinkedList<Object> eventList;
    protected Comparator<ComplexEvent> comparator;
    protected final ScheduledExecutorService scheduledExecutorService;
    protected List<Integer> aggregateAttributePositionList;
    private Map<Integer, Object> aggregateAttributeValueMap;
    protected ComplexEventChunk<ComplexEvent> eventChunk;
    protected Scheduler scheduler;
    protected long scheduledTime;
    protected Lock lock;

    protected AggregationWindowedPerSnapshotOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService, final List<Integer> aggregateAttributePositionList, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        super(wrappedSnapshotOutputRateLimiter);
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
        this.eventList = new LinkedList<Object>();
        this.aggregateAttributePositionList = aggregateAttributePositionList;
        Collections.sort(aggregateAttributePositionList);
        lock = new ReentrantLock();
        aggregateAttributeValueMap = new HashMap<Integer, Object>(aggregateAttributePositionList.size());
        eventChunk = new ComplexEventChunk<ComplexEvent>();
        this.comparator = new Comparator<ComplexEvent>() {
            Integer[] aggregateAttributePositions = aggregateAttributePositionList.toArray(new Integer[aggregateAttributePositionList.size()]);
            int ignoreIndexLength = aggregateAttributePositions.length;

            @Override
            public int compare(ComplexEvent event1, ComplexEvent event2) {
                int ignoreIndex = 0;
                int ignoreIndexPosition = aggregateAttributePositions[0];
                Object[] data = event1.getOutputData();
                for (int i = 0; i < data.length; i++) {
                    if (ignoreIndexPosition == i) {
                        ignoreIndex++;
                        if (ignoreIndex == ignoreIndexLength) {
                            ignoreIndexPosition = -1;
                        } else {
                            ignoreIndexPosition = aggregateAttributePositions[i];
                        }
                        continue;
                    }
                    if (!data[i].equals(event2.getOutputData()[i])) {
                        return 1;
                    }

                }
                return 0;
            }
        };
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        ComplexEvent firstEvent = complexEventChunk.getFirst();
        try {
            lock.lock();
            if (firstEvent != null && firstEvent.getType() == ComplexEvent.Type.TIMER) {
                if (firstEvent.getTimestamp() >= scheduledTime) {
                    sendEvents();
                    scheduledTime = scheduledTime + value;
                    scheduler.notifyAt(scheduledTime);
                }
            } else {
                processAndSend(eventChunk, aggregateAttributeValueMap, "");
                eventChunk.clear();
            }

        } finally {
            lock.unlock();
        }
    }

    @Override
    public void add(ComplexEvent complexEvent) {
        try {
            lock.lock();
            eventChunk.add(complexEvent);
        } finally {
            lock.unlock();
        }
    }

    protected void processAndSend(ComplexEventChunk complexEventChunk, Map<Integer, Object> aggregateAttributeValueMap, String groupByKey) {
        ComplexEvent complexEvent = complexEventChunk.getFirst();

        while (complexEvent != null) {
            ComplexEvent next = complexEvent.getNext();
            complexEvent.setNext(null);

            if (complexEvent.getType() == ComplexEvent.Type.CURRENT) {
                addEventToList(complexEvent, groupByKey);
                for (Integer position : aggregateAttributePositionList) {
                    aggregateAttributeValueMap.put(position, (complexEvent).getOutputData()[position]);
                }
            } else if (complexEvent.getType() == ComplexEvent.Type.EXPIRED) {
                for (Iterator iterator = eventList.iterator(); iterator.hasNext(); ) {
                    ComplexEvent event = getEventFromList(iterator.next());
                    if (comparator.compare(event, complexEvent) == 0) {
                        iterator.remove();
                        for (Integer position : aggregateAttributePositionList) {
                            aggregateAttributeValueMap.put(position, event.getOutputData()[position]);
                        }
                        break;
                    }
                }
            }
            complexEvent = next;
        }

    }

    @Override
    public void start() {
        scheduler = new Scheduler(scheduledExecutorService, this);
        scheduler.setStreamEventPool(new StreamEventPool(0, 0, 0, 5));
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
        return new Object[]{eventList, aggregateAttributeValueMap, eventChunk};
    }

    @Override
    public void restoreState(Object[] state) {
        eventList = (LinkedList<Object>) state[0];
        aggregateAttributeValueMap = (Map<Integer, Object>) state[1];
        eventChunk = (ComplexEventChunk<ComplexEvent>) state[2];
    }

    protected synchronized void sendEvents() {
        ComplexEvent firstEvent = null;
        ComplexEvent lastEvent = null;

        for (Object originalComplexEvent : eventList) {
            ComplexEvent complexEvent = constructSendEvent(originalComplexEvent);
            if (firstEvent == null) {
                firstEvent = complexEvent;
            } else {
                lastEvent.setNext(complexEvent);
            }
            lastEvent = complexEvent;
        }

        ComplexEventChunk<ComplexEvent> complexEventChunk = new ComplexEventChunk<ComplexEvent>();
        if (firstEvent != null) {
            complexEventChunk.add(firstEvent);
        }

        sendToCallBacks(complexEventChunk);
    }

    protected ComplexEvent constructSendEvent(Object originalEvent) {
        return createSendEvent((ComplexEvent) originalEvent, aggregateAttributeValueMap);
    }

    protected ComplexEvent createSendEvent(ComplexEvent originalEvent, Map<Integer, Object> aggregateAttributeValueMap) {
        ComplexEvent copiedEvent = cloneComplexEvent(originalEvent);

        for (Integer position : aggregateAttributePositionList) {
            copiedEvent.getOutputData()[position] = aggregateAttributeValueMap.get(position);
        }
        return copiedEvent;
    }

    @Override
    public SnapshotOutputRateLimiter clone(String key, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        return new AggregationWindowedPerSnapshotOutputRateLimiter(id + key, value, scheduledExecutorService, aggregateAttributePositionList, wrappedSnapshotOutputRateLimiter);
    }

    protected ComplexEvent getEventFromList(Object eventObject) {
        return (ComplexEvent) eventObject;
    }

    protected void addEventToList(ComplexEvent event, String groupByKey) {
        eventList.add(event);
    }

}
