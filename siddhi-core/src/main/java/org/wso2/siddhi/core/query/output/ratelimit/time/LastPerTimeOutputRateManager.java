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
package org.wso2.siddhi.core.query.output.ratelimit.time;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateManager;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LastPerTimeOutputRateManager extends OutputRateManager {
    private final Long value;
    static final Logger log = Logger.getLogger(LastPerTimeOutputRateManager.class);

    private volatile long timeStamp;
    private volatile InEvent lastCurrentEvent = null;
    private volatile InEvent lastExpiredEvent = null;
    private volatile InEvent lastAllEvent = null;

    public LastPerTimeOutputRateManager(Long value, ScheduledExecutorService scheduledExecutorService) {
        this.value = value;
        scheduledExecutorService.scheduleAtFixedRate(new EventSender(), 0, value.longValue(), TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void send(long timeStamp, StreamEvent currentEvent, StreamEvent expiredEvent, String groupByKey) {
        if (currentEvent != null) {
            if (currentEvent instanceof ListEvent) {
                int lastEventIndex = ((ListEvent) currentEvent).getActiveEvents() - 1;
                InEvent lastEvent = ((InEvent) ((ListEvent) currentEvent).getEvent(lastEventIndex));
                lastCurrentEvent = lastEvent;
                lastExpiredEvent = null;
                lastAllEvent = lastEvent;
                this.timeStamp = timeStamp;
            } else {
                lastCurrentEvent = ((InEvent) currentEvent);
                lastExpiredEvent = null;
                lastAllEvent = ((InEvent) currentEvent);
                this.timeStamp = timeStamp;
            }
        }
        if (expiredEvent != null) {
            if (expiredEvent instanceof ListEvent) {
                int lastEventIndex = ((ListEvent) expiredEvent).getActiveEvents() - 1;
                InEvent lastEvent = ((InEvent) ((ListEvent) expiredEvent).getEvent(lastEventIndex));
                lastCurrentEvent = null;
                lastExpiredEvent = lastEvent;
                lastAllEvent = lastEvent;
                this.timeStamp = timeStamp;
            } else {
                lastCurrentEvent = null;
                lastExpiredEvent = ((InEvent) expiredEvent);
                lastAllEvent = ((InEvent) expiredEvent);
                this.timeStamp = timeStamp;
            }
        }

    }

    private synchronized void sendEvents() {
        if (lastAllEvent != null) {
            StreamEvent currentEvent = null;
            StreamEvent expiredEvent = null;
            StreamEvent allEvent = null;

            if (lastCurrentEvent != null) {
                currentEvent = new InEvent(lastCurrentEvent);
            }
            if (lastExpiredEvent != null) {
                expiredEvent = new InEvent(lastExpiredEvent);
            }
            if (lastAllEvent != null) {
                allEvent = new InEvent(lastAllEvent);
            }
            sendToCallBacks(timeStamp, currentEvent, expiredEvent, allEvent);
            timeStamp = 0;
            lastCurrentEvent = null;
            lastExpiredEvent = null;
            lastAllEvent = null;
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
