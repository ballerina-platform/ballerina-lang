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
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AggregationWindowedPerSnapshotOutputRateLimiter extends SnapshotOutputRateLimiter {
    protected String id;
    protected final Long value;
    protected LinkedList<Object> eventList;
    protected Comparator<ComplexEvent> comparator;
    protected final ScheduledExecutorService scheduledExecutorService;
    protected List<Integer> aggregateAttributePositionList;
    private Map<Integer, Object> aggregateAttributeValueMap;
    private ComplexEventChunk<ComplexEvent> eventChunk;

    protected AggregationWindowedPerSnapshotOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService, final List<Integer> aggregateAttributePositionList, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        super(wrappedSnapshotOutputRateLimiter);
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
        this.eventList = new LinkedList<Object>();
        this.aggregateAttributePositionList = aggregateAttributePositionList;
        Collections.sort(aggregateAttributePositionList);
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
    public void send(ComplexEventChunk complexEventChunk) {
        processAndSend(eventChunk, aggregateAttributeValueMap, "");
        eventChunk.clear();
    }

    @Override
    public void add(ComplexEvent complexEvent) {
        eventChunk.add(complexEvent);
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
        scheduledExecutorService.scheduleAtFixedRate(new EventSender(), 0, value, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        //Nothing to stop
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
        ComplexEvent copiedEvent = null;
        if (originalEvent instanceof StreamEvent) {
            copiedEvent = streamEventCloner.copyStreamEvent((StreamEvent) originalEvent);
        } else if (originalEvent instanceof StateEvent) {
            copiedEvent = stateEventCloner.copyStateEvent((StateEvent) originalEvent);
        }

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
}
