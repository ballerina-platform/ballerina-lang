package org.wso2.siddhi.core.table.holder;

import org.wso2.siddhi.core.event.stream.StreamEvent;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by suho on 5/22/16.
 */
public class MapEventHolder implements EventHolder {
    private SortedMap<Object, StreamEvent> eventMap = new TreeMap<Object, StreamEvent>();

    @Override
    public Object getEventCollection() {
        return eventMap.values();
    }

    public SortedMap<Object, StreamEvent> getEventMap() {
        return eventMap;
    }
}
