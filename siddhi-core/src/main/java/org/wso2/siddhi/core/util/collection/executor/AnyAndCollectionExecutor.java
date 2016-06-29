package org.wso2.siddhi.core.util.collection.executor;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;

import java.util.HashSet;
import java.util.Set;

public class AnyAndCollectionExecutor implements CollectionExecutor {


    private final CollectionExecutor rightCollectionExecutor;
    private final CollectionExecutor leftCollectionExecutor;
    private CollectionExecutor exhaustiveCollectionExecutor;

    public AnyAndCollectionExecutor(CollectionExecutor leftCollectionExecutor, CollectionExecutor rightCollectionExecutor, CollectionExecutor exhaustiveCollectionExecutor) {

        this.leftCollectionExecutor = leftCollectionExecutor;
        this.rightCollectionExecutor = rightCollectionExecutor;
        this.exhaustiveCollectionExecutor = exhaustiveCollectionExecutor;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner candidateEventCloner) {

        Set<StreamEvent> resultEventSet = findEventSet(matchingEvent, indexedEventHolder);
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);

        if (resultEventSet != null) {
            for (StreamEvent resultEvent : resultEventSet) {
                if (candidateEventCloner != null) {
                    returnEventChunk.add(candidateEventCloner.copyStreamEvent(resultEvent));
                } else {
                    returnEventChunk.add(resultEvent);
                }
            }
            return returnEventChunk.getFirst();
        } else {
            return exhaustiveCollectionExecutor.find(matchingEvent, indexedEventHolder, candidateEventCloner);
        }
    }

    public Set<StreamEvent> findEventSet(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> leftStreamEvents = leftCollectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
        if (leftStreamEvents == null || (leftStreamEvents.size() > 0)) {
            return null;
        } else {
            Set<StreamEvent> rightStreamEvents = rightCollectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
            if (rightStreamEvents == null || (rightStreamEvents.size() > 0)) {
                return null;
            } else {
                Set<StreamEvent> returnSet = new HashSet<StreamEvent>();
                for (StreamEvent aStreamEvent : leftStreamEvents) {
                    if (rightStreamEvents.contains(aStreamEvent)) {
                        returnSet.add(aStreamEvent);
                    }
                }
                return returnSet;
            }
        }
    }

    @Override
    public boolean contains(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> resultEventSet = findEventSet(matchingEvent, indexedEventHolder);
        if (resultEventSet != null) {
            return resultEventSet.size() > 0;
        } else {
            return exhaustiveCollectionExecutor.contains(matchingEvent, indexedEventHolder);
        }
    }

    @Override
    public void delete(StateEvent deletingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> resultEventSet = findEventSet(deletingEvent, indexedEventHolder);
        if (resultEventSet != null) {
            indexedEventHolder.deleteAll(resultEventSet);
        } else {
            exhaustiveCollectionExecutor.delete(deletingEvent, indexedEventHolder);
        }
    }

}
