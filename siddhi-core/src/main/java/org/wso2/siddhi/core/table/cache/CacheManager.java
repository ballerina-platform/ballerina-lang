package org.wso2.siddhi.core.table.cache;

import org.wso2.siddhi.core.event.StreamEvent;

public interface CacheManager {

    public void add(StreamEvent item);

    public void delete(StreamEvent item);

    public void read(StreamEvent item);

    public void update(StreamEvent item);


}
