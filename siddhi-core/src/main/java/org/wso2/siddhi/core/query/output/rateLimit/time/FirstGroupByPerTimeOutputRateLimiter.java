/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.output.rateLimit.time;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.query.output.rateLimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.selector.QuerySelector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FirstGroupByPerTimeOutputRateLimiter extends OutputRateLimiter {
    static final Logger log = Logger.getLogger(FirstGroupByPerTimeOutputRateLimiter.class);

    private String id;
    private final Long value;
    private List<String> groupByKeys = new ArrayList<String>();
    private List<ComplexEvent> complexEventList = new ArrayList<ComplexEvent>();
    private ScheduledExecutorService scheduledExecutorService;

    public FirstGroupByPerTimeOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService) {
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public OutputRateLimiter clone(String key) {
        return new FirstGroupByPerTimeOutputRateLimiter(id + key, value, scheduledExecutorService);
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        ComplexEventChunk<ComplexEvent> eventChunk = new ComplexEventChunk<ComplexEvent>();
        for (ComplexEvent complexEvent : complexEventList) {
            eventChunk.add(complexEvent);
        }
        complexEventList.clear();
        sendToCallBacks(eventChunk);
    }

    @Override
    public void add(ComplexEvent complexEvent) {
        String groupByKey = QuerySelector.getThreadLocalGroupByKey();
        if (!groupByKeys.contains(groupByKey)) {
            groupByKeys.add(groupByKey);
            complexEventList.add(complexEvent);
        }
    }

    @Override
    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(new EventReSetter(), 0, value, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    private synchronized void resetEvents() {
        groupByKeys.clear();
    }


    private class EventReSetter implements Runnable {
        @Override
        public void run() {
            try {
                resetEvents();
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        }
    }
}
