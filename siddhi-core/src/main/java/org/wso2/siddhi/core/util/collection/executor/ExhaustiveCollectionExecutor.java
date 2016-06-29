package org.wso2.siddhi.core.util.collection.executor;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;

import java.util.Iterator;
import java.util.Set;

public class ExhaustiveCollectionExecutor implements CollectionExecutor {
    private ExpressionExecutor expressionExecutor;
    private int candidateEventIndex;

    public ExhaustiveCollectionExecutor(ExpressionExecutor expressionExecutor, int candidateEventIndex) {

        this.expressionExecutor = expressionExecutor;
        this.candidateEventIndex = candidateEventIndex;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner candidateEventCloner) {
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
        Set<StreamEvent> candidateEventSet = indexedEventHolder.getAllEventSet();

        for (StreamEvent candidateEvent : candidateEventSet) {
            matchingEvent.setEvent(candidateEventIndex, candidateEvent);
            if ((Boolean) expressionExecutor.execute(matchingEvent)) {
                if (candidateEventCloner != null) {
                    returnEventChunk.add(candidateEventCloner.copyStreamEvent(candidateEvent));
                } else {
                    returnEventChunk.add(candidateEvent);
                }
            }
            matchingEvent.setEvent(candidateEventIndex, null);
        }
        return returnEventChunk.getFirst();
    }

    public Set<StreamEvent> findEventSet(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        return null;
    }

    @Override
    public boolean contains(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> candidateEventSet = indexedEventHolder.getAllEventSet();

        for (StreamEvent candidateEvent : candidateEventSet) {
            matchingEvent.setEvent(candidateEventIndex, candidateEvent);
            try {
                if ((Boolean) expressionExecutor.execute(matchingEvent)) {
                    return true;
                }
            } finally {
                matchingEvent.setEvent(candidateEventIndex, null);
            }
        }
        return false;
    }

    @Override
    public void delete(StateEvent deletingEvent, IndexedEventHolder indexedEventHolder) {
        Set<StreamEvent> candidateEventSet = indexedEventHolder.getAllEventSet();

        for (Iterator<StreamEvent> iterator = candidateEventSet.iterator(); iterator.hasNext(); ) {
            StreamEvent candidateEvent = iterator.next();
            deletingEvent.setEvent(candidateEventIndex, candidateEvent);
            if ((Boolean) expressionExecutor.execute(deletingEvent)) {
                iterator.remove();
            }
            deletingEvent.setEvent(candidateEventIndex, null);
        }
        indexedEventHolder.deleteAll(candidateEventSet);
    }
}
