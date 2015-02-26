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
import org.wso2.siddhi.core.query.processor.Processor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LastPerTimeOutputRateLimiter extends OutputRateLimiter {
    private String id;
    private final Long value;
    private ComplexEvent lastevent = null;
    private ScheduledExecutorService scheduledExecutorService;

    static final Logger log = Logger.getLogger(LastPerTimeOutputRateLimiter.class);


    public LastPerTimeOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService) {
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public OutputRateLimiter clone(String key) {
        return new LastPerTimeOutputRateLimiter(id + key, value, scheduledExecutorService);
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {

    }

    @Override
    public void add(ComplexEvent complexEvent) {
        lastevent = complexEvent;
    }

    private synchronized void sendEvents() {
        if (lastevent != null) {
            ComplexEventChunk<ComplexEvent> complexEventChunk = new ComplexEventChunk<ComplexEvent>();
            complexEventChunk.add(lastevent);
            sendToCallBacks(complexEventChunk);
            lastevent = null;
        }
    }

    @Override
    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(new EventSender(), 0, value, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        //Nothing to stop
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
