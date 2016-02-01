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

public class LRUCacheManager implements CacheManager {

    private ConcurrentHashMap<StreamEvent, Long> eventTimestamps;
    private final LinkedList<StreamEvent> cacheList;
    private long limit;

    public LRUCacheManager(LinkedList<StreamEvent> cacheList, long limit) {
        this.eventTimestamps = new ConcurrentHashMap<StreamEvent, Long>();
        this.cacheList = cacheList;
        this.limit = limit;
    }

    @Override
    public void add(StreamEvent item) {
        eventTimestamps.put(item, System.currentTimeMillis());
        if (eventTimestamps.size() >= limit) {
            StreamEvent leastRecent = null;
            long leastRecentTime = Long.MAX_VALUE;
            for (Map.Entry<StreamEvent, Long> entry : eventTimestamps.entrySet()) {
                if (leastRecentTime > entry.getValue()) {
                    leastRecentTime = entry.getValue();
                    leastRecent = entry.getKey();
                }
            }
            eventTimestamps.remove(leastRecent);
            cacheList.remove(leastRecent);
        }
    }

    @Override
    public void delete(StreamEvent item) {
        eventTimestamps.remove(item);
    }

    @Override
    public void read(StreamEvent item) {
        eventTimestamps.put(item, System.currentTimeMillis());
    }

    @Override
    public void update(StreamEvent item) {
        eventTimestamps.put(item, System.currentTimeMillis());
    }

    @Override
    public void invalidateCache() {
        cacheList.clear();
    }

    @Override
    public boolean isContains(StreamEvent item) {
        return cacheList.contains(item);
    }
}
