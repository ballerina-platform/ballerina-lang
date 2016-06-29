package org.wso2.siddhi.core.util.collection.executor;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;

import java.util.Set;

public class OrCollectionExecutor implements CollectionExecutor {


    private final CollectionExecutor rightCollectionExecutor;
    private final CollectionExecutor leftCollectionExecutor;
    private CollectionExecutor exhaustiveCollectionExecutor;

    public OrCollectionExecutor(CollectionExecutor leftCollectionExecutor, CollectionExecutor rightCollectionExecutor, CollectionExecutor exhaustiveCollectionExecutor) {

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
        if (leftStreamEvents == null) {
            return null;
        } else {
            Set<StreamEvent> rightStreamEvents = rightCollectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
            if (rightStreamEvents == null) {
                return null;

            } else {
                leftStreamEvents.addAll(rightStreamEvents);
                return leftStreamEvents;
            }
        }
    }

    @Override
    public boolean contains(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> leftStreamEvents = leftCollectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
        if (leftStreamEvents != null && leftStreamEvents.size() > 0) {
            return true;
        }

        Set<StreamEvent> rightStreamEvents = rightCollectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
        if (rightStreamEvents != null && rightStreamEvents.size() > 0) {
            return true;
        }

        return exhaustiveCollectionExecutor.contains(matchingEvent, indexedEventHolder);
    }

    @Override
    public void delete(StateEvent deletingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> leftStreamEvents = leftCollectionExecutor.findEventSet(deletingEvent, indexedEventHolder);
        if (leftStreamEvents == null) {
            exhaustiveCollectionExecutor.delete(deletingEvent, indexedEventHolder);
        } else {
            Set<StreamEvent> rightStreamEvents = rightCollectionExecutor.findEventSet(deletingEvent, indexedEventHolder);
            if (rightStreamEvents == null) {
                exhaustiveCollectionExecutor.delete(deletingEvent, indexedEventHolder);
            } else {
                leftCollectionExecutor.delete(deletingEvent, indexedEventHolder);
                rightCollectionExecutor.delete(deletingEvent, indexedEventHolder);
            }
        }
    }

}
