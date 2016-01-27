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

package org.wso2.siddhi.extension.eventtable.cache;


import org.wso2.siddhi.core.event.stream.StreamEvent;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LFUCacheManager implements CacheManager {

    private ConcurrentHashMap<StreamEvent, Integer> eventAccessFrequencies;
    private final LinkedList<StreamEvent> cacheList;
    private long limit;

    public LFUCacheManager(LinkedList<StreamEvent> cacheList, long limit) {
        this.eventAccessFrequencies = new ConcurrentHashMap<StreamEvent, Integer>();
        this.cacheList = cacheList;
        this.limit = limit;
    }


    @Override
    public void add(StreamEvent item) {
        eventAccessFrequencies.put(item, 1);
        if (eventAccessFrequencies.size() >= limit) {
            StreamEvent leastFrequentItem = null;
            Integer leastFrequency = Integer.MAX_VALUE;
            for (Map.Entry<StreamEvent, Integer> entry : eventAccessFrequencies.entrySet()) {
                if (leastFrequency > entry.getValue()) {
                    leastFrequency = entry.getValue();
                    leastFrequentItem = entry.getKey();
                }
            }
            eventAccessFrequencies.remove(leastFrequentItem);
            cacheList.remove(leastFrequentItem);
        }
    }

    @Override
    public void delete(StreamEvent item) {
        eventAccessFrequencies.remove(item);
    }

    @Override
    public void read(StreamEvent item) {
        Integer currentAccessAttempts = eventAccessFrequencies.get(item);
        if (currentAccessAttempts != null) {
            eventAccessFrequencies.put(item, currentAccessAttempts + 1);
        }
    }

    @Override
    public void update(StreamEvent item) {
        Integer currentAccessAttempts = eventAccessFrequencies.get(item);
        if (currentAccessAttempts != null) {
            eventAccessFrequencies.put(item, currentAccessAttempts + 1);
        }
    }

    @Override
    public void invalidateCache() {
        cacheList.clear();
    }

    @Override
    public boolean isContains(StreamEvent item) {
        return eventAccessFrequencies.contains(item);
    }

}
