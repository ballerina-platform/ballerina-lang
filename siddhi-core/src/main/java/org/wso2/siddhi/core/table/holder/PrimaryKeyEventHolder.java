package org.wso2.siddhi.core.table.holder;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.query.api.expression.condition.Compare;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class PrimaryKeyEventHolder implements IndexedEventHolder {

    private StreamEventPool tableStreamEventPool;
    private StreamEventConverter eventConverter;
    private int indexPosition;
    private String indexAttribute;
    private TreeMap<Object, StreamEvent> data = new TreeMap<Object, StreamEvent>();

    public PrimaryKeyEventHolder(StreamEventPool tableStreamEventPool, StreamEventConverter eventConverter, int indexPosition, String indexAttribute) {
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
            data.put(streamEvent.getOutputData()[indexPosition], streamEvent);
        }
    }

    @Override
    public boolean isSupportedIndex(String attribute, Compare.Operator operator) {
        return indexAttribute.equalsIgnoreCase(attribute) &&
                (operator == Compare.Operator.EQUAL || operator == Compare.Operator.NOT_EQUAL);
    }

    @Override
    public boolean isAttributeIndexed(String attribute) {
        return indexAttribute.equalsIgnoreCase(attribute);
    }

    @Override
    public Set<StreamEvent> getAllEventSet() {
        return new HashSet<StreamEvent>(data.values());
    }

    @Override
    public Set<StreamEvent> findEventSet(String attribute, Compare.Operator operator, Object value) {
        if (operator == Compare.Operator.EQUAL) {
            Set<StreamEvent> streamEventSet = new HashSet<StreamEvent>();
            StreamEvent resultEvent = data.get(value);
            if (resultEvent != null) {
                streamEventSet.add(resultEvent);
            }
            return streamEventSet;
        } else if (operator == Compare.Operator.NOT_EQUAL) {
            Set<StreamEvent> streamEventSet;
            if (data.size() > 0) {
                streamEventSet = new HashSet<StreamEvent>(data.values());
            } else {
                streamEventSet = new HashSet<StreamEvent>();
            }

            StreamEvent resultEvent = data.get(value);
            if (resultEvent != null) {
                streamEventSet.remove(resultEvent);
            }
            return streamEventSet;
        } else {
            throw new OperationNotSupportedException(operator + " not supported by " + getClass().getName());
        }
    }

    @Override
    public void deleteAll() {
        data.clear();
    }

    @Override
    public void deleteAll(Set<StreamEvent> candidateEventSet) {
        for (StreamEvent streamEvent : candidateEventSet) {
            data.remove(streamEvent.getOutputData()[indexPosition]);
        }
    }

    @Override
    public void delete(String attribute, Compare.Operator operator, Object value) {
        if (operator == Compare.Operator.EQUAL) {
            data.remove(value);
        } else if (operator == Compare.Operator.NOT_EQUAL) {
            StreamEvent streamEvent = data.get(value);
            deleteAll();
            if (streamEvent != null) {
                data.put(value, streamEvent);
            }
        } else {
            throw new OperationNotSupportedException(operator + " not supported by " + getClass().getName());
        }
    }
}
