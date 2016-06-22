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
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.util.Scheduler;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

public class AggregationWindowedPerSnapshotOutputRateLimiter extends SnapshotOutputRateLimiter {
    protected String id;
    protected final Long value;
    protected LinkedList<ComplexEvent> eventList;
    protected Comparator<ComplexEvent> comparator;
    protected final ScheduledExecutorService scheduledExecutorService;
    protected List<Integer> aggregateAttributePositionList;
    private Map<Integer, Object> aggregateAttributeValueMap;
    protected Scheduler scheduler;
    protected long scheduledTime;

    protected AggregationWindowedPerSnapshotOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService, final List<Integer> aggregateAttributePositionList, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        super(wrappedSnapshotOutputRateLimiter);
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
        this.eventList = new LinkedList<ComplexEvent>();
        this.aggregateAttributePositionList = aggregateAttributePositionList;
        Collections.sort(aggregateAttributePositionList);
        aggregateAttributeValueMap = new HashMap<Integer, Object>(aggregateAttributePositionList.size());
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
        complexEventChunk.reset();
        ArrayList<ComplexEventChunk<ComplexEvent>> outputEventChunks = new ArrayList<ComplexEventChunk<ComplexEvent>>();
        synchronized (this) {
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next();
                if (event.getType() == ComplexEvent.Type.TIMER) {
                    if (event.getTimestamp() >= scheduledTime) {
                        ComplexEventChunk<ComplexEvent> outputEventChunk = new ComplexEventChunk<ComplexEvent>(false);
                        for (ComplexEvent originalComplexEvent : eventList) {
                            ComplexEvent eventCopy = cloneComplexEvent(originalComplexEvent);
                            for (Integer position : aggregateAttributePositionList) {
                                eventCopy.getOutputData()[position] = aggregateAttributeValueMap.get(position);
                            }
                            outputEventChunk.add(eventCopy);
                        }
                        outputEventChunks.add(outputEventChunk);
                        scheduledTime += value;
                        scheduler.notifyAt(scheduledTime);
                    }
                } else {
                    complexEventChunk.remove();
                    if (event.getType() == ComplexEvent.Type.CURRENT) {
                        eventList.add(event);
                        for (Integer position : aggregateAttributePositionList) {
                            aggregateAttributeValueMap.put(position, event.getOutputData()[position]);
                        }
                    } else if (event.getType() == ComplexEvent.Type.EXPIRED) {
                        for (Iterator<ComplexEvent> iterator = eventList.iterator(); iterator.hasNext(); ) {
                            ComplexEvent complexEvent = iterator.next();
                            if (comparator.compare(event, complexEvent) == 0) {
                                iterator.remove();
                                for (Integer position : aggregateAttributePositionList) {
                                    aggregateAttributeValueMap.put(position, event.getOutputData()[position]);
                                }
                                break;
                            }
                        }
                    } else if (event.getType() == ComplexEvent.Type.RESET) {
                        eventList.clear();
                        aggregateAttributeValueMap.clear();
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
        return new Object[]{eventList, aggregateAttributeValueMap};
    }

    @Override
    public void restoreState(Object[] state) {
        eventList = (LinkedList<ComplexEvent>) state[0];
        aggregateAttributeValueMap = (Map<Integer, Object>) state[1];
    }

    @Override
    public SnapshotOutputRateLimiter clone(String key, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        return new AggregationWindowedPerSnapshotOutputRateLimiter(id + key, value, scheduledExecutorService, aggregateAttributePositionList, wrappedSnapshotOutputRateLimiter);
    }


}
