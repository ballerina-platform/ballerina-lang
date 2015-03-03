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

package org.wso2.siddhi.core.query.output.rateLimit.event;


import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.query.output.rateLimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.selector.QuerySelector;

import java.util.LinkedHashMap;
import java.util.Map;

public class LastGroupByPerEventOutputRateLimiter extends OutputRateLimiter {
    private final Integer value;
    private String id;
    private volatile int counter = 0;
    Map<String, ComplexEvent> allGroupByKeyEvents = new LinkedHashMap<String, ComplexEvent>();

    public LastGroupByPerEventOutputRateLimiter(String id,Integer value){
        this.id = id;
        this.value = value;
    }

    @Override
    public OutputRateLimiter clone(String key) {
        LastGroupByPerEventOutputRateLimiter lastGroupByPerEventOutputRateLimiter = new LastGroupByPerEventOutputRateLimiter(id+key,value);
        return lastGroupByPerEventOutputRateLimiter;
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {

    }

    @Override
    public void add(ComplexEvent complexEvent) {
        String groupByKey = QuerySelector.getThreadLocalGroupByKey();
        allGroupByKeyEvents.put(groupByKey+complexEvent.getType(),complexEvent);
        if (++counter == value) {
            sendEvents();
        }

    }

    private void sendEvents() {
        if (allGroupByKeyEvents.size() != 0) {
            ComplexEventChunk<ComplexEvent> complexEventChunk = new ComplexEventChunk<ComplexEvent>();

            for (ComplexEvent complexEvent : allGroupByKeyEvents.values()) {
                complexEventChunk.add(complexEvent);
            }
            sendToCallBacks(complexEventChunk);
        }
        counter = 0;
        allGroupByKeyEvents.clear();
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
    public Object[] currentState() {
        return new Object[]{allGroupByKeyEvents, counter};
    }

    @Override
    public void restoreState(Object[] state) {
        allGroupByKeyEvents = (Map<String, ComplexEvent>) state[0];
        counter = (Integer) state[1];
    }
}
