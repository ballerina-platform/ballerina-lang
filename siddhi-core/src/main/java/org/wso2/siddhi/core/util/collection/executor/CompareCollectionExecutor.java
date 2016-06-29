package org.wso2.siddhi.core.util.collection.executor;

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;
import org.wso2.siddhi.query.api.expression.condition.Compare;

import java.util.Set;

/**
 * Created by suho on 6/25/16.
 */
public class CompareCollectionExecutor implements CollectionExecutor {


    private final String attribute;
    private final Compare.Operator operator;
    private final ExpressionExecutor valueExpressionExecutor;

    public CompareCollectionExecutor(String attribute, Compare.Operator operator, ExpressionExecutor valueExpressionExecutor) {

        this.attribute = attribute;
        this.operator = operator;
        this.valueExpressionExecutor = valueExpressionExecutor;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner candidateEventCloner) {
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
        ComplexEventChunk<StreamEvent> candidateEventChunk = new ComplexEventChunk<StreamEvent>(false);
        candidateEventChunk.add(indexedEventHolder.findAllEvents(attribute, operator, valueExpressionExecutor.execute(matchingEvent)));
        candidateEventChunk.reset();
        while (candidateEventChunk.hasNext()) {
            returnEventChunk.add(candidateEventCloner.copyStreamEvent(candidateEventChunk.next()));
        }
        return returnEventChunk.getFirst();
    }

    public Set<StreamEvent> findEventSet(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        return indexedEventHolder.findEventSet(attribute, operator, valueExpressionExecutor.execute(matchingEvent));
    }

}
