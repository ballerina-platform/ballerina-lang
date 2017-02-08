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
        Collection<StreamEvent> candidateEvents = indexedEventHolder.getAllEvents();

        for (StreamEvent candidateEvent : candidateEvents) {
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

    public Collection<StreamEvent> findEvents(StateEvent matchingEvent, Collection<StreamEvent> preProcessedCandidateEvents) {
        HashSet<StreamEvent> streamEvents = new HashSet<StreamEvent>();
        for (Iterator<StreamEvent> iterator = preProcessedCandidateEvents.iterator(); iterator.hasNext(); ) {
            StreamEvent candidateEvent = iterator.next();
            matchingEvent.setEvent(candidateEventIndex, candidateEvent);
            if ((Boolean) expressionExecutor.execute(matchingEvent)) {
                streamEvents.add(candidateEvent);
            }
            matchingEvent.setEvent(candidateEventIndex, null);
        }
        return streamEvents;
    }

    public Set<StreamEvent> findEvents(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        return null;
    }

    @Override
    public boolean contains(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Collection<StreamEvent> candidateEvents = indexedEventHolder.getAllEvents();

        for (StreamEvent candidateEvent : candidateEvents) {
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
        Collection<StreamEvent> candidateEvents = indexedEventHolder.getAllEvents();
        Set<StreamEvent> toDeleteEvents = new HashSet<StreamEvent>();
        for (StreamEvent candidateEvent : candidateEvents) {
            deletingEvent.setEvent(candidateEventIndex, candidateEvent);
            if ((Boolean) expressionExecutor.execute(deletingEvent)) {
                toDeleteEvents.add(candidateEvent);
            }
            deletingEvent.setEvent(candidateEventIndex, null);
        }
        indexedEventHolder.deleteAll(toDeleteEvents);
    }

    @Override
    public Cost getDefaultCost() {
        return Cost.EXHAUSTIVE;
    }
}
