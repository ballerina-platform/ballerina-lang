package org.wso2.siddhi.core.table.holder;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.query.api.expression.condition.Compare;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created by suho on 5/22/16.
 */
public class IndexedEventHolder extends TreeMap<Object, StreamEvent> implements EventHolder {

    private StreamEventPool tableStreamEventPool;
    private StreamEventConverter eventConverter;
    private int indexPosition;
    private String indexAttribute;
    private StreamEvent allStreamEventSet;

    public IndexedEventHolder(StreamEventPool tableStreamEventPool, StreamEventConverter eventConverter, int indexPosition, String indexAttribute) {
        this.tableStreamEventPool = tableStreamEventPool;
        this.eventConverter = eventConverter;
        this.indexPosition = indexPosition;
        this.indexAttribute = indexAttribute;
    }

    @Override
    public void add(ComplexEventChunk<StreamEvent> addingEventChunk) {
        addingEventChunk.reset();
        while (addingEventChunk.hasNext()) {
            ComplexEvent complexEvent = addingEventChunk.next();
            StreamEvent streamEvent = tableStreamEventPool.borrowEvent();
            eventConverter.convertComplexEvent(complexEvent, streamEvent);
            this.put(streamEvent.getOutputData()[indexPosition], streamEvent);
        }
    }

    public String getIndexAttribute() {
        return indexAttribute;
    }

    public int getIndexPosition() {
        return indexPosition;
    }

    public StreamEvent getAllEvents() {
        return null;
    }

    public StreamEvent findAllEvents(String attribute, Compare.Operator operator, Object value) {
        return null;
    }

    public Set<StreamEvent> getAllEventSet() {
        return null;
    }

    public Set<StreamEvent> findEventSet(String attribute, Compare.Operator operator, Object value) {
        return null;
    }
}
