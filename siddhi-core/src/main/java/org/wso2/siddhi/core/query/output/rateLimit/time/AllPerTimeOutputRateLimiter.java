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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AllPerTimeOutputRateLimiter extends OutputRateLimiter {

    private final Long value;
    private String id;
    private ScheduledExecutorService scheduledExecutorService;
    private ComplexEventChunk<ComplexEvent> allComplexEventChunk;

    static final Logger log = Logger.getLogger(AllPerTimeOutputRateLimiter.class);

    public AllPerTimeOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService) {
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
        allComplexEventChunk = new ComplexEventChunk<ComplexEvent>();
    }

    @Override
    public OutputRateLimiter clone(String key) {
        return new AllPerTimeOutputRateLimiter(id + key, value, scheduledExecutorService);
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {

    }

    @Override
    public void add(ComplexEvent complexEvent) {
        allComplexEventChunk.add(complexEvent);
    }


    @Override
    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(new EventSender(), 0, value, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        //Nothing to stop
    }


    private void sendEvents() {
        ComplexEvent first = allComplexEventChunk.getFirst();
        if (first != null) {
            allComplexEventChunk.clear();
            ComplexEventChunk<ComplexEvent> allEventChunk = new ComplexEventChunk<ComplexEvent>();
            allEventChunk.add(first);
            sendToCallBacks(allEventChunk);
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
