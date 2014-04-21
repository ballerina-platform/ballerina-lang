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
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class AggregationGroupByWindowedPerSnapshotOutputRateManager extends AggregationWindowedPerSnapshotOutputRateManager {

    private final Map<String, Map<Integer, Object>> groupByAggregateAttributeValueMap;

    public AggregationGroupByWindowedPerSnapshotOutputRateManager(Long value, ScheduledExecutorService scheduledExecutorService, final List<Integer> aggregateAttributePositionList, WrappedSnapshotOutputRateManager wrappedSnapshotOutputRateManager) {
        super(value, scheduledExecutorService, aggregateAttributePositionList,wrappedSnapshotOutputRateManager);
        groupByAggregateAttributeValueMap = new HashMap<String, Map<Integer, Object>>();
    }


    @Override
    public synchronized void send(long timeStamp, StreamEvent currentEvent, StreamEvent expiredEvent, String groupByKey) {
        this.timeStamp = timeStamp;
        Map<Integer, Object> aggregateAttributeValueMap = groupByAggregateAttributeValueMap.get(groupByKey);
        if (aggregateAttributeValueMap == null) {
            aggregateAttributeValueMap = new HashMap<Integer, Object>(aggregateAttributePositionList.size());
            groupByAggregateAttributeValueMap.put(groupByKey, aggregateAttributeValueMap);
        }
        processSend(timeStamp, currentEvent, expiredEvent, aggregateAttributeValueMap, groupByKey);
    }

    protected InEvent constructNewSendEvent(Object originalEventObject) {
        Event originalEvent = ((GroupedEvent) originalEventObject).event;
        Map<Integer, Object>  aggregateAttributeValueMap = groupByAggregateAttributeValueMap.get(((GroupedEvent) originalEventObject).groupByKey) ;
        return createNewSendEvent(originalEvent, aggregateAttributeValueMap);
    }

    protected Event getEventFromList(Object eventObject) {
        return ((GroupedEvent) eventObject).event;
    }

    protected void addEventToList(Event event, String groupByKey) {
        eventList.add(new GroupedEvent(event,groupByKey));
    }


    private class GroupedEvent {
        Event event;
        String groupByKey;
        public GroupedEvent(Event event, String groupByKey) {
            this.event = event;
            this.groupByKey = groupByKey;
        }
    }
}
