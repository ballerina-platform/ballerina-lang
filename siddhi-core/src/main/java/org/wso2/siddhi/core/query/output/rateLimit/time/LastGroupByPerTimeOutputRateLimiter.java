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
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.state.StateEventCloner;
import org.wso2.siddhi.core.event.state.StateEventPool;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.query.output.rateLimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.selector.QuerySelector;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LastGroupByPerTimeOutputRateLimiter extends OutputRateLimiter {
    private String id;
    private final Long value;
    private Map<String, ComplexEvent> allGroupByKeyEvents = new LinkedHashMap<String, ComplexEvent>();
    private ScheduledExecutorService scheduledExecutorService;

    static final Logger log = Logger.getLogger(LastGroupByPerTimeOutputRateLimiter.class);

    public LastGroupByPerTimeOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService) {
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public OutputRateLimiter clone(String key) {
        return new LastGroupByPerTimeOutputRateLimiter(id + key, value, scheduledExecutorService);
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {
    }

    @Override
    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(new EventSender(), 0, value, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public void add(ComplexEvent complexEvent) {
        String groupByKey = QuerySelector.getThreadLocalGroupByKey();
        allGroupByKeyEvents.put(groupByKey + complexEvent.getType(), complexEvent);
    }

    private synchronized void sendEvents() {
        if (allGroupByKeyEvents.size() != 0) {
            ComplexEventChunk<ComplexEvent> complexEventChunk = new ComplexEventChunk<ComplexEvent>();
            for (ComplexEvent complexEvent : allGroupByKeyEvents.values()) {
                complexEventChunk.add(complexEvent);
            }

            sendToCallBacks(complexEventChunk);
            allGroupByKeyEvents.clear();
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
