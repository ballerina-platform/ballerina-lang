package org.wso2.siddhi.core.util.collection.executor;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;

import java.util.Set;

/**
 * Created by suho on 6/25/16.
 */
public class ExhaustiveBasicCollectionExecutor implements CollectionExecutor {
    private ExpressionExecutor expressionExecutor;
    private int candidateEventIndex;

    public ExhaustiveBasicCollectionExecutor(ExpressionExecutor expressionExecutor, int candidateEventIndex) {

        this.expressionExecutor = expressionExecutor;
        this.candidateEventIndex = candidateEventIndex;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner candidateEventCloner) {
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
        ComplexEventChunk<StreamEvent> candidateEventChunk = new ComplexEventChunk<StreamEvent>(false);
        candidateEventChunk.add(indexedEventHolder.getAllEvents());

        candidateEventChunk.reset();
        while (candidateEventChunk.hasNext()) {
            StreamEvent candidateEvent = candidateEventChunk.next();
            matchingEvent.setEvent(candidateEventIndex, candidateEvent);
            if ((Boolean) expressionExecutor.execute(matchingEvent)) {
                returnEventChunk.add(candidateEventCloner.copyStreamEvent(candidateEvent));
            }
            matchingEvent.setEvent(candidateEventIndex, null);
        }
        return returnEventChunk.getFirst();
    }

    public Set<StreamEvent> findEventSet(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        return null;
    }
}
