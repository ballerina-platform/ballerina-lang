package org.wso2.siddhi.core.util.collection.executor;

import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;
import org.wso2.siddhi.core.util.collection.expression.CollectionExpression;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by suho on 6/25/16.
 */
public class NonAnyAndCollectionExecutor implements CollectionExecutor {


    private final CollectionExecutor collectionExecutor;
    private final CollectionExpression.CollectionScope collectionScope;
    private final ExpressionExecutor valueExpressionExecutor;

    public NonAnyAndCollectionExecutor(ExpressionExecutor valueExpressionExecutor, CollectionExecutor aCollectionExecutor, CollectionExpression.CollectionScope collectionScope) {

        this.valueExpressionExecutor = valueExpressionExecutor;
        collectionExecutor = aCollectionExecutor;
        this.collectionScope = collectionScope;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner candidateEventCloner) {
        if ((Boolean) valueExpressionExecutor.execute(matchingEvent)) {
            return collectionExecutor.find(matchingEvent, indexedEventHolder, candidateEventCloner);
        } else {
            return null;
        }
    }

    public Set<StreamEvent> findEventSet(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        if ((Boolean) valueExpressionExecutor.execute(matchingEvent)) {
            switch (collectionScope) {
                case NON:
                case INDEXED_ATTRIBUTE:
                case INDEXED_RESULT_SET:
                case OPTIMISED_RESULT_SET:
                    return collectionExecutor.findEventSet(matchingEvent, indexedEventHolder);
                case EXHAUSTIVE:
                    return null;
            }
        } else {
            return new HashSet<StreamEvent>();
        }
        return null;
    }

}
