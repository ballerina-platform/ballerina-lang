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

import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AllAggregationPerSnapshotOutputRateManager extends SnapshotOutputRateManager {

    private long timeStamp;
    private InEvent event = null;
    private long count = 0;

    public AllAggregationPerSnapshotOutputRateManager(Long value, ScheduledExecutorService scheduledExecutorService, WrappedSnapshotOutputRateManager wrappedSnapshotOutputRateManager) {
        super(wrappedSnapshotOutputRateManager);
        scheduledExecutorService.scheduleAtFixedRate(new EventSender(), 0, value, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void send(long timeStamp, StreamEvent currentEvent, StreamEvent expiredEvent, String groupByKey) {
        this.timeStamp = timeStamp;
        if (currentEvent != null) {
            if (currentEvent instanceof ListEvent) {
                event = (InEvent) ((ListEvent) currentEvent).getEvent(((ListEvent) currentEvent).getActiveEvents());
                count+=((ListEvent) currentEvent).getActiveEvents();
            } else {
                event = (InEvent) currentEvent;
                count++;
            }
        } else {
            if (expiredEvent instanceof ListEvent) {
                count-=((ListEvent) expiredEvent).getActiveEvents();
            } else {
                count--;
            }
        }
    }

    public synchronized void sendEvents() {
        if (event != null && count > 0) {
            sendToCallBacks(timeStamp, event, null, event);
        }  else {
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
}
