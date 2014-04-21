/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.core.query.output.ratelimit.snapshot;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AggregationWindowedPerSnapshotOutputRateManager extends SnapshotOutputRateManager {

    protected final Long value;
    protected final ScheduledExecutorService scheduledExecutorService;
    protected long timeStamp;
    protected LinkedList<Object> eventList;
    protected Comparator comparator;
    protected List<Integer> aggregateAttributePositionList;
    private Map<Integer, Object> aggregateAttributeValueMap;


    public AggregationWindowedPerSnapshotOutputRateManager(Long value, ScheduledExecutorService scheduledExecutorService, final List<Integer> aggregateAttributePositionList, WrappedSnapshotOutputRateManager wrappedSnapshotOutputRateManager) {
        super(wrappedSnapshotOutputRateManager);
        this.eventList = new LinkedList<Object>();
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
        this.aggregateAttributePositionList = aggregateAttributePositionList;
        Collections.sort(aggregateAttributePositionList);
        aggregateAttributeValueMap = new HashMap<Integer, Object>(aggregateAttributePositionList.size());
        this.comparator = new Comparator<Event>() {
            Integer[] aggregateAttributePositions = aggregateAttributePositionList.toArray(new Integer[aggregateAttributePositionList.size()]);
            int ignoreIndexLength = aggregateAttributePositions.length;

            @Override
            public int compare(Event event1, Event event2) {
                int ignoreIndex = 0;
                int ignoreIndexPosition = aggregateAttributePositions[0];
                Object[] data = event1.getData();
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
                    if (!data[i].equals(event2.getData()[i])) {
                        return 1;
                    }

                }
                return 0;
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(new EventSender(), 0, value, TimeUnit.MILLISECONDS);
    }


    @Override
    public synchronized void send(long timeStamp, StreamEvent currentEvent, StreamEvent expiredEvent, String groupByKey) {
        processSend(timeStamp, currentEvent, expiredEvent, aggregateAttributeValueMap, groupByKey);
    }

    protected void processSend(long timeStamp, StreamEvent currentEvent, StreamEvent expiredEvent, Map<Integer, Object> aggregateAttributeValueMap, String groupByKey) {
        this.timeStamp = timeStamp;
        if (currentEvent != null) {
            if (currentEvent instanceof ListEvent) {
                for (int i = 0, size = ((ListEvent) currentEvent).getActiveEvents(); i < size; i++) {
                    Event event = ((ListEvent) currentEvent).getEvent(i);
                    addEventToList(event, groupByKey);
                    for (Integer position : aggregateAttributePositionList) {
                        aggregateAttributeValueMap.put(position, event.getData(position));
                    }
                }
            } else {
                addEventToList((InEvent) currentEvent, groupByKey);
                for (Integer position : aggregateAttributePositionList) {
                    aggregateAttributeValueMap.put(position, ((InEvent) currentEvent).getData(position));
                }
            }
        }
        if (expiredEvent != null) {
            if (expiredEvent instanceof ListEvent) {
                for (int i = 0, size = ((ListEvent) expiredEvent).getActiveEvents(); i < size; i++) {
                    for (Iterator iterator = eventList.iterator(); iterator.hasNext(); ) {
                        Event event = getEventFromList(iterator.next());
                        if (comparator.compare(event, expiredEvent) == 0) {
                            iterator.remove();
                            for (Integer position : aggregateAttributePositionList) {
                                aggregateAttributeValueMap.put(position, event.getData(position));
                            }
                            break;
                        }
                    }
                }
            } else {
                for (Iterator iterator = eventList.iterator(); iterator.hasNext(); ) {
                    Event event = getEventFromList(iterator.next());
                    if (comparator.compare(event, expiredEvent) == 0) {
                        iterator.remove();
                        for (Integer position : aggregateAttributePositionList) {
                            aggregateAttributeValueMap.put(position, event.getData(position));
                        }
                        break;
                    }
                }
            }
        }
    }

    protected synchronized void sendEvents() {
        if (eventList.size() > 0) {
            StreamEvent newEvent = null;
            if (eventList.size() == 1) {
                newEvent = constructNewSendEvent(eventList.get(0));
            } else if (eventList.size() >= 1) {
                InEvent[] events = new InEvent[eventList.size()];
                for (int i = 0; i < eventList.size(); i++) {
                    events[i] = constructNewSendEvent(eventList.get(i));
                }
                newEvent = new InListEvent(events);
            }
            sendToCallBacks(timeStamp, newEvent, null, newEvent);
        } else {
            sendToCallBacks(timeStamp, null, null, null);
        }
    }

    protected InEvent constructNewSendEvent(Object originalEventObject) {
        Event originalEvent = (Event) originalEventObject;
        return createNewSendEvent(originalEvent, aggregateAttributeValueMap);
    }

    protected InEvent createNewSendEvent(Event originalEvent, Map<Integer, Object> aggregateAttributeValueMap) {
        Object[] newData = new Object[originalEvent.getData().length];
        System.arraycopy(originalEvent.getData(), 0, newData, 0, originalEvent.getData().length);
        for (Integer position : aggregateAttributePositionList) {
            newData[position] = aggregateAttributeValueMap.get(position);
        }
        return new InEvent(originalEvent.getStreamId(), originalEvent.getTimeStamp(), newData);
    }

    protected Event getEventFromList(Object eventObject) {
        return (Event) eventObject;
    }

    protected void addEventToList(Event event, String groupByKey) {
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
