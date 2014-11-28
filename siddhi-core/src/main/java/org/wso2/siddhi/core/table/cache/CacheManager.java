package org.wso2.siddhi.core.table.cache;

import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.util.collection.list.SiddhiList;

public interface CacheManager {

    public void init(SiddhiList<StreamEvent> cacheList, int limit);

    public void add(StreamEvent item);

    public void delete(StreamEvent item);

    public void read(StreamEvent item);

    public void update(StreamEvent item);


}
