/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.core.tracer;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StreamEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class EventMonitor {
    public abstract void trace(ComplexEvent complexEvent, String message);

    private ConcurrentHashMap<String, AtomicLong> streamStatsMap = new ConcurrentHashMap();
    private volatile long statStartTime = System.currentTimeMillis();
    private volatile long lastUpdateTime = statStartTime;

    void calculateStats(StreamEvent streamEvent) {
        if (streamEvent instanceof Event) {
            String streamId = ((Event) streamEvent).getStreamId();
            AtomicLong value = streamStatsMap.get(streamId);
            if (value == null) {
                streamStatsMap.putIfAbsent(streamId, new AtomicLong());
                streamStatsMap.get(streamId).incrementAndGet();
            } else {
                value.incrementAndGet();
            }
        } else {   //List Event
            for (Event event : ((ListEvent) streamEvent).getEvents()) {
                String streamId = event.getStreamId();
                AtomicLong value = streamStatsMap.get(streamId);
                if (value == null) {
                    streamStatsMap.putIfAbsent(streamId, new AtomicLong());
                    streamStatsMap.get(streamId).incrementAndGet();
                } else {
                    value.incrementAndGet();
                }
            }
        }
        lastUpdateTime = System.currentTimeMillis();
    }

    public ConcurrentHashMap<String, AtomicLong> getStreamStats() {
        return streamStatsMap;
    }

    public void resetStats() {
        streamStatsMap.clear();
        statStartTime = System.currentTimeMillis();
        lastUpdateTime = statStartTime;
    }

    public long getStatStartTime() {
        return statStartTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
}
