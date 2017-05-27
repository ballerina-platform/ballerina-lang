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

package org.wso2.siddhi.core.query.output.ratelimit.event;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.GroupedComplexEvent;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateLimiter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link OutputRateLimiter} which will collect pre-defined number of events and the emit only the
 * first event. This implementation specifically handle queries with group by.
 */
public class FirstGroupByPerEventOutputRateLimiter extends OutputRateLimiter {
    private final Integer value;
    private List<String> groupByKeys = new ArrayList<String>();
    private String id;
    private ComplexEventChunk<ComplexEvent> allComplexEventChunk;
    private volatile int counter = 0;

    public FirstGroupByPerEventOutputRateLimiter(String id, Integer value) {
        this.id = id;
        this.value = value;
        allComplexEventChunk = new ComplexEventChunk<ComplexEvent>(false);
    }

    @Override
    public OutputRateLimiter clone(String key) {
        FirstGroupByPerEventOutputRateLimiter instance = new FirstGroupByPerEventOutputRateLimiter(id + key, value);
        instance.setLatencyTracker(latencyTracker);
        return instance;
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {
        complexEventChunk.reset();
        ArrayList<ComplexEventChunk<ComplexEvent>> outputEventChunks = new ArrayList<ComplexEventChunk<ComplexEvent>>();
        synchronized (this) {
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next();
                if (event.getType() == ComplexEvent.Type.CURRENT || event.getType() == ComplexEvent.Type.EXPIRED) {
                    complexEventChunk.remove();
                    GroupedComplexEvent groupedComplexEvent = ((GroupedComplexEvent) event);
                    if (!groupByKeys.contains(groupedComplexEvent.getGroupKey())) {
                        groupByKeys.add(groupedComplexEvent.getGroupKey());
                        allComplexEventChunk.add(groupedComplexEvent.getComplexEvent());
                    }
                    if (++counter == value) {
                        if (allComplexEventChunk.getFirst() != null) {
                            ComplexEventChunk<ComplexEvent> outputEventChunk = new ComplexEventChunk<ComplexEvent>
                                    (complexEventChunk.isBatch());
                            outputEventChunk.add(allComplexEventChunk.getFirst());
                            outputEventChunks.add(outputEventChunk);
                            allComplexEventChunk.clear();
                            counter = 0;
                            groupByKeys.clear();
                        } else {
                            counter = 0;
                            groupByKeys.clear();
                        }

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
        //Nothing to start
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> state = new HashMap<>();
        synchronized (this) {
            state.put("Counter", counter);
            state.put("GroupByKeys", groupByKeys);
            state.put("AllComplexEventChunk", allComplexEventChunk.getFirst());
        }
        return state;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        synchronized (this) {
            counter = (int) state.get("Counter");
            groupByKeys = (List<String>) state.get("GroupByKeys");
            allComplexEventChunk.clear();
            allComplexEventChunk.add((ComplexEvent) state.get("AllComplexEventChunk"));
        }
    }

}
