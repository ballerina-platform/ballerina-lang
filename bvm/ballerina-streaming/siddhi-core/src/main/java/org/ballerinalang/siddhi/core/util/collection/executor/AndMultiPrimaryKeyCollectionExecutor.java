/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.siddhi.core.util.collection.executor;

import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.state.StateEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventCloner;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.table.holder.IndexedEventHolder;
import org.ballerinalang.siddhi.core.util.SiddhiConstants;
import org.ballerinalang.siddhi.query.api.expression.condition.Compare;

import java.util.Collection;
import java.util.List;

/**
 * Implementation of {@link CollectionExecutor}.
 */
public class AndMultiPrimaryKeyCollectionExecutor implements CollectionExecutor {


    private final String compositePrimaryKey;
    private final List<ExpressionExecutor> multiPrimaryKeyExpressionExecutors;

    public AndMultiPrimaryKeyCollectionExecutor(String compositePrimaryKey,
                                                List<ExpressionExecutor> multiPrimaryKeyExpressionExecutors) {
        this.compositePrimaryKey = compositePrimaryKey;
        this.multiPrimaryKeyExpressionExecutors = multiPrimaryKeyExpressionExecutors;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner
            storeEventCloner) {

        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
        Collection<StreamEvent> storeEventSet = findEvents(matchingEvent, indexedEventHolder);

        if (storeEventSet == null) {
            return returnEventChunk.getFirst();
        } else {
            for (StreamEvent storeEvent : storeEventSet) {
                if (storeEventCloner != null) {
                    returnEventChunk.add(storeEventCloner.copyStreamEvent(storeEvent));
                } else {
                    returnEventChunk.add(storeEvent);
                }
            }
            return returnEventChunk.getFirst();
        }
    }

    public Collection<StreamEvent> findEvents(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        return indexedEventHolder.findEvents(compositePrimaryKey, Compare.Operator.EQUAL,
                constructPrimaryKeyValue(matchingEvent, multiPrimaryKeyExpressionExecutors));
    }

    @Override
    public boolean contains(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        return indexedEventHolder.containsEventSet(compositePrimaryKey, Compare.Operator.EQUAL,
                constructPrimaryKeyValue(matchingEvent, multiPrimaryKeyExpressionExecutors));
    }

    @Override
    public void delete(StateEvent deletingEvent, IndexedEventHolder indexedEventHolder) {
        indexedEventHolder.delete(compositePrimaryKey, Compare.Operator.EQUAL,
                constructPrimaryKeyValue(deletingEvent, multiPrimaryKeyExpressionExecutors));
    }

    @Override
    public Cost getDefaultCost() {
        return Cost.SINGLE_RETURN_INDEX_MATCHING;
    }

    private Object constructPrimaryKeyValue(StateEvent matchingEvent,
                                            List<ExpressionExecutor> multiPrimaryKeyExpressionExecutors) {
        if (multiPrimaryKeyExpressionExecutors.size() == 1) {
            return multiPrimaryKeyExpressionExecutors.get(0).execute(matchingEvent);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (ExpressionExecutor expressionExecutor : multiPrimaryKeyExpressionExecutors) {
                stringBuilder.append(expressionExecutor.execute(matchingEvent))
                        .append(SiddhiConstants.KEY_DELIMITER);
            }
            return stringBuilder.toString();
        }
    }
}
