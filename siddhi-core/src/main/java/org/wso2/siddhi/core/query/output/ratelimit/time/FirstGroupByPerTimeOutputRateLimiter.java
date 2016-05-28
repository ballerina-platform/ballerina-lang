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
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class FirstGroupByPerTimeOutputRateLimiter extends OutputRateLimiter implements Schedulable {
    static final Logger log = Logger.getLogger(FirstGroupByPerTimeOutputRateLimiter.class);

    private String id;
    private final Long value;
    private List<String> groupByKeys = new ArrayList<String>();
    private ComplexEventChunk<ComplexEvent> allComplexEventChunk;
    private ScheduledExecutorService scheduledExecutorService;
    private Scheduler scheduler;
    private long scheduledTime;

    public FirstGroupByPerTimeOutputRateLimiter(String id, Long value, ScheduledExecutorService scheduledExecutorService) {
        this.id = id;
        this.value = value;
        this.scheduledExecutorService = scheduledExecutorService;
        this.allComplexEventChunk = new ComplexEventChunk<ComplexEvent>(false);
    }

    @Override
    public OutputRateLimiter clone(String key) {
        FirstGroupByPerTimeOutputRateLimiter instance = new FirstGroupByPerTimeOutputRateLimiter(id + key, value, scheduledExecutorService);
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
                        if (allComplexEventChunk.getFirst() != null) {
                            ComplexEventChunk<ComplexEvent> eventChunk = new ComplexEventChunk<ComplexEvent>(complexEventChunk.isBatch());
                            eventChunk.add(allComplexEventChunk.getFirst());
                            allComplexEventChunk.clear();
                            groupByKeys.clear();
                            outputEventChunks.add(eventChunk);
                        } else {
                            groupByKeys.clear();
                        }
                        scheduledTime = scheduledTime + value;
                        scheduler.notifyAt(scheduledTime);
                    }
                } else if (event.getType() == ComplexEvent.Type.CURRENT || event.getType() == ComplexEvent.Type.EXPIRED) {
                    GroupedComplexEvent groupedComplexEvent = ((GroupedComplexEvent) event);
                    if (!groupByKeys.contains(groupedComplexEvent.getGroupKey())) {
                        complexEventChunk.remove();
                        groupByKeys.add(groupedComplexEvent.getGroupKey());
                        allComplexEventChunk.add(groupedComplexEvent.getComplexEvent());
                    }
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
        return new Object[]{allComplexEventChunk, groupByKeys};
    }

    @Override
    public void restoreState(Object[] state) {
        allComplexEventChunk = (ComplexEventChunk<ComplexEvent>) state[0];
        groupByKeys = (List<String>) state[1];
    }

}
