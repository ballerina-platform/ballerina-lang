package org.wso2.siddhi.core.table.holder;

import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.query.api.expression.condition.Compare;

import java.util.Set;

public interface IndexedEventHolder extends EventHolder {

    boolean isAttributeIndexed(String attribute);

    boolean isSupportedIndex(String attribute, Compare.Operator operator);

    Set<StreamEvent> getAllEventSet();

    Set<StreamEvent> findEventSet(String attribute, Compare.Operator operator, Object value);

    void deleteAll();

    void deleteAll(Set<StreamEvent> candidateEventSet);

    void delete(String attribute, Compare.Operator operator, Object value);
}
