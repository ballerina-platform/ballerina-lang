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

import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link CollectionExecutor}
 */
public class ExhaustiveCollectionExecutor implements CollectionExecutor {
    private ExpressionExecutor expressionExecutor;
    private int storeEventIndex;

    public ExhaustiveCollectionExecutor(ExpressionExecutor expressionExecutor, int storeEventIndex) {

        this.expressionExecutor = expressionExecutor;
        this.storeEventIndex = storeEventIndex;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner
            storeEventCloner) {
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
        Collection<StreamEvent> storeEvents = indexedEventHolder.getAllEvents();

        for (StreamEvent storeEvent : storeEvents) {
            matchingEvent.setEvent(storeEventIndex, storeEvent);
            if ((Boolean) expressionExecutor.execute(matchingEvent)) {
                if (storeEventCloner != null) {
                    returnEventChunk.add(storeEventCloner.copyStreamEvent(storeEvent));
                } else {
                    returnEventChunk.add(storeEvent);
                }
            }
            matchingEvent.setEvent(storeEventIndex, null);
        }
        return returnEventChunk.getFirst();
    }

    public Collection<StreamEvent> findEvents(StateEvent matchingEvent, Collection<StreamEvent>
            preProcessedstoreEvents) {
        HashSet<StreamEvent> streamEvents = new HashSet<StreamEvent>();
        for (StreamEvent storeEvent : preProcessedstoreEvents) {
            matchingEvent.setEvent(storeEventIndex, storeEvent);
            if ((Boolean) expressionExecutor.execute(matchingEvent)) {
                streamEvents.add(storeEvent);
            }
            matchingEvent.setEvent(storeEventIndex, null);
        }
        return streamEvents;
    }

    public Set<StreamEvent> findEvents(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        return null;
    }

    @Override
    public boolean contains(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Collection<StreamEvent> storeEvents = indexedEventHolder.getAllEvents();

        for (StreamEvent storeEvent : storeEvents) {
            matchingEvent.setEvent(storeEventIndex, storeEvent);
            try {
                if ((Boolean) expressionExecutor.execute(matchingEvent)) {
                    return true;
                }
            } finally {
                matchingEvent.setEvent(storeEventIndex, null);
            }
        }
        return false;
    }

    @Override
    public void delete(StateEvent deletingEvent, IndexedEventHolder indexedEventHolder) {
        Collection<StreamEvent> storeEvents = indexedEventHolder.getAllEvents();
        Set<StreamEvent> toDeleteEvents = new HashSet<StreamEvent>();
        for (StreamEvent storeEvent : storeEvents) {
            deletingEvent.setEvent(storeEventIndex, storeEvent);
            if ((Boolean) expressionExecutor.execute(deletingEvent)) {
                toDeleteEvents.add(storeEvent);
            }
            deletingEvent.setEvent(storeEventIndex, null);
        }
        indexedEventHolder.deleteAll(toDeleteEvents);
    }

    @Override
    public Cost getDefaultCost() {
        return Cost.EXHAUSTIVE;
    }
}
