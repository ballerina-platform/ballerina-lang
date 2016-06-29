package org.wso2.siddhi.core.util.collection.executor;

import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;

import java.util.HashSet;
import java.util.Set;

public class CompareExhaustiveAndCollectionExecutor implements CollectionExecutor {

    private final CollectionExecutor compareCollectionExecutor;
    private CollectionExecutor aCollectionExecutor;

    public CompareExhaustiveAndCollectionExecutor(CollectionExecutor compareCollectionExecutor, CollectionExecutor aCollectionExecutor) {
        this.compareCollectionExecutor = compareCollectionExecutor;
        this.aCollectionExecutor = aCollectionExecutor;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner candidateEventCloner) {
        Set<StreamEvent> compareStreamEvents = findEventSet(matchingEvent, indexedEventHolder);
        if (compareStreamEvents == null) {
            return aCollectionExecutor.find(matchingEvent, indexedEventHolder, candidateEventCloner);
        } else {
            return null;
        }
    }

    public Set<StreamEvent> findEventSet(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> compareStreamEvents = compareCollectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
        if (compareStreamEvents == null || compareStreamEvents.size() > 0) {
            return null;
        } else {
            return new HashSet<StreamEvent>();
        }
    }

    @Override
    public boolean contains(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> compareStreamEvents = findEventSet(matchingEvent, indexedEventHolder);
        if (compareStreamEvents == null) {
            return aCollectionExecutor.contains(matchingEvent, indexedEventHolder);
        } else {
            return compareStreamEvents.size() > 0;
        }
    }

    @Override
    public void delete(StateEvent deletingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> compareStreamEvents = findEventSet(deletingEvent, indexedEventHolder);
        if (compareStreamEvents == null) {
             aCollectionExecutor.delete(deletingEvent, indexedEventHolder);
        }
    }

}
