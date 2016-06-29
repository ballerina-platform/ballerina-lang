package org.wso2.siddhi.core.util.collection.executor;

import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;

import java.util.Set;

/**
 * Created by suho on 6/25/16.
 */
public interface CollectionExecutor {

    /**
     * Find the Events matching to the condition, used on the primary call
     *
     * @param matchingEvent        matching input event
     * @param indexedEventHolder   indexed EventHolder containing data
     * @param candidateEventCloner candidate event cloner
     * @return matched StreamEvent, null if no events matched.
     */
    StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner candidateEventCloner);

    /**
     * Find the Events matching to the condition, used for consecutive calls from parent CollectionExecutor
     *
     * @param matchingEvent      matching input event
     * @param indexedEventHolder indexed EventHolder containing data
     * @return matched events as Set, null if Exhaustive processing need to be done.
     */
    Set<StreamEvent> findEventSet(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder);
}
