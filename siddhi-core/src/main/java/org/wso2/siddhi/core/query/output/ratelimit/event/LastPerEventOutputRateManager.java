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
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateManager;

public class LastPerEventOutputRateManager extends OutputRateManager {
    private final Integer value;

    private volatile int counter = 0;

    public LastPerEventOutputRateManager(Integer value) {
        this.value = value;
    }

    @Override
    public synchronized void send(long timeStamp, StreamEvent currentEvent, StreamEvent expiredEvent, String groupByKey) {
        if (currentEvent != null) {
            if (currentEvent instanceof ListEvent) {
                for (int i = 0, size = ((ListEvent) currentEvent).getActiveEvents(); i < size; i++) {
                    if (++counter == value) {
                        InEvent event = ((InEvent) ((ListEvent) currentEvent).getEvent(i));
                        sendToCallBacks(timeStamp, event, null, event);
                        counter=0;
                    }
                }
            } else {
                if (++counter == value) {
                    sendToCallBacks(timeStamp, currentEvent, null, currentEvent);
                    counter=0;
                }
            }
        }
        if (expiredEvent != null) {
            if (expiredEvent instanceof ListEvent) {
                for (int i = 0, size = ((ListEvent) expiredEvent).getActiveEvents(); i < size; i++) {
                    if (++counter == value) {
                        InEvent event = ((InEvent) ((ListEvent) expiredEvent).getEvent(i));
                        sendToCallBacks(timeStamp, null, event, event);
                        counter=0;
                    }
                }
            } else {
                if (++counter == value) {
                    sendToCallBacks(timeStamp, null, expiredEvent, expiredEvent);
                    counter=0;
                }
            }
        }
    }
}
