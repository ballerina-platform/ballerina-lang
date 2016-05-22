package org.wso2.siddhi.core.table.holder;

import org.wso2.siddhi.core.event.stream.StreamEvent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by suho on 5/22/16.
 */
public class ListEventHolder implements EventHolder {
    private List<StreamEvent> eventList = new LinkedList<StreamEvent>();

    public Object getEventCollection() {
        return eventList;
    }

    public List<StreamEvent> getEventList() {
        return eventList;
    }
}
