package org.wso2.siddhi.core.util.collection.executor;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by suho on 6/25/16.
 */
public class NonBasicCollectionExecutor implements CollectionExecutor {
    private ExpressionExecutor expressionExecutor;

    public NonBasicCollectionExecutor(ExpressionExecutor expressionExecutor) {

        this.expressionExecutor = expressionExecutor;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner candidateEventCloner) {


        if ((Boolean) expressionExecutor.execute(matchingEvent)) {
            ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
            ComplexEventChunk<StreamEvent> candidateEventChunk = new ComplexEventChunk<StreamEvent>(false);
            candidateEventChunk.add(indexedEventHolder.getAllEvents());

            candidateEventChunk.reset();
            while (candidateEventChunk.hasNext()) {
                returnEventChunk.add(candidateEventCloner.copyStreamEvent(candidateEventChunk.next()));
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
}
