package org.wso2.siddhi.core.table.cache;

import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.util.collection.list.SiddhiList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LFUCacheManager implements CacheManager {

    private ConcurrentHashMap<StreamEvent, Integer> eventAccessFrequencies;
    private SiddhiList<StreamEvent> cacheList;
    private int limit;

    public LFUCacheManager(SiddhiList<StreamEvent> cacheList, int limit) {
        this.eventAccessFrequencies = new ConcurrentHashMap<StreamEvent, Integer>();
        this.cacheList = cacheList;
        this.limit = limit;
    }


    @Override
    public void add(StreamEvent item) {
        cacheList.add(item);
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
        cacheList.remove(item);
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

}
