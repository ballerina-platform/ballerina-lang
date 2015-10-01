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
 */kage org.wso2.siddhi.core.query.output.ratelimit.snapshot;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.query.selector.QuerySelector;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class AggregationGroupByWindowedPerSnapshotOutputRateLimiter extends AggregationWindowedPerSnapshotOutputRateLimiter {
    private Map<String, Map<Integer, Object>> groupByAggregateAttributeValueMap;
    private String currentKey = null;

    protected AggregationGroupByWindowedPerSnapshotOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService, List<Integer> aggregateAttributePositionList, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        super(id, value, scheduledExecutorService, aggregateAttributePositionList, wrappedSnapshotOutputRateLimiter);
        groupByAggregateAttributeValueMap = new HashMap<String, Map<Integer, Object>>();
        eventChunk = new ComplexEventChunk<ComplexEvent>();
    }

    public void process(ComplexEventChunk complexEventChunk) {
        ComplexEvent firstEvent = complexEventChunk.getFirst();
        try {
            lock.lock();
            if(firstEvent != null && firstEvent.getType() == ComplexEvent.Type.TIMER) {
                if (firstEvent.getTimestamp() >= scheduledTime) {
                    sendEvents();
                    scheduledTime = scheduledTime + value;
                    scheduler.notifyAt(scheduledTime);
                }
            } else {
                Map<Integer, Object> aggregateAttributeValueMap = groupByAggregateAttributeValueMap.get(currentKey);
                if (aggregateAttributeValueMap == null) {
                    aggregateAttributeValueMap = new HashMap<Integer, Object>(aggregateAttributePositionList.size());
                    groupByAggregateAttributeValueMap.put(currentKey, aggregateAttributeValueMap);
                }
                eventChunk.reset();
                processAndSend(eventChunk, aggregateAttributeValueMap, currentKey);

                currentKey = null;
                eventChunk.clear();

            }
        } finally {
            lock.unlock();
        }
    }

    public void add(ComplexEvent complexEvent) {
        try {
            lock.lock();
            String groupByKey = QuerySelector.getThreadLocalGroupByKey();
            if (currentKey == null) {
                currentKey = groupByKey;
                eventChunk.add(complexEvent);
            } else if (!currentKey.equals(groupByKey)) {
                Map<Integer, Object> aggregateAttributeValueMap = groupByAggregateAttributeValueMap.get(currentKey);
                if (aggregateAttributeValueMap == null) {
                    aggregateAttributeValueMap = new HashMap<Integer, Object>(aggregateAttributePositionList.size());
                    groupByAggregateAttributeValueMap.put(currentKey, aggregateAttributeValueMap);
                }
                processAndSend(eventChunk, aggregateAttributeValueMap, currentKey);
                eventChunk.clear();
                eventChunk.add(complexEvent);
                currentKey = groupByKey;
            }
        } finally {
            lock.unlock();
        }
    }

    protected ComplexEvent constructSendEvent(Object originalEventObject) {
        ComplexEvent originalEvent = ((GroupedEvent) originalEventObject).event;
        Map<Integer, Object> aggregateAttributeValueMap = groupByAggregateAttributeValueMap.get(((GroupedEvent) originalEventObject).groupByKey);
        return createSendEvent(originalEvent, aggregateAttributeValueMap);
    }

    protected ComplexEvent getEventFromList(Object eventObject) {
        return ((GroupedEvent) eventObject).event;
    }

    protected void addEventToList(ComplexEvent event, String groupByKey) {
        eventList.add(new GroupedEvent(event, groupByKey));
    }

    @Override
    public Object[] currentState() {
        return new Object[]{eventList,groupByAggregateAttributeValueMap, eventChunk, currentKey};
    }

    @Override
    public void restoreState(Object[] state) {
        eventList = (LinkedList<Object>) state[0];
        groupByAggregateAttributeValueMap = (Map<String, Map<Integer, Object>>) state[1];
        eventChunk = (ComplexEventChunk<ComplexEvent>) state[2];
        currentKey = (String) state[3];
    }

    @Override
    public SnapshotOutputRateLimiter clone(String key, WrappedSnapshotOutputRateLimiter wrappedSnapshotOutputRateLimiter) {
        return new AggregationGroupByWindowedPerSnapshotOutputRateLimiter(id + key, value, scheduledExecutorService, aggregateAttributePositionList, wrappedSnapshotOutputRateLimiter);
    }

    private class GroupedEvent {
        ComplexEvent event;
        String groupByKey;

        public GroupedEvent(ComplexEvent event, String groupByKey) {
            this.event = event;
            this.groupByKey = groupByKey;
        }
    }
}
