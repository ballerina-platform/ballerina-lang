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
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link CollectionExecutor} which handles not condition.
 */
public class NotCollectionExecutor implements CollectionExecutor {


    private final CollectionExecutor notCollectionExecutor;
    private final ExhaustiveCollectionExecutor exhaustiveCollectionExecutor;

    public NotCollectionExecutor(CollectionExecutor notCollectionExecutor, ExhaustiveCollectionExecutor
            exhaustiveCollectionExecutor) {

        this.notCollectionExecutor = notCollectionExecutor;
        this.exhaustiveCollectionExecutor = exhaustiveCollectionExecutor;
    }

    public StreamEvent find(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder, StreamEventCloner
            storeEventCloner) {

        Collection<StreamEvent> notStreamEvents = notCollectionExecutor.findEvents(matchingEvent, indexedEventHolder);
        if (notStreamEvents == null) {
            return exhaustiveCollectionExecutor.find(matchingEvent, indexedEventHolder, storeEventCloner);
        } else if (notStreamEvents.size() == 0) {
            ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
            Collection<StreamEvent> storeEventSet = indexedEventHolder.getAllEvents();

            for (StreamEvent storeEvent : storeEventSet) {
                if (storeEventCloner != null) {
                    returnEventChunk.add(storeEventCloner.copyStreamEvent(storeEvent));
                } else {
                    returnEventChunk.add(storeEvent);
                }
            }
            return returnEventChunk.getFirst();
        } else {
            Collection<StreamEvent> allEvents = indexedEventHolder.getAllEvents();
            ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
            for (StreamEvent aEvent : allEvents) {
                if (!notStreamEvents.contains(aEvent)) {
                    if (storeEventCloner != null) {
                        returnEventChunk.add(storeEventCloner.copyStreamEvent(aEvent));
                    } else {
                        returnEventChunk.add(aEvent);
                    }
                }
            }
            return returnEventChunk.getFirst();
        }
    }

    public Collection<StreamEvent> findEvents(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Collection<StreamEvent> notStreamEvents = notCollectionExecutor.findEvents(matchingEvent, indexedEventHolder);
        if (notStreamEvents == null) {
            return null;
        } else if (notStreamEvents.size() == 0) {
            return indexedEventHolder.getAllEvents();
        } else {
            Collection<StreamEvent> allEvents = indexedEventHolder.getAllEvents();
            Set<StreamEvent> returnSet = new HashSet<StreamEvent>();
            for (StreamEvent aEvent : allEvents) {
                if (!notStreamEvents.contains(aEvent)) {
                    returnSet.add(aEvent);
                }
            }
            return returnSet;
        }
    }

    @Override
    public boolean contains(StateEvent matchingEvent, IndexedEventHolder indexedEventHolder) {
        Collection<StreamEvent> notStreamEvents = notCollectionExecutor.findEvents(matchingEvent, indexedEventHolder);
        if (notStreamEvents == null) {
            return exhaustiveCollectionExecutor.contains(matchingEvent, indexedEventHolder);
        } else {
            return notStreamEvents.size() != indexedEventHolder.getAllEvents().size();
        }
    }

    @Override
    public void delete(StateEvent deletingEvent, IndexedEventHolder indexedEventHolder) {
        Collection<StreamEvent> notStreamEvents = notCollectionExecutor.findEvents(deletingEvent, indexedEventHolder);
        if (notStreamEvents == null) {
            exhaustiveCollectionExecutor.delete(deletingEvent, indexedEventHolder);
        } else if (notStreamEvents.size() == 0) {

            indexedEventHolder.deleteAll();
        } else if (notStreamEvents.size() != indexedEventHolder.getAllEvents().size()) {
            Collection<StreamEvent> allEvents = indexedEventHolder.getAllEvents();
            Set<StreamEvent> returnSet = new HashSet<StreamEvent>();
            for (StreamEvent aEvent : allEvents) {
                if (!notStreamEvents.contains(aEvent)) {
                    returnSet.add(aEvent);
                }
            }
            indexedEventHolder.deleteAll(returnSet);
        }
    }

    @Override
    public Cost getDefaultCost() {
        if (notCollectionExecutor.getDefaultCost() == Cost.EXHAUSTIVE) {
            return Cost.EXHAUSTIVE;
        } else {
            return Cost.MULTI_RETURN_INDEX_MATCHING;
        }
    }

}
