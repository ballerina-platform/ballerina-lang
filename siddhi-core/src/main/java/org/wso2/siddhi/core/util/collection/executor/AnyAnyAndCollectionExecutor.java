package org.wso2.siddhi.core.util.collection.executor;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by suho on 6/25/16.
 */
public class AnyAnyAndCollectionExecutor implements CollectionExecutor {


    private final CollectionExecutor rightCollectionExecutor;
    private final CollectionExecutor leftCollectionExecutor;
    private CollectionExecutor aCollectionExecutor;

    public AnyAnyAndCollectionExecutor(CollectionExecutor leftCollectionExecutor, CollectionExecutor rightCollectionExecutor, CollectionExecutor aCollectionExecutor) {

        this.leftCollectionExecutor = leftCollectionExecutor;
        this.rightCollectionExecutor = rightCollectionExecutor;
        this.aCollectionExecutor = aCollectionExecutor;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner candidateEventCloner) {

        Set<StreamEvent> resultEventSet = findEventSet(matchingEvent, indexedEventHolder);
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);

        if (resultEventSet != null) {
            for (StreamEvent resultEvent : resultEventSet) {
                returnEventChunk.add(candidateEventCloner.copyStreamEvent(resultEvent));
            }
            return returnEventChunk.getFirst();
        } else {
            return aCollectionExecutor.find(matchingEvent, indexedEventHolder, candidateEventCloner);
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

}
