package org.wso2.siddhi.core.table.holder;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;

/**
 * Created by suho on 5/22/16.
 */
public interface EventHolder {
    void add(ComplexEventChunk<StreamEvent> addingEventChunk);
}
