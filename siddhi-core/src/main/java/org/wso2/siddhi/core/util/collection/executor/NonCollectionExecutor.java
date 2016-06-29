package org.wso2.siddhi.core.util.collection.executor;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;

import java.util.HashSet;
import java.util.Set;

public class NonCollectionExecutor implements CollectionExecutor {
    private ExpressionExecutor expressionExecutor;

    public NonCollectionExecutor(ExpressionExecutor expressionExecutor) {

        this.expressionExecutor = expressionExecutor;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner candidateEventCloner) {

        if ((Boolean) expressionExecutor.execute(matchingEvent)) {

            ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
            Set<StreamEvent> candidateEventSet = indexedEventHolder.getAllEventSet();

            for (StreamEvent candidateEvent : candidateEventSet) {
                if (candidateEventCloner != null) {
                    returnEventChunk.add(candidateEventCloner.copyStreamEvent(candidateEvent));
                } else {
                    returnEventChunk.add(candidateEvent);
                }
            }
            return returnEventChunk.getFirst();
        } else {
            return null;
        }
    }

    public Set<StreamEvent> findEventSet(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {

        if ((Boolean) expressionExecutor.execute(matchingEvent)) {
            return indexedEventHolder.getAllEventSet();
        } else {
            return new HashSet<StreamEvent>();
        }
    }

    @Override
    public boolean contains(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        return (Boolean) expressionExecutor.execute(matchingEvent);
    }

    @Override
    public void delete(StateEvent deletingEvent, IndexedEventHolder indexedEventHolder) {
        if ((Boolean) expressionExecutor.execute(deletingEvent)) {
            indexedEventHolder.deleteAll();
        }
    }
}
