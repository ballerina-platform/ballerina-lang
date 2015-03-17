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

package org.wso2.siddhi.core.query.output.ratelimit.event;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.selector.QuerySelector;

import java.util.ArrayList;
import java.util.List;


public class FirstGroupByPerEventOutputRateLimiter extends OutputRateLimiter {
    private final Integer value;
    private String id;
    private List<ComplexEvent> complexEventList;
    private volatile int counter = 0;
    List<String> groupByKeys = new ArrayList<String>();

    public FirstGroupByPerEventOutputRateLimiter(String id, Integer value) {
        this.id = id;
        this.value = value;
        complexEventList = new ArrayList<ComplexEvent>();
    }

    @Override
    public OutputRateLimiter clone(String key) {
        return new FirstGroupByPerEventOutputRateLimiter(id + key, value);
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
        if (++counter == value) {
            counter = 0;
            groupByKeys.clear();
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
    public Object[] currentState() {
        return new Object[]{complexEventList, groupByKeys, counter};
    }

    @Override
    public void restoreState(Object[] state) {
        complexEventList = (List<ComplexEvent>) state[0];
        groupByKeys = (List<String>) state[1];
        counter = (Integer) state[2];
    }

}
