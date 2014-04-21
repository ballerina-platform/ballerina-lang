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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AllAggregationGroupByWindowedPerSnapshotOutputRateManager extends SnapshotOutputRateManager {

    private long timeStamp;
    Map<String, LastEventHolder> groupByKeyEvents = new LinkedHashMap<String, LastEventHolder>();


    public AllAggregationGroupByWindowedPerSnapshotOutputRateManager(Long value, ScheduledExecutorService scheduledExecutorService, WrappedSnapshotOutputRateManager wrappedSnapshotOutputRateManager) {
        super(wrappedSnapshotOutputRateManager);
        scheduledExecutorService.scheduleAtFixedRate(new EventSender(), 0, value, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void send(long timeStamp, StreamEvent currentEvent, StreamEvent expiredEvent, String groupByKey) {
        this.timeStamp = timeStamp;
        LastEventHolder lastEventHolder = groupByKeyEvents.get(groupByKey);
        if (lastEventHolder == null) {
            lastEventHolder = new LastEventHolder();
            groupByKeyEvents.put(groupByKey, lastEventHolder);
        }
        if (currentEvent != null) {
            if (currentEvent instanceof ListEvent) {
                lastEventHolder.addLastInEvent((InEvent) ((ListEvent) currentEvent).getEvent(((ListEvent) currentEvent).getActiveEvents()));
                lastEventHolder.count = lastEventHolder.count + ((ListEvent) currentEvent).getActiveEvents() - 1;
            } else {
                lastEventHolder.addLastInEvent((InEvent) currentEvent);
            }
        } else {
            if (expiredEvent instanceof ListEvent) {
                lastEventHolder.count = lastEventHolder.count - ((ListEvent) expiredEvent).getActiveEvents() - 1;
                lastEventHolder.removeLastInEvent();
            } else {
                lastEventHolder.removeLastInEvent();
            }
            if (lastEventHolder.lastInEvent == null) {
                groupByKeyEvents.remove(groupByKey);
            }
        }
    }

    public synchronized void sendEvents() {
        if (groupByKeyEvents.size() == 1) {
            Event event = groupByKeyEvents.values().iterator().next().lastInEvent;
            sendToCallBacks(timeStamp, event, null, event);
        } else if (groupByKeyEvents.size() > 1) {
            InEvent[] events = new InEvent[groupByKeyEvents.size()];
            int i = 0;
            for (LastEventHolder lastEventHolder : groupByKeyEvents.values()) {
                events[i] = lastEventHolder.lastInEvent;
                i++;
            }
            StreamEvent streamEvent = new InListEvent(events);
            sendToCallBacks(timeStamp, streamEvent, null, streamEvent);
        } else {
            sendToCallBacks(timeStamp, null, null, null);

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
        InEvent lastInEvent = null;

        public void addLastInEvent(InEvent lastInEvent) {
            this.lastInEvent = lastInEvent;
            count++;
        }

        public void removeLastInEvent() {
            count--;
            if (count <= 0) {
                lastInEvent = null;
            }
        }
    }
}
