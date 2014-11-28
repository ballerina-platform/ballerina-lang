package org.wso2.siddhi.core.table.cache;

import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.util.collection.list.SiddhiList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LRUCacheManager implements CacheManager {

    private ConcurrentHashMap<StreamEvent, Long> eventSequenceNumbers;
    private SiddhiList<StreamEvent> cacheList;
    private int limit;
    private volatile long sequenceNo;

    public LRUCacheManager() {
    }

    @Override
    public void init(SiddhiList<StreamEvent> cacheList, int limit) {
        this.eventSequenceNumbers = new ConcurrentHashMap<StreamEvent, Long>();
        this.cacheList = cacheList;
        this.limit = limit;
        this.sequenceNo = 0;
    }

    @Override
    public void add(StreamEvent item) {
        cacheList.add(item);
        eventSequenceNumbers.put(item, ++sequenceNo);
        if (eventSequenceNumbers.size() >= limit) {
            StreamEvent leastRecent = null;
            long leastRecentTime = Long.MAX_VALUE;
            for (Map.Entry<StreamEvent, Long> entry : eventSequenceNumbers.entrySet()) {
                if (leastRecentTime > entry.getValue()) {
                    leastRecentTime = entry.getValue();
                    leastRecent = entry.getKey();
                }
            }
            eventSequenceNumbers.remove(leastRecent);
            cacheList.remove(leastRecent);
        }
    }

    @Override
    public void delete(StreamEvent item) {
        eventSequenceNumbers.remove(item);
        cacheList.remove(item);
    }

    @Override
    public void read(StreamEvent item) {
        eventSequenceNumbers.put(item, ++sequenceNo);
    }

    @Override
    public void update(StreamEvent item) {
        eventSequenceNumbers.put(item, ++sequenceNo);
    }
}
