/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.util.collection.executor;

import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;
import org.wso2.siddhi.core.util.collection.expression.CollectionExpression;

import java.util.HashSet;
import java.util.Set;

public class NonAndCollectionExecutor implements CollectionExecutor {


    private final CollectionExecutor collectionExecutor;
    private final CollectionExpression.CollectionScope collectionScope;
    private final ExpressionExecutor valueExpressionExecutor;

    public NonAndCollectionExecutor(ExpressionExecutor valueExpressionExecutor, CollectionExecutor aCollectionExecutor, CollectionExpression.CollectionScope collectionScope) {

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

    @Override
    public boolean contains(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        return (Boolean) valueExpressionExecutor.execute(matchingEvent) && collectionExecutor.contains(matchingEvent, indexedEventHolder);
    }

    @Override
    public void delete(StateEvent deletingEvent, IndexedEventHolder indexedEventHolder) {
        if ((Boolean) valueExpressionExecutor.execute(deletingEvent)) {
            collectionExecutor.delete(deletingEvent, indexedEventHolder);
        }
    }

}
