package org.wso2.siddhi.core.table.cache;

import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.util.collection.list.SiddhiList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LRUCacheManager implements CacheManager {

    private ConcurrentHashMap<StreamEvent, Long> eventTimestamps;
    private SiddhiList<StreamEvent> cacheList;
    private int limit;

    public LRUCacheManager(SiddhiList<StreamEvent> cacheList, int limit) {
        this.eventTimestamps = new ConcurrentHashMap<StreamEvent, Long>();
        this.cacheList = cacheList;
        this.limit = limit;
    }

    @Override
    public void add(StreamEvent item) {
        cacheList.add(item);
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
        cacheList.remove(item);
    }

    @Override
    public void read(StreamEvent item) {
        eventTimestamps.put(item, System.currentTimeMillis());
    }

    @Override
    public void update(StreamEvent item) {
        eventTimestamps.put(item, System.currentTimeMillis());
    }
}
