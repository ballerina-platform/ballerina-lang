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
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LastGroupByPerTimeOutputRateManager extends OutputRateManager {
    private final Long value;

    private Map<String, InEvent> currentGroupByKeyEvents = new LinkedHashMap<String, InEvent>();
    private Map<String, InEvent> expiredGroupByKeyEvents = new LinkedHashMap<String, InEvent>();
    private Map<String, InEvent> allGroupByKeyEvents = new LinkedHashMap<String, InEvent>();
    static final Logger log = Logger.getLogger(LastGroupByPerTimeOutputRateManager.class);


    public LastGroupByPerTimeOutputRateManager(Long value, ScheduledExecutorService scheduledExecutorService) {
        this.value = value;
        scheduledExecutorService.scheduleAtFixedRate(new EventSender(), 0, value.longValue(), TimeUnit.MILLISECONDS);

    }

    @Override
    public synchronized void send(long timeStamp, StreamEvent currentEvent, StreamEvent expiredEvent, String groupByKey) {
        if (currentEvent != null) {
            if (currentEvent instanceof ListEvent) {
                for (int i = 0, size = ((ListEvent) currentEvent).getActiveEvents(); i < size; i++) {
                    currentGroupByKeyEvents.put(groupByKey, (InEvent) ((ListEvent) currentEvent).getEvent(i));
                    allGroupByKeyEvents.put(groupByKey + "-current", (InEvent) ((ListEvent) currentEvent).getEvent(i));
                }
            } else {
                currentGroupByKeyEvents.put(groupByKey, (InEvent) currentEvent);
                allGroupByKeyEvents.put(groupByKey + "-current", (InEvent) currentEvent);
            }
        }
        if (expiredEvent != null) {
            if (expiredEvent instanceof ListEvent) {
                for (int i = 0, size = ((ListEvent) expiredEvent).getActiveEvents(); i < size; i++) {
                    expiredGroupByKeyEvents.put(groupByKey, (InEvent) ((ListEvent) expiredEvent).getEvent(i));
                    allGroupByKeyEvents.put(groupByKey + "-expired", (InEvent) ((ListEvent) expiredEvent).getEvent(i));
                }
            } else {
                expiredGroupByKeyEvents.put(groupByKey, (InEvent) expiredEvent);
                allGroupByKeyEvents.put(groupByKey + "-expired", (InEvent) expiredEvent);
            }
        }
    }

    private synchronized void sendEvents(long timeStamp) {
        if (allGroupByKeyEvents.size() != 0) {
            if (allGroupByKeyEvents.size() == 1) {
                if (currentGroupByKeyEvents.size() > 0) {
                    InEvent event=currentGroupByKeyEvents.values().iterator().next();
                    sendToCallBacks(timeStamp, event, null, event);
                } else {
                    InEvent event=expiredGroupByKeyEvents.values().iterator().next();
                    sendToCallBacks(timeStamp, null, event, event);
                }
            } else {
                StreamEvent allEvent;
                StreamEvent currentEvent = null;
                StreamEvent expiredEvent = null;

                if (currentGroupByKeyEvents.size() > 0) {
                    InEvent[] currentEvents = new InEvent[currentGroupByKeyEvents.size()];
                    currentGroupByKeyEvents.values().toArray(currentEvents);
                    currentEvent = new InListEvent(currentEvents);
                }
                if (expiredGroupByKeyEvents.size() > 0) {
                    InEvent[] expiredEvents = new InEvent[expiredGroupByKeyEvents.size()];
                    expiredGroupByKeyEvents.values().toArray(expiredEvents);
                    expiredEvent = new InListEvent(expiredEvents);
                }

                InEvent[] allEvents = new InEvent[allGroupByKeyEvents.size()];
                allGroupByKeyEvents.values().toArray(allEvents);
                allEvent = new InListEvent(allEvents);
                sendToCallBacks(timeStamp, currentEvent, expiredEvent, allEvent);

            }

            allGroupByKeyEvents.clear();
            currentGroupByKeyEvents.clear();
            expiredGroupByKeyEvents.clear();
        }
    }


    private class EventSender implements Runnable {
        @Override
        public void run() {
            try {
                sendEvents(System.currentTimeMillis());
            }catch (Throwable t){
                log.error(t.getMessage(), t) ;
            }
        }
    }
}
