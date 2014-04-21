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
package org.wso2.siddhi.core.query.output.ratelimit.event;

import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateManager;

import java.util.ArrayList;
import java.util.List;

public class AllPerEventOutputRateManager extends OutputRateManager {
    private final Integer value;

    private volatile int counter = 0;
    private long timeStamp;
    private List<InEvent> currentEventList;
    private List<InEvent> expiredEventList;
    private List<InEvent> allEventList;

    public AllPerEventOutputRateManager(Integer value) {
        this.value = value;
        currentEventList = new ArrayList<InEvent>(value);
        expiredEventList = new ArrayList<InEvent>(value);
        allEventList = new ArrayList<InEvent>(value);
    }

    @Override
    public synchronized void send(long timeStamp, StreamEvent currentEvent, StreamEvent expiredEvent, String groupByKey) {
        this.timeStamp = timeStamp;
        if (currentEvent != null) {
            if (currentEvent instanceof ListEvent) {
                for (int i = 0, size = ((ListEvent) currentEvent).getActiveEvents(); i < size; i++) {
                    currentEventList.add((InEvent) ((ListEvent) currentEvent).getEvent(i));
                    allEventList.add((InEvent) ((ListEvent) currentEvent).getEvent(i));
                    if (++counter == value) {
                        sendEvents();
                        this.timeStamp = timeStamp;
                    }
                }
            } else {
                currentEventList.add((InEvent) currentEvent);
                allEventList.add((InEvent) currentEvent);
                if (++counter == value) {
                    sendEvents();
                    this.timeStamp = timeStamp;
                }
            }
        }
        if (expiredEvent != null) {
            if (expiredEvent instanceof ListEvent) {
                for (int i = 0, size = ((ListEvent) expiredEvent).getActiveEvents(); i < size; i++) {
                    expiredEventList.add((InEvent) ((ListEvent) expiredEvent).getEvent(i));
                    allEventList.add((InEvent) ((ListEvent) expiredEvent).getEvent(i));
                    if (++counter == value) {
                        sendEvents();
                        this.timeStamp = timeStamp;
                    }
                }
            } else {
                expiredEventList.add((InEvent) expiredEvent);
                allEventList.add((InEvent) expiredEvent);
                if (++counter == value) {
                    sendEvents();
                    this.timeStamp = timeStamp;
                }
            }
        }
    }

    private void sendEvents() {
        StreamEvent currentEvent = null;
        StreamEvent expiredEvent = null;
        StreamEvent allEvent = null;

        if (currentEventList.size() == 1) {
            currentEvent = new InEvent(currentEventList.get(0));
        } else if (currentEventList.size() >= 1) {
            InEvent[] events = new InEvent[currentEventList.size()];
            currentEventList.toArray(events);
            currentEvent = new InListEvent(events);
        }

        if (expiredEventList.size() == 1) {
            expiredEvent = new InEvent(expiredEventList.get(0));
        } else if (expiredEventList.size() >= 1) {
            InEvent[] events = new InEvent[expiredEventList.size()];
            expiredEventList.toArray(events);
            expiredEvent = new InListEvent(events);
        }

        if (allEventList.size() == 1) {
            allEvent = new InEvent(allEventList.get(0));
        } else if (allEventList.size() >= 1) {
            InEvent[] events = new InEvent[allEventList.size()];
            allEventList.toArray(events);
            allEvent = new InListEvent(events);
        }
        sendToCallBacks(timeStamp, currentEvent, expiredEvent, allEvent);
        timeStamp = 0;
        currentEventList.clear();
        expiredEventList.clear();
        allEventList.clear();
        counter=0;
    }
}
