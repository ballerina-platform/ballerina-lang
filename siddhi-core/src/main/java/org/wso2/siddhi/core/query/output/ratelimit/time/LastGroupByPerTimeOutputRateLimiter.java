/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.siddhi.core.event.GroupedComplexEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateLimiter;
import org.wso2.siddhi.core.util.Schedulable;
import org.wso2.siddhi.core.util.Scheduler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class LastGroupByPerTimeOutputRateLimiter extends OutputRateLimiter implements Schedulable {
    private String id;
    private final Long value;
    private Map<String, ComplexEvent> allGroupByKeyEvents = new LinkedHashMap<String, ComplexEvent>();
    private ScheduledExecutorService scheduledExecutorService;
    private Scheduler scheduler;
    private long scheduledTime;

    static final Logger log = Logger.getLogger(LastGroupByPerTimeOutputRateLimiter.class);

    public LastGroupByPerTimeOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService) {
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public OutputRateLimiter clone(String key) {
        LastGroupByPerTimeOutputRateLimiter instance = new LastGroupByPerTimeOutputRateLimiter(id + key, value, scheduledExecutorService);
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
                        if (allGroupByKeyEvents.size() != 0) {
                            ComplexEventChunk<ComplexEvent> outputEventChunk = new ComplexEventChunk<ComplexEvent>(complexEventChunk.isBatch());
                            for (ComplexEvent complexEvent : allGroupByKeyEvents.values()) {
                                outputEventChunk.add(complexEvent);
                            }
                            outputEventChunks.add(outputEventChunk);
                            allGroupByKeyEvents.clear();
                        }
                        scheduledTime = scheduledTime + value;
                        scheduler.notifyAt(scheduledTime);
                    }
                } else if (event.getType() == ComplexEvent.Type.CURRENT || event.getType() == ComplexEvent.Type.EXPIRED) {
                    complexEventChunk.remove();
                    GroupedComplexEvent groupedComplexEvent = ((GroupedComplexEvent) event);
                    allGroupByKeyEvents.put(groupedComplexEvent.getGroupKey(), groupedComplexEvent.getComplexEvent());
                }
            }
        }
        for (ComplexEventChunk eventChunk : outputEventChunks) {
            sendToCallBacks(eventChunk);
        }

    }

    @Override
    public void start() {
        scheduler = new Scheduler(scheduledExecutorService, this);
        scheduler.setStreamEventPool(new StreamEventPool(0, 0, 0, 5));
        long currentTime = System.currentTimeMillis();
        scheduledTime = currentTime + value;
        scheduler.notifyAt(scheduledTime);
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public Object[] currentState() {
        return new Object[]{allGroupByKeyEvents};
    }

    @Override
    public void restoreState(Object[] state) {
        allGroupByKeyEvents = (Map<String, ComplexEvent>) state[0];
    }

}
