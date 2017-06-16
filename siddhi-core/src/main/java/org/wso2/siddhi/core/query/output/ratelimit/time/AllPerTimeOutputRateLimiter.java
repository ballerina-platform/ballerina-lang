/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.output.ratelimit.time;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateLimiter;
import org.wso2.siddhi.core.util.Schedulable;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.parser.SchedulerParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Implementation of {@link OutputRateLimiter} which will collect pre-defined time period and the emit all
 * collected events as a batch.
 */

public class AllPerTimeOutputRateLimiter extends OutputRateLimiter implements Schedulable {

    private static final Logger log = Logger.getLogger(AllPerTimeOutputRateLimiter.class);
    private final Long value;
    private String id;
    private ScheduledExecutorService scheduledExecutorService;
    private Scheduler scheduler;
    private ComplexEventChunk<ComplexEvent> allComplexEventChunk;
    private long scheduledTime;
    private String queryName;

    public AllPerTimeOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService,
                                       String queryName) {
        this.id = id;
        this.value = value;
        this.queryName = queryName;
        this.scheduledExecutorService = scheduledExecutorService;
        allComplexEventChunk = new ComplexEventChunk<ComplexEvent>(false);
    }

    @Override
    public OutputRateLimiter clone(String key) {
        AllPerTimeOutputRateLimiter instance = new AllPerTimeOutputRateLimiter(id + key, value,
                                                                               scheduledExecutorService, queryName);
        instance.setLatencyTracker(latencyTracker);
        return instance;
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {

        ArrayList<ComplexEventChunk<ComplexEvent>> outputEventChunks = new ArrayList<ComplexEventChunk<ComplexEvent>>();
        complexEventChunk.reset();
        synchronized (this) {
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next();
                if (event.getType() == ComplexEvent.Type.TIMER) {
                    if (event.getTimestamp() >= scheduledTime) {
                        ComplexEvent first = allComplexEventChunk.getFirst();
                        if (first != null) {
                            allComplexEventChunk.clear();
                            ComplexEventChunk<ComplexEvent> outputEventChunk = new ComplexEventChunk<ComplexEvent>
                                    (complexEventChunk.isBatch());
                            outputEventChunk.add(first);
                            outputEventChunks.add(outputEventChunk);
                        }
                        scheduledTime = scheduledTime + value;
                        scheduler.notifyAt(scheduledTime);
                    }
                } else if (event.getType() == ComplexEvent.Type.CURRENT || event.getType() == ComplexEvent.Type
                        .EXPIRED) {
                    complexEventChunk.remove();
                    allComplexEventChunk.add(event);
                }
            }
        }
        for (ComplexEventChunk eventChunk : outputEventChunks) {
            sendToCallBacks(eventChunk);
        }
    }

    @Override
    public void start() {
        scheduler = SchedulerParser.parse(scheduledExecutorService, this, siddhiAppContext);
        scheduler.setStreamEventPool(new StreamEventPool(0, 0, 0, 5));
        scheduler.init(lockWrapper, queryName);
        long currentTime = System.currentTimeMillis();
        scheduledTime = currentTime + value;
        scheduler.notifyAt(scheduledTime);
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> state = new HashMap<>();
        synchronized (this) {
            state.put("AllComplexEventChunk", allComplexEventChunk.getFirst());
        }
        return state;
    }

    @Override
    public synchronized void restoreState(Map<String, Object> state) {
        allComplexEventChunk.clear();
        allComplexEventChunk.add((ComplexEvent) state.get("AllComplexEventChunk"));
    }

}
