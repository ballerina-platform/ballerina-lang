package org.wso2.siddhi.core.table.cache;

import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.remove.RemoveEvent;
import org.wso2.siddhi.core.util.collection.list.SiddhiList;

public class BasicCacheManager implements CacheManager {

    private SiddhiList<StreamEvent> cacheList;
    private int limit;

    public BasicCacheManager(SiddhiList<StreamEvent> cacheList, int limit) {
        this.cacheList = cacheList;
        this.limit = limit;
    }

    @Override
    public void add(StreamEvent item) {
        if (item instanceof AtomicEvent) {
            if (cacheList.size() >= limit) {
                cacheList.remove(0);
            }
            cacheList.add(new RemoveEvent((Event) item, Long.MAX_VALUE));
        } else {
            for (int i = 0, size = ((ListEvent) item).getActiveEvents(); i < size; i++) {
                if (cacheList.size() >= limit) {
                    cacheList.remove(0);
                }
                cacheList.add(new RemoveEvent(((ListEvent) item).getEvent(i), Long.MAX_VALUE));
            }
        }
    }

    @Override
    public void delete(StreamEvent item) {
        cacheList.remove(item);
    }

    @Override
    public void read(StreamEvent item) {

    }

    @Override
    public void update(StreamEvent item) {

    }
}
