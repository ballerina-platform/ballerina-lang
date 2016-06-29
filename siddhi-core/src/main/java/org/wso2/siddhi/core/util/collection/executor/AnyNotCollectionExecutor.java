package org.wso2.siddhi.core.util.collection.executor;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;

import java.util.Set;

/**
 * Created by suho on 6/25/16.
 */
public class AnyNotCollectionExecutor implements CollectionExecutor {


    private final CollectionExecutor notCollectionExecutor;
    private final ExhaustiveBasicCollectionExecutor exhaustiveBasicCollectionExecutor;

    public AnyNotCollectionExecutor(CollectionExecutor notCollectionExecutor, ExhaustiveBasicCollectionExecutor exhaustiveBasicCollectionExecutor) {

        this.notCollectionExecutor = notCollectionExecutor;
        this.exhaustiveBasicCollectionExecutor = exhaustiveBasicCollectionExecutor;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner candidateEventCloner) {

        Set<StreamEvent> notStreamEvents = notCollectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
        if (notStreamEvents == null) {
            return exhaustiveBasicCollectionExecutor.find(matchingEvent, indexedEventHolder, candidateEventCloner);
        } else if (notStreamEvents.size() == 0) {
            return indexedEventHolder.getAllEvents();
        } else {
            Set<StreamEvent> returnSet = indexedEventHolder.getAllEventSet();
            for (StreamEvent aStreamEvent : notStreamEvents) {
                returnSet.remove(aStreamEvent);
            }
            ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
            for (StreamEvent resultEvent : returnSet) {
                returnEventChunk.add(candidateEventCloner.copyStreamEvent(resultEvent));
            }
            return returnEventChunk.getFirst();
        }
    }

    public Set<StreamEvent> findEventSet(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> notStreamEvents = notCollectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
        if (notStreamEvents == null) {
            return null;
        } else if (notStreamEvents.size() == 0) {
            return indexedEventHolder.getAllEventSet();
        } else {
            Set<StreamEvent> returnSet = indexedEventHolder.getAllEventSet();
            for (StreamEvent aStreamEvent : notStreamEvents) {
                returnSet.remove(aStreamEvent);
            }
            return returnSet;
        }
    }

}
